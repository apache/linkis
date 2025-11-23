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
      <Search :statusList="statusList" :ownerList="ownerList" :engineTypes="engineTypes" @search="search" @stop="stopAll" @batchUnhealth="batchUnhealth" :stopbtn="true" :healthbtn="true" />
      <Spin
        v-if="loading"
        size="large"
        fix/>
      <Table class="table-content ecm-engine-table" border :width="tableWidth" :columns="columns" :data="pageDatalist" @on-selection-change="selctionChange">
        <template slot-scope="{row}" slot="engineInstance">
          <span>{{row.instance}}</span>
        </template>
        <template slot-scope="{row}" slot="usedResource">
          <!-- The background does not do the processing when returning, the following can be processed according to(后台未做返回时的处理，下面几个可按照处理) -->
          <span v-if="row.usedResource">{{`${calcCompany(row.usedResource.cores)}cores,${calcCompany(row.usedResource.memory, true)}G,${calcCompany(row.usedResource.instance)}apps`}}</span>
          <span v-else>Null cores,Null G</span>
        </template>
        <!-- <template slot-scope="{row}" slot="maxResource">
          <span>Linkis:({{`${calcCompany(row.minResource.cores)}cores,${calcCompany(row.minResource.memory, true)}G`}})</span>
        </template> -->
        <!-- <template slot-scope="{row}" slot="minResource">
          <span>Linkis:({{`${calcCompany(row.maxResource.cores)}cores,${calcCompany(row.maxResource.memory, true)}G`}})</span>
        </template> -->
        <template slot-scope="{row}" slot="labels" >
          <div class="tag-box">
            <Tooltip v-for="(item, index) in row.labels" :key="index" :content="`${item.stringValue}`" placement="top">
              <Tag class="tag-item" type="border" color="primary">{{`${item.stringValue}`}}</Tag>
            </Tooltip>
          </div>
        </template>
        <template slot-scope="{row}" slot="startTime">
          <span>{{ timeFormat(row) }}</span>
        </template>
      </Table>
      <div class="page-bar">
        <Page
          ref="page"
          :total="this.tableData.length"
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
            <Input disabled v-model="formItem.instance" ></Input>
          </FormItem>
          <FormItem class="addTagClass" :label="`${$t('message.linkis.tableColumns.label')}：`">
            <WbTag :tagList="formItem.labels" :selectList="keyList" @addEnter="addEnter" @onCloseTag="onCloseTag" @editEnter="editEnter"></WbTag>
          </FormItem>
          <FormItem :label="`${$t('message.linkis.tableColumns.status')}：`">
            <Select v-model="formItem.emStatus" disabled>
              <Option
                v-for="(item) in statusList"
                :label="item"
                :value="item"
                :key="item"/>
            </Select>
          </FormItem>
          <FormItem :label="`${$t('message.linkis.healthInfo')}：`">
            <Select v-model="formItem.nodeHealthy">
              <Option
                v-for="(item) in healthinfoList"
                :label="item"
                :value="item"
                :key="item"/>
            </Select>
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
import Search from '@/apps/linkis/module/ECM/search.vue';
import WbTag from '@/apps/linkis/components/tag';
import ViewLog from './log'
export default {
  name: 'engineConn',
  data() {
    return {
      showviewlog: false,
      loading: false,
      healthyStatusList: [],
      healthinfoList: ['Healthy', 'UnHealthy'],
      ownerList: [],
      engineTypes: [],
      applicationName: '',
      instance: '',
      keyList: [],
      statusList: [],
      formItem: {
        instance: '',
        labels: [],
        emStatus: '',
        applicationName: '',
        nodeHealthy: ''
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
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('message.linkis.tableColumns.engineInstance'),
          key: 'engineInstance',
          minWidth: 150,
          className: 'table-project-column',
          slot: 'engineInstance'
        },
        {
          title: this.$t('message.linkis.tableColumns.engineType'),
          key: 'engineType',
          minWidth: 100,
          className: 'table-project-column'
        },
        {
          title: this.$t('message.linkis.tableColumns.healthInfo'),
          key: 'nodeHealthy',
          minWidth: 100,
          className: 'table-project-column'
        },
        {
          title: this.$t('message.linkis.tableColumns.status'),
          key: 'nodeStatus',
          minWidth: 100,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.label'),
          key: 'labels',
          minWidth: 300,
          className: 'table-project-column',
          slot: 'labels'
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
          key: 'owner',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.startTime'),
          key: 'startTime',
          className: 'table-project-column',
          slot: 'startTime',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.control.title'),
          key: 'action',
          width: '215',
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
                    this.$refs.logPanel.clearLogs()
                    
                    this.$refs.logPanel.getLogs(0, {
                      emInstance: params?.row?.emInstance || '',
                      instance: params?.row?.instance || '',
                      applicationName: params?.row?.applicationName || '',
                      engineType: params?.row?.engineType || '',
                    })
                  }
                }
              }, this.$t('message.linkis.viewlog')),
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
                    obj.emStatus = params.row.nodeStatus;
                    obj.applicationName = params.row.applicationName;
                    obj.nodeHealthy ='';
                    this.formItem = Object.assign(this.formItem, obj)
                  }
                }
              }, this.$t('message.linkis.tagEdit')),
              h('Button', {
                props: {
                  type: 'error',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.$Modal.confirm({
                      title: this.$t('message.linkis.stop'),
                      content: this.$t('message.linkis.stopEngineTip'),
                      onOk: () => {
                        let data = [];
                        data.push({
                          engineType: params.row.applicationName, // 当期需求是写死此参数
                          engineInstance: params.row.instance,
                        });
                        api.fetch(`/linkisManager/rm/enginekill`, data).then(() => {
                          this.initExpandList();
                          this.$Message.success({
                            background: true,
                            content: 'Stop Success！！'
                          });
                        }).catch((err) => {
                          window.console.err(err)
                        });
                      }
                    })
                  }
                }
              }, this.$t('message.linkis.stop'))
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
  computed: {
    pageDatalist() {// Displayed data(展示的数据)
      return this.tableData.filter((item, index) => {
        return (this.page.pageNow - 1) * this.page.pageSize <= index && index < this.page.pageNow * this.page.pageSize;
      })
    }
  },
  created() {
    this.applicationName = this.$route.query.applicationName || '';
    this.instance = this.$route.query.instance || '';
    this.initExpandList();
    // Get a list of status information(获取状态信息列表)
    this.getListAllNodeHealthyStatus();
    this.getSearchStatus();
    this.getKeyList();
  },
  methods: {
    stopAll() {
      if (!this.selection || !this.selection.length) {
        this.$Message.warning(this.$t('message.linkis.noselection'));
        return;
      }
      this.$Modal.confirm({
        title: this.$t('message.linkis.modal.modalTitle'),
        content: this.$t('message.linkis.modal.modalDeleteInstance'),
        onOk: () => {
          let data = [];
          this.selection.forEach(row => {
            data.push({
              engineType: row.applicationName,
              engineInstance: row.instance,
            });
          })
          api.fetch(`/linkisManager/rm/enginekill`, data).then(() => {
            this.initExpandList();
            this.$Message.success({
              background: true,
              content: 'Stop Success！！'
            });
          }).catch((err) => {
            window.console.err(err)
          });
        }
      })
    },
    batchUnhealth() {
      if (!this.selection || !this.selection.length) {
        this.$Message.warning(this.$t('message.linkis.noselection'));
        return;
      }
      this.$Modal.confirm({
        title: this.$t('message.linkis.modal.modalTitle'),
        content: this.$t('message.linkis.modal.modalunheathyInstance'),
        onOk: () => {
          let data = [];
          this.selection.forEach(row => {
            data.push({
              applicationName: row.applicationName,
              instance: row.instance,
              engineType: row.engineType,
            });
          })
          api.fetch(`/linkisManager/batchSetEngineToUnHealthy`, { instances: data }).then(() => {
            this.initExpandList();
            this.$Message.success({
              background: true,
              content: 'Modify Success！！'
            });
          }).catch((err) => {
            window.console.err(err)
          });
        }
      })
    },
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
        let params = {
          em: {
            serviceInstance: {
              applicationName: this.applicationName,
              instance: this.instance,
            }
          }
        }
        let engines = await api.fetch('/linkisManager/listEMEngines', params, 'post') || {};
        // Get a list of used engine resources(获取使用的引擎资源列表)
        let enginesList = engines.engines || [];
        this.allEngines = [ ...enginesList ];
        this.tableData = [ ...enginesList ];
        this.ownerList = [];
        this.engineTypes = []
        enginesList.forEach(item => {
          if (this.ownerList.indexOf(item.owner) === -1) {
            this.ownerList.push(item.owner)
          }
          if (this.engineTypes.indexOf(item.engineType) === -1) {
            this.engineTypes.push(item.engineType)
          }
        })
        this.loading = false;
      } catch (err) {
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
        return;
      }
    },
    // Get a list of states for a search(获取搜索的状态列表)
    async getSearchStatus() {
      try {
        let statusList = await api.fetch('/linkisManager/listAllNodeHealthyStatus', 'get') || {};
        let list = statusList.nodeStatus || [];
        this.statusList = [...list];
      } catch (err) {
        return;
      }
    },
    // add tag(添加tag)
    addEnter (key, value) {
      this.formItem.labels.push({ key, value });

    },
    // Edit tags(修改标签)
    editEnter(editInputKey, editInputValue,editedInputValue) {
      let index = this.formItem.labels.findIndex((item)=>{
        return  item.value === editInputValue
      })
      this.formItem.labels.splice(index,1,{key: editInputKey,modifiable: true,value: editedInputValue})
    },


    //delete tag( 删除tag)
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
      api.fetch('/linkisManager/modifyEngineInfo', param, 'put').then(() => {
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
        em: {
          serviceInstance: {
            applicationName: this.applicationName,
            instance: this.instance,
          }
        },
        emInstance: e.instance.replace(/ /g, ''),
        nodeStatus: e.nodeHealthy,
        owner: e.owner,
        engineType: e.engineType
      }
      api.fetch('/linkisManager/listEMEngines',param,'post').then((res)=>{
        this.tableData=res.engines
      })

      this.page.pageNow = 1;
      this.page.totalSize = this.tableData.length;
    },
    // time format conversion(时间格式转换)
    timeFormat(row) {
      return moment(new Date(row.startTime)).format('YYYY-MM-DD HH:mm:ss')
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
