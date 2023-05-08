package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.RmExternalResourceProviderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RmExternalResourceProviderMapperTest extends BaseDaoTest {


    @Autowired
    RmExternalResourceProviderMapper rmExternalResourceProviderMapper;

    RmExternalResourceProviderEntity insert() {
        RmExternalResourceProviderEntity rmExternalResourceProviderEntity = new RmExternalResourceProviderEntity();
        rmExternalResourceProviderEntity.setResourceType("resourceType");
        rmExternalResourceProviderEntity.setConfig("config");
        rmExternalResourceProviderEntity.setLabels("labels");
        rmExternalResourceProviderEntity.setName("name");
        rmExternalResourceProviderMapper.insert(rmExternalResourceProviderEntity);
        return rmExternalResourceProviderEntity;
    }

    @Test
    void getListByPage() {
        insert();
        List<RmExternalResourceProviderEntity> list = rmExternalResourceProviderMapper.getListByPage("a");
        assertTrue(list.size() > 0);
    }

}