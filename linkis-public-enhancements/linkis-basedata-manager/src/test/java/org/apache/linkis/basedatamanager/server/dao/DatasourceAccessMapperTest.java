package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.DatasourceAccessEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatasourceAccessMapperTest extends BaseDaoTest{
    @Autowired
    DatasourceAccessMapper datasourceAccessMapper;

    private DatasourceAccessEntity insertDatasourceAccessMapper() {
        DatasourceAccessEntity datasourceAccessEntity = new DatasourceAccessEntity();
        datasourceAccessEntity.setId(1L);
        datasourceAccessEntity.setAccessTime(new Date());
        datasourceAccessEntity.setFields("test");
        datasourceAccessEntity.setTableId(10L);
        datasourceAccessEntity.setApplicationId(22);
        return datasourceAccessEntity;
    }

    @Test
    void testInsertValue() {
        DatasourceAccessEntity result = insertDatasourceAccessMapper();
        assertTrue(result.getId() > 0);
    }

}
