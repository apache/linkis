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
    class="filter-view-item">
    <Icon
      type="ios-checkbox-outline"
      v-if="item.checked"
      size="14"
      color="#000"></Icon>
    <Icon
      type="ios-square-outline"
      size="14"
      color="#000"
      v-else></Icon>
    <span
      class="filter-view-item-text"
      :title="item.title">{{ item.title }}</span>
  </li>
</template>
<script>
export default {
  name: 'FilterItem',
  props: {
    item: Object
  }
}
</script>
<style lang="scss" scoped>
  .filter-view-item {
    line-height: 28px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .filter-view-item-text {
      width: 130px;
      margin-left: 4px;
      display: inline-block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      vertical-align: middle;
    }
  }
</style>
