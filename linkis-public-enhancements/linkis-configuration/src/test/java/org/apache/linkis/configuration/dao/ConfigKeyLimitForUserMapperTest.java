package org.apache.linkis.configuration.dao;
import java.util.Date;

import org.apache.linkis.configuration.entity.ConfigKeyLimitForUser;
import org.apache.linkis.configuration.entity.TemplateConfigKey;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfigKeyLimitForUserMapperTest extends BaseDaoTest {
    @Autowired
    ConfigKeyLimitForUserMapper configKeyLimitForUserMapper;

    String uuid = UUID.randomUUID().toString();
    String name = "for-test";

    private List<ConfigKeyLimitForUser> initData() {
        List<ConfigKeyLimitForUser> list = Instancio.ofList(ConfigKeyLimitForUser.class).size(9).create();
        ConfigKeyLimitForUser configKeyLimitForUser = new ConfigKeyLimitForUser();
        configKeyLimitForUser.setUserName("testuser");
        configKeyLimitForUser.setCombinedLabelValue("IDE-hadoop,spark-2.3.3");
        configKeyLimitForUser.setKeyId(1L);
        configKeyLimitForUser.setLatestUpdateTemplateUuid(uuid);
        configKeyLimitForUser.setCreateBy("test");
        configKeyLimitForUser.setUpdateBy("test");
        list.add(configKeyLimitForUser);
        configKeyLimitForUserMapper.batchInsertList(list);
        return list;
    }


    @Test
    void batchInsertOrUpdateListTest() {
        List<ConfigKeyLimitForUser> list=initData();
        list.get(1).setLatestUpdateTemplateUuid("123456");
        int isOk=configKeyLimitForUserMapper.batchInsertOrUpdateList(list);
        assertEquals(isOk, 1);

    }
}