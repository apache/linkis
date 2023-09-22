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
                            <template v-for="subMenuItem in menuItem.items" :key="subMenuItem.title">
                                <f-menu-item :value="menuItem.title + subMenuItem.title" @click="
                                    handleClick(subMenuItem?.path ?? '/')
                                    ">
                                    <template #label>
                                        <div class="submenu-item">
                                            {{ $t(subMenuItem.title) }}123
                                        </div>
                                    </template>
                                </f-menu-item>
                            </template>
                        </f-menu-group>
                    </f-sub-menu>
                </template>
                <template v-else>
                    <f-menu-item :value="menuItem.title" @click="handleClick(menuItem?.path ?? '/')">
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
        icon: '/任务概览-常规.svg',
        path: '/console/globalHistoryManagement',
    },
    {
        title: 'message.linkis.sideNavList.function.children.resource',
        icon: '/资源管理-常规.svg',
        path: '/console/resource',
    },
    {
        title: 'message.linkis.sideNavList.function.children.setting',
        icon: '/参数配置-常规.svg',
        path: '/console/parameterConfig',
    },
    {
        title: 'message.linkis.sideNavList.function.children.dateReport',
        icon: '/全局变量-常规.svg',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.ECMManage',
        icon: '/ECM管理-常规.svg',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.microserviceManage',
        icon: '/微服务管理-常规.svg',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.dataSourceManage',
        icon: '/数据源管理-常规.svg',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.udfFunctionTitle',
        icon: '/UDF函数-常规.svg',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.basedataManagement',
        icon: '/基础数据管理-常规.svg',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.codeQuery',
        icon: '/代码检索-常规.svg',
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
