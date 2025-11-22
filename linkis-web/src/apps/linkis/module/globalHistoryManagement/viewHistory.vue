
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
    <Tabs v-model="tabName" @on-click="onClickTabs">
      <TabPane
        v-for="tab in tabs"
        :key="tab.name"
        :name="tab.name"
        :label="$t(tab.label)">
      </TabPane>
    </Tabs>
    <!-- <Button v-if="tabName === 'log' && yarnAddress" class="jumpButton" type="primary" @click="jump">{{$t('message.linkis.jump')}}</Button> -->
    <Button v-if="tabName === 'log'" class="foldButton" type="primary" @click="fold">{{foldFlag ? $t('message.linkis.unfold') : $t('message.linkis.fold')}}</Button>
    <Button v-if="!isHistoryDetail" class="backButton" type="primary" @click="back">{{$t('message.linkis.back')}}</Button>

    <Icon v-show="isLoading" type="ios-loading" size="30" class="global-history-loading" />
    <log ref="logRef" key="log" v-if="tabName === 'log'" :logs="logs" :from-line="fromLine" :script-view-state="scriptViewState" @tabClick="handleTabClick" />
    <log ref="codeRef" key="code" v-if="tabName === 'code'" :logs="codes" :from-line="fromLine" :script-view-state="scriptViewState" @tabClick="handleTabClick" status="code" />
    <result
      v-if="tabName === 'result'"
      class="result-class"
      ref="result"
      :script="script"
      :dispatch="dispatch"
      :visualShow="visualShow"
      :script-view-state="scriptViewState"
      :dataWranglerParams="dataWranglerParams"
      getResultUrl="filesystem"
      @on-set-change="changeResultSet"
      @on-analysis="openAnalysisTab"
      :visualParams="visualParams"
    />
    <ViewLog ref="logPanel" :inHistory="true" v-show="tabName === 'engineLog' && hasEngine" @back="showviewlog = false" />
    <logWithPage ref="udfLog" logType="udfLog" v-show="tabName === 'udfLog' && showUDF && hasEngine" />

    <term ref="termRef" v-if="tabName === 'terminal'" :logs="termLogs" :script-view-state="scriptViewState" :loading="termLogLoading" />
  </div>
</template>
<script>
import result from '@/components/consoleComponent/result.vue'
import logWithPage from './log.vue';
import log from '@/components/consoleComponent/log.vue'
import term from '@/components/consoleComponent/term.vue'
import api from '@/common/service/api'
import mixin from '@/common/service/mixin'
import util from '@/common/util'
import ViewLog from '@/apps/linkis/module/resourceManagement/log.vue'
import { cloneDeep, isUndefined } from 'lodash'
import storage from '@/common/helper/storage';

export default {
  name: 'viewHistory',
  components: {
    log,
    result,
    ViewLog,
    term,
    logWithPage
  },
  mixins: [mixin],
  props: {},
  data() {
    return {
      foldFlag: true,
      hasResultData: false,
      isLoading: true,
      tabName: 'log',
      visualShow: 'table',
      dataWranglerParams: {},
      script: {
        data: '',
        oldData: '',
        result: {},
        steps: [],
        progress: {},
        resultList: null,
        resultSet: 0,
        params: {},
        readOnly: false
      },
      visualParams: {},
      scriptViewState: {
        // topPanelHeight: '250px',
        bottomContentHeight: 800,
        topPanelFull: false,
        showPanel: 'log',
        bottomPanelFull: false,
        cacheLogScroll: 0
      },
      logs: {
        all: '',
        error: '',
        warning: '',
        info: ''
      },
      codes: { code: '' },
      termLogs: '',
      termLogLoading: false,
      engineLogs: '',
      fromLine: 1,
      isAdminModel: false,
      jobhistoryTask: null,
      hasEngine: false,
      yarnAddress: '',
      logTimer: null,
      preName: 'log',
      showUDF: false,
      tabs: [],
    }
  },
  async created() {
    this.hasResultData = false
    let engineInstance = this.$route.query.engineInstance

    const engineLogOnlyAdminEnable = storage.get('engineLogOnlyAdminEnable')
    // 仅管理员可以查看引擎日志
    const isAdminShowEngineLog = !engineLogOnlyAdminEnable || (engineLogOnlyAdminEnable && (storage.get('isLogAdmin') || storage.get('isLogHistoryAdmin') || storage.get('isLogDeptAdmin')))
    let taskID = this.$route.query.taskID
    await this.initHistory(taskID);
    if(engineInstance && isAdminShowEngineLog) {
      let hasEngine = (!!this.jobhistoryTask.ticketId && !!this.jobhistoryTask.ecmInstance && !!this.jobhistoryTask.engineInstance && !!this.jobhistoryTask.engineLogPath);
      if(!hasEngine){
        // 可能是因为老数据在get里面没有 再查一下ecrHistoryList试试
        let url = '/linkisManager/ecinfo/ecrHistoryList?';
        const endDate = new Date();
        const startDate = new Date();
        startDate.setMonth(startDate.getMonth() - 3);
        url += `instance=${engineInstance}&startDate=${this.formatDate(startDate)}&endDate=${this.formatDate(endDate)}`;
        const res = await api.fetch(url,'get')
        const param = res.engineList[0]
        if(param){
          this.jobhistoryTask.ticketId = param.ticketId;
          this.jobhistoryTask.engineType = param.engineType;
          this.jobhistoryTask.ecmInstance = param.ecmInstance;
          this.jobhistoryTask.engineInstance = param.serviceInstance;
          this.jobhistoryTask.engineLogPath = param.logDirSuffix;
          this.jobhistoryTask.udfLogPath = param.logDirSuffix;
        }
      }
      this.hasEngine = (!!this.jobhistoryTask.ticketId && !!this.jobhistoryTask.ecmInstance && !!this.jobhistoryTask.engineInstance && !!this.jobhistoryTask.engineLogPath);
      this.showUDF = this.hasEngine && ['spark', 'hive'].includes(this.jobhistoryTask?.engineType || '')
    }
    this.tabs = [
      { name: 'log', label: 'message.linkis.log' },
      { name: 'code', label: 'message.linkis.executionCode' },
      // { name: 'detail', label: 'message.linkis.detail', disabled: true },
      { name: 'result', label: 'message.linkis.result' },
      { name: 'engineLog', label: 'message.linkis.engineLog' },
      { name: 'udfLog', label: 'message.linkis.udfLog' },
      { name: 'terminal', label: 'message.linkis.diagnosticLog' }
    ]
    if(!this.hasEngine) {
      this.tabs = this.tabs.filter(tab => tab.name !== 'engineLog')
    }
    if(!this.showUDF) {
      this.tabs = this.tabs.filter(tab => tab.name !== 'udfLog')
    }
  },
  destroyed() {
    if(this.logTimer) {
      clearTimeout(this.logTimer);
    }
  },
  async mounted() {
    const node = document.getElementsByClassName('global-history')[0];
    this.scriptViewState.bottomContentHeight = node.clientHeight - 85;
  },
  computed: {
    isHistoryDetail() {
      return this.$route.path === '/console/viewHistoryDetail'
    }
  },
  methods: {
    handleTabClick() {
      this.foldFlag = true;
    },
    // The request is triggered when the tab is clicked, and the log is requested at the beginning, and no judgment is made.(点击tab时触发请求，log初始就请求了，不做判断)
    async onClickTabs(name) {
      const canResultSet = sessionStorage.getItem('canResultSet');
      if(name === 'result' && canResultSet === 'false') {
        try {
          setTimeout(() => {
            this.tabName = this.preName;
          }, 0);
          window.open(`http://${window.location.host}/#/results?parentPath=${this.jobhistoryTask.resultLocation}&taskId=${this.$route.query.taskID}&fileName=example.${this.jobhistoryTask.runType}`, '_blank')

          return;
        } catch (error) {
          window.console.error(error)
        }

      }
      this.tabName = name
      this.preName = name
      if (name === 'result') {
        // Determine whether it is a result set(判断是否为结果集)
        if (this.hasResultData) return // Determine whether the data has been obtained, and return directly if it is obtained(判断是否已经获取过数据，获取过则直接返回)
        if (this.jobhistoryTask && this.jobhistoryTask.resultLocation) {
          //Determine if there is a resource address, if not, do not send a request(判断是否有资源地址，如果没有则不发请求)
          this.getResult(this.jobhistoryTask)
        } else {
          this.$Notice.warning({
            title: this.$t('message.linkis.tip'),
            desc: this.$t('message.linkis.serverTip')
          })
        }
      } else if(name === 'engineLog') {
        if(this.jobhistoryTask) {
          this.$refs.logPanel.getLogs(0, {
            applicationName: "linkis-cg-engineconn",
            emInstance: this.jobhistoryTask.ecmInstance || '',
            instance: this.jobhistoryTask.engineInstance || '',
            ticketId: this.jobhistoryTask.ticketId || '',
            engineType: this.jobhistoryTask.engineType || '',
            logDirSuffix: this.jobhistoryTask.engineLogPath || '',
          })
        }

      } else if(name === 'udfLog') {
        if(this.jobhistoryTask) {
          this.$refs.udfLog.getLogs(0, {
            applicationName: "linkis-cg-engineconn",
            emInstance: this.jobhistoryTask.ecmInstance || '',
            instance: this.jobhistoryTask.engineInstance || '',
            ticketId: this.jobhistoryTask.ticketId || '',
            engineType: this.jobhistoryTask.engineType || '',
            logDirSuffix: this.jobhistoryTask.engineLogPath || '',
          })
        }

      } else if(name === 'code') {
        // window.console.log('click code')
        if(this.codes.code) return;
        const res = await api.fetch(`/jobhistory/job-extra-info?jobId=${this.$route.query.taskID}`, 'get')
        const { executionCode } = res.metricsMap;
        this.codes = { code: executionCode };
      } else if(name === 'terminal') {
        if (['Scheduled', 'Running'].includes(this.$route.query.status) || !this.termLogs) {
          try {
            this.termLogLoading = true;
            const res = await api.fetch(`/jobhistory/diagnosis-query?taskID=${this.$route.query.taskID}`, 'get')
            this.termLogLoading = false;
            const { diagnosisMsg } = res;
            this.termLogs = diagnosisMsg;
          } catch (err) {
            window.console.error(err);
            this.termLogs = '';
          }

        }

      } else {
        this.$nextTick(() => {
          this.$refs.logRef.fold();
          this.foldFlag = true;
        })

      }
    },
    changeResultSet(data, cb) {
      const resultSet = isUndefined(data.currentSet)
        ? this.script.resultSet
        : data.currentSet
      const findResult = this.script.resultList[resultSet]
      const resultPath = findResult && findResult.path
      const hasResult = Object.prototype.hasOwnProperty.call(
        this.script.resultList[resultSet],
        'result'
      )
      if (!hasResult) {
        const pageSize = 5000
        const url = '/filesystem/openFile'
        api
          .fetch(
            url,
            {
              path: resultPath,
              enableLimit: true,
              pageSize
            },
            'get'
          )
          .then(ret => {
            let result = {}
            if (ret.display_prohibited) {
              result = {
                headRows: [],
                bodyRows: [],
                // If totalLine is null, it will be displayed as 0(如果totalLine是null，就显示为0)
                total: ret.totalLine ? ret.totalLine : 0,
                // (If the content is null, it will display no data)如果内容为null,就显示暂无数据
                type: ret.type,
                path: resultPath,
                current: 1,
                size: 20,
                hugeData: true,
                tipMsg: localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg
              }
            } else if(ret.column_limit_display) {
              result = {
                headRows: ret.metadata,
                bodyRows: ret.fileContent,
                // If totalLine is null, it will be displayed as 0(如果totalLine是null，就显示为0)
                total: ret.totalLine ? ret.totalLine : 0,
                // If the content is null, it will display no data(如果内容为null,就显示暂无数据)
                type: ret.fileContent ? ret.type : 0,
                path: resultPath,
                current: 1,
                size: 20,
                tipMsg: localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg,
              };
            } else {
              result = {
                headRows: ret.metadata,
                bodyRows: ret.fileContent,
                // If totalLine is null, it will be displayed as 0(如果totalLine是null，就显示为0)
                total: ret.totalLine ? ret.totalLine : 0,
                // If the content is null, it will display no data(如果内容为null,就显示暂无数据)
                type: ret.fileContent ? ret.type : 0,
                path: resultPath,
                current: 1,
                size: 20
              }
            }

            this.script.resultList[resultSet].result = result
            this.script.resultSet = resultSet
            this.script = {
              ...this.script
            }
            cb()
          })
          .catch(() => {
            cb()
          })
      } else {
        this.script.resultSet = resultSet
        this.script = {
          ...this.script
        }
        cb()
      }
    },
    fold() {
      if(this.foldFlag) {
        this.$refs.logRef.unfold()
      } else {
        this.$refs.logRef.fold()
      }
      this.foldFlag = !this.foldFlag;
    },
    // Format the array into json form.(将数组格式化成json形式。)
    openAnalysisTab(type) {
      this.visualShow = type
      if (type === 'visual') {
        this.biLoading = true
        let rows = this.scriptResult.headRows
        let model = {}
        let dates = ['DATE', 'DATETIME', 'TIMESTAMP', 'TIME', 'YEAR']
        let numbers = [
          'TINYINT',
          'SMALLINT',
          'MEDIUMINT',
          'INT',
          'INTEGER',
          'BIGINT',
          'FLOAT',
          'DOUBLE',
          'DOUBLE PRECISION',
          'REAL',
          'DECIMAL',
          'BIT',
          'SERIAL',
          'BOOL',
          'BOOLEAN',
          'DEC',
          'FIXED',
          'NUMERIC'
        ]
        rows.forEach(item => {
          let sqlType = item.dataType.toUpperCase()
          let visualType = 'string'
          if (numbers.indexOf(sqlType) > -1) {
            visualType = 'number'
          } else if (dates.indexOf(sqlType) > -1) {
            visualType = 'date'
          }
          model[item.columnName] = {
            sqlType,
            visualType,
            modelType: visualType === 'number' ? 'value' : 'category'
          }
        })
        this.visualParams = {
          // viewId: id,
          // projectId,
          json: {
            name: `${this.script.fileName.replace(/\./g, '')}${
              this.script.resultSet
            }`,
            model,
            source: {
              engineType: 'spark', //engine type(引擎类型)
              dataSourceType: 'resultset', //Data source types, result sets, scripts, library tables(数据源类型，结果集、脚本、库表)
              dataSourceContent: {
                resultLocation: this.scriptResult.path
              },
              creator: 'IDE'
            }
          }
        }
      } else if (type === 'dataWrangler') {
        this.dataWranglerParams = {
          simpleMode: true,
          showBottomBar: false,
          importConfig: {
            dataSourceConfig: {
              dataSourceType: 'linkis',
              dataSourceOptions: {
                taskID: this.work.taskID || this.work.data.history[0].taskID
              }
            },
            config: {
              myConfig: {
                resultSetPath: [this.scriptResult.path]
              },
              importConfig: {
                mergeTables: true,
                limitRows: 5000,
                pivotTable: false,
                tableHeaderRows: 1
              }
            }
          }
        }
      }
    },
    formatDate(date) {
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      const seconds = date.getSeconds().toString().padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    async getLogs(jobhistory) {
      const params = {
        path: jobhistory.task.logPath
      }
      if (this.$route.query.proxyUser) {
        params.proxyUser = this.$route.query.proxyUser
      }
      let openLog = {}
      if (this.$route.query.status === 'Scheduled' || this.$route.query.status === 'Running') {
        const tempParams = {
          fromLine: this.fromLine,
          size: 10000,
        }
        openLog = await api.fetch(`/entrance/${this.$route.query.execID}/log`, tempParams, 'get')
      } else {
        openLog = await api.fetch('/filesystem/openLog', params, 'get')
      }
      if (openLog) {
        const log = cloneDeep(this.logs)
        const convertLogs = util.convertLog(openLog.log)
        Object.keys(convertLogs).forEach(key => {
          if (convertLogs[key]) {
            log[key] += convertLogs[key] + '\n'
          }
        })
        this.logs = log
        this.fromLine = openLog.fromLine
        this.$nextTick(() => {
          this.$refs.logRef.fold();
          this.foldFlag = true;
        })
        if (convertLogs['all'].split('\n').length >= 10000 && ['Scheduled', 'Running'].includes(this.$route.query.status)) {
          if(this.logTimer) clearTimeout(this.logTimer);
          this.logTimer = setTimeout(async () => await this.getLogs(jobhistory), 2000);
        }
      }
    },
    // Get historical details(获取历史详情)
    async initHistory(jobId) {
      try {
        let jobhistory = await api.fetch(`/jobhistory/${jobId}/get`, 'get')
        const option = jobhistory.task
        // const executionCode = option.executionCode;
        this.jobhistoryTask = option
        this.script.runType = option.runType
        this.yarnAddress = option.yarnAddress
        // this.codes = { code: executionCode }
        if (!jobhistory.task.logPath) {
          const errCode = jobhistory.task.errCode
            ? `\n${this.$t('message.linkis.errorCode')}：${
              jobhistory.task.errCode
            }`
            : ''
          const errDesc = jobhistory.task.errDesc
            ? `\n${this.$t('message.linkis.errorDescription')}：${
              jobhistory.task.errDesc
            }`
            : ''
          const info = this.$t('message.linkis.notLog') + errCode + errDesc
          this.logs = { all: info, error: '', warning: '', info: '' }
          this.fromLine = 1
          return
        }
        await this.getLogs(jobhistory);

        this.isLoading = false
      } catch (errorMsg) {
        window.console.error(errorMsg)
        this.isLoading = false
      }
    },
    // Get result set content(获取结果集内容)
    async getResult(option) {
      this.isLoading = true
      try {
        const url1 = `/filesystem/getDirFileTrees`
        const rst = await api.fetch(
          url1,
          {
            path: option.resultLocation
          },
          'get'
        )
        if (rst.dirFileTrees) {
          // The order of the result set in the background is sorted by string according to the name of the result set. When displaying, there will be a problem that the result set cannot be matched, so add sorting(后台的结果集顺序是根据结果集名称按字符串排序的，展示时会出现结果集对应不上的问题，所以加上排序)
          let scriptResultList = rst.dirFileTrees.children.sort(
            (a, b) => parseInt(a.name.split('_')[1].split('.')[0], 10) - parseInt(b.name.split('_')[1].split('.')[0], 10)
          )
          if (scriptResultList.length) {
            const currentResultPath = rst.dirFileTrees.children[0].path
            const url2 = `/filesystem/openFile`
            api
              .fetch(
                url2,
                {
                  path: currentResultPath,
                  page: 1,
                  enableLimit: true,
                  pageSize: 5000
                },
                'get'
              )
              .then(ret => {
                let tmpResult = {}
                if (ret.display_prohibited) {
                  tmpResult = {
                    headRows: [],
                    bodyRows: [],
                    total: ret.totalLine,
                    type: ret.type,
                    path: currentResultPath,
                    hugeData: true,
                    tipMsg: localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg
                  };
                } else if(ret.column_limit_display) {
                  tmpResult = {
                    headRows: ret.metadata,
                    bodyRows: ret.fileContent,
                    // If totalLine is null, it will be displayed as 0(如果totalLine是null，就显示为0)
                    total: ret.totalLine,
                    // If the content is null, it will display no data(如果内容为null,就显示暂无数据)
                    type: ret.type,
                    path: currentResultPath,
                    current: 1,
                    size: 20,
                    tipMsg: localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg,
                  };
                } else {
                  tmpResult = {
                    headRows: ret.metadata,
                    bodyRows: ret.fileContent,
                    total: ret.totalLine,
                    type: ret.type,
                    path: currentResultPath
                  }
                }
                this.script.resultSet = 0
                this.script.resultList = scriptResultList
                this.$set(this.script.resultList[0], 'result', {})
                Object.assign(this.script.resultList[0].result, tmpResult)
                this.scriptViewState.showPanel = 'result'
                this.isLoading = false
              })
          }
          this.hasResultData = true
        } else {
          // If not returned, set an initialization data(没有返回则设置一个初始化数据)
          let tmpResult = {
            headRows: [],
            bodyRows: [],
            total: 0,
            type: '2',
            path: ''
          }
          this.script.resultSet = 0
          this.script.resultList = [{}]
          this.$set(this.script.resultList[0], 'result', {})
          Object.assign(this.script.resultList[0].result, tmpResult)
          this.scriptViewState.showPanel = 'result'
          this.isLoading = false
        }
      } catch (error) {
        this.isLoading = false
        window.console.error(error)
      }
    },
    // go back to the last page(返回上一页)
    back() {
      if (this.isLoading) {
        return this.$Message.warning(this.$t('message.linkis.logLoading'))
      }
      this.$router.go(-1)
    },
    // quick jump to yarn log page(快速跳转至yarn日志页面)
    // jump() {
    //   window.open(this.yarnAddress, '_blank')
    // }
  }
}
</script>
<style lang="scss" scoped>
.foldButton {
  position: absolute;
  top: 0;
  right:83px;
}
// .jumpButton {
//   position: absolute;
//   top: 0;
//   right:146px;
// }

.backButton {
  position: absolute;
  top: 0;
  right: 20px;
}
::v-deep .table-div {
  height: 100% !important;
}
::v-deep .log {
    height: calc(100% - 70px)
}
</style>

