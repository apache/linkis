package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerMapperTest extends BaseDaoTest {

  @Autowired ResourceManagerMapper resourceManagerMapper;

  @Test
  void registerResource() {
    PersistenceResource persistenceResource = new PersistenceResource();
    persistenceResource.setId(1);
    persistenceResource.setMaxResource("testmax");
    persistenceResource.setMinResource("mintest");
    persistenceResource.setLeftResource("left");
    persistenceResource.setUsedResource("user");
    persistenceResource.setTicketId("1");
    persistenceResource.setResourceType("testtype");
    resourceManagerMapper.registerResource(persistenceResource);
  }

  @Test
  void nodeResourceUpdate() {
    registerResource();
    PersistenceResource persistenceResource = new PersistenceResource();
    persistenceResource.setId(1);
    persistenceResource.setMaxResource("testmaxss");
    persistenceResource.setMinResource("mintestss");
    persistenceResource.setLeftResource("left");
    persistenceResource.setUsedResource("user");
    resourceManagerMapper.nodeResourceUpdate("1", persistenceResource);
  }

  @Test
  void nodeResourceUpdateByResourceId() {
    registerResource();
    PersistenceResource persistenceResource = new PersistenceResource();
    persistenceResource.setId(1);
    persistenceResource.setMaxResource("testmaxss");
    persistenceResource.setMinResource("mintestss");
    persistenceResource.setLeftResource("left");
    persistenceResource.setUsedResource("user");
    resourceManagerMapper.nodeResourceUpdateByResourceId(1, persistenceResource);
  }

  @Test
  void getNodeResourceUpdateResourceId() {
    registerResource();
    Integer i = resourceManagerMapper.getNodeResourceUpdateResourceId("instance1");
    assertTrue(i == null);
  }

  @Test
  void deleteResourceAndLabelId() {
    resourceManagerMapper.deleteResourceAndLabelId("instance1");
  }

  @Test
  void deleteResourceByInstance() {
    resourceManagerMapper.deleteResourceByInstance("instance1");
  }

  @Test
  void deleteResourceByTicketId() {
    registerResource();
    resourceManagerMapper.deleteResourceByTicketId("1");
  }

  @Test
  void getResourceByInstanceAndResourceType() {
    registerResource();
    List<PersistenceResource> list =
        resourceManagerMapper.getResourceByInstanceAndResourceType("instance1", "testtype");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void getResourceByServiceInstance() {
    registerResource();
    List<PersistenceResource> list =
        resourceManagerMapper.getResourceByServiceInstance("instance1");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void getNodeResourceByTicketId() {
    registerResource();
    PersistenceResource persistenceResource = resourceManagerMapper.getNodeResourceByTicketId("1");
    assertTrue(persistenceResource != null);
  }

  @Test
  void getResourceByUserName() {
    registerResource();
    List<PersistenceResource> list = resourceManagerMapper.getResourceByUserName("testname");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void getLabelsByTicketId() {
    registerResource();
    List<PersistenceLabel> list = resourceManagerMapper.getLabelsByTicketId("1");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void deleteResourceById() {
    registerResource();
    List<Integer> list = new ArrayList<>();
    list.add(1);
    resourceManagerMapper.deleteResourceById(list);
  }

  @Test
  void deleteResourceRelByResourceId() {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    resourceManagerMapper.deleteResourceRelByResourceId(list);
  }

  @Test
  void getResourceById() {
    registerResource();
    PersistenceResource persistenceResource = resourceManagerMapper.getResourceById(1);
    assertTrue(persistenceResource != null);
  }
}
