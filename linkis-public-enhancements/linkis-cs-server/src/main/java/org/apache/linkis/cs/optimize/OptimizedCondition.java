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

package org.apache.linkis.cs.optimize;

import org.apache.linkis.cs.condition.BinaryLogicCondition;
import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.condition.UnaryLogicCondition;
import org.apache.linkis.cs.optimize.cost.ConditionCostCalculator;
import org.apache.linkis.cs.optimize.dfs.Node;

public class OptimizedCondition implements Node {

  static Double HIGH_PRIORITY = 1d;
  static Double LOW_PRIORITY = 0.5d;

  Condition condition;
  Double cost;
  Double priority;
  boolean visited = false;
  OptimizedCondition left;
  OptimizedCondition right;

  public OptimizedCondition(Condition condition, ConditionCostCalculator conditionCostCalculator) {
    new OptimizedCondition(condition, HIGH_PRIORITY, conditionCostCalculator);
  }

  public OptimizedCondition(
      Condition condition, Double priority, ConditionCostCalculator conditionCostCalculator) {
    this.condition = condition;
    this.priority = priority;
    this.cost = conditionCostCalculator.calculate(condition);
    if (condition instanceof BinaryLogicCondition) {
      BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
      this.left =
          new OptimizedCondition(
              binaryLogicCondition.getLeft(), HIGH_PRIORITY, conditionCostCalculator);
      this.right =
          new OptimizedCondition(
              binaryLogicCondition.getRight(), LOW_PRIORITY, conditionCostCalculator);
    } else if (condition instanceof UnaryLogicCondition) {
      this.left =
          new OptimizedCondition(
              ((UnaryLogicCondition) condition).getOrigin(), conditionCostCalculator);
    }
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  @Override
  public Double getCost() {
    return this.cost;
  }

  @Override
  public Double getPriority() {
    return this.priority;
  }

  @Override
  public Node getLeft() {
    return this.left;
  }

  @Override
  public Node getRight() {
    return this.right;
  }

  @Override
  public void shift() {
    if (condition instanceof BinaryLogicCondition) {
      OptimizedCondition tmp = this.left;
      this.left = this.right;
      this.right = tmp;
      this.right.priority = this.left.priority;
      this.left.priority = tmp.priority;
      ((BinaryLogicCondition) condition).shift();
    }
  }

  @Override
  public boolean visited() {
    return visited;
  }

  @Override
  public void visit() {
    this.visited = true;
  }
}
