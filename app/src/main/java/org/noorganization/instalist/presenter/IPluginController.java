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

package org.noorganization.instalist.presenter;

/**
 * This Interface enables the view to search for extenal plugins that need to be registered in this
 * app at least for settings. The intended use is more overview, what belongs to this app.
 * Created by damihe on 06.01.16.
 */
public interface IPluginController {

    /**
     * Starts a search for the Plugins. This Process is asynchronous and it's return value will be
     * delivered via the bus.
     */
    void searchPlugins();
}
