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
  <div class="console-page">
    <div class="console-page-content-head">
      <div>
        <span class="console-page-content-title">{{ $t('message.linkis.title') }}</span>
      </div>
    </div>
    <div class="console-page-content-body">
      <div class="content-body-side-nav">
        <Card
          :padding="sideNavList.padding"
          :icon="sideNavList.icon"
          shadow
          class="content-body-card">
          <CellGroup
            v-for="(item, index2) in sideNavList.children"
            :key="index2"
            @on-click="handleCellClick">
            <Cell
              v-if="!isLogAdmin? (item.path !=='/console/ECM')&&(item.path !=='/console/microService'):true"
              :key="index2"
              :class="{ crrentItem: crrentItem === item.key }"
              :title="item.name"
              :name="item.key"/>
          </CellGroup>
          
        </Card>
      </div>
      <div
        class="content-body-side-right">
        <div class="content-body-side-right-title">
          <Breadcrumb>
            <BreadcrumbItem :to="skipPath">{{ breadcrumbSecondName }}</BreadcrumbItem>
            <BreadcrumbItem v-if="$route.name === 'viewHistory'">{{ $route.query.taskID }}</BreadcrumbItem>
            <template v-if="$route.name === 'EngineConnList'">
              <BreadcrumbItem>{{ $route.query.instance }}</BreadcrumbItem>
              <BreadcrumbItem>EngineConnList</BreadcrumbItem>
            </template>
          </Breadcrumb>
        </div>
        <div
          class="content-body-side-right-content">
          <router-view></router-view>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import storage from '@/common/helper/storage';
import api from '@/common/service/api';
export default {
  name: 'Layout',
  data() {
    return {
      crrentItem: '1-1',
      isLogAdmin: false,
      sideNavList: {
        key: '1',
        name: this.$t('message.linkis.sideNavList.function.name'),
        padding: 0,
        icon: 'ios-options',
        children: [
          { key: '1-1', name: this.$t('message.linkis.sideNavList.function.children.globalHistory'), path: '/console/globalHistory' },
          { key: '1-2', name: this.$t('message.linkis.sideNavList.function.children.resource'), path: '/console/resource' },
          { key: '1-3', name: this.$t('message.linkis.sideNavList.function.children.setting'), path: '/console/setting' },
          { key: '1-4', name: this.$t('message.linkis.sideNavList.function.children.dateReport'), path: '/console/globalValiable' },
          { key: '1-6', name: this.$t('message.linkis.sideNavList.function.children.ECMManage'), path: '/console/ECM' },
          { key: '1-7', name: this.$t('message.linkis.sideNavList.function.children.microserviceManage'), path: '/console/microService' },
          { key: '1-5', name: this.$t('message.linkis.sideNavList.function.children.globalValiable'), path: '/console/FAQ' },
        ],
      },
      breadcrumbSecondName: this.$t('message.linkis.sideNavList.function.children.globalHistory')
    };
  },
  computed: {
    skipPath() {
      let path = '';
      if(this.$route.name === 'viewHistory') path = '/console';
      if(this.$route.name === 'EngineConnList') path = '/console/ECM';
      return path;
    }
  },

  created() {
    // 根据路径显示页面的标题
    this.sideNavList.children.forEach(element => {
      if(element.path === this.$route.path) {
        this.breadcrumbSecondName = element.name
      }
    });
    // 获取是否是历史管理员权限
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then((res) => {
      this.isLogAdmin = res.admin;
      storage.set('isLogAdmin',res.admin,'session');
    }) 
  },
  methods: {
    handleCellClick(index) {
      const activedCellParent = this.sideNavList;
      this.crrentItem = index;
      const activedCell = activedCellParent.children.find((item) => item.key === index);
      this.breadcrumbFirstName = activedCellParent.name;
      this.breadcrumbSecondName = activedCell.name;
      storage.set('lastActiveConsole', activedCell);
      this.$router.push({
        path: activedCell.path,
        query: {
          workspaceId: this.$route.query.workspaceId
        },
      });
    }
  },
  beforeRouteEnter(to, from, next) {
    if (to.name === 'FAQ' && from.name === 'Home') {
      next((vm) => {
        vm.breadcrumbFirstName = this.$t('message.linkis.sideNavList.function.name');
        vm.breadcrumbSecondName = this.$t('message.linkis.sideNavList.function.children.globalValiable');
      });
    } else if ((to.name === 'Console' && from.name === 'Home') || (to.name === 'Console' && from.name === 'Project') || (to.name === 'Console' && from.name === 'Workflow') || !from.name) {
      const lastActiveConsole = storage.get('lastActiveConsole');
      // 如果为历史详情则直接刷新
      if(to.name === 'viewHistory') return next();
      next((vm) => {
        vm.handleCellClick(lastActiveConsole ? lastActiveConsole.key : '1-1');
      });
    }
    next();
  },
};
</script>
<style lang="scss" src="@/apps/linkis/assets/styles/console.scss"></style>
<style lang="scss" scoped>
  .crrentItem {
    color: #338cf0;
  }
</style>

