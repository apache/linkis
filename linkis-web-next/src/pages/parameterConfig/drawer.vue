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
                    :data="data"
                    class="table"
                    :rowKey="
                            (row: Record<string, number|string>) => {
                                return row.taskId;
                            }
                        "
                >
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
        label: '任务ID',
    },
    {
        prop: 'taskName',
        label: '任务名',
    },
    {
        prop: 'status',
        label: '状态',
        formatter: () => '1',
    },
    {
        prop: 'time',
        label: '已耗时',
    },
    {
        prop: 'query',
        label: '查询语句',
        // { row }: { row: Record<string, number | string> }
        formatter: () => '1q',
        // h(TooltipText, {
        //     text: row.query,
        //     tipTitle: '这是标题',
        //     tipContent: '这是内容',
        // }),
    },
    {
        label: '关键信息',
        action: [
            {
                label: '查看',
                func: () => {
                    console.log('[table.action] [action.编辑] row:');
                },
            },
        ],
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
                func: () => {
                    // drawerRef.value?.open?.(rawData);
                    // setIsShowDrawer(true);
                },
            },
        ],
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
]);

const show = ref(false);
const open = () => {
    show.value = true;
};

defineExpose({
    open,
});
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
</style>
