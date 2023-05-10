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

package org.apache.linkis.manager.am.selector.rule;

import org.apache.linkis.manager.common.entity.node.Node;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ConcurrencyNodeSelectRule implements NodeSelectRule {

  @Override
  public Node[] ruleFiltering(Node[] nodes) {
    // 1.The concurrent selection rule only applies to the Engine.(并发选择规则只对Engine有效)
    // 2.TODO: Determine if the engine supports concurrency by checking its tag. If the engine is of
    // type "IO" and its status supports concurrency, it should be reserved.
    // (2. TODO 通过标签判断engine是否支持并发，如果Engine为io,状态，当支持并发engine需要进行保留)
    return nodes;
  }
}
