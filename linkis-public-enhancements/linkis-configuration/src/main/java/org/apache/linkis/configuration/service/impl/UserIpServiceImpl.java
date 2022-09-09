package org.apache.linkis.configuration.service.impl;

import org.apache.linkis.configuration.dao.UserIpMapper;
import org.apache.linkis.configuration.entity.UserIpVo;
import org.apache.linkis.configuration.service.UserIpService;
import org.apache.linkis.governance.common.protocol.conf.UserIpRequest;
import org.apache.linkis.governance.common.protocol.conf.UserIpResponse;
import org.apache.linkis.rpc.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserIpServiceImpl implements UserIpService {

    @Autowired
    private UserIpMapper userIpMapper;

    @Override
    public UserIpResponse getUserIpData(UserIpRequest request, Sender sender) {
        UserIpVo userIpVo = userIpMapper.queryUserIP(request.user(), request.creator());
        return new UserIpResponse(userIpVo.getUser(), userIpVo.getCreator(), userIpVo.getIpList());
    }
}
