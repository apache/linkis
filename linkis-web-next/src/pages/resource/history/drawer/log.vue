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
        <div class="btn-group">
            <FButton class="btn" :disabled="prevDisabled" @click="handleClickPrevBtn" type="primary">{{ t('message.linkis.previousPage') }}</FButton>
            <FButton class="btn" :disabled="nextDisabled" @click="handleClickNextBtn" type="primary">{{ t('message.linkis.nextPage') }}</FButton>
        </div>
        <FButton class="download-btn" type="primary" @click="handleDownloadLogs">{{ t('message.linkis.downloadLogs') }}</FButton>
        <FTabs @change="onTaskDetailTabChange" class="task-log-tabs" type="card" v-model="activeTaskLogTabPane">
            <FTabPane name="stdout" value="stdout" />
            <FTabPane name="stderr" value="stderr" />
            <FTabPane name="gc" value="gc" />
            <FTabPane v-if="['hive', 'spark'].includes(engine?.engineType)" name="yarnApp" value="yarnApp" />
        </FTabs>
        <WeEditor
            :model-value="formattedLogs"
            :height="props.isFullScreen ? '85vh' : '420px'"
            v-if="formattedLogs !== ''"
        />
        <div v-else :style="{ height: props.isFullScreen ? '85vh' : '420px' }" class="no-log">
            <img src="/log/noLog.svg" />
            <div>{{ t('message.linkis.noLog') }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { forEach, filter } from 'lodash';
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import WeEditor from '@/components/editor/index.vue';
import api from '@/service/api';
import { FModal } from '@fesjs/fes-design';

const { t } = useI18n();

const props = defineProps<{
    engine: any,
    isFullScreen: boolean,
}>();

const activeTaskLogTabPane = ref('stdout');
const logs = ref<any>({});
const formattedLogs = ref('');
const fromLine = ref(0);
const pageNow = ref(0);
const searchText = ref('');
const prevDisabled = ref(false);
const nextDisabled = ref(true);
const logPath = ref('');

const onTaskDetailTabChange = (value: string) => {
    activeTaskLogTabPane.value = value;
    fromLine.value = 0;
    pageNow.value = 0;
    nextDisabled.value = true;
    handleInitiation(fromLine.value);
}

const handleClickPrevBtn = () => {
    pageNow.value = pageNow.value + 1;
    fromLine.value = pageNow.value * 1000;
    nextDisabled.value = false;
    handleInitiation(fromLine.value);
}

const handleClickNextBtn = () => {
    pageNow.value = pageNow.value - 1;
    fromLine.value = pageNow.value * 1000;
    if (pageNow.value === 0) {
        nextDisabled.value = true;
    }
    handleInitiation(fromLine.value);
}

const handleDownloadLogs = () => {
    FModal.confirm({
        title: t('message.linkis.tip'),
        content: t('message.linkis.downloadLogsConfirm'),
        closable: true,
        okText: t('message.linkis.submit'),
        cancelText: t('message.linkis.cancel'),
        onOk() {
            const url = `/api/rest_j/v1/engineconnManager/downloadEngineLog?emInstance=${props.engine?.ecmInstance}&instance=${props.engine?.serviceInstance}&logDirSuffix=${encodeURIComponent(logPath.value)}&logType=${activeTaskLogTabPane.value}`;
            const downloadLink = document.createElement('a');
            downloadLink.href = url;
            downloadLink.download = 'log';
            downloadLink.style.display = 'none';
            document.body.appendChild(downloadLink);
            downloadLink.click();
            document.body.removeChild(downloadLink);
        },
    });
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
    return processedLogs[activeTaskLogTabPane.value];
}

const handleSearch = () => {
    formattedLogs.value = getFormattedLogs();
}

const handleInitiation = async (fromLine: number) => {
    if(props.engine?.ecmInstance) {
        try {
          let res = await api.fetch('/linkisManager/openEngineLog', {
            applicationName: 'linkis-cg-engineconn',
            emInstance: props.engine?.ecmInstance,
            instance: props.engine?.serviceInstance,
            parameters: {
                pageSize: 1000,
                fromLine,
                logType: activeTaskLogTabPane.value,
                logDirSuffix: props.engine?.logDirSuffix,
                ticketId: props.engine?.ticketId,
            }
          }, 'post') || {};
          if (res && res.result) {
            if (res.result.rows < 1000) {
                prevDisabled.value = true;
            } else {
                prevDisabled.value = false;
            }
            logs.value = {
              [activeTaskLogTabPane.value]: res.result.logs ? res.result.logs.join('\n') : ''
            }
            logPath.value = res.result.logPath.split('/').slice(0, -1).join('/');
          }
          handleSearch();
        } catch (err) {
          return;
        }
    }
}

onMounted(() => {
    handleInitiation(0);
})

watch(
    () => props.engine,
    () => {
        activeTaskLogTabPane.value = 'stdout';
        handleInitiation(0);
    }
)
</script>

<style scoped lang="less">
.sub-tabs {
    position: relative;
    .task-log-tabs {
        background-color: #f7f7f8;
        padding-left: 10px;
    }
    :deep(.fes-tabs-tab-active) {
        background-color: #fff;
    }
    :deep(.fes-tabs-tab-pane-wrapper) {
        display: none;
    }
    .search-input {
        margin-top: 4px;
        width: 140px;
        position: absolute;
        left: 480px;
        z-index: 1000;
    }
    .btn-group {
        display: flex;
        margin-top: 4px;
        height: 24px;
        position: absolute;
        left: 670px;
        z-index: 1000;
        gap: 10px;
        .btn {
            height: 24px;
        }
    }
    .download-btn {
        margin-top: 4px;
        height: 24px;
        position: absolute;
        right: 16px;
        z-index: 1000;
    }
    :deep(.fes-input-inner) {
        height: 24px;
    }
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
</style>