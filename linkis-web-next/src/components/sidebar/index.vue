<template>
    <div class="menu-wrapper">
        <f-menu mode="vertical" id="menu">
            <template v-for="menuItem in menuItemsConfig" :key="menuItem.title">
                <template v-if="menuItem.items">
                    <f-sub-menu :value="menuItem.title">
                        <template #label>
                            <div class="title-text">{{ menuItem.title }}\</div>
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
                                            {{ subMenuItem.title }}
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
                                {{ menuItem.title }}
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
    { title: '任务概览', icon: '', path: '/console/globalHistoryManagement' },
    { title: '资源管理', icon: '', path: '/console/resource/manage' },
    { title: '参数配置', icon: '', path: '' },
    { title: '全局变量', path: '' },
    { title: 'ECM管理', path: '' },
    { title: '微服务管理', path: '' },
    { title: '数据源管理', items: [{ title: 'xxx', path: '' }] },
    { title: 'UDF函数', items: [{ title: 'xxx', path: '' }] },
    { title: '基础数据管理', items: [{ title: 'xxx', path: '' }] },
    { title: '代码检索', path: '' },
];

const handleClick = (path: string) => {
    router.push(path);
};
</script>

<style src="./index.less" scoped></style>
