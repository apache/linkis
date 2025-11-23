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

package org.apache.linkis.engineplugin.spark.common;

import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;

import java.util.ArrayList;
import java.util.List;

public class MultiTreeNode {
  int level;
  LogicalPlan logicalPlan;
  MultiTreeNode parent;
  List<MultiTreeNode> children = new ArrayList<>();

  public MultiTreeNode(LogicalPlan logicalPlan) {
    this.logicalPlan = logicalPlan;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public LogicalPlan getLogicalPlan() {
    return logicalPlan;
  }

  public void setLogicalPlan(LogicalPlan logicalPlan) {
    this.logicalPlan = logicalPlan;
  }

  public MultiTreeNode getParent() {
    return parent;
  }

  public void setParent(MultiTreeNode parent) {
    this.parent = parent;
  }

  public List<MultiTreeNode> getChildren() {
    return children;
  }

  public void setChildren(List<MultiTreeNode> children) {
    this.children = children;
  }
}
