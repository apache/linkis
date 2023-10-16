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
    <FDrawer
        v-model:show="show"
        placement="right"
        width="960"
        @ok="show = false"
        :footer="true"
    >
        <template #title>
            <div class="title">
                <template v-if="isAddingApplicationAndEngine">
                    <span
                        class="back-icon"
                        @click="isAddingApplicationAndEngine = false"
                    >
                        &lt;
                    </span>
                </template>
                {{ $t('message.linkis.applicationAndEngine') }}
            </div>
        </template>

        <template v-if="!isAddingApplicationAndEngine">
            <FButton
                type="primary"
                style="margin: 8px 0 24px"
                @click="isAddingApplicationAndEngine = true"
            >
                {{ $t('message.linkis.addApplicationAndEngine') }}
            </FButton>
        </template>

        <div class="content-wrapper">
            <template v-if="!isAddingApplicationAndEngine">
                <f-table
                    :data="currentData"
                    class="table"
                    :rowKey="(row: Record<string, number | string>) => {
                    return row.taskId;
                }
                    "
                >
                    <template v-for="col in tableColumns" :key="col.label">
                        <f-table-column
                            v-if="col.formatter"
                            :prop="col.prop"
                            :label="col.label"
                            :formatter="col.formatter"
                            :width="col?.width"
                        >
                        </f-table-column>
                        <f-table-column
                            v-else
                            :prop="col.prop"
                            :label="col.label"
                            :width="col?.width"
                            :action="col.action"
                            :fixed="col?.fixed ?? false"
                        >
                        </f-table-column>
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
                    <FFormItem :label="$t('message.linkis.applicationName')">
                        <FInput placeholder="请输入"></FInput>
                    </FFormItem>
                    <FFormItem :label="$t('message.linkis.applicationDesc')">
                        <FInput placeholder="请输入"></FInput>
                    </FFormItem>

                    <div class="engine-config">
                        <div class="label">
                            {{ $t('message.linkis.engineConfig') }}
                        </div>
                        <div class="configs">
                            <template
                                v-for="(config, index) in engineConfigList"
                                :key="`${config.type}-${index}`"
                            >
                                <div class="config-card">
                                    <div class="header">
                                        <div class="title">
                                            引擎{{
                                                (index + 1)
                                                    .toString()
                                                    .padStart(2, '0')
                                            }}
                                        </div>
                                        <div class="delete_btn">删除</div>
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
                            <FButton
                                type="link"
                                class="engine-adding-btn"
                                @click="addEngine"
                            >
                                添加引擎
                            </FButton>
                        </div>
                    </div>
                </FForm>
            </template>
        </div>

        <template #footer>
            <FSpace>
                <FButton type="primary" @click="confirmEditing">{{
                    $t('message.common.submit')
                }}</FButton>
                <FButton @click="show = false">{{
                    $t('message.common.cancel')
                }}</FButton>
            </FSpace>
        </template>
    </FDrawer>
</template>

<script setup lang="ts">
import { ref, reactive, h } from 'vue';
import { FButton } from '@fesjs/fes-design';

const isAddingApplicationAndEngine = ref(false);
const tableColumns = [
    {
        prop: 'taskId',
        label: '序号',
        width: 60,
    },
    {
        prop: 'application',
        label: '应用名称',
        width: 140,
    },
    {
        prop: 'statement',
        label: '描述',
        width: 160,
        formatter: () => '1',
    },
    {
        prop: 'engineNumber',
        label: '引擎个数',
        width: 160,
        formatter: () => 0,
    },
    {
        prop: 'creator',
        width: 140,
        label: '创建人',
    },
    {
        prop: 'creatingTime',
        width: 180,
        label: '创建时间',
    },

    {
        label: '操作',
        width: 160,
        fixed: 'right',
        action: [
            {
                label: '编辑',
                func: () => {
                    // console.log('[table.action] [action.编辑] row:', row);
                },
            },
            {
                label: h(
                    'span',
                    {
                        style: {
                            color: '#F75F56',
                        },
                    },
                    '删除',
                ),
                func: () => {
                    // console.log('[table.action] [action.删除] row:', row);
                },
            },
        ],
    },
];

const engineConfigList = reactive([
    {
        type: '引擎类型',
        version: '版本',
        description: '描述',
    },
]);

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
        engineNumber: 0,
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
        engineNumber: 0,
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
        engineNumber: 0,
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
        engineNumber: 0,
    },
    {
        taskId: '42',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
    {
        taskId: '43',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
    {
        taskId: '44',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
    {
        taskId: '45',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
    {
        taskId: '46',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
    {
        taskId: '47',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
    {
        taskId: '48',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
    {
        taskId: '12',
        taskName: '任务',
        status: '1',
        time: '1',
        query: '哈哈哈',
        application: '应用',
        engine: '引擎',
        creator: '张小明',
        creatingTime: '1204',
        engineNumber: 0,
    },
]);

const pageSizeOption = reactive([10, 20, 30, 50, 100]);

const currentData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
// eslint-disable-next-line no-shadow
const handleChange = (currentPage: number, pageSize: number) => {
    const temp = data.slice(
        (currentPage - 1) * pageSize,
        currentPage * pageSize,
    );
    currentData.value = [...temp] as any;
};

const show = ref(false);
const open = () => {
    show.value = true;
};

const addEngine = () => {
    engineConfigList.push({
        type: '',
        version: '',
        description: '',
    });
};

const confirmEditing = () => {
    // TODO: send a request to save the config
    show.value = false;
};

defineExpose({
    open,
});
</script>

<style scoped>
.header {
    font-family: PingFangSC-Medium;
    font-size: 16px;

    display: flex;
    justify-content: space-between;
    font-family: PingFangSC-Regular;

    .title {
        color: #000000;
        letter-spacing: 0;
        line-height: 24px;
        font-weight: 500;
    }

    .delete_btn {
        font-size: 14px;
        color: #63656f;
        line-height: 22px;
        font-weight: 400;
        cursor: pointer;
    }
}

.back-icon {
    color: #93949b;
    font-weight: 800;
    cursor: pointer;
}

.table {
    overflow: auto;
    max-height: calc(78vh - 100px);
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

            .engine-adding-btn {
                transform: translateY(-5px);
            }
        }
    }
}

:deep(.fes-pagination) {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
}
</style>
