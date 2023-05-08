package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.ConfigurationKeyEngineRelation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationKeyEngineRelationMapperTest extends BaseDaoTest {

    @Autowired
    ConfigurationKeyEngineRelationMapper relationMapper;

    ConfigurationKeyEngineRelation insert() {
        ConfigurationKeyEngineRelation configurationKeyEngineRelation = new ConfigurationKeyEngineRelation();
        configurationKeyEngineRelation.setConfigKeyId(1L);
        configurationKeyEngineRelation.setEngineTypeLabelId(1L);
        int insert = relationMapper.insert(configurationKeyEngineRelation);
        System.out.println(insert > 0);
        return configurationKeyEngineRelation;
    }

    @Test
    void testInsert() {
        insert();
    }

    @Test
    void deleteByKeyId() {
        ConfigurationKeyEngineRelation insert = insert();
        int i = relationMapper.deleteByKeyId(insert.getConfigKeyId());
        assertTrue(i > 0);
    }

}