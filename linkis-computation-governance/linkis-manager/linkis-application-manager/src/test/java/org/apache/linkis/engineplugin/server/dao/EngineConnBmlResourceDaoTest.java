package org.apache.linkis.engineplugin.server.dao;

import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.vo.EnginePluginBMLVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineConnBmlResourceDaoTest extends BaseDaoTest{

    @Autowired
    EngineConnBmlResourceDao engineConnBmlResourceDao;

    @Test
    void save() {
        EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
        engineConnBmlResource.setEngineConnType("engineConnType");
        engineConnBmlResource.setVersion("version");
        engineConnBmlResource.setFileName("filename");
        engineConnBmlResource.setFileSize(1L);
        engineConnBmlResource.setLastModified(1L);
        engineConnBmlResource.setBmlResourceId("1");
        engineConnBmlResource.setBmlResourceVersion("bmlResourceVersion");
        engineConnBmlResource.setCreateTime(new Date());
        engineConnBmlResource.setLastUpdateTime(new Date());
        engineConnBmlResourceDao.save(engineConnBmlResource);
        EnginePluginBMLVo enginePluginBMLVo = new EnginePluginBMLVo("engineConnType","version");
        List<EngineConnBmlResource> list = engineConnBmlResourceDao.selectByPageVo(enginePluginBMLVo);
        assertTrue(list.size() > 0);
    }

    @Test
    void getTypeList() {
        save();
        List<String> list = engineConnBmlResourceDao.getTypeList();
        assertTrue(list.size() > 0);
    }

    @Test
    void getTypeVersionList() {
        save();
        List<String> list = engineConnBmlResourceDao.getTypeVersionList("engineConnType");
        assertTrue(list.size() > 0);
    }

    @Test
    void selectByPageVo() {
        save();
        EnginePluginBMLVo enginePluginBMLVo = new EnginePluginBMLVo("engineConnType","version");
        List<EngineConnBmlResource> list = engineConnBmlResourceDao.selectByPageVo(enginePluginBMLVo);
        assertTrue(list.size() > 0);
    }

    @Test
    void getAllEngineConnBmlResource() {
        save();
        List<EngineConnBmlResource> list = engineConnBmlResourceDao.getAllEngineConnBmlResource("engineConnType","version");
        assertTrue(list.size() > 0);
    }

    @Test
    void update() {
        save();
        EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
        engineConnBmlResource.setEngineConnType("engineConnType");
        engineConnBmlResource.setVersion("version");
        engineConnBmlResource.setFileName("filename");
        engineConnBmlResource.setFileSize(2L);
        engineConnBmlResource.setLastModified(2L);
        engineConnBmlResource.setBmlResourceId("1");
        engineConnBmlResource.setBmlResourceVersion("bmlResourceVersion2");
        engineConnBmlResource.setLastUpdateTime(new Date());
        engineConnBmlResourceDao.update(engineConnBmlResource);
    }

    @Test
    void delete() {
        save();
        EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
        engineConnBmlResource.setEngineConnType("engineConnType");
        engineConnBmlResource.setVersion("version");
        engineConnBmlResource.setFileName("filename");
        engineConnBmlResourceDao.delete(engineConnBmlResource);
        List<EngineConnBmlResource> list = engineConnBmlResourceDao.getAllEngineConnBmlResource("engineConnType","version");
        assertTrue(list.size() == 0);
    }

}