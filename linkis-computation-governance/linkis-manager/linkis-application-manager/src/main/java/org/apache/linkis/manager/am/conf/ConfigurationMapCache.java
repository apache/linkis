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

package org.apache.linkis.manager.am.conf;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.governance.common.protocol.conf.*;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.protocol.CacheableProtocol;
import org.apache.linkis.rpc.RPCMapCache;

import java.util.Map;

import scala.Tuple2;

public class ConfigurationMapCache {

  public static RPCMapCache<UserCreatorLabel, String, String> globalMapCache =
      new RPCMapCache<UserCreatorLabel, String, String>(
          Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue()) {
        @Override
        public CacheableProtocol createRequest(UserCreatorLabel userCreatorLabel) {
          return new RequestQueryGlobalConfig(userCreatorLabel.getUser());
        }

        @Override
        public Map<String, String> createMap(Object any) {
          if (any instanceof ResponseQueryConfig) {
            return ((ResponseQueryConfig) any).getKeyAndValue();
          }
          return null;
        }
      };

  public static RPCMapCache<Tuple2<UserCreatorLabel, EngineTypeLabel>, String, String>
      engineMapCache =
          new RPCMapCache<Tuple2<UserCreatorLabel, EngineTypeLabel>, String, String>(
              Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue()) {
            @Override
            public CacheableProtocol createRequest(
                Tuple2<UserCreatorLabel, EngineTypeLabel> labelTuple) {
              return new RequestQueryEngineConfigWithGlobalConfig(
                  labelTuple._1(), labelTuple._2(), null);
            }

            @Override
            public Map<String, String> createMap(Object any) {
              if (any instanceof ResponseQueryConfig) {
                return ((ResponseQueryConfig) any).getKeyAndValue();
              }
              return null;
            }
          };
}
