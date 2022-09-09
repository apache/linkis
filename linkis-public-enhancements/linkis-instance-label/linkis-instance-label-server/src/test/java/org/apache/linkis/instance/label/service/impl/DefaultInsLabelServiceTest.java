
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

package org.apache.linkis.instance.label.service.impl; 
 
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.dao.InsLabelRelationDao;
import org.apache.linkis.instance.label.entity.InsPersistenceLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** 
 * DefaultInsLabelService Tester
*/
@ExtendWith(MockitoExtension.class)
public class DefaultInsLabelServiceTest {

    @InjectMocks
    private DefaultInsLabelService defaultInsLabelService;

    @Mock
    private InsLabelRelationDao insLabelRelationDao;
 

 
 
    @Test
    public void testRemoveLabelsFromInstance() throws Exception {
        //TODO: Test goes here...
        List<InsPersistenceLabel> labelsCandidateRemoved =new ArrayList<>();
        InsPersistenceLabel insPersistenceLabel=new InsPersistenceLabel();
        insPersistenceLabel.setId(1);
        insPersistenceLabel.setLabelKey("testLabelKey");
        insPersistenceLabel.setStringValue("testStringValue");
        labelsCandidateRemoved.add(insPersistenceLabel);
        ServiceInstance serviceInstance=new ServiceInstance();
        serviceInstance.setInstance("testInstance");
        serviceInstance.setApplicationName("testApplicationName");
        Mockito.when(insLabelRelationDao.searchLabelsByInstance(serviceInstance.getInstance())).thenReturn(labelsCandidateRemoved);
        Mockito.doNothing().when(insLabelRelationDao).dropRelationsByInstance(serviceInstance.getInstance());
        defaultInsLabelService.removeLabelsFromInstance(serviceInstance);

    }
 
 
    @Test
    public void testSearchInstancesByLabelsLabels() throws Exception { 
        //TODO: Test goes here...
        /*List<? extends Label<?>> labels =new ArrayList<>();
        List<ServiceInstance> list=new ArrayList<>();
        ServiceInstance serviceInstance=new ServiceInstance();
        serviceInstance.setInstance("testInstance");
        serviceInstance.setApplicationName("testApplicationName");
        list.add(serviceInstance);
        Mockito.when(defaultInsLabelService.searchInstancesByLabels(labels, Label.ValueRelation.ALL)).thenReturn(list);
        List<ServiceInstance> lists=defaultInsLabelService.searchInstancesByLabels(labels);
        assertTrue( list.equals(lists));*/
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testSearchInstancesByLabelsForLabelsRelation() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testSearchUnRelateInstances() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testSearchLabelRelatedInstances() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testRemoveLabelsIfNotRelation() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testListAllInstanceWithLabel() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testGetInstancesByNames() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testRemoveInstance() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testGetInstanceInfoByServiceInstance() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testUpdateInstance() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testGetEurekaURL() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 
    @Test
    @DisplayName("Method description: ...")
    public void testMarkInstanceLabel() throws Exception { 
        //TODO: Test goes here... 
    } 
 
 

    @Test
    @DisplayName("Method description: ...")
    public void testInitQueue() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("initQueue"); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testUrlPreDeal() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("urlPreDeal", String.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testReplaceEurekaURL() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("replaceEurekaURL", String.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testIsIPAddress() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("isIPAddress", String.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testFilterLabelNeededInsert() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("filterLabelNeededInsert", List<InsPersistenceLabel>.class, boolean.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testDoInsertInsLabels() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("doInsertInsLabels", List<InsPersistenceLabel>.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testDoInsertInstance() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("doInsertInstance", ServiceInstance.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testToInsPersistenceLabels() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("toInsPersistenceLabels", List<?.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 

    @Test
    @DisplayName("Method description: ...")
    public void testBatchOperation() throws Exception { 
        //TODO: Test goes here... 
        /* 
        try { 
           Method method = DefaultInsLabelService.getClass().getMethod("batchOperation", T.class, Consumer<T>.class, int.class); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */ 
        } 
 
} 
