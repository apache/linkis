<template>
    <div class="wrapper">
        <Count></Count>
        <!-- <div @click="test">test</div> -->
        <Filter ref="filterRef" :checkedKeys="checkedKeys" :search="search"></Filter>
        <f-table :data="currentData" class="table" v-model:checkedKeys="checkedKeys" :height="450" :rowKey="(row: Record<string, number | string>) => {
            return row.taskID;
        }
            ">
            <f-table-column v-if="filterRef?.isCheckingTaskToStop" type="selection" :width="32"
                fixed="left"></f-table-column>
            <template v-for="col in tableColumns" :key="col.label">
                <f-table-column v-if="col.formatter" :prop="col.prop" :label="col.label" :action="col.action"
                    :formatter="col.formatter">
                </f-table-column>
                <f-table-column v-else :prop="col.prop" :label="col.label" :action="col.action">
                </f-table-column>
            </template>
        </f-table>
        <FPagination class="pagination" show-size-changer :pageSizeOption="pageSizeOption" :total-count="data.length"
            @change="handleChange" v-model:pageSize="pageSize" v-model:currentPage="currentPage"></FPagination>
        <Drawer ref="drawerRef" :isShow="isShowDrawer" @closeDrawer="setIsShowDrawer"></Drawer>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, h, Ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import Drawer from './drawer.vue';
import Filter from './filter.vue';
import Count from './count.vue';
import TooltipText from './tooltipText.vue';
import api from '@/service/api';
import { FMessage } from '@fesjs/fes-design';

const filterRef = ref<Ref<{ isCheckingTaskToStop: boolean }> | null>(null);

const drawerRef = ref<Ref<{
    // eslint-disable-next-line no-unused-vars
    open: (rawData: Record<string, string | number>) => void;
}> | null>(null);

const isShowDrawer = ref(false);
const checkedKeys = reactive<Array<string>>([]);
const pageSizeOption = reactive([10, 20, 30, 50, 100]);
const { t } = useI18n();

// TODO: subsequent adjustments to standard enumeration values
const statusEnum: Record<string | number, Record<string, string>> = {
    1: {
        text: t('message.linkis.statusType.inited'),
        color: '#F29360',
    },
    2: {
        text: t('message.linkis.statusType.running'),
        color: '#F29360',
    },
    3: {
        text: t('message.linkis.statusType.succeed'),
        color: '#00CB91',
    },
    4: {
        text: t('message.linkis.statusType.failed'),
        color: '#F75F56',
    },
    5: {
        text: t('message.linkis.statusType.cancelled'),
        color: '#93949B',
    },
    6: {
        text: t('message.linkis.statusType.scheduled'),
        color: '#F29360',
    },
    7: {
        text: t('message.linkis.statusType.timeout'),
        color: '#F75F56',
    },
};

const setIsShowDrawer = (flag: boolean) => {
    isShowDrawer.value = flag;
};

const tableColumns = [
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
            const attr = statusEnum[row.status];
            return h('div', { style: `color:${attr.color}` }, attr.text);
        },
    },
    {
        prop: 'costTime',
        label: t('message.linkis.tableColumns.costTime'),
    },
    {
        prop: 'query',
        label: t('message.linkis.tableColumns.executionCode'),
        formatter: ({ row }: { row: Record<string, number | string> }) =>
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            h(TooltipText, {
                text: row.query,
                tipTitle: '这是标题',
                tipContent: '这是内容',
            }),
    },
    {
        label: t('message.linkis.tableColumns.failedReason'),
        prop: 'query',
        formatter: ({ row }: { row: Record<string, number | string> }) =>
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            h(TooltipText, {
                text: row.query,
                textStyle: { color: '#5384FF' },
                tipTitle: '这是标题',
                tipContent: '这是内容',
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

// TODO: replace mock data with the real
const data = reactive([
    {
        taskID: '1',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        keyInfo: '1',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '2',
        taskName: '任务',
        status: '3',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '3',
        taskName: '任务',
        status: '4',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '4',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '5',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '6',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '7',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '8',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '9',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '10',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
    {
        taskID: '11',
        taskName: '任务',
        status: '1',
        costTime: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        createdTime: '1204',
    },
]);

// 需要展示的数据
const currentData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);

const handleChange = (currenPage: number, size: number) => {
    const temp = data.slice((currenPage - 1) * size, currenPage * size);
    (currentData.value as typeof temp) = reactive([...temp]);
};
handleChange(currentPage.value, pageSize.value);

// TODO: 
const getParams = () => {
    console.log(1);
    return {};
};

const search = () => {
    //   this.isLoading = true
    const params = getParams();
    //   this.column = this.getColumns()
    api.fetch('/jobhistory/list', params, 'get').then((rst: unknown) => {
        console.log('rst', rst);
        //   this.pageSetting.total = rst.totalPage
        //   this.isLoading = false
        //   this.list = this.getList(rst.tasks)
    }).catch(() => {
        FMessage.error('Something Wrong!');
    });
};


onMounted(() => {
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then(() => {
        // this.isLogAdmin = res.admin;
        // this.isHistoryAdmin = res.historyAdmin;
    });
});
</script>

<style scoped src="./index.less"></style>
<style scoped>
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

    .tooltip-text {
        background-color: red;
    }
}
</style>
