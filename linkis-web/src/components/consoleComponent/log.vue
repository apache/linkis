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
  <div class="workbench-log-view" :style="{height:height+'px'}">
    <div class="log-tools">
      <div class="log-tools-control">
        <Tabs
          v-model="curPage"
          class="log-tabs"
          @on-click="handleTabClick">
          <TabPane
            v-if="logs.all !== undefined"
            name="all"
            label="All"
          />
          <TabPane
            v-if="logs.error !== undefined"
            :label="errorLabel"
            name="error"
          />
          <TabPane
            v-if="logs.warning !== undefined"
            :label="warnLabel"
            name="warning"
          />
          <TabPane
            v-if="logs.info !== undefined"
            name="info"
            label="Info"/>
          <TabPane
            v-if="logs.code !== undefined"
            name="code"
            label="Code"
          />
        </Tabs>
        <Input
          v-model="searchText"
          :placeholder="$t('message.common.filter')"
          suffix="ios-search"
          size="small"
          class="log-search"/>
      </div>
    </div>
    <we-editor
      ref="logEditor"
      :value="formattedLogs()"
      :style="{height: height - 48 + 'px', overflow:'hidden'}"
      @scrollChange="change"
      @onload="editorOnload"
      type="log"/>
  </div>
</template>
<script>
import { trim, forEach, filter, debounce } from 'lodash';
import elementResizeEvent from '@/common/helper/elementResizeEvent';
export default {
  props: {
    status: String,
    logs: {
      type: Object,
      default: function() {
        return {
          all: '',
          error: '',
          warning: '',
          info: '',
        };
      },
    },
    logLine: {
      type: Number,
      default: 1,
    },
    scriptViewState: {
      type: Object,
      default: () => {
        return {};
      },
    }
  },
  data() {

    return {
      curPage: 'all',
      searchText: '',
      errorNum: 0,
      warnNum: 0,
      errorLabel: (h) => {
        return h('div', [
          h('span', 'Error'),
          h('Badge', {
            props: {
              count: this.errorNum,
              className: 'err-badge',
            },
          }),
        ]);
      },
      warnLabel: (h) => {
        return h('div', [
          h('span', 'Warning'),
          h('Badge', {
            props: {
              count: this.warnNum,
              className: 'warn-badge',
            },
          }),
        ]);
      }
    };
  },
  watch: {
    'status': function(val){
      if(val === 'Failed'){
        this.curPage = 'error'
      }
    },
    logLine(val) {
      if (this.$refs.logEditor.editor) {
        this.$refs.logEditor.editor.revealLine(val);
      }
    },
  },
  computed: {
    height() {
      return this.scriptViewState.bottomContentHeight
    }
  },
  mounted() {
    elementResizeEvent.bind(this.$el, this.resize);
    if(this.status === 'code') {
      this.curPage = 'code'
    }
  },
  beforeDestroy() {
    elementResizeEvent.unbind(this.$el);
  },
  methods: {
    resize: debounce(function() {
      if (this.$refs.logEditor && this.$refs.logEditor.editor) {
        this.$refs.logEditor.editor.layout();
      }
    }, 1000),
    handleTabClick() {
      this.$nextTick(() => this.$refs.logEditor.fold())
      this.$emit('tabClick')
    },
    change() {
      this.scriptViewState.cacheLogScroll = this.$refs.logEditor.editor.getScrollTop();
    },
    editorOnload() {
      this.$refs.logEditor.editor.setScrollPosition({ scrollTop: this.scriptViewState.cacheLogScroll || 0 });
    },
    unfold() {
      this.$refs.logEditor.unfold();
    },
    fold() {
      this.$refs.logEditor.fold();
    },
    formattedLogs() {
      let logs = {};
      Object.keys(this.logs).map((key) => {
        logs[key] = this.getSearchList(this.logs[key]);
      });
      this.getPointNum(logs);
      return logs[this.curPage]
    },
    getPointNum(logs) {
      const errorLogs = trim(logs.error).split('\n').filter((e) => !!e);
      const warnLogs = trim(logs.warning).split('\n').filter((e) => !!e);
      const errorRegex = /\sERROR\s.*/;
      const warnRegex = /\sWARN\s.*/;
      let errorCount = 0;
      let warnCount = 0;
      for (const str of errorLogs) {
        if (str.match(errorRegex)) {
          errorCount++;
        }
      }
      for (const str of warnLogs) {
        if (str.match(warnRegex)) {
          warnCount++;
        }
      }
      this.errorNum = errorCount;
      this.warnNum = warnCount;
    },
    getSearchList(log) {
      let MatchText = '';
      const val = this.searchText;
      if (!log) return MatchText;
      if (val) {
        // This part of the code is to make the regular expression not report an error, so add \ in front of the special symbol(这部分代码是为了让正则表达式不报错，所以在特殊符号前面加上\)
        let formatedVal = '';
        forEach(val, (o) => {
          if (/^[\w\u4e00-\u9fa5]$/.test(o)) {
            formatedVal += o;
          } else {
            formatedVal += `\\${o}`;
          }
        });
        // Global and case-insensitive mode, the regular pattern is to match characters other than newlines before and after searchText(全局和不区分大小写模式，正则是匹配searchText前后出了换行符之外的字符)
        let regexp = new RegExp(`.*${formatedVal}.*`, 'gi');
        MatchText = filter(log.split('\n'), (item) => {
          return regexp.test(item);
        }).join('\n');
        regexp = null;
      } else {
        MatchText = log;
      }
      return MatchText;
    }
  },
};
</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
  .workbench-log-view {
    height: 100%;
    .we-editor {
      height: calc(100% - 36px) !important;
    }
    .log-tools {
      height: 36px;
      line-height: 36px;
      padding-left: 10px;
      background: $background-color-base;
      position: $relative;
      border-bottom: 2px solid $border-color-base;
      overflow: hidden;
      margin-bottom: -2px;
      .log-tools-control {
        display: inline-block;
        position: $absolute;
        top: 2px;
        .log-tabs {
          display: inline-block;
          position: relative;
        }
        .log-search {
          width: 100px;
          position: relative;
          top: 3px;
          vertical-align: top;
          font-size: $font-size-small;
        }
        .err-badge {
          background: $error-color !important;
        }
        .warn-badge {
          background: $yellow-color !important;
        }
      }
    }
  }
</style>

