/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.variable.restful.api;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.linkis.MessageJava;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Api(tags = "全局变量")
@RestController
@RequestMapping(path = "/variable")
public class VariableRestfulApi {

    @Autowired private VariableService variableService;

    ObjectMapper mapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /*@RequestMapping(path = "addGlobalVariable",method = RequestMethod.POST)
    public Message addGlobalVariable(HttpServletRequest req,@RequestBody JsonNode json) throws IOException {
        String userName = SecurityFilter.getLoginUsername(req);
        List globalVariables = mapper.readValue(json.get("globalVariables"), List.class);
        globalVariables.stream().forEach(f -> {
            String j = BDPJettyServerHelper.gson().toJson(f);
            variableService.addGlobalVariable(BDPJettyServerHelper.gson().fromJson(j, VarKeyValueVO.class), userName);
        });
        return Message.ok();
    }

    @RequestMapping(path = "removeGlobalVariable",method = RequestMethod.POST)
    public Message removeGlobalVariable(HttpServletRequest req, JsonNode json) {
        String userName = SecurityFilter.getLoginUsername(req);
        Long keyID = json.get("keyID").getLongValue();
        variableService.removeGlobalVariable(keyID);
        return Message.ok();
    }*/
    @ApiOperation(value="全局变量列表",notes="获取全局变量清单" ,response = MessageJava.class)
   /* @ApiOperationSupport(
            responses = @DynamicResponseParameters(properties = {
                    @DynamicParameter(value = "结果集",name = "data",dataTypeClass = MessageJava.class)
            })
    )*/
    @RequestMapping(path = "listGlobalVariable", method = RequestMethod.GET)
    public Message listGlobalVariable(HttpServletRequest req) {
        String userName = ModuleUserUtils.getOperationUser(req, "listGlobalVariable ");
        List<VarKeyValueVO> kvs = variableService.listGlobalVariable(userName);
        return Message.ok().data("globalVariables", kvs);
    }
    @ApiOperation(value="添加全局变量",notes="添加全局变量" ,response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="globalVariables",dataType="Map",required=true,value="新增参数数据一对多key:globalVariables,value:List"),
            @ApiImplicitParam(name="key",dataType="String",required=true,value="参数名称，属于globalVariables"),
            @ApiImplicitParam(name="value",dataType="List",required=true,value="变量值，跟key属于键值对 属于被globalVariables包含")
    })
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
