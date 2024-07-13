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
    <div class="label">{{ $t('message.linkis.globalTaskQuery') }}</div>
    <div>
        <FForm v-show="!isCheckingTaskToStop" layout="inline" :span="4">
            <FFormItem
                :label="$t('message.linkis.columns.taskID')"
                :props="$t('message.linkis.columns.taskID')"
            >
                <FInput
                    v-model="formData.taskID"
                    placeholder="请输入"
                    type="number"
                />
            </FFormItem>
            <FFormItem
                :label="$t('message.linkis.columns.status')"
                :props="$t('message.linkis.columns.status')"
            >
                <FSelect
                    v-model="formData.status"
                    clearable
                    placeholder="请选择"
                >
                    <FOption
                        v-for="(item, index) in statusList"
                        :key="index"
                        :value="item.value"
                        :label="$t(item.label)"
                    />
                </FSelect>
            </FFormItem>
            <FFormItem
                :label="$t('message.linkis.columns.requestApplicationName')"
                :props="$t('message.linkis.columns.requestApplicationName')"
            >
                <FInput
                    v-model="formData.creator"
                    placeholder="请输入"
                />
            </FFormItem>
            <FFormItem
                :label="$t('message.linkis.columns.executeApplicationName')"
                :props="$t('message.linkis.columns.executeApplicationName')"
            >
                <FSelect
                    v-model="formData.executeApplicationName"
                    clearable
                    placeholder="请选择"
                >
                    <FOption
                        v-for="(item, index) in engineList"
                        :key="index"
                        :value="item.value"
                        :label="$t(item.label)"
                    />
                </FSelect>
            </FFormItem>
            <FFormItem
                :label="$t('message.linkis.columns.createdTime')"
                :prop="$t('message.linkis.columns.createdTime')"
                :span="8"
            >
                <!-- 日期修改完成后需要让日期选择组件失焦 -->
                <FDatePicker
                    type="daterange"
                    v-model="formData.timeRange"
                    :shortcuts="$props.shortcuts"
                    @focus="(e:any) => { e.target.blur() }"
                />
            </FFormItem>
            <FFormItem
                :label="$t('message.linkis.columns.umUser')"
                :props="$t('message.linkis.columns.umUser')"
                v-if="isAdminView"
            >
                <FInput
                    v-model="formData.proxyUser"
                    placeholder="请输入"
                />
            </FFormItem>
        </FForm>
        <div class="buttons">
            <template v-if="!isCheckingTaskToStop">
                <FButton type="primary" @click="search">
                    {{ $t('message.linkis.find') }}
                </FButton>
                <FButton @click="reset">
                    {{ $t('message.linkis.reset') }}
                </FButton>
                <FButton @click="switchAdmin" v-show="isLogAdmin || isHistoryAdmin">
                    {{ isAdminView ? $t('message.linkis.generalView') : $t('message.linkis.manageView') }}
                </FButton>
                <FButton @click="isCheckingTaskToStop = true">
                    {{ $t('message.linkis.batchStop') }}
                </FButton>
            </template>
        </div>
        <template v-if="isCheckingTaskToStop">
            <div>
                <FButton
                    type="primary"
                    class="confirm-stop-button"
                    @click="confirmStop"
                >
                    {{ $t('message.linkis.confirmToStop') }}
                </FButton>
                <FButton
                    class="confirm-stop-button"
                    @click="isCheckingTaskToStop = false"
                >
                    {{ $t('message.linkis.cancel') }}
                </FButton>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { FForm, FMessage } from '@fesjs/fes-design';
import { ref, onMounted, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import api from '@/service/api';
import { CheckedRow, CheckedRows } from './type';
import storage from '@/helper/storage';

const { t } = useI18n();

const emit = defineEmits(['search']);
const props = defineProps<{
    tableRef: any;
    pageSize: number;
    currentPage: number;
    checkedRows: CheckedRows;
    isLogAdmin: boolean;
    isHistoryAdmin: boolean;
    shortcuts: any;
}>();

const isCheckingTaskToStop = ref(false);

const formData = reactive({
    taskID: '',
    status: '',
    executeApplicationName: '',
    timeRange: [new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()],
    creator: '',
    proxyUser: '',
    isAdminView: false,
});

const engineList = ref<Array<{ label: string; value: string }>>([]);
const isAdminView = ref<boolean>(storage.get('isAdminView') === 'true');

const statusList = ref([
    {
        label: 'message.linkis.statusType.all',
        value: '',
    },
    {
        label: 'message.linkis.statusType.inited',
        value: 'Inited',
    },
    {
        label: 'message.linkis.statusType.running',
        value: 'Running',
    },
    {
        label: 'message.linkis.statusType.succeed',
        value: 'Succeed',
    },
    {
        label: 'message.linkis.statusType.cancelled',
        value: 'Cancelled',
    },
    {
        label: 'message.linkis.statusType.failed',
        value: 'Failed',
    },
    {
        label: 'message.linkis.statusType.scheduled',
        value: 'Scheduled',
    },
    {
        label: 'message.linkis.statusType.timeout',
        value: 'Timeout',
    },
]);

const getParams = () => {
    const params = {
        taskID: formData.taskID,
        creator: formData.creator,
        executeApplicationName: formData.executeApplicationName === 'all' ? '' : formData.executeApplicationName,
        status: formData.status,
        startDate: formData.timeRange?.[0],
        endDate: formData.timeRange?.[1],
        proxyUser: formData.proxyUser,
        isAdminView: formData.isAdminView,
        pageNow: props?.currentPage,
        pageSize: props?.pageSize,
        // instance: this.searchBar.instance?.replace(/ /g, '') || '',
    };
    return params;
};

const search = async () => {
    const params = getParams();
    emit('search', params);
};

const reset = () => {
    formData.taskID = '';
    formData.status = '';
    formData.executeApplicationName = '';
    formData.timeRange = [new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()];
    formData.creator = '';
    formData.proxyUser = '';
    search();
};

const switchAdmin = () => {
    if (isAdminView.value) {
        formData.taskID = '';
        formData.proxyUser = '';
        storage.set('isAdminView', 'false')
    } else {
        storage.set('isAdminView', 'true')
    }
    isAdminView.value = !isAdminView.value;
    formData.isAdminView = isAdminView.value;
    search();
}

const confirmStop = () => {
    // instance, taskID, strongerExecId
    const selected = props.checkedRows;
    if (!selected.length) {
        FMessage.warning(t('message.linkis.unselect'));
        return;
    }
    const inst: Record<string, any> = {};
    selected.forEach((it: CheckedRow) => {
        if (inst[it.instance]) {
            inst[it.instance].taskIDList.push(it.taskID);
            inst[it.instance].idList.push(it.strongerExecId);
        } else {
            inst[it.instance] = {
                taskIDList: [it.taskID],
                idList: [it.strongerExecId],
            };
        }
    });
    const p: any[] = [];
    Object.keys(inst).forEach((instkey) => {
        if (instkey)
            p.push(
                api.fetch(
                    `/entrance/${inst[instkey].idList[0]}/killJobs`,
                    {
                        idList: inst[instkey].idList,
                        taskIDList: inst[instkey].taskIDList,
                    },
                    'post',
                ),
            );
    });
    Promise.all(p).then(() => {
        FMessage.success(t('message.common.notice.kill.desc'));
        search();
    }).finally(() => {
        props.tableRef.clearSelection();
    });
};

onMounted(() => {
    api.fetch('/configuration/engineType', 'get').then(
        (res: { engineType: Array<string> }) => {
            // engine config selector list
            engineList.value = ['all', ...res.engineType].map((item) => ({
                label: item,
                value: item,
            }));
        },
    );
    search();
});

defineExpose({
    isCheckingTaskToStop,
    handleSearch: search,
    formData,
});
</script>

<style scoped src="./index.less"></style>
<style scoped lang="less">
.buttons {
    float: right;
    width: 360px;
    display: flex;
    margin: -8px 0 16px 0;
    gap: 16px;
}
.confirm-stop-button {
    margin: 0 12px 12px 0;
}
</style>
