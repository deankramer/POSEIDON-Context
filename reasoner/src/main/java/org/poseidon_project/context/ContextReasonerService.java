/*
 * Copyright 2015 POSEIDON Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.poseidon_project.context;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.Map;

/**
 * Android Service to handle Context Reasoner requests.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextReasonerService extends Service{

    private static final String LOGTAG = "ContextService";
    private ContextReasonerCore mReasonerCore;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mReasonerCore = new ContextReasonerCore(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (IContextReasoner.class.getName().equals(intent.getAction())) {
            ContextReasonerBootReceiver.completeWakefulIntent(intent);
        } else if (ILogBackup.class.getName().equals(intent.getAction())){
            mReasonerCore.getLogger().attemptBackup(intent);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReasonerCore.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        if (IContextReasoner.class.getName().equals(intent.getAction())) {
            //mLogger.logVerbose(DataLogger.SYSTEM_CORE, LOGTAG, "Service in use");
            return mContextBinder;
        } else if (ILogBackup.class.getName().equals(intent.getAction())) {
            return mLogBackupBinder;
        } else if (IContextPreference.class.getName().equals(intent.getAction())) {
            return mContextPreferenceBinder;
        }

        return null;
    }

    public boolean onUnbind(Intent intent) {

       /* if (IContextReasoner.class.getName().equals(intent.getAction())) {
            mLogger.logVerbose(DataLogger.SYSTEM_CORE, LOGTAG, "Service no longer in use");
        }
        */
        return true;
    }

    public final IContextPreference.Stub mContextPreferenceBinder = new IContextPreference.Stub() {

        @Override
        public void synchroniseService() throws RemoteException {
            mReasonerCore.getLogger().uploadLog();
        }

        @Override
        public void alterSynchroniseTime(int hour, int min) throws RemoteException {
            mReasonerCore.alterSychroiniseTime(hour, min);
        }

        @Override
        public void alterPreferenceLong(String prefName, long value) throws RemoteException {

        }

        @Override
        public void alterPreferenceDouble(String prefName, double value) throws RemoteException {

        }

        @Override
        public void alterPreferenceBool(String prefName, boolean value) throws RemoteException {

        }

        @Override
        public void alterPreferenceString(String prefName, String value) throws RemoteException {

        }

        @Override
        public void alterPreferenceObject(String prefName, Map values) throws RemoteException {

        }
    };

    public final ILogBackup.Stub mLogBackupBinder = new ILogBackup.Stub() {

        @Override
        public void runLogBackup() throws RemoteException {
            mReasonerCore.getLogger().attemptBackup(null);
        }

    };

    public final IContextReasoner.Stub mContextBinder = new IContextReasoner.Stub() {

        @Override
        public void importDexFile(String appKey, String newDex, String[] contexts,
                                  String packageName, int permission) throws RemoteException {
            mReasonerCore.importDexFile(appKey, newDex, contexts, packageName, permission);
        }

        @Override
        public boolean addContextRequirement(String appKey, String observerName)
                throws RemoteException {
            return mReasonerCore.addContextRequirement(appKey, observerName);
        }

        @Override
        public boolean addContextRequirementWithParameters
                (String appkey, String observerName, Map parameters) throws RemoteException {
            return mReasonerCore.addContextRequirementWithParameters(appkey, observerName, parameters);
        }

        @Override
        public boolean removeContextRequirement(String appKey, String observerName)
                throws RemoteException {
            return mReasonerCore.removeContextRequirement(appKey, observerName);
        }

        @Override
        public void importOntologyURLMappingFile(String appkey, String fileLocation)
                throws RemoteException {
            mReasonerCore.importOntologyURLMappingFile(fileLocation);
        }

        @Override
        public void useOntologyURLMappingFile(String appkey, String fileLocation) throws RemoteException {

        }

        @Override
        public boolean setContextParameters(String appkey, String observerName, Map parameters) throws RemoteException {
            return mReasonerCore.setContextParameters(appkey, observerName, parameters);
        }

        @Override
        public void registerUserIdentifier(String userIdentifier) throws RemoteException {
            mReasonerCore.getLogger().registerUser(userIdentifier);
        }

    };
}
