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

package org.apache.linkis.cs.client.service;

import org.apache.linkis.cs.common.entity.metadata.CSTable;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.exception.CSErrorException;

import java.util.List;

public interface TableService {

  /**
   * 通过ContextKey获取一个确定的CSTable
   *
   * @param contextID
   * @param contextKey
   * @return
   * @throws CSErrorException
   */
  CSTable getCSTable(ContextID contextID, ContextKey contextKey) throws CSErrorException;

  /**
   * 获取上游节点的所有表
   *
   * @param contextIDStr
   * @param nodeName
   * @return
   * @throws CSErrorException
   */
  List<CSTable> getUpstreamTables(String contextIDStr, String nodeName) throws CSErrorException;

  void putCSTable(String contextIDStr, String ContextKey, CSTable csTable) throws CSErrorException;

  CSTable getCSTable(String contextIDStr, String contextKey) throws CSErrorException;

  CSTable getUpstreamSuitableTable(String contextIDStr, String nodeName, String keyword)
      throws CSErrorException;

  List<ContextKeyValue> searchUpstreamTableKeyValue(String contextIDStr, String nodeName)
      throws CSErrorException;

  void registerCSTable(String contextIDStr, String nodeName, String alias, CSTable csTable)
      throws CSErrorException;
}
