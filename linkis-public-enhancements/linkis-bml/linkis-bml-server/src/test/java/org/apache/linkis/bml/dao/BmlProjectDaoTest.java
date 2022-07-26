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
        bmlProject.setName("testName");
        bmlProject.setSystem("testSy");
        bmlProject.setSource("test");
        bmlProject.setDescription("descTest");
        bmlProject.setCreator("creCreatorUser");
        bmlProject.setEnabled(1);
        bmlProject.setCreateTime(new Date());
        bmlProject.setId(1);
        bmlProjectDao.createNewProject(bmlProject);
    }

    @Test
    void getBmlProject() {
        createNewProject();
        bmlProjectDao.getBmlProject("testName");
    }

    @Test
    void setProjectPriv() {
        List<String> usernamesList = new ArrayList<>();
        usernamesList.add("creCreatorUser");
        usernamesList.add("creCreatorUser1");
        int priv = 2;
        bmlProjectDao.setProjectPriv(1, usernamesList, priv, "creCreatorUser", new Date());
    }

    @Test
    void getPrivInProject() {
        setProjectPriv();
        createNewProject();
        bmlProjectDao.getPrivInProject("testName", "creCreatorUser");
    }

    @Test
    void addProjectResource() {
        bmlProjectDao.addProjectResource(1, "123");
    }

    @Test
    void getProjectNameByResourceId() {
        bmlProjectDao.getProjectNameByResourceId("123");
    }

    @Test
    void getProjectIdByName() {
        createNewProject();
        bmlProjectDao.getProjectIdByName("testName");
    }

    @Test
    void attachResourceAndProject() {
        createNewProject();
        bmlProjectDao.attachResourceAndProject(1, "123");
    }

    @Test
    void checkIfExists() {
        bmlProjectDao.checkIfExists(1, "123");
    }

    @Test
    void deleteAllPriv() {
        setProjectPriv();
        bmlProjectDao.deleteAllPriv(1);
    }
}
