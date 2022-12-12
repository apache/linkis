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

package org.apache.linkis.cs.server;

import org.apache.linkis.cs.common.entity.enumeration.ExpireType;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping(path = "/contextservice")
public class ContextIDRestfulApi implements CsRestfulParent {

  @Autowired private CsScheduler csScheduler;

  @RequestMapping(path = "createContextID", method = RequestMethod.POST)
  public Message createContextID(HttpServletRequest req) throws InterruptedException {
    // contextID是client传过来的序列化的id
    PersistenceContextID contextID = new PersistenceContextID();
    contextID.setUser("hadoop");
    contextID.setExpireType(ExpireType.TODAY);
    contextID.setExpireTime(new Date());
    contextID.setInstance("cs-server1");
    contextID.setBackupInstance("cs-server2,cs-server3");
    contextID.setApplication("spark");
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID);
    return generateResponse(answerJob, "contextId");
  }

  @RequestMapping(path = "getContextID", method = RequestMethod.GET)
  public Message getContextID(
      HttpServletRequest req, @RequestParam(value = "contextId", required = false) String id)
      throws InterruptedException, CSErrorException {
    if (StringUtils.isEmpty(id)) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, id);
    return generateResponse(answerJob, "");
  }

  @RequestMapping(path = "updateContextID", method = RequestMethod.POST)
  public Message updateContextID(HttpServletRequest req)
      throws InterruptedException, CSErrorException {
    PersistenceContextID contextID = new PersistenceContextID();
    contextID.setUser("hadoop");
    contextID.setExpireType(ExpireType.NEVER);
    contextID.setExpireTime(new Date());
    contextID.setInstance("cs-server2");
    contextID.setBackupInstance("cs-server1,cs-server3");
    contextID.setApplication("hive");
    // TODO: 2020/2/25 这里要填响应的contextId
    contextID.setContextId("84714");
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.UPDATE, contextID);
    return generateResponse(answerJob, "");
  }

  @RequestMapping(path = "resetContextID", method = RequestMethod.POST)
  public Message resetContextID(HttpServletRequest req)
      throws InterruptedException, CSErrorException {
    String id = null;
    if (StringUtils.isEmpty(id)) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.RESET, id);
    return generateResponse(answerJob, "");
  }

  @RequestMapping(path = "removeContextID", method = RequestMethod.POST)
  public Message removeContextID(HttpServletRequest req, @RequestBody JsonNode json)
      throws InterruptedException, CSErrorException {
    String id = json.get("contextId").textValue();
    if (StringUtils.isEmpty(id)) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, id);
    return generateResponse(answerJob, "");
  }

  @Override
  public ServiceType getServiceType() {
    return ServiceType.CONTEXT_ID;
  }

  @Override
  public CsScheduler getScheduler() {
    return this.csScheduler;
  }
}
