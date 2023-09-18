<template>
    <FSpace class="wrapper">
        <FTooltip mode="popover">
            <div :style="props.textStyle">{{ props.text }}</div>
            <template #title>
                <div class="title">
                    {{ props.tipTitle }}
                </div>
            </template>
            <template #content>
                <div style="max-width: 640px">
                    {{ props.tipContent }}
                </div>
                <slot name="default"></slot>
            </template>
        </FTooltip>
    </FSpace>
</template>

<script setup lang="ts">
const props = defineProps<{
    text: string;
    textStyle?: Record<string, number | string>;
    tipTitle?: string;
    tipContent?: string;
}>();
</script>

<style scoped>
.wrapper {
    cursor: pointer;
    .title {
        font-family: PingFangSC-Medium;
        font-size: 16px;
        color: #0f1222;
        letter-spacing: 0;
        line-height: 24px;
        font-weight: 500;
    }
}
</style>
