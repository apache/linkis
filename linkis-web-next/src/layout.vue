<template>
    <div class="wd-page" @click="changeMenusDisplay">
        <!-- <BHorizontalLayout
            v-model:curPath="curPath"
            :menus="menus"
            @menuChange="onMenuClick"
        >
            <template v-slot:top>
                <div class="wd-logo">linkis</div>
            </template>
            <template v-slot:container>
                <div style="margin: -16px -16px -32px">
                    <router-view></router-view>
                </div>
            </template>
        </BHorizontalLayout> -->

        <!-- <template v-for="(rt, index) in menus" :key="index">
            <span
                @click="myRouteHandler(rt.value)"
                style="border: 1px solid black"
                >{{ rt.label }}</span
            >
        </template> -->

        <div class="wrapper">
            <f-layout style="margin-top: 20px">
                <!-- <f-header></f-header> -->
                <f-layout>
                    <f-aside style="margin-right: 16px">
                        <Sidebar></Sidebar>
                    </f-aside>
                    <f-main
                        style="
                            margin-top: 16px;
                            padding: 24px;
                            background-color: #fff;
                        "
                    >
                        <router-view></router-view>
                    </f-main>
                </f-layout>
            </f-layout>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, h, computed } from 'vue';
import type { VNode } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { AppstoreOutlined, SettingOutlined } from '@fesjs/fes-design/icon';
// import { BHorizontalLayout } from '@webank/bdp-design';
import Sidebar from '@/components/sidebar/index.vue';

const myRouteHandler = (rt: string) => {
    console.log(rt);
};

const changeMenusDisplay = () => {
    console.log(1);
};

const route = useRoute();
const router = useRouter();
interface menuItem {
    label: string;
    value: string;
    icon: () => VNode;
    children?: menuItem[];
}
const path = computed(() => route.path);
// 用于反显
const curPath = computed(() => {
    // eslint-disable-next-line no-use-before-define
    const curMenuItem: menuItem | undefined = menus.value.find(
        (item: menuItem) =>
            path.value.includes(item.value) &&
            path.value.length > item.value.length,
    );

    return curMenuItem?.value || '';
});
const menus = ref<menuItem[]>([
    {
        label: '全局历史',
        value: '/console/globalHistoryManagement',
        icon: () => h(AppstoreOutlined),
    },
    {
        label: '资源管理',
        value: '/console/resource',
        icon: () => h(SettingOutlined),
    },
    // {
    //     label: '任务查询1',
    //     value: '/tasks1',
    //     icon: () => h(AppstoreOutlined),
    // },
    // {
    //     label: '任务查询2',
    //     value: '/tasks2',
    //     icon: () => h(AppstoreOutlined),
    // },
    // {
    //     label: '任务查询3',
    //     value: '/tasks3',
    //     icon: () => h(AppstoreOutlined),
    // },
    // {
    //     label: '任务查询4',
    //     value: '/tasks4',
    //     icon: () => h(AppstoreOutlined),
    // },
    // {
    //     label: '任务查询5',
    //     value: '/tasks5',
    //     icon: () => h(AppstoreOutlined),
    // },
    // {
    //     label: '任务查询6',
    //     value: '/tasks6',
    //     icon: () => h(AppstoreOutlined),
    // },
    // { label: '任务查询7', value: '/tasks7', icon: () => h(AppstoreOutlined) },
    // { label: '任务查询8', value: '/tasks8', icon: () => h(AppstoreOutlined) },
    // { label: '任务查询9', value: '/tasks9', icon: () => h(AppstoreOutlined) },
    // {
    //     label: '任务查询10',
    //     value: '/tasks10',
    //     icon: () => h(AppstoreOutlined),
    // },
]);
const currentPath = ref<string>('');
const onMenuClick = (v: { value: string }) => {
    currentPath.value = v.value;
    router.push(v.value);
};
</script>
<style lang="less" scoped>
@import '@/style/variable.less';

* {
    overflow: hidden;
}
.wrapper {
    width: 100%;
    height: 100vh;
}

.wd-side-menus {
    position: relative;
    width: 220px;
    // display: grid;
    grid-template-rows: 64px 1fr 48px;
    background: #fff;
    transition: all ease 0.2s;

    &.collapse {
        width: 56px;
        flex: 0 0 56px;

        .collapse-btn {
            .collapse-icon {
                transform: rotate(-90deg);
            }
        }

        .wd-logo {
            background: none;
        }

        .wd-menu-text {
            display: none;
        }

        .copyright {
            display: none;
        }
    }

    .collapse-btn {
        position: absolute;
        right: 16px;
        bottom: 70px;
        width: 24px;
        height: 24px;
        background: #ffffff;
        border-radius: 12px;
        box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.08);
        text-align: center;
        cursor: pointer;
        user-select: none;

        .collapse-icon {
            transform: rotate(90deg);
            transition: transform ease 0.2s;
        }
    }

    .wd-logo {
        position: relative;
        background: url(@/assets/images/logo.svg) 16px center no-repeat;
        background-size: 110px;
        height: 64px;

        .simulator-badge {
            font-size: 12px;
            color: #b7b7bc;
        }

        .avatar {
            position: absolute;
            top: 4px;
            right: 0;
            z-index: 10;
            width: 56px;
            height: 40px;
            padding: 14px 16px 0;
            cursor: pointer;
            background: #fff;

            .user-menus-list {
                &.active {
                    display: block;
                }

                display: none;
                position: absolute;
                top: 40px;
                left: 16px;
                min-width: 160px;
                background: #fff;
                border-radius: 4px;
                box-shadow: 0 2px 12px rgba(15, 18, 34, 0.1);

                .user-name {
                    padding: 16px;
                    border-bottom: 1px solid rgba(15, 18, 34, 0.06);
                }
            }
        }
    }

    .wd-menus-list {
        height: calc(100% - 112px);
    }

    .wd-menu-item {
        position: relative;
        display: flex;
        align-items: center;
        padding: 0 16px;
        height: 54px;
        line-height: 54px;
        color: #0f1222;
        cursor: pointer;
        transition: background ease 0.3s;
        user-select: none;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;

        &.small {
            height: 36px;
            line-height: 36px;

            &::after {
                display: none;
            }
        }

        &::after {
            content: '';
            position: absolute;
            right: 0;
            top: 50%;
            transform: translateY(-50%);
            display: block;
            width: 2px;
            height: 22px;
            background: transparent;
            transition: all ease 0.3s;
        }

        &:hover,
        &.router-link-active {
            color: #5384ff;
            background: rgba(83, 132, 255, 0.06);

            .wd-menu-icon {
                filter: invert(45%) sepia(13%) saturate(4258%)
                    hue-rotate(197deg) brightness(108%) contrast(100%);
            }

            &::after {
                background: #5384ff;
            }
        }

        .wd-menu-icon {
            margin-right: 8px;
            width: 14px;
        }

        .s-user-ctn {
            position: relative;
            padding-right: 50px;

            .user-logout-btn {
                position: absolute;
                z-index: 10;
                top: 0;
                right: 0;
                padding: 0 8px;
                color: @blue-color;
            }
        }
    }

    .copyright {
        height: 48px;
        line-height: 48px;
        font-size: 12px;
        color: rgba(15, 18, 34, 0.2);
        text-align: center;
    }
}

.simulator-user-list {
    display: flex;
    align-items: center;

    .list-label {
        width: 80px;
        padding-right: 16px;
    }

    .list-ctn {
        flex: 1;
    }
}
</style>
