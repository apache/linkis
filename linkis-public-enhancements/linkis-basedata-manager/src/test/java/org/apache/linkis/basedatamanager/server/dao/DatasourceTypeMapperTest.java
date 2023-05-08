package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasourceTypeMapperTest extends BaseDaoTest {

    @Autowired
    DatasourceTypeMapper datasourceTypeMapper;

    @Test
    void getListByPage() {
        List<DatasourceTypeEntity> list = datasourceTypeMapper.getListByPage("kafka");
        assertTrue(list.size() > 0);
    }

}