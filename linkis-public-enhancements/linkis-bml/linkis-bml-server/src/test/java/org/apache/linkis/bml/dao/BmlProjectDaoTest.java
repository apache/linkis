package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.BmlProject;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class BmlProjectDaoTest extends BaseDaoTest {

    @Autowired BmlProjectDao bmlProjectDao;

    @Test
    void createNewProject() {
        BmlProject bmlProject = new BmlProject();
        bmlProject.setName("test");
        bmlProject.setSystem("testSy");
        bmlProject.setSource("test");
        bmlProject.setDescription("descTest");
        bmlProject.setCreator("creTest");
        bmlProject.setEnabled(1);
        bmlProject.setCreateTime(new Date());
        bmlProjectDao.createNewProject(bmlProject);
    }

    @Test
    void getBmlProject() {
        String projectName = "testName";
        bmlProjectDao.getBmlProject(projectName);
    }

    @Test
    void setProjectPriv() {
        Integer projectId = 1;
        List<String> usernamesList = new ArrayList<>();
        usernamesList.add("test1");
        usernamesList.add("test2");
        int priv = 2;
        String creator = "testc";
        bmlProjectDao.setProjectPriv(projectId, usernamesList, priv, creator, new Date());
    }

    @Test
    void getPrivInProject() {
        String projectName = "testpro";
        String username = "testname";
        bmlProjectDao.getPrivInProject(projectName, username);
    }

    @Test
    void addProjectResource() {
        Integer id = 1;
        String resourceId = "321456";
        bmlProjectDao.addProjectResource(id, resourceId);
    }

    @Test
    void getProjectNameByResourceId() {
        String resourceId = "321";
        bmlProjectDao.getProjectNameByResourceId(resourceId);
    }

    @Test
    void getProjectIdByName() {
        String projectName = "test";
        bmlProjectDao.getProjectIdByName(projectName);
    }

    @Test
    void attachResourceAndProject() {
        Integer projectId = 32;
        String resourceId = "123";
        bmlProjectDao.attachResourceAndProject(projectId, resourceId);
    }

    @Test
    void checkIfExists() {
        Integer projectId = 3;
        String resourceId = "321";
        bmlProjectDao.checkIfExists(projectId, resourceId);
    }

    @Test
    void deleteAllPriv() {
        Integer projectId = 12;
        bmlProjectDao.deleteAllPriv(projectId);
    }
}
