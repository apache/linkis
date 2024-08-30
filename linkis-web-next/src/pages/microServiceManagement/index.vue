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
    <FForm layout="inline" :span="8">
        <FFormItem
            :label="t('message.linkis.tableColumns.instanceName')"
            :props="t('message.linkis.tableColumns.instanceName')"
        >
            <FInput
                v-model="instanceName"
                :placeholder="t('message.linkis.datasource.pleaseInput')"
            />
        </FFormItem>
        <FFormItem
            :label="t('message.linkis.tableColumns.engineType')"
            :props="t('message.linkis.tableColumns.engineType')"
        >
            <FSelect
                v-model="engineType"
                clearable
                :placeholder="t('message.linkis.unselect')"
            >
                <FOption
                    v-for="(item, index) in engineTypes"
                    :key="index"
                    :value="item"
                    :label="item"
                />
            </FSelect>
        </FFormItem>
        <FFormItem>
            <div class="btns">
                <FButton type="primary" @click="search">
                    {{ t('message.linkis.find') }}
                </FButton>
                <FButton type="primary" @click="handleJump">
                    {{ t('message.linkis.serviceRegistryCenter') }}
                </FButton>
            </div>
        </FFormItem>
    </FForm>
    <f-table
        :data="dataList"
        class="table"
        :rowKey="(row: Record<string, number | string>) => row.instance"
        :emptyText="t('message.linkis.noDataText')"
    >
        <template v-for="col in tableColumns" :key="col.label">
            <f-table-column
                v-if="col.formatter"
                :prop="col.prop"
                :label="col.label"
                :formatter="col.formatter"
            />
            <f-table-column
                v-else
                :prop="col.prop"
                :label="col.label"
                :action="col.action"
            />
        </template>
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
    <Modal ref="modalRef" :instance="currentIns" :handleInitialization="handleInitialization" />
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { FSpace, FTag } from '@fesjs/fes-design';
import dayjs from 'dayjs';
import { onMounted, reactive, ref, h } from 'vue';
import Modal from './modal.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const instanceName = ref('');
const engineType = ref('');
const engineTypes = reactive<{ label: string, value: string }[]>([]);
const dataList = ref();
const tableData = ref();
const allInstance = ref();
const currentPage = ref(1);
const pageSize = ref(10);
const modalRef = ref();
const currentIns = ref();

const handleEdit = (instance: any) => {
    currentIns.value = instance;
    modalRef.value?.open();
}

const tableColumns = [
    {
        prop: 'instance',
        label: t('message.linkis.tableColumns.instanceName'),
    },
    {
        prop: 'labels',
        label: t('message.linkis.tableColumns.label'),
        formatter: (data: { row: { labels: { stringValue: string }[] }}) => 
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            h(FSpace, data.row.labels.map(label => h(FTag, { type: 'default', effect: 'plain', size: 'small' }, label.stringValue)))
    },
    {
        prop: 'applicationName',
        label: t('message.linkis.tableColumns.serveType'),
    },
    {
        prop: 'createTime',
        label: t('message.linkis.tableColumns.createdTime'),
        formatter: ({ row }: { row: { createTime: number } }) => dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
        prop: 'updateTime',
        label: t('message.linkis.tableColumns.updateTime'),
        formatter: ({ row }: { row: { updateTime: number } }) => dayjs(row.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
        // 在“操作”两个字前添加空格，使其能与“编辑”上下对齐
        label: `\u00A0\u00A0\u00A0\u00A0${t('message.linkis.columns.control.title')}`,
        action: [{
            label: t('message.linkis.tableColumns.editLabel'),
            func: handleEdit,
        }],
    },
];

const search = () => {
    if (instanceName.value || engineType.value) {
        tableData.value = allInstance.value.filter((item: any) => 
            item.instance.match(instanceName.value) && item.applicationName.match(engineType.value)
        );
    } else {
        tableData.value = [...allInstance.value];
    }
    currentPage.value = 1;
    handleChangePagination(currentPage.value, pageSize.value);
}

const handleJump = () => {
    api.fetch("/microservice/serviceRegistryURL", "get").then((rst: any) => {
        window.open(rst.url, "_blank");
    });
}

// eslint-disable-next-line no-shadow
const handleChangePagination = (currentPage: number, pageSize: number) => {
    const temp = tableData.value.slice(
        (currentPage - 1) * pageSize,
        currentPage * pageSize,
    );
    dataList.value = [...temp] as any;
};

const handleInitialization = () => {
    api.fetch("/microservice/allInstance", "get").then((res: any) => {
        allInstance.value = [...res.instances] || [];
        tableData.value = [...res.instances] || [];
        handleChangePagination(currentPage.value, pageSize.value);
        res.instances.forEach((item: any) => {
            if (engineTypes.indexOf(item.applicationName) === -1) {
                engineTypes.push(item.applicationName);
            }
        })
    })
}

onMounted(() => {
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