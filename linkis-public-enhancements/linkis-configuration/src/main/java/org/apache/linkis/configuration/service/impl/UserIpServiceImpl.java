package org.apache.linkis.configuration.service.impl;

import org.apache.linkis.configuration.dao.UserIpMapper;
import org.apache.linkis.configuration.entity.UserIpVo;
import org.apache.linkis.configuration.service.UserIpService;
import org.apache.linkis.governance.common.protocol.conf.UserIpRequest;
import org.apache.linkis.governance.common.protocol.conf.UserIpResponse;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserIpServiceImpl implements UserIpService {

  private static final Logger logger = LoggerFactory.getLogger(UserIpService.class);

  @Autowired private UserIpMapper userIpMapper;

  @Receiver
  @Override
  public UserIpResponse getUserIpData(UserIpRequest request, Sender sender) {
    UserIpVo userIpVo = userIpMapper.queryUserIP(request.user(), request.creator());
    return new UserIpResponse(userIpVo.getUser(), userIpVo.getCreator(), userIpVo.getIpList());
  }
}
