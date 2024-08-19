<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<template>
    <FSpace class="wrapper">
        <FTooltip mode="popover" showAfter="500">
            <div :style="props.textStyle">{{ props.text }}</div>
            <template #title>
                <div class="tip-title">
                    {{ props.tipTitle }}
                </div>
            </template>
            <template #content>
                <div ref="contentRef" class="tip-content">
                    {{ props.tipContent }}
                </div>
                <div v-html="tipHTMLContent" class="tip-content"></div>
            </template>
        </FTooltip>
    </FSpace>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
    text: string;
    textStyle?: Record<string, number | string>;
    tipTitle?: string;
    tipContent?: string;
    tipHTMLContent?: string;
}>();

const contentRef = ref(null);

defineExpose({
    contentRef,
});
</script>

<style scoped lang="less">
.wrapper {
    cursor: pointer;
    .tip-title {
        font-size: 16px;
        color: #0f1222;
        letter-spacing: 0;
        line-height: 24px;
        font-weight: bold;
    }
    .tip-content {
        max-width: 640px;
    }
}
</style>
