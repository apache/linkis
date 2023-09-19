<template>
    <div class="menu-wrapper">
        <f-menu mode="vertical" id="menu">
            <template v-for="menuItem in menuItemsConfig" :key="menuItem.title">
                <template v-if="menuItem.items">
                    <f-sub-menu :value="menuItem.title">
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
                                    :value="menuItem.title + subMenuItem.title"
                                    @click="
                                        handleClick(subMenuItem?.path ?? '/')
                                    "
                                >
                                    <template #label>
                                        <div class="title-text">
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
        icon: '',
        path: '/console/globalHistoryManagement',
    },
    {
        title: 'message.linkis.sideNavList.function.children.resource',
        icon: '',
        path: '/console/resource',
    },
    {
        title: 'message.linkis.sideNavList.function.children.setting',
        icon: '',
        path: '/console/parameterConfig',
    },
    {
        title: 'message.linkis.sideNavList.function.children.dateReport',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.ECMManage',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.microserviceManage',
        path: '',
    },
    {
        title: 'message.linkis.sideNavList.function.children.dataSourceManage',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.udfFunctionTitle',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.basedataManagement',
        items: [{ title: 'xxx', path: '' }],
    },
    {
        title: 'message.linkis.sideNavList.function.children.codeQuery',
        path: '',
    },
];

const handleClick = (path: string) => {
    router.push(path);
};
</script>

<style src="./index.less" scoped></style>
