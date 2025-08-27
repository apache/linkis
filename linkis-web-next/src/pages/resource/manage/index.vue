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
    <FForm layout="inline" :span="4" v-if="isHistoryAdmin && isLogAdmin">
        <FFormItem
            :label="t('message.linkis.userName')"
            :props="t('message.linkis.userName')"
        >
            <FInput
                v-model="userName"
                :placeholder="t('message.linkis.datasource.pleaseInput')"
            />
        </FFormItem>
        <FFormItem
            :label="t('message.linkis.formItems.appType')"
            :props="t('message.linkis.formItems.appType')"
        >
            <FInput
                v-model="creator"
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
                <FButton type="primary" @click="() => { search() }">
                    {{ t('message.linkis.find') }}
                </FButton>
                <FButton type="danger" @click="handleReset">
                    {{ t('message.linkis.resetAll') }}
                </FButton>
            </div>
        </FFormItem>
    </FForm>
    <f-table
        :data="dataList"
        class="table"
        :rowKey="(row: Record<string, number | string>) => row.id"
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
        <f-table-column
            v-if="isHistoryAdmin && isLogAdmin"
            :label="`\u00A0\u00A0\u00A0\u00A0${t('message.linkis.columns.control.title')}`"
            :action="[{
                label: t('message.linkis.reset'),
                func: (resource: any) => {
                  handleReset(resource.id);
                },
            }]"
        />
    </f-table>
    <FPagination
        class="pagination"
        show-size-changer
        show-total
        :total-count="totalSize"
        v-model:currentPage="currentPage"
        @change="handleChangePagination"
        :pageSizeOption="[10, 20, 30, 50, 100]"
        v-model:pageSize="pageSize"
    />
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { FMessage, FModal } from '@fesjs/fes-design';
import { isNumber } from 'lodash';
import { onMounted, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const isHistoryAdmin = ref(false);
const isLogAdmin = ref(false);
const userName = ref('');
const creator = ref('');
const engineType = ref('');
const engineTypes = reactive<string[]>([]);
const dataList = ref();
const currentPage = ref(1);
const pageSize = ref(10);
const totalSize = ref(0);

const handleReset = (resourceId: number) => {
  FModal.confirm({
    title: t('message.linkis.reset'),
    content: isNumber(resourceId) ? t('message.linkis.resetTip'): t('message.linkis.resetAllTip'),
    onOk: () => {
      const param = isNumber(resourceId) ? { resourceId } : { }
      api.fetch('linkisManager/rm/resetResource', param, 'delete').then(() => {
        FMessage.success(t('message.linkis.udf.success'));
        search();
      }).catch((err: any) => {
        window.console.error(err);
      })
    }
  })
}

const tableColumns = reactive<{ prop?: string, label: string, action?: any, formatter?: any }[]>([
    {
        prop: 'username',
        label: t('message.linkis.userName'),
    },
    {
        prop: 'creator',
        label: t('message.linkis.tableColumns.appType'),
    },
    {
        prop: 'engineTypeWithVersion',
        label: t('message.linkis.tableColumns.engineType'),
    },
    {
        prop: 'usedResource',
        label: t('message.linkis.tableColumns.engineUsed'),
        formatter: ({ row }: { row: { usedResource: { cores: number, memory: number, instance: number }}}) =>
            `${row.usedResource.cores}cores,${row.usedResource.memory / 1024 / 1024 / 1024}G,${row.usedResource.instance}apps`,
    },
    {
        prop: 'maxResource',
        label: t('message.linkis.tableColumns.engineTop'),
        formatter: ({ row }: { row: { maxResource: { cores: number, memory: number, instance: number }}}) =>
            `${row.maxResource.cores}cores,${row.maxResource.memory / 1024 / 1024 / 1024}G,${row.maxResource.instance}apps`,
    },
    {
        prop: 'leftResource',
        label: t('message.linkis.tableColumns.engineRemain'),
        formatter: ({ row }: { row: { leftResource: { cores: number, memory: number, instance: number }}}) =>
            `${row.leftResource.cores}cores,${row.leftResource.memory / 1024 / 1024 / 1024}G,${row.leftResource.instance}apps`,
    },
    {
        prop: 'yarnUsedResource',
        label: t('message.linkis.tableColumns.queueUsed'),
        formatter: ({ row }: { row: { yarnUsedResource?: { cores: number, memory: number, instance: number }}}) => `
            ${row.yarnUsedResource?.cores || 0}cores,
            ${(row.yarnUsedResource?.memory || 0) / 1024 / 1024 / 1024}G,
            ${row.yarnUsedResource?.instance || 0}apps
        `,
    },
    {
        prop: 'yarnMaxResource',
        label: t('message.linkis.tableColumns.queueTop'),
        formatter: ({ row }: { row: { yarnMaxResource?: { cores: number, memory: number, instance: number }}}) => `
            ${row.yarnMaxResource?.cores || 0}cores,
            ${(row.yarnMaxResource?.memory || 0) / 1024 / 1024 / 1024}G,
            ${row.yarnMaxResource?.instance || 0}apps
        `,
    },
    {
        prop: 'yarnLeftResource',
        label: t('message.linkis.tableColumns.queenRemain'),
        formatter: ({ row }: { row: { yarnLeftResource?: { cores: number, memory: number, instance: number }}}) => `
            ${row.yarnLeftResource?.cores || 0}cores,
            ${(row.yarnLeftResource?.memory || 0) / 1024 / 1024 / 1024}G,
            ${row.yarnLeftResource?.instance || 0}apps
        `,
    },
])

const search = (currentPage = 1) => {
    api.fetch('/linkisManager/rm/allUserResource', {
        username: userName.value,
        creator: creator.value,
        engineType: engineType.value,
        pageNow: currentPage,
        pageSize: pageSize.value,
    }, 'get').then((res: any) => {
        dataList.value = [...res.resources] || [];
        totalSize.value = res.total;
    })
}

const handleChangePagination = () => {
    search(currentPage.value)
}

const handleInitialization = () => {
    engineTypes.length = 0;
    api.fetch('/linkisManager/rm/engineType', 'get').then((res: any) => {
        res.engineType.forEach((engineType: string) => {
            engineTypes.push(engineType);
        })
    })
    search();
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