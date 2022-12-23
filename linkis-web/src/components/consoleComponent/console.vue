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
  <div
    ref="bottomPanel"
    class="log-panel">
    <div class="workbench-tabs">
      <div class="workbench-tab-wrapper">
        <div class="workbench-tab">
          <div
            :class="{active: scriptViewState.showPanel == 'progress'}"
            class="workbench-tab-item"
            @click="showPanelTab('progress')">
            <span>{{ $t('message.common.tabs.progress') }}</span>
          </div>
          <div
            v-if="script.result"
            :class="{active: scriptViewState.showPanel == 'result'}"
            class="workbench-tab-item"
            @click="showPanelTab('result')">
            <span>{{ $t('message.common.tabs.result') }}</span>
          </div>
          <div
            v-if="isLogShow"
            :class="{active: scriptViewState.showPanel == 'log'}"
            class="workbench-tab-item"
            @click="showPanelTab('log')">
            <span>{{ $t('message.common.tabs.log') }}</span>
          </div>
        </div>
        <!-- <div
          class="workbench-tab-button">
          <Icon
            type="ios-close"
            size="20"
            @click="closeConsole"></Icon>
        </div> -->
      </div>
      <div
        class="workbench-container">
        <we-progress
          v-if="scriptViewState.showPanel == 'progress'"
          :current="script.progress.current"
          :waiting-size="script.progress.waitingSize"
          :steps="script.steps"
          :status="script.status"
          :application="script.application"
          :progress-info="script.progress.progressInfo"
          :cost-time="script.progress.costTime"
          @open-panel="openPanel"
          :script-view-state="scriptViewState"/>
        <result
          v-if="scriptViewState.showPanel == 'result'"
          ref="result"
          :result="script.result"
          :script="script"
          :dispatch="dispatch"
          :getResultUrl="getResultUrl"
          :comData="comData"
          @on-analysis="openAnalysisTab"
          @on-set-change="changeResultSet"
          :script-view-state="scriptViewState"/>
        <log
          v-if="scriptViewState.showPanel == 'log'"
          :logs="script.log"
          :log-line="script.logLine"
          :script-view-state="scriptViewState"/>
      </div>
    </div>
  </div>
</template>
<script>
import api from '@/common/service/api';
import util from '@/common/util';
import storage from '@/common/helper/storage';
import {debounce, isUndefined, isEmpty, last} from 'lodash';
import {Script} from './modal.js';
import result from './result.vue';
import log from './log.vue';
import weProgress from './progress.vue';
import Execute from '@/common/service/execute';
import mixin from '@/common/service/mixin';
export default {
  components: {
    result,
    log,
    weProgress,
  },
  mixins: [mixin],
  props: {
    stop: {
      type: Boolean,
    },
    comData: {
      type: Object
    },
    heigth: {
      type: [String, Number]
    },
    dispatch: {
      type: Function,
      required: true
    },
    getResultUrl: {
      type: String,
      defalut: `filesystem`
    }
  },
  data() {
    return {
      execute: null,
      scriptViewState: {
        showPanel: 'progress',
        cacheLogScroll: 0,
        bottomContentHeight: '250'
      },
      isBottomPanelFull: false,
      isLogShow: false,
      localLog: {
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
      },
      script: {
        result: null,
        steps: [],
        progress: {},
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
        resultList: null,
      },
    }
  },
  watch: {
    stop(val, oldval) {
      if (oldval && this.execute) {
        // Keep the workflow page first, it should be used(先保留工作流页，应该会用到)
        this.killExecute();
      }
    },
    'scriptResult.headRows': function() {
      if(this.visualShow === 'visual'){
        this.openAnalysisTab('table')
      }
    },
    executRun(val) {
      this.$emit('executRun', val)
    }
  },
  computed: {
    executRun() {
      return this.execute ? this.execute.run : false
    }
  },
  mounted() {
    this.scriptViewState.bottomContentHeight = this.heigth - 75;
  },
  methods: {
    killExecute(flag = false) {
      this.execute.trigger('stop');
      if (flag) {
        this.execute.trigger('kill');
      }
    },
    createScript() {
      this.script = new Script({
        id: this.comData.id,
        title: this.comData.name,
        scriptViewState: this.scriptViewState,
      });
    },
    createExecute(needQuery) {
      const data = {
        getResultUrl: this.getResultUrl,
        method: '/api/rest_j/v1/entrance/execute',
        websocketTag: this.comData.id,
        data: {
          executeApplicationName: null,
          executionCode: null,
          runType: this.comData.runType,
          params: null,
          postType: 'http',
          source: {
            scriptPath: null,
          },
          fromLine: this.script.logLine,
        },
      }
      this.execute = new Execute(data);
      if (needQuery) {
        this.queryState();
      }
    },
    queryState() {
      if (this.comData.execID) {
        const option = {
          taskID: this.comData.taskID,
          execID: this.comData.execID,
          isRestore: true,
          id: this.comData.id,
          nodeId: this.comData.key || undefined,
        }
        this.execute.halfExecute(option);
        this.monitoringData();
      }
    },
    monitoringData() {
      if (this.execute.taskID !== this.comData.taskID) {
        return;
      }
      this.execute.on('log', (logs) => {
        this.localLog = this.convertLogs(logs);
        this.script.log = this.localLog.log;
        if (this.scriptViewState.showPanel === 'log') {
          this.localLogShow();
        }
      });
      this.execute.on('result', (ret) => {
        this.showPanelTab('result');
        const storeResult = {
          'headRows': ret.metadata,
          'bodyRows': ret.fileContent,
          'total': ret.totalLine,
          'type': ret.type,
          'cache': {
            offsetX: 0,
            offsetY: 0,
          },
          'path': this.execute.currentResultPath,
          'current': 1,
          'size': 20,
        };
        if (this.execute.resultList[0]) {
          this.$set(this.execute.resultList[0], 'result', storeResult);
        }
        this.$set(this.script, 'resultSet', 0);
        this.script.result = storeResult;
        this.script.resultList = this.execute.resultList;
        this.script.resultSet = 0;
        // Set to 1 if progress has been obtained(获取过progress的情况下设置为1)
        if (this.script.progress.current) {
          this.script.progress.current = 1;
        }
      });
      this.execute.on('progress', ({ progress, progressInfo, waitingSize }) => {
        // Here progressInfo may just be an empty array, or the first data of the data is an empty object(这里progressInfo可能只是个空数组，或者数据第一个数据是一个空对象)
        if (progressInfo.length && !isEmpty(progressInfo[0])) {
          progressInfo.forEach((newProgress) => {
            let newId = newProgress.id;
            let index = this.script.progress.progressInfo.findIndex((progress) => {
              return progress.id === newId;
            });
            if (index !== -1) {
              this.script.progress.progressInfo.splice(index, 1, newProgress);
            } else {
              this.script.progress.progressInfo.push(newProgress);
            }
          });
        } else {
          progressInfo = [];
        }

        if (progress == 1) {
          let runningScripts = storage.get(this._running_scripts_key, 'local') || {};
          delete runningScripts[this.script.nodeId];
          storage.set(this._running_scripts_key, runningScripts, 'local');
        }

        this.script.progress.current = progress;

        if (waitingSize !== null && waitingSize >= 0) {
          this.script.progress.waitingSize = waitingSize;
        }
      });
      this.execute.on('steps', (status) => {
        if (this.executeLastStatus === status) {
          return;
        }
        this.executeLastStatus = status;
        if (status === 'Inited') {
          this.script.steps = ['Submitted', 'Inited'];
        } else {
          const lastStep = last(this.script.steps);
          if (this.script.steps.indexOf(status) === -1) {
            this.script.steps.push(status);
            // When there may be a WaitForRetry state, the background will re-push the Scheduled or running state(针对可能有WaitForRetry状态后，后台会重新推送Scheduled或running状态的时候)
          } else if (lastStep !== status) {
            this.script.steps.push(status);
          }
        }
        if (status !== 'Inited' && this.script.steps.length === 1) {
          this.script.steps.unshift('ellipsis');
        }
      });
      this.execute.on('status', (status) => {
        this.script.status = status;
      });
      this.execute.on('querySuccess', ({ type, task }) => {
        const costTime = util.convertTimestamp(task.costTime);
        this.script.progress.costTime = costTime;
        const name = this.script.title;
        this.$Notice.close(name);
        this.$Notice.success({
          title: this.$root.$t('message.common.notice.querySuccess.title'),
          desc: '',
          render: (h) => {
            return h('span', {
              style: {
                'word-break': 'break-all',
                'line-height': '20px',
              },
            }, `${this.script.title} ${type}${this.$root.$t('message.common.notice.querySuccess.render')}：${costTime}！`);
          },
          name,
          duration: 3,
        });
      });
      this.execute.on('notice', ({ type, msg, autoJoin }) => {
        const name = this.script.title;
        const label = autoJoin ? `${this.$root.$t('message.common.script')}${this.script.title} ${msg}` : msg;
        this.$Notice.close(name);
        this.$Notice[type]({
          title: this.$root.$t('message.common.notice.notice.title'),
          name,
          desc: '',
          duration: 5,
          render: (h) => {
            return h('span', {
              style: {
                'word-break': 'break-all',
                'line-height': '20px',
              },
            }, label);
          },
        });
      });
      this.execute.on('costTime', (time) => {
        this.script.progress.costTime = util.convertTimestamp(time);
      });
    },
    resetQuery() {
      this.showPanelTab('progress');
      this.isLogShow = false;
      this.localLog = {
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
      };
      this.script.progress = {
        current: null,
        progressInfo: [],
        waitingSize: null,
        costTime: null,
      };
      this.script.log = { all: '', error: '', warning: '', info: '' };
      this.script.logLine = 1;
      this.script.steps = ['Submitted'];
      this.script.diagnosis = null;
      this.script.result = {
        headRows: [],
        bodyRows: [],
        type: 'EMPTY',
        total: 0,
        path: '',
        cache: {},
      };
      this.script.resultList = null;
    },
    showPanelTab(type) {
      this.scriptViewState.showPanel = type;
      this.script.showPanel = type;
      if (type === 'log') {
        this.localLogShow();
      }
    },
    localLogShow() {
      if (!this.debounceLocalLogShow) {
        this.debounceLocalLogShow = debounce(() => {
          if (this.localLog) {
            this.script.log = Object.freeze({ ...this.localLog.log });
            this.script.logLine = this.localLog.logLine;
          }
        }, 1500);
      }
      this.debounceLocalLogShow();
    },
    openAnalysisTab(type) {
      this.visualShow = type;
      // flage ? this.visualShow = 'table' : this.visualShow = 'visual'
      // this.flage = !this.flage;

      if (type === 'visual') {
        this.biLoading = true;
        let rows = this.scriptResult.headRows;
        let model = {}
        let dates = ["DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR"]
        let numbers = [
          "TINYINT",
          "SMALLINT",
          "MEDIUMINT",
          "INT",
          "INTEGER",
          "BIGINT",
          "FLOAT",
          "DOUBLE",
          "DOUBLE PRECISION",
          "REAL",
          "DECIMAL",
          "BIT",
          "SERIAL",
          "BOOL",
          "BOOLEAN",
          "DEC",
          "FIXED",
          "NUMERIC"];
        rows.forEach(item=>{
          let sqlType = item.dataType.toUpperCase();
          let visualType = 'string'
          if (numbers.indexOf(sqlType) > -1) {
            visualType = 'number'
          } else if(dates.indexOf(sqlType) > -1) {
            visualType = 'date'
          }
          model[item.columnName] =  {
            sqlType,
            visualType,
            modelType: visualType ==="number" ? "value": "category"
          }
        })
        this.visualParams = {
          // viewId: id,
          // projectId,
          json: {
            name: `${this.script.fileName.replace(/\./g,'')}${this.script.resultSet}`,
            model,
            source: {
              "engineType": "spark", //引擎类型
              "dataSourceType": "resultset", //数据源类型，结果集、脚本、库表
              "dataSourceContent": {
                "resultLocation": this.scriptResult.path
              },
              "creator": "IDE"
            }
          }
        }
      } else if (type === 'dataWrangler') {
        this.dataWranglerParams = {
          simpleMode: true,
          showBottomBar: false,
          importConfig: {
            "dataSourceConfig": {
              "dataSourceType": "linkis",
              "dataSourceOptions": {"taskID": this.work.taskID || this.work.data.history[0].taskID}
            },
            "config": {
              "myConfig": {
                "resultSetPath": [this.scriptResult.path]
              },
              "importConfig": {
                "mergeTables": true,
                "limitRows": 5000,
                "pivotTable": false,
                "tableHeaderRows": 1,
              }
            }
          }
        }
      }
    },
    changeResultSet(data, cb) {
      const resultSet = isUndefined(data.currentSet) ? this.script.resultSet : data.currentSet;
      const findResult = this.script.resultList[resultSet];
      const resultPath = findResult && findResult.path;
      const hasResult = Object.prototype.hasOwnProperty.call(this.script.resultList[resultSet], 'result');
      if (!hasResult) {
        const pageSize = 5000;
        const url = `/${this.getResultUrl}/openFile`;
        let params = {
          path: resultPath,
          pageSize,
        }
        // If it is api execution, you need to bring taskId(如果是api执行需要带上taskId)
        if (this.getResultUrl !== 'filesystem') {
          params.taskId = this.comData.taskID
        }
        api.fetch(url, {
          path: resultPath,
          pageSize,
        }, 'get')
          .then((ret) => {
            const result = {
              'headRows': ret.metadata,
              'bodyRows': ret.fileContent,
              // If totalLine is null, it will be displayed as 0(如果totalLine是null，就显示为0)
              'total': ret.totalLine ? ret.totalLine : 0,
              // If the content is null, it will display no data(如果内容为null,就显示暂无数据)
              'type': ret.fileContent ? ret.type : 0,
              'cache': {
                offsetX: 0,
                offsetY: 0,
              },
              'path': resultPath,
              'current': 1,
              'size': 20,
            };
            this.$set(this.script.resultList[resultSet], 'result', result);
            this.$set(this.script, 'resultSet', resultSet);
            this.script.result = result;
            this.script.resultSet = resultSet;
            this.script.showPanel = 'result';
            cb();
          }).catch(() => {
            cb();
          });
      } else {
        this.script.result = this.script.resultList[resultSet].result;
        this.$set(this.script, 'resultSet', resultSet);
        this.script.resultSet = resultSet;
        this.script.showPanel = 'result';
        cb();
      }
    },
    closeConsole() {
      this.$emit('close-console');
    },
    openPanel(type) {
      if (type === 'log') {
        this.isLogShow = true;
        this.showPanelTab(type);
      }
    },
    checkFromCache() {
      // Every time you right-click the console, a new instance will be created, so stop the last executed instance first(每次右键控制台，都会创建一个新实例，所以把上一个执行的实例先停掉)
      if (this.execute) {
        this.killExecute();
      }
      this.resetQuery();
      this.createScript();
      this.$nextTick(() => {
        this.createExecute(true);
      })
    },
    getLogs() {
      api.fetch(`/entrance/${this.comData.execID}/log`, {
        fromLine: this.fromLine,
        size: -1,
      }, 'get')
        .then((rst) => {
          this.localLog = this.convertLogs(rst.log);
          this.script.log = this.localLog.log;
          this.script.logLine = this.fromLine = rst.fromLine;
          if (this.scriptViewState.showPanel === 'log') {
            this.localLogShow();
          }
        });
    },
    convertLogs(logs) {
      const tmpLogs = this.localLog || {
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
      };
      let hasLogInfo = Array.isArray(logs) ? logs.some((it) => it.length > 0) : logs;
      if (!hasLogInfo) {
        return tmpLogs;
      }
      const convertLogs = util.convertLog(logs);
      Object.keys(convertLogs).forEach((key) => {
        const convertLog = convertLogs[key];
        if (convertLog) {
          tmpLogs.log[key] += convertLog + '\n';
        }
        if (key === 'all') {
          tmpLogs.logLine += convertLog.split('\n').length;
        }
      });
      return tmpLogs;
    }
  }
}
</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
  .log-panel {
    margin-top: 1px;
    border-top: $border-width-base $border-style-base $border-color-base;
    background-color: $background-color-base;
    .workbench-tabs {
      position: $relative;
      height: 100%;
      overflow: hidden;
      box-sizing: border-box;
      z-index: 3;
      .workbench-tab-wrapper {
        display: flex;
        border-top: $border-width-base $border-style-base #dcdcdc;
        border-bottom: $border-width-base $border-style-base #dcdcdc;
        .workbench-tab {
          flex: 1;
          display: flex;
          flex-direction: row;
          flex-wrap: nowrap;
          justify-content: flex-start;
          align-items: flex-start;
          height: 32px;
          background-color: $body-background;
          width: calc(100% - 45px);
          overflow: hidden;
          &.work-list-tab {
            overflow-x: auto;
            overflow-y: hidden;
            &::-webkit-scrollbar {
              width: 0;
              height: 0;
              background-color: transparent;
            }
            .list-group>span {
              white-space: nowrap;
              display: block;
              height: 0;
            }
          }
          .workbench-tab-item {
            text-align: center;
            border-top: none;
            display: inline-block;
            height: 32px;
            line-height: 32px;
            background-color: $background-color-base;
            color: $title-color;
            cursor: pointer;
            min-width: 100px;
            max-width: 200px;
            overflow: hidden;
            margin-right: 2px;
            border: 1px solid #eee;
            &.active {
              margin-top: 1px;
              background-color: $body-background;
              color: $primary-color;
              border-radius: 4px 4px 0 0;
              border: 1px solid $border-color-base;
              border-bottom: 2px solid $primary-color;
            }
          }
        }
        .workbench-tab-control {
            flex: 0 0 45px;
            text-align: right;
            background-color: $body-background;
            border-left: $border-width-base $border-style-base $border-color-split;
            .ivu-icon {
              font-size: $font-size-base;
              margin-top: 8px;
              margin-right: 2px;
              cursor: pointer;
              &:hover {
                  color: $primary-color;
              }
              &.disable {
                  color: $btn-disable-color;
              }
            }
        }
        .workbench-tab-button {
            flex: 0 0 30px;
            text-align: center;
            background-color: $body-background;
            .ivu-icon {
                font-size: $font-size-base;
                margin-top: 8px;
                cursor: pointer;
            }
        }
      }
      .workbench-container {
        height: calc(100% - 36px);
        &.node {
            height: 100%;
        }
        @keyframes ivu-progress-active {
            0% {
                opacity: .3;
                width: 0;
            }
            100% {
                opacity: 0;
                width: 100%;
            }
        }
      }
    }
  }
</style>

