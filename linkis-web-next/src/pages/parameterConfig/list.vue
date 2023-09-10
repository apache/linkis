<template>
    <template v-for="dataItem in data" :key="dataItem.name">
        <div>
            <div class="line">
                <div class="text">
                    <div class="title">{{ dataItem.name }}</div>
                    <div class="description">
                        {{ dataItem.description }}
                    </div>
                </div>

                <div class="value">
                    <FInput
                        style="width: 120px"
                        ref="inputRef"
                        v-model="dataItem.value"
                        placeholder="请输入"
                    />
                </div>
            </div>
            <!-- <template v-if="index !== dataList.length - 1"> -->
            <FDivider></FDivider>
            <!-- </template> -->
        </div>
    </template>
</template>

<script setup lang="ts">
interface Props {
    data: Array<{
        name: string;
        description: string;
        value: string | number;
    }>;
}

const props = defineProps<Props>();
const { data } = props;
console.log('props', props.data);
</script>

<style scoped>
.line {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .text {
        .title {
            font-family: PingFangSC-Regular;
            font-size: 14px;
            color: #0f1222;
            line-height: 22px;
            font-weight: 400;
        }
        .description {
            font-family: PingFangSC-Regular;
            font-size: 12px;
            color: #93949b;
            line-height: 20px;
            font-weight: 400;
        }
    }
}
</style>
