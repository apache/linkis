package org.apache.linkis.instance.label.dao;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.entity.InstanceInfo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    assertTrue(instanceInfo != null);
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
    InstanceInfo instanceInfos = instanceInfoDao.getInstanceInfoByServiceInstance(serviceInstances);
    assertTrue(instanceInfos == null);
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
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance1");
    serviceInstance.setApplicationName("testApplicationName1");
    InstanceInfo instanceInfos = instanceInfoDao.getInstanceInfoByServiceInstance(serviceInstance);
    assertTrue(instanceInfos != null);
  }

  @Test
  public void testGetInstanceInfoByServiceInstance() {
    insert();
    ServiceInstance serviceInstance = new ServiceInstance();
    serviceInstance.setInstance("testInstance");
    serviceInstance.setApplicationName("testApplicationName");
    InstanceInfo instanceInfo = instanceInfoDao.getInstanceInfoByServiceInstance(serviceInstance);
    assertTrue(instanceInfo != null);
  }
}
