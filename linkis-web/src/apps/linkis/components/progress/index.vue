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
  <div
    class="progress-content"
    @click="expandChange"
    :class="{'children': children}">
    <span
      class="item-progress"
      :style="{'width': progressData.percent}"></span>
    <div class="text-content">
      <Icon
        v-if="!children"
        class="item-icon"
        :type="!progressData.expand? 'md-arrow-dropup' : 'md-arrow-dropdown'"
      ></Icon>
      <span class="item-label">{{ progressData.title }}</span>
      <span class="item-progress-label">{{ progressData.percent }}</span>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    progressData: {
      type: Object,
      default: () => {}
    },
    children: {
      type: Boolean,
      default: false
    },
    parentData: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {

    }
  },
  methods: {
    expandChange() {
      // Returns the parent component to change the expanded state(返回父组件更改展开状态)
      if(!this.children) this.progressData.expand = !this.progressData.expand;
      this.$emit('expandChange', this.progressData, this.children, this.parentData)
    }
  }
}
</script>
<style src="./index.scss" lang="scss" scoped></style>

