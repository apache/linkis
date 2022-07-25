package org.apache.linkis.bml.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.linkis.bml.entity.ResourceVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VersionDaoTest {

    @Autowired
    VersionDao versionDao;
    @Test
    void getVersion() {
        String resourceId="123";
        String version="1.2";
        versionDao.getVersion(resourceId,version);
    }

    @Test
    void getVersions() {
        String resourceId="123";
        versionDao.getVersions(resourceId);
    }

    @Test
    void getResourcesVersions() {
        Map<String,String>  map=new HashMap<>();
        versionDao.getResourcesVersions(map);
    }

    @Test
    void deleteVersion() {
        String resourceId="123";
        String version="1.2";
        versionDao.deleteVersion(resourceId,version);
    }

    @Test
    void deleteVersions() {
        String resourceId="123";
        versionDao.deleteVersions(resourceId);
    }

    @Test
    void bathDeleteVersions() {
        List<String> resourceIdlist=new ArrayList<>();
        List<String> versionlist=new ArrayList<>();
        versionDao.bathDeleteVersions(resourceIdlist,versionlist);
    }

    @Test
    void insertNewVersion() {
        ResourceVersion resourceVersion =new ResourceVersion();
        versionDao.insertNewVersion(resourceVersion);
    }

    @Test
    void getResourcePath() {
        String resourceId="123";
        versionDao.getResourcePath(resourceId);
    }

    @Test
    void getNewestVersion() {
        String resourceId="123";
        versionDao.getNewestVersion(resourceId);
    }

    @Test
    void getStartByteForResource() {
        String resourceId="123";
        String version="1.2";
        versionDao.getStartByteForResource(resourceId,version);
    }

    @Test
    void getEndByte() {
        String resourceId="123";
        String version="1.2";
        versionDao.getEndByte(resourceId,version);
    }

    @Test
    void findResourceVersion() {
        String resourceId="123";
        String version="1.2";
        versionDao.findResourceVersion(resourceId,version);
    }

    @Test
    void getAllResourcesViaSystem() {
        String resourceId="123";
        String version="1.2";
        versionDao.getAllResourcesViaSystem(resourceId,version);
    }

    @Test
    void selectResourcesViaSystemByPage() {
        String resourceId="123";
        String version="1.2";
        versionDao.selectResourcesViaSystemByPage(resourceId,version);
    }

    @Test
    void checkVersion() {
        String resourceId="123";
        String version="1.2";
        versionDao.checkVersion(resourceId,version);
    }

    @Test
    void selectResourceVersionEnbleFlag() {
        String resourceId="123";
        String version="1.2";
        versionDao.selectResourceVersionEnbleFlag(resourceId,version);
    }

    @Test
    void deleteResource() {
        String resourceId="123";
        versionDao.deleteResource(resourceId);
    }

    @Test
    void batchDeleteResources() {
        List<String> resourceIdlist=new ArrayList<>();
        List<String> versionlist=new ArrayList<>();
        versionDao.bathDeleteVersions(resourceIdlist,versionlist);
    }

    @Test
    void getResourceVersion() {
        String resourceId="123";
        String version="1.2";
        versionDao.getResourceVersion(resourceId,version);
    }

    @Test
    void selectVersionByPage() {
        String resourceId="123";
        versionDao.selectVersionByPage(resourceId);

    }

    @Test
    void getResourceVersionsByResourceId() {
        String resourceId="123";
        versionDao.getResourceVersionsByResourceId(resourceId);
    }
}