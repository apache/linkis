<template>
    <div class="menu-wrapper">
        <f-menu mode="vertical" id="menu">
            <template v-for="menuItem in menuItemsConfig" :key="menuItem.title">
                <template v-if="menuItem.items">
                    <f-sub-menu :value="menuItem.title">
                        <template #label>
                            <img :src="menuItem.icon">
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
                            <img :src="menuItem.icon">
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
    { title: '任务概览', icon: '../../../public/任务概览-常规.svg', path: '/console/globalHistoryManagement' },
    { title: '资源管理', icon: '../../../public/资源管理-常规.svg', path: '/console/resource' },
    { title: '参数配置', icon: '../../../public/参数配置-常规.svg', path: '/console/parameterConfig' },
    { title: '全局变量', icon: '../../../public/全局变量-常规.svg', path: '' },
    { title: 'ECM管理', icon: '../../../public/ECM管理-常规.svg', path: '' },
    { title: '微服务管理', icon: '../../../public/微服务管理-常规.svg', path: '' },
    { title: '数据源管理', icon: '../../../public/数据源管理-常规.svg', items: [{ title: 'xxx', path: '' }] },
    { title: 'UDF函数', icon: '../../../public/UDF函数-常规.svg', items: [{ title: 'xxx', path: '' }] },
    { title: '基础数据管理', icon: '../../../public/基础数据管理-常规.svg', items: [{ title: 'xxx', path: '' }] },
    { title: '代码检索', icon: '../../../public/代码检索-常规.svg', path: '' },
];

const handleClick = (path: string) => {
    router.push(path);
};
</script>

<style src="./index.less" scoped></style>
