<template>
    <div class="label">{{ $t('message.linkis.globalTaskQuery') }}</div>
    <div class="filter">
        <FForm ref="formRef" class="form" v-show="!isCheckingTaskToStop">
            <FFormItem :label="$t('message.linkis.jobId')" :props="$t('message.linkis.jobId')">
                <FInput v-model="formData.id" placeholder="请输入" class="short-form-item"></FInput>
            </FFormItem>

            <FFormItem :label="$t('message.linkis.codeQuery.status')" :props="$t('message.linkis.codeQuery.status')">
                <FSelect v-model="formData.status" clearable placeholder="请选择" class="short-form-item">
                    <FOption v-for="(item, index) in statusList" :key="index" :value="item.value" :label="$t(item.label)">
                    </FOption>
                </FSelect>
            </FFormItem>

            <FFormItem :label="$t('message.common.resourceSimple.YQ')" :props="$t('message.common.resourceSimple.YQ')">
                <FSelect v-model="formData.engine" clearable placeholder="请选择" class="short-form-item">
                    <FOption v-for="(item, index) in optionList" :key="index" :value="item.value" :label="$t(item.label)">
                    </FOption>
                </FSelect>
            </FFormItem>

            <FFormItem :label="$t('message.linkis.codeQuery.createdTime')"
                :prop="$t('message.linkis.codeQuery.createdTime')" class="long-form-item">
                <FDatePicker v-model="formData.dateRange" type="daterange" :modelValue="[
                    Date.now(),
                    Date.now() + 7 * 24 * 60 * 60 * 1000,
                ]">
                    <template #separator> - </template>
                </FDatePicker>
            </FFormItem>

            <FFormItem :label="$t('message.linkis.datasource.creator')" :props="$t('message.linkis.datasource.creator')">
                <FSelect v-model="formData.creator" clearable placeholder="请选择" class="short-form-item">
                    <FOption v-for="(item, index) in optionList" :key="index" :value="item.value" :label="$t(item.label)">
                    </FOption>
                </FSelect>
            </FFormItem>

            <div class="buttons">
                <template v-if="!isCheckingTaskToStop">
                    <FButton type="primary" @click="search">{{ $t('message.linkis.find') }}</FButton>
                    <FButton>{{ $t('message.linkis.EnginePluginManagement.Reset') }}</FButton>
                    <FButton class="stop-button" @click="isCheckingTaskToStop = true">
                        {{ $t('message.linkis.batchStopping') }}
                    </FButton>
                </template>
            </div>
        </FForm>
        <template v-if="isCheckingTaskToStop">
            <div class="buttons">
                <FButton type="primary" class="confirm-stop-button" @click="confirmStop">
                    {{ $t('message.linkis.confirmToStop') }}
                </FButton>
                <FButton class="confirm-stop-button" @click="isCheckingTaskToStop = false">
                    {{ $t('message.linkis.cancel') }}
                </FButton>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import api from '@/service/api';
import { FForm } from '@fesjs/fes-design';
import { ref, onMounted, reactive } from 'vue';
const emits = defineEmits(['search']);
const isCheckingTaskToStop = ref(false);
const formRef = ref<null | typeof FForm>(null);

const formData = reactive({
    id: '',
    status: '',
    engine: '',
    dateRange: '',
    creator: ''
});

const optionList = ref([
    {
        value: 'xxx',
        label: 'xxx',
    },
    {
        value: 'yyy',
        label: 'yyy',
    },
    {
        value: 'zzz',
        label: 'zzz',
    },
]);
const statusList = ref([
    {
        label: 'message.linkis.statusType.all',
        value: 'all'
    },
    {
        label: 'message.linkis.statusType.inited',
        value: 'Inited'
    },
    {
        label: 'message.linkis.statusType.running',
        value: 'Running'
    },
    {
        label: 'message.linkis.statusType.succeed',
        value: 'Succeed'
    },
    {
        label: 'message.linkis.statusType.cancelled',
        value: 'Cancelled'
    },
    {
        label: 'message.linkis.statusType.failed',
        value: 'Failed'
    },
    {
        label: 'message.linkis.statusType.scheduled',
        value: 'Scheduled'
    },
    {
        label: 'message.linkis.statusType.timeout',
        value: 'Timeout'
    }
]);

// const props = defineProps<{
//     checkedKeys: Array<string>;
// }>();

const search = async () => {
    // const formData = await formRef?.value?.validate()
    console.log('formData', formData);
    emits('search');
};

const confirmStop = () => {
    console.log(1);
};


onMounted(() => {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    api.fetch('/configuration/engineType', 'get').then(() => {
        // engine config selector list
        // this.getEngineTypes = ['all', ...res.engineType];
    });
});


defineExpose({
    isCheckingTaskToStop,
});
</script>

<style scoped src="./index.less"></style>
<style scoped>
.filter {
    position: relative;
    height: 54px;

    .form {
        display: flex;
        gap: 22px;

        .short-form-item {
            width: 120px;
        }

        .long-form-item {
            width: 314px;
        }
    }

    .buttons {
        flex: 1;
        display: flex;
        justify-content: flex-start;
        gap: 16px;

        .stop-button {
            margin-left: auto;
        }
    }
}
</style>
