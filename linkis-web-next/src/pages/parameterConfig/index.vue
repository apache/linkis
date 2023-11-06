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
    <f-layout class="wrapper">
        <f-header class="f-header" style="margin-bottom: 19px">
            <div class="title">应用列表：</div>
            <FTabs @change="onTabChange">
                <FTabPane name="IDE" value="/console/parameterConfig/ide">
                </FTabPane>
                <FTabPane
                    name="Scripts"
                    value="/console/parameterConfig/scripts"
                    displayDirective="show"
                >
                </FTabPane>
                <FTabPane
                    name="TableauServer"
                    value="/console/parameterConfig/TableauServer"
                    displayDirective="show"
                >
                </FTabPane>
            </FTabs>
            <div class="options">
                <FButton type="link" @click="openDrawer">
                    应用及引擎管理
                </FButton>
                <div class="delimiter"></div>
                <FButton type="link" @click="openModal">全局配置</FButton>
                <Drawer ref="drawerRef"></Drawer>
                <Modal ref="modalRef"> </Modal>
            </div>
        </f-header>
        <f-main>
            <router-view></router-view>
        </f-main>
    </f-layout>
</template>
<script setup lang="ts">
import { Ref, ref } from 'vue';
import { useRouter } from 'vue-router';
import Drawer from './drawer.vue';
import Modal from './modal.vue';

const router = useRouter();
const drawerRef = ref<Ref<{ open: () => void }> | null>(null);
const modalRef = ref<Ref<{ open: () => void }> | null>(null);

const openDrawer = () => {
    drawerRef.value?.open();
};

const openModal = () => {
    modalRef.value?.open();
};

const onTabChange = (key: string) => {
    router.push(key);
};
</script>
<style lang="less" scoped>
:deep(.fes-tabs-tab-pane-wrapper) {
    display: none;
}

.f-header {
    position: relative;
    height: 30px;
    display: flex;

    .title {
        height: 22px;
        transform: translateY(-12px);
    }
    .options {
        display: flex;
        position: absolute;
        right: 0;
        align-items: center;

        .delimiter {
            background: #d8d8d8;
            width: 1px;
            height: 14px;
        }
    }
}
</style>
