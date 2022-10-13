package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LockManagerMapperTest  extends BaseDaoTest{

    @Autowired
    LockManagerMapper lockManagerMapper;

    @Test
    void lock() {
        lockManagerMapper.lock("testjson",1L);
    }

    @Test
    void unlock() {
        lockManagerMapper.unlock(1);
    }

    @Test
    void getLockersByLockObject() {
        lock();
        List<PersistenceLock> list=lockManagerMapper.getLockersByLockObject("testjson");
        assertTrue(list.size() >=1);
    }

    @Test
    void getAll() {
        lock();
        List<PersistenceLock> list=lockManagerMapper.getAll();
        assertTrue(list.size() >=1);
    }
}