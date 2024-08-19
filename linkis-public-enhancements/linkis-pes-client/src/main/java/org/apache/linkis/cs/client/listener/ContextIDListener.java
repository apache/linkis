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

package org.apache.linkis.cs.client.listener;

import org.apache.linkis.common.listener.Event;
import org.apache.linkis.cs.client.Context;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.listener.event.impl.DefaultContextIDEvent;

/** Description: 这个listener是用来监听contextID的，用户可以进行实现 */
public abstract class ContextIDListener implements ContextClientListener {

  private ContextID contextID;

  private Context context;

  public ContextIDListener() {}

  public ContextIDListener(ContextID contextID) {
    this.contextID = contextID;
  }

  public ContextID getContextID() {
    return contextID;
  }

  public void setContextID(ContextID contextID) {
    this.contextID = contextID;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  @Override
  public void onContextCreated(Event event) {}

  @Override
  public void onContextUpdated(Event event) {}

  public abstract void onContextRemoved(Event event);

  @Override
  public void onEvent(Event event) {
    if (event instanceof DefaultContextIDEvent) {
      DefaultContextIDEvent defaultContextKeyEvent = (DefaultContextIDEvent) event;
      if (defaultContextKeyEvent.getContextID().equals(contextID)) {
        switch (defaultContextKeyEvent.getOperateType()) {
          case UPDATE:
            onContextUpdated(defaultContextKeyEvent);
            break;
          case CREATE:
            onContextCreated(defaultContextKeyEvent);
            break;
          case REMOVE:
            onContextRemoved(defaultContextKeyEvent);
            break;
          default:
            break;
        }
      }
    }
  }
}
