package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetrics;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeMetricManagerMapperTest extends BaseDaoTest {

  @Autowired NodeMetricManagerMapper nodeMetricManagerMapper;

  @Test
  void addNodeMetrics() {
    PersistenceNodeMetrics nodeMetrics = new PersistenceNodeMetrics();
  }

  @Test
  void checkInstanceExist() {}

  @Test
  void getNodeMetricsByInstances() {}

  @Test
  void getNodeMetricsByInstance() {}

  @Test
  void updateNodeMetrics() {}

  @Test
  void deleteNodeMetrics() {}

  @Test
  void deleteNodeMetricsByInstance() {}

  @Test
  void getAllNodeMetrics() {}
}
