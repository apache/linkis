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
  <div class="log-editor-wrap">
    <div class="log-tools">
      <div class="log-tools-control">
        <Tabs
          v-model="curPage"
          class="log-tabs">
          <TabPane
            name="all"
            label="All"
          />
          <TabPane
            :label="errorLabel"
            name="error"
          />
          <TabPane
            :label="warnLabel"
            name="warning"
          />
          <TabPane
            name="info"
            label="Info"/>
        </Tabs>
        <Input
          v-model="searchText"
          placeholder="filter"
          suffix="ios-search"
          size="small"
          class="log-search"></Input>
      </div>
    </div>
    <we-editor
      style="height: calc(100% - 34px);"
      ref="logEditor"
      v-model="curLogs"
      type="log"/>
  </div>
</template>
<script>
import { trim, filter, forEach } from 'lodash';

export default {
  props: {
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
    fromLine: {
      type: Number,
      default: 1,
    },
  },
  data() {
    return {
      isLoading: false,
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
      },
    };
  },
  computed: {
    curLogs: {
      get() {
        const logs = this.formattedLogs();
        return logs[this.curPage];
      },
      set: function(val) {
        return val;
      },
    },
  },
  // watch: {
  //     fromLine(val) {
  //         if (this.$refs.logEditor.editor) {
  //             this.$refs.logEditor.editor.revealLine(val);
  //         }
  //     },
  // },
  methods: {
    formattedLogs() {
      let logs = {
        all: '',
        error: '',
        warning: '',
        info: '',
      };
      Object.keys(this.logs).map((key) => {
        logs[key] = this.getSearchList(this.logs[key]);
      });
      this.getPointNum(logs);
      return logs;
    },
    getPointNum(logs) {
      const errorLogs = trim(logs.error).split('\n').filter((e) => !!e);
      const warnLogs = trim(logs.warning).split('\n').filter((e) => !!e);
      this.errorNum = errorLogs.length;
      this.warnNum = warnLogs.length;
    },
    unfold() {
      this.$refs.logEditor.unfold();
    },
    fold() {
      this.$refs.logEditor.fold();
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
        const regexp = new RegExp(`.*${formatedVal}.*`, 'gi');
        MatchText = filter(log.split('\n'), (item) => {
          return regexp.test(item);
        }).join('\n');
      } else {
        MatchText = log;
      }
      return MatchText;
    },
  },
};
</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
.log-editor-wrap {
        border: $border-width-base $border-style-base $border-color-base;
        box-shadow: $shadow-card;
        position: $absolute;
        left: 0;
        right: 0;
        top: 40px;
        bottom: 10px;
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
                    position: $absolute;
                }
                .log-search {
                    width: 100px;
                    position: $absolute;
                    left: 350px;
                    top: 5px;
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
