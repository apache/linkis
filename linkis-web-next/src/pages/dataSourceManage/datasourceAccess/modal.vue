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
            <div class="modal-title">{{ access ? t('message.linkis.edit') : t('message.common.add') }}</div>
        </template>
        <FForm>
            <FFormItem
                :label="t('message.linkis.basedataManagement.tableId')"
                :props="t('message.linkis.basedataManagement.tableId')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (tableId.length < 1) {
                            callback(t('message.linkis.keyTip'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="tableId"
                    :placeholder="t('message.linkis.datasource.pleaseInput')"
                />
            </FFormItem>
            <FFormItem
                :label="t('message.linkis.basedataManagement.visitor')"
                :props="t('message.linkis.basedataManagement.visitor')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (visitor.length < 1) {
                            callback(t('message.linkis.keyTip'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="visitor"
                    :placeholder="t('message.linkis.datasource.pleaseInput')"
                />
            </FFormItem>
            <FFormItem
                :label="t('message.linkis.basedataManagement.field')"
                :props="t('message.linkis.basedataManagement.field')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (fields.length < 1) {
                            callback(t('message.linkis.keyTip'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="fields"
                    :placeholder="t('message.linkis.datasource.pleaseInput')"
                />
            </FFormItem>
            <FFormItem
                :label="t('message.linkis.basedataManagement.applicationId')"
                :props="t('message.linkis.basedataManagement.applicationId')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (applicationId.length < 1) {
                            callback(t('message.linkis.keyTip'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="applicationId"
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
const tableId = ref();
const visitor = ref();
const fields = ref();
const applicationId = ref();
const access = ref();

const handleConfirm = async () => {
    try {
        await api.fetch("/basedata-manager/datasource-access", {
            id: access.value?.id || '',
            tableId: tableId.value,
            visitor: visitor.value,
            fields: fields.value,
            applicationId: applicationId.value,
        }, access.value ? "put" : "post");
        FMessage.success(access.value ? t("message.linkis.editedSuccess") : t('message.linkis.basedataManagement.modal.modalAddSuccess'));
        show.value = false;
        props.handleInitialization();
    } catch (error) {
        window.console.error(error);
    }
}

const open = (currentAccess?: any) => {
    show.value = true;
    access.value = currentAccess;
    tableId.value = currentAccess?.tableId;
    visitor.value = currentAccess?.visitor;
    fields.value = currentAccess?.fields;
    applicationId.value = currentAccess?.applicationId;
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