package org.apache.linkis.manager.rm.external.dao;

import org.apache.linkis.engineplugin.server.dao.BaseDaoTest;
import org.apache.linkis.manager.rm.external.domain.ExternalResourceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExternalResourceProviderDaoTest extends BaseDaoTest {

    @Autowired
    ExternalResourceProviderDao externalResourceProviderDao;

    @Test
    public void selectByResourceType(){
        List<ExternalResourceProvider> list = externalResourceProviderDao.selectByResourceType("Yarn");
        assertTrue(list.size() >= 0);
    }

}