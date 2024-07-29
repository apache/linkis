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
    <div class="sub-tabs">
        <FInput
            class="search-input"
            :placeholder="$t('message.linkis.search')"
            v-model="searchText"
            @change="handleSearch"
        />
        <FTabs @change="onTaskLogTabChange" class="task-log-tabs" type="card" v-model="activeTaskLogTabPane">
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
                    <WeEditor
                        :model-value="formattedLogs"
                        :height="props.isFullScreen ? '85vh' : '420px'"
                        v-if="formattedLogs !== ''"
                    />
                    <div v-else :style="{ height: props.isFullScreen ? '85vh' : '420px' }" class="no-log">
                        <img src="/log/noLog.svg" />
                        <div>{{ $t('message.linkis.noLog') }}</div>
                    </div>
                </FTabPane>
            </template>
        </FTabs>
    </div>
</template>

<script setup lang="ts">
import { trim, forEach, filter } from 'lodash';
import { reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import WeEditor from '@/components/editor/index.vue';
import api from '@/service/api';
import storage from '@/helper/storage';
import util from '@/util';

const { t } = useI18n();

enum LOG_CLASS_PANE_VALUE {
    'ALL' = 'all',
    'WARNING' = 'warning',
    'ERROR' = 'error',
    'INFO' = 'info',
}

const props = defineProps<{
    task: any,
    isFullScreen: boolean,
}>();

const activeTaskLogTabPane = ref(LOG_CLASS_PANE_VALUE.ALL);
const logs = ref<any>({});
const formattedLogs = ref('');
const fromLine = ref(1);
const searchText = ref('');
const errorNum = ref(0);
const warnNum = ref(0);
const infoNum = ref(0);

const logClassTabs = reactive([
    {
        name: 'All',
        value: LOG_CLASS_PANE_VALUE.ALL,
        count: 0,
        color: '#0F1222',
    },
    {
        name: 'Warning',
        value: LOG_CLASS_PANE_VALUE.WARNING,
        count: warnNum,
        color: '#F29360',
    },
    {
        name: 'Error',
        value: LOG_CLASS_PANE_VALUE.ERROR,
        count: errorNum,
        color: '#F75F56 ',
    },
    {
        name: 'Info',
        value: LOG_CLASS_PANE_VALUE.INFO,
        count: infoNum,
        color: '#5384FF',
    },
]);

const onTaskLogTabChange = (value: LOG_CLASS_PANE_VALUE) => {
    activeTaskLogTabPane.value = value;
    formattedLogs.value = getFormattedLogs();
}

const getPointNum = (logs:{ [key: string]: any }) => {
    const errorLogs = trim(logs.error).split('\n').filter((e) => !!e);
    const warnLogs = trim(logs.warning).split('\n').filter((e) => !!e);
    const infoLogs = trim(logs.info).split('\n').filter((e) => !!e);
    errorNum.value = errorLogs.length;
    warnNum.value = warnLogs.length;
    infoNum.value = infoLogs.length;
}

const getSearchList = (log:string) => {
    let MatchText = '';
    const val = searchText.value;
    if (!log) return MatchText;
    if (val) {
        // This part of the code is to make the regular expression not report an error, so add \ in front of the special symbol(这部分代码是为了让正则表达式不报错，所以在特殊符号前面加上\)
        let formattedVal = '';
        forEach(val, (o) => {
            if (/^[\w\u4e00-\u9fa5]$/.test(o)) {
                formattedVal += o;
            } else {
                formattedVal += `\\${o}`;
            }
        });
        // Global and case-insensitive mode, the regular pattern is to match characters other than newlines before and after searchText(全局和不区分大小写模式，正则是匹配searchText前后出了换行符之外的字符)
        const regexp = new RegExp(`.*${formattedVal}.*`, 'gi');
        MatchText = filter(log.split('\n'), (item) => {
            return regexp.test(item);
        }).join('\n');
    } else {
        MatchText = log;
    }
    return MatchText;
}

const getFormattedLogs = () => {
    const processedLogs:{ [key: string]: any } = {};
    Object.keys(logs.value).map((key) => {
        processedLogs[key] = getSearchList(logs.value[key]);
    });
    getPointNum(processedLogs);
    return processedLogs[activeTaskLogTabPane.value];
}

const handleSearch = () => {
    formattedLogs.value = getFormattedLogs();
}

watch(
    () => props.task,
    async () => {
        if(props.task) {
            if (!props.task.logPath) {
                const errCode = props.task.errCode
                ? `\n${t('message.linkis.errorCode')}：${
                    props.task.errCode
                }`
                : ''
                const errDesc = props.task.errDesc
                ? `\n${t('message.linkis.errorDescription')}：${
                    props.task.errDesc
                }`
                : ''
                const info = t('message.linkis.notLog') + errCode + errDesc
                logs.value = { all: info, error: '', warning: '', info: '' }
                fromLine.value = 1
                return
            }
            const params:any = {
                path: props.task.logPath
            }
            if (storage.get('isAdminView') === 'true') {
                params.proxyUser = props.task.umUser
            }
            let openLog:{ log: string | Array<string> } = { log: [] }
            try {
                if (props.task.status === 'Scheduled' || props.task.status === 'Running') {
                    const tempParams = {
                    fromLine: fromLine.value,
                    size: -1,
                    }
                    openLog = await api.fetch(`/entrance/${props.task.execID}/log`, tempParams, 'get')
                } else {
                    openLog = await api.fetch('/filesystem/openLog', params, 'get')
                }
                if (openLog) {
                    const log:{ [key: string]: any } = { all: '', error: '', warning: '', info: '' }
                    const convertLogs = util.convertLog(openLog.log)
                    Object.keys(convertLogs).forEach(key => {
                    if (convertLogs[key]) {
                        log[key] += convertLogs[key] + '\n'
                    }
                    })
                    logs.value = log
                    fromLine.value = log['all'].split('\n').length
                    formattedLogs.value = getFormattedLogs();
                }
            } catch (errorMsg) {
                window.console.error(errorMsg)
            }
            activeTaskLogTabPane.value = LOG_CLASS_PANE_VALUE.ALL;
        }
    }
)
</script>

<style scoped lang="less">
.sub-tabs {
    position: relative;
    .task-log-tabs {
        background-color: #f7f7f8;
        padding-left: 10px;
        .no-log {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            gap: 30px;
            color: #b7b7bc;
            font-size: 14px;
        }
    }
    :deep(.fes-tabs-tab-active) {
        background-color: #fff;
    }
    .search-input {
        margin-top: 4px;
        width: 140px;
        position: absolute;
        left: 480px;
        z-index: 1000;
    }
    :deep(.fes-input-inner) {
        height: 24px;
    }
}
</style>