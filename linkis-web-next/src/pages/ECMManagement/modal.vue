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
            <div class="modal-title">{{ instance.instance }}</div>
        </template>
        <FForm>
            <FFormItem
                v-for="(label, index) in labels"
                :key="index"
                :label="t('message.linkis.tableColumns.label') + (index + 1)"
                :rules="[{
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (!label.labelKey || !label.stringValue) {
                            callback(t('message.linkis.labelEmptyError'))
                        }
                        if (label.stringValue.length >= 16) {
                            callback(t('message.linkis.labelLengthLimit'))
                        }
                        if (!new RegExp(/^[^\s\u4e00-\u9fa5]*$/).test(label.stringValue)) {
                            callback(t('message.linkis.labelNoSpecialSymbol'))
                        }
                    }
                }]"
            >
                <FSelect
                    v-model="label.labelKey"
                    clearable
                    :placeholder="t('message.linkis.unselect')"
                >
                    <FOption
                        v-for="(item, index) in keyList"
                        :key="index"
                        :value="item"
                        :label="item"
                    />
                </FSelect>
                &nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;
                <FInput
                    v-model:modelValue="label.stringValue"
                    :placeholder="t('message.linkis.datasource.pleaseInput')"
                />
                &nbsp;&nbsp;&nbsp;
                <FButton @click="() => { handleRemove(index) }" type="primary">
                    {{ t('message.linkis.remove') }}
                </FButton>
            </FFormItem>
        </FForm>
        <FButton type="info" long @click="handleAdd">{{ t('message.linkis.addTags') }}</FButton>
        &nbsp;
        <FForm>
            <FFormItem
                :label="t('message.linkis.formItems.status.label')"
                :props="t('message.linkis.formItems.status.label')"
            >
                <FSelect
                    v-model="instance.nodeHealthy"
                    clearable
                    :placeholder="t('message.linkis.unselect')"
                >
                    <FOption
                        v-for="(item, index) in nodeHealthyList"
                        :key="index"
                        :value="item"
                        :label="item"
                    />
                </FSelect>
            </FFormItem>
        </FForm>
    </FModal>
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { FMessage } from '@fesjs/fes-design';
import { onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

const props = defineProps<{
    instance: {
        instance: string,
        applicationName: string,
        labels: {
            labelKey: string,
            stringValue: string,
        }[],
        nodeHealthy: string,
    },
    handleInitialization: Function,
    nodeHealthyList: string[],
}>();
const show = ref(false);
const labels = reactive<{ labelKey: string, stringValue: string }[]>([]);
const keyList = ref();

const handleAdd = () => {
    labels.push({
        labelKey: '',
        stringValue: '',
    })
}

const handleRemove = (index: number) => {
    labels.splice(index, 1);
}

const handleConfirm = async () => {
    try {
        await api.fetch("/linkisManager/modifyEMInfo", {
            instance: props.instance.instance,
            applicationName: props.instance.applicationName,
            labels,
            emStatus: props.instance.nodeHealthy,
        }, "put");
        FMessage.success(t("message.linkis.editedSuccess"));
        show.value = false;
        props.handleInitialization();
    } catch (error) {
        window.console.error(error);
    }
}

const open = () => {
    show.value = true;
};

watch(() => props.instance, () => {
    labels.length = 0;
    props.instance.labels.forEach((label: any) => {
        labels.push(label);
    })
})

onMounted(() => {
    api.fetch("/microservice/modifiableLabelKey", "get").then((res: any) => {
        keyList.value = res.keyList || [];
    });
})

defineExpose({
    open,
});
</script>

<style lang="less" scoped>
.modal-title {
    font-weight: bold;
}
</style>