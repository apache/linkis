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
    <div class="filter">
        <FForm ref="formRef" class="form" v-show="!isCheckingTaskToStop">
            <FFormItem
                :label="$t('message.linkis.jobId')"
                :props="$t('message.linkis.jobId')"
            >
                <FInput
                    v-model="formData.id"
                    placeholder="请输入"
                    class="short-form-item"
                ></FInput>
            </FFormItem>

            <FFormItem
                :label="$t('message.linkis.codeQuery.status')"
                :props="$t('message.linkis.codeQuery.status')"
            >
                <FSelect
                    v-model="formData.status"
                    clearable
                    placeholder="请选择"
                    class="short-form-item"
                >
                    <FOption
                        v-for="(item, index) in statusList"
                        :key="index"
                        :value="item.value"
                        :label="$t(item.label)"
                    >
                    </FOption>
                </FSelect>
            </FFormItem>

            <FFormItem
                :label="$t('message.common.resourceSimple.YQ')"
                :props="$t('message.common.resourceSimple.YQ')"
            >
                <FSelect
                    v-model="formData.engine"
                    clearable
                    placeholder="请选择"
                    class="short-form-item"
                >
                    <FOption
                        v-for="(item, index) in engineList"
                        :key="index"
                        :value="item.value"
                        :label="$t(item.label)"
                    >
                    </FOption>
                </FSelect>
            </FFormItem>

            <FFormItem
                :label="$t('message.linkis.codeQuery.createdTime')"
                :prop="$t('message.linkis.codeQuery.createdTime')"
                class="long-form-item"
            >
                <FDatePicker
                    v-model="formData.dateRange"
                    type="daterange"
                    :modelValue="[
                        Date.now(),
                        Date.now() + 7 * 24 * 60 * 60 * 1000,
                    ]"
                >
                    <template #separator> - </template>
                </FDatePicker>
            </FFormItem>

            <!-- TODO:  -->
            <!-- <FFormItem
                :label="$t('message.linkis.datasource.creator')"
                :props="$t('message.linkis.datasource.creator')"
            >
                <FSelect
                    v-model="formData.creator"
                    clearable
                    placeholder="请选择"
                    class="short-form-item"
                >
                    <FOption
                        v-for="(item, index) in optionList"
                        :key="index"
                        :value="item.value"
                        :label="$t(item.label)"
                    >
                    </FOption>
                </FSelect>
            </FFormItem> -->

            <div class="buttons">
                <template v-if="!isCheckingTaskToStop">
                    <FButton type="primary" @click="search">
                        {{ $t('message.linkis.find') }}
                    </FButton>
                    <FButton @click="reset">
                        {{ $t('message.linkis.EnginePluginManagement.Reset') }}
                    </FButton>
                    <FButton
                        class="stop-button"
                        @click="isCheckingTaskToStop = true"
                    >
                        {{ $t('message.linkis.batchStopping') }}
                    </FButton>
                </template>
            </div>
        </FForm>
        <template v-if="isCheckingTaskToStop">
            <div class="buttons">
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

const { t } = useI18n();

const emit = defineEmits(['search']);
const props = defineProps<{
    pageSize: number;
    currentPage: number;
    checkedRows: CheckedRows;
}>();

const isCheckingTaskToStop = ref(false);
const formRef = ref<null | typeof FForm>(null);

const formData = reactive({
    id: '',
    status: '',
    engine: '',
    dateRange: '',
    creator: '',
});

const engineList = ref<Array<{ label: string; value: string }>>([]);

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
        taskID: formData.id,
        creator: formData.creator,
        executeApplicationName: formData.engine,
        status: formData.status,
        startDate: formData.dateRange?.[0],
        endDate: formData.dateRange?.[1],
        pageNow: props?.currentPage,
        pageSize: props?.pageSize,
        // proxyUser: this.searchBar.proxyUser,
        // isAdminView: this.isAdminModel,
        // instance: this.searchBar.instance?.replace(/ /g, '') || '',
    };
    return params;
};

const search = async () => {
    const params = getParams();
    emit('search', params);
};

const reset = () => {
    formData.id = '';
    formData.status = '';
    formData.engine = '';
    formData.dateRange = '';
    formData.creator = '';

    search();
};

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
    });
};

onMounted(() => {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    api.fetch('/configuration/engineType', 'get').then(
        (res: { engineType: Array<string> }) => {
            // engine config selector list
            engineList.value = ['all', ...res.engineType].map((item) => ({
                label: item,
                value: item,
            }));
        },
    );
});

defineExpose({
    isCheckingTaskToStop,
    handleSearch: search,
});
</script>

<style scoped src="./index.less"></style>
<style scoped>
.filter {
    position: relative;
    height: 54px;

    .form {
        display: flex;
        gap: 22px;

        .short-form-item {
            width: 100px;
        }

        .long-form-item {
            width: 300px;
        }
    }

    .buttons {
        margin-left: 10px;
        flex: 1;
        display: flex;
        justify-content: flex-start;
        gap: 16px;

        .stop-button {
            margin-left: auto;
            text-align: center;
        }
    }
    :deep(button) {
        padding-left: 16px;
    }
}
</style>
