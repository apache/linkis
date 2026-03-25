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
  <div class="global-history">
    <div ref="searchBar" class="global-history-searchbar">
      <div class="searchbar-items">
        <div class="searchbar-item">
          <label class="label">{{$t('message.linkis.jobId')}}</label>
          <InputNumber
            v-model="searchBar.id"
            :placeholder="$t('message.linkis.formItems.id.placeholder')"
            style="width:100px;"
            :min="1"
          ></InputNumber>
        </div>
        <div v-if="isAdminModel" class="searchbar-item">
          <label class="label">{{$t('message.linkis.userName')}}</label>
          <Input
            :maxlength="50"
            v-model="searchBar.proxyUser"
            :placeholder="$t('message.linkis.searchName')"
            style="width:60px;"
          />
        </div>
        <div class="searchbar-item">
          <label class="label">{{$t('message.linkis.formItems.date.label')}}</label>
          <DatePicker
            :transfer="true"
            class="datepicker"
            :options="shortcutOpt"
            v-model="searchBar.shortcut"
            type="datetimerange"
            placement="bottom-start"
            format="yyyy-MM-dd HH:mm"
            :placeholder="$t('message.linkis.formItems.date.placeholder')"
            style="width: 250px"
            :editable="false"
            @on-change="onDatePickerChange"
          />
        </div>
        <div class="searchbar-item">
          <label class="label">{{$t('message.linkis.formItems.creator.label')}}</label>
          <Input
            :maxlength="50"
            v-model.trim="searchBar.creator"
            :placeholder="$t('message.linkis.formItems.creator.placeholder')"
            style="width:80px;"
          />
        </div>
        <div class="searchbar-item">
          <label class="label">{{$t('message.linkis.formItems.runType.label')}}</label>
          <Input
            :maxlength="50"
            v-model.trim="searchBar.runType"
            :placeholder="$t('message.linkis.formItems.runType.placeholder')"
            style="width:80px;"
          />
        </div>
        <div class="searchbar-item">
          <label class="label">{{$t('message.linkis.formItems.engine.label')}}</label>
          <Select v-model="searchBar.engine" style="width: 70px">
            <Option v-for="(item) in getEngineTypes" :label="item === 'all' ? $t('message.linkis.engineTypes.all'): item" :value="item" :key="item" />
          </Select>
        </div>
        <div class="searchbar-item">
          <label class="label">{{$t('message.linkis.formItems.status.label')}}</label>
          <Select v-model="searchBar.status" style="width: 70px">
            <Option
              v-for="(item) in statusType"
              :label="item.label"
              :value="item.value"
              :key="item.value"
            />
          </Select>
        </div>
        <Form v-show="showAdvance" class="global-history-searchbar" :model="searchBar" inline>
          <FormItem prop="instance" :label="$t('message.linkis.formItems.instance.label')">
            <Input
              v-model="searchBar.instance"
              :placeholder="$t('message.linkis.formItems.instance.placeholder')"
              style="width:150px;"
            />
          </FormItem>
          <FormItem prop="engineInstance" :label="$t('message.linkis.engineInstance.label')">
            <Input
              v-model.trim="searchBar.engineInstance"
              :placeholder="$t('message.linkis.engineInstance.placeholder')"
              style="width:150px;"
            />
          </FormItem>
        </Form>
      </div>
      <div class="search-btns">
        <Button
          class="search-btn"
          type="primary"
          @click="search(false)"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.search') }}</Button>
        <Button
          class="search-btn"
          type="warning"
          @click="reset"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.clearSearch') }}</Button>
        <Button class="search-btn" type="error" @click="stop" style="margin-right: 10px;">{{$t('message.linkis.stop')}}</Button>
        <Button
          class="search-btn"
          type="primary"
          @click="switchAdmin(false)"
          v-show="isLogAdmin || isHistoryAdmin || isDeptAdmin"
          style="margin-right: 10px;"
        >{{ isAdminModel ? $t('message.linkis.generalView') : (isDeptAdmin && !isLogAdmin && !isHistoryAdmin ? $t('message.linkis.deptManageView') : $t('message.linkis.manageView')) }}</Button>
        <Button
          class="search-btn"
          type="primary"
          @click="clickAdvance"
        >{{ showAdvance ? $t('message.linkis.hideAdvancedSearch') :  $t('message.linkis.showAdvancedSearch') }}</Button>
        <Dropdown placement="bottom-start" trigger="click">
          <Button
            class="search-btn"
            type="primary"
          >{{$t('message.linkis.tableSetting')}}</Button>
          <template #list>
            <DropdownMenu>
              <CheckboxGroup v-model="visibleColumns" style="display: flex; flex-direction: column; padding: 10px 10px 0;">
                <Checkbox :label="item.title" v-for="(item, index) in column" v-show="item.title" :key="index" style="margin-bottom: 10px">
                  <span>{{item.title}}</span>
                </Checkbox>
              </CheckboxGroup>
            </DropdownMenu>
          </template>
        </Dropdown>
        <Button
          class="search-btn"
          type="primary"
          @click="download"
          :loading="downloading"
        >{{ $t('message.linkis.downloadLog') }}</Button>
      </div>
      
    </div>
    <div class="global-history-table" :style="{width: '100%', 'height': moduleHeight +'px'}">
      <Icon v-show="isLoading" type="ios-loading" size="30" class="global-history-loading" />
      <history-table
        v-if="!isLoading"
        :columns="filteredColumns"
        :data="list"
        :height="moduleHeight"
        :no-data-text="$t('message.linkis.noDataText')"
        border
        stripe
        @checkall="checkChange"
        @select-change="selectChange"
      />
    </div>
    <div class="global-history-page">
      <Page
        :total="pageSetting.total"
        :page-size="pageSetting.pageSize"
        :current="pageSetting.current"
        :page-size-opts="pageSetting.sizeOpts"
        size="small"
        show-total
        show-elevator
        show-sizer
        :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
        @on-change="changePage"
        @on-page-size-change="changePageSize"
      />
    </div>
  </div>
</template>
<script>
import storage from '@/common/helper/storage'
import table from '@/components/virtualTable'
import mixin from '@/common/service/mixin'
import api from '@/common/service/api'
import axios from 'axios'
export default {
  name: 'GlobalHistory',
  components: {
    historyTable: table.historyTable
  },
  mixins: [mixin],
  data() {
    const today = new Date(new Date().toLocaleDateString())
    return {
      list: [],
      column: [],
      visibleColumns: [],
      getEngineTypes: [],
      isLoading: false,
      showAdvance: false,
      pageSetting: {
        total: 0,
        pageSize: 50,
        current: 1,
        sizeOpts: [25, 50, 100]
      },
      searchBar: {
        id: null,
        proxyUser: '',
        creator: '',
        engine: 'all',
        status: '',
        shortcut: [new Date(today.setHours(0, 0, 0, 0)), new Date(today.setHours(23, 59, 59, 0))],
      },
      inputType: 'number',
      shortcutOpt: {
        disabledDate(date) {
          if (!date) {
            return false;
          }

          const now = new Date();
          const ninetyDaysAgo = new Date(now);
          ninetyDaysAgo.setDate(now.getDate() - 90);
          ninetyDaysAgo.setHours(0, 0, 0, 0);

          return date.valueOf() < ninetyDaysAgo.valueOf() || date.valueOf() > now.valueOf();
        },
        shortcuts: [
          {
            text: this.$t('message.linkis.shortcuts.week'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              return [start, end]
            }
          },
          {
            text: this.$t('message.linkis.shortcuts.month'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
              return [start, end]
            }
          },
          {
            text: this.$t('message.linkis.shortcuts.threeMonths'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
              return [start, end]
            }
          }
        ]
      },
      statusType: [
        {
          label: this.$t('message.linkis.statusType.all'),
          value: 'all'
        },
        {
          label: this.$t('message.linkis.statusType.inited'),
          value: 'Inited'
        },
        {
          label: this.$t('message.linkis.statusType.running'),
          value: 'Running'
        },
        {
          label: this.$t('message.linkis.statusType.succeed'),
          value: 'Succeed'
        },
        {
          label: this.$t('message.linkis.statusType.cancelled'),
          value: 'Cancelled'
        },
        {
          label: this.$t('message.linkis.statusType.failed'),
          value: 'Failed'
        },
        {
          label: this.$t('message.linkis.statusType.scheduled'),
          value: 'Scheduled'
        },
        {
          label: this.$t('message.linkis.statusType.timeout'),
          value: 'Timeout'
        }
      ],
      engineTypes: [
        {
          label: this.$t('message.linkis.engineTypes.all'),
          value: 'all'
        },
        {
          label: 'spark',
          value: 'spark'
        },
        {
          label: 'hive',
          value: 'hive'
        },
        {
          label: 'pipeline',
          value: 'pipeline'
        },
        {
          label: 'python',
          value: 'python'
        },
        {
          label: 'flowexecution',
          value: 'flowexecution'
        },
        {
          label: 'appjoint',
          value: 'appjoint'
        },
        {
          label: 'shell',
          value: 'shell'
        }
      ],
      isLogAdmin: false,
      isHistoryAdmin: false,
      isDeptAdmin: false,
      isAdminModel: false,
      moduleHeight: 300,
      lastChooseDate: ['', ''],
      curDateTime: ['', ''],
      downloading: false
    }
  },
  async created() {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    try {
      await api.fetch('/jobhistory/governanceStationAdmin', 'get').then(res => {
        this.isLogAdmin = res.admin
        this.isHistoryAdmin = res.historyAdmin
        this.isDeptAdmin = res.deptAdmin
      })
      await api.fetch('/configuration/engineType', 'get').then(res => {
        this.getEngineTypes = ['all', ...res.engineType]
      })
    } catch(err) {
      window.console.warn(err);
    }

  },
  mounted() {
    if(sessionStorage.getItem('last-page-columns')) {
      this.visibleColumns = JSON.parse(sessionStorage.getItem('last-page-columns'))
    } else {
      this.visibleColumns = this.getColumns().filter(
        (item) => {
          return [
            'checked',
            'control',
            'taskID',
            'source',
            'executionCode',
            'status',
            'costTime',
            'failedReason',
            'requestApplicationName',
            'createdTime',
            'isReuse',
          ].includes(item.key)
        }
      ).map(item => item.title);
      this.visibleColumns.push(this.$t('message.linkis.tableColumns.user'));
    }
    
    this.init()
    // Monitor window changes and get browser width and height(监听窗口变化，获取浏览器宽高)
    window.addEventListener('resize', this.getHeight)
  },
  beforeDestroy() {
    // Monitor window changes and get browser width and height(监听窗口变化，获取浏览器宽高)
    storage.set('last-admin-model', this.isAdminModel)
    // storage.set('last-searchbar-status', this.searchBar)
    window.removeEventListener('resize', this.getHeight)
  },
  beforeRouteEnter(to, from, next) {
    if(from.name !== 'viewHistory') {
      sessionStorage.removeItem('last-admin-model');
      sessionStorage.removeItem('last-searchbar-status');
      sessionStorage.removeItem('last-pageSetting-status');
      sessionStorage.removeItem('last-searchbar-advance');
      sessionStorage.removeItem('last-page-columns');
    }
    next();
  },
  activated() {
    this.init()
  },
  computed: {
    filteredColumns() {
      return this.column.filter(item => this.visibleColumns.includes(item.title));
    }
  },
  methods: {
    async download() {
      try {
        
        if(this.downloading) return;
        if(this.pageSetting.total >= 10000) {
          this.$Modal.confirm({
            title: this.$t('message.linkis.countinueDownload'),
            content: this.$t('message.linkis.exceed10000'),
            onOk: async () => {
              try {
                const params = this.getParams();
                params.pageNow = 1;
                params.pageSize = 10000;
                this.downloading = true;
                const res = await axios({
                  url: process.env.VUE_APP_MN_CONFIG_PREFIX || `http://${window.location.host}/api/rest_j/v1/jobhistory/download-job-list`,
                  method: 'get',
                  responseType: 'blob',
                  params,
                  headers: {
                    'Content-Language': localStorage.getItem('locale') || 'zh-CN'
                  }
                })
                
                let blob = res.data
                let url = window.URL.createObjectURL(blob);
                let l = document.createElement('a')
                l.href = url;
                l.download = 'ResultOfJobHistory';
                document.body.appendChild(l);
                l.click()
                window.URL.revokeObjectURL(url)
                this.downloading = false
                this.$Message.success(this.$t('message.linkis.downloadSucceed'))
              } catch (err) {
                window.console.warn(err)
                this.downloading = false;
              }
            
            }
          })
        } else {
          this.downloading = true
          const params = this.getParams();
          params.pageNow = 1;
          params.pageSize = 10000;
          const res = await axios({
            url: process.env.VUE_APP_MN_CONFIG_PREFIX || `http://${window.location.host}/api/rest_j/v1/jobhistory/download-job-list`,
            method: 'get',
            responseType: 'blob',
            params,
            headers: {
              'Content-Language': localStorage.getItem('locale') || 'zh-CN'
            }
          })
          let blob = res.data
          let url = window.URL.createObjectURL(blob);
          let l = document.createElement('a')
          l.href = url;
          l.download = 'ResultOfJobHistory';
          document.body.appendChild(l);
          l.click()
          window.URL.revokeObjectURL(url)
          this.downloading = false
          this.$Message.success(this.$t('message.linkis.downloadSucceed'))
        }
       
      } catch(err) {
        this.downloading = false
      }
      
        
    },
    getHeight() {
      this.moduleHeight = this.$parent.$el.clientHeight - this.$refs.searchBar.offsetHeight - 210;
      if(this.showAdvance) {
        this.moduleHeight -= 10
      }
    },
    clickAdvance() {
      this.showAdvance = !this.showAdvance
      this.getHeight()
    },
    init() {
      let isAdminModel = storage.get('last-admin-model')
      const lastSearch = storage.get('last-searchbar-status')
      const lastPage = storage.get('last-pageSetting-status')
      this.showAdvance = storage.get('last-searchbar-advance')
      this.getHeight()
      if (lastSearch) {
        if (lastSearch.shortcut[0] && lastSearch.shortcut[1]) {
          lastSearch.shortcut = [new Date(lastSearch.shortcut[0]), new Date(lastSearch.shortcut[1])]
        } else {
          const today = new Date(new Date().toLocaleDateString())
          lastSearch.shortcut = [new Date(today.setHours(0, 0, 0, 0)), new Date(today.setHours(23, 59, 59, 0))]
        }
        this.searchBar = lastSearch
      }
      this.$nextTick(()=> {
        if (lastPage) {
          this.pageSetting = lastPage
        }
        if (isAdminModel) {
          this.switchAdmin(true)
        } else {
          this.search(true)
        }
      })
      storage.remove('last-pageSetting-status')
    },
    convertTimes(runningTime) {
      const time = Math.floor(runningTime / 1000)
      if (time < 0) {
        return `0${this.$t('message.linkis.time.second')}`
      } else if (time < 60) {
        return `${time}${this.$t('message.linkis.time.second')}`
      } else if (time < 3600) {
        return `${(time / 60).toPrecision(2)}${this.$t(
          'message.linkis.time.minute'
        )}`
      } else if (time < 86400) {
        return `${(time / 3600).toPrecision(2)}${this.$t(
          'message.linkis.time.hour'
        )}`
      }
      return `${(time / 86400).toPrecision(2)}${this.$t(
        'message.linkis.time.day'
      )}`
    },
    // Click to view historical details log and return results(点击查看历史详情日志和返回结果)
    async viewHistory(params) {
      let sourceJson = params.row.sourceJson
      if (typeof sourceJson === 'string') {
        try {
          sourceJson = JSON.parse(sourceJson)
        } catch (error) {
          window.console.log(sourceJson)
        }
      }
      let fileName = ''
      if (sourceJson && sourceJson.scriptPath) {
        fileName = sourceJson.scriptPath.split('/').pop()
      }
      if (sourceJson && sourceJson.nodeName) {
        fileName = sourceJson.nodeName
      }
      const query =  {
        taskID: params.row.taskID,
        execID: params.row.strongerExecId,
        status: params.row.status,
        fileName,
        engineInstance: params.row.engineInstance,
      }
      if (this.isAdminModel) {
        query.proxyUser = params.row.executeUser
      }
      storage.set('last-searchbar-status', this.searchBar)
      storage.set('last-pageSetting-status', this.pageSetting)
      storage.set('last-searchbar-advance', this.showAdvance)
      storage.set('last-page-columns', this.visibleColumns)
      // Jump to view the history details page(跳转查看历史详情页面)
      this.$router.push({
        path: '/console/viewHistory',
        query
      })
    },

    getParams(page) {
      const startDate = this.searchBar.shortcut[0] ? new Date(this.searchBar.shortcut[0].setSeconds(0)) : this.searchBar.shortcut[0]
      const endDate = this.searchBar.shortcut[1] ? new Date(this.searchBar.shortcut[1].setSeconds(59)): this.searchBar.shortcut[1]
      const params = {
        taskID: this.searchBar.id,
        runType: this.searchBar.runType,
        creator: this.searchBar.creator,
        executeApplicationName: this.searchBar.engine,
        status: this.searchBar.status,
        startDate: startDate && startDate.getTime(),
        endDate: endDate && endDate.getTime(),
        engineInstance: this.searchBar.engineInstance,
        pageNow: page || this.pageSetting.current,
        pageSize: this.pageSetting.pageSize,
        proxyUser: this.searchBar.proxyUser,
        isAdminView: this.isAdminModel && (storage.get('isLogAdmin') || storage.get('isLogHistoryAdmin')),
        instance: this.searchBar.instance?.replace(/ /g, '') || '',
        isDeptView: this.isAdminModel && storage.get('isLogDeptAdmin') && !storage.get('isLogAdmin') && !storage.get('isLogHistoryAdmin')
      }
      if (!this.isAdminModel) {
        delete params.proxyUser
      }
      if (this.searchBar.id) {
        delete params.creator
        delete params.executeApplicationName
        delete params.status
        delete params.startDate
        delete params.endDate
        delete params.proxyUser
        delete params.instance
        delete params.engineInstance
        delete params.runType
      } else {
        let { engine, status, shortcut } = this.searchBar
        if (engine === 'all') {
          delete params.executeApplicationName
        }
        if (status === 'all') {
          delete params.status
        }
        if (!shortcut[0]) {
          delete params.startDate
        }
        if (!shortcut[1]) {
          delete params.endDate
        }
        delete params.taskID
      }
      return params
    },
    changePage(page) {
      this.isLoading = true
      const params = this.getParams(page)
      this.column = this.getColumns()
      api
        .fetch('/jobhistory/list', params, 'get')
        .then(rst => {
          this.isLoading = false
          this.list = this.getList(rst.tasks)
          // this.pageSetting.current = page
          const maxPage = Math.ceil(rst.totalPage / this.pageSetting.pageSize);
          if(maxPage < page) {
            this.pageSetting.current = maxPage;
          } else {
            this.pageSetting.current = page;
          }
          this.pageSetting.total = rst.totalPage;
        })
        .catch(() => {
          this.isLoading = false
          this.list = []
        })
    },
    // page size change(页容量变化)
    changePageSize(pageSize) {
      this.pageSetting.pageSize = pageSize
      if(this.pageSetting.current === 1) this.search()
      this.pageSetting.current = 1
    },
    search(isInit = false) {
      this.isLoading = true
      if(!isInit) {
        this.pageSetting.current = 1;
      }
      const params = this.getParams()
      
      this.column = this.getColumns()
      api
        .fetch('/jobhistory/list', params, 'get')
        .then(rst => {
          this.pageSetting.total = rst.totalPage
          this.isLoading = false
          this.list = this.getList(rst.tasks)
        })
        .catch(() => {
          this.list = []
          this.isLoading = false
        })
    },
    getList(list) {
      const getFailedReason = item => {
        return item.errCode && item.errDesc
          ? item.errCode + item.errDesc
          : item.errCode || item.errDesc || ''
      }
      if (!this.isAdminModel) {
        return list.map(item => {
          return {
            disabled: ['Submitted', 'Inited', 'Scheduled', 'Running'].indexOf(item.status) === -1,
            taskID: item.taskID,
            strongerExecId: item.strongerExecId,
            source: item.sourceTailor,
            executionCode: item.executionCode,
            status: item.status,
            costTime: item.costTime,
            requestApplicationName: item.requestApplicationName,
            executeApplicationName: item.executeApplicationName,
            createdTime: item.createdTime,
            progress: item.progress,
            failedReason: getFailedReason(item),
            runType: item.runType,
            instance: item.instance,
            engineInstance: item.engineInstance,
            isReuse: item.isReuse === null 
              ? '' 
              : item.isReuse 
                ? this.$t('message.linkis.yes') 
                : this.$t('message.linkis.no'),
            requestSpendTime: item.requestSpendTime,
            requestStartTime: item.requestStartTime,
            requestEndTime: item.requestEndTime,
            metrics: item.metrics
          }
        })
      }
      return list.map(item => {
        return Object.assign(item, {
          disabled:
              ['Submitted', 'Inited', 'Scheduled', 'Running'].indexOf(item.status) === -1,
          failedReason: getFailedReason(item),
          source: item.sourceTailor,
          isReuse: item.isReuse === null 
            ? '' 
            : item.isReuse 
              ? this.$t('message.linkis.yes') 
              : this.$t('message.linkis.no'),
        })
      })
    },
    checkChange(v) {
      this.list = this.list.map(it => {
        it.checked = !it.disabled && v
        return it
      })
    },
    selectChange() {
      this.list = this.list.slice(0)
    },
    getColumns() {
      const column = [
        {
          title: '',
          key: 'checked',
          align: 'center',
          width: 60,
          renderType: 'checkbox'
        },
        {
          title: this.$t('message.linkis.tableColumns.control.title'),
          key: 'control',
          fixed: 'right',
          align: 'center',
          width: 60,
          renderType: 'button',
          renderParams: [
            {
              label: this.$t('message.linkis.tableColumns.control.label'),
              action: this.viewHistory
            }
          ]
        },
        {
          title: this.$t('message.linkis.tableColumns.taskID'),
          key: 'taskID',
          align: 'center',
          width: 90
        },
        {
          title: this.$t('message.linkis.tableColumns.fileName'),
          key: 'source',
          align: 'center',
          ellipsis: true,
          width: 170
        },
        {
          title: this.$t('message.linkis.tableColumns.executionCode'),
          key: 'executionCode',
          align: 'center',
          width: 420,
          // overflow to show(溢出以...显示)
          ellipsis: true
          // renderType: 'tooltip',
        },
        {
          title: this.$t('message.linkis.tableColumns.status'),
          key: 'status',
          align: 'center',
          width: 164,
          renderType: 'if',
          renderParams: {
            action: this.setRenderType
          }
        },
        {
          title: this.$t('message.linkis.tableColumns.costTime'),
          key: 'costTime',
          align: 'center',
          width: 100,
          renderType: 'convertTime'
        },
        {
          title: this.$t('message.linkis.tableColumns.failedReason'),
          key: 'failedReason',
          align: 'center',
          className: 'history-failed',
          width: 200,
          renderType: 'a',
          renderParams: {
            hasDoc: this.checkIfHasDoc,
            action: this.linkTo
          }
        },
        {
          title: this.$t('message.linkis.tableColumns.isReuse'),
          key: 'isReuse',
          align: 'center',
          width: 100,
          // renderType: 'tooltip',
        },
        {
          title: this.$t('message.linkis.tableColumns.requestStartTime'),
          key: 'requestStartTime',
          align: 'center',
          width: 150,
          // overflow to show(溢出以...显示)
          renderType: 'formatTime'
          // renderType: 'tooltip',
        },
        {
          title: this.$t('message.linkis.tableColumns.requestEndTime'),
          key: 'requestEndTime',
          align: 'center',
          width: 150,
          renderType: 'formatTime'
          // renderType: 'tooltip',
        },
        {
          title: this.$t('message.linkis.tableColumns.requestSpendTime'),
          key: 'requestSpendTime',
          align: 'center',
          width: 100,
          // overflow to show(溢出以...显示)
          renderType: 'convertTime'
          // renderType: 'tooltip',
        },
        {
          title: this.$t('message.linkis.tableColumns.metrix'),
          key: 'metrics',
          align: 'center',
          width: 300,
          // overflow to show(溢出以...显示)
          ellipsis: true
          // renderType: 'tooltip',
        },
        {
          title: this.$t('message.linkis.tableColumns.requestApplicationName') + ' / ' +  this.$t('message.linkis.tableColumns.runType') + ' / ' + this.$t('message.linkis.tableColumns.executeApplicationName'),
          key: 'requestApplicationName',
          align: 'center',
          width: 130,
          renderType: 'multiConcat',
          renderParams: {
            concatKey1: 'runType',
            concatKey2: 'executeApplicationName'
          }

        },
        {
          title: this.$t('message.linkis.tableColumns.user'),
          key: 'umUser',
          align: 'center',
          width: 80
        },
        {
          title: this.$t('message.linkis.tableColumns.createdTime'),
          key: 'createdTime',
          align: 'center',
          width: 150,
          renderType: 'formatTime'
        }
      ]
      if (!this.isAdminModel) {
        const index = column.findIndex(item => item.key === 'umUser')
        column.splice(index, 1)
      }
      return column
    },
    reset() {
      this.searchBar = {
        id: null,
        proxyUser: '',
        creator: '',
        engine: 'all',
        status: 'all',
        shortcut: ''
      }
    },
    switchAdmin(isInit = false) {
      if (!this.isLoading) {
        if (this.isAdminModel) {
          this.searchBar.id = null
          this.searchBar.proxyUser = ''

        }
        this.isAdminModel = !this.isAdminModel
        this.search(isInit)
      }
    },
    linkTo(params) {
      this.$router.push({
        path: '/console/FAQ',
        query: {
          errCode: parseInt(params.row.failedReason),
          isSkip: true
        }
      })
    },
    checkIfHasDoc(params) {
      const errCodeList = [11011, 11012, 11013, 11014, 11015, 11016, 11017]
      const errCode = parseInt(params.row.failedReason)
      if (errCodeList.indexOf(errCode) !== -1) {
        return true
      }
      return false
    },
    setRenderType(params) {
      if (params.row.status === 'Running' && params.row.progress !== 0) {
        return {
          type: 'Progress',
          value: params.row.progress
        }
      } else {
        return {
          type: 'Tag',
          value: params.row.status
        }
      }
    },
    stop() {
      const selected = this.list.filter(it => it.checked)
      if (!selected.length) {
        this.$Message.warning(this.$t('message.linkis.unselect'))
        return;
      }
      this.$Modal.confirm({
        title: this.$t('message.linkis.modal.modalTitle'),
        content: this.$t('message.linkis.modal.modalDeleteTask'),
        onOk: ()=>{
          const inst = {}
          selected.forEach(it => {
            if (inst[it.instance]) {
              inst[it.instance].taskIDList.push(it.taskID)
              inst[it.instance].idList.push(it.strongerExecId)
            } else {
              inst[it.instance] = {
                taskIDList: [it.taskID],
                idList: [it.strongerExecId]
              }
            }
          })
          const p = []
          Object.keys(inst).forEach(instkey => {
            if (instkey) p.push(api.fetch(`/entrance/${inst[instkey].idList[0]}/killJobs`, { idList: inst[instkey].idList, taskIDList: inst[instkey].taskIDList }, 'post'))
          })

          Promise.all(p).then(()=> {
            this.$Message.success(this.$t('message.linkis.udf.success'));
            this.search(false)
          })
        }
      })
    },
    // 改造日期选择器选择日期时默认时间段为00:00-23:59
    onDatePickerChange(date) {
      // 兼容重复修改时间段00:00-00:00的情况
      const isLastChooseDefault = (this.lastChooseDate[0].slice(-5) === '00:00'&& this.lastChooseDate[1].slice(-5) === '00:00')
        && (date[0].slice(-5) === '00:00'&& date[1].slice(-5) === '00:00')
        && (this.lastChooseDate[0].slice(0, 10) === date[0].slice(0, 10) && this.lastChooseDate[1].slice(0, 10) === date[1].slice(0, 10))

      // 是否手动改动日期为00:00-00:00，条件：更改过日期 && （ 上次时间为00:00-(xx:xx) || (上次时间为（xx:xx)-00:00)）
      const isChooseDefault = (date[0].slice(-5) === '00:00'&& date[1].slice(-5) === '00:00')
          &&
          (
            (this.lastChooseDate[0].slice(-5) === '00:00'
              && ((this.lastChooseDate[1].slice(-5, -3) === '00' && this.lastChooseDate[1].slice(-2) !== '00') || (this.lastChooseDate[1].slice(-5, -3) !== '00' && this.lastChooseDate[1].slice(-2) === '00')))
            || (this.lastChooseDate[1].slice(-5) === '00:00'
              && ((this.lastChooseDate[0].slice(-5, -3) === '00' && this.lastChooseDate[0].slice(-2) !== '00') || (this.lastChooseDate[0].slice(-5, -3) !== '00' && this.lastChooseDate[0].slice(-2) === '00'))))

      if(isLastChooseDefault || isChooseDefault) {
        this.lastChooseDate = JSON.parse(JSON.stringify(date))
        return
      }
      // 修改日期默认为00:00-23:59
      if(date[0].slice(-5) === '00:00'&& date[1].slice(-5) === '00:00') {
        this.searchBar.shortcut = [new Date(new Date(date[0]).setHours(0, 0, 0, 0)), new Date(new Date(date[1]).setHours(23, 59, 59, 0))]
        this.curDateTime =  JSON.parse(JSON.stringify([date[0].split(' ')[0]+' 00:00',  date[1].split(' ')[0] + ' 23:59']))
      } else {
        this.curDateTime =  JSON.parse(JSON.stringify(date))
      }
      this.lastChooseDate = JSON.parse(JSON.stringify(this.curDateTime))
    },
  }
}
</script>
<style src="./index.scss" lang="scss">

</style>
