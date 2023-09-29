<template>
    <div class="label">
        <span>{{ $t('message.common.statisticsTime') }}: </span>
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
import { onMounted, reactive } from 'vue';

const emit = defineEmits(['find']);

const countDataList = reactive([
    {
        title: 'message.linkis.countList.all',
        number: 2000,
        color: '#5384FF',
    },
    {
        title: 'message.linkis.countList.succeed',
        number: 2000,
        color: '#00CB91',
    },
    {
        title: 'message.linkis.countList.inited',
        number: 2000,
        color: '#FF9540',
    },
    {
        title: 'message.linkis.countList.running',
        number: 2000,
        color: '#F29360',
    },
    {
        title: 'message.linkis.countList.failed',
        number: 2000,
        color: '#F75F56',
    },
    {
        title: 'message.linkis.countList.others',
        number: 2000,
        color: '#0F1222',
    },
]);

const handleFind = (status: string, callback: (v: any) => void) => {
    emit('find', { status }, callback);
};

onMounted(() => {
    Promise.all(
        ['', 'Succeed', 'Inited', 'Running', 'Failed'].map(
            (status) =>
                new Promise((resolve) => {
                    handleFind(status, resolve);
                }),
        ),
    )
        .then((res) => {
            let sum = 0;
            (res as Array<{ totalPage: number }>).forEach((item, index) => {
                countDataList[index].number = item.totalPage;
                if (index > 0) sum += item.totalPage;
            });
            countDataList[countDataList.length - 1].number =
                countDataList[0].number - sum;
        })
        .catch((err) => {
            FMessage.error(err);
        });
});
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
