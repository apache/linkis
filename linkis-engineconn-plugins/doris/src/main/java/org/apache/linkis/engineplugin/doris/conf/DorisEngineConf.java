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

package org.apache.linkis.engineplugin.doris.conf;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryEngineConfigWithGlobalConfig;
import org.apache.linkis.governance.common.protocol.conf.ResponseQueryConfig;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.protocol.CacheableProtocol;
import org.apache.linkis.rpc.RPCMapCache;

import java.util.Map;

import scala.Tuple2;

public class DorisEngineConf
    extends RPCMapCache<Tuple2<UserCreatorLabel, EngineTypeLabel>, String, String> {

  public DorisEngineConf() {
    super(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue());
  }

  @Override
  public CacheableProtocol createRequest(Tuple2<UserCreatorLabel, EngineTypeLabel> labelTuple) {
    return new RequestQueryEngineConfigWithGlobalConfig(labelTuple._1(), labelTuple._2(), null);
  }

  @Override
  public Map<String, String> createMap(Object obj) {
    if (obj instanceof ResponseQueryConfig) {
      ResponseQueryConfig response = (ResponseQueryConfig) obj;
      return response.getKeyAndValue();
    } else {
      return null;
    }
  }
}
