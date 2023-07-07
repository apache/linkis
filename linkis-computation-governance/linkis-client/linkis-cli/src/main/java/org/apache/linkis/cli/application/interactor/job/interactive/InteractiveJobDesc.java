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

package org.apache.linkis.cli.application.interactor.job.interactive;

import java.util.Map;

public class InteractiveJobDesc {
  private String submitUser;
  private String proxyUser;
  private String creator;
  private Map<String, Object> executionMap;
  private Map<String, Object> paramConfMap;
  private Map<String, Object> paramRunTimeMap;
  private Map<String, Object> paramVarsMap;
  private Map<String, Object> labelMap;
  private Map<String, Object> sourceMap;

  public String getSubmitUser() {
    return submitUser;
  }

  public void setSubmitUser(String submitUser) {
    this.submitUser = submitUser;
  }

  public String getProxyUser() {
    return proxyUser;
  }

  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public Map<String, Object> getParamConfMap() {
    return paramConfMap;
  }

  public void setParamConfMap(Map<String, Object> paramConfMap) {
    this.paramConfMap = paramConfMap;
  }

  public Map<String, Object> getParamRunTimeMap() {
    return paramRunTimeMap;
  }

  public void setParamRunTimeMap(Map<String, Object> paramRunTimeMap) {
    this.paramRunTimeMap = paramRunTimeMap;
  }

  public Map<String, Object> getExecutionMap() {
    return executionMap;
  }

  public void setExecutionMap(Map<String, Object> executionMap) {
    this.executionMap = executionMap;
  }

  public Map<String, Object> getParamVarsMap() {
    return paramVarsMap;
  }

  public void setParamVarsMap(Map<String, Object> paramVarsMap) {
    this.paramVarsMap = paramVarsMap;
  }

  public Map<String, Object> getSourceMap() {
    return sourceMap;
  }

  public void setSourceMap(Map<String, Object> sourceMap) {
    this.sourceMap = sourceMap;
  }

  public Map<String, Object> getLabelMap() {
    return labelMap;
  }

  public void setLabelMap(Map<String, Object> labelMap) {
    this.labelMap = labelMap;
  }
}
