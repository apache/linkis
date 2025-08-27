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

import org.apache.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import org.apache.linkis.cs.common.entity.source.ContextKey;

public class AContextKeyListener implements ContextKeyListenerDomain {

  private String source;

  private ContextKey contextKey;

  @Override
  public String getSource() {
    return this.source;
  }

  @Override
  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public ContextKey getContextKey() {
    return this.contextKey;
  }

  @Override
  public void setContextKey(ContextKey contextKey) {
    this.contextKey = contextKey;
  }
}
