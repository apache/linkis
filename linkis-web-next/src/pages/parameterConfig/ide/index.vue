<template>
    <div class="card-list">
        <template v-for="item in cardList" :key="item.name">
            <Card
                :name="item.name"
                :description="item.description"
                :dataList="item.listData"
                class="card"
            >
            </Card>
        </template>
    </div>
</template>

<script setup lang="ts">
import Card from '../card.vue';
// import List from '../list.vue';

const cardList = [
    {
        name: 'card1',
        description: 'desc1',
        listData: [
            {
                name: 'spark引擎最大并发数',
                description: '{wds.linkis.instance}',
                value: 1,
            },
            { name: 'spark引擎核心个数', description: 'description', value: 1 },
            { name: 'spark引擎内存', description: 'description', value: 1 },
            {
                name: 'spark执行器核心个数',
                description: 'description',
                value: 1,
            },
            { name: 'worker并发数', description: 'description', value: 1 },
            { name: 'worker内存大小', description: 'description', value: 1 },
            { name: 'python版本', description: 'description', value: 1 },
            { name: '引擎空间退出时间', description: 'description', value: 1 },
        ],
    },
    {
        name: 'card2',
        description: 'desc2',
        listData: [
            { name: 'name1', description: 'description', value: 1 },
            { name: 'name2', description: 'description', value: 1 },
            { name: 'name3', description: 'description', value: 1 },
        ],
    },
    {
        name: 'card3',
        description: 'desc3',
        listData: [
            { name: 'name1', description: 'description', value: 1 },
            { name: 'name2', description: 'description', value: 1 },
            { name: 'name3', description: 'description', value: 1 },
        ],
    },
    {
        name: 'card4',
        description: 'desc3',
        listData: [
            { name: 'name1', description: 'description', value: 1 },
            { name: 'name2', description: 'description', value: 1 },
            { name: 'name3', description: 'description', value: 1 },
        ],
    },
];
</script>

<style scoped>
.list-wrapper {
    height: 592px;
}
.card-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    .card {
        width: calc(33.3% - 16px);
        min-width: 280px;
        overflow: hidden;
    }
}
</style>
