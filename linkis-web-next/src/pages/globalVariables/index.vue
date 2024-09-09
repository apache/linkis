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
    <div class="btn-group">
        <FButton type="primary" @click="handleEdit" v-if="!isEditing">
            {{ t('message.linkis.edit') }}
        </FButton>
        <FButton type="primary" @click="handleSave" v-if="isEditing">
            {{ t('message.linkis.save') }}
        </FButton>
        <FButton @click="handleCancel" v-if="isEditing">
            {{ t('message.linkis.cancel') }}
        </FButton>
    </div>
    <FForm>
        <FFormItem
            v-for="(variable, index) in globalVariables"
            :key="index"
            :label="t('message.linkis.globalVariable') + (index + 1)"
            :rules="[{
                trigger: ['change', 'blur'],
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                validator: (rule, value, callback) => {
                    if (variable.key.length === 0) {
                        callback(t('message.linkis.rules.first.required'))
                    }
                    if (variable.value.length === 0) {
                        callback(t('message.linkis.rules.second.required'))
                    }
                    if (!new RegExp(/^[a-zA-z][^\s\u4e00-\u9fa5]*$/).test(variable.key)) {
                        callback(t('message.linkis.rules.first.letterTypeLimit'))
                    }
                    if (globalVariables.findIndex(v => v.key === variable.key) !== index) {
                        callback(t('message.linkis.sameName'))
                    }
                }
            }]"
        >
            <FInput
                v-model:modelValue="variable.key"
                :placeholder="t('message.linkis.rules.first.placeholder')"
                :disabled="!isEditing"
            />
            &nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;
            <FInput
                v-model:modelValue="variable.value"
                :placeholder="t('message.linkis.rules.second.placeholder')"
                :disabled="!isEditing"
            />
            <FButton @click="() => { handleRemove(index) }" v-if="isEditing" class="remove-btn" type="primary">
                {{ t('message.linkis.remove') }}
            </FButton>
        </FFormItem>
    </FForm>
    <FButton type="info" long @click="handleAdd" v-if="isEditing">{{ t('message.linkis.addArgs') }}</FButton>
</template>

<script setup lang="ts">
import api from '@/service/api';
import { FMessage } from '@fesjs/fes-design';
import { onMounted, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

let globalVariables = reactive<{ key: string, value: string }[]>([]);
let originGlobalVariables = reactive<{ key: string, value: string }[]>([]);
const isEditing = ref(false);


const getGlobalVariableList = async () => {
    const res = await api.fetch('/variable/listGlobalVariable', 'get');
    globalVariables.length = 0;
    res.globalVariables.forEach((v: any) => {
        globalVariables.push(v);
    })
}

const handleEdit = () => {
    isEditing.value = true;
    originGlobalVariables.length = 0;
    globalVariables.forEach((v: any) => {
        originGlobalVariables.push(v);
    });
}

const handleSave = async () => {
    try {
        await api.fetch('/variable/saveGlobalVariable ', { globalVariables });
        FMessage.success(t('message.linkis.success.update'));
    } catch (error) {
        window.console.error(error);
    } finally {
        isEditing.value = false;
        getGlobalVariableList();
    }
}

const handleCancel = () => {
    isEditing.value = false;
    globalVariables.length = 0;
    originGlobalVariables.forEach((v: any) => {
        globalVariables.push(v);
    });
}

const handleRemove = (index: number) => {
    globalVariables.splice(index, 1);
}

const handleAdd = () => {
    globalVariables.push({
        key: '',
        value: '',
    })
}

onMounted(() => {
    getGlobalVariableList();
})
</script>

<style lang="less" scoped>
.btn-group {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
}

.remove-btn {
    margin-left: 20px;
}
</style>