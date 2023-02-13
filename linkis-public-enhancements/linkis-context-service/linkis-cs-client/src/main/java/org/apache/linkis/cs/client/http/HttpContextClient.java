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

package org.apache.linkis.cs.client.http;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.AbstractContextClient;
import org.apache.linkis.cs.client.Context;
import org.apache.linkis.cs.client.LinkisWorkFlowContext;
import org.apache.linkis.cs.client.builder.ContextClientConfig;
import org.apache.linkis.cs.client.builder.HttpContextClientConfig;
import org.apache.linkis.cs.client.listener.ContextIDListener;
import org.apache.linkis.cs.client.listener.ContextKeyListener;
import org.apache.linkis.cs.client.listener.HeartBeater;
import org.apache.linkis.cs.client.utils.ContextClientConf;
import org.apache.linkis.cs.client.utils.ContextServerHttpConf;
import org.apache.linkis.cs.client.utils.ExceptionHelper;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.source.*;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.protocol.ContextHTTPConstant;
import org.apache.linkis.cs.common.search.ContextSearchConditionMapBuilder;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.httpclient.config.ClientConfig;
import org.apache.linkis.httpclient.dws.DWSHttpClient;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.response.DWSResult;
import org.apache.linkis.httpclient.request.Action;
import org.apache.linkis.httpclient.response.Result;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.cs.client.errorcode.CsClientErrorCodeSummary.*;

/** Description: HttpContextClient是ContextClient的使用Http方式进行通信的具体实现 一般可以将其做成单例 */
public class HttpContextClient extends AbstractContextClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpContextClient.class);

  private DWSHttpClient dwsHttpClient;
  private ContextClientConfig contextClientConfig;
  private final String linkis_version = Configuration.LINKIS_WEB_VERSION().getValue();

  private final String name = "HttpContextClient";

  private static HttpContextClient httpContextClient;

  private HeartBeater heartBeater;

  private HttpContextClient() {}

  private HttpContextClient(ContextClientConfig contextClientConfig) {
    // 初始化dwsHttpClient
    this.contextClientConfig = contextClientConfig;
    if (contextClientConfig instanceof HttpContextClientConfig) {
      HttpContextClientConfig httpContextClientConfig =
          (HttpContextClientConfig) contextClientConfig;
      ClientConfig clientConfig = httpContextClientConfig.getClientConfig();
      DWSClientConfig dwsClientConfig = new DWSClientConfig(clientConfig);
      dwsClientConfig.setDWSVersion(linkis_version);
      dwsHttpClient = new DWSHttpClient(dwsClientConfig, name);
    }
    if ("true".equals(ContextClientConf.HEART_BEAT_ENABLED().getValue())) {
      this.heartBeater = new HttpHeartBeater(contextClientConfig);
      heartBeater.start();
    }
  }

  public static HttpContextClient getInstance(ContextClientConfig contextClientConfig) {
    if (httpContextClient == null) {
      synchronized (HttpContextClient.class) {
        if (httpContextClient == null) {
          httpContextClient = new HttpContextClient(contextClientConfig);
        }
      }
    }
    return httpContextClient;
  }

  @Override
  @Deprecated
  public Context createContext(
      String projectName, String flowName, String user, Map<String, Object> params)
      throws ErrorException {
    ContextCreateAction contextCreateAction = new ContextCreateAction();
    LinkisHAWorkFlowContextID contextID = new LinkisHAWorkFlowContextID();
    contextID.setProject(projectName);
    contextID.setFlow(flowName);
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    contextCreateAction.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    contextCreateAction.getRequestPayloads().put(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextCreateAction);
    } catch (Exception e) {
      LOGGER.error("create context failed", e);
      ExceptionHelper.throwErrorException(
          CREATE_CONTEXT_FAILED.getErrorCode(), CREATE_CONTEXT_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextCreateResult) {
      ContextCreateResult contextCreateResult = (ContextCreateResult) result;
      int status = contextCreateResult.getStatus();
      if (status != 0) {
        String errMsg = contextCreateResult.getMessage();
        LOGGER.error(
            "create context for project {}, flow {} failed, msg is {}",
            projectName,
            flowName,
            errMsg);
        throw new ErrorException(80014, "create context failed" + errMsg);
      } else {
        LinkisWorkFlowContext context = new LinkisWorkFlowContext();
        Map<String, Object> map = contextCreateResult.getData();
        contextID.setContextId(map.get("contextId").toString());
        context.setContextID(contextID);
        context.setContextClient(this);
        context.setUser(user);
        return context;
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public Context createContext(ContextID contextID) throws ErrorException {
    ContextCreateAction contextCreateAction = new ContextCreateAction();
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    LOGGER.info("contextIDStr: {}", contextIDStr);
    contextCreateAction.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    contextCreateAction.getRequestPayloads().put(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextCreateAction);
    } catch (Exception e) {
      LOGGER.error("create context failed", e);
      ExceptionHelper.throwErrorException(
          CREATE_CONTEXT_FAILED.getErrorCode(), CREATE_CONTEXT_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextCreateResult) {
      ContextCreateResult contextCreateResult = (ContextCreateResult) result;
      int status = contextCreateResult.getStatus();
      if (status != 0) {
        String errMsg = contextCreateResult.getMessage();
        LOGGER.error("create context failed, msg is {}", errMsg);
        throw new ErrorException(80014, "create context failed" + errMsg);
      } else {
        LinkisWorkFlowContext context = new LinkisWorkFlowContext();
        Map<String, Object> map = contextCreateResult.getData();
        contextID.setContextId(map.get("contextId").toString());
        context.setContextID(contextID);
        context.setContextClient(this);
        return context;
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public Context getContext(ContextID contextId) throws ErrorException {
    LinkisWorkFlowContext context = new LinkisWorkFlowContext();
    context.setContextID(contextId);
    context.setContextClient(this);
    return context;
  }

  @Override
  public Context getContext(String contextIDStr) throws ErrorException {
    ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
    return getContext(contextID);
  }

  @Override
  public ContextValue getContextValue(ContextID contextID, ContextKey contextKey)
      throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    String contextKeyStr = SerializeHelper.serializeContextKey(contextKey);
    ContextGetValueAction contextGetValueAction = new ContextGetValueAction();
    contextGetValueAction.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    contextGetValueAction
        .getRequestPayloads()
        .put(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    contextGetValueAction
        .getRequestPayloads()
        .put(ContextHTTPConstant.CONTEXT_KEY_STR, contextKeyStr);
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextGetValueAction);
    } catch (Exception e) {
      LOGGER.error("get context value id: {} , key: {} failed", contextIDStr, contextKeyStr, e);
      ExceptionHelper.throwErrorException(
          GET_CONTEXT_VALUE_FAILED.getErrorCode(), GET_CONTEXT_VALUE_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextGetValueResult) {
      ContextGetValueResult contextGetValueResult = (ContextGetValueResult) result;
      int status = contextGetValueResult.getStatus();
      if (status != 0) {
        String errMsg = contextGetValueResult.getMessage();
        LOGGER.error(
            "get context value id: {} , key: {} failed, msg is {}",
            contextIDStr,
            contextKeyStr,
            errMsg);
        throw new ErrorException(80014, "create context failed" + errMsg);
      } else {
        Map<String, Object> map = contextGetValueResult.getData();
        if (null == map || null == map.get("contextValue")) {
          return null;
        }
        String contextValueStr = map.get("contextValue").toString();
        return SerializeHelper.deserializeContextValue(contextValueStr);
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public void update(ContextID contextID, ContextKey contextKey, ContextValue contextValue)
      throws ErrorException {
    String contextIdStr = SerializeHelper.SERIALIZE_HELPER.serialize(contextID);
    String contextKeyValueStr =
        SerializeHelper.SERIALIZE_HELPER.serialize(
            new CommonContextKeyValue(contextKey, contextValue));
    ContextSetKeyValueAction contextSetKeyValueAction = new ContextSetKeyValueAction();
    contextSetKeyValueAction.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIdStr);
    contextSetKeyValueAction.getRequestPayloads().put("contextID", contextIdStr);
    contextSetKeyValueAction.getRequestPayloads().put("contextKeyValue", contextKeyValueStr);
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextSetKeyValueAction);
    } catch (Exception e) {
      LOGGER.error("update context failed", e);
      ExceptionHelper.throwErrorException(
          UPDATE_CONTEXT_FAILED.getErrorCode(), UPDATE_CONTEXT_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextSetKeyValueResult) {
      ContextSetKeyValueResult contextSetKeyValueResult = (ContextSetKeyValueResult) result;
      int status = contextSetKeyValueResult.getStatus();
      if (status != 0) {
        String errMsg = contextSetKeyValueResult.getMessage();
        LOGGER.error(
            "Calling client to update ContextId {} failed with error message {} returned (调用客户端去更新contextId {} 失败, 返回的错误信息是 {}) ",
            contextIdStr,
            errMsg,
            contextIdStr,
            errMsg);
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public void reset(ContextID contextID, ContextKey contextKey) throws ErrorException {
    String contextIdStr = SerializeHelper.SERIALIZE_HELPER.serialize(contextID);
    String contextKeyStr = SerializeHelper.SERIALIZE_HELPER.serialize(contextKey);
    ContextResetValueAction contextResetValueAction = new ContextResetValueAction();
    contextResetValueAction.getRequestPayloads().put("contextKey", contextKeyStr);
    contextResetValueAction.getRequestPayloads().put("contextID", contextIdStr);
    // contextResetValueAction.getParameters().put("contextId", contextID.getContextId());
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextResetValueAction);
    } catch (Exception e) {
      LOGGER.error("reset contextID {}, contextKey {}  failed", contextIdStr, contextKeyStr, e);
      ExceptionHelper.throwErrorException(
          RESET_CONTEXT_FAILED.getErrorCode(), RESET_CONTEXT_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextResetResult) {
      ContextResetResult contextResetResult = (ContextResetResult) result;
      int status = contextResetResult.getStatus();
      if (status != 0) {
        String errMsg = contextResetResult.getMessage();
        LOGGER.error(
            "ContextKey {} fails to reset the ContextId {} with error message {} (调用客户端去reset contextId {}, contextKey {} 失败, 返回的错误信息是 {} )",
            contextKeyStr,
            contextIdStr,
            errMsg,
            contextIdStr,
            contextKeyStr,
            errMsg);
        throw new ErrorException(80015, "reset contextID failed");
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public void reset(ContextID contextID) throws ErrorException {
    String contextIdStr = SerializeHelper.serializeContextID(contextID);
    ContextResetIDAction contextResetIDAction = new ContextResetIDAction();
    contextResetIDAction.getRequestPayloads().put(ContextHTTPConstant.CONTEXT_ID_STR, contextIdStr);
    // contextResetIDAction.getParameters().put("contextId", contextID.getContextId());
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextResetIDAction);
    } catch (Exception e) {
      LOGGER.error("reset contextID {} failed", contextIdStr, e);
      ExceptionHelper.throwErrorException(
          RESET_CONTEXT_FAILED.getErrorCode(), RESET_CONTEXT_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextResetIDResult) {
      ContextResetIDResult contextResetResult = (ContextResetIDResult) result;
      int status = contextResetResult.getStatus();
      if (status != 0) {
        String errMsg = contextResetResult.getMessage();
        LOGGER.error(
            "The call to the client to reset ContextId {} failed with error message {} returned(调用客户端去reset contextId {} 失败, 返回的错误信息是 {} )",
            contextIdStr,
            errMsg,
            contextIdStr,
            errMsg);
        throw new ErrorException(80015, "reset contextID failed");
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public void remove(ContextID contextID, ContextKey contextKey) throws ErrorException {
    String contextIdStr = SerializeHelper.serializeContextID(contextID);
    String contextKeyStr = SerializeHelper.serializeContextKey(contextKey);
    ContextRemoveAction contextRemoveAction = new ContextRemoveAction(contextIdStr, contextKeyStr);
    contextRemoveAction.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIdStr);
    contextRemoveAction
        .getRequestPayloads()
        .put(ContextHTTPConstant.CONTEXT_KEY_STR, contextKeyStr);
    contextRemoveAction.getRequestPayloads().put(ContextHTTPConstant.CONTEXT_ID_STR, contextIdStr);
    contextRemoveAction.getRequestPayloads().put("contextId", contextID.getContextId());
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextRemoveAction);
    } catch (Exception e) {
      LOGGER.error("remove context id {} context key {} failed", contextIdStr, contextIdStr, e);
      ExceptionHelper.throwErrorException(
          REMOVE_CONTEXT_FAILED.getErrorCode(), REMOVE_CONTEXT_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextRemoveResult) {
      ContextRemoveResult contextRemoveResult = (ContextRemoveResult) result;
      int status = contextRemoveResult.getStatus();
      if (status != 0) {
        String errMsg = contextRemoveResult.getMessage();
        LOGGER.error(
            "remove context failed contextID {}, contextKey {} ",
            contextIdStr,
            contextKey.getKey());
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public void setContextKeyValue(ContextID contextID, ContextKeyValue contextKeyValue)
      throws ErrorException {
    String contextIDStr = SerializeHelper.SERIALIZE_HELPER.serialize(contextID);
    String contextKeyValueStr = SerializeHelper.SERIALIZE_HELPER.serialize(contextKeyValue);
    ContextSetKeyValueAction action = new ContextSetKeyValueAction();
    action.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    action.getRequestPayloads().put("contextID", contextIDStr);
    action.getRequestPayloads().put("contextKeyValue", contextKeyValueStr);
    Result result = null;
    try {
      result = dwsHttpClient.execute(action);
    } catch (Exception e) {
      LOGGER.error("set value failed", e);
      ExceptionHelper.throwErrorException(
          UPDATE_CONTEXT_FAILED.getErrorCode(), REMOVE_CONTEXT_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextSetKeyValueResult) {
      ContextSetKeyValueResult contextSetKeyValueResult = (ContextSetKeyValueResult) result;
      int status = contextSetKeyValueResult.getStatus();
      if (status != 0) {
        String errMsg = contextSetKeyValueResult.getMessage();
        LOGGER.error("set value failed {} ,err is {}", contextIDStr, errMsg);
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public void bindContextIDListener(ContextIDListener contextIDListener) throws ErrorException {
    ContextID contextID = contextIDListener.getContextID();
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    ContextBindIDAction contextBindIDAction = new ContextBindIDAction();
    contextBindIDAction.getRequestPayloads().put(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    // contextBindIDAction.getParameters().put("contextId", contextID.getContextId());
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextBindIDAction);
    } catch (Exception e) {
      LOGGER.error("bind context id {} failed", contextIDStr, e);
      ExceptionHelper.throwErrorException(
          BIND_CONTEXTID_FAILED.getErrorCode(), BIND_CONTEXTID_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextBindIDResult) {
      ContextBindIDResult contextBindIDResult = (ContextBindIDResult) result;
      int status = contextBindIDResult.getStatus();
      if (status != 0) {
        String errMsg = contextBindIDResult.getMessage();
        LOGGER.error("bind context id failed {} ,err is {}", contextIDStr, errMsg);
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public void bindContextKeyListener(ContextKeyListener contextKeyListener) throws ErrorException {
    ContextID contextID = contextKeyListener.getContext().getContextID();
    ContextKey contextKey = contextKeyListener.getContextKey();
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    String contextKeyStr = SerializeHelper.serializeContextKey(contextKey);
    ContextBindKeyAction contextBindKeyAction = new ContextBindKeyAction();
    contextBindKeyAction.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    contextBindKeyAction.getRequestPayloads().put(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    contextBindKeyAction
        .getRequestPayloads()
        .put(ContextHTTPConstant.CONTEXT_KEY_STR, contextKeyStr);
    // todo 这里要改一下source的来历
    contextBindKeyAction.getRequestPayloads().put("source", name);
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextBindKeyAction);
    } catch (Exception e) {
      LOGGER.error("bind context id {} context key {} failed", contextIDStr, contextKeyStr, e);
      ExceptionHelper.throwErrorException(
          BIND_CONTEXTID_FAILED.getErrorCode(), BIND_CONTEXTID_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextBindKeyResult) {
      ContextBindKeyResult contextBindKeyResult = (ContextBindKeyResult) result;
      int status = contextBindKeyResult.getStatus();
      if (status != 0) {
        String errMsg = contextBindKeyResult.getMessage();
        LOGGER.error(
            "bind context id {} context key {} failed ,err is {}",
            contextIDStr,
            contextKeyStr,
            errMsg);
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
  }

  @Override
  public List<ContextKeyValue> search(
      ContextID contextID,
      List<ContextType> contextTypes,
      List<ContextScope> contextScopes,
      List<String> contains,
      List<String> regex)
      throws ErrorException {
    return search(
        contextID,
        contextTypes,
        contextScopes,
        contains,
        regex,
        false,
        null,
        Integer.MAX_VALUE,
        null);
  }

  @Override
  public List<ContextKeyValue> search(
      ContextID contextID,
      List<ContextType> contextTypes,
      List<ContextScope> contextScopes,
      List<String> contains,
      List<String> regex,
      boolean upstream,
      String nodeName,
      int num,
      List<Class> contextValueTypes)
      throws ErrorException {
    ContextSearchConditionMapBuilder builder = ContextSearchConditionMapBuilder.newBuilder();
    if (contextTypes != null) {
      contextTypes.forEach(builder::contextTypes);
    }
    if (contextScopes != null) {
      contextScopes.forEach(builder::contextScopes);
    }
    if (contains != null) {
      contains.forEach(builder::contains);
    }
    if (regex != null) {
      regex.forEach(builder::regex);
    }
    builder.nearest(nodeName, num, upstream);
    if (contextValueTypes != null) {
      contextValueTypes.forEach(builder::contextValueTypes);
    }
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    ContextSearchContextAction contextSearchContextAction = new ContextSearchContextAction();
    contextSearchContextAction.addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    contextSearchContextAction.getRequestPayloads().put("condition", builder.build());
    contextSearchContextAction.getRequestPayloads().put("contextID", contextIDStr);
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextSearchContextAction);
    } catch (Exception e) {
      LOGGER.error("search condition failed", e);
      ExceptionHelper.throwErrorException(
          SEARCH_CONDITION_FAILED.getErrorCode(), SEARCH_CONDITION_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextSearchResult) {
      ContextSearchResult contextSearchResult = (ContextSearchResult) result;
      int status = contextSearchResult.getStatus();
      if (status != 0) {
        String errMsg = contextSearchResult.getMessage();
        LOGGER.error("search condition failed, err is  {}", errMsg);
      } else {
        Map<String, Object> data = contextSearchResult.getData();
        if (data.get("contextKeyValue") != null) {
          List<ContextKeyValue> retKvs = new ArrayList<>();
          Object o = data.get("contextKeyValue");
          List<String> list = (List<String>) o;
          list.stream()
              .map(
                  s -> {
                    try {
                      return SerializeHelper.deserializeContextKeyValue(s);
                    } catch (ErrorException e) {
                      LOGGER.error("failed to deserialize {} to a contextKeyValue", s, e);
                      return null;
                    }
                  })
              .filter(Objects::nonNull)
              .forEach(retKvs::add);
          return retKvs;
        }
      }
    } else if (result != null) {
      LOGGER.error(
          "result is not a correct type, result type is {}", result.getClass().getSimpleName());
      throw new ErrorException(80015, "result is not a correct type");
    } else {
      LOGGER.error("result is null");
      throw new ErrorException(80015, "result is null");
    }
    return null;
  }

  @Override
  public void createHistory(ContextID contextID, ContextHistory history) throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.createContextHistory())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .with(history)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    checkDWSResult(execute(action));
  }

  @Override
  public void removeHistory(ContextID contextID, ContextHistory history) throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.removeContextHistory())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .with(history)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    checkDWSResult(execute(action));
  }

  @Override
  public List<ContextHistory> getHistories(ContextID contextID) throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.getContextHistories())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    DWSResult dwsResult = checkDWSResult(execute(action));
    ContextHistoriesGetResult result = (ContextHistoriesGetResult) dwsResult;
    ArrayList<ContextHistory> histories = new ArrayList<>();
    for (String s : result.getContextHistory()) {
      histories.add(SerializeHelper.deserializeContextHistory(s));
    }
    return histories;
  }

  @Override
  public ContextHistory getHistory(ContextID contextID, String source) throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.getContextHistory())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .with("source", source)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    DWSResult dwsResult = checkDWSResult(execute(action));
    ContextHistoryGetResult result = (ContextHistoryGetResult) dwsResult;
    return result.getContextHistory() == null
        ? null
        : SerializeHelper.deserializeContextHistory(result.getContextHistory());
  }

  @Override
  public List<ContextHistory> searchHistory(ContextID contextID, String... keyword)
      throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.searchContextHistory())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .with("keywords", keyword)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    DWSResult dwsResult = checkDWSResult(execute(action));
    ContextHistoriesGetResult result = (ContextHistoriesGetResult) dwsResult;
    ArrayList<ContextHistory> histories = new ArrayList<>();
    for (String s : result.getContextHistory()) {
      histories.add(SerializeHelper.deserializeContextHistory(s));
    }
    return histories;
  }

  @Override
  public void removeAllValueByKeyPrefixAndContextType(
      ContextID contextID, ContextType contextType, String keyPrefix) throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(
                ContextServerHttpConf.removeAllValueByKeyPrefixAndContextTypeURL())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .with(ContextHTTPConstant.CONTEXT_KEY_TYPE_STR, contextType.toString())
            .with(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR, keyPrefix)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    checkDWSResult(execute(action));
  }

  @Override
  public void removeAllValueByKeyAndContextType(
      ContextID contextID, ContextType contextType, String key) throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.removeAllValueByKeyAndContextTypeURL())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .with(ContextHTTPConstant.CONTEXT_KEY_TYPE_STR, contextType.toString())
            .with(ContextHTTPConstant.CONTEXT_KEY_STR, key)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    checkDWSResult(execute(action));
  }

  @Override
  public void removeAllValueByKeyPrefix(ContextID contextID, String keyPrefix)
      throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.removeAllValueByKeyPrefixURL())
            .with(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .with(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR, keyPrefix)
            .addHeader(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr)
            .build();
    checkDWSResult(execute(action));
  }

  @Override
  public List<String> searchHAIDByTime(
      String createTimeStart,
      String createTimeEnd,
      String updateTimeStart,
      String updateTimeEnd,
      String accessTimeStart,
      String accessTimeEnd,
      Integer pageNow,
      Integer pageSize)
      throws ErrorException {
    if (StringUtils.isBlank(createTimeStart)
        && StringUtils.isBlank(createTimeEnd)
        && StringUtils.isBlank(updateTimeStart)
        && StringUtils.isBlank(updateTimeEnd)
        && StringUtils.isBlank(accessTimeStart)
        && StringUtils.isBlank(accessTimeEnd)) {
      throw new CSErrorException(
          97000,
          " createTimeStart,  createTimeEnd,  updateTimeStart,  updateTimeEnd,  accessTimeStart,  accessTimeEnd cannot all be blank.");
    }
    ContextSearchIDByTimeAction action = new ContextSearchIDByTimeAction();
    action.setParameter("createTimeStart", createTimeStart);
    action.setParameter("createTimeEnd", createTimeEnd);
    action.setParameter("updateTimeStart", updateTimeStart);
    action.setParameter("updateTimeEnd", updateTimeEnd);
    action.setParameter("accessTimeStart", accessTimeStart);
    action.setParameter("accessTimeEnd", accessTimeEnd);
    action.setParameter("pageNow", pageNow);
    action.setParameter("pageSize", pageSize);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(action.getURL());
    }
    Result result = null;
    try {
      result = execute(action);
    } catch (Exception e) {
      LOGGER.error("searchHAIDByTime failed, {}", e.getMessage(), e);
      ExceptionHelper.throwErrorException(
          HAIDBYTIME_FAILED.getErrorCode(), HAIDBYTIME_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextSearchIDByTimeResult) {
      ContextSearchIDByTimeResult contextSearchIDByTimeResult =
          (ContextSearchIDByTimeResult) result;
      if (null != contextSearchIDByTimeResult
          && null != contextSearchIDByTimeResult.getContextIDs()) {
        return contextSearchIDByTimeResult.getContextIDs();
      } else {
        return new ArrayList<>();
      }
    } else if (null == result) {
      throw new CSErrorException(80017, "Invalid null result ");
    } else {
      throw new CSErrorException(80017, "Invalid result type : " + result.getClass().getName());
    }
  }

  @Override
  public int batchClearContextByHAID(List<String> idList) throws ErrorException {
    if (null == idList) return 0;
    if (idList.size() > CSCommonUtils.CONTEXT_MAX_PAGE_SIZE) {
      throw new CSErrorException(
          97000,
          "idList size : "
              + idList.size()
              + " is over max page size : "
              + CSCommonUtils.CONTEXT_MAX_PAGE_SIZE);
    }
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.clearAllContextByID())
            .with("idList", idList)
            .build();
    Result result = null;
    try {
      result = execute(action);
    } catch (Exception e) {
      LOGGER.error("batchClearContextByHAID failed, {}", e.getMessage(), e);
      ExceptionHelper.throwErrorException(
          CLEAR_CONTEXT_HAID_FAILED.getErrorCode(), CLEAR_CONTEXT_HAID_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextClearByIDResult) {
      ContextClearByIDResult contextClearByIDResult = (ContextClearByIDResult) result;
      if (null != contextClearByIDResult) {
        return contextClearByIDResult.num();
      } else {
        return 0;
      }
    } else if (null == result) {
      throw new CSErrorException(80017, "Invalid null result ");
    } else {
      throw new CSErrorException(80017, "Invalid result type : " + result.getClass().getName());
    }
  }

  @Override
  public int batchClearContextByTime(
      String createTimeStart,
      String createTimeEnd,
      String updateTimeStart,
      String updateTimeEnd,
      String accessTimeStart,
      String accessTimeEnd)
      throws ErrorException {
    if (StringUtils.isBlank(createTimeStart)
        && StringUtils.isBlank(createTimeEnd)
        && StringUtils.isBlank(updateTimeStart)
        && StringUtils.isBlank(updateTimeEnd)
        && StringUtils.isBlank(accessTimeStart)
        && StringUtils.isBlank(accessTimeEnd)) {
      throw new CSErrorException(
          97000,
          " createTimeStart,  createTimeEnd,  updateTimeStart,  updateTimeEnd,  accessTimeStart,  accessTimeEnd cannot all be blank.");
    }
    DefaultContextPostAction action =
        ContextPostActionBuilder.of(ContextServerHttpConf.clearAllContextByTime())
            .with("createTimeStart", createTimeStart)
            .with("createTimeEnd", createTimeEnd)
            .with("updateTimeStart", updateTimeStart)
            .with("updateTimeEnd", updateTimeEnd)
            .with("accessTimeStart", accessTimeStart)
            .with("accessTimeEnd", accessTimeEnd)
            .build();
    Result result = null;
    try {
      result = execute(action);
    } catch (Exception e) {
      LOGGER.error("batchClearContextByTime failed, {}", e.getMessage(), e);
      ExceptionHelper.throwErrorException(
          CLEAR_CONTEXT_HAID_FAILED.getErrorCode(), CLEAR_CONTEXT_HAID_FAILED.getErrorDesc(), e);
    }
    if (result instanceof ContextClearByTimeResult) {
      ContextClearByTimeResult contextClearByTimeResult = (ContextClearByTimeResult) result;
      if (null != contextClearByTimeResult) {
        return contextClearByTimeResult.num();
      } else {
        return 0;
      }
    } else if (null == result) {
      throw new CSErrorException(80017, "Invalid null result ");
    } else {
      throw new CSErrorException(80017, "Invalid result type : " + result.getClass().getName());
    }
  }

  private Result execute(Action action) throws ErrorException {
    try {
      return dwsHttpClient.execute(action);
    } catch (Exception e) {
      LOGGER.error("execute failed", e);
      ExceptionHelper.throwErrorException(
          EXECUTE_FALIED.getErrorCode(), EXECUTE_FALIED.getErrorDesc(), e);
    }
    return null;
  }

  private DWSResult checkDWSResult(Result result) throws CSErrorException {
    if (result instanceof DWSResult) {
      int status = ((DWSResult) result).getStatus();
      if (status != 0) {
        String errMsg = ((DWSResult) result).getMessage();
        LOGGER.error("request failed, err is  {}", errMsg);
        throw new CSErrorException(80015, errMsg);
      } else {
        return (DWSResult) result;
      }
    } else {
      throw new CSErrorException(80015, "resulet is not instance of DWSResult");
    }
  }

  @Override
  public void close() throws IOException {
    try {
      LOGGER.info("client close");
      if (null != this.dwsHttpClient) {
        this.dwsHttpClient.close();
        this.heartBeater.close();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to close httpContextClient", e);
      throw new IOException(e);
    }
  }
}
