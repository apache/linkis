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

package org.apache.linkis.cs.client.test.restful;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.history.CommonResourceHistory;
import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.resource.LinkisBMLResource;
import org.apache.linkis.cs.common.entity.resource.Resource;
import org.apache.linkis.cs.common.entity.source.*;

import java.util.List;

public class RestfulTest {
  /** setValueByKey */
  ContextClient contextClient = null;

  ContextID contextID = null;

  ContextKey contextKey = null;

  ContextValue contextValue = null;

  ContextKeyValue keyValue = null;

  CommonResourceHistory history = null;

  Resource resource = new LinkisBMLResource();

  /*@BeforeEach*/
  public void init() {
    contextClient = ContextClientFactory.getOrCreateContextClient();
    contextID = new LinkisHAWorkFlowContextID();
    contextID.setContextId("84716");

    contextKey = new CommonContextKey();
    contextKey.setContextScope(ContextScope.FRIENDLY);
    contextKey.setContextType(ContextType.ENV);
    contextKey.setKey("project1.flow1.node1.key2");

    contextValue = new CommonContextValue();
    LinkisBMLResource resource = new LinkisBMLResource();
    resource.setResourceId("dfasdfsr2456wertg");
    resource.setVersion("v000002");
    contextValue.setValue(resource);

    keyValue = new CommonContextKeyValue();
    keyValue.setContextKey(contextKey);
    keyValue.setContextValue(contextValue);

    resource.setResourceId("edtr44356-34563456");
    resource.setVersion("v000004");

    history = new CommonResourceHistory();
    history.setSource("history source");
    history.setResource(resource);
  }

  /**
   * setValueByKey
   *
   * @throws ErrorException
   */
  /* @org.junit.Test*/
  public void test01() throws ErrorException {
    contextClient.update(contextID, contextKey, contextValue);
  }

  /**
   * reset
   *
   * @throws ErrorException
   */
  /* @org.junit.Test*/
  public void test02() throws ErrorException {
    contextClient.reset(contextID, contextKey);
  }

  /** createHistory */
  /* @org.junit.Test*/
  public void Test03() throws ErrorException {
    contextClient.createHistory(contextID, history);
  }

  /**
   * removeHistory
   *
   * @throws ErrorException
   */
  /*@org.junit.Test*/
  public void Test04() throws ErrorException {
    contextClient.removeHistory(contextID, history);
  }

  /**
   * getHistories
   *
   * @throws ErrorException
   */
  /*@org.junit.Test*/
  public void Test05() throws ErrorException {
    List<ContextHistory> histories = contextClient.getHistories(contextID);
    System.out.println(histories.size());
  }

  /**
   * getHistoriy
   *
   * @throws ErrorException
   */
  /* @org.junit.Test*/
  public void Test06() throws ErrorException {
    ContextHistory getsource = contextClient.getHistory(contextID, "getsource");
    System.out.println(getsource.getSource());
  }

  /**
   * searchHistory
   *
   * @throws ErrorException
   */
  /*@org.junit.Test*/
  public void Test07() throws ErrorException {
    List<ContextHistory> contextHistories = contextClient.searchHistory(contextID, "key1");
    System.out.println(contextHistories.size());
  }
}
