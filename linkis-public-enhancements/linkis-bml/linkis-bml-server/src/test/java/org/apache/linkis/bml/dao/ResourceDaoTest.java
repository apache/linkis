package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDaoTest extends BaseDaoTest {

    @Autowired ResourceDao resourceDao;

    @Test
    void getResources() {
        uploadResource();
        Map<String, Object> map = new HashMap<>();
        map.put("owner", "testowner");
        map.put("resource_id", "123");
        map.put("sys", "testsys");
        resourceDao.getResources(map);
    }

    @Test
    void deleteResource() {
        uploadResource();
        resourceDao.deleteResource("123");
    }

    @Test
    void batchDeleteResources() {
        uploadResource();
        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("2");
        list.add("3");
        resourceDao.batchDeleteResources(list);
    }

    @Test
    void uploadResource() {
        Resource resource = new Resource();
        resource.setResourceId("123");
        resource.setResourceHeader("2");
        resource.setDownloadedFileName("testFileName");
        resource.setSystem("testSystem");
        resource.setCreateTime(new Date());
        resource.setUser("testUser");
        resource.setExpireTime("2012.12.02");
        resource.setMaxVersion(3);
        resource.setUpdateTime(new Date());
        resource.setUpdator("testUpdator");
        resource.setEnableFlag(false);
        resourceDao.uploadResource(resource);
    }

    @Test
    void checkExists() {
        uploadResource();
        resourceDao.checkExists("123");
    }

    @Test
    void getResource() {
        uploadResource();
        resourceDao.getResource("123");
    }

    @Test
    void getUserByResourceId() {
        uploadResource();
        resourceDao.getUserByResourceId("123");
    }

    @Test
    void changeOwner() {
        String oldOwner = "oldtest";
        String newOwner = "newtest";
        resourceDao.changeOwner("123", oldOwner, newOwner);
    }
}
