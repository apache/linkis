<template>
    <div class="label">
        <span>统计日期: </span>
        <FDatePicker
            type="daterange"
            style="width: 320px"
            :modelValue="[Date.now(), Date.now() + 7 * 24 * 60 * 60 * 1000]"
        >
            <template #separator> - </template>
        </FDatePicker>
    </div>
    <div class="count-data-list">
        <template v-for="item in countDataList" :key="item.title">
            <div class="count-card">
                <div class="title">{{ item.title }}</div>
                <div class="num" :style="`color:${item.color}`">
                    {{ item.number }}
                </div>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';

const countDataList = reactive([
    { title: '任务总数', number: 2000, color: '#5384FF' },
    { title: '成功', number: 2000, color: '#00CB91' },
    { title: '排队中', number: 2000, color: '#FF9540' },
    { title: '运行中', number: 2000, color: '#F29360' },
    { title: '失败', number: 2000, color: '#F75F56' },
    { title: '其他', number: 2000, color: '#0F1222' },
]);
</script>

<style scoped src="./index.less"></style>
<style scoped>
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
