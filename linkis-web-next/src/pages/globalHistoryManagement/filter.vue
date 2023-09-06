<template>
    <div class="label">全局任务查询</div>
    <div class="filter">
        <FForm class="form" v-show="!isCheckingTaskToStop">
            <FFormItem label="任务ID">
                <FInput placeholder="请输入" class="short-form-item"></FInput>
            </FFormItem>

            <FFormItem label="状态">
                <FSelect clearable placeholder="请选择" class="short-form-item">
                    <FOption
                        v-for="(item, index) in optionList"
                        :key="index"
                        :value="item.value"
                        :label="item.label"
                    >
                    </FOption>
                </FSelect>
            </FFormItem>

            <FFormItem label="引擎">
                <FSelect clearable placeholder="请选择" class="short-form-item">
                    <FOption
                        v-for="(item, index) in optionList"
                        :key="index"
                        :value="item.value"
                        :label="item.label"
                    ></FOption>
                </FSelect>
            </FFormItem>

            <FFormItem label="创建时间" prop="time" class="long-form-item">
                <FDatePicker
                    type="daterange"
                    :modelValue="[
                        Date.now(),
                        Date.now() + 7 * 24 * 60 * 60 * 1000,
                    ]"
                >
                    <template #separator> - </template>
                </FDatePicker>
            </FFormItem>

            <FFormItem label="创建人">
                <FSelect clearable placeholder="请选择" class="short-form-item">
                    <FOption
                        v-for="(item, index) in optionList"
                        :key="index"
                        :value="item.value"
                        :label="item.label"
                    ></FOption>
                </FSelect>
            </FFormItem>

            <div class="buttons">
                <template v-if="!isCheckingTaskToStop">
                    <FButton type="primary">查询</FButton>
                    <FButton>重置</FButton>
                    <FButton
                        class="stop-button"
                        @click="isCheckingTaskToStop = true"
                    >
                        批量停止
                    </FButton>
                </template>
            </div>
        </FForm>
        <template v-if="isCheckingTaskToStop">
            <div class="buttons">
                <FButton
                    type="primary"
                    class="confirm-stop-button"
                    @click="confirmStop"
                >
                    确定停止
                </FButton>
                <FButton
                    class="confirm-stop-button"
                    @click="isCheckingTaskToStop = false"
                >
                    取消
                </FButton>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';

const props = defineProps<{
    checkedKeys: Array<string>;
}>();

const optionList = [
    {
        value: 'xxx',
        label: 'xxx',
    },
    {
        value: 'yyy',
        label: 'yyy',
    },
    {
        value: 'zzz',
        label: 'zzz',
    },
];
const isCheckingTaskToStop = ref(false);

const confirmStop = () => {
    console.log(props.checkedKeys);
};

defineExpose({
    isCheckingTaskToStop,
});
</script>

<style scoped src="./index.less"></style>
<style scoped>
.filter {
    position: relative;
    height: 54px;
    .form {
        display: flex;
        gap: 22px;
        .short-form-item {
            width: 120px;
        }
        .long-form-item {
            width: 314px;
        }
    }
    .buttons {
        flex: 1;
        display: flex;
        justify-content: flex-start;
        gap: 16px;
        .stop-button {
            margin-left: auto;
        }
    }
}
</style>
