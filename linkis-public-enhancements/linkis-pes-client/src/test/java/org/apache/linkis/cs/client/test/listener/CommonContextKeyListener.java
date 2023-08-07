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

package org.apache.linkis.cs.client.test.listener;

import org.apache.linkis.common.listener.Event;
import org.apache.linkis.cs.client.listener.ContextKeyListener;

public class CommonContextKeyListener extends ContextKeyListener {

  @Override
  public void onContextCreated(Event event) {
    System.out.println("it is created");
  }

  @Override
  public void onContextUpdated(Event event) {
    super.onContextUpdated(event);
    System.out.println("it is updated");
  }

  @Override
  public void onEventError(Event event, Throwable t) {
    System.out.println("it is not ok");
  }
}
