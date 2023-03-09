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
  <div :style="{height: '100%'}">
    <div v-show="!showviewlog" class="ecmEngine">
      <Search ref="search" :statusList="statusList" :engineTypes="engineTypes" @search="search" :stopbtn="true" :page="page" />
      <Spin
        v-if="loading"
        size="large"
        fix/>
      <Table class="table-content ecm-engine-table" border :width="tableWidth" :columns="columns" :data="tableData" @on-selection-change="selctionChange">
        <template slot-scope="{row}" slot="serviceInstance">
          <span>{{row.serviceInstance ? row.serviceInstance : '-'}}</span>
        </template>
        <template slot-scope="{row}" slot="usedResource">
          <!-- The background does not do the processing when returning, the following can be processed according to(后台未做返回时的处理，下面几个可按照处理) -->
          <span v-if="row.usedResource">{{`${calcCompany(row.usedResource.cores)}cores,${calcCompany(row.usedResource.memory, true)}G,${calcCompany(row.usedResource.instance)}apps`}}</span>
          <span v-else>-</span>
        </template>
        <template slot-scope="{row}" slot="labelValue" >
          <div class="tag-box">
            <Tooltip v-for="(item, index) in row.labelValue.split(',')" :key="index" :content="`${item}`" placement="top">
              <Tag class="tag-item" type="border" color="primary">{{`${item}`}}</Tag>
            </Tooltip>
          </div>
        </template>
        <template slot-scope="{row}" slot="usedTime">
          <span>{{ row.usedTime ? timeFormat(row) : '-' }}</span>
        </template>
      </Table>
      <div class="page-bar">
        <Page
          ref="page"
          :total="page.totalSize"
          :page-size-opts="page.sizeOpts"
          :page-size="page.pageSize"
          :current="page.pageNow"
          class-name="page"
          size="small"
          show-total
          show-sizer
          :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
          @on-change="change"
          @on-page-size-change="changeSize" />
      </div>
      <Modal
        @on-ok="submitTagEdit"
        :title="$t('message.linkis.tagEdit')"
        v-model="isTagEdit"
        :mask-closable="false"
        :ok-text="$t('message.common.ok')">
        <Form :model="formItem" :label-width="80">
          <FormItem :label="`${$t('message.linkis.instanceName')}`">
            <Input disabled v-model="formItem.serviceInstance" ></Input>
          </FormItem>
          <FormItem class="addTagClass" :label="`${$t('message.linkis.tableColumns.label')}：`">
            <WbTag :tagList="formItem.labelValue" :selectList="keyList" @addEnter="addEnter"></WbTag>
          </FormItem>
        </Form>
      </Modal>
    </div>
    <ViewLog ref="logPanel" v-show="showviewlog" @back="showviewlog = false" />
  </div>
</template>
<script>
import api from '@/common/service/api';
import moment from "moment";
import Search from './search.vue';
import WbTag from '@/apps/linkis/components/tag';
import ViewLog from './log'
export default {
  name: 'engineConn',
  data() {
    return {
      showviewlog: false,
      loading: false,
      healthyStatusList: [],
      engineTypes: [],
      keyList: [],
      statusList: [],
      formItem: {
        serviceInstance: '',
        labelValue: [],
        emStatus: '',
      },
      tagTitle: [],
      applicationList: {},
      addTagForm: { // form with new label(新增标签的form表单)
        key: '',
        value: ''
      },
      isShowTable: false,
      addTagFormRule: { // validation rules(验证规则)
        key: [
          { required: true, message: this.$t('message.linkis.keyTip'), trigger: 'blur' }
        ]
      },
      tableData: [],
      allEngines: [],
      tableWidth: 0,
      // Open the label modification popup(开启标签修改弹框)
      isTagEdit: false,
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      },
      columns: [
        // {
        //   type: 'selection',
        //   width: 60,
        //   align: 'center'
        // },
        {
          title: this.$t('message.linkis.tableColumns.engineInstance'),
          key: 'serviceInstance',
          minWidth: 160,
          className: 'table-project-column',
          slot: 'serviceInstance'
        },
        {
          title: this.$t('message.linkis.tableColumns.engineType'),
          key: 'engineType',
          minWidth: 100,
          className: 'table-project-column'
        },
        {
          title: this.$t('message.linkis.tableColumns.label'),
          key: 'labelValue',
          minWidth: 300,
          className: 'table-project-column',
          slot: 'labelValue'
        },
        {
          title: this.$t('message.linkis.tableColumns.usedResources'),
          key: 'usedResource',
          className: 'table-project-column',
          slot: 'usedResource',
          minWidth: 150,
        },
        /* {
          title: this.$t('message.linkis.tableColumns.maximumAvailableResources'),
          key: 'maxResource',
          slot: 'maxResource',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.minimumAvailableResources'),
          key: 'minResource',
          slot: 'minResource',
          minWidth: 150,
          className: 'table-project-column',
        }, */
        {
          title: this.$t('message.linkis.tableColumns.requestApplicationName'),
          key: 'createUser',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.startTime'),
          key: 'usedTime',
          className: 'table-project-column',
          slot: 'usedTime',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.control.title'),
          key: 'action',
          width: '100',
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  size: 'small'
                },
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.showviewlog = true
                    this.$refs.logPanel.getLogs(0, {
                      applicationName: "linkis-cg-engineconn",
                      emInstance: params.row.ecmInstance,
                      instance: params.row.serviceInstance,
                      ticketId: params.row.ticketId,
                      logDirSuffix: params.row.logDirSuffix,
                    })
                  }
                }
              }, this.$t('message.linkis.viewlog')),
            ]);
          }
        }
      ],
    }
  },
  components: {
    Search,
    WbTag,
    ViewLog
  },
  created() {
    this.initExpandList();
    // Get a list of status information(获取状态信息列表)
    this.getListAllNodeHealthyStatus();
    this.getSearchStatus();
    this.getKeyList();
  },
  methods: {
    selctionChange(selection) {
      this.selection = selection
    },
    // refresh progress bar(刷新进度条)
    refreshResource() {
      this.initExpandList();
    },
    // Initialize the engine list(初始化引擎列表)
    async initExpandList() {
      // Get engine data(获取引擎数据)
      this.loading = true;
      try {
        let url = '/linkisManager/ecinfo/ecrHistoryList?';
        if (this.page.pageNow) url += `pageNow=${this.page.pageNow}&`
        if (this.page.pageSize) url += `pageSize=${this.page.pageSize}`
        let engines = await api.fetch(url, 'get') || {};
        // Get a list of used engine resources(获取使用的引擎资源列表)
        let enginesList = engines.engineList || [];
        this.page.totalSize = engines.totalPage ? engines.totalPage : enginesList.length;
        this.allEngines = [ ...enginesList ];
        this.tableData = [ ...enginesList ];
        let data = await api.fetch('/configuration/engineType', 'get')
        this.engineTypes = data && data.engineType ? data.engineType : []
        this.loading = false;
      } catch (err) {
        window.console.log(err)
        this.loading = false;
      }
    },
    // Get all modifiable labelKeys(获取所有可修改的labelKey)
    getKeyList() {
      api.fetch('/microservice/modifiableLabelKey', 'get').then((res) => {
        let list = res.keyList || [];
        this.keyList = list.map(item => {
          return {
            lable: item,
            value: item
          }
        })
      })
    },
    // Get all modifiable state information(获取所有可修改的状态信息)
    async getListAllNodeHealthyStatus() {
      try {
        let healthyStatusList = await api.fetch('/linkisManager/listAllECMHealthyStatus', { onlyEditable: true }, 'get') || {};
        let list = healthyStatusList.nodeStatus || [];
        this.healthyStatusList = [...list];
      } catch (err) {
        window.console.log(err)
      }
    },
    // Get a list of states for a search(获取搜索的状态列表)
    async getSearchStatus() {
      try {
        let statusList = await api.fetch('/linkisManager/listAllNodeHealthyStatus', 'get') || {};
        let list = statusList.nodeStatus || [];
        this.statusList = [...list];
      } catch (err) {
        window.console.log(err)
      }
    },
    // add tag(添加tag)
    addEnter (key, value) {
      this.formItem.labelValue.push({ key, value });

    },
    //  Submit changes(提交修改)
    submitTagEdit() {
      let param = JSON.parse(JSON.stringify(this.formItem));
      param.instance = param.serviceInstance
      delete param.serviceInstance
      let tempLabelValue = ''
      param.labelValue.forEach((item, index) => {
        if (index === 0) {
          tempLabelValue += item.value
        } else {
          tempLabelValue += ',' + item.value
        }
      })
      param.labelValue = tempLabelValue
      api.fetch('/linkisManager/modifyEngineInfo', param, 'put').then(() => {
        this.isTagEdit = false;
        this.$Message.success(this.$t('message.linkis.editedSuccess'));
        this.refreshResource(); // 刷新
      }).catch(() => {
        this.isTagEdit = false;
      })
    },
    // Toggle pagination(切换分页)
    change(val) {
      this.page.pageNow = val;
      this.$refs.search.search(true);
    },
    // page size change(页容量变化)
    changeSize(val) {
      this.page.pageSize = val;
      this.page.pageNow = 1;
      this.$refs.search.search(true);
    },
    // search(搜索)
    search(e) {
      let url = '/linkisManager/ecinfo/ecrHistoryList?';
      if (e.instance) url += `instance=${e.instance}&`
      if (e.owner) url += `creator=${e.owner}&`
      if (e.shortcut[0]) url += `startDate=${moment(new Date(e.shortcut[0])).format('YYYY-MM-DD HH:mm:ss')}&`
      if (e.shortcut[1]) {
        if (moment(new Date(e.shortcut[1])).format('YYYY-MM-DD HH:mm:ss') === moment(new Date(e.shortcut[0])).format('YYYY-MM-DD HH:mm:ss')) {
          // If the start time is selected on the same day, the endDate will be added to the zero o'clock of the next day(如果起始时间选的是同一天，则endDate要加到第二天的零点)
          url += `endDate=${moment(new Date(e.shortcut[1]).getTime() + 24 * 60 * 60 * 1000).format('YYYY-MM-DD HH:mm:ss')}&`
        } else {
          url += `endDate=${moment(new Date(e.shortcut[1])).format('YYYY-MM-DD HH:mm:ss')}&`
        }
      }
      if (e.engineType) url += `engineType=${e.engineType}&`
      if (this.page.pageNow) url += `pageNow=${this.page.pageNow}&`
      if (this.page.pageSize) url += `pageSize=${this.page.pageSize}`
      api.fetch(url,'get').then((res)=>{
        this.tableData=res.engineList
        this.page.totalSize = res.totalPage ? res.totalPage : this.page.totalSize;
      })
    },
    // time format conversion(时间格式转换)
    timeFormat(row) {
      return moment(new Date(row.usedTime)).format('YYYY-MM-DD HH:mm:ss')
    },
    calcCompany(num, isCompany = false) {
      let data = num > 0 ? num : 0;
      if(isCompany) {
        return data / 1024 / 1024 / 1024;
      }
      return data;
    }
  }
}
</script>

<style src="./index.scss" lang="scss" scoped></style>

<style lang="scss">
.ecm-engine-table {
  border: 0;
  height: calc(100% - 110px);
  overflow: auto;

  .ivu-table:before {
    height: 0
  }

  .ivu-table:after {
    width: 0
  }

  .ivu-table {
    height: auto;
    border: 1px solid #dcdee2;
  }
}
</style>
