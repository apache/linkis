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
    <div class="wrapper">
        <template
            v-for="(dataItem, index) in tempDataList"
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

<script setup lang="ts">
import { ref, watch } from 'vue';

type PropsDataList = Array<{
    name: string;
    description: string;
    value: string | number;
}>;

interface Props {
    dataList: PropsDataList;
    isEditing: boolean;
}

const props = defineProps<Props>();
const tempDataList = ref<PropsDataList>([...props.dataList]);
const tempDataBeforeEditing = ref<PropsDataList>([...props.dataList]);

const save = () => {
    // TODO: send a request
    tempDataList.value?.forEach((row, index) => {
        tempDataBeforeEditing.value[index].value = row.value;
    });
};

const cancel = (dataBeforeEditing: any) => {
    tempDataBeforeEditing.value?.forEach((row, index) => {
        tempDataList.value[index].value = row.value;
    });
    tempDataBeforeEditing.value = [...dataBeforeEditing];
};
// const { data, isEditing } = props;
watch(
    () => props.dataList,
    (newDataList) => {
        tempDataList.value = newDataList;
    },
);

defineExpose({
    data: tempDataList,
    save,
    cancel,
});
</script>

<style scoped>
.wrapper {
    height: 100%;
    .line {
        display: flex;
        height: 42px;
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
}
</style>
