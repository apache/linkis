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
  <div class="wd-page">
    <div class="wrapper">
      <f-layout>
        <f-header class="header">
          <div class="header-left">
            <f-button class="collapse-btn" @click="handleCollapse">
              <template #icon>
                <div v-if="collapsed"><IndentOutlined :size="24"/></div>
                <div v-else><OutdentOutlined :size="24"/></div>
              </template>
            </f-button>
            <img src="@/dss/assets/images/Linkis.svg" @click="handleClickImage" />
          </div>
          <div class="header-right">
            <FDropdown trigger="click" :options="userOptions" @visible-change="handleVisibleChange" placement="bottom-end">
              <div class="user-options">
                {{ userName }}
                <DownOutlined v-if="!isShowUserOptions" class="icon" />
                <UpOutlined v-else class="icon" />
              </div>
            </FDropdown>
          </div>
        </f-header>
        <f-layout>
          <f-aside class="side" width="250px" collapsedWidth="48px" >
            <Sidebar :collapsed="collapsed" />
          </f-aside>
          <f-layout fixed :style="{ left: collapsed ? '48px' : '250px' }">
            <f-main class="main">
              <router-view />
            </f-main>
          </f-layout>
        </f-layout>
      </f-layout>
    </div>
  </div>
</template>
<script setup lang="ts">
import Sidebar from "@/components/sidebar/index.vue";
import { FDropdown, FMessage } from "@fesjs/fes-design";
import { DownOutlined, UpOutlined, IndentOutlined, OutdentOutlined, SwapOutlined, QuitOutlined } from '@fesjs/fes-design/icon';
import { h, reactive, ref } from "vue";
import { useI18n } from 'vue-i18n';
import storage from "./helper/storage";
import router from "./router";
import api from "./service/api";
const { t } = useI18n();

const collapsed = ref(false);
const isShowUserOptions = ref(false);
const userName = storage.get('userName', 'session');
const userOptions = reactive([
  {
    value: 'changeLang',
    label: () => h('div', {
      onClick: handleChangeLang
    }, t('message.common.lang')),
    icon: () => h(SwapOutlined),
  },
  {
    value: 'logout',
    label: () => h('div', {
      onClick: handleLogOut
    }, t('message.common.logOut')),
    icon: () => h(QuitOutlined),
  },
])

const handleCollapse = () => {
  collapsed.value = !collapsed.value;
}

const handleClickImage = () => {
  router.push('/console/globalHistoryManagement');
}

const handleVisibleChange = (visible: boolean) => {
  isShowUserOptions.value = visible;
}

const handleChangeLang = () => {
  const lang = storage.get('locale', 'local');
  if (!lang || lang === 'zh') {
    storage.set('locale', 'en', 'local');
  } else {
    storage.set('locale', 'zh', 'local');
  }
  window.location.reload();
}

const handleLogOut = () => {
  api.fetch('/user/logout', {}).then(() => {
    storage.set('need-refresh-proposals-hql', true);
    storage.set('need-refresh-proposals-python', true);
    router.push({ path: '/login' });
    FMessage.success(t('message.common.login.logoutSuccess'));
  });
}
</script>
<style lang="less" scoped>
.wd-page {
  height: 100%;
  background: #f7f8fa;
  .wrapper {
    width: 100%;
    height: 100%;
    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: #fff;
      border: 1px solid rgba(15,18,34,0.06);
      box-shadow: 0 2px 12px 0 rgba(15,18,34,0.10);
      z-index: 10;
      .header-left {
        display: flex;
        align-items: center;
        .collapse-btn {
          border-radius: 0;
          border: 0;
          background: #5384ff;
          color: #fff;
          height: 56px;
          transform: translate(-1px, -1px);
        }
        img {
          width: 200px;
          height: 56px;
          padding: 0 36px;
          cursor: pointer;
          background-color: #5384ff;
          transform: translateY(-1px);
        }
      }
      .header-right {
        margin-right: 16px;
        cursor: pointer;
        .user-options {
          margin-right: 6px;
          font-weight: 640;
          font-size: 18px;
          .icon {
            transform: translateY(2px);
          }
        }
      }
    }
    .side {
      min-height: calc(100vh - 54px);
    }
    :deep(.fes-layout-container) {
      background: #f7f8fa;
    }
    .main {
      min-width: calc(100vw - 300px);
      margin: 16px;
      padding: 24px;
      background: #fff;
    }
  }
}
</style>
