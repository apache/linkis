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

package org.apache.linkis.variable.restful.api;

import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.apache.linkis.variable.entity.VarKeyValueVO;
import org.apache.linkis.variable.exception.VariableException;
import org.apache.linkis.variable.service.VariableService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "global variable")
@RestController
@RequestMapping(path = "/variable")
public class VariableRestfulApi {

  @Autowired private VariableService variableService;

  ObjectMapper mapper = new ObjectMapper();

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @ApiOperation(
      value = "listGlobalVariable",
      notes = "list global variable",
      response = Message.class)
  @RequestMapping(path = "listGlobalVariable", method = RequestMethod.GET)
  public Message listGlobalVariable(HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "listGlobalVariable ");
    List<VarKeyValueVO> kvs = variableService.listGlobalVariable(userName);
    return Message.ok().data("globalVariables", kvs);
  }

  @ApiOperation(
      value = "saveGlobalVariable",
      notes = "save global variable",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "globalVariables", required = true, dataType = "Map"),
    @ApiImplicitParam(name = "key", required = true, dataType = "String"),
    @ApiImplicitParam(name = "value", required = true, dataType = "List"),
    @ApiImplicitParam(name = "keyID", required = true, dataType = "String", example = "2"),
    @ApiImplicitParam(name = "valueID", required = true, dataType = "List", example = "2")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "saveGlobalVariable", method = RequestMethod.POST)
  public Message saveGlobalVariable(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException, VariableException {
    String userName = ModuleUserUtils.getOperationUser(req, "saveGlobalVariable ");
    List<VarKeyValueVO> userVariables = variableService.listGlobalVariable(userName);
    List globalVariables = mapper.treeToValue(json.get("globalVariables"), List.class);
    variableService.saveGlobalVaraibles(globalVariables, userVariables, userName);
    return Message.ok();
  }
}
