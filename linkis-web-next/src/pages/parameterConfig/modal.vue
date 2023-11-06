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
    <FSpace>
        <FModal
            v-model:show="show"
            title="常规"
            displayDirective="if"
            @ok="show = false"
        >
            <template #title>
                <div>全局配置</div>
            </template>

            <div class="list-wrapper">
                <template
                    v-for="(dataItem, index) in dataList"
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
                                <div>
                                    <FInput
                                        style="width: 120px"
                                        ref="inputRef"
                                        v-model="dataItem.value"
                                        placeholder="请输入"
                                    />
                                </div>
                            </FSpace>
                        </div>
                    </div>
                    <template v-if="index !== dataList.length - 1">
                        <FDivider></FDivider>
                    </template>
                </template>
            </div>
        </FModal>
    </FSpace>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';

const show = ref(false);

const dataList = reactive([
    { name: 'yarn队列名', description: '{wds.linkis.yarnqueue}', value: '1' },
    { name: '队列CPU使用上限', description: '2', value: '2' },
    { name: '队列内存使用上限', description: '3', value: '3' },
    { name: '全局各个引擎内存使用上限', description: '3', value: '3' },
    { name: '全局各个引擎核心个数上限', description: '3', value: '3' },
    { name: '全局各个引擎最大并发数', description: '3', value: '3' },
]);

const open = () => {
    show.value = true;
};

defineExpose({
    open,
});
</script>
<style src="./index.less" scoped></style>
