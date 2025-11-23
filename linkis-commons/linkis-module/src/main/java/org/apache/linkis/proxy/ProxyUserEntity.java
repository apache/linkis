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

package org.apache.linkis.proxy;

import org.apache.commons.lang3.StringUtils;

public class ProxyUserEntity {

  private String username;

  private String proxyUser;

  private String desc;

  private Long elapseDay;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getProxyUser() {
    return proxyUser;
  }

  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public Long getElapseDay() {
    return elapseDay;
  }

  public void setElapseDay(Long elapseDay) {
    this.elapseDay = elapseDay;
  }

  public boolean isProxyMode() {
    return StringUtils.isNotBlank(proxyUser) && !proxyUser.equals(username);
  }

  @Override
  public String toString() {
    return "ProxyUserEntity{"
        + "username='"
        + username
        + '\''
        + ", proxyUser='"
        + proxyUser
        + '\''
        + '}';
  }
}
