/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
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

package org.noorganization.instalist.presenter.implementation;

import android.content.Context;

import org.noorganization.instalist.presenter.IPluginController;

/**
 * Factory that gas access to plugin related class instances.
 * Created by Desnoo on 14.02.2016.
 */
public class PluginControllerFactory {
    public static IPluginController getPluginController(Context _context) {
        return PluginController.getInstance(_context);
    }
}