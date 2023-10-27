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
    <FTabs @change="onTabChange">
        <FTabPane name="资源管理" value="/console/resource/manage"></FTabPane>
        <FTabPane
            name="历史管理"
            value="/console/resource/history"
            displayDirective="show"
        >
        </FTabPane>
    </FTabs>
    <router-view></router-view>
</template>
<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const onTabChange = (key: string) => {
    router.push(key);
};
</script>
<style lang="less" scoped>
:deep(.fes-tabs-tab-pane-wrapper) {
    display: none;
}
</style>
