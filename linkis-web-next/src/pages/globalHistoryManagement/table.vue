<template>
    <div class="wrapper">
        <Count></Count>
        <!-- <div @click="test">test</div> -->
        <Filter ref="filterRef" :checkedKeys="checkedKeys"></Filter>
        <f-table
            :data="currentData"
            class="table"
            v-model:checkedKeys="checkedKeys"
            :rowKey="
                    (row: Record<string, number|string>) => {
                        return row.taskId;
                    }
                "
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
            :total-count="data.length"
            @change="handleChange"
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
import { reactive, ref, h, Ref } from 'vue';
import Drawer from './drawer.vue';
import Filter from './filter.vue';
import Count from './count.vue';
import TooltipText from './tooltipText.vue';

const filterRef = ref<Ref<{ isCheckingTaskToStop: boolean }> | null>(null);

const drawerRef = ref<Ref<{
    // eslint-disable-next-line no-unused-vars
    open: (rawData: Record<string, string | number>) => void;
        }> | null>(null);

const isShowDrawer = ref(false);
const checkedKeys = reactive<Array<string>>([]);
const pageSizeOption = reactive([10, 20, 30, 50, 100]);

// TODO: subsequent adjustments to standard enumeration values
const statusEnum: Record<string | number, Record<string, string>> = {
    1: {
        text: '排队中',
        color: '#F29360',
    },
    2: {
        text: '运行中',
        color: '#F29360',
    },
    3: {
        text: '运行成功',
        color: '#00CB91',
    },
    4: {
        text: '失败',
        color: '#F75F56',
    },
    5: {
        text: '取消',
        color: '#93949B',
    },
    6: {
        text: '资源申请中',
        color: '#F29360',
    },
    7: {
        text: '超时',
        color: '#F75F56',
    },
};

const setIsShowDrawer = (flag: boolean) => {
    isShowDrawer.value = flag;
};

const tableColumns = [
    {
        prop: 'taskId',
        label: '任务ID',
    },
    {
        prop: 'taskName',
        label: '任务名',
    },
    {
        prop: 'status',
        label: '状态',
        formatter: ({ row }: { row: Record<string, number | string> }) => {
            const attr = statusEnum[row.status];
            return h('div', { style: `color:${attr.color}` }, attr.text);
        },
    },
    {
        prop: 'time',
        label: '已耗时',
    },
    {
        prop: 'query',
        label: '查询语句',
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
        label: '关键信息',
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
        label: '应用',
    },
    {
        prop: 'engine',
        label: '引擎',
    },
    {
        prop: 'creator',
        label: '创建人',
    },
    {
        prop: 'creatingTime',
        label: '创建时间',
    },
    {
        label: '操作',
        action: [
            {
                label: '查看',
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
        taskId: '1',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        keyInfo: '1',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '2',
        taskName: '任务',
        status: '3',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '3',
        taskName: '任务',
        status: '4',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '4',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '5',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '6',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '7',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '8',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '9',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '10',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
    },
    {
        taskId: '11',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
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
</script>

<style scoped src="./index.less"></style>
<style scoped>
.wrapper {
    position: relative;
    height: 100vh;
    overflow: auto;

    .table {
        height: calc(100% - 350px);
        overflow-y: auto;
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
