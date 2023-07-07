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

public class UJESClientContext {
  private String gatewayUrl;
  private Long connectionTimeout;
  private Boolean discoveryEnabled;
  private Boolean loadBalancerEnabled;
  private Long readTimeoutMills;

  private String tokenKey;
  private String tokenValue;

  private String authenticationStrategyStr;
  private Long discoveryFrequencyMills;
  private Integer maxConnectionSize;
  private Boolean retryEnabled;
  private String dwsVersion;

  public UJESClientContext() {}

  public String getGatewayUrl() {
    return gatewayUrl;
  }

  public void setGatewayUrl(String gatewayUrl) {
    this.gatewayUrl = gatewayUrl;
  }

  public Long getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(Long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public Boolean getDiscoveryEnabled() {
    return discoveryEnabled;
  }

  public void setDiscoveryEnabled(Boolean discoveryEnabled) {
    this.discoveryEnabled = discoveryEnabled;
  }

  public Boolean getLoadBalancerEnabled() {
    return loadBalancerEnabled;
  }

  public void setLoadBalancerEnabled(Boolean loadBalancerEnabled) {
    this.loadBalancerEnabled = loadBalancerEnabled;
  }

  public Long getReadTimeoutMills() {
    return readTimeoutMills;
  }

  public void setReadTimeoutMills(Long readTimeoutMills) {
    this.readTimeoutMills = readTimeoutMills;
  }

  public String getTokenKey() {
    return tokenKey;
  }

  public void setTokenKey(String tokenKey) {
    this.tokenKey = tokenKey;
  }

  public String getTokenValue() {
    return tokenValue;
  }

  public void setTokenValue(String tokenValue) {
    this.tokenValue = tokenValue;
  }

  public String getAuthenticationStrategyStr() {
    return authenticationStrategyStr;
  }

  public void setAuthenticationStrategyStr(String authenticationStrategyStr) {
    this.authenticationStrategyStr = authenticationStrategyStr;
  }

  public Long getDiscoveryFrequencyMills() {
    return discoveryFrequencyMills;
  }

  public void setDiscoveryFrequencyMills(Long discoveryFrequencyMills) {
    this.discoveryFrequencyMills = discoveryFrequencyMills;
  }

  public Integer getMaxConnectionSize() {
    return maxConnectionSize;
  }

  public void setMaxConnectionSize(Integer maxConnectionSize) {
    this.maxConnectionSize = maxConnectionSize;
  }

  public Boolean getRetryEnabled() {
    return retryEnabled;
  }

  public void setRetryEnabled(Boolean retryEnabled) {
    this.retryEnabled = retryEnabled;
  }

  public String getDwsVersion() {
    return dwsVersion;
  }

  public void setDwsVersion(String dwsVersion) {
    this.dwsVersion = dwsVersion;
  }
}
