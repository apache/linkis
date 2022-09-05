package org.apache.linkis.configuration.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.linkis.configuration.entity.TenantVo;

import java.util.List;

public interface TenantMapper {


    List<TenantVo> queryTenantList(@Param("user") String user, @Param("creator") String creator, @Param("tenant_value") String tenant);

    void deleteTenant(@Param("id") Integer id);

    void updateTenant(TenantVo tenantVo);

    void createTenant(TenantVo tenantVo);
}
