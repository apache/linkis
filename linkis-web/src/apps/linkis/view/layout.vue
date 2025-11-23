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
  <div class="layout-body" :class="{ 'layout-top': showHeader}">
    <layout-header
      v-show="showHeader"
      @clear-session="clearSession"/>
    <router-view />
    <layout-footer v-show="showFooter"/>
  </div>
</template>
<script>
import headerModule from '../module/header';
import footerModule from '@/dss/module/footer';
import layoutMixin from '@/common/service/layoutMixin.js';
import eventbus from '@/common/helper/eventbus';
export default {
  components: {
    layoutFooter: footerModule.component,
    layoutHeader: headerModule.component,
  },
  mixins: [layoutMixin],
  computed: {
    showHeader() {
      return this.$route.query.noHeader || location.search.indexOf('noHeader') < 0
    },
    showFooter() {
      return this.$route.query.noFooter || location.search.indexOf('noFooter') < 0
    }
  },
  mounted() {
    eventbus.on('show_app_update_notice', () => {
      window.console.log('received message, update')
      this.$Modal.confirm({
        title: this.$t('message.linkis.findNewVer'),
        content: this.$t('message.linkis.updateNow'),
        onOk: async () => {
          try {
            location.reload()
          } catch (err) {
            window.console.warn(err)
          }
        },
        onCancel: () => {
          // do nothing
        }
      })
    })
  },
  unmounted() {
    eventbus.off('show_app_update_notice')
  }
};
</script>
