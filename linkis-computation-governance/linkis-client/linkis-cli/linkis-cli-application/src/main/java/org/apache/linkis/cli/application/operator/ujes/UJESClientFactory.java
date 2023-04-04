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

package org.apache.linkis.cli.application.operator.ujes;

import org.apache.linkis.cli.application.constants.AppKeys;
import org.apache.linkis.cli.application.constants.LinkisConstants;
import org.apache.linkis.cli.application.interactor.validate.UJESContextValidator;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.validate.Validator;
import org.apache.linkis.cli.common.entity.var.VarAccess;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.BuilderException;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy;
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.UJESClientImpl;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UJESClientFactory {
  private static Logger logger = LoggerFactory.getLogger(UJESClientFactory.class);

  private static UJESClient client;

  public static UJESClient getReusable(VarAccess stdVarAccess, VarAccess sysVarAccess) {
    if (client == null) {
      synchronized (UJESClientFactory.class) {
        if (client == null) {
          client = getNew(stdVarAccess, sysVarAccess);
        }
      }
    }
    return client;
  }

  public static UJESClient getNew(VarAccess stdVarAccess, VarAccess sysVarAccess) {
    try {
      DWSClientConfig config = generateDWSClientConfig(stdVarAccess, sysVarAccess);
      UJESClient ret = new UJESClientImpl(config);
      logger.info("Linkis ujes client inited.");
      return ret;
    } catch (Exception e) {
      throw new LinkisClientExecutionException(
          "EXE0010", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "Cannot init UJESClient", e);
    }
  }

  public static DWSClientConfig generateDWSClientConfig(
      VarAccess stdVarAccess, VarAccess sysVarAccess) {
    UJESClientContext context = generateContext(stdVarAccess, sysVarAccess);
    try {
      AuthenticationStrategy authenticationStrategy;
      if (StringUtils.isBlank(context.getAuthenticationStrategyStr())
          || !LinkisConstants.AUTH_STRATEGY_TOKEN.equalsIgnoreCase(
              context.getAuthenticationStrategyStr())) {
        authenticationStrategy =
            new StaticAuthenticationStrategy(); // this has to be newed here otherwise
        // log-in fails for static
      } else {
        authenticationStrategy = new TokenAuthenticationStrategy();
      }

      DWSClientConfigBuilder builder = DWSClientConfigBuilder.newBuilder();
      DWSClientConfig config =
          ((DWSClientConfigBuilder)
                  (builder
                      .addServerUrl(context.getGatewayUrl())
                      .connectionTimeout(30000)
                      .discoveryEnabled(false)
                      .discoveryFrequency(1, TimeUnit.MINUTES)
                      .loadbalancerEnabled(false)
                      .maxConnectionSize(5)
                      .retryEnabled(false)
                      .readTimeout(context.getReadTimeoutMills())
                      .setAuthenticationStrategy(authenticationStrategy)
                      .setAuthTokenKey(context.getTokenKey())
                      .setAuthTokenValue(context.getTokenValue())))
              .setDWSVersion(context.getDwsVersion())
              .build();

      logger.info("Linkis ujes client inited.");
      return config;
    } catch (Exception e) {
      throw new LinkisClientExecutionException(
          "EXE0010",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Cannot init DWSClientConfig",
          e);
    }
  }

  public static DWSClientConfig generateDWSClientConfigForBML(
      VarAccess stdVarAccess, VarAccess sysVarAccess) {
    UJESClientContext context = generateContext(stdVarAccess, sysVarAccess);
    try {
      AuthenticationStrategy authenticationStrategy;
      if (StringUtils.isBlank(context.getAuthenticationStrategyStr())
          || !LinkisConstants.AUTH_STRATEGY_TOKEN.equalsIgnoreCase(
              context.getAuthenticationStrategyStr())) {
        authenticationStrategy =
            new StaticAuthenticationStrategy(); // this has to be newed here otherwise
        // log-in fails for static
      } else {
        authenticationStrategy = new TokenAuthenticationStrategy();
      }

      DWSClientConfigBuilder builder = DWSClientConfigBuilder.newBuilder();
      String authKey = stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_TOKEN_KEY);
      String authValue = stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_TOKEN_VALUE);
      DWSClientConfig config =
          ((DWSClientConfigBuilder)
                  (builder
                      .addServerUrl(context.getGatewayUrl())
                      .connectionTimeout(30000)
                      .discoveryEnabled(false)
                      .discoveryFrequency(1, TimeUnit.MINUTES)
                      .loadbalancerEnabled(false)
                      .maxConnectionSize(5)
                      .retryEnabled(false)
                      .readTimeout(context.getReadTimeoutMills())
                      .setAuthenticationStrategy(authenticationStrategy)
                      .setAuthTokenKey(authKey)
                      .setAuthTokenValue(authValue)))
              .setDWSVersion(context.getDwsVersion())
              .build();

      logger.info("Linkis ujes client inited.");
      return config;
    } catch (Exception e) {
      throw new LinkisClientExecutionException(
          "EXE0010",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Cannot init DWSClientConfig",
          e);
    }
  }

  private static UJESClientContext generateContext(VarAccess stdVarAccess, VarAccess sysVarAccess) {
    String gatewayUrl = stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_GATEWAY_URL);
    if (StringUtils.isBlank(gatewayUrl)) {
      throw new BuilderException(
          "BLD0007",
          ErrorLevel.ERROR,
          CommonErrMsg.BuilderBuildErr,
          "Cannot build UjesClientDriverContext: gatewayUrl is empty");
    }

    String authKey = stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_TOKEN_KEY);
    String authValue = stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_TOKEN_VALUE);

    String authenticationStrategy =
        stdVarAccess.getVarOrDefault(
            String.class,
            AppKeys.LINKIS_COMMON_AUTHENTICATION_STRATEGY,
            LinkisConstants.AUTH_STRATEGY_STATIC);

    long connectionTimeout =
        stdVarAccess.getVarOrDefault(
            Long.class, AppKeys.UJESCLIENT_COMMON_CONNECTT_TIMEOUT, 30000L);
    boolean discoveryEnabled =
        stdVarAccess.getVarOrDefault(
            Boolean.class, AppKeys.UJESCLIENT_COMMON_DISCOVERY_ENABLED, false);
    boolean loadBalancerEnabled =
        stdVarAccess.getVarOrDefault(
            Boolean.class, AppKeys.UJESCLIENT_COMMON_LOADBALANCER_ENABLED, true);
    int maxConnectionSize =
        stdVarAccess.getVarOrDefault(
            Integer.class, AppKeys.UJESCLIENT_COMMON_MAX_CONNECTION_SIZE, 5);
    boolean retryEnabled =
        stdVarAccess.getVarOrDefault(Boolean.class, AppKeys.UJESCLIENT_COMMON_RETRY_ENABLED, false);
    long readTimeout =
        stdVarAccess.getVarOrDefault(Long.class, AppKeys.UJESCLIENT_COMMON_READTIMEOUT, 30000L);
    String dwsVersion =
        stdVarAccess.getVarOrDefault(String.class, AppKeys.UJESCLIENT_COMMON_DWS_VERSION, "v1");

    UJESClientContext context = new UJESClientContext();

    context.setGatewayUrl(gatewayUrl);
    context.setAuthenticationStrategyStr(authenticationStrategy);
    context.setTokenKey(authKey);
    context.setTokenValue(authValue);
    context.setConnectionTimeout(connectionTimeout);
    context.setDiscoveryEnabled(discoveryEnabled);
    context.setLoadBalancerEnabled(loadBalancerEnabled);
    context.setMaxConnectionSize(maxConnectionSize);
    context.setRetryEnabled(retryEnabled);
    context.setReadTimeoutMills(readTimeout);
    context.setDwsVersion(dwsVersion);

    logger.info("==========UJES_CTX============\n" + Utils.GSON.toJson(context));
    Validator ctxValidator = new UJESContextValidator();
    ctxValidator.doValidation(context);
    return context;
  }
}
