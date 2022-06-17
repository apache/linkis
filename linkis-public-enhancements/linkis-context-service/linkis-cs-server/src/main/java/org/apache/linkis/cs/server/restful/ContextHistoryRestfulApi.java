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

package org.apache.linkis.cs.server.restful;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.linkis.MessageJava;
import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.server.enumeration.ServiceMethod;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.scheduler.CsScheduler;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.server.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


@Api(tags = "上下文历史记录服务")
@RestController
@RequestMapping(path = "/contextservice")
public class ContextHistoryRestfulApi implements CsRestfulParent {

    @Autowired private CsScheduler csScheduler;

    private ObjectMapper objectMapper = new ObjectMapper();
    @ApiOperation(value="创建历史记录",notes="创建历史记录",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="contextHistory",dataType="String",value="历史上下文"),
            @ApiImplicitParam(name="contextID",dataType="String",value="上下文id")
    })
    @RequestMapping(path = "createHistory", method = RequestMethod.POST)
    public Message createHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextHistory history = getContextHistoryFromJsonNode(jsonNode);
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        // source and contextid cannot be empty
        if (StringUtils.isEmpty(history.getSource())) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID, history);
        return generateResponse(answerJob, "");
    }

    @ApiOperation(value="删除历史记录",notes="删除历史记录",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="contextHistory",dataType="String",value="历史上下文"),
            @ApiImplicitParam(name="contextID",dataType="String",value="上下文id")
    })
    @RequestMapping(path = "removeHistory", method = RequestMethod.POST)
    public Message removeHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextHistory history = getContextHistoryFromJsonNode(jsonNode);
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        // source and contextid cannot be empty
        if (StringUtils.isEmpty(history.getSource())) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, history);
        return generateResponse(answerJob, "");
    }

    @ApiOperation(value="获取多个历史记录",notes="获取多个历史记录",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="contextID",dataType="String",value="上下文id")
    })
    @RequestMapping(path = "getHistories", method = RequestMethod.POST)
    public Message getHistories(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID);
        Message message = generateResponse(answerJob, "contextHistory");
        return message;
    }

    @ApiOperation(value="获取历史记录",notes="获取历史记录",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="contextID",dataType="String",value="上下文Id"),
            @ApiImplicitParam(name="source",dataType="String",value="上下文源")
    })
    @RequestMapping(path = "getHistory", method = RequestMethod.POST)
    public Message getHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        // ContextID contextID, String source
        String source = jsonNode.get("source").textValue();
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        // source and contextid cannot be empty
        if (StringUtils.isEmpty(source)) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, source);
        Message message = generateResponse(answerJob, "contextHistory");
        return message;
    }

    @ApiOperation(value="搜索历史记录",notes="搜索历史记录",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="contextID",dataType="String",value="上下文Id"),
            @ApiImplicitParam(name="keywords",dataType="String",value="关键词")
    })
    @RequestMapping(path = "searchHistory", method = RequestMethod.POST)
    public Message searchHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        // ContextID contextID, String[] keywords
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        String[] keywords = objectMapper.treeToValue(jsonNode.get("keywords"), String[].class);
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, keywords);
        Message message = generateResponse(answerJob, "contextHistory");
        return message;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.CONTEXT_HISTORY;
    }

    @Override
    public CsScheduler getScheduler() {
        return this.csScheduler;
    }
}
