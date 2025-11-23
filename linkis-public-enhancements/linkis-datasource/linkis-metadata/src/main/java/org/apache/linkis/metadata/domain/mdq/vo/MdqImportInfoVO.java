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

package org.apache.linkis.metadata.domain.mdq.vo;

import java.util.Map;

public class MdqImportInfoVO {

  /** 0 表示 csv 1 表示 excel 2 表示 hive */
  private Integer importType;

  private Map<String, Object> args;
  private String destination;
  private String source;

  public int getImportType() {
    return importType;
  }

  public void setImportType(int importType) {
    this.importType = importType;
  }

  public Map<String, Object> getArgs() {
    return args;
  }

  public void setArgs(Map<String, Object> args) {
    this.args = args;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public String toString() {
    return "MdqImportInfoVO{"
        + "importType="
        + importType
        + ", args="
        + args
        + ", destination='"
        + destination
        + '\''
        + ", source='"
        + source
        + '\''
        + '}';
  }
}
