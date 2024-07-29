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
    <FDrawer v-model:show="show" width="960" :footer="true">
        <template #title>
            <div class="header-title">
                <div v-if="isAddingApplicationAndEngine">
                    <LeftOutlined class="back-icon" @click="isAddingApplicationAndEngine = false" />
                    {{ $t('message.linkis.addApplicationAndEngine') }}
                </div>
                <div v-else-if="isEditingApplicationAndEngine">
                    <LeftOutlined class="back-icon" @click="isEditingApplicationAndEngine = false" />
                    {{ $t('message.linkis.editApplicationAndEngine') }}
                </div>
                <div v-else>
                    {{ $t('message.linkis.applicationAndEngine') }}
                </div>
            </div>
        </template>
        <template v-if="!isAddingApplicationAndEngine && !isEditingApplicationAndEngine">
            <FButton type="primary" style="margin: 8px 0 24px" @click="handleAddApplicationAndEngine">
                {{ $t('message.linkis.addApplicationAndEngine') }}
            </FButton>
        </template>
        <div class="content-wrapper">
            <template v-if="isEditingApplicationAndEngine || isAddingApplicationAndEngine">
                <FForm :labelWidth="72">
                    <FFormItem
                        :label="$t('message.linkis.applicationName')"
                        style="color: #0f1222"
                        :rules="[{
                            required: true,
                            trigger: ['change', 'blur'],
                            message: t('message.linkis.keyTip'),
                            validator: () => trim(tmpData.categoryName).length !== 0
                        }]"
                    >
                        <FInput
                            :placeholder="t('message.linkis.datasource.pleaseInput')"
                            v-model="tmpData.categoryName"
                            :disabled="isEditingApplicationAndEngine"
                        />
                    </FFormItem>
                    <FFormItem :label="$t('message.linkis.applicationDesc')" style="color: #0f1222">
                        <FInput :placeholder="t('message.linkis.datasource.pleaseInput')" v-model="tmpData.description" />
                    </FFormItem>
                    <FFormItem :label="$t('message.linkis.engineConfig')" style="color: #0f1222">
                        <div class="configs">
                            <template v-for="(cate, index) in tmpData.childCategory" :key="cate">
                                <div class="config-card">
                                    <div class="card-header">
                                        <div class="card-title">
                                            {{ `${t('message.linkis.formItems.engine.label')}${(index + 1).toString().padStart(2, '0')}` }}
                                        </div>
                                        <div class="delete-btn" @click="() => { deleteEngine(index) }">{{ t('message.common.delete') }}</div>
                                    </div>
                                    <FFormItem
                                        :label="t('message.linkis.tableColumns.engineType')"
                                        :rules="[{
                                            required: true,
                                            trigger: ['change', 'blur'],
                                            message: t('message.linkis.keyTip'),
                                            validator: () => engineTypes.some((v: any) => v.value === cate.engineType)
                                        }]"
                                    >
                                        <FSelect
                                            v-model="cate.engineType"
                                            :options="engineTypes"
                                            valueField="value"
                                            labelField="name"
                                            @change="handleChangeEngineType"
                                        />
                                    </FFormItem>
                                    <FFormItem
                                        :label="t('message.linkis.tableColumns.engineVersion')"
                                        :rules="[{
                                            required: true,
                                            trigger: ['change', 'blur'],
                                            message: t('message.linkis.keyTip'),
                                            validator: () => engineTypeVersions[cate.engineType].some((v: any) => v.value === cate.version)
                                        }]"
                                    >
                                        <FSelect
                                            v-model="cate.version"
                                            :options="engineTypeVersions[cate.engineType]"
                                            valueField="value"
                                            labelField="name"
                                        />
                                    </FFormItem>
                                    <FFormItem :label="t('message.linkis.description')">
                                        <FInput
                                            :placeholder="t('message.linkis.datasource.pleaseInput')"
                                            v-model="cate.description"
                                        />
                                    </FFormItem>
                                </div>
                            </template>
                            <FButton type="link" class="engine-adding-btn" @click="addEngine">
                                <template #icon>
                                    <PlusCircleOutlined />
                                </template>
                                {{ t('message.linkis.addEngineType') }}
                            </FButton>
                        </div>
                    </FFormItem>
                </FForm>
            </template>
            <template v-else>
                <f-table
                    :data="dataList"
                    class="table"
                    :rowKey="(row: Record<string, number | string>) => row.categoryId"
                    :emptyText="t('message.linkis.noDataText')"
                >
                    <template v-for="col in tableColumns" :key="col.label">
                        <f-table-column
                            v-if="col.formatter"
                            :prop="col.prop"
                            :label="col.label"
                            :formatter="col.formatter"
                            :width="col?.width"
                        />
                        <f-table-column
                            v-else
                            :prop="col.prop"
                            :label="col.label"
                            :width="col?.width"
                            :action="col.action"
                        />
                    </template>
                </f-table>
                <FPagination
                    show-size-changer
                    show-total
                    :total-count="menuList.length"
                    v-model:currentPage="currentPage"
                    @change="handleChangePagination"
                    :pageSizeOption="[10, 20, 30, 50, 100]"
                    v-model:pageSize="pageSize"
                />
            </template>
        </div>
        <template #footer>
            <FSpace v-if="isAddingApplicationAndEngine || isEditingApplicationAndEngine">
                <FButton type="primary" @click="confirmEditing">{{ $t('message.common.submit') }}</FButton>
                <FButton @click="show = false">{{ $t('message.common.cancel') }}</FButton>
            </FSpace>
        </template>
    </FDrawer>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import { LeftOutlined, PlusCircleOutlined } from '@fesjs/fes-design/icon';
import { FButton, FMessage, FModal, FSelect } from '@fesjs/fes-design';
import { useI18n } from 'vue-i18n';
import dayjs from 'dayjs';
import api from '@/service/api';
import { cloneDeep, trim } from 'lodash';
const { t } = useI18n();

const props = defineProps<{
    getCategory: Function
}>();

const show = ref(false);
const menuList = ref<Array<{ categoryName: string, childCategory: Array<any> }>>([]);
const dataList = ref<Array<{ categoryName: string, childCategory: Array<any> }>>([]);
const isAddingApplicationAndEngine = ref(false);
const isEditingApplicationAndEngine = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const engineTypes = ref([]);
const engineTypeVersions = ref<Record<string, any>>({});
const tmpData = ref<{
    categoryName?: string,
    description?: string,
    childCategory?: any,
    categoryId?: number,
}>({});
const tmpDataBeforeEditing = ref<{
    categoryName?: string,
    description?: string,
    childCategory?: any,
    categoryId?: number,
}>({});

const handleAddApplicationAndEngine = () => {
    tmpData.value = {
        categoryName: '',
        description: '',
        childCategory: [{
            engineType: '',
            version: '',
            description: '',
        }]
    }
    isAddingApplicationAndEngine.value = true;
}

const handleEditApplicationAndEngine = (row: any) => {
    tmpData.value = row;
    tmpData.value.childCategory.forEach((cate: any) => {
        cate.engineType = cate.categoryName.split('-')?.[0];
        cate.version = cate.categoryName.split('-')?.[1];
    })
    tmpDataBeforeEditing.value = cloneDeep(tmpData.value);
    isEditingApplicationAndEngine.value = true;
}

const handleDeleteCreator = (row: any) => {
    FModal.confirm({
        title: t('message.common.userMenu.title'),
        content: t('message.linkis.confirmToDeleteCreator'),
        closable: true,
        onOk: async () => {
            await api.fetch("/configuration/deleteCategory", { categoryId: row.categoryId }, "post")
            FMessage.success(t('message.linkis.basedataManagement.engineConfigurationTemplate.delSuccess'));
            await refresh();
        }
    });
}

const tableColumns = [
    {
        prop: 'categoryId',
        label: t('message.linkis.id'),
        width: 60,
    },
    {
        prop: 'categoryName',
        label: t('message.linkis.applicationName'),
        width: 140,
    },
    {
        prop: 'description',
        label: t('message.linkis.description'),
        width: 200,
        formatter: ({ row }: { row: Record<string, number | string> }) => row.description || t('message.linkis.noDescription'),
    },
    {
        prop: 'childCategory',
        label: t('message.linkis.engineNumber'),
        width: 100,
        formatter: ({ row }: { row: { childCategory: Array<any> } }) => row.childCategory?.length,
    },
    {
        prop: 'createTime',
        label: t('message.linkis.tableColumns.createdTime'),
        width: 100,
        formatter: ({ row }: { row: { createTime: number } }) => dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
        prop: 'updateTime',
        label: t('message.linkis.tableColumns.updateTime'),
        width: 100,
        formatter: ({ row }: { row: { updateTime: number } }) => dayjs(row.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
        // 在“操作”两个字前添加空格，使其能与“查看”上下对齐
        label: `\u00A0\u00A0\u00A0\u00A0${t('message.linkis.columns.control.title')}`,
        width: 140,
        action: [
            {
                label: t('message.linkis.edit'),
                func: handleEditApplicationAndEngine,
            },
            {
                label: h('span', { style: { color: '#F75F56' }}, t('message.common.delete')),
                func: handleDeleteCreator,
            },
        ],
    },
];

// eslint-disable-next-line no-shadow
const handleChangePagination = (currentPage: number, pageSize: number) => {
    const temp = menuList.value.slice(
        (currentPage - 1) * pageSize,
        currentPage * pageSize,
    );
    dataList.value = [...temp] as any;
};

// Make sure the version is matched to the engineType
const handleChangeEngineType = (type: any) => {
    tmpData.value.childCategory.forEach((cate: any) => {
        if (cate.engineType === type) {
            cate.version = engineTypeVersions.value[type][0].value;
        }
    })
}

const open = () => {
    show.value = true;
};

const deleteEngine = (index: number) => {
    tmpData.value.childCategory.splice(index, 1);
}

const addEngine = () => {
    tmpData.value.childCategory.push({
        engineType: '',
        version: '',
        description: '',
    })
};

const confirmEditing = () => {
    tmpData.value.categoryName = trim(tmpData.value.categoryName);
    // 判断引擎类型和引擎版本是否为空，若为空则通过错误信息提示用户，且阻断后续逻辑
    let isBlank = false;
    tmpData.value.childCategory.forEach((cate: any) => {
        if(cate.engineType === '' || cate.version === '' || !cate.version) {
            FMessage.error(t('message.linkis.tableColumns.engineVersionCannotBeNull'));
            isBlank = true;
        }
    })
    if (isBlank) return;
    // 判断当前的引擎列表中是否有重复的引擎，若存在重复引擎则通过错误信息提示用户，且阻断后续逻辑
    const distinctEngineTypeList = [...new Set(tmpData.value.childCategory.map((cate: any) => cate.engineType))];
    if (distinctEngineTypeList.length !== tmpData.value.childCategory.length) {
        FMessage.error(t('message.linkis.nonRepetitiveEngines'))
        return;
    }
    // 添加新应用时判断应用名称是否为空，若为空则通过错误信息提示用户，且阻断后续逻辑
    if (isAddingApplicationAndEngine.value) {
        if (tmpData.value.categoryName.length === 0) {
            FMessage.error(t('message.linkis.tableColumns.addApplicationRules'));
            return;
        }
    }
    FModal.confirm({
        title: t('message.common.userMenu.title'),
        content: t('message.linkis.confirmToModifyCreator'),
        closable: true,
        onOk: async () => {
            if (isAddingApplicationAndEngine.value) {
                // 添加新应用时，先创建应用
                try {
                    await api.fetch("/configuration/createFirstCategory", {
                        categoryName: tmpData.value.categoryName,
                        description: tmpData.value.description,
                    }, "post")
                    FMessage.success(t('message.linkis.successfullyAddCreator'));
                } catch (error) {
                    FMessage.error(t('message.linkis.failedToAddCreator'));
                    return;
                }
                // 应用创建成功后，获取应用的categoryId
                let categoryId;
                try {
                    const res = await api.fetch("/configuration/getCategory", "get");
                    categoryId = res.Category.find((v: any) => v.categoryName === tmpData.value.categoryName).categoryId;
                } catch (error) {
                    FMessage.error(t('message.linkis.failedToGetCategoryId'))
                    await refresh();
                    return;
                }
                // 获取categoryId后，依次创建引擎列表中的各个引擎
                for (const cate of tmpData.value.childCategory) {
                    try {
                        await api.fetch("/configuration/createSecondCategory", {
                            categoryId,
                            engineType: cate.engineType,
                            version: cate.version,
                            description: cate.description,
                        }, "post")
                        FMessage.success(cate.engineType + cate.version + t('message.linkis.successfullyAddEngine'));
                    } catch (error) {
                        FMessage.error(cate.engineType + cate.version + t('message.linkis.failedToAddEngine'));
                    }
                }
            }
            if (isEditingApplicationAndEngine.value) {
                // 编辑应用时，先判断是否需要更改应用描述
                if(tmpData.value.description !== tmpDataBeforeEditing.value.description) {
                    try {
                        await api.fetch("configuration/updateCategoryInfo", {
                            categoryId: tmpData.value.categoryId,
                            description: tmpData.value.description
                        }, "post")
                        FMessage.success(t('message.linkis.successfullyUpdateCreator'));
                    } catch (error) {
                        FMessage.error(t('message.linkis.failedToUpdateCreator'));
                    }
                }
                const addList = [] as any[]; // 需要添加的引擎列表
                const modifyList = [] as any[]; // 需要修改的引擎列表
                // 对现有引擎列表中的所有引擎进行分类
                tmpData.value.childCategory.forEach((cate: any) => {
                    const originCateIndex = tmpDataBeforeEditing.value.childCategory.findIndex((v: any) => v.engineType === cate.engineType);
                    if (originCateIndex === -1) {
                        // 将原始列表中没有的引擎放入addList中
                        addList.push(cate);
                    } else {
                        const originCate = tmpDataBeforeEditing.value.childCategory[originCateIndex];
                        if (cate.version === originCate.version) {
                            // 原始列表中的引擎版本与现有列表中的引擎版本一致且描述发生了修改，就把该引擎从原始列表中剔除，放到modifyList中
                            tmpDataBeforeEditing.value.childCategory.splice(originCateIndex, 1);
                            if (cate.description !== originCate.description) {
                                modifyList.push(cate);
                            }
                        } else {
                            // 原始列表中的引擎版本与现有列表中的引擎版本不一致，直接将该引擎加入addList中，作为新引擎安装
                            addList.push(cate);
                        }
                    }
                })
                // 经过上述操作，原始列表tmpDataBeforeEditing.value.childCategory中只会保留需要被删除的引擎，将这些引擎遍历删除
                for (const cateToDelete of tmpDataBeforeEditing.value.childCategory) {
                    try {
                        await api.fetch("/configuration/deleteCategory", { categoryId: cateToDelete.categoryId }, "post")
                        FMessage.success(
                            cateToDelete.engineType +
                            cateToDelete.version +
                            t('message.linkis.basedataManagement.engineConfigurationTemplate.delSuccess')
                        );
                    } catch (error) {
                        FMessage.error(
                            cateToDelete.engineType +
                            cateToDelete.version +
                            t('message.linkis.basedataManagement.engineConfigurationTemplate.delFail')
                        );
                    }
                }
                // 遍历添加addList中的引擎
                for (const cateToAdd of addList) {
                    try {
                        await api.fetch("/configuration/createSecondCategory", {
                            categoryId: tmpData.value.categoryId,
                            engineType: cateToAdd.engineType,
                            version: cateToAdd.version,
                            description: cateToAdd.description,
                        }, "post")
                        FMessage.success(cateToAdd.engineType + cateToAdd.version + t('message.linkis.successfullyAddEngine'));
                    } catch (error) {
                        FMessage.error(cateToAdd.engineType + cateToAdd.version + t('message.linkis.failedToAddEngine'));
                    }
                }
                // 遍历修改modifyList中的引擎
                for (const cateToModify of modifyList) {
                    try {
                        await api.fetch("configuration/updateCategoryInfo", {
                            categoryId: cateToModify.categoryId,
                            description: cateToModify.description
                        }, "post")
                        FMessage.success(cateToModify.engineType + cateToModify.version + t('message.linkis.successfullyUpdateEngine'));
                    } catch (error) {
                        FMessage.error(cateToModify.engineType + cateToModify.version + t('message.linkis.failedToUpdateEngine'));
                    }
                }
            }
            // 完成编辑操作后刷新页面
            await refresh();
        }
    });
};

const refresh = async () => {
    isAddingApplicationAndEngine.value = false;
    isEditingApplicationAndEngine.value = false;
    show.value = false;
    tmpData.value = {};
    tmpDataBeforeEditing.value = {};
    await props.getCategory();
    handleInitialization();
}

const handleInitialization = async () => {
    // Get settings directory(获取设置目录)
    const cateRes = await api.fetch("/configuration/getCategory", "get");
    menuList.value = cateRes.Category.filter((menu: any) => menu.categoryName !== 'GlobalSettings') || [];
    handleChangePagination(currentPage.value, pageSize.value)
    // Get all engine types(获取所有引擎类型)
    const engineTypeRes = await api.fetch("/configuration/engineType", "get");
    engineTypes.value = engineTypeRes.engineType.map((type: string) => ({ name: type, value: type }));
    // Get all engine type versions(获取所有引擎版本)
    engineTypeRes.engineType.forEach(async (type: string) => {
        const engineTypeVersionRes = await api.fetch(`/engineplugin/getTypeVersionList/${type}`, {}, "get")
        engineTypeVersions.value[type] = (engineTypeVersionRes.queryList || []).map((item: string) => {
            if (/^v/.test(item)) {
                const version = item.replace(/v/, '');
                return { name: version, value: version };
            } else {
                return { name: item, value: item };
            }
        });
    })
}

onMounted(() => {
    handleInitialization();
})

defineExpose({
    open,
});
</script>

<style scoped lang="less">
.header-title {
    font-weight: bold;
    .back-icon {
        color: #93949b;
        cursor: pointer;
        transform: translate(-4px, 2px);
    }
}

.content-wrapper {
    width: 960px;
    .configs {
        width: 100%;
        .config-card {
            width: 100%;
            background: #ffffff;
            border: 1px solid #cfd0d3;
            border-radius: 4px;
            padding: 16px 16px 0;
            margin-bottom: 16px;
            .card-header {
                font-size: 16px;
                display: flex;
                justify-content: space-between;
                .card-title {
                    font-size: 14px;
                    color: #0f1222;
                    line-height: 22px;
                    margin-bottom: 16px;
                }
                .delete-btn {
                    font-size: 14px;
                    color: #63656f;
                    line-height: 22px;
                    font-weight: 400;
                    cursor: pointer;
                }
            }
        }
        .engine-adding-btn {
            transform: translate(-15px, -5px);
        }
    }
    .table {
        overflow: auto;
        max-height: calc(75vh - 100px);
    }
}

:deep(.fes-pagination) {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
}
</style>
