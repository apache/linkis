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
  <div class="wd-page">
    <div class="wrapper">
      <f-layout>
        <f-header class="header">
          <f-button class="collapse-btn" @click="handleCollapse">
            <template #icon>
              <div v-if="collapsed"><IndentOutlined :size="24"/></div>
              <div v-else><OutdentOutlined :size="24"/></div>
            </template>
          </f-button>
        </f-header>
        <f-layout>
          <f-aside class="side">
            <Sidebar :collapsed="collapsed"></Sidebar>
          </f-aside>
          <f-layout fixed :style="{ left: collapsed ? '48px' : '200px' }">
            <f-main class="main">
              <router-view></router-view>
            </f-main>
          </f-layout>
        </f-layout>
      </f-layout>
    </div>
  </div>
</template>
<script setup lang="ts">
import Sidebar from "@/components/sidebar/index.vue";
import { IndentOutlined, OutdentOutlined } from '@fesjs/fes-design/icon';
import { ref } from "vue";

const collapsed = ref(false);

const handleCollapse = () => {
  collapsed.value = !collapsed.value;
}
</script>
<style lang="less" scoped>
.wd-page {
  height: 100%;
  background: #f7f8fa;
  .wrapper {
    width: 100%;
    height: 100%;
    .header {
      background: #fff;
      border: 1px solid rgba(15,18,34,0.06);
      box-shadow: 0 2px 12px 0 rgba(15,18,34,0.10);
      z-index: 10;
      .collapse-btn {
        border-radius: 0;
        border: 0;
        background: #5384ff;
        color: #fff;
        height: 54px;
        transform: translateY(-3px);
      }
    }
    .side {
      min-height: calc(100vh - 54px);
    }
    :deep(.fes-layout-container) {
      background: #f7f8fa;
    }
    .main {
      min-width: calc(100vw - 250px);
      margin: 16px;
      padding: 24px;
      background: #fff;
    }
  }
}
</style>
