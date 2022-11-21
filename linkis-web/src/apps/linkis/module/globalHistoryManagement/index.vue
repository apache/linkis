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
    <Form class="global-history-searchbar" :model="searchBar" inline>
      <FormItem prop="id" :label="$t('message.linkis.jobId')">
        <InputNumber
          v-model="searchBar.id"
          :placeholder="$t('message.linkis.formItems.id.placeholder')"
          style="width:60px;"
          :min="1"
        ></InputNumber>
      </FormItem>
      <FormItem prop="proxyUser" :label="$t('message.linkis.userName')" v-if="isAdminModel">
        <Input
          :maxlength="50"
          v-model="searchBar.proxyUser"
          :placeholder="$t('message.linkis.searchName')"
          style="width:60px;"
        />
      </FormItem>
      <FormItem prop="shortcut" :label="$t('message.linkis.formItems.date.label')">
        <DatePicker
          :transfer="true"
          class="datepicker"
          :options="shortcutOpt"
          v-model="searchBar.shortcut"
          type="daterange"
          placement="bottom-start"
          format="yyyy-MM-dd"
          :placeholder="$t('message.linkis.formItems.date.placeholder')"
          style="width: 160px"
          :editable="false"
        />
      </FormItem>
      <FormItem prop="instance" :label="$t('message.linkis.formItems.instance.label')">
        <Input
          :maxlength="50"
          v-model="searchBar.instance"
          :placeholder="$t('message.linkis.formItems.instance.placeholder')"
          style="width:100px;"
        />
      </FormItem>
      <FormItem prop="creator" :label="$t('message.linkis.formItems.creator.label')">
        <Input
          :maxlength="50"
          v-model="searchBar.creator"
          :placeholder="$t('message.linkis.formItems.creator.placeholder')"
          style="width:80px;"
        />
      </FormItem>
      <FormItem prop="engine" :label="$t('message.linkis.formItems.engine.label')">
        <Select v-model="searchBar.engine" style="width: 70px">
          <Option v-for="(item) in getEngineTypes" :label="item === 'all' ? $t('message.linkis.engineTypes.all'): item" :value="item" :key="item" />
        </Select>
      </FormItem>
      <FormItem prop="status" :label="$t('message.linkis.formItems.status.label')">
        <Select v-model="searchBar.status" style="width: 70px">
          <Option
            v-for="(item) in statusType"
            :label="item.label"
            :value="item.value"
            :key="item.value"
          />
        </Select>
      </FormItem>
      <FormItem>
        <Button
          type="primary"
          @click="search"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.search') }}</Button>
        <Button
          type="warning"
          @click="reset"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.clearSearch') }}</Button>
        <Button type="error" @click="stop" style="margin-right: 10px;">{{$t('message.linkis.stop')}}</Button>
        <Button
          type="primary"
          @click="switchAdmin"
          v-if="isLogAdmin"
        >{{ isAdminModel ? $t('message.linkis.generalView') : $t('message.linkis.manageView') }}</Button>
      </FormItem>
    </Form>
    <div >
      <div class="global-history-table" :style="{width: '100%', 'height': moduleHeight+'px'}">
        <Icon v-show="isLoading" type="ios-loading" size="30" class="global-history-loading" />
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
      <div class="global-history-page">
        <Page
          :total="pageSetting.total"
          :page-size="pageSetting.pageSize"
          :current="pageSetting.current"
          size="small"
          show-total
          show-elevator
          :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
          @on-change="changePage"
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
      getEngineTypes: [],
      isLoading: false,
      pageSetting: {
        total: 0,
        pageSize: 50,
        current: 1
      },
      searchBar: {
        id: null,
        proxyUser: '',
        creator: '',
        engine: 'all',
        status: '',
        shortcut: [today, today]
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
      isAdminModel: false,
      moduleHeight: 300
    }
  },
  created() {
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then(res => {
      this.isLogAdmin = res.admin
    })
    api.fetch('/configuration/engineType', 'get').then(res => {
      this.getEngineTypes = ['all', ...res.engineType]
    })
  },
  mounted() {
    this.init()
    this.moduleHeight = this.$parent.$el.clientHeight - 220
    // Monitor window changes and get browser width and height(监听窗口变化，获取浏览器宽高)
    window.addEventListener('resize', this.getHeight)
  },
  beforeDestroy() {
    // Monitor window changes and get browser width and height(监听窗口变化，获取浏览器宽高)
    storage.set('last-admin-model', this.isAdminModel)
    // storage.set('last-searchbar-status', this.searchBar)
    window.removeEventListener('resize', this.getHeight)
  },
  activated() {
    this.init()
  },
  methods: {
    getHeight() {
      this.moduleHeight = this.$parent.$el.clientHeight - 228
    },
    init() {
      let isAdminModel = storage.get('last-admin-model')
      const lastSearch = storage.get('last-searchbar-status')
      const lastPage = storage.get('last-pageSetting-status')
      if (lastSearch) {
        if (lastSearch.shortcut[0] && lastSearch.shortcut[1]) {
          lastSearch.shortcut = [new Date(lastSearch.shortcut[0]), new Date(lastSearch.shortcut[1])]
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
          console.log(sourceJson)
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
        fileName
      }
      if (this.isAdminModel) {
        query.proxyUser = params.row.umUser
      }
      storage.set('last-searchbar-status', this.searchBar)
      storage.set('last-pageSetting-status', this.pageSetting)
      // Jump to view the history details page(跳转查看历史详情页面)
      this.$router.push({
        path: '/console/viewHistory',
        query
      })
    },

    getParams(page) {
      const startDate = this.searchBar.shortcut[0] ? new Date(this.searchBar.shortcut[0].setHours(0, 0, 0, 0)) : this.searchBar.shortcut[0]
      const endDate = this.searchBar.shortcut[1] ? new Date(this.searchBar.shortcut[1].setHours(23, 59, 59, 0)): this.searchBar.shortcut[1]
      const params = {
        taskID: this.searchBar.id,
        creator: this.searchBar.creator,
        executeApplicationName: this.searchBar.engine,
        status: this.searchBar.status,
        startDate: startDate && startDate.getTime(),
        endDate: endDate && endDate.getTime(),
        pageNow: page || this.pageSetting.current,
        pageSize: this.pageSetting.pageSize,
        proxyUser: this.searchBar.proxyUser,
        isAdminView: this.isAdminModel,
        instance: this.searchBar.instance
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
          this.pageSetting.current = page
          this.pageSetting.total = rst.totalPage
        })
        .catch(() => {
          this.isLoading = false
          this.list = []
        })
    },
    search() {
      this.isLoading = true
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
            instance: item.instance
          }
        })
      }
      return list.map(item => {
        return Object.assign(item, {
          disabled:
              ['Submitted', 'Inited', 'Scheduled', 'Running'].indexOf(item.status) === -1,
          failedReason: getFailedReason(item),
          source: item.sourceTailor
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
          tooltip: true,
          width: 190
        },
        {
          title: this.$t('message.linkis.tableColumns.executionCode'),
          key: 'executionCode',
          align: 'center',
          width: 440,
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
          width: 210,
          renderType: 'a',
          renderParams: {
            hasDoc: this.checkIfHasDoc,
            action: this.linkTo
          }
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
            concatKey: 'executeApplicationName'
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
      if (selected.length) {
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
          this.$Message.success(this.$t('message.linkis.editedSuccess'))
          this.search()
        })
      } else {
        this.$Message.warning(this.$t('message.linkis.unselect'))
      }
    }
  }
}
</script>
<style src="./index.scss" lang="scss"></style>
