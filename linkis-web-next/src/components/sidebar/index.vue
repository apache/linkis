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
        <f-menu mode="vertical" id="menu" :collapsed="collapsed" accordion v-model="activePath">
            <template v-for="menuItem in menuItemsConfig" :key="menuItem.title">
                <template v-if="menuItem.items">
                    <f-sub-menu :value="menuItem.path">
                        <template #icon>
                            <img :src="menuItem.icon" />
                        </template>
                        <template #label>
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
                                    :value="subMenuItem.path"
                                    @click="
                                        handleClick(subMenuItem.path ?? '/')
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
                        :value="menuItem.path"
                        @click="handleClick(menuItem?.path ?? '/')"
                    >
                        <template #icon>
                            <img :src="menuItem.icon" />
                        </template>
                        <template #label>
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
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

defineProps<{
    collapsed: boolean
}>();

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
        title: 'message.linkis.sideNavList.function.children.globalVariable',
        icon: '/sidebar/globalVariable.svg',
        path: '/console/globalVariables',
    },
    {
        title: 'message.linkis.sideNavList.function.children.ECMManage',
        icon: '/sidebar/ECMManage.svg',
        path: '/console/ECMManagement',
    },
    {
        title: 'message.linkis.sideNavList.function.children.microserviceManage',
        icon: '/sidebar/microserviceManage.svg',
        path: '/console/microServiceManagement',
    },
    {
        title: 'message.linkis.sideNavList.function.children.dataSourceManage',
        icon: '/sidebar/dataSourceManage.svg',
        path: '/xxxx',
        items: [{ title: 'xxx', path: '/xxxx' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.udfFunctionTitle',
        icon: '/sidebar/udfFunctionTitle.svg',
        path: '/xxxxx',
        items: [{ title: 'xxx', path: '/xxxxx' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.basedataManagement',
        icon: '/sidebar/basedataManagement.svg',
        path: '/xxxxxx',
        items: [{ title: 'xxx', path: '/xxxxxx' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.codeQuery',
        icon: '/sidebar/codeQuery.svg',
        path: '/xxxxxxxxx',
    },
];

// 获取当前路径，使得sidebar显示的当前菜单能够与当前路径相匹配
const getActivePath = () => {
    const currentPath = router.currentRoute.value.path;
    let activePath = currentPath;
    menuItemsConfig.forEach(config => {
        if(currentPath.startsWith(config.path)){
            activePath = config.path;
            if(config.items){
                config.items.forEach(subItem => {
                    if(currentPath.startsWith(subItem.path)){
                        activePath = subItem.path;
                    }
                })
            }
        }
    })
    return activePath;
}

const activePath = ref(getActivePath());

const handleClick = (path: string) => {
    router.push(path);
};

watch(
    () => router.currentRoute.value.path,
    () => {
        activePath.value = getActivePath();
    }
);
</script>

<style src="./index.less" scoped></style>