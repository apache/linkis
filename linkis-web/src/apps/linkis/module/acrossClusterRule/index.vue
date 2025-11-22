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
      
      <Col span="5" class="search-item">
        <span :title="$t('message.linkis.ipListManagement.userName')" class="search-label">{{$t('message.linkis.ipListManagement.userName')}}</span>
        <Input
          v-model="queryData.username"
          class="input"
          :placeholder="$t('message.linkis.ipListManagement.inputUser')"
          @on-enter="search"
          style="width: 270px"
        ></Input>
      </Col>
      <Col span="5" class="search-item">
        <span :title="$t('message.linkis.ipListManagement.appName')" class="search-label">{{$t('message.linkis.ipListManagement.appName')}}</span>
        <Input
          v-model="queryData.creator"
          class="input"
          :placeholder="$t('message.linkis.ipListManagement.inputApp')"
          @on-enter="search"
          style="width: 270px"
        ></Input>
      </Col>
      <Col span="5" class="search-item">
        <span :title="$t('message.linkis.ipListManagement.cluster')" class="search-label">{{$t('message.linkis.ipListManagement.cluster')}}</span>
        <Input
          v-model="queryData.clusterName"
          class="input"
          :placeholder="$t('message.linkis.ipListManagement.inputCluster')"
          @on-enter="search"
          style="width: 270px"
        ></Input>
      </Col>
      <Col span="5" class="search-item">
        <span :title="$t('message.linkis.ipListManagement.isValid')" class="search-label">{{$t('message.linkis.ipListManagement.isValid')}}</span>
        <Select v-model="queryData.isValid" filterable clearable style="width: calc(100% - 105px);">
          <Option v-for="item in validOptions" :value="item.value" :key="item.value" :label="item.label"/>
        </Select>
      </Col>
      <Col span="4">
        <Button :disabled="isBatch" type="primary" class="button" @click="search">{{
          $t('message.linkis.ipListManagement.search')
        }}</Button>
        <Button :disabled="isBatch" type="warning" class="button" @click="clearSearch">{{
          $t('message.linkis.ipListManagement.clear')
        }}</Button>
        <Button v-if="isLogAdmin && !isBatch" type="success" class="button" @click="createRules">{{
          $t('message.linkis.ipListManagement.create')
        }}</Button>
        <Dropdown v-if="isLogAdmin" @on-click="batch">
          <Button type="primary" class="button">
            {{ $t('message.linkis.ipListManagement.batchOperate')}}
          </Button>
          <template #list>
            <DropdownMenu>
              <DropdownItem name="enable">{{ $t('message.linkis.ipListManagement.batchEnable')}}</DropdownItem>
              <DropdownItem name="disable">{{ $t('message.linkis.ipListManagement.batchDisable')}}</DropdownItem>
              <DropdownItem name="delete">{{ $t('message.linkis.ipListManagement.batchDelete')}}</DropdownItem>
              <DropdownItem name="modify">{{ $t('message.linkis.ipListManagement.batchModify')}}</DropdownItem>
            </DropdownMenu>
          </template>
        </Dropdown>
      </Col>
    </Row>
    <div v-if="isBatch" class="second-action-bar">
      <Button class="action-btn" v-if="batchMode === 'modify'" type="primary" @click="createRules">{{ $t('message.linkis.ipListManagement.adjustConf')}}</Button>
      <Button class="action-btn" type="primary" :disabled="!confirmable" @click="batchConfirm">{{ confirmBtnText}}</Button>
      <Button class="action-btn" type="default" @click="batchCancel">{{ $t('message.linkis.ipListManagement.Cancel')}}</Button>
    </div>
    <Table
      border
      size="small"
      align="center"
      :columns="tableColumns"
      :data="datalist"
      :loading="tableLoading"
      class="table-content data-source-table"
      @on-selection-change="handleSelectionChange">
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
      class="page"
    ></Page>
    <Modal
      v-model="showCreateModal"
      class="modal"
      width="620px"
      :title="modalTitle"
      @on-cancel="cancel">
      <div class="form">
        <Form ref="createRuleForm" :model="modalData" label-position="left" :label-width="92" :rules="modalDataRule">
          <FormItem :label="$t('message.linkis.ipListManagement.cluster')" prop="clusterName">
            <Input class="input" v-model="modalData.clusterName"></Input>
          </FormItem>
          <FormItem v-if="!isBatch" :label="$t('message.linkis.ipListManagement.userName')" prop="username">
            <Input class="input" v-model="modalData.username"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.appName')" prop="creator">
            <Input class="input" v-model="modalData.creator"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.startTime')" prop="startTime">
            <Input class="input" v-model="modalData.startTime" placeholder="XX:XX"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.endTime')" prop="endTime">
            <Input class="input" v-model="modalData.endTime" placeholder="XX:XX"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.acrossClusterQueue')" prop="crossQueue">
            <Input class="input" v-model="modalData.crossQueue"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.priorityCluster')" prop="priorityCluster">
            <Input class="input" v-model="modalData.priorityCluster"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.targetCPUThreshold')" prop="targetCPUThreshold">
            <Input class="input" v-model="modalData.targetCPUThreshold"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.targetMemoryThreshold')" prop="targetMemoryThreshold">
            <Input class="input" v-model="modalData.targetMemoryThreshold"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.targetCPUPercentageThreshold')" prop="targetCPUPercentageThreshold">
            <Input class="input" v-model="modalData.targetCPUPercentageThreshold"  placeholder="0~1"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.targetMemoryPercentageThreshold')" prop="targetMemoryPercentageThreshold">
            <Input class="input" v-model="modalData.targetMemoryPercentageThreshold"  placeholder="0~1"></Input>
          </FormItem>
          <!-- <FormItem :label="$t('message.linkis.ipListManagement.CPUThreshold')" prop="CPUThreshold">
            <Input class="input" v-model="modalData.CPUThreshold"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.MemoryThreshold')" prop="MemoryThreshold">
            <Input class="input" v-model="modalData.MemoryThreshold"></Input>
          </FormItem> -->
          <FormItem :label="$t('message.linkis.ipListManagement.originCPUPercentageThreshold')" prop="originCPUPercentageThreshold">
            <Input class="input" v-model="modalData.originCPUPercentageThreshold"  placeholder="0~1"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.originMemoryPercentageThreshold')" prop="originMemoryPercentageThreshold">
            <Input class="input" v-model="modalData.originMemoryPercentageThreshold"  placeholder="0~1"></Input>
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
        <Button @click="cancel">{{$t('message.linkis.ipListManagement.Cancel')}}</Button>
        <Button type="primary" @click="addRules" :loading="isRequesting">{{$t('message.common.ok')}}</Button>
      </div>
    </Modal>
    <Modal
      v-model="showViewModal"
      :title="modalTitle"
      width="620px"
      class="modal"
      @on-cancel="cancel">
      <v-jsoneditor ref="editor" :options="viewerOptions" />
    </Modal>
  </div>
</template>
<script>
import storage from "@/common/helper/storage";
import api from '@/common/service/api';
import { cloneDeep, isNumber } from 'lodash';
export default {
  name: 'acrossClusterRule',
  data() {
    return {
      isLogAdmin: false,
      modalTitle: '',
      loading: false,
      queryData: {
        username: '',
        creator: '',
        isValid: '',
      },
      confirmQuery: {
        username: '',
        creator: '',
        isValid: '',
      },
      tableColumns: [
        // {
        //   type: 'selection', 
        //   width: 60,
        //   align: 'center'
        // },
        {
          title: "ID",
          key: 'id',
          width: 133,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.cluster'),
          key: 'clusterName',
          width: 450,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.userCreator'),
          key: 'userCreator',
          width: 450,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.isValid'),
          key: 'isValid',
          width: 150,
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('span', params.row.isValid === 'Y' ? this.$t('message.linkis.ipListManagement.yes') : this.$t('message.linkis.ipListManagement.no'))
            ]);
          }
        },
        {
          title: this.$t('message.linkis.ipListManagement.priorityCluster'),
          key: 'parsedRules',
          width: 150,
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('span', params.row?.parsedRules?.priorityClusterRule?.priorityCluster || '--')
            ]);
          }
        },
        {
          title: this.$t('message.linkis.ipListManagement.rules'),
          key: '',
          width: 100,
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  type: 'primary',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.viewRule(params.row)
                  }
                }
              }, this.$t('message.linkis.ipListManagement.view'))
            ]);
          }
        },
        {
          title: this.$t('message.linkis.ipListManagement.action'),
          key: 'action',
          width: 200,
          align: 'center',
          render: (h, params) => {
            if(!this.isLogAdmin) {
              return h('div', [
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
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.delete(params.row)
                  }
                }
              }, this.$t('message.linkis.ipListManagement.delete')),
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
      isBatch: false,
      batchMode: '',
      tableLoading: false,
      showCreateModal: false,
      modalData: {
        username: '',
        creator: '',
        clusterName: '',
        crossQueue: '',
        priorityCluster: '',
        targetCPUThreshold: '',
        targetMemoryThreshold: '',
        targetCPUPercentageThreshold: '',
        targetMemoryPercentageThreshold: '',
        originCPUPercentageThreshold: '',
        originMemoryPercentageThreshold: '',
        startTime: '',
        endTime: '',
        isValid: 'Y'
      },
      modalDataRule: {
        username: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.ipListManagement.contentError1'), type: 'string'},
          {type: 'string', max: 20, message: this.$t('message.linkis.ipListManagement.customLen', {length: '20'})}
        ],
        creator: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.ipListManagement.contentError1'), type: 'string'},
          {type: 'string', max: 20, message: this.$t('message.linkis.ipListManagement.customLen', {length: '20'})}
        ],
        // parsedRules: [
        //   {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur', type: 'object'},
        //   {validator: this.ruleJsonValidator, trigger: 'blur'}
        // ],
        clusterName: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.ipListManagement.contentError1'), type: 'string'},
          {type: 'string', max: 20, message: this.$t('message.linkis.ipListManagement.customLen', {length: '20'})}
        ],
        startTime: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^([01]\d|2[0-3]):[0-5]\d$/, message: this.$t('message.linkis.ipListManagement.timeError'), type: 'string'}
        ],
        endTime: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^([01]\d|2[0-3]):[0-5]\d$/, message: this.$t('message.linkis.ipListManagement.timeError'), type: 'string'}
        ],
        targetMemoryThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.thresholdValidator, trigger: 'blur'},
        ],
        targetCPUThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.thresholdValidator, trigger: 'blur'},
        ],
        targetMemoryPercentageThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.percentageThresholdValidator, trigger: 'blur'},
          {type: 'string', max: 4, message: this.$t('message.linkis.ipListManagement.customLen', {length: '4'})}
        ],
        targetCPUPercentageThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.percentageThresholdValidator, trigger: 'blur'},
          {type: 'string', max: 4, message: this.$t('message.linkis.ipListManagement.customLen', {length: '4'})}
        ],
        originMemoryPercentageThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.percentageThresholdValidator, trigger: 'blur'},
          {type: 'string', max: 4, message: this.$t('message.linkis.ipListManagement.customLen', {length: '4'})}
        ],
        originCPUPercentageThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.percentageThresholdValidator, trigger: 'blur'},
          {type: 'string', max: 4, message: this.$t('message.linkis.ipListManagement.customLen', {length: '4'})}
        ],
        crossQueue: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          // {validator: this.percentageThresholdValidator, trigger: 'blur'},
          {type: 'string', max: 50, message: this.$t('message.linkis.ipListManagement.customLen', {length: '50'})}
        ],
        priorityCluster: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.ipListManagement.contentError1'), type: 'string'},
          {type: 'string', max: 20, message: this.$t('message.linkis.ipListManagement.customLen', {length: '20'})}
        ],
      },
      // tagIsExist: true,
      mode: 'create',
      page: {
        pageSize: 10,
        pageNow: 1,
        totalPage: 0,
      },
      userName: '',
      isRequesting: false,
      json: '',
      viewerOptions: {
        mode: 'view'
      },
      editorOptions: {
        mode: 'code'
      },
      showViewModal: false,
      errorJson: false,
      batchList: [],
      completeModifyInfo: false,
      validOptions: [{label: this.$t('message.linkis.yes'), value: 'Y'}, {label: this.$t('message.linkis.no'), value: 'N'}],
    }
  },
  computed: {
    mapping() {
      return (this.modalData.username || 'username') + '-' + (this.modalData.creator || 'creator') + '  -->  ' + (this.modalData.ipList || 'IPList')
    },
    confirmable() {
      const pageCount = Math.ceil(this.page.totalPage / this.page.pageSize)
      let hasItem = false;
      for (let i = 0;i < pageCount; i++) {
        if (this.batchList[i+1]?.length > 0) {
          hasItem = true
        }
      }
      return hasItem && (this.batchMode === 'modify' ? this.completeModifyInfo : true)
    },
    confirmBtnText() {
      switch (this.batchMode) {
        case 'enable':
          return this.$t('message.linkis.ipListManagement.confirmEnableBtn')
        case 'disable':
          return this.$t('message.linkis.ipListManagement.confirmDisableBtn')
        case 'delete':
          return this.$t('message.linkis.ipListManagement.confirmDeleteBtn')
        case 'modify':
          return this.$t('message.linkis.ipListManagement.confirmModifyBtn')
        default:
          return '';
      }
    }
  },
  watch: {
    modalData: {
      handler() {
        this.completeModifyInfo = false;
      },
      deep: true,
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
        params.pageNow = this.page.pageNow;
        params.pageSize = this.page.pageSize;
        const res = await api.fetch("/configuration/acrossClusterRule/list", params, "get")
          
        this.datalist = res.acrossClusterRuleList.map((item) => {
          item.userCreator = item.username + "-" + item.creator;
          item.parsedRules = JSON.parse(item.rules)
          if(!this.batchList[this.page.pageNow]) this.$set(this.batchList, this.page.pageNow, []);
          // iview table cannot cache or bind selection
          item._checked = this.batchList[this.page.pageNow].includes(item.id)
          // item.createTime = new Date(item.createTime).toLocaleString();
          return item;
        })
        this.page.totalPage = res.totalPage;
          
        this.tableLoading = false;
      } catch(err) {
        this.tableLoading = false;
        window.console.warn(err);
      }

    },
    async init() {
      this.loading = true;
      await this.getTableData();
      this.loading = false;
    },
    async clearSearch() {
      this.queryData = {
        username: '',
        creator: '',
        clusterName: '',
        isValid: '',
      };
      this.confirmQuery = {
        username: '',
        creator: '',
        clusterName: '',
      }
      this.page.pageNow = 1;
      await this.getTableData()
    },
    async createRules () {
      this.showCreateModal = true;
      this.mode = 'create';
      this.modalTitle = this.isBatch ? this.$t('message.linkis.ipListManagement.batchModify') : this.$t('message.linkis.ipListManagement.createRules');
      if(this.isBatch) return;
      this.modalData.username = this.userName;
    },
    batch(mode) {
      this.batchMode = mode;
      if(this.isBatch) return;
      this.isBatch = true;
      this.tableColumns.unshift({
        type: 'selection', 
        width: 200,
        align: 'center',
        key: 'id'
      })
      this.tableColumns.pop()
      
    },
    async batchConfirm() {
      const ids = [];
      const pageCount = Math.ceil(this.page.totalPage / this.page.pageSize)
      for (let i = 0;i < pageCount; i++) {
        if(this.batchList[i+1]) {
          ids.push(...this.batchList[i+1])
        }
      }
      let target;
      if(this.batchMode === 'modify') {
        target = '/configuration/acrossClusterRule/updateByBatch';
        this.$Modal.confirm({
          title: this.$t('message.linkis.ipListManagement.batchModify'),
          content: this.$t('message.linkis.ipListManagement.confirmBatchModify'),
          onOk: async () => {
            try {
              const body = cloneDeep(this.modalData);
              delete body.username;
              body.ids = ids;
              await api.fetch(target, body, "put");
              this.batchCancel()
              await this.getTableData();
            } catch (err) {
              window.console.warn(err)
            }
            
          }
        })
      } else if(this.batchMode === 'delete') {
        target = '/configuration/acrossClusterRule/deleteByBatch';
        this.$Modal.confirm({
          title: this.$t('message.linkis.ipListManagement.batchDelete'),
          content: this.$t('message.linkis.ipListManagement.confirmBatchDelete'),
          onOk: async () => {
            try {
              await api.fetch(target, { ids }, "put");
              this.batchCancel();
              this.page.pageNow = 1;
              await this.getTableData();
            } catch (err) {
              window.console.warn(err)
            }
            
          }
        })
      } else {
        let content;
        let title;
        const body = { ids };
        if(this.batchMode === 'enable') {
          content = this.$t('message.linkis.ipListManagement.batchEnable');
          title = this.$t('message.linkis.ipListManagement.batchEnable');
          body.isValid = 'Y';
        } else {
          content = this.$t('message.linkis.ipListManagement.confirmBatchDisable');
          title = this.$t('message.linkis.ipListManagement.batchDisable');
          body.isValid = 'N';
        }
        target = '/configuration/acrossClusterRule/isValidByBatch';
        this.$Modal.confirm({
          title,
          content,
          onOk: async () => {
            try {
              await api.fetch(target, body, "put");
              this.batchCancel();
              await this.getTableData();
            } catch (err) {
              window.console.warn(err)
            }
          }
        })
      }
    },
    batchCancel() {
      this.isBatch = false;
      this.tableColumns.shift();
      this.tableColumns.push({
        title: this.$t('message.linkis.ipListManagement.action'),
        key: 'action',
        width: 200,
        align: 'center',
        render: (h, params) => {
          if(!this.isLogAdmin) {
            return h('div', [
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
              style: {
                marginRight: '5px'
              },
              on: {
                click: () => {
                  this.delete(params.row)
                }
              }
            }, this.$t('message.linkis.ipListManagement.delete')),
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
      })
      this.batchMode = '';
      this.batchList = [];
      this.datalist = this.datalist.map((item) => {
        delete item._checked;
        return item;
      })
      this.cancel();
    },
    handleSelectionChange(selection) {
      this.$set(this.batchList, this.page.pageNow, selection.map(item => item.id));
    },
    cancel() {
      this.showCreateModal = false;
      if(!this.isBatch) {
        this.modalData = {
          username: '',
          creator: '',
          clusterName: '',
          crossQueue: '',
          priorityCluster: '',
          targetCPUThreshold: '',
          targetMemoryThreshold: '',
          targetCPUPercentageThreshold: '',
          targetMemoryPercentageThreshold: '',
          originCPUPercentageThreshold: '',
          originMemoryPercentageThreshold: '',
          startTime: '',
          endTime: '',
          isValid: 'Y'
        }
        this.$refs.createRuleForm.resetFields();
      }
      
    },
    addRules() {
      if(this.isRequesting) return;
      const restfulMode = this.mode === 'edit' ? 'put' : 'post';
      const target = this.mode === 'edit' ? '/configuration/acrossClusterRule/update' : '/configuration/acrossClusterRule/add';
      this.$refs.createRuleForm.validate(async (valid) => {
        if(valid) {
          if(this.isBatch) {
            this.completeModifyInfo = true;
            this.showCreateModal = false;
            return;
          }
          this.clearSearch();
          try {
            if(this.mode !== 'edit') {
              this.page.pageNow = 1;
            }
            this.isRequesting = true
            const body = cloneDeep(this.modalData);
            // body.CPUThreshold = Number(body.CPUThreshold);
            // body.MemoryThreshold = Number(body.MemoryThreshold);
            // body.CPUPercentageThreshold = Number(body.CPUPercentageThreshold);
            // body.MemoryPercentageThreshold = Number(body.MemoryPercentageThreshold);
            const res = await api.fetch(target, body, restfulMode)
            window.console.log(res);
            await this.getTableData();
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
        id, username, creator, clusterName, parsedRules, isValid,
      } = data
      const { targetCPUPercentageThreshold, targetMemoryPercentageThreshold, targetMemoryThreshold, targetCPUThreshold } = parsedRules?.targetClusterRule || {};
      const { originCPUPercentageThreshold, originMemoryPercentageThreshold } = parsedRules?.originClusterRule || {};
      const { crossQueue } = parsedRules?.queueRule || {};
      const { priorityCluster } = parsedRules?.priorityClusterRule || {};
      const { startTime, endTime } = parsedRules?.timeRule || {}
      this.modalData = {
        id, username, creator, clusterName, crossQueue, targetCPUPercentageThreshold, targetMemoryPercentageThreshold, targetMemoryThreshold, targetCPUThreshold, originCPUPercentageThreshold, originMemoryPercentageThreshold, startTime, endTime, isValid, priorityCluster
      };
      this.showCreateModal = true;
      this.mode = 'edit';
      this.modalTitle = this.$t('message.linkis.ipListManagement.editRules')
    },
    delete(data) {
      this.$Modal.confirm({
        title: this.$t('message.linkis.ipListManagement.confirmDel'),
        content: this.$t('message.linkis.ipListManagement.isConfirmDel', {name: `${data.userCreator}`}),
        onOk: async () => {
          await this.confirmDelete(data);
          await this.getTableData();
        }
      })
    },
    async confirmEnable(id, status) {
      let targetStatus = ''
      if(status === 'N') {
        targetStatus = 'Y';
      } else {
        targetStatus = 'N'
      }
      try {
        await api.fetch('/configuration/acrossClusterRule/isValid', { id, isValid: targetStatus }, 'put');
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
          await this.confirmEnable(data.id, data.isValid);
          await this.getTableData();
        },
      })
    },
    async confirmDelete(data) {
      try {
        const { id } = data
        await api.fetch('/configuration/acrossClusterRule/delete', { id }, 'delete');
        this.$Message.success(this.$t('message.linkis.ipListManagement.deleteSuccess'));
      } catch(err) {
        return;
      }
    },
    thresholdValidator (rule, val, cb) {
      const threshold = Number(val)
      if(isNumber(threshold) && threshold >= 0 && threshold <= 10000) {
        cb()
      } else {
        cb(new Error(this.$t('message.linkis.ipListManagement.thresholdError')))
      }
    },
    percentageThresholdValidator (rule, val, cb) {
      const threshold = Number(val)
      if(isNumber(threshold) && threshold >= 0 && threshold <= 1) {
        cb()
      } else {
        cb(new Error(this.$t('message.linkis.ipListManagement.percentageThresholdError')))
      }
    },
    async changePage(val) {
      this.page.pageNow = val;
      await this.getTableData();
    },
    async search() {
      const { username, creator, clusterName, isValid } = this.queryData;
      this.confirmQuery = { username, creator, clusterName, isValid };
      this.page.pageNow = 1;
      await this.getTableData()
    },
    viewRule(row) {
      this.$refs.editor.editor.set(row.parsedRules)
      this.showViewModal = true;
      this.$refs.editor.editor.expandAll();
      this.modalTitle = this.$t('message.linkis.ipListManagement.viewRules')
    }
  },
  created() {
    this.isLogAdmin = storage.get('isLogAdmin') || '';
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
.page {
  bottom: 10px; 
  overflow: hidden; 
  text-align: center;
}
.ivu-tooltip-popper {
  .ivu-tooltip-content {
    .ivu-tooltip-inner-with-width {
      word-wrap: break-word;
    }
  }
}
.second-action-bar {
  margin-top: 10px;
  margin-bottom: -15px;
  .action-btn {
    margin-right: 5px;
  }
}
</style>
