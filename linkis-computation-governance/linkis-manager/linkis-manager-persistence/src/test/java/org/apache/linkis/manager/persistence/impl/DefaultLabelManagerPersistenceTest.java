
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

package org.apache.linkis.manager.persistence.impl;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.dao.BaseDaoTest;
import org.h2.tools.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** 
 * DefaultLabelManagerPersistence Tester
*/
public class DefaultLabelManagerPersistenceTest extends BaseDaoTest {

    @Autowired(required = true)
    private DefaultLabelManagerPersistence defaultLabelManagerPersistence;
 
    @BeforeEach
    @DisplayName("Each unit test method is executed once before execution")
    public void before() throws Exception {
        // Start the console of h2 to facilitate viewing of h2 data
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
        //AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Scan.class);
    }
 
    @AfterEach
    @DisplayName("Each unit test method is executed once before execution")
    public void after() throws Exception {
    }
 
 
    @Test
    public void testGetLabelManagerMapper() throws Exception { 

    } 
 
    @Test
    public void testSetLabelManagerMapper() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetNodeManagerMapper() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testSetNodeManagerMapper() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelByPattern() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testAddLabel() throws Exception {
        //insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`)
        // VALUES ('combined_userCreator_engineType','*-全局设置,*-*', 'OPTIONAL', 2, now(), now());
        PersistenceLabel label = new PersistenceLabel();
        label.setLabelKey("testLabelKey");
        String labelValue = "testLabelValue";
        label.setLabelKey(labelValue);
        label.setLabelValueSize(labelValue.length());
        Date date = new Date(System.currentTimeMillis());
        label.setCreateTime(date);
        label.setUpdateTime(date);
        defaultLabelManagerPersistence.addLabel(label);
        PersistenceLabel persistenceLabel = defaultLabelManagerPersistence.getLabel(label.getId());
        assertNotNull(persistenceLabel);

    } 
 
    @Test
    public void testRemoveLabelId() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testRemoveLabelPersistenceLabel() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testUpdateLabel() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabel() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelByServiceInstance() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelByResource() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testAddLabelToNode() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelsByValue() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelsByValueList() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelsByKeyValue() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelsByKeyValueMap() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelsByKey() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testRemoveNodeLabels() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetNodeByLabel() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetNodeByLabels() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testAddLabelToUser() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testRemoveLabelFromUser() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetUserByLabel() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetUserByLabels() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelsByUser() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetNodeRelationsByLabels() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelRelationsByServiceInstance() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetLabelByKeyValue() throws Exception { 
        //TODO: Test goes here... 
    } 
 
    @Test
    public void testGetNodeByLabelKeyValue() throws Exception { 
        //TODO: Test goes here... 
    } 
} 
