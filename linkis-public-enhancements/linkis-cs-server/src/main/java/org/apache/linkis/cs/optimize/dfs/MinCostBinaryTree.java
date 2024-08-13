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

package org.apache.linkis.cs.optimize.dfs;

public class MinCostBinaryTree implements BinaryTree {

  Node rootNode;
  BinaryTree left;
  BinaryTree right;
  Double cost;

  public MinCostBinaryTree(Node rootNode) {
    this.rootNode = rootNode;
    if (this.rootNode.visited()) {
      return;
    }
    if (rootNode.getLeft() == null) {
      this.cost = rootNode.getCost();
      return;
    }
    if (rootNode.getRight() == null) {
      this.left = new MinCostBinaryTree(rootNode.getLeft());
      this.cost = this.left.getCost();
      return;
    }

    BinaryTree firstLeft = new MinCostBinaryTree(rootNode.getLeft());
    BinaryTree firstRight = new MinCostBinaryTree(rootNode.getRight());
    Double firstCost =
        firstLeft.getCost() * rootNode.getLeft().getPriority()
            + firstRight.getCost() * rootNode.getRight().getPriority();

    rootNode.shift();

    BinaryTree secondLeft = new MinCostBinaryTree(rootNode.getLeft());
    BinaryTree secondRight = new MinCostBinaryTree(rootNode.getRight());
    Double secondCost =
        secondLeft.getCost() * rootNode.getLeft().getPriority()
            + secondRight.getCost() * rootNode.getRight().getPriority();

    if (firstCost > secondCost) {
      this.left = secondLeft;
      this.right = secondRight;
      this.cost = secondCost;
    } else {
      rootNode.shift();
      this.left = firstLeft;
      this.right = secondRight;
      this.cost = firstCost;
    }
    this.rootNode.visit();
  }

  @Override
  public BinaryTree getLeft() {
    return this.left;
  }

  @Override
  public BinaryTree getRight() {
    return this.right;
  }

  @Override
  public Node getRootNode() {
    return this.rootNode;
  }

  @Override
  public Double getCost() {
    return this.cost;
  }
}
