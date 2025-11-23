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

package org.apache.linkis.engineplugin.openlookeng.conf;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryEngineConfigWithGlobalConfig;
import org.apache.linkis.governance.common.protocol.conf.ResponseQueryConfig;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.rpc.Sender;

import java.util.Map;

public class OpenLooKengEngineConfCache {

  public static Map<String, String> getConfMap(
      UserCreatorLabel userCreatorLabel, EngineTypeLabel engineTypeLabel) {
    Sender sender =
        Sender.getSender(
            Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue());
    Object any =
        sender.ask(
            new RequestQueryEngineConfigWithGlobalConfig(userCreatorLabel, engineTypeLabel, null));
    if (any instanceof ResponseQueryConfig) {
      return ((ResponseQueryConfig) any).getKeyAndValue();
    }
    return null;
  }
}
