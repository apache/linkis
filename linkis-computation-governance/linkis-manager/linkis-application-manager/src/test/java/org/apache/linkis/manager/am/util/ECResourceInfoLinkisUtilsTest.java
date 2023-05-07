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

package org.apache.linkis.manager.am.util;

import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.manager.am.vo.ResourceVo;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.server.BDPJettyServerHelper;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** ECResourceInfoUtilsTest */
public class ECResourceInfoLinkisUtilsTest {

  @Test
  public void testGetStringToMap() throws Exception {
    ECResourceInfoRecord info = new ECResourceInfoRecord();
    info.setLabelValue("hadoop-LINKISCLI,spark-2.4.3");
    String str =
        "{\"driver\":{\"instance\":1,\"memory\":\"3.0 GB\",\"cpu\":1}, \"yarn\":{\"queueName\":\"dws\",\"queueMemory\":\"2.0 GB\", \"queueCpu\":2, \"instance\":0}} ";
    Map<String, Object> map = BDPJettyServerHelper.gson().fromJson(str, new HashMap<>().getClass());
    ResourceVo resourceVO = ECResourceInfoUtils.getStringToMap(str, info);
    Map diverMap = (Map) map.get("driver");
    Assertions.assertEquals(
        resourceVO.getInstance(), ((Double) diverMap.get("instance")).intValue());
    Assertions.assertEquals(resourceVO.getInstance(), 1);
    Assertions.assertEquals(resourceVO.getCores(), ((Double) diverMap.get("cpu")).intValue());
    Assertions.assertEquals(resourceVO.getCores(), 1);
    Assertions.assertEquals(
        resourceVO.getMemory(),
        ByteTimeUtils.byteStringAsBytes(String.valueOf(diverMap.getOrDefault("memory", "0k"))));
  }
}
