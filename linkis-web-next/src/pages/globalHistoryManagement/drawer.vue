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
    <FDrawer
        ref="drawerRef"
        v-model:show="show"
        placement="bottom"
        @ok="show = false"
        :closable="false"
        resizable
        contentClass="drawer-content"
    >
        <template #title>
            <div class="drawer-title">
                <div class="buttons">
                    <div class="full-screen-button" @click="expandDrawer">
                        {{ $t('message.common.fullScreen') }}
                    </div>
                    <div class="delimiter"></div>
                    <div class="close-button" @click="show = false">
                        {{ $t('message.linkis.close') }}
                    </div>
                </div>
                <div class="title-text">
                    {{ $t('message.linkis.jobId') }}：{{ tableRawData?.taskID }}
                </div>
                <FTabs @change="onTabChange" class="tabs">
                    <template v-for="tab in tabs" :key="tab.name">
                        <FTabPane
                            :name="t(tab.name)"
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
                    <FInput class="search-input" placeholder="搜索" />
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
import { useI18n } from 'vue-i18n';
import WeEditor from '@/components/editor/editor.vue';

const { t } = useI18n();

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
    { name: 'message.linkis.log', value: TAB_PANE_VALUE.TASK_LOG },
    { name: 'message.linkis.result', value: TAB_PANE_VALUE.TASK_RESULT },
    { name: 'message.linkis.detail', value: TAB_PANE_VALUE.ENGINE_LOG },
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
const drawerHeight = ref(520);
const drawerRef = ref<any>();

const open = (rawData: Record<string, string | number>) => {
    tableRawData.value = rawData;
    // TODO: editor value
    show.value = true;
};

const expandDrawer = () => {
    drawerRef.value.height = document.body.clientHeight;
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
