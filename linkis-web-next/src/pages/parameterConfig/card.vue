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
                    <template v-if="isEditing === false">
                        <FButton type="link" @click="options.edit">
                            操作
                        </FButton>
                    </template>
                    <template v-else>
                        <div>
                            <FButton type="link" @click="options.confirm">
                                确定
                            </FButton>
                            <FButton type="link" @click="options.cancel">
                                取消
                            </FButton>
                        </div>
                    </template>
                </div>
            </template>
            <template #default>
                <!-- <slot :setSlotRef="setSlotRef" :isEditing="isEditing"></slot> -->

                <div class="list-wrapper">
                    <template
                        v-for="(dataItem, index) in listData"
                        :key="dataItem.name"
                    >
                        <div class="line">
                            <div class="text">
                                <div class="title">{{ dataItem.name }}</div>
                                <div class="description">
                                    {{ dataItem.description }}
                                </div>
                            </div>

                            <div class="value">
                                <FSpace>
                                    <template v-if="isEditing">
                                        <div>
                                            <FInput
                                                style="width: 120px"
                                                ref="inputRef"
                                                v-model="dataItem.value"
                                                placeholder="请输入"
                                            />
                                        </div>
                                    </template>
                                    <template v-else>
                                        {{ dataItem.value }}
                                    </template>
                                </FSpace>
                            </div>
                        </div>
                        <template v-if="index !== dataList.length - 1">
                            <FDivider></FDivider>
                        </template>
                    </template>
                </div>
            </template>
        </FCard>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

type DataList = Array<{
    name: string;
    description: string;
    value: string | number;
}>;

const props = defineProps<{
    name: string;
    description: string;
    dataList: DataList;
}>();
const isEditing = ref(false);
const listData = ref<DataList>([...props.dataList]);
const listDataBeforeEditing = ref<DataList>([]);

const options = {
    edit: () => {
        isEditing.value = true;
        listDataBeforeEditing.value = listData.value.map((item) => ({
            ...item,
        }));
    },
    confirm: () => {
        isEditing.value = false;
        listDataBeforeEditing.value = [...listData.value];
        listDataBeforeEditing.value = listData.value.map((item) => ({
            ...item,
        }));
    },
    cancel: () => {
        isEditing.value = false;
        listData.value = listDataBeforeEditing.value.map((item) => ({
            ...item,
        }));
    },
};
</script>

<style src="./index.less" scoped></style>
<style scoped>
:deep(.fes-card__header) {
    background: #f7f7f8;
}
:deep(.fes-btn, .fes-btn-type-link) {
    padding-right: 0;
}
:deep(.fes-card__body) {
    height: 592px;
}
:deep(.fes-divider) {
    margin: 16px 0;
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
