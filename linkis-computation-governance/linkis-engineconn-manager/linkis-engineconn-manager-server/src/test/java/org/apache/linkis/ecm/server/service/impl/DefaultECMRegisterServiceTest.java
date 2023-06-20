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

package org.apache.linkis.ecm.server.service.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.protocol.em.RegisterEMRequest;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.rpc.serializer.ProtostuffSerializeUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.manager.label.conf.LabelCommonConfig.ENGINE_CONN_MANAGER_SPRING_NAME;

public class DefaultECMRegisterServiceTest {
  @Test
  void testECM() {
    DefaultECMRegisterService defaultECMRegisterService = new DefaultECMRegisterService();
    RegisterEMRequest request = new RegisterEMRequest();
    ServiceInstance instance = new ServiceInstance();
    instance.setInstance("127.0.0.1:9001");
    instance.setApplicationName("ecm");
    request.setUser("hadoop");
    request.setServiceInstance(instance);
    request.setAlias(instance.getApplicationName());

    Map<String, Object> labels = new HashMap<>();
    labels.put(
        LabelKeyConstant.SERVER_ALIAS_KEY,
        Collections.singletonMap("alias", ENGINE_CONN_MANAGER_SPRING_NAME));
    request.setLabels(defaultECMRegisterService.getLabelsFromArgs(null));
    // the ECMUtils.inferDefaultMemory() will throw error disable the test
    // request.setNodeResource(defaultECMRegisterService.getEMRegiterResourceFromConfiguration());
    String res = ProtostuffSerializeUtil.serialize(request);
    ProtostuffSerializeUtil.deserialize(res, RegisterEMRequest.class);
  }
}
