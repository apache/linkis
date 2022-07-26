package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.ResourceTask;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TaskDaoTest extends BaseDaoTest {
    @Autowired TaskDao taskDao;

    @Test
    void insert() {
        ResourceTask resourceTask = new ResourceTask();
        resourceTask.setResourceId("123");
        resourceTask.setClientIp("192.168.142");
        resourceTask.setEndTime(new Date());
        resourceTask.setId(32);
        resourceTask.setStartTime(new Date());
        resourceTask.setErrMsg("testErr");
        resourceTask.setExtraParams("testpar");
        resourceTask.setInstance("testInst");
        resourceTask.setLastUpdateTime(new Date());
        resourceTask.setOperation("testOPer");
        resourceTask.setState("1");
        resourceTask.setSubmitUser("testSumUser");
        resourceTask.setSystem("testSym");
        resourceTask.setVersion("1.2");
        taskDao.insert(resourceTask);
    }

    @Test
    void updateState() {
        insert();
        taskDao.updateState(32, "1", new Date());
    }

    @Test
    void updateState2Failed() {
        insert();
        taskDao.updateState2Failed(32, "1", new Date(), "errMsg");
    }

    @Test
    void getNewestVersion() {
        insert();
        taskDao.getNewestVersion("123");
    }
}
