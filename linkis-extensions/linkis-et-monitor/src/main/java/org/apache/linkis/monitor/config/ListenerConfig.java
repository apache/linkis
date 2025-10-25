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

package org.apache.linkis.monitor.config;

import org.apache.linkis.monitor.entity.ClientSingleton;
import org.apache.linkis.monitor.until.ThreadUtils;
import org.apache.linkis.monitor.utils.log.LogUtils;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;

import org.slf4j.Logger;

@Configuration
public class ListenerConfig {

  private static final Logger logger = LogUtils.stdOutLogger();

  @EventListener
  private void shutdownEntrance(ContextClosedEvent event) {
    try {
      ThreadUtils.executors.shutdown();
      ClientSingleton.getInstance().close();
    } catch (IOException e) {
      logger.error("ListenerConfig error msg  {}", e.getMessage());
    }
  }
}
