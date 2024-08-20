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

package org.apache.linkis.cs.server.restful;

import org.apache.linkis.cs.common.entity.listener.CommonContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.listener.CommonContextKeyListenerDomain;
import org.apache.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.service.ContextListenerService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.text.MessageFormat;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static org.apache.linkis.cs.errorcode.LinkisCsServerErrorCodeSummary.PARAMS_CANNOT_EMPTY;

@Api(tags = "cs(contextservice) listener operation")
@RestController
@RequestMapping(path = "/contextservice")
public class ContextListenerRestfulApi implements CsRestfulParent {

  @Autowired private ContextListenerService contextListenerService;

  @ApiOperation(value = "onBindIDListener", notes = "on bind id listener", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "onBindIDListener", method = RequestMethod.POST)
  public Message onBindIDListener(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    String source = jsonNode.get("source").textValue();
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "onBindIDListener,contextID:" + contextID.getContextId());
    ContextIDListenerDomain listener = new CommonContextIDListenerDomain();
    listener.setSource(source);
    contextListenerService.onBind(contextID, listener);
    return generateMessage(null, "listener");
  }

  @ApiOperation(
      value = "onBindKeyListener",
      notes = "on bind key listener",
      response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "onBindKeyListener", method = RequestMethod.POST)
  public Message onBindKeyListener(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException, IOException, ClassNotFoundException {
    String source = jsonNode.get("source").textValue();
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(
        req, "onBindKeyListener,contextID:" + contextID.getContextId());
    ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
    CommonContextKeyListenerDomain listener = new CommonContextKeyListenerDomain();
    listener.setSource(source);
    contextListenerService.onBind(contextID, contextKey, listener);
    return generateMessage(null, "listener");
  }

  @ApiOperation(value = "heartbeat", notes = "heart beat", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "heartbeat", method = RequestMethod.POST)
  public Message heartbeat(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException {
    ModuleUserUtils.getOperationUser(req, "heartbeat");
    String source = jsonNode.get("source").textValue();
    Object res = contextListenerService.heartbeat(source);
    return generateMessage(res, "ContextKeyValueBean");
  }

  @Override
  public ServiceType getServiceType() {
    return ServiceType.CONTEXT_LISTENER;
  }
}
