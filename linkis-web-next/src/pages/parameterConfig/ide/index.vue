<template>
    <div class="card-list">
        <template v-for="item in cardList" :key="item.name">
            <Card
                :name="item.name"
                :description="item.description"
                class="card"
            >
                <List :data="item.listData"></List>
            </Card>
        </template>
    </div>
</template>

<script setup lang="ts">
import Card from '../card.vue';
import List from '../list.vue';

const cardList = [
    {
        name: 'card1',
        description: 'desc1',
        listData: [
            { name: 'name1', description: 'description', value: 1 },
            { name: 'name2', description: 'description', value: 1 },
            { name: 'name3', description: 'description', value: 1 },
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
