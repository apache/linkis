package org.apache.linkis.configuration.service;

import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.server.Message;

import java.util.List;

public interface TenantConfigService {

    List<TenantVo> queryTenantList(String user, String creator, String tenant);

    void deleteTenant(Integer id);

    Message updateTenant(TenantVo tenantVo);

    Message createTenant(TenantVo tenantVo);
}
