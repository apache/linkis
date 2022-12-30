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
        <Icon type="ios-arrow-back" size="20" color="#338cf0" class="back" @click="back"></Icon>
        <span class="console-page-content-title">{{$t('message.linkis.sideNavList.function.children.userResourceManagement')}}</span>
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
            @on-click="handleCellClick(item)">
            <Cell
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
            <BreadcrumbItem>
              {{ breadcrumbSecondName }}
            </BreadcrumbItem>
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
          { key: '1-1', name: this.$t('message.linkis.sideNavList.function.children.udfFunctionManage'), path: '/urm/udfManagement' },
          { key: '1-2', name: this.$t('message.linkis.sideNavList.function.children.functionManagement'), path: '/urm/functionManagement' }
        ],
      },
      breadcrumbSecondName: this.$t('message.linkis.sideNavList.function.children.udfFunctionManage')
    };
  },
  computed: {

  },

  created() {
    // Display the title of the page based on the path(根据路径显示页面的标题)
    this.sideNavList.children.forEach(element => {
      if(element.path === this.$route.path) {
        this.breadcrumbSecondName = element.name
      }
    });
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then((res) => {
      this.isLogAdmin = res.admin;
      storage.set('isLogAdmin',res.admin,'session');
    })
    const keyMap = {
      '/urm/functionManagement': '1-2',
      '/urm/udfManagement': '1-1'
    }
    this.crrentItem = keyMap[this.$route.path]
  },
  methods: {
    handleCellClick(item) {
      const activedCellParent = this.sideNavList;
      this.crrentItem = item.key;
      this.breadcrumbFirstName = activedCellParent.name;
      this.breadcrumbSecondName = item.name;
      storage.set('lastActiveConsole', item);
      this.$router.push({
        path: item.path,
        query: {
          workspaceId: this.$route.query.workspaceId
        },
      });
    },
    back() {
      this.$router.push({
        path: '/console/globalHistory',
      });
    }
  }
};
</script>
<style lang="scss" src="@/apps/linkis/assets/styles/console.scss"></style>
<style lang="scss" scoped>
  .crrentItem {
    color: #338cf0;
  }
  .back {
    position: relative;
    top: -3px;
    cursor: pointer;
  }
</style>

