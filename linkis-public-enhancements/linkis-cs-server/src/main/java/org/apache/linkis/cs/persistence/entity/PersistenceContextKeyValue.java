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

package org.apache.linkis.cs.persistence.entity;

import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.persistence.annotation.Ignore;

import java.util.Date;

@Ignore
public class PersistenceContextKeyValue implements ContextKeyValue {

  private Integer id;

  private String contextId;

  private ContextKey contextKey;

  private ContextValue contextValue;

  private String props;

  private Date createTime;

  private Date updateTime;

  private Date accessTime;

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getProps() {
    return props;
  }

  public void setProps(String props) {
    this.props = props;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public void setContextKey(ContextKey contextKey) {
    this.contextKey = contextKey;
  }

  @Override
  public ContextKey getContextKey() {
    return this.contextKey;
  }

  @Override
  public ContextValue getContextValue() {
    return this.contextValue;
  }

  @Override
  public void setContextValue(ContextValue contextValue) {
    this.contextValue = contextValue;
  }

  public String getContextId() {
    return contextId;
  }

  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  public Date getAccessTime() {
    return accessTime;
  }

  public void setAccessTime(Date accessTime) {
    this.accessTime = accessTime;
  }
}
