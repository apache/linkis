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

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.exception.CSErrorException;

import java.util.List;
import java.util.Map;

public interface SearchService {

  <T> T getContextValue(ContextID contextId, ContextKey contextKey, Class<T> contextValueType)
      throws CSErrorException;

  /**
   * 返回匹配条件中最合适的一个
   *
   * @param contetID LinkisHAFlowContextID实例
   * @param keyword 包含的关键字
   * @param contextValueType 返回的contextValue必须是该类型的实例
   * @param nodeName 如果nodeName是null，搜寻全部的，如果不为空搜寻上游的
   * @param <T>
   * @return
   * @throws CSErrorException
   */
  <T> T searchContext(
      ContextID contetID, String keyword, String nodeName, Class<T> contextValueType)
      throws CSErrorException;

  <T> List<T> searchUpstreamContext(
      ContextID contextID, String nodeName, int num, Class<T> contextValueType)
      throws CSErrorException;

  <T> Map<ContextKey, T> searchUpstreamContextMap(
      ContextID contextID, String nodeName, int num, Class<T> contextValueType)
      throws CSErrorException;

  <T> List<ContextKeyValue> searchUpstreamKeyValue(
      ContextID contextID, String nodeName, int num, Class<T> contextValueType)
      throws CSErrorException;
}
