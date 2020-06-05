/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.cs.client.builder;

import com.webank.wedatasphere.linkis.common.conf.Configuration;
import com.webank.wedatasphere.linkis.cs.client.utils.ContextClientConf;
import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationStrategy;
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfig;
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfigBuilder;
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy;

/**
 * created by cooperyang on 2020/2/10
 * Description: 以http的方式和cs-server进行交互的配置,包括http的诸多配置
 */
public class HttpContextClientConfig extends ContextClientConfig{


    private ClientConfig clientConfig;


    public HttpContextClientConfig(){
        //初始化clientConfig
        String gatewayUrl = Configuration.getGateWayURL();
        AuthenticationStrategy authenticationStrategy = new TokenAuthenticationStrategy();
        int maxConnection = 10;
        int connectionTimeout = 3000;
        int readTimeout = 10000;
        clientConfig = ClientConfigBuilder.newBuilder().addUJESServerUrl(gatewayUrl).
                connectionTimeout(connectionTimeout).discoveryEnabled(false).loadbalancerEnabled(false).
                maxConnectionSize(maxConnection).retryEnabled(false).readTimeout(readTimeout)
                .setAuthenticationStrategy(authenticationStrategy).setAuthTokenKey(ContextClientConf.CONTEXT_CLIENT_AUTH_KEY().getValue())
                .setAuthTokenValue(ContextClientConf.CONTEXT_CLIENT_AUTH_VALUE().getValue()).build();

    }



    /**
     *
     * @return 返回一个的
     */
    public ClientConfig getClientConfig(){
        return this.clientConfig;
    }



}
