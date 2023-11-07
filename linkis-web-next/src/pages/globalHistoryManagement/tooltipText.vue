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
        <FTooltip mode="popover">
            <div :style="props.textStyle">{{ props.text }}</div>
            <template #title>
                <div class="title">
                    {{ props.tipTitle }}
                </div>
            </template>
            <template #content>
                <div class="content" ref="contentRef">
                    {{ props.tipContent }}
                </div>
                <div class="content" v-html="tipHTMLContent"></div>
                <!-- <slot name="default"></slot> -->
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

<style lang="less" src="./index.less"></style>
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

    .content {
        max-width: 640px;
    }
}
</style>
