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

package org.apache.linkis.cli.application.entity.command;

import java.util.Map;

public class Params {
  /** identifier identifies which command corresponds to this param data structure */
  String cid;

  private CmdType cmdType;

  /** Stores Mapping from param key to value/default-value etc. */
  private Map<String, ParamItem> paramItemMap;

  private Map<String, Object> extraProperties;

  public Params(
      String cid,
      CmdType cmdType,
      Map<String, ParamItem> paramItemMap,
      Map<String, Object> extraProperties) {
    this.cid = cid;
    this.cmdType = cmdType;
    this.paramItemMap = paramItemMap;
    this.extraProperties = extraProperties;
  }

  public String getCid() {
    return cid;
  }

  public void setCid(String cid) {
    this.cid = cid;
  }

  public CmdType getCmdType() {
    return cmdType;
  }

  public void setCmdType(CmdType cmdType) {
    this.cmdType = cmdType;
  }

  public Map<String, ParamItem> getParamItemMap() {
    return paramItemMap;
  }

  public void setParamItemMap(Map<String, ParamItem> paramItemMap) {
    this.paramItemMap = paramItemMap;
  }

  public Map<String, Object> getExtraProperties() {
    return extraProperties;
  }

  public void setExtraProperties(Map<String, Object> extraProperties) {
    this.extraProperties = extraProperties;
  }

  public boolean containsParam(String key) {
    return paramItemMap.containsKey(key);
  }
}
