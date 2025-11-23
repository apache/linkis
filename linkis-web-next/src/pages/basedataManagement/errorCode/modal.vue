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
        @ok="handleConfirm"
    >
        <template #title>
            <div class="modal-title">{{ error ? t('message.linkis.edit') : t('message.common.add') }}</div>
        </template>
        <FForm>
            <FFormItem
                :label="t('message.linkis.basedataManagement.errorCode.errorCode')"
                :props="t('message.linkis.basedataManagement.errorCode.errorCode')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (errorCode.length < 1) {
                            callback(t('message.linkis.keyTip'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="errorCode"
                    :placeholder="t('message.linkis.datasource.pleaseInput')"
                />
            </FFormItem>
            <FFormItem
                :label="t('message.linkis.basedataManagement.errorCode.errorDesc')"
                :props="t('message.linkis.basedataManagement.errorCode.errorDesc')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (errorDesc.length < 1) {
                            callback(t('message.linkis.keyTip'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="errorDesc"
                    :placeholder="t('message.linkis.datasource.pleaseInput')"
                />
            </FFormItem>
            <FFormItem
                :label="t('message.linkis.basedataManagement.errorCode.errorRegex')"
                :props="t('message.linkis.basedataManagement.errorCode.errorRegex')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (errorRegex.length < 1) {
                            callback(t('message.linkis.keyTip'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="errorRegex"
                    :placeholder="t('message.linkis.datasource.pleaseInput')"
                />
            </FFormItem>
        </FForm>
    </FModal>
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { FMessage } from '@fesjs/fes-design';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

const props = defineProps<{
    handleInitialization: Function,
}>();
const show = ref(false);
const errorCode = ref();
const errorDesc = ref();
const errorRegex = ref();
const error = ref();

const handleConfirm = async () => {
    try {
        await api.fetch("/basedata-manager/error-code", {
            id: error.value?.id || '',
            errorCode: errorCode.value,
            errorDesc: errorDesc.value,
            errorRegex: errorRegex.value,
        }, error.value ? "put" : "post");
        FMessage.success(error.value ? t("message.linkis.editedSuccess") : t('message.linkis.basedataManagement.modal.modalAddSuccess'));
        show.value = false;
        props.handleInitialization();
    } catch (error) {
        window.console.error(error);
    }
}

const open = (currentError?: any) => {
    show.value = true;
    error.value = currentError;
    errorCode.value = currentError?.errorCode;
    errorDesc.value = currentError?.errorDesc;
    errorRegex.value = currentError?.errorRegex;
};

defineExpose({
    open,
});
</script>

<style lang="less" scoped>
.modal-title {
    font-weight: bold;
}
</style>