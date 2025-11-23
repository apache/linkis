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
    <div class="label">
        <span>{{ $t('message.common.statisticsTime') }}：</span>
        <!-- 日期修改完成后需要让日期选择组件失焦 -->
        <FDatePicker
            type="daterange"
            v-model="timeRange"
            :shortcuts="$props.shortcuts"
            @change="handleDateChange"
            @focus="(e:any) => { e.target.blur() }"
            style="width: 320px"
        />
    </div>
    <div class="count-data-list">
        <template v-for="item in countDataList" :key="item.title">
            <div class="count-card" @click="() => { handleCountCardClick(item) }">
                <div class="title">{{ $t(item.title) }}</div>
                <div class="num" :style="`color:${item.color}`">
                    {{ item.number }}
                </div>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { FMessage } from '@fesjs/fes-design';
import { onMounted, reactive, ref } from 'vue';

const timeRange = ref([new Date().getTime() - 3600 * 1000 * 24 * 7, new Date().getTime()]);
const emit = defineEmits(['find']);

const props = defineProps<{
    shortcuts: any;
    filterRef: any;
}>();

const countDataList = reactive([
    {
        title: 'message.linkis.countList.all',
        number: 0,
        color: '#5384FF',
        status: ''
    },
    {
        title: 'message.linkis.countList.succeed',
        number: 0,
        color: '#00CB91',
        status: 'Succeed'
    },
    {
        title: 'message.linkis.countList.inited',
        number: 0,
        color: '#FF9540',
        status: 'Inited'
    },
    {
        title: 'message.linkis.countList.running',
        number: 0,
        color: '#F29360',
        status: 'Running'
    },
    {
        title: 'message.linkis.countList.failed',
        number: 0,
        color: '#F75F56',
        status: 'Failed'
    },
    {
        title: 'message.linkis.countList.others',
        number: 0,
        color: '#0F1222',
        status: 'Other'
    },
]);

const handleFind = (status: string, timeRange: number[], callback: (v: any) => void) => {
    emit('find', { status, startDate: timeRange[0], endDate: timeRange[1] }, callback);
};

const handleDateChange = () => {
    Promise.all(['', 'Succeed', 'Inited', 'Running', 'Failed'].map((status) =>
        new Promise((resolve) => {
            handleFind(status, timeRange.value, resolve);
        }))
    ).then((res) => {
        let sum = 0;
        (res as Array<{ totalPage: number }>).forEach((item, index) => {
            countDataList[index].number = item.totalPage;
            if (index > 0) sum += item.totalPage;
        });
        countDataList[countDataList.length - 1].number = countDataList[0].number - sum;
    }).catch((err) => {
        FMessage.error(err);
    })
}

// 点击状态分类卡片时，将Filter组件搜索条件中的其它项置空，时间和状态条件同步为Count组件中的内容
const handleCountCardClick = (countCard: { status: string }) => {
    if(countCard.status !== 'Other') {
        props.filterRef.formData.taskID = '';
        props.filterRef.formData.status = countCard.status;
        props.filterRef.formData.executeApplicationName = '';
        props.filterRef.formData.timeRange = [timeRange.value[0], timeRange.value[1]];
        props.filterRef.formData.creator = '';
        props.filterRef.formData.proxyUser = '';
        props.filterRef.handleSearch();
    }
}

onMounted(() => {
    handleDateChange();
});
</script>

<style scoped src="./index.less"></style>
<style scoped lang="less">
.count-data-list {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
    .count-card {
        background-color: #f7f7f8;
        border-radius: 4px;
        width: 16%;
        height: 80px;
        padding: 16px 20px;
        &:not(:last-child) {
            cursor: pointer;
        }
        .title {
            font-family: PingFangSC-Regular;
            font-size: 12px;
            color: #646670;
            line-height: 20px;
            font-weight: 400;
        }
        .num {
            font-family: PingFangSC-Medium;
            font-size: 20px;
            line-height: 28px;
            font-weight: 500;
        }
    }
}
</style>
