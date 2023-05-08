package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.UdfTreeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UdfTreeMapperTest extends BaseDaoTest {

    @Autowired
    UdfTreeMapper udfTreeMapper;

    UdfTreeEntity insert() {
        UdfTreeEntity udfTreeEntity = new UdfTreeEntity();
        udfTreeEntity.setCreateTime(new Date());
        udfTreeEntity.setUpdateTime(new Date());
        udfTreeEntity.setCategory("category");
        udfTreeEntity.setDescription("desc");
        udfTreeEntity.setName("name");
        udfTreeEntity.setParent(1L);
        udfTreeEntity.setUserName("userName");
        udfTreeMapper.insert(udfTreeEntity);
        return udfTreeEntity;
    }

    @Test
    void getListByPage() {
        insert();
        List<UdfTreeEntity> list = udfTreeMapper.getListByPage("name");
        assertTrue(list.size() > 0);
    }

}