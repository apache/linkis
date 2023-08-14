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

package org.apache.linkis.cs.listener.callback.imp;

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextValue;

import java.util.Objects;

public class ContextKeyValueBean {

  private ContextKey csKey;
  private ContextValue csValue;
  private ContextID csID;

  public ContextID getCsID() {
    return csID;
  }

  public void setCsID(ContextID csID) {
    this.csID = csID;
  }

  public ContextKey getCsKey() {
    return csKey;
  }

  public void setCsKey(ContextKey csKey) {
    this.csKey = csKey;
  }

  public ContextValue getCsValue() {
    return csValue;
  }

  public void setCsValue(ContextValue csValue) {
    this.csValue = csValue;
  }

  @Override
  public int hashCode() {
    return Objects.hash(csKey, csValue);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContextKeyValueBean csmapKey = (ContextKeyValueBean) o;
    return Objects.equals(csKey, csmapKey.csKey) && Objects.equals(csValue, csmapKey.csValue);
  }
}
