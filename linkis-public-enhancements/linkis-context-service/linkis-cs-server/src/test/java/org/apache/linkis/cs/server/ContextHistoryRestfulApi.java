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

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.entity.PersistenceContextHistory;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;
import org.apache.linkis.cs.server.enumeration.ServiceMethod;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.scheduler.CsScheduler;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.server.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/contextservice")
public class ContextHistoryRestfulApi implements CsRestfulParent {

  @Autowired private CsScheduler csScheduler;

  @RequestMapping(path = "createHistory", method = RequestMethod.POST)
  public Message createHistory(HttpServletRequest req)
      throws InterruptedException, CSErrorException {
    ContextHistory history = new PersistenceContextHistory();
    history.setSource("server1:prot1");
    history.setContextType(ContextType.METADATA);
    ContextID contextID = new PersistenceContextID();

    contextID.setContextId("84716");
    // source and contextid cannot be empty
    if (StringUtils.isEmpty(history.getSource())) {
      throw new CSErrorException(97000, "history source cannot be empty");
    }
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID, history);
    return generateResponse(answerJob, "");
  }

  @RequestMapping(path = "removeHistory", method = RequestMethod.POST)
  public Message removeHistory(HttpServletRequest req)
      throws InterruptedException, CSErrorException {
    ContextHistory history = new PersistenceContextHistory();
    history.setSource("server1:prot1");
    ContextID contextID = new PersistenceContextID();
    contextID.setContextId("84716");
    // source and contextid cannot be empty
    if (StringUtils.isEmpty(history.getSource())) {
      throw new CSErrorException(97000, "history source cannot be empty");
    }
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, history);
    return generateResponse(answerJob, "");
  }

  @RequestMapping(path = "getHistories", method = RequestMethod.GET)
  public Message getHistories(HttpServletRequest req)
      throws InterruptedException, CSErrorException {
    ContextID contextID = new PersistenceContextID();
    contextID.setContextId("84716");
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID);
    return generateResponse(answerJob, "");
  }

  @RequestMapping(path = "getHistory", method = RequestMethod.GET)
  public Message getHistory(HttpServletRequest req) throws InterruptedException, CSErrorException {
    // ContextID contextID, String source
    String source = "server1:prot1";
    ContextID contextID = new PersistenceContextID();
    contextID.setContextId("84716");
    // source and contextid cannot be empty
    if (StringUtils.isEmpty(source)) {
      throw new CSErrorException(97000, "history source cannot be empty");
    }
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, source);
    return generateResponse(answerJob, "");
  }

  @RequestMapping(path = "searchHistory", method = RequestMethod.GET)
  public Message searchHistory(HttpServletRequest req)
      throws InterruptedException, CSErrorException {
    // ContextID contextID, String[] keywords
    ContextID contextID = new PersistenceContextID();
    contextID.setContextId("84716");
    String[] keywords = new String[] {"keyword1", "keyword2"};
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contextId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, keywords);
    return generateResponse(answerJob, "");
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
