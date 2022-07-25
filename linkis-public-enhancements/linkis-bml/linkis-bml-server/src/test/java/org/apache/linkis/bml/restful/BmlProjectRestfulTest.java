
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

package org.apache.linkis.bml.restful; 
 
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.linkis.bml.service.BmlProjectService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/** 
 * BmlProjectRestful Tester
*/ 
public class BmlProjectRestfulTest { 
 
    @Autowired
    private BmlProjectRestful bmlProjectRestful;

    @Autowired
    private BmlProjectService bmlProjectService;
 
    @BeforeEach
    @DisplayName("Each unit test method is executed once before execution")
    public void before() throws Exception {
    }
 
    @AfterEach
    @DisplayName("Each unit test method is executed once before execution")
    public void after() throws Exception {
    }
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testCreateBmlProject() throws Exception { 
        //TODO: Test goes here...
        /*String projectName = "test";
        String username = "admin";
        JsonNode editUserNode = jsonNode.get(EDIT_USERS_STR);
        JsonNode accessUserNode = jsonNode.get(ACCESS_USERS_STR);
        List<String> accessUsers = new ArrayList<>();
        List<String> editUsers = new ArrayList<>();
        if (editUserNode.isArray()) {
            for (JsonNode node : editUserNode) {
                editUsers.add(node.textValue());
            }
        }
        if (accessUserNode.isArray()) {
            for (JsonNode node : accessUserNode) {
                accessUsers.add(node.textValue());
            }
        }
         bmlProjectService.createBmlProject(projectName, username, editUsers, accessUsers);

         Message.ok("success to create project(创建工程ok)");*/


    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testUploadShareResource() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testUpdateShareResource() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testDownloadShareResource() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testGetProjectInfo() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testAttachResourceAndProject() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testUpdateProjectUsers() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
} 
