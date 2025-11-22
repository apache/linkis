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
  <div class="tenantManagement">
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <Row class="search-bar">
      <Col span="6" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.tenantTagManagement.department')">{{$t('message.linkis.tenantTagManagement.department')}}</span>
        <Select
          v-model="queryData.department"
          style="width: calc(100% - 66px);"
          class="input"
          :placeholder="$t('message.linkis.tenantTagManagement.selectDepartment')"
          filterable
          clearable
        >
          <Option v-for="item in departments" :value="item.label" :key="item.label" :label="item.label"/>
        </Select>
      </Col>
      <Col span="6" class="search-item">
        <span :title="$t('message.linkis.tenantTagManagement.appName')" :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}">{{$t('message.linkis.tenantTagManagement.appName')}}</span>
        <Input
          v-model="queryData.creator"
          class="input"
          :placeholder="$t('message.linkis.tenantTagManagement.inputApp')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="6" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}">{{$t('message.linkis.tenantTagManagement.tenantTag')}}</span>
        <Input
          v-model="queryData.tenantValue"
          class="input"
          :placeholder="$t('message.linkis.tenantTagManagement.inputTenant')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="6">
        <Button type="primary" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="search">{{
          $t('message.linkis.tenantTagManagement.search')
        }}</Button>
        <Button type="warning" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="clearSearch">{{
          $t('message.linkis.tenantTagManagement.clear')
        }}</Button>
        <Button type="success" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="createTenant">{{
          $t('message.linkis.tenantTagManagement.create')
        }}</Button>
      </Col>
    </Row>
    <Table
      border
      size="small"
      align="center"
      :columns="tableColumns"
      :data="datalist"
      :loading="tableLoading"
      class="table-content data-source-table">

    </Table>
    <Page
      :page-size="page.pageSize"
      :current="page.pageNow"
      :total="page.totalPage"
      @on-change="changePage"
      size="small"
      show-elevator
      show-total
      :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
      style="position: absoulute; bottom: 10px; overflow: hidden; text-align: center;"
    ></Page>
    <Modal
      v-model="showCreateModal"
      class="modal"
      @on-cancel="cancel">
      <div class="form">
        <Form ref="createTenantForm" :model="modalData" label-position="left" :label-width="85" :rules="modalDataRule">
          <FormItem :label="$t('message.linkis.tenantTagManagement.department')" prop="department">
            <Select
              v-model="modalData.department"
              style="width: 319px"
              class="input"
              :placeholder="$t('message.linkis.tenantTagManagement.selectDepartment')"
              @on-change="handleChange"
              filterable
              clearable
            >
              <Option v-for="item in departments" :value="item.label" :key="item.label" :label="item.label"/>
            </Select>
          </FormItem>
          <FormItem :label="$t('message.linkis.tenantTagManagement.appName')" prop="creator">
            <Input class="input" v-model="modalData.creator" style="width: 319px"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.tenantTagManagement.tenantTag')" prop="tenantValue">
            <Input class="input" v-model="modalData.tenantValue" style="width: 319px"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.tenantTagManagement.createUser')" prop="createBy">
            <Input class="input" v-model="modalData.createBy" style="width: 319px"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.isValid')" prop="isValid">
            <RadioGroup v-model="modalData.isValid">
              <Radio label="Y">{{$t('message.linkis.ipListManagement.yes')}}</Radio>
              <Radio label="N">{{$t('message.linkis.ipListManagement.no')}}</Radio>
            </RadioGroup>
          </FormItem>
        </Form>
      </div>
      <div slot="footer">
        <Button @click="cancel">{{$t('message.linkis.tenantTagManagement.Cancel')}}</Button>
        <Button type="primary"  @click="addTenantTag" :loading="isRequesting">{{$t('message.common.ok')}}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import {cloneDeep} from 'lodash'
import storage from "@/common/helper/storage";
import api from '@/common/service/api'
export default {
  name: 'tenantTagManagement',
  data() {
    return {
      loading: false,
      queryData: {
        department: '',
        creator: '',
        tenantValue: '',
      },
      confirmQuery: {
        department: '',
        creator: '',
        tenantValue: '',
      },
      tableColumns: [
        {
          title: "ID",
          key: 'id',
          width: 150,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.department'),
          key: 'department',
          width: 180,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.appName'),
          key: 'creator',
          width: 170,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.tenantTag'),
          key: 'tenantValue',
          width: 250,
          tooltip: true,
          align: 'center',
        },
        
        {
          title: this.$t('message.linkis.ipListManagement.isValid'),
          key: 'isValid',
          width: 100,
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('span', params.row.isValid === 'Y' ? this.$t('message.linkis.ipListManagement.yes') : this.$t('message.linkis.ipListManagement.no'))
            ]);
          }
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.createUser'),
          key: 'createBy',
          width: 350,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.createTime'),
          key: 'createTime',
          width: 245,
          tooltip: true,
          align: 'center',
        },
        
        {
          title: this.$t('message.linkis.tenantTagManagement.action'),
          key: 'action',
          width: 200,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  type: 'primary',
                  size: 'small'
                },
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.edit(params.row)
                  }
                }
              }, this.$t('message.linkis.tenantTagManagement.edit')),
              h('Button', {
                props: {
                  type: 'error',
                  size: 'small'
                },
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.delete(params.row)
                  }
                }
              }, this.$t('message.linkis.tenantTagManagement.delete')),
              h('Button', {
                props: {
                  type: params.row.isValid === 'N' ? 'success' : 'warning',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.enable(params.row)
                  }
                }
              }, params.row.isValid === 'N' ? this.$t('message.linkis.ipListManagement.enable') : this.$t('message.linkis.ipListManagement.disable')),
            ]);
          }
        }
      ],
      datalist: [],
      tableLoading: false,
      showCreateModal: false,
      departments: [],
      modalData: {
        department: '',
        creator: '',
        tenantValue: '',
        createBy: '',
        isValid: 'Y',
      },
      modalDataRule: {
        department: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
        ],
        creator: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_\*]+$/, message: this.$t('message.linkis.tenantTagManagement.contentError'), type: 'string'}
        ],
        tenantValue: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_\*\-]+$/, message: this.$t('message.linkis.tenantTagManagement.contentError2'), type: 'string'}
        ],
        createBy: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.tenantTagManagement.contentError1'), type: 'string'}
        ],
      },
      mode: 'create',
      // 缓存，用于校验
      editData: {},
      page: {
        pageSize: 10,
        pageNow: 1,
        totalPage: 0,
      },
      userName: '',
      isRequesting: false
    }
  },
  methods: {
    async getTableData() {
      try {
        this.tableLoading = true;
        let params = {};
        const keys = Object.keys(this.confirmQuery);
        for (let i = 0; i< keys.length; i++) {
          if(this.confirmQuery[keys[i]]) {
            params[[keys[i]]] = this.confirmQuery[keys[i]];
          }
        }
        if(params.department) {
          params.departmentId = this.departments.find(item => item.label===params.department).value;
        }
        params.pageNow = this.page.pageNow;
        params.pageSize = this.page.pageSize;
        await api.fetch("/configuration/tenant-mapping/query-department-tenant", params, "get")
          .then((res) => {
            this.datalist = res.tenantList.map((item) => {
              item.createTime = new Date(item.createTime).toLocaleString();
              return item;
            });
            this.page.totalPage = res.totalPage;
          })
        this.tableLoading = false;
      } catch(err) {
        this.tableLoading = false;
      }

    },
    async getDepartments() {
      try {
        const res = await api.fetch('/configuration/tenant-mapping/query-department', {}, 'get');
        this.departments = res.departmentList.map((item) => ({
          label: item.orgName,
          value: item.orgId
        }))
      } catch (err) {
        window.console.warn(err);
      }
    },
    async init() {
      this.loading = true;
      await this.getDepartments();
      await this.getTableData();
      this.loading = false;
    },
    async clearSearch() {
      this.queryData = {
        department: '',
        creator: '',
        tenantValue: '',
      };
      this.confirmQuery = {
        department: '',
        creator: ''
      }
      this.page.pageNow = 1;
      await this.getTableData()
    },
    async createTenant () {
      this.showCreateModal = true;
      this.mode = 'create';
      this.modalData.createBy = this.userName;
    },
    cancel() {
      this.showCreateModal = false;
      this.modalData = {
        departmentId: '',
        department: '',
        creator: '',
        isValid: 'Y',
        createBy: '',
      };
      this.editData = {
        departmentId: '',
        department: '',
        creator: '',
        isValid: 'Y',
        createBy: '',
      };
      this.$refs.createTenantForm.resetFields();
    },
    addTenantTag() {
      if(this.isRequesting) return;
      const target = '/configuration/tenant-mapping/save-department-tenant';
      this.$refs.createTenantForm.validate(async (valid) => {
        if(valid) {
          this.clearSearch();
          try {
            if (this.mode !== 'edit') {
              this.page.pageNow = 1;
            }
            this.isRequesting = true;
            await api.fetch(target, this.modalData, "post").then(async (res) => {
              window.console.log(res);
              await this.getTableData();
              this.cancel();
              this.$Message.success(this.$t('message.linkis.udf.success'));
            });
            this.isRequesting = false;
          } catch(err) {
            this.isRequesting = false;
          }
        } else {
          this.$Message.error(this.$t('message.linkis.error.validate'));
        }
      })

    },
    edit(data) {
      const {
        id, departmentId, department, creator, tenantValue, createBy, isValid
      } = data
      this.modalData = {
        id, departmentId, department, creator, tenantValue, createBy, isValid
      };
      this.editData = {
        id, departmentId, department, creator, tenantValue, createBy, isValid
      };
      this.showCreateModal = true;
      this.mode = 'edit';
    },
    delete(data) {
      this.$Modal.confirm({
        title: this.$t('message.linkis.tenantTagManagement.confirmDel'),
        content: this.$t('message.linkis.tenantTagManagement.isConfirmDel', {name: `id:${data.id}`}),
        onOk: async () => {
          await this.confirmDelete(data);
          await this.getTableData();
        },
      })
    },
    handleChange(val) {
      if(this.departments.find(item => item.label === val)?.value) {
        this.modalData.departmentId = this.departments.find(item => item.label === val).value;
      }
      
    },
    async confirmDelete(data) {
      try {
        await api.fetch('configuration/tenant-mapping/delete-department-tenant', {id: data.id}, 'get');
      } catch(err) {
        return;
      }
    },
    async changePage(val) {
      this.page.pageNow = val;
      await this.getTableData();
    },
    async search() {
      const { department, creator, tenantValue } = this.queryData;
      this.confirmQuery = { department, creator, tenantValue };
      this.page.pageNow = 1;
      await this.getTableData()
    },
    async confirmEnable(data) {
      const status = data.isValid;
      const tempData = cloneDeep(data);
      delete tempData._index;
      delete tempData._rowKey;
      delete tempData.updateTime;
      delete tempData.createTime;
      delete tempData.userCreator;
      tempData.isValid = tempData.isValid === 'Y' ? 'N' : 'Y';
      try {
        await api.fetch('/configuration/tenant-mapping/save-department-tenant', tempData, 'post');
        if(status === 'N') {
          this.$Message.success(this.$t('message.linkis.ipListManagement.enableSuccessfully'));
        } else {
          this.$Message.success(this.$t('message.linkis.ipListManagement.disableSuccessfully'));
        }
          
      } catch (err) {
        return;
      }
        
      
    },
    enable(data) {
      this.$Modal.confirm({
        title: this.$t('message.linkis.ipListManagement.confirmDel'),
        content: data.isValid === 'N' ? this.$t('message.linkis.ipListManagement.confirmEnable', {name: `${data.id}`}) : this.$t('message.linkis.ipListManagement.confirmDisable', {name: `${data.id}`}),
        onOk: async () => {
          await this.confirmEnable(data);
          await this.getTableData();
        },
      })
    },
  },
  created() {
    this.userName = storage.get('userName') || storage.get('baseInfo', 'local')?.username || '';
    this.init();
  }

};
</script>
<style lang="scss" src="./index.scss" scoped></style>
<style lang="scss" scoped>
.modal {

  .input-area {
    padding: 20px 50px;
    .item {
      display: flex;
      margin-bottom: 10px;
      .input {
        width: calc(100% - 66px);
      }
    }
  }
}

</style>
