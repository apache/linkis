package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.ResourceVersion;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class VersionDaoTest extends BaseDaoTest {

    @Autowired VersionDao versionDao;

    @Test
    void getVersion() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.getVersion(resourceId, version);
    }

    @Test
    void getVersions() {
        String resourceId = "123";
        versionDao.getVersions(resourceId);
    }

    @Test
    void getResourcesVersions() {
        Map<String, Object> map = new HashMap<>();
        map.put("system", "testSys");
        map.put("user", "testUser");
        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("321");
        map.put("resourceIds", list);
        versionDao.getResourcesVersions(map);
    }

    @Test
    void deleteVersion() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.deleteVersion(resourceId, version);
    }

    @Test
    void deleteVersions() {
        String resourceId = "123";
        versionDao.deleteVersions(resourceId);
    }

    @Test
    void bathDeleteVersions() {
        List<String> resourceIdlist = new ArrayList<>();
        resourceIdlist.add("12");
        resourceIdlist.add("21");
        List<String> versionlist = new ArrayList<>();
        versionlist.add("1.2");
        versionlist.add("2.1");
        versionDao.bathDeleteVersions(resourceIdlist, versionlist);
    }

    @Test
    void insertNewVersion() {
        ResourceVersion resourceVersion = new ResourceVersion();
        resourceVersion.setResourceId("12");
        resourceVersion.setFileMd5("binbin");
        resourceVersion.setVersion("1.2");
        resourceVersion.setSize(25);
        resourceVersion.setStartByte(35);
        resourceVersion.setEndByte(36);
        resourceVersion.setResource("testreso");
        resourceVersion.setDescription("testDesc");
        resourceVersion.setStartTime(new Date());
        resourceVersion.setEndTime(new Date());
        resourceVersion.setClientIp("132.145.36");
        resourceVersion.setUpdator("testUp");
        resourceVersion.setEnableFlag(false);
        versionDao.insertNewVersion(resourceVersion);
    }

    @Test
    void getResourcePath() {
        String resourceId = "123";
        versionDao.getResourcePath(resourceId);
    }

    @Test
    void getNewestVersion() {
        String resourceId = "123";
        versionDao.getNewestVersion(resourceId);
    }

    @Test
    void getStartByteForResource() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.getStartByteForResource(resourceId, version);
    }

    @Test
    void getEndByte() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.getEndByte(resourceId, version);
    }

    @Test
    void findResourceVersion() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.findResourceVersion(resourceId, version);
    }

    @Test
    void getAllResourcesViaSystem() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.getAllResourcesViaSystem(resourceId, version);
    }

    @Test
    void selectResourcesViaSystemByPage() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.selectResourcesViaSystemByPage(resourceId, version);
    }

    @Test
    void checkVersion() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.checkVersion(resourceId, version);
    }

    @Test
    void selectResourceVersionEnbleFlag() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.selectResourceVersionEnbleFlag(resourceId, version);
    }

    @Test
    void deleteResource() {
        String resourceId = "123";
        versionDao.deleteResource(resourceId);
    }

    @Test
    void batchDeleteResources() {
        List<String> resourceIdlist = new ArrayList<>();
        resourceIdlist.add("12");
        resourceIdlist.add("21");
        List<String> versionlist = new ArrayList<>();
        versionlist.add("1.2");
        versionlist.add("2.1");
        versionDao.bathDeleteVersions(resourceIdlist, versionlist);
    }

    @Test
    void getResourceVersion() {
        String resourceId = "123";
        String version = "1.2";
        versionDao.getResourceVersion(resourceId, version);
    }

    @Test
    void selectVersionByPage() {
        String resourceId = "123";
        versionDao.selectVersionByPage(resourceId);
    }

    @Test
    void getResourceVersionsByResourceId() {
        String resourceId = "123";
        versionDao.getResourceVersionsByResourceId(resourceId);
    }
}
