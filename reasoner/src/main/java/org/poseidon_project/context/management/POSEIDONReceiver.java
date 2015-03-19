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

package org.poseidon_project.context.management;

import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.UIEvent;

import java.util.Map;

/**
 * The Context Receiver to handle built in POSEIDON contexts.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class POSEIDONReceiver extends ContextReceiver{

    @Override
    public void newContextValue(String name, long value) {

    }

    @Override
    public void newContextValue(String name, double value) {

    }

    @Override
    public void newContextValue(String name, boolean value) {

    }

    @Override
    public void newContextValue(String name, String value) {

    }

    @Override
    public void newContextValue(String name, Object value) {

    }

    @Override
    public void newContextValues(Map<String, String> values) {

    }

    @Override
    public void newUIEvent(UIEvent event) {

    }
}