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

package org.apache.linkis.basedatamanager.server.restful;

import org.apache.linkis.basedatamanager.server.Scan;
import org.apache.linkis.basedatamanager.server.WebApplicationServer;
import org.apache.linkis.basedatamanager.server.domain.DatasourceEnvEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceAccessService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class DatasourceAccessRestfulApiTest {
  private Logger logger = LoggerFactory.getLogger(DatasourceAccessRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @Mock private DatasourceAccessService datasourceAccessService;

  @Test
  public void TestList() throws Exception {
    MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    paramsMap.add("searchName", "");
    paramsMap.add("currentPage", "1");
    paramsMap.add("pageSize", "10");
    String url = "/basedata-manager/datasource-env";
    sendUrl(url, paramsMap, "get", null);
  }

  @Test
  public void TestGet() throws Exception {
    MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    String url = "/basedata-manager/datasource-env/" + "1";
    sendUrl(url, paramsMap, "get", null);
  }

  @Test
  public void TestAdd() throws Exception {
    String url = "/basedata-manager/datasource-env";
    DatasourceEnvEntity datasourceEnv = new DatasourceEnvEntity();
    datasourceEnv.setId(2);
    datasourceEnv.setEnvName("test");
    datasourceEnv.setEnvDesc("test");
    datasourceEnv.setParameter("params");
    datasourceEnv.setCreateTime(new Date());
    datasourceEnv.setModifyTime(new Date());
    datasourceEnv.setDatasourceTypeId(1);
    datasourceEnv.setModifyUser("linkis");
    datasourceEnv.setCreateUser("linkis");
    ObjectMapper objectMapper = new ObjectMapper();
    String msg = objectMapper.writeValueAsString(datasourceEnv);
    sendUrl(url, null, "post", msg);
  }

  @Test
  public void TestUpdate() throws Exception {
    String url = "/basedata-manager/datasource-env";
    DatasourceEnvEntity datasourceEnv = new DatasourceEnvEntity();
    datasourceEnv.setId(2);
    datasourceEnv.setEnvName("test");
    datasourceEnv.setEnvDesc("test");
    datasourceEnv.setParameter("params");
    datasourceEnv.setCreateTime(new Date());
    datasourceEnv.setModifyTime(new Date());
    datasourceEnv.setDatasourceTypeId(1);
    datasourceEnv.setModifyUser("linkis");
    datasourceEnv.setCreateUser("linkis");
    ObjectMapper objectMapper = new ObjectMapper();
    String msg = objectMapper.writeValueAsString(datasourceEnv);
    sendUrl(url, null, "put", msg);
  }

  @Test
  public void TestRemove() throws Exception {
    MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    String url = "/basedata-manager/datasource-env/" + "1";
    sendUrl(url, paramsMap, "delete", null);
  }

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

    if (type.equals("delete")) {
      mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultDelete(url));
    }

    if (type.equals("post")) {
      if (msg != null) {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, msg));
      } else {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url));
      }
    }
    if (type.equals("put")) {
      if (msg != null) {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, msg));
      } else {
        mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
      }
    }
    assertEquals(MessageStatus.SUCCESS(), mvcResult.getStatus());
    logger.info(String.valueOf(mvcResult));
  }
}
