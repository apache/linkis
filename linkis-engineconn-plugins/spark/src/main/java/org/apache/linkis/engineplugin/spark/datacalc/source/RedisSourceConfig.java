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

package org.apache.linkis.engineplugin.spark.datacalc.source;

import org.apache.linkis.engineplugin.spark.datacalc.model.SourceConfig;

import javax.validation.constraints.NotBlank;

public class RedisSourceConfig extends SourceConfig {

  @NotBlank private String host;

  @NotBlank private String port;

  @NotBlank private String serializer = "table";

  private String keysPattern;

  private String sourceTable;

  private String dbNum = "0";
  private String auth = "password";

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getKeysPattern() {
    return keysPattern;
  }

  public void setKeysPattern(String keysPattern) {
    this.keysPattern = keysPattern;
  }

  public String getDbNum() {
    return dbNum;
  }

  public void setDbNum(String dbNum) {
    this.dbNum = dbNum;
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth = auth;
  }

  public String getSourceTable() {
    return sourceTable;
  }

  public void setSourceTable(String sourceTable) {
    this.sourceTable = sourceTable;
  }

  public String getSerializer() {
    return serializer;
  }

  public void setSerializer(String serializer) {
    this.serializer = serializer;
  }
}
