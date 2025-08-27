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
    <FForm layout="inline" :span="4">
        <FFormItem
            :label="t('message.linkis.userName')"
            :props="t('message.linkis.userName')"
        >
            <FInput
                v-model="userName"
                :placeholder="t('message.linkis.datasource.pleaseInput')"
            />
        </FFormItem>
        <FFormItem>
            <div class="btns">
                <FButton type="primary" @click="() => { search() }">
                    {{ t('message.linkis.find') }}
                </FButton>
                <FButton type="info" @click="handleAdd">
                    {{ t('message.common.add') }}
                </FButton>
            </div>
        </FFormItem>
    </FForm>
    <f-table
        :data="dataList"
        class="table"
        :rowKey="(row: Record<string, number | string>) => row.id"
        :emptyText="t('message.linkis.noDataText')"
    >
        <template v-for="col in tableColumns" :key="col.label">
          <f-table-column
              v-if="col.formatter"
              :prop="col.prop"
              :label="col.label"
              :formatter="col.formatter"
          />
          <f-table-column
              v-else
              :prop="col.prop"
              :label="col.label"
              :action="col.action"
          />
        </template>
    </f-table>
    <FPagination
        class="pagination"
        show-size-changer
        show-total
        :total-count="totalSize"
        v-model:currentPage="currentPage"
        @change="handleChangePagination"
        :pageSizeOption="[10, 20, 30, 50, 100]"
        v-model:pageSize="pageSize"
    />
    <Modal ref="modalRef" :handleInitialization="handleInitialization" />
</template>

<script lang="ts" setup>
import api from "@/service/api";
import { FMessage, FModal } from "@fesjs/fes-design";
import { onMounted, reactive, ref } from "vue";
import Modal from "./modal.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const modalRef = ref();
const userName = ref("");
const dataList = ref();
const currentPage = ref(1);
const pageSize = ref(10);
const totalSize = ref(0);

const handleAdd = () => {
  modalRef.value?.open();
};

const handleEdit = (user: any) => {
  modalRef.value?.open(user);
};

const handleDelete = (user: any) => {
  FModal.confirm({
    title: t("message.linkis.basedataManagement.modal.modalTitle"),
    content: t('message.linkis.basedataManagement.modal.modalDelete', {name: user.userName}),
    onOk: () => {
      api.fetch(`/basedata-manager/udf-manager/${user.id}`, "delete").then(() => {
        FMessage.success(t("message.linkis.basedataManagement.modal.modalDeleteSuccess"));
        search();
      }).catch((err: any) => {
        window.console.error(err);
      });
    }
  });
};

const tableColumns = reactive<{ prop?: string, label: string, action?: any, formatter?: any }[]>([
  {
    prop: "id",
    label: "ID",
  },
  {
    prop: "userName",
    label: t("message.linkis.userName"),
  },
  {
    label: `\u00A0\u00A0\u00A0\u00A0${t('message.linkis.columns.control.title')}`,
    action: [
      {
        label: t('message.linkis.edit'),
        func: (user: any) => {
          handleEdit(user);
        },
      },
      {
        label: t('message.common.delete'),
        func: (user: any) => {
          handleDelete(user);
        },
      },
    ]
  },
]);

const search = (currentPage = 1) => {
  api.fetch("/basedata-manager/udf-manager", {
    searchName: userName.value,
    currentPage,
    pageSize: pageSize.value,
  }, "get").then((res: any) => {
    dataList.value = [...res.list.list] || [];
    totalSize.value = res.list.total;
  });
};

const handleChangePagination = () => {
  search(currentPage.value);
};

const handleInitialization = () => {
  search();
};

onMounted(() => {
  handleInitialization();
});
</script>

<style lang="less" scoped>
.btns {
    display: flex;
    gap: 12px;
}
.pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
}
</style>