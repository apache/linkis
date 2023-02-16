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
      v-if="showHeader "
      @clear-session="clearSession"
      @set-init="setInit"></layout-header>
    <keep-alive v-if="isInit">
      <router-view
        v-if="$route.meta.keepAlive"/>
    </keep-alive>
    <router-view
      v-if="!$route.meta.keepAlive"/>
    <layout-footer v-if="showFooter"/>
  </div>
</template>
<script>
import Vue from 'vue'
import storage from '@/common/helper/storage';
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
      isInit: false,
      interval: null
    }
  },
  mixins: [layoutMixin],
  computed: {
    showHeader() {
      return this.$route.query.noHeader || location.search.indexOf('noHeader') < 0
    },
    showFooter() {
      return this.$route.query.noFooter || location.search.indexOf('noFooter') < 0
    },
    isEmbedInFrame() {
      // If it is imported by iframe, top !== self returns true, which is used to distinguish between running alone or being introduced. Only when running alone, a watermark should be added.(如果是被iframe引入时 top !== self 返回true，用来区分单独跑还是被引入，只有单独跑时要加水印)
      return top !== self;
    }
  },
  methods: {
    setInit() {
      this.isInit = true;
    },
    showTime(now) {
      const year = now.getFullYear();
      const month = now.getMonth();
      const date = now.getDate();
      const hour = this.addZero(now.getHours());
      const minute = this.addZero(now.getMinutes());
      return `${year}-${month}-${date} ${hour}:${minute}`
    },
    addZero(num) {
      if (num < 10) return '0' + num
      return num + '';
    },
    setWaterMark() {
      let userNameAndPass = storage.get('saveUserNameAndPass', 'local');
      let watermark = null;
      const username = userNameAndPass ? userNameAndPass.split('&')[0] : '';
      if (username) {
        watermark = username + ' ' + this.showTime(new Date());
        Vue.prototype.$watermark.set(watermark)
      }
    }
  },
  created() {
    if (!this.isEmbedInFrame && storage.get('enableWatermark')) {
      this.setWaterMark();
      this.interval = setInterval(this.setWaterMark, 1000)
    }
  },
  beforeDestroy() {
    if (!this.isEmbedInFrame && storage.get('enableWatermark')) {
      Vue.prototype.$watermark.clear();
      clearInterval(this.interval)
    }
  }
};
</script>
