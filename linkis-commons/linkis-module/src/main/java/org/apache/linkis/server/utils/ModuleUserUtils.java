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

package org.apache.linkis.server.utils;

import org.apache.linkis.proxy.ProxyUserEntity;
import org.apache.linkis.server.security.ProxyUserSSOUtils;
import org.apache.linkis.server.security.SecurityFilter;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

import scala.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleUserUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleUserUtils.class);

  private static String TOKEN_KEY = "Token-Code";
  private static String TOKEN_USER_KEY = "Token-User";

  /**
   * get ProxyUserEntity 1.if not support proxy return loginUser 2.if proxy user exist return
   * proxyUser and loginUser
   *
   * @param httpServletRequest
   * @return
   */
  public static ProxyUserEntity getProxyUserEntity(HttpServletRequest httpServletRequest) {
    String loginUser = SecurityFilter.getLoginUsername(httpServletRequest);
    Option<String> proxyUserUsername = ProxyUserSSOUtils.getProxyUserUsername(httpServletRequest);
    ProxyUserEntity proxyUserEntity = new ProxyUserEntity();
    proxyUserEntity.setUsername(loginUser);
    if (proxyUserUsername.isDefined()) {
      proxyUserEntity.setProxyUser(proxyUserUsername.get());
    }
    return proxyUserEntity;
  }

  /**
   * get ProxyUserEntity and print operation log
   *
   * @param httpServletRequest
   * @param msg
   * @return
   */
  public static ProxyUserEntity getProxyUserEntity(
      HttpServletRequest httpServletRequest, String msg) {
    ProxyUserEntity proxyUserEntity = getProxyUserEntity(httpServletRequest);
    LOGGER.info(
        "user {} proxy to {} operation {}",
        proxyUserEntity.getUsername(),
        proxyUserEntity.getProxyUser(),
        msg);
    return proxyUserEntity;
  }

  public static String getOperationUser(HttpServletRequest httpServletRequest) {
    ProxyUserEntity proxyUserEntity = getProxyUserEntity(httpServletRequest);
    if (proxyUserEntity.isProxyMode()) {
      return proxyUserEntity.getProxyUser();
    } else {
      return proxyUserEntity.getUsername();
    }
  }

  /**
   * get operation user and print log
   *
   * @param httpServletRequest
   * @param msg
   * @return
   */
  public static String getOperationUser(HttpServletRequest httpServletRequest, String msg) {
    ProxyUserEntity proxyUserEntity = getProxyUserEntity(httpServletRequest, msg);
    if (proxyUserEntity.isProxyMode()) {
      return proxyUserEntity.getProxyUser();
    } else {
      return proxyUserEntity.getUsername();
    }
  }

  public static String getToken(HttpServletRequest httpServletRequest) {
    String token = httpServletRequest.getHeader(TOKEN_KEY);
    if (StringUtils.isBlank(token)) {
      LOGGER.info("Cannot get token from this http request!");
    }
    return token;
  }

  public static String getTokenUser(HttpServletRequest httpServletRequest) {
    String tokenUser = httpServletRequest.getHeader(TOKEN_USER_KEY);
    if (StringUtils.isBlank(tokenUser)) {
      LOGGER.info("Cannot get token user from this http request!");
    }
    return tokenUser;
  }
}
