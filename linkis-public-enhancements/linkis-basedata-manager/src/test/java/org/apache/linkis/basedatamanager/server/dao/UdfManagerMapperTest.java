package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.UdfManagerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UdfManagerMapperTest extends BaseDaoTest {

    @Autowired
    UdfManagerMapper udfManagerMapper;

    UdfManagerEntity insert() {
        UdfManagerEntity udfManagerEntity = new UdfManagerEntity();
        udfManagerEntity.setUserName("userName");
        udfManagerMapper.insert(udfManagerEntity);
        return udfManagerEntity;
    }

    @Test
    void getListByPage() {
        insert();
        List<UdfManagerEntity> list = udfManagerMapper.getListByPage("user");
        assertTrue(list.size() > 0);
    }

}