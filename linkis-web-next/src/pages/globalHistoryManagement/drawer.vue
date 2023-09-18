<template>
    <FDrawer
        v-model:show="show"
        placement="bottom"
        hight="700px"
        @ok="show = false"
        :closable="false"
        :resizable="true"
        contentClass="drawer-content"
    >
        <template #title>
            <div class="drawer-title">
                <div class="buttons">
                    <div class="full-screen-button">全屏</div>
                    <div class="delimiter"></div>
                    <div class="close-button" @click="show = false">关闭</div>
                </div>
                <div class="title-text">任务ID：{{ tableRawData?.taskId }}</div>
                <FTabs @change="onTabChange" class="tabs">
                    <template v-for="tab in tabs" :key="tab.name">
                        <FTabPane
                            :name="tab.name"
                            :value="tab.value"
                        ></FTabPane>
                    </template>
                </FTabs>
                <div class="sub-tabs">
                    <FTabs @change="onTabChange" class="tabs" type="card">
                        <template v-for="tab in logClassTabs" :key="tab.name">
                            <FTabPane
                                :name="
                                    tab.value === LOG_CLASS_PANE_VALUE.ALL
                                        ? `${tab.name}`
                                        : `${tab.name}(${tab.count})`
                                "
                                :value="tab.value"
                                :style="`color:${tab.color}`"
                            >
                            </FTabPane>
                        </template>
                    </FTabs>
                    <FInput
                        class="search-input"
                        placeholder="搜索
                    "
                    />
                </div>
            </div>
        </template>
        <template #default>
            <div class="editor-wrapper">
                <WeEditor ref="logEditor" type="log"></WeEditor>
            </div>
        </template>
    </FDrawer>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import WeEditor from '@/components/editor/editorNext2.vue';

enum TAB_PANE_VALUE {
    'TASK_LOG' = 1,
    'TASK_RESULT',
    'ENGINE_LOG',
}

enum LOG_CLASS_PANE_VALUE {
    'ALL' = 1,
    'WARNING',
    'ERROR',
    'INFO',
}

const tabs = [
    { name: '任务日志', value: TAB_PANE_VALUE.TASK_LOG },
    { name: '任务结果', value: TAB_PANE_VALUE.TASK_RESULT },
    { name: '引擎日志', value: TAB_PANE_VALUE.ENGINE_LOG },
];

const logClassTabs = [
    {
        name: 'All',
        value: LOG_CLASS_PANE_VALUE.ALL,
        count: 100,
        color: '#0F1222',
    },
    {
        name: 'Warning',
        value: LOG_CLASS_PANE_VALUE.WARNING,
        count: 100,
        color: '#F29360',
    },
    {
        name: 'Error',
        value: LOG_CLASS_PANE_VALUE.ERROR,
        count: 100,
        color: '#F75F56 ',
    },
    {
        name: 'Info',
        value: LOG_CLASS_PANE_VALUE.INFO,
        count: 100,
        color: '#5384FF',
    },
];

const activeTabPane = ref(TAB_PANE_VALUE.TASK_LOG);

const show = ref(false);
const tableRawData = ref<Record<string, string | number>>();
const content = ref<string>('');

const open = (rawData: Record<string, string | number>) => {
    tableRawData.value = rawData;
    // TODO: editor value
    show.value = true;
};

const onTabChange = (value: TAB_PANE_VALUE) => {
    activeTabPane.value = value;
};

defineExpose({
    open,
});
</script>

<style>
.drawer-content {
    .fes-scrollbar-container {
        padding: 0;
    }
    .fes-drawer-header {
        padding: 0;
    }
    .fes-tabs-nav-wrapper {
        padding-left: 20px;
    }
    .fes-tabs.fes-tabs-top.tabs {
        background-color: #f7f7f8;
        padding-left: 0px;
    }
    .margin-view-overlays {
        background-color: #f7f7f8;
    }
}
</style>
<style scoped>
:deep(.fes-tabs-tab-pane-wrapper) {
    display: none;
}
:deep(.fes-drawer-header) {
    border-bottom: none;
    display: block;
    :deep(.fes-tabs) {
        background-color: #f7f7f8;
    }
}

:deep(.tabs) {
    transform: translateX(-16px);
}

.drawer-title {
    width: 100%;
    position: relative;
    background-color: #f7f7f8;
    .buttons {
        position: absolute;
        right: 16px;
        top: 5px;
        display: flex;
        gap: 12px;
        font-family: PingFangSC-Regular;
        font-size: 14px;
        color: #63656f;
        letter-spacing: 0;
        line-height: 22px;
        font-weight: 400;
        .delimiter {
            width: 1px;
            height: 14px;
            background: #e3e8ee;
        }
        .close-button,
        .full-screen-button {
            cursor: pointer;
        }
    }
    .title-text {
        height: 32px;
        padding: 6px 20px;
        font-family: PingFangSC-Medium;
        font-size: 14px;
        color: #0f1222;
        font-weight: 500;
        background-color: #fff;
    }
    .sub-tabs {
        display: flex;
        outline: none;
        box-shadow: none;
        border: none;
        background-color: #f7f7f8;
        .tabs {
            transform: translateX(-4px);
        }
        :deep(.fes-tabs-tab-active) {
            background-color: #fff;
        }
        :deep(.fes-input-inner) {
            margin-left: 95px;
            height: 24px;
            width: 140px;
            margin-top: 4px;
        }
    }
}
</style>
