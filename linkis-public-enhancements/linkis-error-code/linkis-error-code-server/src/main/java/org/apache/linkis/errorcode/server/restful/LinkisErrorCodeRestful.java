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

package org.apache.linkis.errorcode.server.restful;

import org.apache.linkis.errorcode.common.CommonConf;
import org.apache.linkis.errorcode.common.LinkisErrorCode;
import org.apache.linkis.errorcode.server.service.LinkisErrorCodeService;
import org.apache.linkis.server.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "linkis error code restful")
@RestController
@RequestMapping(path = "/errorcode")
public class LinkisErrorCodeRestful {

  @Autowired private LinkisErrorCodeService linkisErrorCodeService;

  @ApiOperation(value = "getErrorCodes", notes = "get error codes", response = Message.class)
  @RequestMapping(path = CommonConf.GET_ERRORCODE_URL, method = RequestMethod.GET)
  public Message getErrorCodes(HttpServletRequest request) {
    List<LinkisErrorCode> errorCodes = linkisErrorCodeService.getAllErrorCodes();
    Message message = Message.ok();
    message.data("errorCodes", errorCodes);
    return message;
  }
}
