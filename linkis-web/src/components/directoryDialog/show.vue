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
  <Modal
    v-model="show"
    class="we-import-dialog"
    width="360">
    <div slot="header">
      <span>{{ title }}</span>
    </div>
    <div class="we-import-dialog-content">
      <span>{{$t('message.common.path')}}</span>
      <directory-dialog
        :tree="tree"
        :load-data-fn="loadDataFn"
        :filter-node="filterNode"
        :path="path"
        @set-node="setNode"/>
    </div>
    <div slot="footer">
      <Button
        type="primary"
        @click="submit">{{$t('message.common.ok')}}</Button>
    </div>
  </Modal>
</template>
<script>
import directoryDialog from './index.vue';
export default {
  name: 'ShowDialog',
  components: {
    directoryDialog,
  },
  props: {
    filterNode: Function,
    loadDataFn: Function,
    path: {
      type: String,
      require: true,
    },
    fsType: {
      type: String,
      default: 'file',
    },
    tree: {
      type: Array,
      require: true,
      default: () => [],
    },
    title: String,
  },
  data() {
    return {
      show: false,
      node: null,
    };
  },
  methods: {
    open() {
      this.show = true;
    },
    setNode(node) {
      this.node = node;
      this.$emit('set-node', node, this.fsType);
    },
    submit() {
      this.show = false;
      this.$emit('import', this.node);
    },
  },
};
</script>
