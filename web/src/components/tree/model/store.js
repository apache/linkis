/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import { getNodeKey } from './utils';
import Node from './node.js';
/**
 * tree store
 * handle node store in tree
 * @export
 * @class Store
 */
export default class Store {
  /**
     * init dara of Store.
     * @param {*} options
     * @memberof Store
     */
  constructor(options) {
    this.currentNode = null;
    this.currentNodeKey = null;

    Object.keys(options).forEach((option) => {
      this[option] = options[option];
    });

    this.nodesMap = {};

    this.root = new Node({
      data: this.data,
      store: this,
    });

    // todo lazy & load
  }
  /**
     * register node in nodesMap
     * @param {*} node
     * @memberof Store
     */
  registerNode(node) {
    if (!node || !node.data) return;
    const nodeKey = node.id;
    if (nodeKey) this.nodesMap[nodeKey] = node;
  }
  /**
     * deregister node in nodesMap
     * @param {*} node
     * @memberof Store
     */
  deregisterNode(node) {
    if (!node || !node.data) return;
    delete this.nodesMap[node.key];
  }
  /**
     * expand default key
     * @param {*} [keys=[]]
     * @memberof Store
     */
  setExpandedKeys(keys = []) {
    this.expandedKeys = keys;
    keys.forEach((key) => {
      const node = this.getNode(key);
      if (node) node.expand(this.expandParent);
    });
  }
  /**
     * get node by data
     * @param {*} data
     * @return {*} nodesMap[key]
     * @memberof Store
     */
  getNode(data) {
    const key = typeof data === 'object' ? getNodeKey(this.key, data) : data;
    return this.nodesMap[key];
  }
  /**
     * set current node
     * @param {*} node
     * @memberof Store
     */
  setCurrentNode(node) {
    this.currentNode = node;
  }
  /**
     * append newNode to parent
     * @param {*} newNodeData
     * @param {*} parentData
     * @memberof Store
     * @return {Node} childNode
     */
  append(newNodeData, parentData) {
    const parentNode = parentData ? this.getNode(parentData) : this.root;
    const childNode = parentNode.insertChild({ data: newNodeData });
    parentNode.expanded = true;
    childNode.isNew = true;
    childNode.isLeaf = newNodeData.isFn || newNodeData.isLeaf;
    return childNode;
  }
  /**
     * filter node and its children show
     * @memberof Store
     */
  filter() {
    const filterFn = this.filterNode;
    const recursion = function(node) {
      const children = node.root
        ? node.root._childNodes
        : node._childNodes;
      children.forEach((child) => {
        child.visible = filterFn.call(child, child);
        recursion(child);
      });
      if (!node.visible && children.length) {
        let allHidden = true;
        children.forEach((child) => {
          if (child.visible) {
            allHidden = false;
          }
        });
        if (node.root) {
          node.root.visible = allHidden === false;
        } else {
          node.visible = allHidden === false;
        }
        // 由于有可能有文件夹未打开，所以只要文件夹未打开的，都显示
      } else if (!node.visible && !children.length && !node.isLeaf) {
        node.visible = true;
      }
    };
    recursion(this);
  }
}
