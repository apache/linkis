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
import api from '@/common/service/api';
import SUPPORTED_LANG_MODES from '@/common/config/scriptis';

export default {
  data: function () {
    return {
      SUPPORTED_LANG_MODES,
    };
  },
  beforeRouteLeave(to, from, next) {
    if (typeof this.beforeLeaveHook === 'function') {
      let hookRes = this.beforeLeaveHook();
      if (hookRes === false) {
        next(false);
      } else if (hookRes && hookRes.then) {
        hookRes.then((flag) => {
          next(flag);
        });
      } else {
        next(true);
      }
    } else {
      next(true);
    }
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
      const projectJson = vsBi.enhanceJson;
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
    getCommonProjectId(type, query) {
      return api.fetch(`/dss/getAppjointProjectIDByApplicationName`, {
        projectID: query.projectID,
        applicationName: type
      }, 'get').then((res) => {
        localStorage.setItem('appJointProjectId', res.appJointProjectID);
      })
    },
    async gotoCommonIframe(type, query = {}) {
      const baseInfo = storage.get('baseInfo', 'local');
      if (!baseInfo) return;
      const info = baseInfo.applications.find((item) => item.name === type) || {};
      // 根据是否有projectid来确定是走首页还是工程页
      let url = '';
      if (query.projectID) {
        await this.getCommonProjectId(type, query);
        url = info.projectUrl
      } else {
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
