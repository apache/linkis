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
  <div class="log" style="position:relative">
    <Tabs @on-click="onClickTabs" :value="tabName">
      <TabPane name="stdout" label="stdout"></TabPane>
      <TabPane name="stderr" label="stderr"></TabPane>
    </Tabs>
    <Button class="backButton" type="primary" @click="back">{{$t('message.linkis.back')}}</Button>
    <log :logs="logs" :scriptViewState="scriptViewState"/>
    <Page
      ref="page"
      :total="page.totalSize"
      :page-size="page.pageSize"
      :current="page.pageNow"
      class-name="page engine-log-page"
      size="small"
      :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
      @on-change="change"/>
  </div>
</template>
<script>
import api from '@/common/service/api';
import log from '@/components/consoleComponent/log.vue'
import elementResizeEvent from '@/common/helper/elementResizeEvent';
export default {
  components: {
    log
  },
  data() {
    return {
      tabName: 'stdout',
      page: {
        totalSize: 0,
        pageSize: 1000,
        pageNow: 1,
      },
      logs: {
        all: ''
      },
      scriptViewState: {
        bottomContentHeight: window.innerHeight - 353
      }
    };
  },
  computed: {
  },
  created() {

  },
  mounted() {
    window.addEventListener("resize", this.resize);
    elementResizeEvent.bind(this.$el, this.resize);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.resize);
    elementResizeEvent.unbind(this.$el);
  },
  methods: {
    // Toggle pagination(切换分页)
    change(val) {
      this.page.pageNow = val;
      this.getLogs((val - 1) * this.page.pageSize)
    },
    async getLogs(fromLine, param) {
      if (param) {
        this.param = param
      }
      if (this.param) {
        const params = {
          applicationName: this.param.applicationName,
          emInstance: this.param.emInstance,
          instance: this.param.instance,
          parameters: {
            pageSize: 1000,
            fromLine,
            logType: this.tabName
          }
        }
        let res = await api.fetch('/linkisManager/openEngineLog', params, 'post') || {};
        if (res && res.result) {
          if (res.result.rows < 1000) { // the last page(最后一页)
            this.page.totalSize = this.page.pageNow * 1000
          } else {
            this.page.totalSize = (this.page.pageNow + 1) * 1000
          }
          this.logs = {
            all: res.result.logs ? res.result.logs.join('\n') : ''
          }
        }
      }
    },
    resize() {
      this.scriptViewState = {
        bottomContentHeight: window.innerHeight - 353
      }
    },
    onClickTabs(name) {
      this.tabName = name
      this.page.pageNow = 1
      this.getLogs(0)
    },
    back() {
      this.$emit('back')
    }
  }
};
</script>
<style lang="scss" scoped>
.log {
  height: 100%;
}
 .backButton {
  position: absolute;
  top: -2px;
  right: 20px;
}
.page {
  text-align: center
}
</style>
<style>
.engine-log-page .ivu-page-item, .engine-log-page .ivu-page-item-jump-prev {
  display: none;
}
.engine-log-page li.ivu-page-next {
  padding-left: 20px;
}
</style>
