package org.apache.linkis.bml.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.linkis.bml.entity.Resource;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDaoTest extends BaseDaoTest {


    @Autowired
    ResourceDao resourceDao;



    @Test
    void getResources() {

        Map<String,Object> map=new HashMap<>();
        map.put("owner","testowner");
        map.put("resource_id","1");
        map.put("sys","testsys");
        resourceDao.getResources(map);
    }

    @Test
    void deleteResource() {
        String  resourceId="123";
        resourceDao.deleteResource(resourceId);
    }

    @Test
    void batchDeleteResources() {
        List<String> list=new ArrayList<>();
        resourceDao.batchDeleteResources(list);
    }

    @Test
    void uploadResource() {
        Resource resource=new Resource();
        resourceDao.uploadResource(resource);
    }

    @Test
    void checkExists() {
        String  resourceId="123";
        resourceDao.checkExists(resourceId);
    }

    @Test
    void getResource() {
        String  resourceId="123";
        resourceDao.getResource(resourceId);
    }

    @Test
    void getUserByResourceId() {
        String  resourceId="123";
        resourceDao.getUserByResourceId(resourceId);
    }

    @Test
    void changeOwner() {
        String resourceId="123";
        String oldOwner="oldtest";
        String newOwner="newtest";
        resourceDao.changeOwner(resourceId,oldOwner,newOwner);
    }
}