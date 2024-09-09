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
    <FForm layout="inline" :span="4">
        <FFormItem
            :label="t('message.linkis.tableColumns.instanceName')"
            :props="t('message.linkis.tableColumns.instanceName')"
        >
            <FInput
                v-model="instance"
                :placeholder="t('message.linkis.datasource.pleaseInput')"
            />
        </FFormItem>
        <FFormItem
            :label="t('message.linkis.formItems.status.label')"
            :props="t('message.linkis.formItems.status.label')"
        >
            <FSelect
                v-model="nodeHealthy"
                clearable
                :placeholder="t('message.linkis.unselect')"
            >
                <FOption
                    v-for="(item, index) in nodeHealthyList"
                    :key="index"
                    :value="item"
                    :label="item"
                />
            </FSelect>
        </FFormItem>
        <FFormItem
            :label="t('message.linkis.initiator')"
            :props="t('message.linkis.initiator')"
        >
            <FSelect
                v-model="owner"
                clearable
                :placeholder="t('message.linkis.unselect')"
            >
                <FOption
                    v-for="(item, index) in ownerList"
                    :key="index"
                    :value="item"
                    :label="item"
                />
            </FSelect>
        </FFormItem>
        <FFormItem
            :label="t('message.linkis.tenant')"
            :props="t('message.linkis.tenant')"
        >
            <FInput
                v-model="tenantLabel"
                :placeholder="t('message.linkis.datasource.pleaseInput')"
            />
        </FFormItem>
        <FFormItem>
            <div class="btns">
                <FButton type="primary" @click="search">
                    {{ t('message.linkis.find') }}
                </FButton>
            </div>
        </FFormItem>
    </FForm>
    <f-table
        :data="dataList"
        :rowKey="(row: Record<string, number | string>) => row.instance"
        :emptyText="t('message.linkis.noDataText')"
    >
        <template v-for="col in tableColumns" :key="col.label">
            <f-table-column
                v-if="col.formatter"
                :prop="col.prop"
                :label="col.label"
                :formatter="col.formatter"
                :width="col.width"
            />
            <f-table-column
                v-else
                :prop="col.prop"
                :label="col.label"
                :width="col.width"
            />
        </template>
        <f-table-column
            :label="`\u00A0\u00A0\u00A0\u00A0${t('message.linkis.columns.control.title')}`"
            :action="[{
                label: t('message.linkis.tagEdit'),
                func: handleEdit,
            }].concat(isHistoryAdmin && isLogAdmin ? [{
                label: t('message.linkis.killAll'),
                func: handleKillAll,
            }] : [])"
            :width="150"
        />
    </f-table>
    <FPagination
        class="pagination"
        show-size-changer
        show-total
        :total-count="tableData?.length || 0"
        v-model:currentPage="currentPage"
        @change="handleChangePagination"
        :pageSizeOption="[10, 20, 30, 50, 100]"
        v-model:pageSize="pageSize"
    />
    <Modal ref="modalRef" :instance="currentIns" :handleInitialization="handleInitialization" :nodeHealthyList="nodeHealthyList" />
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { FButton, FMessage, FModal, FSpace, FTag } from '@fesjs/fes-design';
import dayjs from 'dayjs';
import { onMounted, reactive, ref, h } from 'vue';
import Modal from './modal.vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

const { t } = useI18n();
const router = useRouter();

const isLogAdmin = ref(false);
const isHistoryAdmin = ref(false);
const instance = ref('');
const nodeHealthy = ref('');
const nodeHealthyList = ref();
const owner = ref('');
const ownerList = reactive<string[]>([]);
const tenantLabel = ref('');
const dataList = ref();
const tableData = ref();
const currentPage = ref(1);
const pageSize = ref(10);
const modalRef = ref();
const currentIns = ref();

const handleEdit = (instance: any) => {
    currentIns.value = instance;
    modalRef.value?.open();
}

const handleKillAll = (instance: any) => {
    FModal.confirm({
        title: t('message.linkis.killAll'),
        content: t('message.linkis.tipForKill', {instance: instance.instance}),
        onOk: async () => {
            try {
                const res = await api.fetch('/linkisManager/rm/killUnlockEngineByEM', { instance: instance.instance }, 'post');
                const { killEngineNum, memory, cores } = res.result;
                // 传回的是Byte
                FMessage.success(t('message.linkis.killFinishedInfo', { killEngineNum, memory: memory / 1024 / 1024 / 1024, cores }))
            } catch (err) {
                window.console.error(err);
            }
        }
    })
}

// const handleJump = (instance: any) => {
//     router.push(`/console/ECMManagement/engines/${instance}`)
// }

const tableColumns = [
    {
        prop: 'instance',
        label: t('message.linkis.tableColumns.instanceName'),
        // TODO: The ECM Engine Page
        // formatter: (data: { row: { instance: string }}) =>
        //     h(FButton, { type: 'link', onClick: () => { handleJump(data.row.instance) }, style: { padding: 0, margin: 0 }}, data.row.instance),
        width: 120,
    },
    {
        prop: 'nodeHealthy',
        label: t('message.linkis.tableColumns.status'),
        width: 100,
    },
    {
        prop: 'labels',
        label: t('message.linkis.tableColumns.label'),
        formatter: (data: { row: { labels: { stringValue: string }[] }}) => 
            h(FSpace, data.row.labels.map(label => h(FTag, { type: 'default', effect: 'plain', size: 'small' }, label.stringValue))),
        width: 270,
    },
    {
        prop: 'usedResource',
        label: t('message.linkis.tableColumns.usedResources'),
        formatter: ({ row }: { row: { usedResource: { cores: number, memory: number, instances: number }}}) =>
            `${row.usedResource.cores}cores,${row.usedResource.memory / 1024 / 1024 / 1024}G,${row.usedResource.instances}apps`,
    },
    {
        prop: 'maxResource',
        label: t('message.linkis.tableColumns.maximumAvailableResources'),
        formatter: ({ row }: { row: { maxResource: { cores: number, memory: number, instances: number }}}) =>
            `${row.maxResource.cores}cores,${row.maxResource.memory / 1024 / 1024 / 1024}G,${row.maxResource.instances}apps`,
        width: 120,
    },
    {
        prop: 'lockedResource',
        label: t('message.linkis.tableColumns.lockedResource'),
        formatter: ({ row }: { row: { lockedResource: { cores: number, memory: number, instances: number }}}) =>
            `${row.lockedResource.cores}cores,${row.lockedResource.memory / 1024 / 1024 / 1024}G,${row.lockedResource.instances}apps`,
    },
    {
        prop: 'owner',
        label: t('message.linkis.tableColumns.initiator'),
    },
    {
        prop: 'startTime',
        label: t('message.linkis.tableColumns.startTime'),
        formatter: ({ row }: { row: { startTime: number } }) => dayjs(row.startTime).format('YYYY-MM-DD HH:mm:ss'),
    },
];

const search = () => {
    currentPage.value = 1;
    api.fetch("/linkisManager/listAllEMs", {
        instance: instance.value.replace(/ /g, ''),
        nodeHealthy: nodeHealthy.value,
        owner: owner.value,
        tenantLabel: tenantLabel.value,
    }, "get").then((res: any) => {
        tableData.value = [...res.EMs] || [];
        handleChangePagination(1, pageSize.value);
    })
}

const handleChangePagination = (currentPage: number, pageSize: number) => {
    const temp = tableData.value.slice(
        (currentPage - 1) * pageSize,
        currentPage * pageSize,
    );
    dataList.value = [...temp] as any;
};

const handleInitialization = () => {
    currentPage.value = 1;
    ownerList.length = 0;
    api.fetch('/linkisManager/listAllECMHealthyStatus', { onlyEditable: true }, 'get').then((res: any) => {
        nodeHealthyList.value = res.nodeHealthy;
    })
    api.fetch("/linkisManager/listAllEMs", "get").then((res: any) => {
        tableData.value = [...res.EMs] || [];
        handleChangePagination(1, pageSize.value);
        res.EMs.forEach((item: any) => {
            if (ownerList.indexOf(item.owner) === -1) {
                ownerList.push(item.owner);
            }
        })
    })
}

onMounted(() => {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then(
        (res: { admin: boolean, historyAdmin: boolean }) => {
        isLogAdmin.value = res.admin;
        isHistoryAdmin.value = res.historyAdmin;
    });
    handleInitialization();
})
</script>

<style lang="less" scoped>
.btns {
    display: flex;
    gap: 12px;
}
.pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
}
</style>