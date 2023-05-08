package org.apache.linkis.configuration.dao;

import org.apache.linkis.configuration.entity.TenantVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTenantMapperTest extends BaseDaoTest {


    @Autowired
    UserTenantMapper userTenantMapper;

    TenantVo insert() {
        TenantVo tenantVo = new TenantVo();
        tenantVo.setUser("user");
        tenantVo.setCreateTime(new Date());
        tenantVo.setCreator("creator");
        tenantVo.setTenantValue("tenantValue");
        tenantVo.setUpdateTime(new Date());
        tenantVo.setBussinessUser("bussinessUser");
        tenantVo.setDesc("desc");
        userTenantMapper.createTenant(tenantVo);
        return tenantVo;
    }

    @Test
    void createTenant() {
        insert();
        List<TenantVo> tenantVos = userTenantMapper.queryTenantList("user", "creator", "tenantValue");
        assertTrue(tenantVos.size() > 0);
    }

    @Test
    void deleteTenant() {
        insert();
        TenantVo tenantVo = userTenantMapper.queryTenant("user", "creator");
        userTenantMapper.deleteTenant(Integer.valueOf(tenantVo.getId()));
        List<TenantVo> tenantVos = userTenantMapper.queryTenantList("user", "creator", "tenantValue");
        assertTrue(tenantVos.size() == 0);
    }

    @Test
    void updateTenant() {
        insert();
        TenantVo tenantVo = userTenantMapper.queryTenant("user", "creator");
        TenantVo updateTenantVo = new TenantVo();
        updateTenantVo.setId(tenantVo.getId());
        updateTenantVo.setDesc("desc2");
        updateTenantVo.setBussinessUser("bussinessUser2");
        userTenantMapper.updateTenant(updateTenantVo);
        TenantVo queryTenant = userTenantMapper.queryTenant("user", "creator");
        assertTrue(queryTenant.getDesc().equals("desc2"));
        assertTrue(queryTenant.getBussinessUser().equals("bussinessUser2"));
    }

    @Test
    void queryTenant() {
        insert();
        TenantVo tenantVo = userTenantMapper.queryTenant("user", "creator");
        assertTrue(tenantVo != null);
    }

}