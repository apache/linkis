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
    <div class="menu-wrapper">
        <f-menu mode="vertical" id="menu">
            <template v-for="menuItem in menuItemsConfig" :key="menuItem.title">
                <template v-if="menuItem.items">
                    <f-sub-menu :value="menuItem.title">
                        <template #label>
                            <img :src="menuItem.icon" />
                            <div class="title-text">
                                {{ $t(menuItem.title) }}
                            </div>
                        </template>
                        <f-menu-group>
                            <template
                                v-for="subMenuItem in menuItem.items"
                                :key="subMenuItem.title"
                            >
                                <f-menu-item
                                    :value="menuItem.title + subMenuItem.title"
                                    @click="
                                        handleClick(subMenuItem?.path ?? '/')
                                    "
                                >
                                    <template #label>
                                        <div class="submenu-item">
                                            {{ $t(subMenuItem.title) }}
                                        </div>
                                    </template>
                                </f-menu-item>
                            </template>
                        </f-menu-group>
                    </f-sub-menu>
                </template>
                <template v-else>
                    <f-menu-item
                        :value="menuItem.title"
                        @click="handleClick(menuItem?.path ?? '/')"
                    >
                        <template #label>
                            <img :src="menuItem.icon" />
                            <div class="title-text">
                                {{ $t(menuItem.title) }}
                            </div>
                        </template>
                    </f-menu-item>
                </template>
            </template>
        </f-menu>
    </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';

const router = useRouter();
const menuItemsConfig = [
    {
        title: 'message.linkis.sideNavList.function.children.globalHistory',
        icon: '/sidebar/globalHistory.svg',
        path: '/console/globalHistoryManagement',
    },
    {
        title: 'message.linkis.sideNavList.function.children.resource',
        icon: '/sidebar/resource.svg',
        path: '/console/resource',
    },
    {
        title: 'message.linkis.sideNavList.function.children.setting',
        icon: '/sidebar/setting.svg',
        path: '/console/parameterConfig',
    },
    {
        title: 'message.linkis.sideNavList.function.children.dateReport',
        icon: '/sidebar/dateReport.svg',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.ECMManage',
        icon: '/sidebar/ECMManage.svg',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.microserviceManage',
        icon: '/sidebar/microserviceManage.svg',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.dataSourceManage',
        icon: '/sidebar/dataSourceManage.svg',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.udfFunctionTitle',
        icon: '/sidebar/udfFunctionTitle.svg',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.basedataManagement',
        icon: '/sidebar/basedataManagement.svg',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.codeQuery',
        icon: '/sidebar/codeQuery.svg',
        path: '',
    },
];

const handleClick = (path: string) => {
    router.push(path);
};
</script>

<style src="./index.less" scoped></style>
<style scoped>
.submenu-item {
    height: 54px;
    line-height: 54px;
}
</style>
