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
    <FButton class="btn" v-if="resultType" type="primary" @click="isShowModal=true">{{ t('message.common.download') }}</FButton>
    <div v-if="resultType === '1'" class="text">
        <p v-for="text in textArr">{{ text }}</p>
    </div>
    <div v-else-if="resultType === '2'" class="table">
        <FTable
            :columns="columnsData"
            :data="tableData.fileContent"
            :height="props.isFullScreen ? 600 : 400"
        />
    </div>
    <div v-else :style="{ height: props.isFullScreen ? '85vh' : '420px' }" class="no-data">
        <img src="/log/noLog.svg" />
        <div>{{ t('message.linkis.noDataText') }}</div>
    </div>
    <FModal v-model:show="isShowModal" @ok="handleDownload">
        <FForm>
            <FFormItem :label="t('message.common.toolbar.format')">
                <FRadioGroup v-model="format">
                    <FRadio value="csv">CSV</FRadio>
                    <FRadio v-if="resultType === '2'" value="xlsx">Excel</FRadio>
                </FRadioGroup>
            </FFormItem>
            <FFormItem :label="t('message.common.toolbar.coding')">
                <FRadioGroup v-model="charset">
                    <FRadio value="utf-8">UTF-8</FRadio>
                    <FRadio value="gbk">GBK</FRadio>
                </FRadioGroup>
            </FFormItem>
            <FFormItem :label="t('message.common.toolbar.replace')">
                <FRadioGroup v-model="nullValue">
                    <FRadio value="NULL">NULL</FRadio>
                    <FRadio value="BLANK">{{ t('message.common.toolbar.emptyString') }}</FRadio>
                </FRadioGroup>
            </FFormItem>
            <FFormItem :label="t('message.common.toolbar.selectSeparator')" v-if="format === 'csv'">
                <FRadioGroup v-model="separator">
                    <FRadio value=",">{{ t('message.common.separator.comma') }}</FRadio>
                    <FRadio :value="`\t`">{{ t('message.common.separator.tab') }}</FRadio>
                    <FRadio value=" ">{{ t('message.common.separator.space') }}</FRadio>
                    <FRadio value="|">{{ t('message.common.separator.vertical') }}</FRadio>
                    <FRadio value=";">{{ t('message.common.separator.semicolon') }}</FRadio>
                </FRadioGroup>
            </FFormItem>
            <FFormItem :label="t('message.common.toolbar.downloadMode')">
                <FCheckbox :label="t('message.common.toolbar.allDownLoad')" v-model="allDownload" style="margin-right: 20px;"/>
                <FCheckbox :label="t('message.common.toolbar.autoFormat')" v-model="autoFormat" v-if="format === 'xlsx'" />
            </FFormItem>
        </FForm>
    </FModal>
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps<{
    task: any,
    isFullScreen: boolean,
}>();

const resultType = ref();
const resultList = ref([]);
const currentResultPath = ref('');
const tableData = ref();
const columnsData = ref();
const textArr = ref();
const isShowModal = ref(false);
const format = ref('csv');
const charset = ref('utf-8');
const nullValue = ref('NULL');
const separator = ref(',');
const allDownload = ref(false);
const autoFormat = ref(false);

const downloadFromHref = (url: string) => {
    const downloadLink = document.createElement('a');
    downloadLink.href = url;
    downloadLink.style.display = 'none';
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
}

const handleDownload = async () => {
    // Before downloading, use the heartbeat interface to confirm whether to log in(下载之前条用心跳接口确认是否登录)
    await api.fetch('/user/heartbeat', 'get');
    let url = `http://${window.location.host}/api/rest_j/v1/filesystem/resultsetToExcel?charset=${charset.value}&outputFileType=${format.value}&nullValue=${nullValue.value}`;
    if (format.value === 'csv') {
        url += `&csvSeparator=${encodeURIComponent(separator.value)}`;
        if (allDownload.value) {
            resultList.value.forEach((v: any, i: number) => {
                downloadFromHref(`${url}&path=${v.path}&outputFileName=ResultSet${i + 1}`);
            })
        } else {
            downloadFromHref(`${url}&path=${currentResultPath.value}&outputFileName=ResultSet${props.task.taskID}`);
        }
    }
    if (format.value === 'xlsx') {
        downloadFromHref(`${url}&autoFormat==${autoFormat.value}&path=${currentResultPath.value}&outputFileName=ResultSet${props.task.taskID}`);
    }
}

onMounted(async () => {
    if (props.task.resultLocation) {
        try {
            const rst = await api.fetch('/filesystem/getDirFileTrees', { path: props.task.resultLocation }, 'get');
            if (rst.dirFileTrees) {
                // The order of the result set in the background is sorted by string according to the name of the result set. When displaying, there will be a problem that the result set cannot be matched, so add sorting(后台的结果集顺序是根据结果集名称按字符串排序的，展示时会出现结果集对应不上的问题，所以加上排序)
                resultList.value = rst.dirFileTrees.children.sort((a: any, b: any) =>
                    parseInt(a.name.split('_')[1].split('.')[0], 10) - parseInt(b.name.split('_')[1].split('.')[0], 10)
                )
                if (resultList.value.length) {
                    currentResultPath.value = rst.dirFileTrees.children[0].path;
                    const ret = await api.fetch('/filesystem/openFile', {
                        path: currentResultPath.value,
                        page: 1,
                        pageSize: 5000
                    }, 'get');
                    resultType.value = ret.type;
                    if (ret.type === '1') {
                        textArr.value = ret.fileContent;
                    }
                    if (ret.type === '2') {
                        tableData.value = ret;
                        columnsData.value = ret.metadata.map((v: any, i: number) => ({
                            prop: i,
                            label: v.columnName,
                            sortable: true,
                        }))
                    }
                }
            }
        } catch (error) {
            window.console.error(error)
        }
    }
})
</script>

<style lang="less" scoped>
.btn {
    margin: 10px;
}
.text {
    margin: 0 10px;
    white-space: normal;
    word-break: break-all;
    word-wrap: break-word;
}
.table {
    margin: 0 10px;
}
.no-data {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 30px;
    color: #b7b7bc;
    font-size: 14px;
}
</style>