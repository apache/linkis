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

package org.apache.linkis.basedatamanager.server.utils;

import org.apache.linkis.basedatamanager.server.domain.UdfTreeEntity;

import java.util.ArrayList;
import java.util.List;

public class UdfTreeUtils {

  /** Build tree structure */
  public List<UdfTreeEntity> udfTreeList = new ArrayList<>();

  /** Construction method */
  public UdfTreeUtils(List<UdfTreeEntity> udfTreeList) {
    this.udfTreeList = udfTreeList;
  }

  /**
   * Obtain all root nodes (top-level nodes) that need to be built
   *
   * @return All Root Node List Collection
   */
  public List<UdfTreeEntity> getRootNode() {
    // Save all root nodes (data for all root nodes)
    List<UdfTreeEntity> rootudfTreeList = new ArrayList<>();
    // UdfTreeEntity: Each piece of data (node) found in the query
    for (UdfTreeEntity UdfTreeEntity : udfTreeList) {
      // Determine whether the current node is a root node. Note here that if the parentId type is
      // String, the equals() method should be used to determine.
      if (-1 == UdfTreeEntity.getParent()) {
        rootudfTreeList.add(UdfTreeEntity);
      }
    }
    return rootudfTreeList;
  }

  /**
   * Build a tree structure according to each top-level node (root node)
   *
   * @return Build the entire tree
   */
  public List<UdfTreeEntity> buildTree() {
    // UdfTreeEntities: Saves the complete tree structure constructed by a top-level node
    List<UdfTreeEntity> UdfTreeEntitys = new ArrayList<UdfTreeEntity>();
    // GetRootNode(): Get all root nodes
    for (UdfTreeEntity treeRootNode : getRootNode()) {
      // Build subtrees from top-level nodes
      treeRootNode = buildChildTree(treeRootNode);
      // Complete the tree structure constructed by a top-level node and add it in
      UdfTreeEntitys.add(treeRootNode);
    }
    return UdfTreeEntitys;
  }

  /**
   * Recursion ----- construct sub tree structure
   *
   * @param udfTreeEntity Root node (top-level node)
   * @return Whole tree
   */
  public UdfTreeEntity buildChildTree(UdfTreeEntity udfTreeEntity) {
    List<UdfTreeEntity> childTree = new ArrayList<UdfTreeEntity>();
    // udfTreeList：All node sets (all data)
    for (UdfTreeEntity UdfTreeEntity : udfTreeList) {
      // Determine whether the parent node ID of the current node is equal to the ID of the root
      // node, that is, if the current node is a child node under it
      if (UdfTreeEntity.getParent().equals(udfTreeEntity.getId())) {
        // Recursively judge the current node's situation and call its own method
        childTree.add(buildChildTree(UdfTreeEntity));
      }
    }
    // Recursively judge the current node's situation and call its own method
    udfTreeEntity.setChildrenList(childTree);
    return udfTreeEntity;
  }
}
