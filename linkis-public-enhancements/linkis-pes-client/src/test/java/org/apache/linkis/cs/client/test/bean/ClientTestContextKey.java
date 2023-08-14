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

package org.apache.linkis.cs.client.test.bean;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextKey;

public class ClientTestContextKey implements ContextKey {

  private final String key = "hadoop.txt";

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public void setKey(String key) {}

  @Override
  public int getType() {
    return 0;
  }

  @Override
  public void setType(int type) {}

  @Override
  public ContextType getContextType() {
    return null;
  }

  @Override
  public void setContextType(ContextType contextType) {}

  @Override
  public ContextScope getContextScope() {
    return null;
  }

  @Override
  public void setContextScope(ContextScope contextScope) {}

  @Override
  public String getKeywords() {
    return null;
  }

  @Override
  public void setKeywords(String keywords) {}
}
