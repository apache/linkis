/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.configuration.service.impl;

import org.apache.linkis.configuration.entity.UserIpVo;
import org.apache.linkis.configuration.service.UserIpConfigService;
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

  @Autowired private UserIpConfigService userIpConfigService;

  @Receiver
  @Override
  public UserIpResponse getUserIpData(UserIpRequest request, Sender sender) {
    UserIpVo userIpVo = userIpConfigService.queryUserIP(request.user(), request.creator());
    if (null == userIpVo) {
      logger.warn(
          "UserIpCache user {} creator {} data loading failed", request.user(), request.creator());
      return new UserIpResponse(request.user(), request.creator(), "");
    } else {
      return new UserIpResponse(userIpVo.getUser(), userIpVo.getCreator(), userIpVo.getIpList());
    }
  }
}
