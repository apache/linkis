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
    class="progress"
    :style="{'overflow-y':'auto', 'padding-left': status ? '40px' : 0, height: height +'px'}">
    <we-toolbar
      :status="status"
      :application="application"
      @open-panel="openPanel"></we-toolbar>
    <steps
      :step-list="steps"
      v-if="steps.length"></steps>
    <Row
      class="total-progress"
      v-if="isStatusOk">
      <Col
        span="2"
        class="title">
        {{ $t('message.common.progress.title') + '：' }}
      </Col>
      <Col span="12">
        <Progress
          :percent="percent"
          status="active"/>
      </Col>
      <Col
        span="10"
        v-if="percent === 100">
        <span>{{$t('message.common.process')}}</span>
        <span class="progress-costtime">{{ costTime }}</span>
      </Col>
    </Row>
    <Row
      class="total-progress"
      v-if="isWaittingSizeShow">
      <Col
        span="2"
        class="title"
      >{{ $t('message.common.progress.title') + '：' }}</Col>
      <Col span="12">
        <span>{{ $t('message.common.progress.watingList', { num: waitingSize }) }}</span>
      </Col>
    </Row>
    <Table
      class="progress-table"
      v-if="isStatusOk"
      :columns="columns"
      :data="progressInfo"
      size="small"
      stripe
      border/>
  </div>
</template>
<script>
import steps from './steps.vue';
import weToolbar from './toolbar_progress.vue';
/**
 * Script execution progress tab panel(脚本执行进度tab面板)
 * ! 1. Shared with workflow node execution console.vue(与工作流节点执行console.vue 共用)
 */
export default {
  components: {
    steps,
    weToolbar,
  },
  props: {
    progressInfo: {
      type: Array,
      required: true,
      default: () => [],
    },
    current: {
      type: Number,
      default: 0,
    },
    waitingSize: Number,
    steps: {
      type: Array,
      required: true,
      default: () => [],
    },
    status: String,
    application: String,
    costTime: {
      type: String,
      defalut: `0 second`,
    },
    scriptViewState: Object
  },
  data() {
    let that = this;
    return {
      columns: [
        {
          title: this.$t('message.common.progress.columns.id.title'),
          key: 'id',
          width: 200,
        },
        {
          title: this.$t('message.common.progress.columns.progress.title'),
          key: 'action',
          render(h, { row }) {
            return h('div', {
              class: 'progress-wrap',
            }, [
              h('div', {
                class: 'progress-item progress-waiting',
                style: that.waitingStyle(row),
              }, {

              }),
              h('div', {
                class: 'progress-item progress-success',
                style: that.successStyle(row),
              }),
              h('div', {
                class: 'progress-item progress-failed',
                style: that.failedStyle(row),
              }),
            ]);
          },
        },
        {
          title: this.$t('message.common.progress.columns.taskNum.title'),
          key: 'action',
          render(h, { row }) {
            return h('div', [
              h('span', {
                class: 'progress-text',
                style: {
                  'color': '#2d8cf0',
                },
              }, `${that.$t('message.common.progress.columns.taskNum.status.running')}：${row.runningTasks || 0}`),
              h('span', {
                class: 'progress-text',
                style: {
                  'color': '#19be6b',
                },
              }, `${that.$t('message.common.progress.columns.taskNum.status.success')}：${row.succeedTasks || 0}`),
              h('span', {
                class: 'progress-text',
                style: {
                  'color': '#ed4014',
                },
              }, `${that.$t('message.common.progress.columns.taskNum.status.fail')}：${row.failedTasks || 0}`),
              h('span', {
                class: 'progress-text',
              }, `${that.$t('message.common.progress.columns.taskNum.status.totalTasks')}：${row.totalTasks || 0}`),
            ]);
          },
        },
      ],
    };
  },
  computed: {
    percent() {
      return Number((this.current * 100).toFixed(2));
    },
    isWaittingSizeShow() {
      // When waitingSize is equal to null, waitingSize >= 0 is true;(当waitingSize等于null时，waitingSize >= 0为true；)
      return (this.status === 'Scheduled' || this.status === 'Inited') && (this.waitingSize !== null && this.waitingSize >= 0);
    },
    isStatusOk() {
      return this.status && !(this.status === 'Scheduled' || this.status === 'Inited');
    },
    height() {
      return this.scriptViewState.bottomContentHeight
    }
  },
  methods: {
    successStyle({ failedTasks, runningTasks, succeedTasks, totalTasks }) {
      let succeedPercent = (succeedTasks / totalTasks) * 100;
      let runningPercent = (runningTasks / totalTasks) * 100;
      let borderStyle = {};
      if (failedTasks) {
        borderStyle = {
          'border-bottom-right-radius': 0,
          'border-top-right-radius': 0,
        };
      }
      if (runningTasks) {
        Object.assign(borderStyle, {
          'border-top-left-radius': 0,
          'border-bottom-left-radius': 0,
        });
      }
      if (succeedPercent) {
        Object.assign(borderStyle, {
          'min-width': '10px',
        });
      } else {
        Object.assign(borderStyle, {
          'min-width': '0px',
        });
      }
      return Object.assign(borderStyle, {
        'left': runningPercent + '%',
        'width': succeedPercent + '%',

      });
    },
    waitingStyle({ failedTasks, runningTasks, succeedTasks, totalTasks }) {
      let runningPercent = (runningTasks / totalTasks) * 100;
      let borderStyle = {};
      if (succeedTasks || failedTasks) {
        borderStyle = {
          'border-bottom-right-radius': 0,
          'border-top-right-radius': 0,
        };
      }
      if (runningPercent) {
        Object.assign(borderStyle, {
          'min-width': '10px',
        });
      } else {
        Object.assign(borderStyle, {
          'min-width': '0px',
        });
      }
      return Object.assign(borderStyle, {
        'width': runningPercent + '%',
      });
    },
    failedStyle({ failedTasks, runningTasks, succeedTasks, totalTasks }) {
      let leftPercent = ((succeedTasks + runningTasks) / totalTasks) * 100;
      let failedPercent = (failedTasks / totalTasks) * 100;
      let borderStyle = {};
      if (runningTasks || succeedTasks) {
        borderStyle = {
          'border-top-left-radius': 0,
          'border-bottom-left-radius': 0,
        };
      }
      if (failedPercent) {
        Object.assign(borderStyle, {
          'min-width': '10px',
        });
      } else {
        Object.assign(borderStyle, {
          'min-width': '0px',
        });
      }
      return Object.assign(borderStyle, {
        'left': leftPercent + '%',
        'width': failedPercent + '%',
      });
    },
    openPanel(type) {
      this.$emit('open-panel', type);
    }
  },
};
</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
  .progress {
    background: $body-background;
    .ivu-table-wrapper {
      border: none;
      .ivu-table th {
        background-color: $table-thead-blue-bg;
        color: $body-background;
      }
    }
    .total-progress {
      padding: 6px 0;
      color: $title-color;
      .title {
        text-align: center;
      }
    }
    .progress-costtime {
      color: $success-color;
    }
    .progress-wrap {
        position: $relative;
        width: 100%;
        height: 10px;
        background: $background-color-select-hover;
        border-radius: 100px;
    }
    .progress-item {
        position: absolute;
        top: 0;
        left: 0;
        height: 100%;
        transition: all $transition-time linear;
        border-radius: 10px;
    }
    .progress-success {
        background: $success-color;
    }
    .progress-failed {
        background: $error-color;
    }
    .progress-waiting {
        background: $primary-color;
    }
    .progress-waiting:after {
        content: '';
        opacity: 0;
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: $body-background;
        border-radius: 10px;
        animation: ivu-progress-active 2s $ease-in-out infinite;
    }
    .progress-text {
        font-size: $font-size-small;
        margin-right: 10px;
    }
    .total-progress {
        padding: 6px 0;
        color: $title-color;
        .title {
            text-align: center;
        }
    }
    .progress-costtime {
        color: $success-color;
    }
  }
</style>

