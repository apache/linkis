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
                v-model="instanceName"
                :placeholder="t('message.linkis.datasource.pleaseInput')"
            />
        </FFormItem>
        <FFormItem
            :label="t('message.linkis.initiator')"
            :props="t('message.linkis.initiator')"
        >
            <FInput
                v-model="creator"
                :placeholder="t('message.linkis.datasource.pleaseInput')"
            />
        </FFormItem>
        <FFormItem
                :label="t('message.linkis.columns.createdTime')"
                :prop="t('message.linkis.columns.createdTime')"
                :span="8"
            >
                <!-- 日期修改完成后需要让日期选择组件失焦 -->
                <FDatePicker
                    type="daterange"
                    v-model="timeRange"
                    :shortcuts="shortcuts"
                    @focus="(e:any) => { e.target.blur() }"
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
                <FButton type="primary" @click="() => { search(currentPage) }">
                    {{ t('message.linkis.find') }}
                </FButton>
            </div>
        </FFormItem>
    </FForm>
    <f-table
        :data="dataList"
        class="table"
        :rowKey="(row: Record<string, number | string>) => row.serviceInstance"
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
        :total-count="totalSize"
        v-model:currentPage="currentPage"
        @change="handleChangePagination"
        :pageSizeOption="[10, 20, 30, 50, 100]"
        v-model:pageSize="pageSize"
    />
    <Drawer ref="drawerRef" :engine="currentEngine" />
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { FSpace, FTag } from '@fesjs/fes-design';
import dayjs from 'dayjs';
import { onMounted, reactive, ref, h } from 'vue';
import Drawer from './drawer/index.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const instanceName = ref('');
const creator = ref('');
const timeRange = ref([new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()]);
const engineType = ref('');
const engineTypes = reactive<string[]>([]);
const dataList = ref();
const totalSize = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const drawerRef = ref();
const currentEngine = ref();

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

const handleViewLog = (engine: any) => {
    currentEngine.value = engine;
    drawerRef.value?.open();
}

const tableColumns = [
    {
        prop: 'serviceInstance',
        label: t('message.linkis.tableColumns.engineInstance'),
    },
    {
        prop: 'engineType',
        label: t('message.linkis.tableColumns.engineType'),
    },
    {
        prop: 'status',
        label: t('message.linkis.tableColumns.status'),
    },
    {
        prop: 'labelValue',
        label: t('message.linkis.tableColumns.label'),
        formatter: (data: { row: { labelValue: string }}) => 
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            h(FSpace, data.row.labelValue.split(',').map(label => h(FTag, { type: 'default', effect: 'plain', size: 'small' }, label)))
    },
    {
        prop: 'usedResource',
        label: t('message.linkis.tableColumns.usedResources'),
        formatter: ({ row }: { row: { usedResource: { cores: number, memory: number, instance: number }}}) =>
            `${row.usedResource.cores}cores,${row.usedResource.memory / 1024 / 1024 / 1024}G,${row.usedResource.instance}apps`,
    },
    {
        prop: 'createUser',
        label: t('message.linkis.tableColumns.requestApplicationName'),
    },
    {
        prop: 'usedTime',
        label: t('message.linkis.tableColumns.startTime'),
        formatter: ({ row }: { row: { usedTime: number } }) => dayjs(row.usedTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
        // 在“操作”两个字前添加空格，使其能与“查看日志”上下对齐
        label: `\u00A0\u00A0\u00A0\u00A0${t('message.linkis.columns.control.title')}`,
        action: [{
            label: t('message.linkis.viewlog'),
            func: handleViewLog,
        }],
    },
];

const search = (currentPage = 1) => {
    api.fetch('/linkisManager/ecinfo/ecrHistoryList', {
        instance: instanceName.value,
        creator: creator.value,
        startDate: dayjs(timeRange.value[0]).format('YYYY-MM-DD HH:mm:ss'),
        endDate: dayjs(timeRange.value[1]).format('YYYY-MM-DD HH:mm:ss'),
        engineType: engineType.value,
        pageNow: currentPage,
        pageSize: pageSize.value,
    }, 'get').then((res: any) => {
        dataList.value = [...res.engineList] || [];
        totalSize.value = res.totalPage;
    })
}

const handleChangePagination = () => {
    search(currentPage.value)
};

const handleInitialization = () => {
    engineTypes.length = 0;
    api.fetch('/configuration/engineType', 'get').then((res: any) => {
        res.engineType.forEach((engineType: string) => {
            engineTypes.push(engineType);
        })
    })
    search();
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