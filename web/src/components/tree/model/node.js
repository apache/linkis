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

import { getPropertyFromData, setPropertyForData, markNodeData } from './utils';
let nodeId = 0;
/**
 * @export
 * @class Node
 */
export default class Node {
  /**
     *Creates an instance of Node.
     * @param {*} options merge tree opions in node
     * @memberof Node
     */
  constructor(options) {
    this.id = nodeId++;
    this.data = null;
    this.expanded = false;
    this.parent = null;
    this.isEditState = false;
    this.isNew = false;

    Object.keys(options).forEach((name) => {
      this[name] = options[name];
    });

    this._level = 0;
    this._loaded = false;
    this._childNodes = [];
    this._loading = false;
    this.isActived = false;

    if (this.parent) {
      this._level = this.parent._level + 1;
    }
    this.isLeaf = getPropertyFromData(this, 'isLeaf');
    this.visible = true;
    this.store.registerNode(this);

    this.setData(this.data);
  }
  /**
     * set children to nodes
     * @param {*} data
     * @memberof Node
     */
  setData(data) {
    if (!data) return;
    // todo why mounted in data
    if (!Array.isArray(data)) {
      markNodeData(this, data);
    }
    this.data = data;
    this._childNodes = [];

    let children = [];
    if (this._level === 0 && Array.isArray(this.data)) {
      children = this.data;
    } else {
      children = getPropertyFromData(this, 'children') || [];
    }
    children.forEach((child) => {
      this.insertChild({ data: child });
    });
  }
  /**
     * add node as child
     * @param {*} child
     * @param {*} index
     * @return { Node } child
     */
  insertChild(child, index) {
    if (!(child instanceof Node)) {
      Object.assign(child, {
        parent: this,
        store: this.store,
      });
      child = new Node(child);
    }
    child._level = this._level + 1;
    // todo handle name
    if (index === undefined || index < 0) {
      this._childNodes.push(child);
    } else {
      this._childNodes.splice(index, 0, child);
    }
    return child;
  }
  /**
     * node expand and cb
     * @param {*} expandParent
     * @param {*} cb
     * @memberof Node
     */
  expand(expandParent, cb) {
    const done = () => {
      if (expandParent) {
        let parent = this.parent;
        while (parent && parent._level > 0) {
          parent.expanded = true;
          parent = parent.parent;
        }
      }
      this.expanded = true;
      cb && cb();
    };
    // todo lazy && load
    done();
  }
  /**
     * insert new Child and delete old child
     * @memberof Node
     */
  updateChildren() {
    const newData = this.getChildren() || [];
    const oldData = this._childNodes.map((node) => node.data);
    const newDataMap = {};
    const newNodes = [];

    newData.forEach((item, index) => {
      if (this.store.nodesMap[item.id]) {
        newDataMap[item.id] = { index, data: item };
      } else {
        newNodes.push({ index, data: item });
      }
    });

    oldData.forEach((item) => {
      if (!newDataMap[item.id]) this.removeChildByData(item);
    });
    newNodes.forEach(({ index, data }) => {
      this.insertChild({ data }, index);
    });
  }
  /**
     * remove child by data
     * @param {*} data
     * @memberof Node
     */
  removeChildByData(data) {
    let targetNode = null;
    this._childNodes.forEach((node) => {
      if (node.data === data) {
        targetNode = node;
      }
    });

    if (targetNode) {
      this.removeChild(targetNode);
    }
  }
  /**
     * remove child
     * @param {*} child
     */
  removeChild(child) {
    const index = this._childNodes.indexOf(child);

    if (index > -1) {
      this.store.deregisterNode(child);
      child.parent = null;
      this._childNodes.splice(index, 1);
    }
  }
  /**
     * remove itSelf
     * @memberof Node
     */
  remove() {
    if (this.store.beforeRemove) {
      this.store.beforeRemove();
    }
    this.parent.removeChild(this);
  }
  /**
     * get data children value
     * @return {*} data[children]
     * @memberof Node
     */
  getChildren() {
    if (!this.data) return null;
    const data = this.data;
    const props = this.store.props;
    const children = props.children || 'children';
    return data[children];
  }
  /**
     * collapse node
     * @memberof Node
     */
  collapse() {
    this.expanded = false;
  }
  /**
     * change edit state
     * @param {*} state
     * @param {*} valid
     * @memberof Node
     */
  changeEditState(state) {
    this.isEditState = state;
  }
  /**
     * handle nodes
     * @readonly
     * @memberof Node
     */
  get computedNodes() {
    let nodes = this._childNodes;
    if (this.store.sortFn) {
      nodes = this.store.sortFn(nodes);
    }
    return nodes;
  }
  /**
     * set the content of node
     * @memberof Node
     * @param {*} value
     */
  set label(value) {
    setPropertyForData(this, 'label', value);
  }
  /**
     * get the content of node
     * @memberof Node
     */
  get label() {
    return getPropertyFromData(this, 'label');
  }
}
