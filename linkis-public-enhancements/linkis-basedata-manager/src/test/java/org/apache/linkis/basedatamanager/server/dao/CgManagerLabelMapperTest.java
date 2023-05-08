package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.CgManagerLabel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CgManagerLabelMapperTest extends BaseDaoTest {

    @Autowired
    CgManagerLabelMapper cgManagerLabelMapper;

    @Test
    void selectById() {
        CgManagerLabel cgManagerLabel = cgManagerLabelMapper.selectById(1);
        assertTrue(cgManagerLabel != null);
    }

    @Test
    void getEngineList() {
        List<CgManagerLabel> engineList = cgManagerLabelMapper.getEngineList();
        assertTrue(engineList.size() > 0);
    }


}