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

package org.apache.linkis.cs.common.utils;

import java.time.LocalDateTime;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CSCommonUtilsTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String contextIdStr = CSCommonUtils.CONTEXT_ID_STR;
    String nodeNameStr = CSCommonUtils.NODE_NAME_STR;
    String nodeId = CSCommonUtils.NODE_ID;
    String idNodeName = CSCommonUtils.ID_NODE_NAME;
    String flowInfos = CSCommonUtils.FLOW_INFOS;
    String contextEnvDev = CSCommonUtils.CONTEXT_ENV_DEV;
    String contextEnvProd = CSCommonUtils.CONTEXT_ENV_PROD;

    Assertions.assertEquals("contextID", contextIdStr);
    Assertions.assertEquals("nodeName", nodeNameStr);
    Assertions.assertEquals("nodeID", nodeId);
    Assertions.assertEquals("id_nodeName", idNodeName);
    Assertions.assertEquals("flow.infos", flowInfos);
    Assertions.assertEquals("BDP_DEV", contextEnvDev);
    Assertions.assertEquals("BDAP_PROD", contextEnvProd);
  }

  @Test
  @DisplayName("getTableKeyTest")
  public void getTableKeyTest() {

    String tableKey = CSCommonUtils.getTableKey("node1", "table1");
    Assertions.assertNotNull(tableKey);
  }

  @Test
  @DisplayName("localDatetimeToDateTest")
  public void localDatetimeToDateTest() {

    LocalDateTime now = LocalDateTime.now();
    Date date = CSCommonUtils.localDatetimeToDate(now);

    Assertions.assertNotNull(date);
  }
}
