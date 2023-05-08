package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.DatasourceEnvEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasourceEnvMapperTest extends BaseDaoTest {

    @Autowired
    DatasourceEnvMapper datasourceEnvMapper;

    DatasourceEnvEntity insert() {
        DatasourceEnvEntity datasourceEnv = new DatasourceEnvEntity();
        datasourceEnv.setDatasourceTypeId(1);
        datasourceEnv.setEnvDesc("envDesc");
        datasourceEnv.setEnvName("envName");
        datasourceEnv.setCreateTime(new Date());
        datasourceEnv.setCreateUser("lldr");
        datasourceEnv.setModifyTime(new Date());
        datasourceEnv.setModifyUser("modifyUser");
        datasourceEnv.setParameter("parameter");
        datasourceEnvMapper.insert(datasourceEnv);
        return datasourceEnv;
    }

    @Test
    void getListByPage() {
        insert();
        List<DatasourceEnvEntity> list = datasourceEnvMapper.getListByPage("env");
        assertTrue(list.size() > 0);
    }


}