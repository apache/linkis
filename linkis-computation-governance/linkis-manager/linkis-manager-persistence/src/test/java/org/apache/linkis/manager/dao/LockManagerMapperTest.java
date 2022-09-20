package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LockManagerMapperTest extends BaseDaoTest {

  @Autowired LockManagerMapper lockManagerMapper;

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  private String jsonObj = "testJsonObj";

  @Test
  public void testLock() {
    lockManagerMapper.lock(jsonObj, 5L);
    List<PersistenceLock> locks = lockManagerMapper.getLockersByLockObject(jsonObj);
    assertEquals(locks.size(), 1);
  }

  @Test
  public void testUnlock() {
    lockManagerMapper.lock(jsonObj, 5L);
    List<PersistenceLock> locks = lockManagerMapper.getLockersByLockObject(jsonObj);
    assertEquals(locks.size(), 1);
    lockManagerMapper.unlock(locks.get(0).getId());
    List<PersistenceLock> locks1 = lockManagerMapper.getLockersByLockObject(jsonObj);
    assertEquals(locks1.size(), 0);
  }

  @Test
  public void testGetAll() {
    lockManagerMapper.lock(jsonObj, 5L);
    lockManagerMapper.lock("testJsonObj1111", 6L);
    List<PersistenceLock> locks = lockManagerMapper.getAll();
    assertEquals(2, locks.size());
  }
}
