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
  <div class="layout-body" :class="{ 'layout-top': !showHeader}">
    <layout-header
      v-show="showHeader"
      @clear-session="clearSession"
      @set-init="setInit"></layout-header>
    <keep-alive v-if="isInit">
      <router-view
        v-if="$route.meta.keepAlive"/>
    </keep-alive>
    <router-view
      v-if="!$route.meta.keepAlive"/>
    <layout-footer v-show="showFooter"/>
  </div>
</template>
<script>
import headerModule from '@/dss/module/header';
import footerModule from '@/dss/module/footer';
import layoutMixin from '@/common/service/layoutMixin.js';
export default {
  components: {
    layoutFooter: footerModule.component,
    layoutHeader: headerModule.component
  },
  data() {
    return {
      isInit: false
    }
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
  methods: {
    setInit() {
      this.isInit = true;
    },
  },
};
</script>
