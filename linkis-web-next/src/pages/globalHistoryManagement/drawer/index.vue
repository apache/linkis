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
        :height="drawerHeight"
        contentClass="history-management-drawer-content"
    >
        <template #title>
            <div class="drawer-title">
                <div class="buttons">
                    <div class="button" @click="expandDrawer" v-if="!isFullScreen">
                        {{ $t('message.common.fullScreen') }}
                    </div>
                    <div class="button" @click="foldDrawer" v-else>
                        {{ $t('message.linkis.fold') }}
                    </div>
                    <div class="delimiter"></div>
                    <div class="button" @click="show = false">
                        {{ $t('message.linkis.close') }}
                    </div>
                </div>
                <div class="title-text">
                    {{ $t('message.linkis.jobId') }}：{{ tableRawData?.taskID }}
                </div>
                <FTabs @change="onDrawerTabChange" class="drawer-tabs" v-model="activeDrawerTabPane">
                    <template v-for="tab in tabs" :key="tab.name">
                        <FTabPane
                            :name="t(tab.name)"
                            :value="tab.value"
                        />
                    </template>
                </FTabs>
            </div>
        </template>
        <template #default>
            <task-logs v-if="activeDrawerTabPane === 1" :task="task" :isFullScreen="isFullScreen" />
            <div v-if="activeDrawerTabPane === 2"></div>
            <div v-if="activeDrawerTabPane === 3"></div>
        </template>
    </FDrawer>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import TaskLogs from './taskLogs.vue';
import api from '@/service/api';

const { t } = useI18n();

enum TAB_PANE_VALUE {
    'TASK_LOG' = 1,
    'TASK_RESULT' = 2,
    'ENGINE_LOG' = 3,
}

const tabs = [
    { name: 'message.linkis.log', value: TAB_PANE_VALUE.TASK_LOG },
    { name: 'message.linkis.result', value: TAB_PANE_VALUE.TASK_RESULT },
    { name: 'message.linkis.detail', value: TAB_PANE_VALUE.ENGINE_LOG },
];

const activeDrawerTabPane = ref(TAB_PANE_VALUE.TASK_LOG);
const show = ref(false);
const tableRawData = ref<Record<string, string | number>>();
const drawerHeight = ref(520);
const drawerRef = ref<any>();
const task = ref<any>();
const isFullScreen = ref(false);

const open = async (rawData: Record<string, string | number>) => {
    tableRawData.value = rawData;
    try {
        const jobhistory = await api.fetch(`/jobhistory/${tableRawData.value.taskID}/get`, 'get')
        task.value = jobhistory.task;
    } catch (errorMsg) {
        window.console.error(errorMsg)
    }
    activeDrawerTabPane.value = TAB_PANE_VALUE.TASK_LOG;
    show.value = true;
};

const expandDrawer = () => {
    drawerHeight.value = document.body.clientHeight;
    isFullScreen.value = true;
};
const foldDrawer = () => {
    drawerHeight.value = 520;
    isFullScreen.value = false;
};

const onDrawerTabChange = (value: TAB_PANE_VALUE) => {
    activeDrawerTabPane.value = value;
};

defineExpose({
    open,
});
</script>

<style lang="less">
.history-management-drawer-content {
    .fes-drawer-header {
        padding: 0;
        .fes-tabs-tab-pane-wrapper {
            display: none;
        }
    }
    .fes-scrollbar-container {
        padding: 0;
    }
}
</style>
<style scoped lang="less">
.drawer-title {
    width: 100%;
    position: relative;
    .buttons {
        position: absolute;
        right: 16px;
        top: 5px;
        display: flex;
        gap: 12px;
        font-size: 14px;
        color: #63656f;
        line-height: 22px;
        font-weight: 400;
        .delimiter {
            width: 1px;
            height: 16px;
            background: #e3e8ee;
            transform: translateY(3px);
        }
        .button {
            cursor: pointer;
        }
    }
    .title-text {
        height: 32px;
        padding: 6px 20px;
        font-family: PingFangSC-Medium;
        font-size: 14px;
        color: #0f1222;
        font-weight: bold;
    }
    .drawer-tabs {
        background-color: #f8f9fc;
        padding-left: 4px;
    }
}
</style>
