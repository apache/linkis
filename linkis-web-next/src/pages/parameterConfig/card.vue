<template>
    <div class="card-wrapper">
        <FCard>
            <template #header>
                <div class="card-header">
                    <div class="header-text">
                        <div class="title">
                            {{ props.name }}
                        </div>
                        <div class="description">
                            {{ props.description }}
                        </div>
                    </div>
                    <FButton type="link">操作</FButton>
                </div>
            </template>
            <template #default>
                <slot></slot>
                <!-- <f-table :data="data" layout="auto" :showHeader="false">
                    <f-table-column prop="text" label="text" :span="23">
                        <div class="item-name">name</div>
                        <div class="item-description">description</div>
                    </f-table-column>

                    <template v-if="true">
                        <f-table-column :span="1">
                            <div class="number"></div>
                            <div class="item-description">description</div>
                        </f-table-column>
                    </template>
                    <template v-else> </template>
                </f-table> -->
            </template>
        </FCard>
    </div>
</template>

<script setup lang="ts">
const props = defineProps<{
    name: string;
    description: string;
}>();
</script>

<style scoped>
:deep(.fes-card__header) {
    background: #f7f7f8;
}
:deep(.fes-btn, .fes-btn-type-link) {
    padding-right: 0;
}
.card-wrapper {
    .card-header {
        .header-text {
            .title {
                font-family: PingFangSC-Medium;
                font-size: 16px;
                color: #0f1222;
                line-height: 24px;
                font-weight: 500;
            }
            .description {
                font-family: PingFangSC-Regular;
                font-size: 12px;
                color: #93949b;
                line-height: 20px;
                font-weight: 400;
            }
        }
        height: 56px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
}
</style>
