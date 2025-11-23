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
    <FCard>
        <template #header>
            <div class="card-header">
                <div>
                    <div class="title">
                        {{ props.name }}
                    </div>
                    <div class="description">
                        {{ props.description }}
                    </div>
                </div>
                <template v-if="isEditing === false">
                    <FButton type="link" @click="options.edit">
                        {{ $t('message.linkis.edit') }}
                    </FButton>
                </template>
                <template v-else>
                    <div>
                        <FButton type="link" @click="options.confirm" :disabled="isDisabled">
                            {{ $t('message.common.submit') }}
                        </FButton>
                        <FButton type="link" @click="options.cancel">
                            {{ $t('message.common.cancel') }}
                        </FButton>
                    </div>
                </template>
            </div>
        </template>
        <template #default>
            <div class="list-wrapper">
                <template v-for="item in listData" :key="item.name">
                    <template v-for="dataItem in item.settings" :key="dataItem.key">
                        <div class="line">
                            <div>
                                <div class="title">{{ dataItem.name || dataItem.key }}</div>
                                <div class="description">
                                    {{ dataItem.name ? `[${dataItem.key}]` : '' }}
                                </div>
                            </div>
                            <div>
                                <FSpace>
                                    <div v-if="isEditing" style="width: 120px">
                                        <FSelect
                                            v-if="dataItem.validateType === 'OFT'"
                                            :options="dataItem.validateRangeList"
                                            v-model="dataItem.configValue"
                                            :placeholder="dataItem.defaultValue ? `${$t('message.linkis.defaultValue')}: ${dataItem.defaultValue}` : $t('message.linkis.datasource.pleaseInput')"
                                            value-field="value"
                                            label-field="name"
                                        />
                                        <FInputNumber
                                            v-else-if="dataItem.validateType === 'NumInterval'"
                                            style="width: 120px"
                                            :precision="0"
                                            v-model="dataItem.configValue"
                                            :placeholder="dataItem.defaultValue ? `${$t('message.linkis.defaultValue')}: ${dataItem.defaultValue}` : $t('message.linkis.datasource.pleaseInput')"
                                            :min="JSON.parse(dataItem.validateRange)[0]"
                                            :max="JSON.parse(dataItem.validateRange)[1]"
                                        />
                                        <FForm v-else-if="dataItem.validateType === 'Regex'">
                                            <FFormItem
                                                :value="dataItem.configValue"
                                                :rules="[{
                                                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                                                    // @ts-ignore 
                                                    trigger: ['change', 'blur'],
                                                    message: dataItem.description,
                                                    type: 'regexp',
                                                    validator: () => {
                                                        const reg = new RegExp(dataItem.validateRange);
                                                        if (reg.test(dataItem.configValue)) {
                                                            isDisabled = false;
                                                            return true;
                                                        } else {
                                                            isDisabled = true;
                                                            return false;
                                                        }
                                                    }
                                                }]"
                                            >
                                                <FInput
                                                    class="regex-input"
                                                    v-model="dataItem.configValue"
                                                    :placeholder="dataItem.defaultValue ? `${$t('message.linkis.defaultValue')}: ${dataItem.defaultValue}` : $t('message.linkis.datasource.pleaseInput')"
                                                />
                                            </FFormItem>
                                        </FForm>
                                        <FInput
                                            v-else
                                            v-model="dataItem.configValue"
                                            :placeholder="dataItem.defaultValue ? `${$t('message.linkis.defaultValue')}: ${dataItem.defaultValue}` : $t('message.linkis.datasource.pleaseInput')"
                                        />
                                    </div>
                                    <div v-else class="model-value">
                                        {{ dataItem.configValue || dataItem.defaultValue }}
                                    </div>
                                </FSpace>
                            </div>
                        </div>
                        <FDivider />
                    </template>
                </template>
            </div>
        </template>
    </FCard>
</template>

<script setup lang="ts">
import api from '@/service/api';
import { FFormItem, FInput, FInputNumber, FMessage, FSelect } from '@fesjs/fes-design';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

type DataList = Array<{
    name: string;
    description: string;
    value: string | number;
    settings: any;
}>;

const props = defineProps<{
    name: string;
    description: string;
    dataList: DataList;
    creator: string;
    engineType: string;
}>();
const isEditing = ref(false);
const listData = ref([...props.dataList]);
const listDataBeforeEditing = ref<DataList>([]);
const isDisabled = ref(false);

const options = {
    edit: () => {
        isEditing.value = true;
        listDataBeforeEditing.value = listData.value.map((item) => ({
            ...item,
        }));
    },
    confirm: async () => {
        try {
            await api.fetch("/configuration/saveFullTree", {
                fullTree: listData.value,
                creator: props.creator,
                engineType: props.engineType,
            })
            FMessage.success(t('message.common.saveSuccess'));
            isEditing.value = false;
        } catch (error) {
            isEditing.value = false;
            listData.value = listDataBeforeEditing.value.map((item: any) => ({
                ...item,
            }));
            window.console.error(error);
        }
    },
    cancel: () => {
        isEditing.value = false;
        listData.value = listDataBeforeEditing.value.map((item: any) => ({
            ...item,
        }));
    },
};
</script>

<style src="./index.less" scoped></style>
<style scoped lang="less">
:deep(.fes-card__header) {
    background: #f7f7f8;
}
:deep(.fes-btn, .fes-btn-type-link) {
    padding-right: 0;
    transform: translateX(8px);
}
:deep(.fes-card__body) {
    height: 446px;
}
:deep(.fes-divider) {
    margin: 16px 0;
}
:deep(.fes-form-item-error) {
    transform: translate(-20px, 10px)
}
.card-header {
    height: 56px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title {
        font-size: 16px;
        color: #0f1222;
        line-height: 24px;
        margin-bottom: 6px;
        white-space: normal;
        word-break: break-all;
        word-wrap: break-word;
    }
    .description {
        font-size: 12px;
        color: #93949b;
        line-height: 20px;
        font-weight: 400;
        white-space: normal;
        word-break: break-all;
        word-wrap: break-word;
    }
}
</style>
