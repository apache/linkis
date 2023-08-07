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
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;

import java.util.List;

/** Description:Context是一个接口让用户能够在微服务中进行对一个工作流的Context进行操作 他应该有一个默认的实现，比如LinkisWorkFlowContext */
public interface Context {

  /**
   * 每一个Context都应该有一个ContextID与之对应d
   *
   * @return contextID是该Context对应的ContextID
   */
  public ContextID getContextID();

  /**
   * 设置ContextID
   *
   * @param contextID contextID
   */
  public void setContextID(ContextID contextID);

  /**
   * 通过contextKey来获取对应的contextValue
   *
   * @param contextKey 是标识一个ContextValue的一个key值，比如资源文件的名称
   * @return 返回key对应的value, 比如resourceid 和 version
   */
  public ContextValue getContextValue(ContextKey contextKey) throws ErrorException;

  /**
   * 设置contextKeyValue
   *
   * @param contextKeyAndValue 需要设置的ContextKeyValue
   * @throws ErrorException 可能由于网络原因出现的
   */
  public void setContextKeyAndValue(ContextKeyValue contextKeyAndValue) throws ErrorException;

  public void set(ContextKey contextKey, ContextValue contextValue) throws ErrorException;

  public void setLocal(ContextKey contextKey, ContextValue contextValue);

  public void setLocal(ContextKeyValue contextKeyValue);

  /**
   * todo 是通过ContextCondition进行搜索
   *
   * @return
   */
  public List<ContextKeyValue> searchContext(
      List<ContextType> contextTypes,
      List<ContextScope> contextScopes,
      List<String> contains,
      List<String> regex)
      throws ErrorException;

  public void reset(ContextKey contextKey) throws ErrorException;

  public void reset() throws ErrorException;

  public void remove(ContextKey contextKey) throws ErrorException;

  public void removeAll() throws ErrorException;

  public void onBind(ContextIDListener contextIDListener) throws ErrorException;

  public void onBind(ContextKey contextKey, ContextKeyListener contextKeyListener)
      throws ErrorException;
}
