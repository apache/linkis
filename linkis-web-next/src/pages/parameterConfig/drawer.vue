<template>
    <FDrawer
        v-model:show="show"
        placement="right"
        width="960"
        @ok="show = false"
    >
        <template #title>
            <div class="title">
                <template v-if="isAddingApplicationAndEngine">
                    <span
                        class="back-icon"
                        @click="isAddingApplicationAndEngine = false"
                        >&lt;</span
                    >
                </template>
                应用及引擎管理
            </div>
        </template>
        <template v-if="!isAddingApplicationAndEngine">
            <FButton
                type="primary"
                style="margin: 8px 0 24px"
                @click="isAddingApplicationAndEngine = true"
            >
                添加应用及引擎
            </FButton>
        </template>

        <div class="content-wrapper">
            <template v-if="!isAddingApplicationAndEngine">
                <f-table
                    :data="currentData"
                    class="table"
                    :rowKey="
                            (row: Record<string, number|string>) => {
                                return row.taskId;
                            }
                        "
                    
                >
                    <template v-for="col in tableColumns" :key="col.label">
                        
                        <f-table-column
                            v-if="col.formatter && col.label!=='操作'"
                            :prop="col.prop"
                            :label="col.label"
                            :formatter="col.formatter"
                        >
                        </f-table-column>
                        <f-table-column
                            v-else-if="col.label !== '操作'"
                            :prop="col.prop"
                            :label="col.label"
                        >
                        </f-table-column>
                        
                        <!-- 单独渲染操作这一栏 -->
                        <f-table-column
                            v-if="col.label === '操作'"
                            :label="'操作'"
                            :align="'center'"
                            :width="200"
                            :action="col.action"
                            fixed="right"
                        ></f-table-column>
                    </template>
                    
                </f-table>
                <FPagination
                    show-size-changer
                    :total-count="data.length"
                    v-model:currentPage="currentPage"
                    @change="handleChange"
                    :pageSizeOption="pageSizeOption"
                    v-model:pageSize="pageSize"
                ></FPagination>
            </template>

            <template v-else>
                <FForm :labelWidth="60">
                    <FFormItem label="应用名称">
                        <FInput placeholder="请输入"></FInput>
                    </FFormItem>
                    <FFormItem label="应用描述">
                        <FInput placeholder="请输入"></FInput>
                    </FFormItem>

                    <div class="engine-config">
                        <div class="label">引擎配置</div>
                        <div class="configs">
                            <template
                                v-for="(config, index) in engineConfigList"
                                :key="`${config.type}-${index}`"
                            >
                                <div class="config-card">
                                    <div class="title">
                                        引擎{{
                                            (index + 1)
                                                .toString()
                                                .padStart(2, '0')
                                        }}
                                    </div>
                                    <FFormItem label="引擎类型">
                                        <FInput
                                            placeholder="请输入"
                                            v-model="config.type"
                                        ></FInput>
                                    </FFormItem>
                                    <FFormItem label="引擎版本">
                                        <FInput
                                            placeholder="请输入"
                                            v-model="config.version"
                                        ></FInput>
                                    </FFormItem>
                                    <FFormItem label="引擎描述">
                                        <FInput
                                            placeholder="请输入"
                                            v-model="config.description"
                                        ></FInput>
                                    </FFormItem>
                                </div>
                            </template>
                        </div>
                    </div>
                </FForm>
            </template>
        </div>
    </FDrawer>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { FButton } from '@fesjs/fes-design';


const isAddingApplicationAndEngine = ref(false);
const tableColumns = [
    {
        prop: 'taskId',
        label: '序号',
    },
    {
        prop: 'application',
        label: '应用名称',
    },
    {
        prop: 'statement',
        label: '描述',
        formatter: () => '1',
    },
    {
        prop: 'engineNumber',
        label: '引擎个数',
        formatter: () => 0,
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
                label: '编辑',
                func: () => {
                    // console.log('[table.action] [action.编辑] row:', row);
                },
                
            },
            {
                label: '删除',
                func: () => {
                    // console.log('[table.action] [action.删除] row:', row);
                    
                },
                
            },
        ]
    },
];

const engineConfigList = [
    {
        type: '引擎类型',
        version: '版本',
        description: '描述',
    },
    {
        type: '引擎类型',
        version: '版本',
        description: '描述',
    },
    {
        type: '引擎类型',
        version: '版本',
        description: '描述',
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
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },
    {
        taskId: '2',
        taskName: '任务',
        status: '3',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明222',
        creatingTime: '1204',
        engineNumber:0
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
        engineNumber:0
    },
    {
        taskId: '41',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '42',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '43',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '44',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '45',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '46',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '47',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '48',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },{
        taskId: '12',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber:0
    },
]);

const pageSizeOption = reactive([10, 20, 30, 50, 100]);

const currentData= ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
// eslint-disable-next-line no-shadow
const handleChange = (currentPage:number, pageSize:number) => {
    console.log('???', currentPage, pageSize);
    const temp = data.slice((currentPage - 1) * pageSize, currentPage * pageSize);
    currentData.value = [...temp];
    console.log(temp)
};
handleChange(currentPage.value, 10);


const show = ref(false);
const open = () => {
    show.value = true;
};

defineExpose({
    open,
});
// const totalItems = data.length;
// const pageSize = 10;
// let currentPage = 1;
// let currentItems = [];
// function getDataSubset() {  
//     // 根据当前页码和每页数据个数，从数据源中获取对应的数据子集  
//     // 这里使用简单的模拟数据，实际情况需要根据具体数据源进行适配  
//     const start = (currentPage - 1) * pageSize;  
//     const end = start + pageSize;  
    
// }
// function handleChange(newPage:number) {  
//     currentPage = newPage;  
//     // 根据当前页码获取对应的数据子集  
//     currentItems = getDataSubset();  
// }


</script>

<style scoped>
.title {
    font-family: PingFangSC-Medium;
    font-size: 16px;
    color: #000000;
    letter-spacing: 0;
    line-height: 24px;
    font-weight: 500;
}
.back-icon {
    color: #93949b;
    font-weight: 800;
    cursor: pointer;
}
.content-wrapper {
    width: 960px;

    .engine-config {
        display: flex;
        .label {
            min-width: 56px;
            font-size: 14px;
            color: #0f1222;
            text-align: right;
            line-height: 22px;
            font-weight: 400;
        }
        .configs {
            margin-left: 22px;
            width: 100%;
            .config-card {
                width: 100%;
                background: #ffffff;
                border: 1px solid #cfd0d3;
                border-radius: 4px;
                padding: 16px 16px 0;
                margin-bottom: 16px;

                .title {
                    font-family: PingFangSC-Medium;
                    font-size: 14px;
                    color: #0f1222;
                    line-height: 22px;
                    font-weight: 500;
                    margin-bottom: 16px;
                }
            }
        }
    }
}
:deep(.fes-pagination){
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
}
</style>
