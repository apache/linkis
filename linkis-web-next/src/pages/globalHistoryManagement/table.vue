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
    <div class="wrapper">
        <Count @find="find"></Count>
        <Filter
            ref="filterRef"
            @search="search"
            :checkedKeys="checkedKeys"
            :checkedRows="checkedRows"
            :currentPage="currentPage"
            :pageSize="pageSize"
        ></Filter>
        <f-table
            @select="handleSelection"
            :data="dataList"
            class="table"
            v-model:checkedKeys="checkedKeys"
            :height="450"
            :rowKey="(row: Record<string, number | string>) => row"
        >
            <f-table-column
                v-if="filterRef?.isCheckingTaskToStop"
                type="selection"
                :width="32"
                fixed="left"
            ></f-table-column>

            <template v-for="col in tableColumns" :key="col.label">
                <f-table-column
                    v-if="col.formatter"
                    :prop="col.prop"
                    :label="col.label"
                    :action="col.action"
                    :formatter="col.formatter"
                >
                </f-table-column>
                <f-table-column
                    v-else
                    :prop="col.prop"
                    :label="col.label"
                    :action="col.action"
                >
                </f-table-column>
            </template>
        </f-table>
        <FPagination
            class="pagination"
            show-size-changer
            :pageSizeOption="pageSizeOption"
            :total-count="listTotalLen"
            @change="handlePageChange"
            v-model:pageSize="pageSize"
            v-model:currentPage="currentPage"
        ></FPagination>
        <Drawer
            ref="drawerRef"
            :isShow="isShowDrawer"
            @closeDrawer="setIsShowDrawer"
        ></Drawer>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, h, Ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { FMessage } from '@fesjs/fes-design';
import dayjs from 'dayjs';
import Drawer from './drawer.vue';
import Filter from './filter.vue';
import Count from './count.vue';
import TooltipText from './tooltipText.vue';
import api from '@/service/api';
import { CheckedRows } from './type';

const filterRef = ref<Ref<{
    isCheckingTaskToStop: boolean;
    handleSearch: () => void;
}> | null>(null);
const drawerRef = ref<Ref<{
    // eslint-disable-next-line no-unused-vars
    open: (rawData: Record<string, string | number>) => void;
}> | null>(null);
const isLoading = ref<boolean>(false);
const currentPage = ref(1);
const pageSize = ref(10);
const isShowDrawer = ref(false);
const checkedRows = ref<CheckedRows>([]);
const checkedKeys = ref<Array<string>>([]);
const pageSizeOption = reactive([10, 20, 30, 50, 100]);
const listTotalLen = ref<number>(0);
const tableColumns: Ref<Array<Record<string, any>>> = ref([]);
const dataList = ref([]);
const { t } = useI18n();

const statusEnum: Record<string | number, Record<string, string>> = {
    inited: {
        text: t('message.linkis.statusType.inited'),
        color: '#F29360',
    },
    running: {
        text: t('message.linkis.statusType.running'),
        color: '#F29360',
    },
    succeed: {
        text: t('message.linkis.statusType.succeed'),
        color: '#00CB91',
    },
    failed: {
        text: t('message.linkis.statusType.failed'),
        color: '#F75F56',
    },
    cancelled: {
        text: t('message.linkis.statusType.cancelled'),
        color: '#93949B',
    },
    scheduled: {
        text: t('message.linkis.statusType.scheduled'),
        color: '#F29360',
    },
    timeout: {
        text: t('message.linkis.statusType.timeout'),
        color: '#F75F56',
    },
    unknown: {
        color: '#000',
        text: t('message.linkis.statusType.unknown'),
    },
};

const setIsShowDrawer = (flag: boolean) => {
    isShowDrawer.value = flag;
};

const getColumns = () => {
    const columns = [
        {
            prop: 'taskID',
            label: t('message.linkis.tableColumns.taskID'),
        },
        {
            prop: 'taskName',
            label: t('message.linkis.tableColumns.taskName'),
        },
        {
            prop: 'status',
            label: t('message.linkis.tableColumns.status'),
            formatter: ({ row }: { row: Record<string, number | string> }) => {
                const attr =
                    statusEnum[`${row.status}`.toLowerCase()] ??
                    statusEnum.unknown;
                return h('div', { style: `color:${attr.color}` }, attr.text);
            },
        },
        {
            prop: 'costTime',
            label: t('message.linkis.tableColumns.costTime'),
            formatter: (data: {
                row: { costTime: number; sourceInfo: Record<string, string> };
            }) => {
                const { costTime, sourceInfo } = data.row;
                let formattedTime;
                if (costTime > 60000) {
                    formattedTime = `m${t('message.common.time.MIN')}s${t(
                        'message.common.time.second',
                    )}`;
                } else {
                    formattedTime = `s${t('message.common.time.second')}`;
                }
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                return h(TooltipText, {
                    text: dayjs(costTime).format(formattedTime),
                    tipTitle: t('message.linkis.sourceInfo'),
                    tipHTMLContent: `
                      <div class="pairs">
                        <div class="left">
                          <div>${t('message.linkis.task.projectName')}</div>
                          <div>${t('message.linkis.task.workflowName')}</div>
                          <div>${t('message.linkis.task.workflowIp')}</div>
                        </div>
                        <div class="right">
                          <div>${sourceInfo.projectName}</div>
                          <div>${sourceInfo.scriptPath}</div>
                          <div>${sourceInfo.requestIP}</div>
                        </div>
                      </div>
                    `,
                });
            },
        },
        {
            prop: 'query',
            label: t('message.linkis.tableColumns.executionCode'),
            formatter: ({ row }: { row: Record<string, number | string> }) =>
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                h(TooltipText, {
                    text: `{id：${row.taskID}}`,
                    tipTitle: row.taskName,
                    tipContent: row.query,
                }),
        },
        {
            label: t('message.linkis.tableColumns.failedReason'),
            prop: 'query',
            formatter: ({ row }: { row: Record<string, number | string> }) =>
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                h(TooltipText, {
                    text: t('message.linkis.codeQuery.check'),
                    textStyle: { color: '#5384FF' },
                    tipTitle: t('message.linkis.tableColumns.failedReason'),
                    tipContent: row.errDesc || 'null',
                }),
        },
        {
            prop: 'application',
            label: t('message.linkis.tableColumns.application'),
        },
        {
            prop: 'engine',
            label: t('message.linkis.tableColumns.engine'),
        },
        {
            prop: 'creator',
            label: t('message.linkis.tableColumns.creator'),
        },
        {
            prop: 'createdTime',
            label: t('message.linkis.tableColumns.createdTime'),
            formatter: (row: { createdTime: number }) =>
                dayjs(row.createdTime).format('YYYY-MM-DD'),
        },
        {
            label: t('message.linkis.tableColumns.control.title'),
            action: [
                {
                    label: t('message.linkis.tableColumns.control.label'),
                    func: (rawData: Record<string, string | number>) => {
                        drawerRef.value?.open?.(rawData);
                        // setIsShowDrawer(true);
                    },
                },
            ],
        },
    ];
    // if (!isAdminModel) {
    //     const index = column.findIndex((item) => item.key === 'umUser');
    //     column.splice(index, 1);
    // }
    return columns;
};

// eslint-disable-next-line consistent-return
const find = async (
    params: Record<string, string | number>,
    callback?: (v: any) => void,
) => {
    isLoading.value = true;
    try {
        const rst = await api.fetch('/jobhistory/list', params, 'get');
        listTotalLen.value = rst.totalPage;

        const res = rst.tasks?.map((task: any) => ({
            taskID: task.taskID ?? '',
            taskName: task.execId ?? '',
            status: task.status ?? '',
            costTime: task.costTime ?? '',
            query: task.executionCode ?? '',
            application: task.executeApplicationName ?? '',
            engine: task.engineInstance ?? '',
            creator: task.umUser ?? '',
            createdTime: task.createdTime ?? '',
            sourceInfo: {
                ...JSON.parse(task.sourceJson ?? ''),
                projectName: task.sourceTailor,
            },
            errDesc: task.errDesc,
            strongerExecId: task.strongerExecId,
            instance: task.instance,
        }));

        isLoading.value = false;
        callback?.(rst);
        return res;
    } catch (err) {
        FMessage.error('Something Wrong!');
        isLoading.value = false;
    }
};

const search = async (params: Record<string, string | number>) => {
    isLoading.value = true;
    const res = await find(params);
    dataList.value = res;
};

const handlePageChange = () => {
    filterRef.value?.handleSearch();
};

const handleSelection = (selectedData: {
    selection: Array<Record<string, string | number>>;
}) => {
    checkedRows.value = selectedData.selection.map((row) => ({
        taskID: row.taskID,
        strongerExecId: row.strongerExecId,
        instance: row.instance,
    }));
};

onMounted(() => {
    // handlePageChange(currentPage.value, pageSize.value);
    tableColumns.value = getColumns();
    search({});
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then(() => {
        // this.isLogAdmin = res.admin;
        // this.isHistoryAdmin = res.historyAdmin;
    });
});
</script>
<style lang="less" scoped>
.wrapper {
    position: relative;
    height: 100vh;
    overflow: auto;
    .table {
        /* height: calc(100% - 350px); */
        /* overflow-y: auto; */
    }

    :deep(.fes-btn) {
        padding-left: 0;
        transform: translateX(-8px);
    }

    .pagination {
        /* position: absolute;
        bottom: 5%;
        right: 0%; */
        display: flex;
        justify-content: flex-end;
        margin-top: 16px;
    }
}
</style>
