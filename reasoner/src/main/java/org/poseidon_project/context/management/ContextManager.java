/*Copyright 2015 POSEIDON Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.poseidon_project.context.management;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;

import org.poseidon_project.context.database.ContextDB;
import org.poseidon_project.contexts.ContextObserver;
import org.poseidon_project.contexts.ContextReceiver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

import static android.content.Context.MODE_PRIVATE;

/**
 * Class to handle runtime management of Context observers.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextManager {

    private Context mContext;
    private static final String LOGTAG = "ContextManager";
    private final HashMap<String, ContextObserver> mActiveContexts = new HashMap<>();
    private final HashMap<String, Long> mContextUseIds = new HashMap<>();
    private ContextDB mContextDatabase;
    private ContextReceiver mContextReceiver;
    private ExternalContextReceiver mExternalContextReceiver;

    public ContextManager(Context c) {

        mContext = c;
        mContextReceiver = new POSEIDONReceiver();
        mExternalContextReceiver = new ExternalContextReceiver(this);
        IntentFilter filter = new IntentFilter(mExternalContextReceiver.CONTEXT_INTENT);
        mContext.registerReceiver(mExternalContextReceiver, filter);
    }

    public boolean addObserverRequirement(String appkey, String observerName) {

        ContextObserver observer = mActiveContexts.get(observerName);

        if (observer != null) {
            Log.v(LOGTAG, "Observer already running, adding requirement");
            observer.addRequiringApp(appkey);
            return true;
        } else {
            return loadContextClass(appkey, observerName);
        }
    }

    public boolean removeObserverRequirement(String appkey, String observerName) {
        ContextObserver observer = mActiveContexts.get(observerName);

        if (observer != null) {
            observer.removeRequiringApp(appkey);

            if (observer.numberOfRequiringApps()<1) {
                Log.v(LOGTAG, "Observer no longer needed, shutting down");
                observer.stop();
                mActiveContexts.remove(observerName);
                long cID = mContextUseIds.remove(observerName);
                mContextDatabase.endContextComponentUse(cID, observerName);
                observer = null;
            }

            return true;
        } else {
            Log.v(LOGTAG, "Observer not running! Ignoring");
            return false;
        }
    }

    public void updateReceiverReference(ContextReceiver newReceiver) {

        for (ContextObserver co : mActiveContexts.values()) {
            co.setContextReceiver(newReceiver);
        }

    }


    public void copyDexFile(String appKey, final String newDex,
                            String[] contexts, String packagename, int permission) {

        File dexInternalStoragePath = new File(mContext.getDir("dex",
                        Context.MODE_PRIVATE), newDex);

        String celoc = Environment.getExternalStorageDirectory()
                        + "/Android/data/org.poseidon_project/files/";
        File newDexFile = new File(celoc, newDex);

        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        final int BUF_SIZE = 8 * 1024;

        try {

            bis = new BufferedInputStream(new FileInputStream(newDexFile));
            dexWriter = new BufferedOutputStream(new FileOutputStream(
                            dexInternalStoragePath));

            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0 ) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            Log.v(LOGTAG, "copied dex file: " + newDex);
            for (String c: contexts) {
                mContextDatabase.insertComponent(packagename, c, appKey, permission, newDex);
            }

        } catch (IOException ioe) {
            Log.e(LOGTAG, ioe.getStackTrace().toString());

        }
    }

    protected boolean loadContextClass(String appId, String componentName) {
        List<String> componentInfo = mContextDatabase.getLoadComponentInfo(appId,
                componentName);

        if (componentInfo.size() > 0) {
            return loadContextClass(appId, componentName, componentInfo.get(0),
                    componentInfo.get(1));
        }

        return false;

    }

    private boolean loadContextClass(String appId, String componentName, String dex,
                              String packagename) {

        final File optimizedDexOutputPath = mContext.getDir("outdex",
                        MODE_PRIVATE);
        File dexInternalStoragePath;

        dexInternalStoragePath = new File(mContext.getDir("dex",
                        MODE_PRIVATE), dex);

        DexClassLoader cl = new DexClassLoader(
                        dexInternalStoragePath.getAbsolutePath(),
                        optimizedDexOutputPath.getAbsolutePath(), null,
                        mContext.getClassLoader());

        Class<?> contextObserver  = null;
        Class<?>[] parameterTypes = { Context.class, ContextReceiver.class };

        try {
            //Load the Class
            contextObserver = cl.loadClass(packagename.concat("." + componentName));
            Constructor<?> contextConstructor = contextObserver.getConstructor(parameterTypes);
            ContextObserver context = (ContextObserver) contextConstructor
                    .newInstance(mContext, mContextReceiver);

            context.addRequiringApp(appId);
            context.start();

            long cId = mContextDatabase.startContextComponentUse(componentName);
            mContextUseIds.put(componentName, cId);

            mActiveContexts.put(componentName, context);

            return true;

        } catch (ClassNotFoundException cnfe) {
            Log.e(LOGTAG, "Context Observer Class not found!");
            return false;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
            return false;
        }
    }

    private boolean loadReceiverClass(String componentName, String dex,
                                     String packagename) {

        final File optimizedDexOutputPath = mContext.getDir("outdex",
                MODE_PRIVATE);
        File dexInternalStoragePath;

        dexInternalStoragePath = new File(mContext.getDir("dex",
                MODE_PRIVATE), dex);

        DexClassLoader cl = new DexClassLoader(
                dexInternalStoragePath.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(), null,
                mContext.getClassLoader());

        Class<?> contextReceiver  = null;
        Class<?>[] parameterTypes = { ContextReceiver.class };

        try {
            //Load the Class
            contextReceiver = cl.loadClass(packagename.concat("." + componentName));
            Constructor<?> receiverConstructor = contextReceiver.getConstructor(parameterTypes);
            ContextReceiver receiver = (ContextReceiver) receiverConstructor
                    .newInstance(mContextReceiver);

            mContextReceiver = receiver;
            updateReceiverReference(mContextReceiver);

            return true;

        } catch (ClassNotFoundException cnfe) {
            Log.e(LOGTAG, "Context Receiver Class not found!");
            return false;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
            return false;
        }
    }

    public void newExternalContextValue(String name, long contextValue) {
        mContextReceiver.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, double contextValue) {
        mContextReceiver.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, boolean contextValue) {
        mContextReceiver.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, String contextValue) {
        mContextReceiver.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, Object contextValue) {
        mContextReceiver.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(Map<String, String> values) {
        mContextReceiver.newContextValues(values);
    }

    public boolean stop() {
        for (ContextObserver co : mActiveContexts.values()) {
            co.stop();
            mActiveContexts.remove(co);
            co.removeAllRequiringApps();
        }

        return true;
    }
}