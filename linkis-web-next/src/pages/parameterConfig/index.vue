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
    <FTabs @change="handleChangeTab" :modelValue="activeTabKey">
        <FTabPane v-for="menu in menuList" :key="menu.categoryName" :name="menu.categoryName" :value="menu.categoryName">
            <div class="card-list">
                <template v-for="item in childCategory" :key="`${menu.categoryName}-${item.categoryName}`">
                    <Card
                        :name="`${menu.categoryName}-${item.categoryName}`"
                        :description="item.description || t('message.linkis.noDescription')"
                        :dataList="item.fullTree"
                        :creator="menu.categoryName"
                        :engineType="item.categoryName"
                        class="card"
                    />
                </template>
            </div>
        </FTabPane>
        <template #prefix>{{ t('message.linkis.resourceManagement.applicationList') }}：</template>
        <template #suffix v-if="isLogAdmin || isHistoryAdmin">
            <div class="options">
                <FButton type="link" @click="openDrawer">
                    {{ t('message.linkis.applicationAndEngine') }}
                </FButton>
                <div class="delimiter"></div>
                <FButton type="link" @click="openModal">{{ t('message.linkis.globalConfigs') }}</FButton>
            </div>
        </template>
    </FTabs>
    <Drawer ref="drawerRef" :getCategory="getCategory" />
    <Modal ref="modalRef" />
</template>
<script setup lang="ts">
import { onMounted, Ref, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import Drawer from './drawer.vue';
import Modal from './modal.vue';
import Card from './card.vue';
import api from '@/service/api';
import { orderBy } from 'lodash';

const drawerRef = ref<Ref<{ open: () => void }> | null>(null);
const modalRef = ref<Ref<{ open: () => void }> | null>(null);
const isLogAdmin = ref<boolean>(false);
const isHistoryAdmin = ref<boolean>(false);
const activeTabKey = ref();
const menuList = ref<Array<{ categoryName: string, childCategory: Array<any> }>>([]);
const childCategory = ref<Array<{
    categoryName: string,
    description: string,
    fullTree: any,
}> | undefined>([]);
const { t } = useI18n();

const openDrawer = () => {
    drawerRef.value?.open();
};

const openModal = () => {
    modalRef.value?.open();
};

const getAppVariable = async (creator: any, engineType: any, version: any) => {
    try {
        // If there is only a first-level directory, it will directly return ['creator'], if it is a second-level directory, ['creator', 'engineType', 'version'](如果只有一级目录则直接返回['creator'],如果为二级目录则['creator', 'engineType', 'version'])
        const rst = await api.fetch(
            "/configuration/getFullTreesByAppName",
            {
                creator, // Specify a first-level directory(指定一级目录)
                engineType, // Specify the engine (secondary directory) if there is only a first-level directory, it will be automatically undefined and no parameters will be passed(指定引擎（二级目录）如果只有一级目录则自动为undefined不会传参)
                version, // The corresponding engine currently only supports the corresponding version. For example, spark will pass version-2.4.3. If there is only a first-level directory, it will be automatically undefined and no parameters will be passed.(对应的引擎目前只支持对应的版本，如spark就传version-2.4.3，如果只有一级目录则自动为undefined不会传参)
            },
            "get"
        );
        rst.settings = [];
        rst.fullTree.forEach((item: any) => {
            item.settings = orderBy(item.settings, ["level"], ["asc"]);
            if (item.settings.length) {
                if (creator !== '全局设置' && creator !== 'GlobalSettings') {
                    item.settings = item.settings.filter((set: any) => set.engineType)
                }
                item.settings.forEach((set: any) => {
                    if (set.validateType === "OFT") {
                        set.validateRangeList = formatValidateRange(
                            set.validateRange,
                            set.key
                        );
                    }
                    if (
                        set.key === "spark.application.pyFiles" ||
                        set.key === "python.application.pyFiles"
                    ) {
                        set.placeholder = t("message.linkis.placeholderZip");
                    }
                    rst.settings.push(set)
                });
            }
        })
        return rst;
    } catch (error) {
        window.console.error(error);
    }
}

const formatValidateRange = (value: any, type: string) => {
    let formatValue: any = [];
    let tmpList = [];
    try {
        tmpList = JSON.parse(value);
    } catch (e) {
        tmpList = value.slice(1, value.length - 1).split(",");
    }
    tmpList.forEach((item: string) => {
        formatValue.push({
            name:
                item === "BLANK" && type === "pipeline.out.null.type"
                    ? t("message.linkis.emptyString")
                    : item,
            value: item,
        });
    });
    return formatValue;
}

const handleChangeTab = (menu: any) => {
    childCategory.value = menuList.value.find((v: any) => v.categoryName === menu)?.childCategory;
    activeTabKey.value = menu;
}

const getCategory = async () => {
    childCategory.value = [];
    // get settings directory(获取设置目录)
    const rst = await api.fetch("/configuration/getCategory", "get");
    menuList.value = rst.Category.filter((menu: any) => menu.categoryName !== 'GlobalSettings') || [];
    await Promise.all(menuList.value.map((menu: any) => Promise.all(menu.childCategory.map(async (category: any) => {
        const arr = category.categoryName.split('-');
        const res = await getAppVariable(menu.categoryName, arr[0], arr[1]);
        category.fullTree = res.fullTree;
    }))))
    handleChangeTab(menuList.value?.[0].categoryName)
}

onMounted(async () => {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    const res = await api.fetch('/jobhistory/governanceStationAdmin', 'get');
    isLogAdmin.value = res.admin;
    isHistoryAdmin.value = res.historyAdmin;
    await getCategory();
})
</script>
<style lang="less" scoped>
.card-list {
    margin-top: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    .card {
        width: calc(33.7% - 16px);
        min-width: 280px;
        overflow: auto;
    }
}
.options {
    display: flex;
    position: absolute;
    top: 0;
    right: -10px;
    align-items: center;
    .delimiter {
        background: #d8d8d8;
        width: 1px;
        height: 14px;
        transform: translateY(1px);
    }
}
:deep(.fes-tabs-nav-wrapper--after::after) {
    box-shadow: none;
}
</style>
