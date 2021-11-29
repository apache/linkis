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
  <li 
    class="node-item v-ellipsis"
    :class="{[item.iconCls]:true, [`level-${item.level}`]: true}"
  >
    <template v-if="render">
      <render-node
        :render="render"
        :item="item"/>
    </template>
    <template v-else>
      {{item.name}}
    </template>
  </li>
</template>
<script>
import RenderNode from './renderNode';
export default {
  name: 'VirtualTreeNode',
  components: {
    RenderNode
  },
  props: {
    item: Object,
    render: Function
  }
}
</script>
<style scoped>
.node-item {
  padding-left: 48px;
}
.node-item.level-1 {
  padding-left: 0;
}
.node-item.level-2 {
  padding-left: 16px;
}
.node-item.level-3 {
  padding-left: 32px;
}
.node-item.level-4 {
  padding-left: 48px;
}
</style>