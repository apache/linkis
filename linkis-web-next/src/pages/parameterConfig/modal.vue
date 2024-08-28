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
    <FModal
        v-model:show="show"
        displayDirective="if"
        @ok="handleSaveGlobalSettings"
    >
        <template #title>
            <div>{{ t('message.linkis.globalSettings') }}</div>
        </template>
        <div class="list-wrapper"> 
            <template v-for="dataItem in dataList[0].settings" :key="dataItem.key">
                <div class="line">
                    <div>
                        <div class="title">{{ dataItem.name || dataItem.key }}</div>
                        <div class="description">
                            {{ dataItem.name ? `[${dataItem.key}]` : '' }}
                        </div>
                    </div>
                    <div>
                        <FSpace>
                            <div style="width: 120px">
                                <FInputNumber
                                    v-if="dataItem.validateType === 'NumInterval'"
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
                                            validator: () => new RegExp(dataItem.validateRange).test(dataItem.configValue)
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
                        </FSpace>
                    </div>
                </div>
                <FDivider />
            </template>
        </div>
    </FModal>
</template>

<script setup lang="ts">
import api from '@/service/api';
import { FMessage } from '@fesjs/fes-design';
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

const show = ref(false);
const dataList = ref<Array<{
    name: string,
    description: string | null,
    settings: Array<{
        name: string,
        key: string,
        description: string,
        defaultValue: string,
        configValue: string,
        validateType: string,
        validateRange: string,
    }>
}>>([]);

const handleSaveGlobalSettings = async () => {
    try {
        await api.fetch("/configuration/saveFullTree", {
            fullTree: dataList.value,
            creator: 'GlobalSettings',
            engineType: null,
        })
        FMessage.success(t('message.common.saveSuccess'));
        show.value = false;
        handleInitialization();
    } catch (error) {
        window.console.error(error);
    }
}

const handleInitialization = async () => {
    const res = await api.fetch("/configuration/getFullTreesByAppName", { creator: 'GlobalSettings' }, "get");
    dataList.value = res.fullTree;
}

const open = () => {
    show.value = true;
};

onMounted(() => {
    handleInitialization();
})

defineExpose({
    open,
});
</script>
<style src="./index.less" scoped></style>
<style scoped lang="less">
.list-wrapper {
    height: 500px;
    overflow: auto;
    margin-right: -24px;
    padding-right: 20px;
}
</style>