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
  <div class="resource-page">
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <div class="resource-title" :class="{'admin-title': !isAdminModel}">
      <!-- <span class="title-text" >{{$t('message.linkis.resourceManagement.resourceUsage')}}</span>
      <Icon class="title-icon" @click="refreshResource" type="md-refresh"></Icon> -->
      <Form v-if="isLogAdmin" class="global-history-searchbar" :style="{float: isAdminModel ? 'none' : 'right'}" :model="searchBar" inline>
        <FormItem v-show="isAdminModel" prop="userName" :label="$t('message.linkis.userName')">
          <Input
            :maxlength="50"
            v-model="searchBar.userName"
            :placeholder="$t('message.linkis.searchName')"
            style="width:120px;"
          />
        </FormItem>
        <FormItem v-show="isAdminModel" prop="appType" :label="$t('message.linkis.formItems.appType')">
          <Input
            :maxlength="50"
            v-model="searchBar.appType"
            :placeholder="$t('message.linkis.searchAppType')"
            style="width:120px;"
          />
        </FormItem>
        <FormItem v-show="isAdminModel" prop="status" :label="$t('message.linkis.formItems.engineType')">
          <Select v-model="searchBar.engineType" style="min-width:90px;">
            <Option
              v-for="(item) in engines"
              :label="item"
              :value="item"
              :key="item"
            />
          </Select>
        </FormItem>
        <FormItem>
          <Button
            v-show="isAdminModel"
            type="primary"
            @click="search"
            style="margin-right: 10px;margin-left: 20px;"
          >{{ $t('message.linkis.search') }}</Button>
          <Button
            v-show="isAdminModel"
            type="warning"
            @click="clearsearch"
            style="margin-right: 10px;"
          >{{ $t('message.linkis.clearSearch') }}</Button>
          <Button
            type="primary"
            @click="switchAdmin"
          >{{ isAdminModel ? $t('message.linkis.generalView') : $t('message.linkis.manageView') }}</Button>
          <Button
            v-show="isAdminModel"
            type="primary"
            @click="clickShowOperations"
            style="margin-left: 10px;"
          >{{ showOperations ? $t('message.linkis.hide') : $t('message.linkis.showOperations') }}</Button>
          <Button
            v-show="isAdminModel && showOperations"
            type="error" @click="() => {this.resetAll()}" style="margin-left: 10px;">{{$t('message.linkis.resetAll')}}</Button>
        </FormItem>
      </Form>
    </div>
    <div class="noData" v-if="!isAdminModel && progressDataList.length <= 0">No data, try refresh</div>
    <!-- progress bar(进度条) -->
    <template v-if="!isAdminModel">
      <div v-for="item in progressDataList" :key="item.id">
        <WbProgress @expandChange="expandChange" :progressData="item">
        </WbProgress>
        <template v-for="subItem in item.engineTypes">
          <WbProgress v-if="item.expand" :key="subItem.id" @expandChange="expandChange" :progressData="subItem" :children="true" :parentData="item"></WbProgress>
        </template>
      </div>
    </template>

    <!-- Application List(应用列表) -->
    <template v-if="isShowTable && !isAdminModel">
      <div class="resource-title appListTitle">
        {{$t('message.linkis.resourceManagement.applicationList')}}
      </div>
      <!-- App List Labels(应用列表标签) -->
      <div class="appListTag">
        <div class="tagName">
          <span class="span-white-space">{{$t('message.linkis.tableColumns.label')}}:</span>
          <Tag v-for="(item, index) in tagTitle" :key="index" color="primary">{{item}}</Tag>
        </div>
        <div class="resourceList">
          <span class="span-white-space">{{$t('message.linkis.resources')}}:</span>
          <span v-if="applicationList.usedResource">
            <Tag color="success">{{`${calcCompany(applicationList.usedResource)}`}}(used)</Tag>
            <Tag color="error">{{`${calcCompany(applicationList.maxResource)}`}}(max)</Tag>
            <Tag color="warning" v-if="applicationList.leftResource">{{`${calcCompany(applicationList.leftResource)}`}}(remain)</Tag>
          </span>
        </div>
        <div class="instanceNum" >
          <span class="span-white-space">{{$t('message.linkis.instanceNum')}}:</span>
          <span v-if="applicationList.usedResource">{{applicationList.usedResource.instance}} / {{applicationList.maxResource.instance}}</span>
        </div>
      </div>
      <Table  class="table-content" border :width="tableWidth" :columns="columns" :data="tableData">
        <template slot-scope="{row}" slot="engineInstance">
          <span>{{`${row.instance}`}}</span>
        </template>
        <template slot-scope="{row}" slot="usedResource">
          <span>{{`${calcCompany(row.resource.usedResource)}`}}</span>
        </template>
        <template slot-scope="{row}" slot="lockedResource">
          <span>{{`${calcCompany(row.resource.lockedResource)}`}}</span>
        </template>
        <template slot-scope="{row}" slot="maxResource">
          <span>{{`${calcCompany(row.resource.maxResource)}`}}</span>
        </template>
        <template slot-scope="{row}" slot="leftResource">
          <span>{{`${calcCompany(row.resource.leftResource)}`}}</span>
        </template>
        <template slot-scope="{row}" slot="minResource">
          <span>{{`${calcCompany(row.resource.minResource)}`}}</span>
        </template>
        <template slot-scope="{row}" slot="startTime">
          <span>{{ timeFormat(row) }}</span>
        </template>
      </Table>
    </template>
    <template v-if="isAdminModel">
      <Table class="table-content" border :width="tableWidth" :columns="admincolumns" :data="adminTableData">
        <template slot-scope="{row}" slot="usedResource">
          <span>{{`${calcCompanyAdmin(row, 'usedResource', 'cores')},${calcCompanyAdmin(row, 'usedResource', 'memory')},${calcCompanyAdmin(row, 'usedResource', 'instances')}`}}</span>
        </template>
        <template slot-scope="{row}" slot="lockedResource">
          <span>{{`${calcCompanyAdmin(row, 'lockedResource', 'cores')},${calcCompanyAdmin(row, 'lockedResource', 'memory')},${calcCompanyAdmin(row, 'lockedResource', 'instances')}`}}</span>
        </template>
        <template slot-scope="{row}" slot="maxResource">
          <span>{{`${calcCompanyAdmin(row, 'maxResource', 'cores')},${calcCompanyAdmin(row, 'maxResource', 'memory')},${calcCompanyAdmin(row, 'maxResource', 'instances')}`}}</span>
        </template>
        <template slot-scope="{row}" slot="leftResource">
          <span :class="'label-'+row.loadResourceStatus">{{`${calcCompanyAdmin(row, 'leftResource', 'cores')},${calcCompanyAdmin(row, 'leftResource', 'memory')},${calcCompanyAdmin(row, 'leftResource', 'instances')}`}}</span>
        </template>
        <template slot-scope="{row}" slot="yarnUsedResource">
          <span>{{`${calcCompanyAdmin(row, 'usedResource', 'cores', true)},${calcCompanyAdmin(row, 'usedResource', 'memory', true)},${calcCompanyAdmin(row, 'usedResource', 'instances', true)}`}}</span>
        </template>
        <template slot-scope="{row}" slot="yarnMaxResource">
          <span>{{`${calcCompanyAdmin(row, 'maxResource', 'cores', true)},${calcCompanyAdmin(row, 'maxResource', 'memory', true)},${calcCompanyAdmin(row, 'maxResource', 'instances', true)}`}}</span>
        </template>
        <template slot-scope="{row}" slot="yarnLeftResource">
          <span :class="'label-'+row.queueResourceStatus">{{`${calcCompanyAdmin(row, 'leftResource', 'cores', true)},${calcCompanyAdmin(row, 'leftResource', 'memory', true)},${calcCompanyAdmin(row, 'leftResource', 'instances', true)}`}}</span>
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
    </template>
  </div>
</template>
<script>
import WbProgress from '@/apps/linkis/components/progress';
import moment from "moment";
import api from '@/common/service/api';
export default {
  name: 'resourceManagement',
  data() {
    return {
      keyList: [],
      isChildren: false,
      stopList: [],
      loading: false,
      isLogAdmin: false, //Does the account have administrator privileges?(账户是否有管理员权限)
      formItem: {
        instance: '',
        labels: [],
        applicationName: '',
      },
      tagTitle: [],
      applicationList: {},
      currentEngineData: {}, // List of currently clicked engines(当前点击的引擎列表)
      currentParentsData: {}, // The main engine of the current click(当前点击的主引擎)
      isShowTable: false,
      progressDataList: [],
      tableWidth: 0,
      tableData: [],
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      },
      showOperations: false,
      columns: [
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
          title: this.$t('message.linkis.tableColumns.status'),
          key: 'status',
          minWidth: 100,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.label'),
          key: 'label',
          minWidth: 150,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.usedResources'),
          key: 'usedResource',
          className: 'table-project-column',
          slot: 'usedResource',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.lockedResource'),
          key: 'lockedResource',
          className: 'table-project-column',
          slot: 'lockedResource',
          minWidth: 150,
        },
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
                  type: 'error',
                  size: 'small',
                  disabled: params.row.isStop
                },
                style: {
                  marginRight: '5px',
                },
                on: {
                  click: () => {
                    this.$Modal.confirm({
                      title: this.$t('message.linkis.stop'),
                      content: this.$t('message.linkis.stopEngineTip'),
                      onOk: () => {
                        let data = [];
                        data.push({
                          engineType: params.row.applicationName,
                          engineInstance: params.row.instance,
                        });
                        api.fetch(`/linkisManager/rm/enginekill`, data).then(() => {
                          // Since there is a delay in the shutdown of the engine, record the engine, do front-end list filtering, refresh the page or refresh(由于引擎关闭有延迟所以记录引擎，做前端列表筛选，刷新页面或刷新)
                          this.stopList.push(params.row.instance);
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
              }, this.$t('message.linkis.stop')),
            ]);
          }
        }
      ],
      admincolumns: [
        {
          title: this.$t('message.linkis.userName'),
          key: 'username',
          minWidth: 100,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.appType'),
          key: 'creator',
          minWidth: 120,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.engineType'),
          key: 'engineTypeWithVersion',
          minWidth: 100,
          className: 'table-project-column'
        },
        {
          title: this.$t('message.linkis.tableColumns.engineUsed'),
          key: 'usedResource',
          minWidth: 120,
          className: 'table-project-column',
          slot: 'usedResource',
        },
        {
          title: this.$t('message.linkis.tableColumns.engineTop'),
          key: 'maxResource',
          minWidth: 150,
          className: 'table-project-column',
          slot: 'maxResource',
        },
        {
          title: this.$t('message.linkis.tableColumns.engineRemain'),
          key: 'leftResource',
          className: 'table-project-column',
          slot: 'leftResource',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.queueUsed'),
          key: 'yarnUsedResource',
          minWidth: 160,
          className: 'table-project-column',
          slot: 'yarnUsedResource',
        },
        {
          title: this.$t('message.linkis.tableColumns.queueTop'),
          key: 'yarnMaxResource',
          minWidth: 160,
          className: 'table-project-column',
          slot: 'yarnMaxResource',
        },
        {
          title: this.$t('message.linkis.tableColumns.queenRemain'),
          key: 'yarnLeftResource',
          className: 'table-project-column',
          slot: 'yarnLeftResource',
          minWidth: 160,
        },
      ],
      engines: [],
      searchBar: {},
      isAdminModel: false,
      adminTableData: []
    }
  },
  components: {
    WbProgress
  },
  computed: {
  },
  async created() {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    await api.fetch('/jobhistory/governanceStationAdmin', 'get').then(async (res) => {
      this.isLogAdmin = res.admin;
      if(this.isLogAdmin) {
        this.isAdminModel = true;
        this.page = {
          totalSize: 0,
          sizeOpts: [15, 30, 45],
          pageSize: 15,
          pageNow: 1
        }
        await this.search()
      } else {
        await this.initExpandList();
      }
      this.getKeyList();
      
    })
    await api.fetch('/linkisManager/rm/engineType', 'get').then(res => {
      this.engines =  ['all'].concat(res.engineType)
    })
    

  },
  methods: {
    // refresh progress bar(刷新进度条)
    refreshResource() {
      this.stopList = []; // Initialize the stop list. Due to the delay of the engine shutdown, this parameter is set for judgment.(初始化停止列表，由于引擎关闭有延迟固设置此参数做判断)
      this.initExpandList();
    },
    // Initialize main data(初始化主要数据)
    initData() {
      this.page = {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      };
      this.currentEngineData = {}; // List of currently clicked engines(当前点击的引擎列表)
      this.currentParentsData = {}; // The main engine of the current click(当前点击的主引擎)
      this.isShowTable = false;
      this.progressDataList = [];
      this.tableData = [];
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
    // Initialize the engine list(初始化引擎列表)
    async initExpandList() {
      // Initialization data(初始化数据)
      this.initData();
      // Get engine data(获取引擎数据)
      this.loading = true;
      try {
        let engines = await api.fetch('/linkisManager/rm/userresources','post') || {};
        // Get a list of used engine resources(获取使用的引擎资源列表)
        let enginesList = engines.userResources || [];
        enginesList.forEach((it, index) => {
          it.id = it.userCreator + index
          it.percent = it.percent * 100 + '%'
          it.title = it.userCreator
          it.expand = index === 0
          if (it.engineTypes) {
            it.engineTypes.forEach(sub => {
              sub.id = sub.engineType + 'sub' + index
              sub.percent = sub.percent * 100 + '%'
              sub.title = sub.engineType
              sub.expand = false
            })
          }
        })
        this.progressDataList = enginesList || [];
        if(enginesList.length) await this.expandChange(enginesList[0])
        this.loading = false;
      } catch (err) {
        this.loading = false;
      }
    },
    // expand and close(展开和关闭)
    async expandChange(item, isChildren = false, parentData = {},) {
      // show table(显示表格)
      this.isShowTable = true;
      this.isChildren = isChildren;
      // If the click is not a sublist, the current data will be cached(如果点击的不为子列表则缓存下当前数据)
      // Block if the same element is clicked twice(如果两次点击同一元素则阻止)
      if(item.id === this.currentEngineData.id) return;
      // if(item.id === this.currentParentsData.id && !isChildren) return;
      this.currentEngineData = item;
      this.loading = true;
      // Resource tab display(资源标签显示)
      this.tagTitle = [];
      if(!isChildren) {
        this.currentParentsData = item;
        this.tagTitle = Object.assign(this.tagTitle, [item.userCreator])
      } else {
        this.tagTitle = Object.assign(this.tagTitle, [parentData.userCreator, item.engineType])
      }
      let parameter = {
        userCreator: isChildren ? parentData.userCreator : this.currentParentsData.userCreator,
        engineType: isChildren ? this.currentEngineData.engineType : undefined
      };
      try {
        let engines = await api.fetch('/linkisManager/rm/applicationlist', parameter, 'post') || {};
        this.loading = false;
        let engineInstances = engines.applications[0].applicationList;
        this.applicationList = engineInstances || {};
        let tableData = engineInstances.engineInstances || [];
        // Check to see if there are any stopped engines(刷选是否有已经停止的引擎)
        if(this.stopList.length && tableData.length) tableData = tableData.map(item => {
          if(this.stopList.includes(item.instance)) item.isStop = true;
          return item;
        })
        this.tableData = tableData;
        //Update the data of formItem.applicationName(更新formItem.applicationName 的数据)
        this.formItem.applicationName = this.tableData[0].engineType;
        this.page.totalSize = this.tableData.length;
      } catch (errorMsg) {
        window.console.error(errorMsg);
        this.loading = false;
      }
    },
    // add tag(添加tag)
    addEnter (key, value) {
      this.formItem.labels.push({ key, value });
    },
    // delete tag(删除tag)
    onCloseTag (name, index) {
      this.formItem.labels.splice(index, 1);
    },
    // Toggle pagination(切换分页)
    change(val) {
      this.page.pageNow = val;
      this.search();
    },
    // page size change(页容量变化)
    changeSize(val) {
      this.page.pageSize = val;
      this.page.pageNow = 1;
      this.search();
    },
    // time format conversion(时间格式转换)
    timeFormat(row) {
      if (row.startTime.indexOf('CST') > 0) {
        return moment(new Date(row.startTime)).utc().subtract(6, 'hour').format('YYYY-MM-DD HH:mm:ss')
      }
      return moment(new Date(row.startTime)).format('YYYY-MM-DD HH:mm:ss')
    },
    calcCompany(resource) {
      const calcCompanyToData = function(num, isCompany = false) {
        let data = num > 0 ? num : 0;
        if (isCompany) {
          return data / 1024 / 1024 / 1024;
        }
        return data;
      }
      return resource && (resource.cores !== undefined || resource.memory !== undefined || resource.instance !== undefined) ? `${calcCompanyToData(resource.cores)}cores,${calcCompanyToData(resource.memory, true)}G,${calcCompanyToData(resource.instance)}apps` : ''
    },
    calcCompanyAdmin(row, field, type, yarn) {
      let data = ' -- '
      if (row.resourceType === 'LoadInstance') {
        if (!yarn) {
          data = row[field][type]
          if (type === 'instance' && data === undefined) {
            data = row[field]['instance']
          }
        }
      } else {
        if (yarn) {
          const yarnType = type === 'memory' ? 'queueMemory' : 'queueCores'
          data = row[field].yarnResource[yarnType]
        } else {
          // if(type === 'instance') {
          //   type = instances;
          // }
          data = row[field].loadInstanceResource[type] || row[field].loadInstanceResource['instances']
        }
      }
      if (type === 'memory' && data !== ' -- ') {
        data = data / 1024 / 1024 / 1024;
      }
      if (data !== ' -- ') {
        if (type === 'memory') {
          data = data + 'G'
        } else if (type === 'instance') {
          data = data + 'apps'
        } else {
          data = data + 'cores'
        }
      }
      return data;
    },
    async switchAdmin() {
      this.isAdminModel = !this.isAdminModel
      if(this.isAdminModel) {
        this.page = {
          totalSize: 0,
          sizeOpts: [15, 30, 45],
          pageSize: 15,
          pageNow: 1
        }
        this.search()
      } else {
        await this.initExpandList();
      }
      
    },
    async search() {
      const param = {
        username: this.searchBar.userName,
        creator: this.searchBar.appType,
        engineType: this.searchBar.engineType === 'all' ? '' : this.searchBar.engineType,
        page: this.page.pageNow,
        size: this.page.pageSize
      }
      await api.fetch('/linkisManager/rm/allUserResource', param, 'get').then((res) => {
        this.adminTableData = res.resources
        this.page.totalSize = res.total
      }).catch(() => {
      })
    },
    clearsearch() {
      this.searchBar.userName = ''
      this.searchBar.appType = ''
      this.searchBar.engineType = ''
      this.search()
    },
    clickShowOperations () {
      if (this.showOperations) {
        this.admincolumns.splice(this.admincolumns.length - 1, 1)
      } else {
        this.admincolumns.push(
          {
            title: this.$t('message.linkis.tableColumns.control.title'),
            key: 'action',
            width: '100',
            align: 'center',
            render: (h, params) => {
              return h('div', [
                h('Button', {
                  props: {
                    type: 'error',
                    size: 'small',
                    disabled: params.row.isStop
                  },
                  style: {
                    marginRight: '5px',
                  },
                  on: {
                    click: () => {
                      this.resetAll(params.row.id)
                    }
                  }
                }, this.$t('message.linkis.reset'))
              ]);
            }
          }
        )
      }
      this.showOperations = !this.showOperations
    },
    resetAll(resourceId) {
      this.$Modal.confirm({
        title: this.$t('message.linkis.reset'),
        content: resourceId !== undefined ? this.$t('message.linkis.resetTip'): this.$t('message.linkis.resetAllTip'),
        onOk: () => {
          const param = resourceId !== undefined ? { resourceId } : { }
          api.fetch('linkisManager/rm/resetResource', param, 'delete').then(() => {
            this.$Message.success(this.$t('message.linkis.udf.success'));
            this.search()
          }).catch(() => {
          })
        }
      })

    }
  }
}
</script>

<style src="./index.scss" lang="scss" scoped></style>
<style lang="scss">
  .resource-page {
    min-height: 250px;
    height: 100%;
    overflow: hidden;
    .ivu-table {
      overflow: auto;
    }
    .ivu-table:before {
      height: 0;
    }
    .admin-title {
      top: 36px;
      position: absolute;
      right: 40px;
    }
    .noData {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%,-50%)
      }
    .resource-title {
      .ivu-form {
        display: flex;
        .ivu-form-item {
            margin-bottom: 10px;
            .ivu-form-item-content {
                display: flex !important;
            }
        }
      }
    }
  }
</style>
