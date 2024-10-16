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
            <div class="modal-title">{{ user ? t('message.linkis.edit') : t('message.linkis.basedataManagement.addUDFAdmin') }}</div>
        </template>
        <FForm>
            <FFormItem
                :label="t('message.linkis.userName')"
                :props="t('message.linkis.userName')"
                :rules="[{
                    required: true,
                    trigger: ['change', 'blur'],
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    validator: (rule, value, callback) => {
                        if (userName.length < 1) {
                            callback(t('message.linkis.basedataManagement.udfManager.userNameValidate.empty'))
                        }
                        if (userName.length > 20) {
                            callback(t('message.linkis.basedataManagement.udfManager.userNameValidate.size'))
                        }
                    }
                }]"
            >
                <FInput
                    v-model:modelValue="userName"
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
const userName = ref();
const user = ref();

const handleConfirm = async () => {
    try {
        await api.fetch("/basedata-manager/udf-manager", {
            id: user.value?.id || '',
            userName: userName.value,
        }, user.value ? "put" : "post");
        FMessage.success(t("message.linkis.editedSuccess"));
        show.value = false;
        props.handleInitialization();
    } catch (error) {
        window.console.error(error);
    }
}

const open = (currentUser?: any) => {
    show.value = true;
    user.value = currentUser;
    userName.value = currentUser?.userName;
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