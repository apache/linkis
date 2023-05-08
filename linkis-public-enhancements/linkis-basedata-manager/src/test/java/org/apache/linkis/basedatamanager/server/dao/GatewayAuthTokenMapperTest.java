package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.GatewayAuthTokenEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayAuthTokenMapperTest extends BaseDaoTest {

    @Autowired
    GatewayAuthTokenMapper gatewayAuthTokenMapper;

    GatewayAuthTokenEntity insert() {
        GatewayAuthTokenEntity gatewayAuthTokenEntity = new GatewayAuthTokenEntity();
        gatewayAuthTokenEntity.setCreateTime(new Date());
        gatewayAuthTokenEntity.setTokenName("tokenName");
        gatewayAuthTokenEntity.setBusinessOwner("busindessOwner");
        gatewayAuthTokenEntity.setElapseDay(1L);
        gatewayAuthTokenEntity.setLegalHosts("legalHosts");
        gatewayAuthTokenEntity.setLegalUsers("legalUsers");
        gatewayAuthTokenEntity.setUpdateBy("updateBy");
        gatewayAuthTokenEntity.setUpdateTime(new Date());
        gatewayAuthTokenMapper.insert(gatewayAuthTokenEntity);
        return gatewayAuthTokenEntity;
    }

    @Test
    void getListByPage() {
        insert();
        List<GatewayAuthTokenEntity> list = gatewayAuthTokenMapper.getListByPage("legal");
        assertTrue(list.size() > 0);
    }

}