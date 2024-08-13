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

package org.apache.linkis.cs.persistence;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.history.ContextHistory;

public class AContextHistory implements ContextHistory {

  private Integer id;

  private Integer contextId;

  private String source;

  private ContextType contextType;

  private String historyJson;

  private String keyword;

  private String version = "v0001";

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Integer getContextId() {
    return contextId;
  }

  public void setContextId(Integer contextId) {
    this.contextId = contextId;
  }

  public String getHistoryJson() {
    return historyJson;
  }

  public void setHistoryJson(String historyJson) {
    this.historyJson = historyJson;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  @Override
  public Integer getId() {
    return this.id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public ContextType getContextType() {
    return this.contextType;
  }

  @Override
  public void setContextType(ContextType contextType) {
    this.contextType = contextType;
  }

  @Override
  public String getSource() {
    return this.source;
  }

  @Override
  public void setSource(String source) {
    this.source = source;
  }
}
