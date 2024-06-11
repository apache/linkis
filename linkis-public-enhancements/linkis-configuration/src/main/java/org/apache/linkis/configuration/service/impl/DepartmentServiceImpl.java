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

import org.apache.linkis.configuration.dao.DepartmentMapper;
import org.apache.linkis.configuration.entity.DepartmentVo;
import org.apache.linkis.configuration.service.DepartmentService;
import org.apache.linkis.governance.common.protocol.conf.DepartmentRequest;
import org.apache.linkis.governance.common.protocol.conf.DepartmentResponse;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DepartmentServiceImpl implements DepartmentService {

  private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

  @Autowired private DepartmentMapper departmentMapper;

  @Receiver
  @Override
  public DepartmentResponse getDepartmentByUser(
      DepartmentRequest departmentRequest, Sender sender) {
    DepartmentVo departmentVo = departmentMapper.getDepartmentByUser(departmentRequest.user());
    if (null == departmentVo) {
      logger.warn(
          "Department data loading failed user {},department cache will set ''  ",
          departmentRequest.user());
      return new DepartmentResponse(departmentRequest.user(), "", "");
    } else {
      return new DepartmentResponse(
          departmentRequest.user(), departmentVo.getOrgId(), departmentVo.getOrgName());
    }
  }
}
