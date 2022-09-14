package org.apache.linkis.configuration.service.impl;

import org.apache.linkis.configuration.dao.TenantMapper;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.service.TenantService;
import org.apache.linkis.governance.common.protocol.conf.TenantRequest;
import org.apache.linkis.governance.common.protocol.conf.TenantResponse;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TenantServiceImpl implements TenantService {

  private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

  @Autowired private TenantMapper tenantMapper;

  @Receiver
  @Override
  public TenantResponse getTenantData(TenantRequest request, Sender sender) {
    TenantVo tenantVo = tenantMapper.queryTenant(request.user(), request.creator());
    return new TenantResponse(tenantVo.getUser(), tenantVo.getCreator(), tenantVo.getTenantValue());
  }
}
