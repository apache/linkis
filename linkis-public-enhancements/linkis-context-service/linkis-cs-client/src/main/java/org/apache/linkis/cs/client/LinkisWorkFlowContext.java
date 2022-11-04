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

package org.apache.linkis.cs.client;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.listener.ContextIDListener;
import org.apache.linkis.cs.client.listener.ContextKeyListener;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LinkisWorkFlowContext extends LinkisContext {

  private ContextID contextID;

  private ContextClient contextClient;

  private String user;

  private Map<ContextKey, ContextValue> keyValues = new ConcurrentHashMap<>();

  @Override
  public ContextID getContextID() {
    return this.contextID;
  }

  @Override
  public void setContextID(ContextID contextID) {
    this.contextID = contextID;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public ContextValue getContextValue(ContextKey contextKey) throws ErrorException {
    if (keyValues.containsKey(contextKey)) {
      return keyValues.get(contextKey);
    } else {
      return this.contextClient.getContextValue(this.contextID, contextKey);
    }
  }

  @Override
  public void setContextKeyAndValue(ContextKeyValue contextKeyValue) throws ErrorException {
    // todo 这个地方是为了进行一次缓存 不过有待商榷
    // this.setLocal(contextKeyValue);
    this.contextClient.setContextKeyValue(this.contextID, contextKeyValue);
  }

  @Override
  public void set(ContextKey contextKey, ContextValue contextValue) throws ErrorException {
    setLocal(contextKey, contextValue);
    ContextKeyValue contextKeyValue = new CommonContextKeyValue();
    contextKeyValue.setContextKey(contextKey);
    contextKeyValue.setContextValue(contextValue);
    this.setContextKeyAndValue(contextKeyValue);
  }

  @Override
  public void setLocal(ContextKey contextKey, ContextValue contextValue) {
    this.keyValues.put(contextKey, contextValue);
  }

  @Override
  public void setLocal(ContextKeyValue contextKeyValue) {
    this.keyValues.put(contextKeyValue.getContextKey(), contextKeyValue.getContextValue());
  }

  @Override
  public List<ContextKeyValue> searchContext(
      List<ContextType> contextTypes,
      List<ContextScope> contextScopes,
      List<String> contains,
      List<String> regex)
      throws ErrorException {
    return this.contextClient.search(contextID, contextTypes, contextScopes, contains, regex);
  }

  @Override
  public void reset(ContextKey contextKey) throws ErrorException {
    this.contextClient.reset(contextID, contextKey);
  }

  @Override
  public void reset() throws ErrorException {
    this.contextClient.reset(this.contextID);
  }

  @Override
  public void remove(ContextKey contextKey) throws ErrorException {
    this.contextClient.remove(contextID, contextKey);
  }

  @Override
  public void removeAll() throws ErrorException {
    this.contextClient.remove(contextID, null);
  }

  @Override
  public void onBind(ContextIDListener contextIDListener) throws ErrorException {
    contextIDListener.setContextID(this.contextID);
    contextIDListener.setContext(this);
    this.contextClient.bindContextIDListener(contextIDListener);
  }

  @Override
  public void onBind(ContextKey contextKey, ContextKeyListener contextKeyListener)
      throws ErrorException {
    contextKeyListener.setContextKey(contextKey);
    contextKeyListener.setContext(this);
    this.contextClient.bindContextKeyListener(contextKeyListener);
  }

  public ContextClient getContextClient() {
    return contextClient;
  }

  public void setContextClient(ContextClient contextClient) {
    this.contextClient = contextClient;
  }
}
