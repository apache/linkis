/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.listener.manager.imp;

import org.apache.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import org.apache.linkis.cs.listener.callback.imp.DefaultContextIDCallbackEngine;
import org.apache.linkis.cs.listener.callback.imp.DefaultContextKeyCallbackEngine;
import org.apache.linkis.cs.listener.manager.ListenerManager;

public class DefaultContextListenerManager implements ListenerManager {
  @Override
  public ContextAsyncListenerBus getContextAsyncListenerBus() {
    ContextAsyncListenerBus contextAsyncListenerBus = ContextAsyncListenerBus.getInstance();
    return contextAsyncListenerBus;
  }

  @Override
  public DefaultContextIDCallbackEngine getContextIDCallbackEngine() {
    DefaultContextIDCallbackEngine instanceIdCallbackEngine =
        DefaultContextIDCallbackEngine.getInstance();
    return instanceIdCallbackEngine;
  }

  @Override
  public DefaultContextKeyCallbackEngine getContextKeyCallbackEngine() {
    DefaultContextKeyCallbackEngine instanceKeyCallbackEngine =
        DefaultContextKeyCallbackEngine.getInstance();
    return instanceKeyCallbackEngine;
  }

  private static DefaultContextListenerManager singleDefaultContextListenerManager = null;

  private DefaultContextListenerManager() {}

  public static DefaultContextListenerManager getInstance() {
    if (singleDefaultContextListenerManager == null) {
      synchronized (DefaultContextListenerManager.class) {
        if (singleDefaultContextListenerManager == null) {
          singleDefaultContextListenerManager = new DefaultContextListenerManager();
        }
      }
    }
    return singleDefaultContextListenerManager;
  }
}
