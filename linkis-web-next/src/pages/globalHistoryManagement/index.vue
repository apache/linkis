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
    <div>
        <Count
            @find="find"
            :shortcuts="shortcuts"
            :filterRef="filterRef"
        />
        <Filter
            ref="filterRef"
            :tableRef="tableRef"
            @search="search"
            :checkedRows="checkedRows"
            :currentPage="currentPage"
            :pageSize="pageSize"
            :isLogAdmin="isLogAdmin"
            :isHistoryAdmin="isHistoryAdmin"
            :shortcuts="shortcuts"
        />
        <f-table
            ref="tableRef"
            @selectionChange="handleSelectionChange"
            :data="dataList"
            :height="500"
            :rowKey="(row: Record<string, number | string>) => row"
            :emptyText="t('message.linkis.noDataText')"
        >
            <f-table-column
                v-if="filterRef?.isCheckingTaskToStop"
                type="selection"
                :width="32"
                fixed="left"
            />
            <template v-for="col in columns" :key="col.label">
                <f-table-column
                    v-if="col.formatter"
                    :prop="col.prop"
                    :label="col.label"
                    :action="col.action"
                    :formatter="col.formatter"
                    :visible="col.visible"
                />
                <f-table-column
                    v-else
                    :prop="col.prop"
                    :label="col.label"
                    :action="col.action"
                    :visible="col.visible"
                />
            </template>
        </f-table>
        <FPagination
            class="pagination"
            show-size-changer
            showTotal
            :pageSizeOption="[10, 20, 30, 50, 100]"
            :total-count="listTotalLen"
            @change="handlePageChange"
            v-model:pageSize="pageSize"
            v-model:currentPage="currentPage"
        />
        <Drawer ref="drawerRef" />
    </div>
</template>

<script setup lang="ts">
import { ref, h, Ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { FMessage } from '@fesjs/fes-design';
import dayjs from 'dayjs';
import Drawer from './drawer/index.vue';
import Filter from './filter.vue';
import Count from './count.vue';
import TooltipText from './tooltipText.vue';
import api from '@/service/api';
import { CheckedRows } from './type';
import storage from '@/helper/storage';

const filterRef = ref<Ref<{
    isCheckingTaskToStop: boolean;
    handleSearch: () => void;
}> | null>(null);
const tableRef = ref();
const drawerRef = ref<Ref<{
    // eslint-disable-next-line no-unused-vars
    open: (rawData: Record<string, string | number>) => void;
}> | null>(null);
const currentPage = ref(1);
const pageSize = ref(20);
const checkedRows = ref<CheckedRows>([]);
const listTotalLen = ref<number>(0);
const columns: Ref<Array<Record<string, any>>> = ref([]);
const dataList = ref([]);
const isLogAdmin = ref<boolean>(false);
const isHistoryAdmin = ref<boolean>(false);
const isAdminView = ref<boolean>(storage.get('isAdminView') === 'true');
const { t } = useI18n();

const shortcuts = {
    [t('message.linkis.shortcuts.today')] : () => {
        // 获取今天的开始时间和结束时间
        const today = new Date();
        const todayStartTime = new Date(
            today.getFullYear(),
            today.getMonth(),
            today.getDate(),
            0,
            0,
            0
        );
        const todayEndTime = new Date(
            today.getFullYear(),
            today.getMonth(),
            today.getDate(),
            23,
            59,
            59
        );
        return [todayStartTime.getTime(), todayEndTime.getTime()];
    },
    [t('message.linkis.shortcuts.week')] : () => [new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()],
    [t('message.linkis.shortcuts.month')] : () => [new Date().getTime() - 3600 * 1000 * 24 * 30, new Date().getTime()],
}

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

const getFailedReason = (item:any) => {
    return item.errCode && item.errDesc
    ? item.errCode + item.errDesc
    : item.errCode || item.errDesc || ''
}

const getColumns = () => {
    const columns = [
        {
            prop: 'taskID',
            label: t('message.linkis.columns.taskID'),
        },
        {
            prop: 'execId',
            label: t('message.linkis.columns.execId'),
            formatter: (data: {
                row: { execId: string; sourceInfo: Record<string, string> };
            }) => {
                const { execId, sourceInfo } = data.row;
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                return h(TooltipText, {
                    text: execId,
                    tipTitle: t('message.linkis.sourceInfo'),
                    tipHTMLContent: `
                        <div style="display: flex; font-size: 16px; line-height: 24px;">
                            <div style="color: #63656F; margin-right: 16px;">
                                <div>${t('message.linkis.task.projectName')}</div>
                                <div>${t('message.linkis.task.workflowName')}</div>
                                <div>${t('message.linkis.task.workflowIp')}</div>
                            </div>
                            <div style="color: #0F1222;">
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
            prop: 'status',
            label: t('message.linkis.columns.status'),
            formatter: ({ row }: { row: Record<string, number | string> }) => {
                const attr =
                    statusEnum[`${row.status}`.toLowerCase()] ??
                    statusEnum.unknown;
                return h('div', { style: `color:${attr.color}` }, attr.text);
            },
        },
        {
            prop: 'costTime',
            label: t('message.linkis.columns.costTime'),
            formatter: (data: {
                row: { costTime: number };
            }) => {
                const { costTime } = data.row;
                let formattedTime;
                if (costTime > 60000) {
                    formattedTime = `m${t('message.common.time.MIN')}s${t(
                        'message.common.time.SEC',
                    )}`;
                } else {
                    formattedTime = `s${t('message.common.time.SEC')}`;
                }
                return h('div', dayjs(costTime).format(formattedTime));
            },
        },
        {
            prop: 'executionCode',
            label: t('message.linkis.columns.executionCode'),
            formatter: ({ row }: { row: Record<string, number | string> }) =>
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                h(TooltipText, {
                    text: `{id：${row.taskID}}`,
                    tipTitle: row.execId,
                    tipContent: row.executionCode,
                }),
        },
        {
            label: t('message.linkis.columns.failedReason'),
            prop: 'failedReason',
            formatter: ({ row }: { row: Record<string, number | string> }) =>
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                h(TooltipText, {
                    text: t('message.linkis.codeQuery.check'),
                    textStyle: { color: '#5384FF' },
                    tipTitle: t('message.linkis.columns.failedReason'),
                    tipContent: row.failedReason || 'null',
                }),
        },
        {
            prop: 'requestApplicationName',
            label: t('message.linkis.columns.requestApplicationName'),
        },
        {
            prop: 'executeApplicationName',
            label: t('message.linkis.columns.executeApplicationName'),
        },
        {
            prop: 'umUser',
            label: t('message.linkis.columns.umUser'),
            visible: isAdminView.value,
        },
        {
            prop: 'createdTime',
            label: t('message.linkis.columns.createdTime'),
            formatter: ( { row }: { row: { createdTime: number } }) =>
                dayjs(row.createdTime).format('YYYY-MM-DD HH:mm:ss'),
        },
        {
            // 在“操作”两个字前添加空格，使其能与“查看”上下对齐
            label: `\u00A0\u00A0\u00A0\u00A0${t('message.linkis.columns.control.title')}`,
            action: [
                {
                    label: t('message.linkis.columns.control.label'),
                    func: (rawData: Record<string, string | number>) => {
                        drawerRef.value?.open?.(rawData);
                    },
                },
            ],
        },
    ];
    return columns;
}

// eslint-disable-next-line consistent-return
const find = async (
    params: Record<string, string | number>,
    callback?: (v: any) => void,
) => {
    try {
        const rst = await api.fetch('/jobhistory/list', params, 'get');
        const res = rst.tasks?.map((task: any) => ({
            taskID: task.taskID ?? '',
            execId: task.execId ?? '',
            status: task.status ?? '',
            costTime: task.costTime ?? '',
            executionCode: task.executionCode ?? '',
            failedReason: getFailedReason(task),
            requestApplicationName: task.requestApplicationName ?? '',
            executeApplicationName: task.executeApplicationName ?? '',
            umUser: task.umUser ?? '',
            createdTime: task.createdTime ?? '',
            sourceInfo: {
                ...JSON.parse(task.sourceJson ?? ''),
                projectName: task.sourceTailor,
            },
            strongerExecId: task.strongerExecId,
            instance: task.instance,
        }));
        // 存在callback参数时，表示是Count组件调用的该方法，不需要更新表格的总条数；
        // 不存在callback参数时，表示是Filter组件调用了search方法进而调用了该方法，此时需要更新表格的总条数；
        if (callback) {
            callback(rst);
        } else {
            listTotalLen.value = rst.totalPage;
        }
        return res;
    } catch (err) {
        window.console.error(err);
    }
};

const search = async (params: Record<string, string | number>) => {
    isAdminView.value = storage.get('isAdminView') === 'true';
    columns.value = getColumns();
    const res = await find(params);
    dataList.value = res;
};

const handlePageChange = () => {
    filterRef.value?.handleSearch();
};

const handleSelectionChange = (selection: Array<Record<string, string | number>>) => {
    checkedRows.value = selection.map((row) => ({
        taskID: row.taskID,
        strongerExecId: row.strongerExecId,
        instance: row.instance,
    }));
};

onMounted(() => {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then(
        (res: { admin: boolean, historyAdmin: boolean }) => {
        isLogAdmin.value = res.admin;
        isHistoryAdmin.value = res.historyAdmin;
    });
});
</script>
<style lang="less" scoped>
.pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
}
</style>
