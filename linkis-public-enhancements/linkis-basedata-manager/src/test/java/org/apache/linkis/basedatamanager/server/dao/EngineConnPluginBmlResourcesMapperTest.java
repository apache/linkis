package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.EngineConnPluginBmlResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineConnPluginBmlResourcesMapperTest extends BaseDaoTest {

    @Autowired
    EngineConnPluginBmlResourcesMapper engineConnPluginBmlResourcesMapper;

    EngineConnPluginBmlResources insert() {
        EngineConnPluginBmlResources engineConnPluginBmlResources = new EngineConnPluginBmlResources();
        engineConnPluginBmlResources.setBmlResourceId("dmlResourceId");
        engineConnPluginBmlResources.setEngineConnType("engineConnType");
        engineConnPluginBmlResources.setBmlResourceVersion("bmlResourceVersion");
        engineConnPluginBmlResources.setCreateTime(new Date());
        engineConnPluginBmlResources.setFileName("fileName");
        engineConnPluginBmlResources.setFileSize(1L);
        engineConnPluginBmlResources.setLastModified(1L);
        engineConnPluginBmlResources.setLastUpdateTime(new Date());
        engineConnPluginBmlResources.setVersion("version");
        engineConnPluginBmlResourcesMapper.insert(engineConnPluginBmlResources);
        return engineConnPluginBmlResources;
    }

    @Test
    void getListByPage() {
        insert();
        List<String> list = engineConnPluginBmlResourcesMapper.getEngineTypeList();
        assertTrue(list.size() > 0);
    }


}