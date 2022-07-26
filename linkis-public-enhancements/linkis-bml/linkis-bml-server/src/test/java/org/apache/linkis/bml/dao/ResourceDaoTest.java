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

        Map<String, Object> map = new HashMap<>();
        map.put("owner", "testowner");
        map.put("resource_id", "1");
        map.put("sys", "testsys");
        resourceDao.getResources(map);
    }

    @Test
    void deleteResource() {
        String resourceId = "123";
        resourceDao.deleteResource(resourceId);
    }

    @Test
    void batchDeleteResources() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        resourceDao.batchDeleteResources(list);
    }

    @Test
    void uploadResource() {
        Resource resource = new Resource();
        resource.setResourceId("1104");
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
        String resourceId = "123";
        resourceDao.checkExists(resourceId);
    }

    @Test
    void getResource() {
        String resourceId = "123";
        resourceDao.getResource(resourceId);
    }

    @Test
    void getUserByResourceId() {
        String resourceId = "123";
        resourceDao.getUserByResourceId(resourceId);
    }

    @Test
    void changeOwner() {
        String resourceId = "123";
        String oldOwner = "oldtest";
        String newOwner = "newtest";
        resourceDao.changeOwner(resourceId, oldOwner, newOwner);
    }
}
