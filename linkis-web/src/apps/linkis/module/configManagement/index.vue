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
        <span :title="$t('message.linkis.ipListManagement.engineType')" class="search-label">{{$t('message.linkis.ipListManagement.engineType')}}</span>
        <Select v-model="queryData.engineType" style="width: 290px">
          <Option v-for="(item) in getEngineTypes" :label="item === 'all' ? $t('message.linkis.engineTypes.all'): item" :value="item" :key="item" />
        </Select>
      </Col>
      <Col span="6" class="search-item">
        <span :title="$t('message.linkis.ipListManagement.key')" class="search-label">{{$t('message.linkis.ipListManagement.key')}}</span>
        <Input
          v-model="queryData.key"
          style="width: 290px"
          :placeholder="$t('message.linkis.ipListManagement.inputKey')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="9">
        <Button type="primary" class="button" @click="search">{{
          $t('message.linkis.ipListManagement.search')
        }}</Button>
        <Button type="warning" class="button" @click="clearSearch">{{
          $t('message.linkis.ipListManagement.clear')
        }}</Button>
        <Button type="success" class="button" @click="createTenant">{{
          $t('message.linkis.ipListManagement.create')
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
      show-total
      @on-change="changePage"
      size="small"
      show-elevator
      :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
      class="page"
    ></Page>
    <Modal
      v-model="showCreateModal"
      class="modal"
      :title="modalTitle"
      @on-cancel="cancel">
      <div class="form">
        <Form ref="createTenantForm" :model="modalData" label-position="left" :label-width="100" :rules="modalDataRule">
          <FormItem :label="$t('message.linkis.ipListManagement.key')" prop="key">
            <Input class="input" v-model="modalData.key"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.name')" prop="name">
            <Input class="input" v-model="modalData.name"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.description')" prop="description">
            <Input class="input" v-model="modalData.description"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.treeName')" prop="treeName">
            <Input class="input" v-model="modalData.treeName"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.defaultValue')" prop="defaultValue">
            <Input class="input" v-model="modalData.defaultValue"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.validType')" prop="validateType">
            <Select class="input" v-model="modalData.validateType">
              <Option v-for="(item) in validateTypeList" :label="item.label" :value="item.value" :key="item.value" />
            </Select>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.dataType')" prop="boundaryType">
            <Select class="input" v-model="modalData.boundaryType">
              <Option v-for="(item) in boundaryTypeList" :label="item.label" :value="item.value" :key="item.value" />
            </Select>
          </FormItem>
          <FormItem :rules="{required: modalData.validateType !== 'None' , message: $t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'}" :label="$t('message.linkis.ipListManagement.validRange')" prop="validateRange"> 
            <Input class="input" v-model="modalData.validateRange"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.engineType')" prop="engineType">
            <Select class="input" v-model="modalData.engineType">
              <Option v-for="(item) in getEngineTypes" :label="item === 'all' ? $t('message.linkis.engineTypes.all'): item" :value="item" :key="item" />
            </Select>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.templateRequired')" prop="templateRequired">
            <RadioGroup v-model="modalData.templateRequired">
              <Radio label="1">{{$t('message.linkis.ipListManagement.yes')}}</Radio>
              <Radio label="0">{{$t('message.linkis.ipListManagement.no')}}</Radio>
            </RadioGroup>
          </FormItem>
          <FormItem v-if="mode !== 'edit'" :label="$t('message.linkis.ipListManagement.enName')" prop="enName">
            <Input class="input" v-model="modalData.enName"></Input>
          </FormItem>
          <FormItem v-if="mode !== 'edit'" :label="$t('message.linkis.ipListManagement.enDescription')" prop="enDescription">
            <Input class="input" v-model="modalData.enDescription"></Input>
          </FormItem>
          <FormItem v-if="mode !== 'edit'" :label="$t('message.linkis.ipListManagement.enTreeName')" prop="enTreeName">
            <Input class="input" v-model="modalData.enTreeName"></Input>
          </FormItem>
        </Form>
      </div>
      <div slot="footer">
        <Button @click="cancel">{{$t('message.linkis.ipListManagement.Cancel')}}</Button>
        <Button type="primary" @click="addConfig" :loading="isRequesting">{{$t('message.common.ok')}}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import api from '@/common/service/api'
import { cloneDeep } from 'lodash';
export default {
  name: 'configManagement',
  data() {
    return {
      validateTypeList: [
        { label: this.$t('message.linkis.ipListManagement.none'), value: 'None'},
        { label: this.$t('message.linkis.ipListManagement.regex'), value: 'Regex'},
        { label: this.$t('message.linkis.ipListManagement.numInterval'), value: 'NumInterval'},
        { label: this.$t('message.linkis.ipListManagement.oft'), value: 'OFT'},
      ],
      modalTitle: '',
      getEngineTypes: [],
      loading: false,
      queryData: {
        engineType: '',
        key: ''
      },
      confirmQuery: {
        engineType: '',
        key: '',
      },
      tableColumns: [
        {
          title: "ID",
          key: 'id',
          width: 60,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.key'),
          key: 'key',
          width: 300,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.name'),
          key: 'name',
          width: 300,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.description'),
          key: 'description',
          width: 300,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.defaultValue'),
          key: 'defaultValue',
          width: 150,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.validType'),
          key: 'formatValidateType',
          width: 200,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.validRange'),
          key: 'validateRange',
          width: 280,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.dataType'),
          key: 'formatBoundaryType',
          width: 150,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.engineType'),
          key: 'engineType',
          width: 150,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.treeName'),
          key: 'treeName',
          width: 120,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.templateRequired'),
          key: 'templateRequired',
          width: 200,
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('span', params.row.templateRequired === '1' ? this.$t('message.linkis.ipListManagement.yes') : this.$t('message.linkis.ipListManagement.no'))
            ]);
          }
        },
        {
          title: this.$t('message.linkis.ipListManagement.action'),
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
              }, this.$t('message.linkis.ipListManagement.edit')),
              h('Button', {
                props: {
                  type: 'error',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.delete(params.row)
                  }
                }
              }, this.$t('message.linkis.ipListManagement.delete'))
            ]);
          }
        }
      ],
      datalist: [],
      tableLoading: false,
      showCreateModal: false,
      modalData: {
        key: '',
        name: '',
        description: '',
        defaultValue: '',
        validateType: '',
        validateRange: '',
        boundaryType: '',
        treeName: '',
        enName: '',
        enDescription: '',
        enTreeName: '',
        templateRequired: '0',
      },
      modalDataRule: {
        key: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[^\u4E00-\u9FFF]+$/, message: this.$t('message.linkis.ipListManagement.noZH'), type: 'string'},
          {type: 'string', max: 50, message: this.$t('message.linkis.ipListManagement.customLen', {length: '50'})}
        ],
        name: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {type: 'string', max: 50, message: this.$t('message.linkis.ipListManagement.customLen', {length: '50'})}
        ],
        description: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {type: 'string', max: 200, message: this.$t('message.linkis.ipListManagement.customLen', {length: '200'})}
        ],
        validateType: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'change'},
        ],
        validateRange: [
          {type: 'string', max: 150, message: this.$t('message.linkis.ipListManagement.customLen', {length: '150'})},
        ],
        boundaryType: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'change', type: 'number'},
        ],
        treeName: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {type: 'string', max: 20, message: this.$t('message.linkis.ipListManagement.customLen', {length: '20'})}
        ],
        engineType: [
          {pattern: /^[^\u4E00-\u9FFF]+$/, message: this.$t('message.linkis.ipListManagement.noZH'), type: 'string'},
        ],
        enName: [
          {pattern: /^[^\u4E00-\u9FFF]+$/, message: this.$t('message.linkis.ipListManagement.noZH'), type: 'string'},
          {type: 'string', max: 200, message: this.$t('message.linkis.ipListManagement.customLen', {length: '200'})}
        ],
        enDescription: [
          {pattern: /^[^\u4E00-\u9FFF]+$/, message: this.$t('message.linkis.ipListManagement.noZH'), type: 'string'},
          {type: 'string', max: 100, message: this.$t('message.linkis.ipListManagement.customLen', {length: '100'})}
        ],
        enTreeName: [
          {pattern: /^[^\u4E00-\u9FFF]+$/, message: this.$t('message.linkis.ipListManagement.noZH'), type: 'string'},
          {type: 'string', max: 100, message: this.$t('message.linkis.ipListManagement.customLen', {length: '100'})}
        ],
      },
      mode: 'create',
      page: {
        pageSize: 10,
        pageNow: 1,
        totalPage: 0,
      },
      isRequesting: false,
      boundaryTypeMap: ['none', 'with min', 'with max', 'min and max both'],
      boundaryTypeList: ['none', 'with min', 'with max', 'min and max both'].map((item, index) => {
        return {
          label: item,
          value: index
        }
      })
    }
  },
  methods: {
    async getTableData() {
      try {
        this.tableLoading = true;
        let params = {};
        if(this.confirmQuery?.engineType === 'all') delete this.confirmQuery.engineType;
        const keys = Object.keys(this.confirmQuery);
        for (let i = 0; i< keys.length; i++) {
          if(this.confirmQuery[keys[i]]) {
            params[[keys[i]]] = this.confirmQuery[keys[i]];
          }
        }
        params.pageNow = this.page.pageNow;
        params.pageSize = this.page.pageSize;
        const res = await api.fetch("/configuration/baseKeyValue", params, "get")
          
        this.datalist = res.configKeyList.map((item) => {
          item.formatBoundaryType = this.boundaryTypeMap[item.boundaryType]
          item.formatValidateType = this.validateTypeList.find(p => p.value === item.validateType)?.label || ''
          item.templateRequired = item.templateRequired ? '1' : '0'
          return item
        });
        this.page.totalPage = res.totalPage;
        this.tableLoading = false;
      } catch(err) {
        this.tableLoading = false;
      }

    },
    async init() {
      this.loading = true;
      await this.getTableData();
      this.loading = false;
    },
    async clearSearch() {
      this.queryData = {
        engineType: '',
        key: ''
      };
      this.confirmQuery = {
        engineType: '',
        key: ''
      }
      this.page.pageNow = 1;
      await this.getTableData()
    },
    async createTenant () {
      this.modalTitle = this.$t('message.linkis.ipListManagement.createRules')
      this.showCreateModal = true;
      this.mode = 'create'
      this.$refs.createTenantForm.resetFields()
    },
    cancel() {
      this.showCreateModal = false;
      this.modalData = {
        key: '',
        name: '',
        description: '',
        defaultValue: '',
        engineType: '',
        validateType: '',
        validateRange: '',
        boundaryType: '',
        treeName: '',
        enName: '',
        enDescription: '',
        enTreeName: ''
      }
    },
    addConfig() {
      if(this.isRequesting) return; 
      const target = '/configuration/baseKeyValue'
      this.$refs.createTenantForm.validate(async (valid) => {
        if(valid) {
          
          try {
            if(this.mode !== 'edit') {
              this.page.pageNow = 1;
            }
            this.isRequesting = true
            const body = cloneDeep(this.modalData);
            if(!body.engineType || body.engineType === 'all') delete body.engineType
            body.templateRequired = body.templateRequired === '1'
            await api.fetch(target, body, "post")
            await this.clearSearch();
            this.cancel();
            this.$Message.success(this.$t('message.linkis.udf.success'));
           
            this.isRequesting = false
          } catch(err) {
            this.isRequesting = false
          }
        } else {
          this.$Message.error(this.$t('message.linkis.error.validate'));
        }
      })

    },
    edit(data) {
      const {
        id,
        key,
        name,
        description,
        defaultValue,
        validateType,
        validateRange,
        boundaryType,
        treeName,
        enName,
        enDescription,
        enTreeName,
        engineType,
        templateRequired
      } = data
      this.modalData = {
        id,
        key,
        name,
        description,
        defaultValue,
        validateType,
        validateRange,
        boundaryType,
        treeName,
        enName,
        enDescription,
        enTreeName,
        engineType,
        templateRequired
      };
      this.showCreateModal = true;
      this.modalTitle = this.$t('message.linkis.ipListManagement.editRules')
      this.$refs.createTenantForm.resetFields()
      this.mode = 'edit';
    },
    delete(data) {
      this.$Modal.confirm({
        title: this.$t('message.linkis.ipListManagement.confirmDel'),
        content: this.$t('message.linkis.ipListManagement.isConfirmDel', {name: `${data.name}`}),
        onOk: async () => {
          await this.confirmDelete(data);
          await this.getTableData();
        },
      })
    },
    async confirmDelete(data) {
      try {
        await api.fetch('/configuration/baseKeyValue', {id: data.id}, 'delete');
      } catch(err) {
        return;
      }
    },
    async changePage(val) {
      this.page.pageNow = val;
      await this.getTableData();
    },
    async search() {
      const { engineType, key } = this.queryData;
      this.confirmQuery = { engineType, key };
      this.page.pageNow = 1;
      await this.getTableData()
    }
  },
  created() {
    api.fetch('/configuration/engineType', 'get').then(res => {
      this.getEngineTypes = ['all', ...res.engineType]
    })
    this.init();
  }

};
</script>
<style lang="scss" src="./index.scss" scoped></style>
<style lang="scss" scoped>

.modal {
  .form {
    .input {
      width: 319px;
    }
  }
}


.ivu-tooltip-popper {
  .ivu-tooltip-content {
    .ivu-tooltip-inner-with-width {
      word-wrap: break-word;
    }
  }
}

</style>
