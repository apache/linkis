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

package org.apache.linkis.storage.domain;

/**
 * Engine unique Id(engine唯一的Id)
 *
 * <p>Fs type(fs类型)
 *
 * <p>Create a user to start the corresponding jvm user(创建用户为对应启动的jvm用户)
 *
 * <p>Proxy user(代理用户)
 *
 * <p>client Ip for whitelist control(ip用于白名单控制)
 *
 * <p>Method name called(调用的方法名)
 *
 * <p>Method parameter(方法参数)
 */
public class MethodEntity {
  private long id;
  private String fsType;
  private String creatorUser;
  private String proxyUser;
  private String clientIp;
  private String methodName;
  private Object[] params;

  public MethodEntity(
      long id,
      String fsType,
      String creatorUser,
      String proxyUser,
      String clientIp,
      String methodName,
      Object[] params) {
    this.id = id;
    this.fsType = fsType;
    this.creatorUser = creatorUser;
    this.proxyUser = proxyUser;
    this.clientIp = clientIp;
    this.methodName = methodName;
    this.params = params;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getFsType() {
    return fsType;
  }

  public void setFsType(String fsType) {
    this.fsType = fsType;
  }

  public String getCreatorUser() {
    return creatorUser;
  }

  public void setCreatorUser(String creatorUser) {
    this.creatorUser = creatorUser;
  }

  public String getProxyUser() {
    return proxyUser;
  }

  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }

  public String getClientIp() {
    return clientIp;
  }

  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public Object[] getParams() {
    return params;
  }

  public void setParams(Object[] params) {
    this.params = params;
  }

  @Override
  public String toString() {
    return "id:"
        + id
        + ", methodName:"
        + methodName
        + ", fsType:"
        + fsType
        + ", creatorUser:"
        + creatorUser
        + ", proxyUser:"
        + proxyUser
        + ", clientIp:"
        + clientIp;
  }
}
