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
  <ul
    class="user-menu">
    <li class="user-menu-arrow"/>
    <li
      class="user-menu-item"
      v-for="(menu) in menuList"
      :key="menu.id"
      @click="handleClick(menu.id)">
      <Icon
        class="user-menu-item-icon"
        :type="menu.icon">
      </Icon>
      <span>{{ menu.name }}</span>
    </li>
  </ul>
</template>
<script>
import api from '@/common/service/api';
import storage from '@/common/helper/storage';
import mixin from '@/common/service/mixin';
import util from '@/common/util';
export default {
  name: 'Menu',
  mixins: [mixin],
  data() {
    return {
      menuList: [
        // {
        //     id: 'user-management',
        //     name: '用户管理',
        //     icon: 'ios-person-outline',
        // },
        {
          id: 'FAQ',
          name: this.$t('message.common.FAQ'),
          icon: 'ios-help-circle-outline',
        }, {
          id: 'clearCache',
          name: this.$t('message.common.clearCache'),
          icon: 'ios-trash-outline',
        },
        {
          id: 'changeLang',
          name: localStorage.getItem('locale') === 'zh-CN' ? 'English' : '简体中文',
          icon: 'md-repeat',
        }, {
          id: 'logout',
          name: this.$t('message.common.logOut'),
          icon: 'ios-log-out',
        }],
    };
  },
  methods: {
    handleClick(type) {
      switch (type) {
        case 'user-management':
          this.openUserManagement();
          break;
        case 'FAQ':
          this.openFAQ();
          break;
        case 'clearCache':
          this.clearCache();
          break;
        case 'logout':
          this.getRunningJob();
          break;
        case 'changeLang':
          this.changeLang();
          break;
      }
    },
    openUserManagement() {
      this.$Message.info(this.$t('message.common.userMenu.comingSoon'));
    },
    openFAQ() {
      util.windowOpen(this.getFAQUrl());
    },
    clearCache() {
      this.$Modal.confirm({
        title: this.$t('message.common.userMenu.title'),
        content: this.$t('message.common.userMenu.content'),
        onOk: () => {
          this.dispatch('dssIndexedDB:deleteDb');
          this.$Message.success(this.$t('message.common.userMenu.clearCacheSuccess'));
          setTimeout(() => {
            window.location.reload();
          }, 1000);
        },
        onCancel: () => {
        },
      });
    },
    getRunningJob() {
      this.logout();
    },
    logout() {
      api.fetch('/user/logout', {}).then(() => {
        this.$emit('clear-session');
        storage.set('need-refresh-proposals-hql', true);
        storage.set('need-refresh-proposals-python', true);
        this.$router.push({ path: '/login' });
      });
    },
    changeLang() {
      // Chinese to English(中文切换英文)
      if (localStorage.getItem('locale') === 'zh-CN') {
        localStorage.setItem('locale', 'en');
        localStorage.setItem('fes_locale', 'en-US');
      } else {
        localStorage.setItem('locale', 'zh-CN');
        localStorage.setItem('fes_locale', 'zh-CN');
      }
      window.location.reload();
    }
  },
};
</script>
