package org.apache.linkis.configuration.restful.api;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.configuration.Scan;
import org.apache.linkis.configuration.WebApplicationServer;
import org.apache.linkis.configuration.service.CategoryService;
import org.apache.linkis.configuration.service.ConfigurationService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class ConfigurationRestfulApiTest {
    private Logger logger = LoggerFactory.getLogger(ConfigurationRestfulApiTest.class);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired private ConfigurationService configurationService;
    @Autowired private CategoryService categoryService;

    @Test
    public void TestAddKeyForEngine() throws Exception {
        MvcUtils mvcUtils = new MvcUtils(mockMvc);
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("engineType","1");
        paramsMap.add("version","1");
        paramsMap.add("token","1");
        paramsMap.add("keyJson","1");
        String url = "/configuration/addKeyForEngine";
        Message mvcResult =
                mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, paramsMap));

        assertEquals(MessageStatus.SUCCESS(), mvcResult.getStatus());

        logger.info(String.valueOf(mvcResult));
    }

    @Test
    public void TestGetFullTreesByAppName(){
        String url = "/configuration/getFullTreesByAppName";

    }

    @Test
    public void TestGetCategory(){
        String url = "/configuration/getCategory";
    }

    @Test
    public void TestCreateFirstCategory(){
        String url = "/configuration/createFirstCategory";
    }

    @Test
    public void TestDeleteCategory(){
        String url = "/configuration/deleteCategory";
    }
}
