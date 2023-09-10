<template>
    <FSpace>
        <FModal
            v-model:show="show"
            title="常规"
            displayDirective="if"
            @ok="show = false"
        >
            <template #title>
                <slot name="title"></slot>
            </template>
            <template #default>
                <slot name="default"></slot>
            </template>
        </FModal>
    </FSpace>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const show = ref(false);

const open = () => {
    show.value = true;
};

defineExpose({
    open,
});
</script>
