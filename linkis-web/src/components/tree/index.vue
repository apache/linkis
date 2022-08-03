<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<template>
  <div class="we-tree">
    <we-tree-node
      v-for="(node, key) in root.computedNodes"
      :key="getNodeKey(node, key)"
      :node="node"
      :fs-type="fsType"
      :is-root-default-open="node._level === 1 ? isRootDefaultOpen : false"
      :highlight-path="highlightPath"
      :before-remove="beforeRemove"
      :currentNode="currentNode"
    />
  </div>
</template>
<script>
import mixin from './mixin.js';
import Store from './model/store';
import WeTreeNode from './node.vue';
export default {
  name: 'WeTree',
  components: {
    WeTreeNode,
  },
  mixins: [mixin],
  props: {
    data: {
      type: Array,
      default: () => [],
    },
    nodeProps: {
      type: Object,
      default() {
        return {
          children: 'children',
          label: 'name',
          icon: 'icon',
          isLeaf: 'isLeaf',
        };
      },
    },
    indent: {
      type: Number,
      default: 16,
    },
    nodeKey: String,
    currentNodeKey: [String, Number],
    expandedKeys: Array,
    expandParent: {
      type: Boolean,
      default: true,
    },
    expandOnClickNode: {
      type: Boolean,
      default: true,
    },
    expandAll: Boolean,
    nodeEditValid: Function,
    beforeChange: Function,
    filterNode: Function,
    loadDataFn: Function,
    sortFn: Function,
    beforeRemove: Function,
    isRootDefaultOpen: {
      type: Boolean,
      defalut: false,
    },
    highlightPath: {
      type: String,
      default: '',
    },
    currentNode: {
      type: Object,
      defalut: {}
    }
  },
  data() {
    return {
      store: null,
      root: null,
      // currentNode: null,
    };
  },
  watch: {
    expandedKeys(newVal) {
      this.store.setExpandedKeys(newVal);
    },
    data: {
      handler: function() {
        this.initTree();
      },
      deep: true,
    },
    filterNode(v) {
      if (this.store) {
        this.store.filterNode = v
        this.store.filter()
      }
    }
  },
  created() {
    this.initTree();
  },
  methods: {
    initTree() {
      this.isTree = true;
      this.store = new Store({
        // todo lazy load && hiddenVisible
        data: this.data,
        key: this.nodeKey,
        props: this.nodeProps,
        currentNodeKey: this.currentNodeKey,
        expandedKeys: this.expandedKeys,
        expandParent: this.expandParent,
        expandAll: this.expandAll,
        filterNode: this.filterNode,
        nodeEditValid: this.nodeEditValid,
        beforeChange: this.beforeChange,
        loadDataFn: this.loadDataFn,
        sortFn: this.sortFn,
        beforeRemove: this.beforeRemove,
      });
      this.root = this.store.root;
    },
    getNodeKey(node, index) {
      const nodeKey = this.nodeKey;
      let key = index;
      if (!node.data) {
        return key;
      }
      if (nodeKey) {
        key = node.data[nodeKey];
      } else {
        key = node.data.name + index;
      }
      return key;
    },
  },
};
</script>
<style lang="scss" src="./index.scss"></style>
