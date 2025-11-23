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

package org.apache.linkis.protocol.query.cache;

import org.apache.linkis.protocol.query.QueryProtocol;

import java.util.List;

public class RequestWriteCache implements QueryProtocol {
  private String executionContent;
  private String user;
  private Long cacheExpireAfter;
  private List<String> labelsStr;
  private String resultSet;

  public RequestWriteCache(
      String executionContent,
      String user,
      Long cacheExpireAfter,
      List<String> labelsStr,
      String resultSet) {
    this.executionContent = executionContent;
    this.user = user;
    this.cacheExpireAfter = cacheExpireAfter;
    this.labelsStr = labelsStr;
    this.resultSet = resultSet;
  }

  public String getExecutionContent() {
    return executionContent;
  }

  public String getUser() {
    return user;
  }

  public Long getCacheExpireAfter() {
    return cacheExpireAfter;
  }

  public List<String> getLabelsStr() {
    return labelsStr;
  }

  public String getResultSet() {
    return resultSet;
  }
}
