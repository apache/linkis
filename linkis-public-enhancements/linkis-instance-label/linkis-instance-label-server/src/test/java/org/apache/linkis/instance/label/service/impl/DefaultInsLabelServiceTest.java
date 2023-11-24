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
import org.apache.linkis.instance.label.dao.InstanceInfoDao;
import org.apache.linkis.instance.label.entity.InsPersistenceLabel;
import org.apache.linkis.instance.label.entity.InstanceInfo;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** DefaultInsLabelService Tester */
@ExtendWith(MockitoExtension.class)
public class DefaultInsLabelServiceTest {

  @InjectMocks private DefaultInsLabelService defaultInsLabelService;

  @Mock private InsLabelRelationDao insLabelRelationDao;

  @Mock private InstanceInfoDao instanceInfoDao;

  @Mock private InstanceInfoDao instanceDao;

  @Test
  public void testRemoveLabelsFromInstance() throws Exception {
    // TODO: Test goes here...
    List<InsPersistenceLabel> labelsCandidateRemoved = new ArrayList<>();
    InsPersistenceLabel insPersistenceLabel = new InsPersistenceLabel();
    insPersistenceLabel.setId(1);
    insPersistenceLabel.setLabelKey("testLabelKey");
    insPersistenceLabel.setStringValue("testStringValue");
    labelsCandidateRemoved.add(insPersistenceLabel);
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    serviceInstance.setApplicationName("testApplicationName");
    Mockito.when(insLabelRelationDao.searchLabelsByInstance(serviceInstance.getInstance()))
        .thenReturn(labelsCandidateRemoved);
    Mockito.doNothing()
        .when(insLabelRelationDao)
        .dropRelationsByInstance(serviceInstance.getInstance());
    defaultInsLabelService.removeLabelsFromInstance(serviceInstance);
  }

  @Test
  public void testListAllInstanceWithLabel() throws Exception {
    // TODO: Test goes here...
    List<InstanceInfo> list = new ArrayList<>();
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setInstance("testInstance");
    instanceInfo.setApplicationName("testApplicationName");
    instanceInfo.setId(1);
    list.add(instanceInfo);
    Mockito.when(defaultInsLabelService.listAllInstanceWithLabel()).thenReturn(list);
    List<InstanceInfo> list1 = defaultInsLabelService.listAllInstanceWithLabel();
    assertTrue(list.equals(list1));
  }

  @Test
  public void testGetInstancesByNames() throws Exception {
    // TODO: Test goes here...
    String appName = "testApplicationName";
    List<ServiceInstance> list = new ArrayList<>();
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    serviceInstance.setApplicationName("testApplicationName");
    list.add(serviceInstance);
    Mockito.when(insLabelRelationDao.getInstancesByNames(appName)).thenReturn(list);
    List<ServiceInstance> list1 = defaultInsLabelService.getInstancesByNames(appName);
    assertTrue(list.equals(list1));
  }

  @Test
  public void testRemoveInstance() throws Exception {
    // TODO: Test goes here...
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    Mockito.doNothing().when(instanceInfoDao).removeInstance(serviceInstance);
    defaultInsLabelService.removeInstance(serviceInstance);
  }

  @Test
  public void testGetInstanceInfoByServiceInstance() throws Exception {
    // TODO: Test goes here...
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setInstance("testInstance");
    instanceInfo.setApplicationName("testApplicationName");
    instanceInfo.setId(1);
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    serviceInstance.setApplicationName("testApplicationName");
    Mockito.when(instanceInfoDao.getInstanceInfoByServiceInstance(serviceInstance))
        .thenReturn(instanceInfo);
    InstanceInfo instanceInfo1 =
        defaultInsLabelService.getInstanceInfoByServiceInstance(serviceInstance);
    assertTrue(instanceInfo1.equals(instanceInfo));
  }

  @Test
  public void testUpdateInstance() throws Exception {
    // TODO: Test goes here...
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setInstance("testInstance1");
    instanceInfo.setApplicationName("testApplicationName1");
    instanceInfo.setId(1);
    Mockito.doNothing().when(instanceInfoDao).updateInstance(instanceInfo);
    defaultInsLabelService.updateInstance(instanceInfo);
  }
}
