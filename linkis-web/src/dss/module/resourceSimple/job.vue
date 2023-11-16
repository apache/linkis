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
  <div>
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <div class="job-manager">
      <div
        class="job-manager-empty"
        v-if="!jobList.length">{{ $t('message.common.resourceSimple.ZWSJ') }}</div>
      <div
        class="job-manager-type"
        v-for="(type, index) in jobTypeList"
        :key="index">
        <span
          class="job-manager-title"
          v-if="jobList.length && checkJobLength(type.en)">{{ type.cn }}</span>
        <div
          v-for="(item, subIndex) in jobList"
          :key="subIndex">
          <div
            v-if="item.requestApplicationName === type.en"
            class="job-manager-item-wrapper"
            :style="getClass(item)"
            :class="{'actived': item.isActive}"
            @click="handleItemClick(item)"
            @contextmenu.prevent.stop="handleItemMenu($event, item)">
            <Icon
              type="md-checkmark"
              class="job-manager-item-active"
              v-if="item.isActive"
            ></Icon>
            <span
              class="job-manager-item-progress"
              :style="{'width': item.progress ? (item.progress * 100).toFixed(2) + '%' : 0,}"></span>
            <span class="job-manager-item-label" :style="{marginLeft: '16px'}">{{ getLabel(item) }}</span>
            <span class="job-manager-item-progress-label">{{ item.progress ? (item.progress * 100).toFixed(2) + '%' : '' }}</span>
            <Icon
              size="16"
              type="ios-close-circle-outline"
              class="job-manager-item-close"
              @click="openKillModal($t('message.common.resourceSimple.RW'))"/>
          </div>
        </div>
      </div>
      <we-menu
        ref="contextMenu"
        id="jobManager">
        <we-menu-item
          @select="openKillModal($t('message.common.resourceSimple.YQRW'))"
          v-if="lastClick && lastClick.status !== 'Inited'">
          <span>{{ $t('message.common.resourceSimple.JSYQRW') }}</span>
        </we-menu-item>
        <we-menu-item @select="openHistoryScript">
          <span>{{ $t('message.common.resourceSimple.CKJBXX') }}</span>
        </we-menu-item>
        <we-menu-item @select="copyInfos">
          <span>{{ $t('message.common.resourceSimple.FZJBXX') }}</span>
        </we-menu-item>
      </we-menu>
    </div>
    <detele-modal
      ref="killModal"
      :loading="loading"
      @delete="kill"></detele-modal>
  </div>
</template>
<script>
import { orderBy } from 'lodash';
import util from '@/common/util';
import api from '@/common/service/api';
import deteleModal from '@/components/deleteDialog';
import mixin from '@/common/service/mixin';
export default {
  name: 'Job',
  components: {
    deteleModal,
  },
  mixins: [mixin],
  data() {
    return {
      loading: false,
      jobTypeList: [],
      jobList: [],
      lastClick: null,
    };
  },
  watch: {
    loading(val) {
      this.$emit('change-loading', val);
    },
  },
  methods: {
    getJobList() {
      if(this.loading) return;
      this.jobList = [];
      this.loading = true;
      api.fetch('/jobhistory/listundonetasks', {
        pageSize: 100,
        status: 'Running,Inited,Scheduled',
      }, 'get').then((rst) => {
        this.loading = false;
        // Eliminate tasks whose requestApplicationName is "nodeexecution"(剔除requestApplicationName为 "nodeexecution" 的task)
        let tasks = rst.tasks.filter(item => item.requestApplicationName !== "nodeexecution" && item.requestApplicationName !== "CLI")
        this.dispatch('Footer:updateRunningJob', tasks.length);
        this.jobTypeList = [
          { 'en': 'IDE', 'cn': this.$t('message.common.resourceSimple.YS') },
          { 'en': 'Visualis', 'cn': this.$t('message.common.resourceSimple.ZH') },
          { 'en': 'flowexecution', 'cn': this.$t('message.common.resourceSimple.FLOW1') },
          { 'en': 'Scheduler', 'cn': this.$t('message.common.resourceSimple.FLOW2')}];
        tasks.forEach((item) => {
          const tmpItem = Object.assign({}, item, { isActive: false, fileName: this.convertJson(item) });
          this.jobList.push(tmpItem);
        });
        this.jobList = orderBy(this.jobList, ['status', 'fileName']);
        this.$emit('update-job', tasks.length);
      }).catch((err) => {
        this.loading = false;
        window.console.error(err)
      });
    },
    // delete current job(删除当前工作)
    killJob() {
      const item = this.lastClick;
      if (this.loading) return this.$Message.warning(this.$t('message.common.resourceSimple.DDJK'));
      if (!item) return this.$Message.warning(this.$t('message.common.resourceSimple.QXZYTJL'));
      if (!item.strongerExecId) return this.$Message.warning(this.$t('message.common.resourceSimple.WHQD'));
      this.loading = true;
      api.fetch(`/entrance/${item.strongerExecId}/kill`, {taskID: item.taskID},'get').then(() => {
        this.loading = false;
        this.$emit('close-modal');
        // stop execution(停止执行)
        this.$Notice.close(item.scriptPath);
        this.$Notice.warning({
          title: this.$t('message.common.resourceSimple.YXTS'),
          desc: `${this.$t('message.common.resourceSimple.YJTZZXJB')}: ${item.fileName || ''} ！`,
          name: item.scriptPath,
          duration: 3,
        });
      }).catch(() => {
        this.loading = false;
      });
    },
    async killJobAndEngine() {
      const item = this.lastClick;
      if (this.loading) return this.$Message.warning(this.$t('message.common.resourceSimple.DDJK'));
      if (!item.strongerExecId) return this.$Message.warning(this.$t('message.common.resourceSimple.WHQD'));
      if (!item.engineInstance) return this.$Message.warning(this.$t('message.common.resourceSimple.WHQD'));
      this.loading = true;
      await api.fetch(`/entrance/${item.strongerExecId}/kill`, {taskID: item.taskID}, 'get');
      api.fetch('/resourcemanager/engines').then((res) => {
        const engines = res.engines;
        const engine = engines.find((e) => e.engineInstance === item.engineInstance);
        if (engine) {
          const params = [{
            ticketId: engine.ticketId,
            moduleName: engine.moduleName,
            engineManagerInstance: engine.engineManagerInstance,
            creator: engine.creator,
          }];
          api.fetch(`/resourcemanager/enginekill`, params).then(() => {
            this.loading = false;
            this.$emit('close-modal');
            this.$Notice.close(item.scriptPath);
            this.$Notice.warning({
              title: this.$t('message.common.resourceSimple.YXTS'),
              desc: this.$t('message.common.resourceSimple.JSYQHRWCG'),
              name: item.scriptPath,
              duration: 3,
            });
          }).catch(() => {
            this.loading = false;
          });
        }
      }).catch(() => {
        this.loading = false;
      });
    },
    openHistoryScript() {
      const item = this.lastClick;
      if (item.requestApplicationName === 'IDE') {
        const supportModes = this.getSupportModes();
        const match = supportModes.find((s) => s.rule.test(item.fileName));
        const ext = match ? match.ext : '.hql';
        const name = `history_item_${item.taskID}${ext}`;
        const md5Id = util.md5(name);
        this.$emit('close-modal');
        if (this.$route.name === 'Home') {
          this.dispatch('Workbench:add', {
            id: md5Id, // Unique identification, even if the file name is changed, it can identify it as it is(唯一标识，就算文件名修改，它都能标识它是它)
            taskID: item.taskID,
            filename: name,
            filepath: item.scriptPath,
            // saveAs represents a temporary script that needs to be closed or saved when saved(saveAs表示临时脚本，需要关闭或保存时另存)
            saveAs: true,
            code: item.executionCode,
            type: 'historyScript',
          }, (f) => {
            if (f) {
              this.$Message.success(this.$t('message.common.resourceSimple.DKCG'));
            }
          });
        } else {
          // After the module is split, there are two cases, one is with scripts, and the other is without the general log display.(模块拆分后分两种情况，一种是带scriptis的，一种是不带走通用日志展示)
          const currentModules = util.currentModules();
          if (currentModules.hasScriptis) {
            this.$router.push({ path: '/home',
              query: {
                id: md5Id,
                taskID: item.taskID,
                filename: name,
                filepath: item.scriptPath,
                saveAs: true,
                type: 'historyScript',
                code: item.executionCode,
              } });
          } else {
            this.$router.push({path: '/log', query: { taskID: item.taskID }})
          }
        }
      }
    },
    getColor(item) {
      let color = '';
      if (item.status === 'Inited') {
        color = '#515a6e';
      } else if (item.status === 'Running') {
        color = 'rgb(45, 140, 240)';
      } else {
        color = 'rgba(243, 133, 243, 0.5)';
      }
      return color;
    },
    getClass(item) {
      const color = this.getColor(item);
      return {
        // border: `1px solid ${color}`,
        color,
      };
    },
    getIconClass(script) {
      let logos = this.getSupportModes().filter((item) => {
        return item.rule.test(script.fileName) || item.runType === script.runType;
      });
      if (logos.length > 0) {
        return {
          className: '',
          icon: logos[0].logo,
          color: logos[0].color
        }
      } else if (script.requestApplicationName === 'flowexecution' || script.requestApplicationName === 'Scheduler') {
        return {
          className: '',
          icon: 'fi-workflow1',
          color: '#00B0F0'
        }
      } else {
        return {
          className: '',
          icon: 'fi-bi',
          color: '#965DF2'
        }
      }
    },
    handleItemClick(item) {
      this.$emit('change-job-disabled', false);
      if (!this.lastClick) {
        this.lastClick = item;
        item.isActive = true;
      } else if (this.lastClick !== item) {
        this.lastClick.isActive = false;
        this.lastClick = item;
        item.isActive = true;
      }
    },
    handleItemMenu(ev, item) {
      this.handleItemClick(item);
      this.$refs.contextMenu.open(ev);
    },
    copyInfos() {
      util.executeCopy(JSON.stringify(this.lastClick));
      this.$Message.success(this.$t('message.common.resourceSimple.JBFZDZTRB'));
    },
    checkJobLength(type) {
      const list = this.jobList.filter((item) => item.requestApplicationName === type);
      return list.length;
    },
    openKillModal(type) {
      this.$refs.killModal.open({ type, name: '' });
    },
    kill(type) {
      if (type === this.$t('message.common.resourceSimple.YQRW')) {
        this.killJobAndEngine();
      } else {
        this.killJob();
      }
    },
    getLabel(item) {
      if (item.fileName) {
        return item.fileName;
      } else {
        // 取sourceJson里的值
        const nameJson = JSON.parse(item.sourceJson);
        if (item.requestApplicationName === 'IDE') {
          if (nameJson.fileName) {
            return nameJson.fileName;
          } else if (nameJson.scriptPath) {
            const arr = nameJson.scriptPath.split('/');
            return arr[arr.length - 1];
          } else  {
            return this.$t('message.common.resourceSimple.WZJBMC')
          }
        } else if (item.requestApplicationName === 'flowexecution') {
          if (nameJson.flowName) {
            return nameJson.flowName;
          } else {
            return this.$t('message.common.resourceSimple.GZLJB');
          }
        } else if (item.requestApplicationName === 'Scheduler') {
          return this.$t('message.common.resourceSimple.GZLJB');
        } else {
          return this.$t('message.common.resourceSimple.ZHJB');
        }
      }
    },
    convertJson(item) {
      if (item.sourceJson) {
        const json = JSON.parse(item.sourceJson);
        return json.fileName || json.flowName;
      }
      return '';
    }
  },
};
</script>
