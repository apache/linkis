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

package org.apache.linkis.instance.label.dao;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.entity.InstanceInfo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * When using the h2 library for testing,if the function(on conflict) is not supported,an error will
 * be reported, and the pg physical library will not guarantee an error pg使用h2库测试时不支持函数（on
 * conflict）会报错，pg实体库不会报错
 */
public class InstanceInfoDaoTest extends BaseDaoTest {

  @Autowired InstanceInfoDao instanceInfoDao;

  void insert() {
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setId(1);
    instanceInfo.setInstance("testInstance");
    instanceInfo.setApplicationName("testApplicationName");
    instanceInfoDao.insertOne(instanceInfo);
  }

  @Test
  public void testInsertOne() {
    insert();
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    serviceInstance.setApplicationName("testApplicationName");
    InstanceInfo instanceInfo = instanceInfoDao.getInstanceInfoByServiceInstance(serviceInstance);
    assertTrue(instanceInfo.getInstance().equals("testInstance"));
  }

  @Test
  public void testRemoveInstance() {
    insert();
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    instanceInfoDao.removeInstance(serviceInstance);
    ServiceInstance serviceInstances = new ServiceInstance();
    serviceInstances.setInstance("testInstance");
    serviceInstances.setApplicationName("testApplicationName");
    InstanceInfo instanceInfo = instanceInfoDao.getInstanceInfoByServiceInstance(serviceInstances);
    assertTrue(instanceInfo == null);
  }

  @Test
  public void testUpdateInstance() {
    insert();
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setId(1);
    instanceInfo.setInstance("testInstance1");
    instanceInfo.setApplicationName("testApplicationName1");
    instanceInfo.setUpdateTime(new Date());
    instanceInfoDao.updateInstance(instanceInfo);
  }

  @Test
  public void testGetInstanceInfoByServiceInstance() {
    insert();
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    serviceInstance.setApplicationName("testApplicationName");
    InstanceInfo instanceInfo = instanceInfoDao.getInstanceInfoByServiceInstance(serviceInstance);
    assertTrue(instanceInfo.getInstance().equals("testInstance"));
  }
}
