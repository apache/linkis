/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import storage from '@/common/helper/storage';
import SUPPORTED_LANG_MODES from '@/common/config/scriptis';

export default {
  data: function () {
    return {
      SUPPORTED_LANG_MODES,
    };
  },
  
  created: function () {},
  mounted: function () {},
  beforeDestroy: function () {},
  destroyed: function () {},
  methods: {
    getVsBiUrl(name = 'visualis') {
      const baseInfo = storage.get('baseInfo', 'local');
      if (!baseInfo) return;
      const vsBi = baseInfo.applications.find((item) => item.name === name) || {};
      return vsBi.url;
    },
    getProjectJsonResult(key, name = 'visualis') {
      // 用于判断返回什么样的返回值
      const baseInfo = storage.get('baseInfo', 'local');
      if (!baseInfo) return;
      const vsBi = baseInfo.applications ? (baseInfo.applications.find((item) => item.name === name) || {}) : {};
      let projectJson = vsBi.enhanceJson;
      if(!projectJson && key==='rsDownload') {
        projectJson = '{"watermark": false, "rsDownload": true}'
      }
      return projectJson ? JSON.parse(projectJson)[key] : true;
    },
    getFAQUrl() {
      const baseInfo = storage.get('baseInfo', 'local');
      if (!baseInfo) return;
      const url = baseInfo.DWSParams.faq;
      return url;
    },
    getSupportModes() {
      return this.SUPPORTED_LANG_MODES;
    },
    async gotoCommonIframe(type, query = {}) {
      const baseInfo = storage.get('baseInfo', 'local');
      if (!baseInfo) return;
      const info = baseInfo.applications.find((item) => item.name === type) || {};
      // 根据是否有projectid来确定是走首页还是工程页
      let url = '';
      if (!query.projectID) {
        localStorage.removeItem('appJointProjectId')
        url = info.homepageUrl
      }
      // 如果没有提示用户功能暂未开发
      if (Object.keys(info).length === 0) {
        this.$Message.warning(this.$t('message.common.warning.comingSoon'));
      } else {
        if (!info.ifIframe) {
          this.$router.push({
            path: url,
            query
          });
        } else {
          this.$router.push({
            name: 'commonIframe',
            query: {
              ...query,
              url,
              type
            }
          })
        }
      }
    },
    getUserName() {
      return  storage.get("userName") || null;
    },
  },
};
