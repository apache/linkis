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

package org.apache.linkis.cs.client;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.listener.ContextIDListener;
import org.apache.linkis.cs.client.listener.ContextKeyListener;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public interface ContextClient extends Closeable {
  /**
   * 通过与cs-server进行交互生成一个工作流的Context 传入的信息应该有工程名 工作流名 user是传入的用户名
   *
   * @return 用户可以使用的Context
   */
  @Deprecated
  Context createContext(String projectName, String flowName, String use, Map<String, Object> params)
      throws ErrorException;

  Context createContext(ContextID contextID) throws ErrorException;

  /**
   * 通过ContextID获取
   *
   * @param contextID
   * @return
   * @throws ErrorException
   */
  Context getContext(ContextID contextID) throws ErrorException;

  Context getContext(String contextIDStr) throws ErrorException;

  ContextValue getContextValue(ContextID contextID, ContextKey contextKey) throws ErrorException;

  void update(ContextID contextID, ContextKey contextKey, ContextValue contextValue)
      throws ErrorException;

  /**
   * 通过contextID和contextKey进行对某一个contextKey进行reset
   *
   * @param contextID contextId
   * @param contextKey contexKey
   * @throws ErrorException 可能捕获的异常
   */
  void reset(ContextID contextID, ContextKey contextKey) throws ErrorException;

  /**
   * 将整个contextID所有的contextKey进行reset,这个会用在工作流实时执行之前的一个reset
   *
   * @param contextID contextID
   * @throws ErrorException
   */
  void reset(ContextID contextID) throws ErrorException;

  /**
   * 删除的操作是为了能够将contextid下面的contextkey进行删除 如果contextKey是空的话，则全部删除
   *
   * @param contextID
   * @param contextKey
   * @throws ErrorException
   */
  void remove(ContextID contextID, ContextKey contextKey) throws ErrorException;

  void setContextKeyValue(ContextID contextID, ContextKeyValue contextKeyValue)
      throws ErrorException;

  void bindContextIDListener(ContextIDListener contextIDListener) throws ErrorException;

  void bindContextKeyListener(ContextKeyListener contextKeyListener) throws ErrorException;

  /**
   * 通过各种condition搜索contextkeyValue
   *
   * @return 一个contextKeyValue数组
   * @throws ErrorException 可能出现的error
   */
  List<ContextKeyValue> search(
      ContextID contextID,
      List<ContextType> contextTypes,
      List<ContextScope> contextScopes,
      List<String> contains,
      List<String> regex)
      throws ErrorException;

  /**
   * 通过各种condition搜索contextkeyValue upstreamOnly 表示取跟当前节点上游中查询 nodeName 获取该节点的关系节点 num 获取的节点数量，1
   * 返回最近的一个， 无穷大表示所有节点
   *
   * @return 一个contextKeyValue数组
   * @throws ErrorException 可能出现的error
   */
  List<ContextKeyValue> search(
      ContextID contextID,
      List<ContextType> contextTypes,
      List<ContextScope> contextScopes,
      List<String> contains,
      List<String> regex,
      boolean upstreamOnly,
      String nodeName,
      int num,
      List<Class> contextValueTypes)
      throws ErrorException;

  void createHistory(ContextID contextID, ContextHistory history) throws ErrorException;

  void removeHistory(ContextID contextID, ContextHistory history) throws ErrorException;

  List<ContextHistory> getHistories(ContextID contextID) throws ErrorException;

  ContextHistory getHistory(ContextID contextID, String source) throws ErrorException;

  List<ContextHistory> searchHistory(ContextID contextID, String... keywords) throws ErrorException;

  /**
   * If you want to remove some type of key starting with keyPrefix, please make sure the keyPrefix
   * is end up with '.'. In this way, you will not remove the value by mistake those node name
   * contains your node name. eg : Suppose you hava a workflow with node1 and node1_xxx , you want
   * to clear values of the type 'METADATA' and prefix 'node.node1', but this will also clear values
   * of node1_xxx . So you can use the prefix 'node.node1.', which is safe.
   *
   * @throws ErrorException
   */
  void removeAllValueByKeyPrefixAndContextType(
      ContextID contextID, ContextType contextType, String keyPrefix) throws ErrorException;

  void removeAllValueByKeyPrefix(ContextID contextID, String keyPrefix) throws ErrorException;

  void removeAllValueByKeyAndContextType(
      ContextID contextID, ContextType contextType, String keyStr) throws ErrorException;

  List<String> searchHAIDByTime(
      String createTimeStart,
      String createTimeEnd,
      String updateTimeStart,
      String updateTimeEnd,
      String accessTimeStart,
      String accessTimeEnd,
      Integer pageNow,
      Integer pageSize)
      throws ErrorException;

  int batchClearContextByHAID(List<String> idList) throws ErrorException;

  int batchClearContextByTime(
      String createTimeStart,
      String createTimeEnd,
      String updateTimeStart,
      String updateTimeEnd,
      String accessTimeStart,
      String accessTimeEnd)
      throws ErrorException;
}
