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
  <div class="statistics-dashboard">
    <Form
      class="statistics-dashboard-searchbar"
      :model="searchBar"
      inline
    >
      <FormItem
        prop="shortcut"
        :label="$t('message.linkis.formItems.date.label')"
      >
        <DatePicker
          :transfer="true"
          class="datepicker"
          :options="shortcutOpt"
          v-model="searchBar.shortcut"
          type="daterange"
          placement="bottom-start"
          format="yyyy-MM-dd"
          :placeholder="$t('message.linkis.formItems.date.placeholder')"
          style="width: 175px"
          :editable="false"
        />
      </FormItem>
      <FormItem prop="creator" :label="$t('message.linkis.formItems.creator.label')">
        <Input
          :maxlength="50"
          v-model="searchBar.creator"
          :placeholder="$t('message.linkis.formItems.creator.placeholder')"
          style="width: 120px"
        />
      </FormItem>
      <FormItem prop="proxyUser" :label="$t('message.linkis.userName')">
        <Input
          :maxlength="50"
          v-model="searchBar.proxyUser"
          :placeholder="$t('message.linkis.searchName')"
          style="width: 120px"
        />
      </FormItem>
      <FormItem prop="engine" :label="$t('message.linkis.formItems.engine.label')">
        <Select v-model="searchBar.engine" style="width: 120px">
          <Option v-for="(item) in getEngineTypes" :label="item === 'all' ? $t('message.linkis.engineTypes.all'): item" :value="item" :key="item" />
        </Select>
      </FormItem>
      <FormItem>
        <Button
          type="primary"
          @click="search"
          style="margin-right: 10px; margin-top: 32px;"
        >{{ $t('message.linkis.search') }}</Button>
        <Button
          type="warning"
          @click="reset"
          style="margin-right: 10px; margin-top: 32px;"
        >{{ $t('message.linkis.clearSearch') }}</Button>
      </FormItem>
    </Form>
    <div class="pie-content">
      <div
        id="task"
        class="pie-chart"
      ></div>
      <div
        id="engine"
        class="pie-chart"
      ></div>
    </div>
    <div>
      <div class="statistics-dashboard-title">{{ $t('message.linkis.taskOverTimeTop') }}
        <InputNumber
          v-model="pageSetting.pageSize"
          style="width:80px;"
          :min="1"
          :max="100"
          :activeChange="false"
          @on-blur="handleBlur"
        ></InputNumber>
      </div>
      <div
        class="statistics-dashboard-table"
        :style="{width: '100%', 'height': moduleHeight+'px'}"
      >
        <Icon
          v-show="isLoading"
          type="ios-loading"
          size="30"
          class="statistics-dashboard-loading"
        />
        <history-table
          v-if="!isLoading"
          :columns="column"
          :data="list"
          :height="moduleHeight"
          :no-data-text="$t('message.linkis.noDataText')"
          border
          stripe
          @checkall="checkChange"
          @select-change="selectChange"
        />
      </div>
    </div>
  </div>
</template>
<script>
import storage from '@/common/helper/storage'
import table from '@/components/virtualTable'
import mixin from '@/common/service/mixin'
import api from '@/common/service/api'
var echarts = require('echarts')
export default {
  name: 'StatisticsDashboard',
  components: {
    historyTable: table.historyTable,
  },
  mixins: [mixin],
  data() {
    const today = new Date(new Date().toLocaleDateString())
    return {
      list: [],
      column: [],
      getEngineTypes: [],
      isLoading: false,
      pageSetting: {
        total: 0,
        pageSize: 10,
        current: 1,
      },
      searchBar: {
        creator: '',
        proxyUser: '',
        engine: 'all',
        status: '',
        shortcut: [today, today],
      },
      inputType: 'number',
      shortcutOpt: {
        shortcuts: [
          {
            text: this.$t('message.linkis.shortcuts.week'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              return [start, end]
            },
          },
          {
            text: this.$t('message.linkis.shortcuts.month'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
              return [start, end]
            },
          },
          {
            text: this.$t('message.linkis.shortcuts.threeMonths'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
              return [start, end]
            },
          },
        ],
      },
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
      isAdminModel: false,
      moduleHeight: 200,
      taskChart: [],
      engineChart: [],
    }
  },
  created() {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then((res) => {
      this.isLogAdmin = res.admin
      this.isHistoryAdmin = res.historyAdmin
    })
    api.fetch('/configuration/engineType', 'get').then((res) => {
      this.getEngineTypes = ['all', ...res.engineType]
    })
  },
  mounted() {
    this.init()
    this.moduleHeight = this.$parent.$el.clientHeight - 480
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
    if (from.name !== 'viewHistory') {
      sessionStorage.removeItem('last-admin-model')
      sessionStorage.removeItem('last-searchbar-status')
      sessionStorage.removeItem('last-pageSetting-status')
    }
    next()
  },
  activated() {
    this.init()
  },
  methods: {
    getHeight() {
      this.moduleHeight = this.$parent.$el.clientHeight - 480
      this.taskChart.resize()
      this.engineChart.resize()
    },
    init() {
      let isAdminModel = storage.get('last-admin-model')
      const lastSearch = storage.get('last-searchbar-status')
      const lastPage = storage.get('last-pageSetting-status')
      if (lastSearch) {
        if (lastSearch.shortcut[0] && lastSearch.shortcut[1]) {
          lastSearch.shortcut = [
            new Date(lastSearch.shortcut[0]),
            new Date(lastSearch.shortcut[1]),
          ]
        } else {
          const today = new Date(new Date().toLocaleDateString())
          lastSearch.shortcut = [today, today]
        }
        this.searchBar = lastSearch
      }
      if (lastPage) {
        this.pageSetting = lastPage
      }
      if (isAdminModel) {
        this.switchAdmin()
      } else {
        this.search()
      }
      storage.remove('last-pageSetting-status')
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
      const query = {
        taskID: params.row.taskID,
        execID: params.row.strongerExecId,
        status: params.row.status,
        fileName,
      }
      if (this.isAdminModel) {
        query.proxyUser = params.row.executeUser
      }
      storage.set('last-searchbar-status', this.searchBar)
      storage.set('last-pageSetting-status', this.pageSetting)
      // Jump to view the history details page(跳转查看历史详情页面)
      this.$router.push({
        path: '/console/statisticsDashboardHistory',
        query,
      })
    },

    getParams() {
      const startDate = this.searchBar.shortcut[0]
        ? new Date(this.searchBar.shortcut[0].setHours(0, 0, 0, 0))
        : this.searchBar.shortcut[0]
      const endDate = this.searchBar.shortcut[1]
        ? new Date(this.searchBar.shortcut[1].setHours(23, 59, 59, 0))
        : this.searchBar.shortcut[1]
      const params = {
        creator: this.searchBar.creator,
        proxyUser: this.searchBar.proxyUser,
        executeApplicationName: this.searchBar.engine,
        startDate: startDate && startDate.getTime(),
        endDate: endDate && endDate.getTime(),
        pageNow: 1,
        pageSize: this.pageSetting.pageSize

      }

      let { engine, shortcut } = this.searchBar
      if (engine === 'all') {
        delete params.executeApplicationName
      }

      if (!shortcut[0]) {
        delete params.startDate
      }
      if (!shortcut[1]) {
        delete params.endDate
      }

      return params
    },
    search() {
      this.isLoading = true
      let taskTotal = 0
      let taskSuccess = 0
      let taskFalied = 0
      let taskCancelled = 0
      let countEngine = 0
      let countEngineSucceed = 0
      let countEngineFailed = 0
      let countEngineShutting = 0
      const params = this.getParams()
      this.column = this.getColumns()
      api
        .fetch('/jobhistory/listDurationTop', params, 'get')
        .then((rst) => {
          this.pageSetting.total = rst.totalPage
          this.isLoading = false
          this.list = this.getList(rst.tasks)
        })
        .catch(() => {
          this.list = []
          this.isLoading = false
        })
      api
        .fetch('/jobhistory/jobstatistics/taskCount', params, 'get')
        .then((rst) => {
          this.isLoading = false
          taskTotal = rst.sumCount
          taskSuccess = rst.succeedCount
          taskFalied = rst.failedCount
          taskCancelled = rst.cancelledCount
          this.taskChart = echarts.init(document.getElementById('task'))
          this.taskChart.setOption({
            title: {
              text: this.$t('message.linkis.taskTotalNum')+'(' + taskTotal + ')',
              left: 'center',
            },
            tooltip: {
              trigger: 'item',
              formatter: '{b}:{c}',
            },
            legend: {
              orient: 'vertical',
              left: 'right',
              top: 'bottom',
              itemWidth: 30,
              formatter: '{name}',
              textStyle: {
                color: '#000000',
              },
              data: [
                { name: this.$t('message.linkis.statusType.succeed'), icon: 'rect' },
                { name: this.$t('message.linkis.statusType.failed'), icon: 'rect' },
                { name: this.$t('message.linkis.statusType.cancelled'), icon: 'rect' },
              ],
            },
            calculable: true,
            series: [
              {
                name: '',
                type: 'pie',
                radius: '55%',
                center: ['50%', '50%'],
                //roseType:'angle',
                label: {
                  normal: {
                    show: true,
                    // position: 'inner',
                    textStyle: {
                      fontWeight: 300,
                      fontSize: 14,
                      color: '#000000',
                    },
                    formatter: '{b}:{c}',
                  },
                  labelLine: { show: true },
                },
                data: [
                  { value: taskSuccess, name: this.$t('message.linkis.statusType.succeed') },
                  { value: taskFalied, name: this.$t('message.linkis.statusType.failed') },
                  { value: taskCancelled, name: this.$t('message.linkis.statusType.cancelled') },
                ],
              },
            ],
          })
        })
        .catch(() => {
          this.isLoading = false
        })
      api
        .fetch('/jobhistory/jobstatistics/engineCount', params, 'get')
        .then((rst) => {
          this.isLoading = false
          countEngine = rst.countEngine
          countEngineSucceed = rst.countEngineSucceed
          countEngineFailed = rst.countEngineFailed
          countEngineShutting = rst.countEngineShutting
          this.engineChart = echarts.init(document.getElementById('engine'))
          this.engineChart.setOption({
            title: {
              text: this.$t('message.linkis.engineTotalNum')+'(' + countEngine + ')',
              left: 'center',
            },
            tooltip: {
              trigger: 'item',
              formatter: '{b}:{c}',
            },
            legend: {
              orient: 'vertical',
              left: 'right',
              top: 'bottom',
              itemWidth: 30,
              formatter: '{name}',
              textStyle: {
                color: '#000000',
              },
              data: [
                { name: this.$t('message.linkis.statusType.succeed'), icon: 'rect' },
                { name: this.$t('message.linkis.statusType.failed') , icon: 'rect' },
                { name: this.$t('message.linkis.statusType.cancelled') , icon: 'rect' },
              ],
            },
            calculable: true,
            series: [
              {
                name: '',
                type: 'pie',
                radius: '55%',
                center: ['50%', '50%'],
                //roseType:'angle',
                label: {
                  normal: {
                    show: true,
                    // position: 'inner',
                    textStyle: {
                      fontWeight: 300,
                      fontSize: 14,
                      color: '#000000',
                    },
                    formatter: '{b}:{c}',
                  },
                  labelLine: { show: true },
                },
                data: [
                  { value: countEngineSucceed, name: this.$t('message.linkis.statusType.succeed') },
                  { value: countEngineFailed, name: this.$t('message.linkis.statusType.failed') },
                  { value: countEngineShutting, name: this.$t('message.linkis.statusType.cancelled') },
                ],
              },
            ],
          })
        })
        .catch(() => {
          this.isLoading = false
        })
    },
    getList(list) {
      const getFailedReason = (item) => {
        return item.errCode && item.errDesc
          ? item.errCode + item.errDesc
          : item.errCode || item.errDesc || ''
      }
      if (!this.isAdminModel) {
        return list.map((item) => {
          const paramsJson = JSON.parse(item.paramsJson)
          let jobName = ''
          try {
            if (paramsJson.configuration.startup.etl_job_name) {
              jobName = paramsJson.configuration.startup.etl_job_name
            }
          } catch (e) {
            jobName = ''
          }
          return {
            disabled:
              ['Submitted', 'Inited', 'Scheduled', 'Running'].indexOf(
                item.status
              ) === -1,
            taskID: item.taskID,
            jobName: jobName,
            strongerExecId: item.strongerExecId,
            source: item.sourceTailor,
            executionCode: item.executionCode,
            status: item.status,
            costTime: item.costTime,
            instance: item.instance,
            engineInstance: item.engineInstance,
            requestApplicationName: item.requestApplicationName,
            executeApplicationName: item.executeApplicationName,
            executeApplicationName2: item.labels
              .filter((item) => {
                return item.startsWith('engineType:')
              })[0]
              .substring(11),
            createdTime: item.createdTime,
            progress: item.progress,
            failedReason: getFailedReason(item),
            runType: item.runType,
            // instance: item.instance
          }
        })
      }
      return list.map((item) => {
        const paramsJson = JSON.parse(item.paramsJson)
        let jobName = ''
        try {
          if (paramsJson.configuration.startup.etl_job_name) {
            jobName = paramsJson.configuration.startup.etl_job_name
          }
        } catch (e) {
          jobName = ''
        }
        return Object.assign(item, {
          disabled:
            ['Submitted', 'Inited', 'Scheduled', 'Running'].indexOf(
              item.status
            ) === -1,
          executeApplicationName2: item.labels
            .filter((item) => {
              return item.startsWith('engineType:')
            })[0]
            .substring(11),
          jobName: jobName,
          failedReason: getFailedReason(item),
          source: item.sourceTailor,
        })
      })
    },
    checkChange(v) {
      this.list = this.list.map((it) => {
        it.checked = !it.disabled && v
        return it
      })
    },
    selectChange() {
      this.list = this.list.slice(0)
    },
    handleBlur(){
      this.isLoading = true
      const params = this.getParams()
      this.column = this.getColumns()
      api
        .fetch('/jobhistory/listDurationTop', params, 'get')
        .then((rst) => {
          this.pageSetting.total = rst.totalPage
          this.isLoading = false
          this.list = this.getList(rst.tasks)
        })
        .catch(() => {
          this.list = []
          this.isLoading = false
        })
    },
    getColumns() {
      const column = [
        {
          title: '',
          key: 'checked',
          align: 'center',
          width: 60,
          renderType: 'checkbox',
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
              action: this.viewHistory,
            },
          ],
        },
        {
          title: this.$t('message.linkis.tableColumns.taskID'),
          key: 'taskID',
          align: 'center',
          width: 80,
        },
        {
          title: this.$t('message.linkis.tableColumns.fileName'),
          key: 'source',
          align: 'center',
          ellipsis: true,
          width: 160,
        },
        {
          title: this.$t('message.linkis.tableColumns.executionCode'),
          key: 'executionCode',
          align: 'center',
          width: 280,
          // overflow to show(溢出以...显示)
          ellipsis: true,
          // renderType: 'tooltip',
        },
        {
          title: this.$t('message.linkis.tableColumns.status'),
          key: 'status',
          align: 'center',
          width: 164,
          renderType: 'if',
          renderParams: {
            action: this.setRenderType,
          },
        },
        {
          title: this.$t('message.linkis.tableColumns.costTime'),
          key: 'costTime',
          align: 'center',
          width: 100,
          renderType: 'convertTime',
        },
        {
          title: this.$t('message.linkis.tableColumns.failedReason'),
          key: 'failedReason',
          align: 'center',
          className: 'history-failed',
          width: 180,
          renderType: 'a',
          renderParams: {
            hasDoc: this.checkIfHasDoc,
            action: this.linkTo,
          },
        },
        {
          title: `${this.$t(
            'message.linkis.tableColumns.requestApplicationName'
          )}/${this.$t('message.linkis.tableColumns.executeApplicationName')}`,
          key: 'requestApplicationName',
          align: 'center',
          width: 140,
          renderType: 'concat',
          renderParams: {
            concatKey: 'executeApplicationName2',
          },
        },
        {
          title: this.$t('message.linkis.tableColumns.user'),
          key: 'umUser',
          align: 'center',
          width: 80,
        },
        {
          title: 'entrance',
          key: 'instance',
          align: 'center',
          width: 150,
        },
        {
          title: 'engineplugin',
          key: 'engineInstance',
          align: 'center',
          width: 100,
        },
        {
          title: this.$t('message.linkis.tableColumns.createdTime'),
          key: 'createdTime',
          align: 'center',
          width: 150,
          renderType: 'formatTime',
        },
      ]
      if (!this.isAdminModel) {
        const index = column.findIndex((item) => item.key === 'umUser')
        column.splice(index, 1)
      }
      return column
    },
    reset() {
      const today = new Date(new Date().toLocaleDateString())
      this.searchBar = {
        creator: '',
        proxyUser: '',
        engine: 'all',
        shortcut: [today, today],
      }
      this.pageSetting={
        pageSize: 10,
      }
      this.search()
    },
    switchAdmin() {
      if (!this.isLoading) {
        if (this.isAdminModel) {
          this.searchBar.id = null
          this.searchBar.proxyUser = ''
        }
        this.isAdminModel = !this.isAdminModel
        this.search()
      }
    },
    linkTo(params) {
      this.$router.push({
        path: '/console/FAQ',
        query: {
          errCode: parseInt(params.row.failedReason),
          isSkip: true,
        },
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
          value: params.row.progress,
        }
      } else {
        return {
          type: 'Tag',
          value: params.row.status,
        }
      }
    },
  },
}
</script>
<style src="./index.scss" lang="scss" scoped></style>
