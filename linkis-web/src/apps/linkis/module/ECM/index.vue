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
  <div class="ecm" >
    <Search :statusList="healthyStatusList" :ownerList="ownerList" @search="search" />
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <Table class="table-content ecm-table" border :width="tableWidth" :columns="columns" :data="pageDatalist">
      <template slot-scope="{row}" slot="instance">
        <a @click="getEngineConnList(row)">{{`${row.instance}`}}</a>
      </template>
      <template slot-scope="{row}" slot="usedResource">
        <span>{{ row.usedResource | formatResource }}</span>
      </template>
      <template slot-scope="{row}" slot="maxResource">
        <span>{{ row.maxResource | formatResource }}</span>
      </template>
      <template slot-scope="{row}" slot="lockedResource">
        <span>{{ row.lockedResource | formatResource }}</span>
      </template>
      <template slot-scope="{row}" slot="startTime">
        <span>{{ timeFormat(row) }}</span>
      </template>
      <template slot-scope="{row}" slot="labels">
        <Tooltip v-for="(item, index) in row.labels" :key="index" :content="`${item.stringValue}`" placement="top">
          <Tag type="border" color="primary">{{`${item.stringValue}`}}</Tag>
        </Tooltip>
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
      @on-visible-change="resetTagAdd"
      :title="$t('message.linkis.tagEdit')"
      v-model="isTagEdit"
      :mask-closable="false">
      <Form :label-width="80" @submit.native.prevent>
        <FormItem :label="`${$t('message.linkis.instanceName')}：`">
          <Input disabled v-model="formItem.instance" />
        </FormItem>
        <FormItem class="addTagClass" :label="`${$t('message.linkis.tableColumns.label')}：`">
          <WbTag ref="wbtags" :tagList="formItem.labels" :selectList="keyList" @addEnter="addEnter" @onCloseTag="onCloseTag" @editEnter="editEnter" ></WbTag>
        </FormItem>
        <FormItem :label="`${$t('message.linkis.tableColumns.status')}：`">
          <Select v-model="formItem.emStatus">
            <Option
              v-for="(item) in healthyStatusList"
              :label="item"
              :value="item"
              :key="item"/>
          </Select>
        </FormItem>
      </Form>
    </Modal>
    <Modal
      @on-ok="confirmKill"
      v-model="killModal">
      <div>
        <div class="tip">
          {{$t('message.linkis.tipForKill', {instance: killInfo.curInstance})}}
        </div>
        <div class="radio">
          {{$t('message.linkis.allEngine')}}
          <RadioGroup v-model="killInfo.all">
            <Radio :label="0">{{$t('message.linkis.no')}}</Radio>
            <Radio :label="1">{{$t('message.linkis.yes')}}</Radio>
          </RadioGroup>
        </div>
      </div>
    </Modal>
  </div>
</template>
<script>
import api from '@/common/service/api';
import moment from "moment";
import Search from '@/apps/linkis/module/ECM/search.vue';
import WbTag from '@/apps/linkis/components/tag';
import { debounce } from 'lodash'
export default {
  name: 'ECM',
  data() {
    return {
      keyList: [],
      statusList: [], // Searchable Status List(可搜索的状态列表)
      healthyStatusList: [], // Modifiable state list(可修改的状态列表)
      ownerList: [],
      loading: false,
      formItem: {
        instance: '',
        labels: [],
        emStatus: '',
        applicationName: '',
      },
      tagTitle: [],
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
      tableWidth: 0,
      // Open the label modification popup(开启标签修改弹框)
      isTagEdit: false,
      // 删除实例instance
      killInfo: {
        curInstance: '',
        all: 0,
      },
      killModal: false,
      tableData: [],
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      },
      columns: [
        {
          title: this.$t('message.linkis.tableColumns.instanceName'), // instance name(实例名称)
          key: 'instance',
          minWidth: 150,
          className: 'table-project-column',
          slot: 'instance'
        },
        {
          title: this.$t('message.linkis.tableColumns.status'), // state(状态)
          key: 'nodeHealthy',
          minWidth: 100,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.label'), // Label(标签)
          key: 'labels',
          minWidth: 160,
          className: 'table-project-column',
          slot: 'labels'
        },
        {
          title: this.$t('message.linkis.tableColumns.usedResources'), // resource used(已用资源)
          key: 'usedResource',
          className: 'table-project-column',
          slot: 'usedResource',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.maximumAvailableResources'), // maximum available resources(最大可用资源)
          key: 'maxResource',
          slot: 'maxResource',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.lockedResource'), // maximum available resources(最大可用资源)
          key: 'lockedResource',
          slot: 'lockedResource',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.initiator'), // initiator(启动者)
          key: 'owner',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.startTime'), // enable time(启用时间)
          key: 'startTime',
          className: 'table-project-column',
          slot: 'startTime',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.control.title'),
          key: 'action',
          width: '230',
          // fixed: 'right',
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  type: 'primary',
                  size: 'small',
                },
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.isTagEdit = true;
                    let obj = {};
                    obj.instance = params.row.instance;
                    let labels = params.row.labels || [];
                    // Convert label data to a component-renderable format(将标签数据转换成组件可渲染格式)
                    obj.labels = labels.map(item => {
                      return {
                        key: item.labelKey,
                        value: item.stringValue,
                        modifiable: item.modifiable || false,
                      }
                    })
                    obj.emStatus = params.row.nodeHealthy;
                    obj.applicationName = params.row.applicationName;
                    this.formItem = Object.assign(this.formItem, obj)
                  }
                }
              }, this.$t('message.linkis.tagEdit')),
              h('Button', {
                props: {
                  type: 'error',
                  size: 'small'
                },
                style: {
                  display: sessionStorage.getItem('isLogAdmin') ? 'inline-block' : 'none'
                },
                on: {
                  click: () => {
                    this.killModal = true;
                    window.console.log(params.row);
                    this.killInfo.curInstance = params.row.instance;
                  }
                }
              }, this.$t('message.linkis.killAll'))
            ]);
          }
        }
      ],
    }
  },
  components: {
    Search,
    WbTag
  },
  computed: {
    pageDatalist() {// Displayed data(展示的数据)
      return this.tableData.filter((item, index) => {
        return (this.page.pageNow - 1) * this.page.pageSize <= index && index < this.page.pageNow * this.page.pageSize;
      })
    }
  },
  filters: {
    formatResource(v) {
      const calcCompany = function(num, isCompany = false) {
        let data = num > 0 ? num : 0;
        if (isCompany) {
          return data / 1024 / 1024 / 1024;
        }
        return data;
      }
      return  v && (v.cores !== undefined || v.memory !== undefined || v.instance !== undefined) ? `${calcCompany(v.cores)}cores,${calcCompany(v.memory, true)}G,${calcCompany(v.instance)}apps` : ''
    }
  },
  created() {
    this.initECMList();
    // Get a list of status information(获取状态信息列表)
    this.getListAllNodeHealthyStatus();
    this.getSearchStatus();
    this.getKeyList();
  },
  methods: {

    // refresh data(刷新数据)
    refreshResource() {
      this.initECMList();
    },
    // Initialize ECM list(初始化ECM列表)
    async initECMList() {
      // Get ECM instance data(获取ECM实例数据)
      this.loading = true;
      try {
        let ECM = await api.fetch('/linkisManager/listAllEMs','get') || {};
        // Get a list of used engine resources(获取使用的引擎资源列表)
        let ECMList = ECM.EMs || [];
        this.tableData = ECMList;
        this.ownerList = [];
        ECMList.forEach(item => {
          if(this.ownerList.indexOf(item.owner) === -1) {
            this.ownerList.push(item.owner)
          }
        })
        this.page.totalSize = this.tableData.length;
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

        let list = healthyStatusList.nodeHealthy || [];
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
      this.formItem.labels.push({ key, value });
    },

    // modify tag(修改tag)
    editEnter(editInputKey, editInputValue,editedInputValue) {
      let index = this.formItem.labels.findIndex((item)=>{
        return  item.value === editInputValue
      })
      this.formItem.labels.splice(index,1,{key: editInputKey,modifiable: true,value: editedInputValue})
    },

    // delete tag(删除tag)
    onCloseTag (name, index) {
      this.formItem.labels.splice(index, 1);
    },

    //  Submit changes(提交修改)
    submitTagEdit() {
      let param = JSON.parse(JSON.stringify(this.formItem));
      param.labels = param.labels.map(item => {
        return {
          labelKey: item.key,
          stringValue: item.value,
        }
      })
      api.fetch('/linkisManager/modifyEMInfo', param, 'put').then(() => {
        this.isTagEdit = false;
        this.$Message.success(this.$t('message.linkis.editedSuccess'));
        this.refreshResource(); // refresh(刷新)
      }).catch(() => {
        this.isTagEdit = false;
      })
    },
    // Toggle pagination(切换分页)
    change(val) {
      this.page.pageNow = val;
    },
    // page size change(页容量变化)
    changeSize(val) {
      this.page.pageSize = val;
      this.page.pageNow = 1;
    },
    // search(搜索)
    search(e) {
      let param = {
        instance: e.instance,
        nodeHealthy: e.nodeHealthy,
        owner: e.owner,
        tenantLabel: e.tenant,
      }
      api.fetch('/linkisManager/listAllEMs',param,'get').then((res)=>{
        this.tableData = res.EMs
      })


      this.page.pageNow = 1;
      this.page.totalSize = this.tableData.length;
    },
    // Jump to engine list(跳转到引擎列表)
    getEngineConnList(e) {
      this.$router.push({ name: 'EngineConnList', query: { instance: e.instance, applicationName: e.applicationName } })
    },
    // time format conversion(时间格式转换)
    timeFormat(row) {
      return moment(new Date(row.startTime)).format('YYYY-MM-DD HH:mm:ss')
    },
    resetTagAdd(v) {
      if (v===false && this.$refs.wbtags) {
        this.$refs.wbtags.resetTagAdd()
      }
    },
    confirmKill: debounce(async function() {
      try {
        const res = await api.fetch('/linkisManager/rm/killUnlockEngineByEM', {
          instance: this.killInfo.curInstance,
          withMultiUserEngine: this.killInfo.all === 1 ? true : false,
        }, 'post');
        const { killEngineNum, memory, cores } = res.result
        window.console.log(res);
        this.killModal = false;
        this.killInfo = {
          curInstance: '',
          all: 0,
        }
        // 传回的是Byte
        this.$Message.success(this.$t('message.linkis.killFinishedInfo', { killEngineNum, memory: memory / 1024 / 1024 / 1024, cores }))
      } catch (err) {
        window.console.warn(err);
        this.killInfo = {
          curInstance: '',
          all: 0,
        }
      }
    }, 1000, { leading: true })
  }
}
</script>

<style src="./index.scss" lang="scss" scoped></style>

<style lang="scss">
.ecm-table {
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
