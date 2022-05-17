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
  <div class="we-directory">
    <Input
      v-model="unPrefixPath"
      disabled
      class="we-directory-input"></Input>
    <div
      :class="{'is-hide':isHide}"
      class="we-directory-tree"
      :style="{'height': height + 'px'}">
      <Spin
        v-if="!(tree && tree.length)"
        size="large"
        fix/>
      <we-tree
        :data="tree"
        :node-props="nodeProps"
        :sort-fn="sortFn"
        :filter-node="filterNode"
        :load-data-fn="loadDataFn"
        @node-click="benchClick"/>
    </div>
  </div>
</template>
<script>
import { orderBy } from 'lodash';
export default {
  name: 'DirectoryDialog',
  props: {
    tree: {
      type: Array,
      require: true,
      default: () => [],
    },
    isHide: {
      type: Boolean,
      default: false,
      require: false,
    },
    path: {
      type: String,
      require: true,
      default: '',
    },
    fsType: {
      type: String,
      require: true,
      default: '',
    },
    filterNode: Function,
    loadDataFn: Function,
    height: {
      type: Number,
      default: 120,
    },
  },
  data() {
    return {
      nodeProps: {
        children: 'children',
        label: 'name',
        icon: 'icon',
        isLeaf: 'isLeaf',
      },
    };
  },
  computed: {
    unPrefixPath() {
      return this.path && this.path.slice(7, this.path.length);
    },
  },
  methods: {
    benchClick(...args) {
      this.$emit('set-node', args[1].node.data, this.fsType);
    },
    sortFn(data) {
      return orderBy(data, ['isLeaf', 'label'], ['asc', 'asc']);
    },
  },
};
</script>
<style lang="scss" src="./index.scss">
</style>
