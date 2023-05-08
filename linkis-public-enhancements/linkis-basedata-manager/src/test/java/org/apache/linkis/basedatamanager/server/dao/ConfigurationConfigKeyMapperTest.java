package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationConfigKeyMapperTest extends BaseDaoTest {

    @Autowired
    ConfigurationConfigKeyMapper configurationConfigKeyMapper;

    private ConfigurationConfigKey insert() {
        ConfigurationConfigKey configKey = new ConfigurationConfigKey();
        configKey.setKey("key");
        configKey.setAdvanced(1);
        configKey.setDefaultValue("defaultValue");
        configKey.setDescription("desc");
        configKey.setEngineConnType("engineConnType");
        configKey.setHidden(1);
        configKey.setLevel(1);
        configKey.setTreeName("treeName");
        configKey.setName("name");
        configKey.setValidateRange("validateRange");
        configKey.setValidateType("validateType");
        int insert = configurationConfigKeyMapper.insert(configKey);
        assertTrue(insert > 0);
        return configKey;
    }

    @Test
    void testInsert() {
        insert();
    }

    @Test
    void testUpdate() {
        ConfigurationConfigKey insert = insert();
        ConfigurationConfigKey configKey = new ConfigurationConfigKey();
        configKey.setKey("key2");
        configKey.setAdvanced(0);
        configKey.setDefaultValue("defaultValue2");
        configKey.setDescription("desc2");
        configKey.setEngineConnType("engineConnType2");
        configKey.setHidden(0);
        configKey.setLevel(2);
        configKey.setTreeName("treeName2");
        configKey.setName("name2");
        configKey.setValidateRange("validateRange2");
        configKey.setValidateType("validateType2");
        configKey.setId(insert.getId());
        int updateKey = configurationConfigKeyMapper.updateById(configKey);
        assertTrue(updateKey > 0);
    }

    @Test
    void getTemplateListByLabelId() {
        configurationConfigKeyMapper.getTemplateListByLabelId("6");
    }

    @Test
    void deleteById() {
        ConfigurationConfigKey insert = insert();
        int deleteKey = configurationConfigKeyMapper.deleteById(insert.getId());
        assertTrue(deleteKey > 0);
    }

}