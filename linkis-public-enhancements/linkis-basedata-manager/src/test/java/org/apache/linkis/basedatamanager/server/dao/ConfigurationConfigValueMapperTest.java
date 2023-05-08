package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationConfigValueMapperTest extends BaseDaoTest {

    @Autowired
    ConfigurationConfigValueMapper configValueMapper;

    ConfigurationConfigValue insert() {
        ConfigurationConfigValue configValue = new ConfigurationConfigValue();
        configValue.setConfigValue("configValue");
        configValue.setConfigKeyId(1L);
        configValue.setConfigLabelId(1);
        configValue.setCreateTime(LocalDateTime.now());
        configValue.setUpdateTime(LocalDateTime.now());
        int insert = configValueMapper.insert(configValue);
        System.out.println(insert > 0);
        return configValue;
    }

    @Test
    void testInsert() {
        insert();
    }

    @Test
    void updateByKeyId() {
        ConfigurationConfigValue insert = insert();
        insert.setConfigValue("configVaule2");
        int i = configValueMapper.updateByKeyId(insert);
        assertTrue(i > 0);
    }

    @Test
    void deleteByKeyId() {
        ConfigurationConfigValue insert = insert();
        int i = configValueMapper.deleteByKeyId(insert.getConfigKeyId());
        assertTrue(i > 0);
    }


}