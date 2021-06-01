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

package com.webank.wedatasphere.linkis.errorcode.client;

import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationStrategy;
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfig;
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfigBuilder;
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient;
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy;
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig;


public class ErrorCodeClientBuilder {

    private String linkisGatewayUrl = ClientConfiguration.getGatewayUrl();

    private AuthenticationStrategy authenticationStrategy = new TokenAuthenticationStrategy();

    private ClientConfigBuilder clientConfigBuilder = ClientConfigBuilder.newBuilder();

    private long connectionTimeout = ClientConfiguration.getConnectTimeOut();

    private long readTimeOut = ClientConfiguration.getReadTimeOut();

    private String authTokenKey = ClientConfiguration.getAuthKey();

    private String authTokenValue = ClientConfiguration.getAuthValue();

    private String version = ClientConfiguration.getVersion();

    private int maxConnection = 100;

    private final static String CLIENT_NAME = "ErrorCode-Client";

    public LinkisErrorCodeClient build(){
        ClientConfig clientConfig = clientConfigBuilder.addServerUrl(linkisGatewayUrl).connectionTimeout(connectionTimeout)
                .discoveryEnabled(false).loadbalancerEnabled(false).maxConnectionSize(maxConnection).retryEnabled(false)
                .readTimeout(readTimeOut).setAuthenticationStrategy(authenticationStrategy).setAuthTokenKey(authTokenKey)
                .setAuthTokenValue(authTokenValue).build();
        DWSClientConfig dwsClientConfig = new DWSClientConfig(clientConfig);
        dwsClientConfig.setDWSVersion(version);
        DWSHttpClient dwsHttpClient = new DWSHttpClient(dwsClientConfig, CLIENT_NAME);
        return new LinkisErrorCodeClient(dwsHttpClient);
    }



    public ErrorCodeClientBuilder setVersion(String version){
        this.version = version;
        return this;
    }


}
