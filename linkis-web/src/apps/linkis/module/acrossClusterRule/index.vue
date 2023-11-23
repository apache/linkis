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
        <span :title="$t('message.linkis.ipListManagement.userName')" class="search-label">{{$t('message.linkis.ipListManagement.userName')}}</span>
        <Input
          v-model="queryData.username"
          class="input"
          :placeholder="$t('message.linkis.ipListManagement.inputUser')"
          @on-enter="search"
          style="width: 290px"
        ></Input>
      </Col>
      <Col span="6" class="search-item">
        <span :title="$t('message.linkis.ipListManagement.appName')" class="search-label">{{$t('message.linkis.ipListManagement.appName')}}</span>
        <Input
          v-model="queryData.creator"
          class="input"
          :placeholder="$t('message.linkis.ipListManagement.inputApp')"
          @on-enter="search"
          style="width: 290px"
        ></Input>
      </Col>
      <Col span="6" class="search-item">
        <span :title="$t('message.linkis.ipListManagement.cluster')" class="search-label">{{$t('message.linkis.ipListManagement.cluster')}}</span>
        <Input
          v-model="queryData.clusterName"
          class="input"
          :placeholder="$t('message.linkis.ipListManagement.inputCluster')"
          @on-enter="search"
          style="width: 290px"
        ></Input>
      </Col>
      <Col span="6">
        <Button type="primary" class="button" @click="search">{{
          $t('message.linkis.ipListManagement.search')
        }}</Button>
        <Button type="warning" class="button" @click="clearSearch">{{
          $t('message.linkis.ipListManagement.clear')
        }}</Button>
        <Button type="success" class="button" @click="createRules">{{
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
          <FormItem :label="$t('message.linkis.ipListManagement.userName')" prop="username">
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
          <FormItem :label="$t('message.linkis.ipListManagement.CPUThreshold')" prop="CPUThreshold">
            <Input class="input" v-model="modalData.CPUThreshold"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.MemoryThreshold')" prop="MemoryThreshold">
            <Input class="input" v-model="modalData.MemoryThreshold"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.CPUPercentageThreshold')" prop="CPUPercentageThreshold">
            <Input class="input" v-model="modalData.CPUPercentageThreshold"  placeholder="0~1"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.MemoryPercentageThreshold')" prop="MemoryPercentageThreshold">
            <Input class="input" v-model="modalData.MemoryPercentageThreshold"  placeholder="0~1"></Input>
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
      modalTitle: '',
      loading: false,
      queryData: {
        username: '',
        creator: ''
      },
      confirmQuery: {
        username: '',
        creator: '',
      },
      tableColumns: [
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
          width: 550,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.isValid'),
          key: 'isValid',
          width: 200,
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('span', params.row.isValid === 'Y' ? this.$t('message.linkis.ipListManagement.yes') : this.$t('message.linkis.ipListManagement.no'))
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
      tableLoading: false,
      showCreateModal: false,
      modalData: {
        username: '',
        creator: '',
        clusterName: '',
        CPUThreshold: '',
        CPUPercentageThreshold: '',
        MemoryThreshold: '',
        MemoryPercentageThreshold: '',
        startTime: '',
        endTime: '',
        isValid: 'Y'
      },
      modalDataRule: {
        username: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.ipListManagement.contentError1'), type: 'string'}
        ],
        creator: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.ipListManagement.contentError1'), type: 'string'}
        ],
        parsedRules: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur', type: 'object'},
          {validator: this.ruleJsonValidator, trigger: 'blur'}
        ],
        clusterName: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.ipListManagement.contentError1'), type: 'string'}
        ],
        startTime: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^([01]\d|2[0-3]):[0-5]\d$/, message: this.$t('message.linkis.ipListManagement.timeError'), type: 'string'}
        ],
        endTime: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^([01]\d|2[0-3]):[0-5]\d$/, message: this.$t('message.linkis.ipListManagement.timeError'), type: 'string'}
        ],
        MemoryThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.thresholdValidator, trigger: 'blur'},
        ],
        CPUThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.thresholdValidator, trigger: 'blur'},
        ],
        MemoryPercentageThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.percentageThresholdValidator, trigger: 'blur'},
          {type: 'string', max: 4, message: this.$t('message.linkis.ipListManagement.customLen', {length: '4'})}
        ],
        CPUPercentageThreshold: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {validator: this.percentageThresholdValidator, trigger: 'blur'},
          {type: 'string', max: 4, message: this.$t('message.linkis.ipListManagement.customLen', {length: '4'})}
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
    }
  },
  computed: {
    mapping () {
      return (this.modalData.username || 'username') + '-' + (this.modalData.creator || 'creator') + '  -->  ' + (this.modalData.ipList || 'IPList')
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
          // item.createTime = new Date(item.createTime).toLocaleString();
          return item;
        })
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
        username: '',
        creator: '',
        clusterName: '',
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
      this.modalTitle = this.$t('message.linkis.ipListManagement.createRules')
      this.modalData.username = this.userName;
    },
    cancel() {
      this.showCreateModal = false;
      this.modalData = {
        username: '',
        creator: '',
        clusterName: '',
        CPUThreshold: '',
        CPUPercentageThreshold: '',
        MemoryThreshold: '',
        MemoryPercentageThreshold: '',
        startTime: '',
        endTime: '',
        isValid: 'Y'
      }
      this.$refs.createRuleForm.resetFields();
    },
    addRules() {
      if(this.isRequesting) return;
      const restfulMode = this.mode === 'edit' ? 'put' : 'post';
      const target = this.mode === 'edit' ? '/configuration/acrossClusterRule/update' : '/configuration/acrossClusterRule/add';
      this.$refs.createRuleForm.validate(async (valid) => {
        if(valid) {
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
        id, username, creator, clusterName, parsedRules, isValid
      } = data
      const { CPUPercentageThreshold, MemoryPercentageThreshold, MemoryThreshold, CPUThreshold } = parsedRules?.thresholdRule || {};
      const { startTime, endTime } = parsedRules?.timeRule || {}
      this.modalData = {
        id, username, creator, clusterName, CPUPercentageThreshold, MemoryPercentageThreshold, MemoryThreshold, CPUThreshold, startTime, endTime, isValid
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
        const { username, creator } = data
        await api.fetch('/configuration/acrossClusterRule/delete', { username, creator }, 'delete');
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
      const { username, creator, clusterName } = this.queryData;
      this.confirmQuery = { username, creator, clusterName };
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
    this.userName = storage.get('userName') || storage.get('baseInfo', 'local').username || '';
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
</style>
