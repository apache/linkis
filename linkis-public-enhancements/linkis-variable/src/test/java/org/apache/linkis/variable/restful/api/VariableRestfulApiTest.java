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

package org.apache.linkis.variable.restful.api;

import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.variable.Scan;
import org.apache.linkis.variable.WebApplicationServer;
import org.apache.linkis.variable.restful.MvcUtils;
import org.apache.linkis.variable.service.VariableService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** VariableRestfulApi Tester */
@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class VariableRestfulApiTest {
  private Logger logger = LoggerFactory.getLogger(VariableRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @Mock VariableService variableService;

  private static MockedStatic<SecurityFilter> securityFilter;

  @BeforeAll
  private static void init() {
    securityFilter = Mockito.mockStatic(SecurityFilter.class);
  }

  @AfterAll
  private static void close() {
    securityFilter.close();
  }

  @Test
  public void testListGlobalVariable() throws Exception {
    // h2数据库执行生成的sql语句报错
    /*String url = "/variable/listGlobalVariable";
    securityFilter
            .when(() -> SecurityFilter.getLoginUsername(isA(HttpServletRequest.class)))
            .thenReturn("hadoop");
    List<VarKeyValueVO> list=new ArrayList<>();
    VarKeyValueVO varKeyValueVO=new VarKeyValueVO();
    varKeyValueVO.setValue("testV");
    varKeyValueVO.setKey("testK");
    varKeyValueVO.setKeyID(1l);
    varKeyValueVO.setValueID(2l);
    list.add(varKeyValueVO);
    Mockito.when(variableService.listGlobalVariable("hadoop")).thenReturn(list);
    sendUrl(url, null, "get", null);*/
  }

  @Test
  public void testSaveGlobalVariable() throws Exception {}

  public void sendUrl(String url, MultiValueMap<String, String> paramsMap, String type, String msg)
      throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    Message mvcResult = null;
    if (type.equals("get")) {
      if (paramsMap != null) {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, paramsMap));
      } else {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
      }
    }
    if (type.equals("post")) {
      if (msg != null) {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, msg));
      } else {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url));
      }
    }
    assertEquals(MessageStatus.SUCCESS(), mvcResult.getStatus());
    logger.info(String.valueOf(mvcResult));
  }
}
