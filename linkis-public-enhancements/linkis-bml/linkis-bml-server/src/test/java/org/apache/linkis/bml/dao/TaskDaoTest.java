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
        resourceTask.setResourceId("12");
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
        long taskId = 123;
        String state = "1";
        taskDao.updateState(taskId, state, new Date());
    }

    @Test
    void updateState2Failed() {
        long taskId = 123;
        String state = "1";
        String errMsg = "testErr";
        taskDao.updateState2Failed(taskId, state, new Date(), errMsg);
    }

    @Test
    void getNewestVersion() {
        String resourceId = "123";
        taskDao.getNewestVersion(resourceId);
    }
}
