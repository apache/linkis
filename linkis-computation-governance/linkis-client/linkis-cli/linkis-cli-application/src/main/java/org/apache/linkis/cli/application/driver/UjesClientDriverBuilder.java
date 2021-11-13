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
 
package org.apache.linkis.cli.application.driver;

import org.apache.linkis.cli.application.constants.LinkisClientKeys;
import org.apache.linkis.cli.application.constants.LinkisConstants;
import org.apache.linkis.cli.application.driver.context.UjesClientDriverContext;
import org.apache.linkis.cli.application.interactor.validate.UjesContextValidator;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.builder.BuildableByVarAccess;
import org.apache.linkis.cli.core.exception.BuilderException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.validate.Validator;
import org.apache.linkis.cli.core.interactor.var.VarAccess;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UjesClientDriverBuilder extends BuildableByVarAccess<UjesClientDriver> {
    private static Logger logger = LoggerFactory.getLogger(UjesClientDriverBuilder.class);

    @Override
    public UjesClientDriver build() {
        String gatewayUrl = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_COMMON_GATEWAY_URL);
        if (StringUtils.isBlank(gatewayUrl)) {
            throw new BuilderException("BLD0007", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "Cannot build UjesClientDriverContext: gatewayUrl is empty");
        }

        String authKey = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_COMMON_TOKEN_KEY);
        String authValue = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_COMMON_TOKEN_VALUE);

        String authenticationStrategy = stdVarAccess.getVarOrDefault(String.class, LinkisClientKeys.LINKIS_COMMON_AUTHENTICATION_STRATEGY, LinkisConstants.AUTH_STRATEGY_STATIC);

        long connectionTimeout = stdVarAccess.getVarOrDefault(Long.class, LinkisClientKeys.UJESCLIENT_COMMON_CONNECTT_TIMEOUT, 30000L);
        boolean discoveryEnabled = stdVarAccess.getVarOrDefault(Boolean.class, LinkisClientKeys.UJESCLIENT_COMMON_DISCOVERY_ENABLED, false);
        boolean loadbalancerEnabled = stdVarAccess.getVarOrDefault(Boolean.class, LinkisClientKeys.UJESCLIENT_COMMON_LOADBALANCER_ENABLED, true);
        int maxConnectionSize = stdVarAccess.getVarOrDefault(Integer.class, LinkisClientKeys.UJESCLIENT_COMMON_MAX_CONNECTION_SIZE, 5);
        boolean retryEnabled = stdVarAccess.getVarOrDefault(Boolean.class, LinkisClientKeys.UJESCLIENT_COMMON_RETRY_ENABLED, false);
        long readTimeout = stdVarAccess.getVarOrDefault(Long.class, LinkisClientKeys.UJESCLIENT_COMMON_READTIMEOUT, 30000L);
        String dwsVersion = stdVarAccess.getVarOrDefault(String.class, LinkisClientKeys.UJESCLIENT_COMMON_DWS_VERSION, "v1");

        UjesClientDriverContext context = new UjesClientDriverContext();

        context.setGatewayUrl(gatewayUrl);
        context.setAuthenticationStrategyStr(authenticationStrategy);
        context.setTokenKey(authKey);
        context.setTokenValue(authValue);
        context.setConnectionTimeout(connectionTimeout);
        context.setDiscoveryEnabled(discoveryEnabled);
        context.setLoadbalancerEnabled(loadbalancerEnabled);
        context.setMaxConnectionSize(maxConnectionSize);
        context.setRetryEnabled(retryEnabled);
        context.setReadTimeoutMills(readTimeout);
        context.setDwsVersion(dwsVersion);

        Validator ctxValidator = new UjesContextValidator();
        ctxValidator.doValidation(context);

        logger.info("==========ujes_Ctx============\n" + Utils.GSON.toJson(context));


        targetObj.initDriver(context);

        return super.build();
    }

    @Override
    public UjesClientDriverBuilder setStdVarAccess(VarAccess varAccess) {
        return (UjesClientDriverBuilder) super.setStdVarAccess(varAccess);
    }

    @Override
    public UjesClientDriverBuilder setSysVarAccess(VarAccess varAccess) {
        return (UjesClientDriverBuilder) super.setSysVarAccess(varAccess);
    }

    @Override
    protected UjesClientDriver getTargetNewInstance() {
        return new UjesClientDriver();
    }
}