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

package org.apache.linkis.cs.common.entity.source;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;

public interface ContextKey {

  String getKey();

  void setKey(String key);

  /**
   * 每一个ContextKey都会有一个type,比如是一个ymlContextKey的类型，这个是在ContextKeyEnum中进行枚举
   * 这里设置这样的enum，是为了方便client和server进行交互的时候，client只需要出传一个int 就可以
   *
   * @return
   */
  int getType();

  void setType(int type);

  ContextType getContextType();

  void setContextType(ContextType contextType);

  ContextScope getContextScope();

  void setContextScope(ContextScope contextScope);

  String getKeywords();

  void setKeywords(String keywords);
}
