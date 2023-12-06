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

package org.apache.linkis.configuration.restful.api;

import org.apache.linkis.configuration.Scan;
import org.apache.linkis.configuration.WebApplicationServer;
import org.apache.linkis.configuration.service.CategoryService;
import org.apache.linkis.configuration.service.ConfigurationService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class ConfigurationRestfulApiTest {
  private final Logger logger = LoggerFactory.getLogger(ConfigurationRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @Mock private ConfigurationService configurationService;
  @Mock private CategoryService categoryService;

  @Test
  public void TestAddKeyForEngine() throws Exception {
    MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    paramsMap.add("engineType", "spark");
    paramsMap.add("version", "3.2.1");
    paramsMap.add("token", "e8724-e");
    paramsMap.add("keyJson", "{'engineType':'spark','version':'3.2.1','boundaryType':'0'}");
    String url = "/configuration/addKeyForEngine";
    sendUrl(url, paramsMap, "get", null);
  }

  @Test
  public void TestGetFullTreesByAppName() throws Exception {
    MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
    paramsMap.add("engineType", "spark");
    paramsMap.add("version", "3.2.1");
    paramsMap.add("creator", "sam");
    String url = "/configuration/getFullTreesByAppName";

    sendUrl(url, paramsMap, "get", null);
  }

  @Test
  public void TestGetCategory() throws Exception {
    String url = "/configuration/getCategory";

    sendUrl(url, null, "get", null);
  }

  @Test
  public void TestCreateFirstCategory() throws Exception {
    String json = "{\"categoryName\":\"hadoop\",\"description\":\"very good\"}";
    String url = "/configuration/createFirstCategory";

    sendUrl(url, null, "post", json);
  }

  @Test
  public void TestDeleteCategory() throws Exception {
    String json = "{\"categoryId\":\"1\"}";
    String url = "/configuration/deleteCategory";

    sendUrl(url, null, "post", json);
  }

  @Test
  public void TestSaveFullTree() throws Exception {
    //        String json = "{\n" +
    //                "  \"fullTree\": [\n" +
    //                "    {\n" +
    //                "      \"name\": \"hive引擎资源上限\",\n" +
    //                "      \"description\": null,\n" +
    //                "      \"settings\": [\n" +
    //                "        {\n" +
    //                "          \"id\": 20,\n" +
    //                "          \"key\": \"wds.linkis.rm.instance\",\n" +
    //                "          \"description\": \"范围：1-20，单位：个\",\n" +
    //                "          \"name\": \"hive引擎最大并发数\",\n" +
    //                "          \"defaultValue\": \"193\",\n" +
    //                "          \"validateType\": \"NumInterval\",\n" +
    //                "          \"validateRange\": \"[1,20]\",\n" +
    //                "          \"level\": 1,\n" +
    //                "          \"engineType\": \"hive\",\n" +
    //                "          \"treeName\": \"hive引擎资源上限\",\n" +
    //                "          \"valueId\": 268,\n" +
    //                "          \"configValue\": \"\",\n" +
    //                "          \"configLabelId\": 1,\n" +
    //                "          \"unit\": null,\n" +
    //                "          \"isUserDefined\": false,\n" +
    //                "          \"hidden\": false,\n" +
    //                "          \"advanced\": false\n" +
    //                "        }\n" +
    //                "      ]\n" +
    //                "    }\n" +
    //                "  ],\n" +
    //                "  \"creator\": \"LINKISCLI\",\n" +
    //                "  \"engineType\": \"hive-3.1.3\"\n" +
    //                "}";
    //        String url = "/configuration/saveFullTree";
    //
    //        sendUrl(url,null,"post",json);
  }

  @Test
  public void TestEngineType() throws Exception {
    String json = "{\"categoryId\":\"1\",\"description\":\"very good\"}";
    String url = "/configuration/engineType";
  }

  @Test
  public void TestUpdateCategoryInfo() throws Exception {
    String json = "{\"categoryId\":\"1\",\"description\":\"very good\"}";
    String url = "/configuration/updateCategoryInfo";
  }

  @Test
  public void TestRpcTest() throws Exception {
    String json = "{\"categoryId\":\"1\",\"description\":\"very good\"}";
    String url = "/configuration/updateCategoryInfo";
  }

  @Test
  public void TestCheckAdmin() throws Exception {
    String json = "{\"categoryId\":\"1\",\"description\":\"very good\"}";
    String url = "/configuration/updateCategoryInfo";
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
