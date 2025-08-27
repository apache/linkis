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

package org.apache.linkis.cs.keyword;

import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;

public class TestContextKeyValue implements ContextKeyValue {

  private ContextKey contextKey;

  private ContextValue contextValue;

  @Override
  public ContextKey getContextKey() {
    return this.contextKey;
  }

  @Override
  public void setContextKey(ContextKey contextKey) {
    this.contextKey = contextKey;
  }

  @Override
  public ContextValue getContextValue() {
    return this.contextValue;
  }

  @Override
  public void setContextValue(ContextValue contextValue) {
    this.contextValue = contextValue;
  }
}
