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
    <template v-if="!noLayout">
      <div class="console-page-content-head">
        <div>
          <span class="console-page-content-title">{{ $t('message.linkis.title') }}</span>
        </div>
      </div>
      <div class="console-page-content-body">
        <div class="content-body-side-nav">
          <Card style="height:87vh; overflow: auto;" :padding="sideNavList.padding"
            :icon="sideNavList.icon" shadow class="content-body-card">
            <CellGroup v-for="(item, index2) in sideNavList.children" :key="index2"
              @on-click="handleCellClick">
              <Cell
                v-if="(!isLogAdmin? (item.path !=='/console/ECM')&&(item.path !=='/console/microService')&&(item.key !== '1-12'):true) && (item.key !== '1-13' || isPythonModuleEnabled)"
                :key="index2" :class="{ crrentItem: crrentItem === item.key }" :title="item.name"
                :name="item.key">
                <div>
                  <span>{{item.name}}</span>
                  <div class="sub-menu-row">
                    <Icon
                      v-show="item.showSubMenu && (item.key === '1-8' || item.key === '1-9' || item.key === '1-10' || item.key === '1-12' || item.key === '1-13')"
                      type="ios-arrow-down" class="user-icon" />
                    <Icon
                      v-show="!item.showSubMenu && (item.key === '1-8' || item.key === '1-9' || item.key === '1-10' || item.key === '1-12' || item.key === '1-13')"
                      type="ios-arrow-up" class="user-icon" />
                  </div>
                </div>
                <div
                  v-if="(item.key === '1-8' || item.key === '1-9' || item.key === '1-10' || item.key === '1-12' || item.key === '1-13') && !item.showSubMenu">
                  <div @click.stop="">
                    <CellGroup
                      v-for="(item3, index3) in getChildMap(item.key).children"
                      :key="index3" @on-click="clickToRoute">
                      <div
                        v-if="isLogAdmin ? true : ['1-8-1', '1-9-2', '1-9-1', '1-10-7', '1-13-1'].includes(item3.key)">
                        <Cell :key="index3" :class="{ crrentItem: crrentItem === item3.key }"
                          :title="item3.name" :name="item3.key" />
                      </div>
                    </CellGroup>
                  </div>
                </div>
              </Cell>
            </CellGroup>
          </Card>
        </div>
        <div class="content-body-side-right">
          <div class="content-body-side-right-title">
            <Breadcrumb
              v-if="!['resource', 'resourceEngineConnList', 'microService', 'eurekaService'].includes($route.name)">
              <BreadcrumbItem :to="skipPath">
                <Icon v-if="skipPath" type="ios-arrow-back" size="16" color="#338cf0"></Icon>
                {{ breadcrumbSecondName }}
              </BreadcrumbItem>
              <BreadcrumbItem v-if="$route.name === 'viewHistory'">{{ $route.query.taskID }}
              </BreadcrumbItem>
              <BreadcrumbItem v-if="$route.name === 'codeDetail'">{{ $route.query.id }}
              </BreadcrumbItem>
              <template v-if="$route.name === 'EngineConnList'">
                <BreadcrumbItem>{{ $route.query.instance }}</BreadcrumbItem>
                <BreadcrumbItem>{{$t('message.linkis.sideNavList.function.children.EngineConnList')}}</BreadcrumbItem>
              </template>
            </Breadcrumb>
            <Tabs v-if="$route.name === 'resource' || $route.name === 'resourceEngineConnList'"
              value="resourceEngineConnList" @on-click="clickResourceTab" class="resource-tab">
              <Tab-pane name="resourceEngineConnList"
                :label="$t('message.linkis.sideNavList.function.children.resourceEngineConnList')"
                href="/ecm"></Tab-pane>
              <Tab-pane name="resource"
                :label="$t('message.linkis.sideNavList.function.children.resource')"
                href="/resource">
              </Tab-pane>
            </Tabs>
            <Tabs v-if="$route.name === 'microService' || $route.name === 'eurekaService'"
              value="microService" @on-click="clickResourceTab" class="resource-tab">
              <Tab-pane name="microService"
                :label="$t('message.linkis.sideNavList.function.children.microserviceManage')"
                href="/microService"></Tab-pane>
              <Tab-pane name="eurekaService"
                :label="$t('message.linkis.sideNavList.function.children.eurekaService')"
                href="/eurekaService">
              </Tab-pane>
            </Tabs>
          </div>
          <div class="content-body-side-right-content">
            <keep-alive :include="['codeQuery']">
              <router-view />
            </keep-alive>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <router-view></router-view>
    </template>
  </div>
</template>
<script>
import storage from '@/common/helper/storage'
import api from '@/common/service/api'
import { isPythonModuleEnabled } from '../../router'
export default {
  name: 'Layout',
  mounted() {
    window.addEventListener('message', (e) => {
      if(e.data === 'Unauhorized') {
        this.$emit('clear-session');
        storage.set('need-refresh-proposals-hql', true);
        storage.set('need-refresh-proposals-python', true);
        this.$router.push({ path: '/login' });
      }
    })
    if(!localStorage.getItem('hasRead')) {
      // Jump to pythonModule if enabled, otherwise jump to globalHistory
      if (isPythonModuleEnabled) {
        this.clickToRoute('1-13-1')
      } else {
        this.clickToRoute('1-1')
      }
    }
  },
  unmounted() {
    window.removeEventListener('message', (e) => {
      if(e.data === 'Unauhorized') {
        this.$emit('clear-session');
        storage.set('need-refresh-proposals-hql', true);
        storage.set('need-refresh-proposals-python', true);
        this.$router.push({ path: '/login' });
      }
    })
  },
  data() {
    return {
      crrentItem: '1-1',
      isLogAdmin: false,
      isPythonModuleEnabled: isPythonModuleEnabled,
      sideNavList: {
        key: '1',
        name: this.$t('message.linkis.sideNavList.function.name'),
        padding: 0,
        icon: 'ios-options',
        children: [
          {
            key: '1-1',
            name: this.$t(
              'message.linkis.sideNavList.function.children.globalHistory'
            ),
            path: '/console/globalHistory',
          },
          {
            key: '1-2',
            name: this.$t(
              'message.linkis.sideNavList.function.children.resource'
            ),
            path: '/console/resourceEngineConnList',
          },
          {
            key: '1-3',
            name: this.$t(
              'message.linkis.sideNavList.function.children.setting'
            ),
            path: '/console/setting',
          },
          {
            key: '1-4',
            name: this.$t(
              'message.linkis.sideNavList.function.children.dateReport'
            ),
            path: '/console/globalValiable',
          },
          {
            key: '1-6',
            name: this.$t(
              'message.linkis.sideNavList.function.children.ECMManage'
            ),
            path: '/console/ECM',
          },
          {
            key: '1-7',
            name: this.$t(
              'message.linkis.sideNavList.function.children.microserviceManage'
            ),
            path: '/console/microService',
          },
          {
            key: '1-8',
            name: this.$t(
              'message.linkis.sideNavList.function.children.dataSourceManage'
            ),
            showSubMenu: true,
          },
          {
            key: '1-9',
            name: this.$t(
              'message.linkis.sideNavList.function.children.udfFunctionTitle'
            ),
            path: '/console/urm/udfManagement',
            showSubMenu: true,
          },
          {
            key: '1-13',
            name: this.$t(
              'message.linkis.sideNavList.function.children.taskResourceManage'
            ),
            showSubMenu: true,
          },
          {
            key: '1-10',
            name: this.$t(
              'message.linkis.sideNavList.function.children.basedataManagement'
            ),
            showSubMenu: true,
          },
          { key: '1-11', name: this.$t('message.linkis.sideNavList.function.children.codeQuery'), path: '/console/codeQuery' },
          {
            key: '1-12',
            name: this.$t(
              'message.linkis.sideNavList.function.children.opsTool'
            ),
            showSubMenu: true,
          },
          // {key: '1-13', name: this.$t('message.linkis.sideNavList.function.children.pythonModule'), path: '/console/pythonModule' },
        ],
      },
      datasourceNavList: {
        key: '1',
        name: this.$t('message.linkis.sideNavList.function.name'),
        padding: 0,
        icon: 'ios-options',
        children: [
          {
            key: '1-8-1',
            name: this.$t(
              'message.linkis.sideNavList.function.children.dataSourceManage'
            ),
            path: '/console/dataSource',
          },
          {
            key: '1-8-2',
            name: this.$t(
              'message.linkis.sideNavList.function.children.datasourceEnv'
            ),
            path: '/console/datasourceEnv',
          },
          {key: '1-8-3', name: this.$t('message.linkis.sideNavList.function.children.datasourceType'), path: '/console/datasourceType' },
          {key: '1-8-4', name: this.$t('message.linkis.sideNavList.function.children.datasourceAccess'), path: '/console/datasourceAccess' },
          {
            key: '1-8-5',
            name: this.$t(
              'message.linkis.sideNavList.function.children.datasourceTypeKey'
            ),
            path: '/console/datasourceTypeKey',
          },
        ],
      },
      basedataNavList: {
        key: '1',
        name: this.$t('message.linkis.sideNavList.function.name'),
        padding: 0,
        icon: 'ios-options',
        children: [
          {
            key: '1-10-1',
            name: this.$t(
              'message.linkis.sideNavList.function.children.gatewayAuthToken'
            ),
            path: '/console/gatewayAuthToken',
          },
          {
            key: '1-10-2',
            name: this.$t(
              'message.linkis.sideNavList.function.children.errorCode'
            ),
            path: '/console/errorCode',
          },
          {
            key: '1-10-3',
            name: this.$t(
              'message.linkis.sideNavList.function.children.rmExternalResourceProvider'
            ),
            path: '/console/rmExternalResourceProvider',
          },
          {
            key: '1-10-4',
            name: this.$t(
              'message.linkis.sideNavList.function.children.EnginePluginManagement'
            ),
            path: '/console/EnginePluginManagement',
          },
          {
            key: '1-10-5',
            name: this.$t(
              'message.linkis.sideNavList.function.children.tenantTagManagement'
            ),
            path: '/console/tenantTagManagement',
          },
          {
            key: '1-10-9',
            name: this.$t(
              'message.linkis.sideNavList.function.children.departmentTagManagement'
            ),
            path: '/console/departmentTagManagement',
          },
          {
            key: '1-10-6',
            name: this.$t(
              'message.linkis.sideNavList.function.children.ipListManagement'
            ),
            path: '/console/ipListManagement',
          },
          {
            key: '1-10-7',
            name: this.$t(
              'message.linkis.sideNavList.function.children.acrossClusterRule'
            ),
            path: '/console/acrossClusterRule',
          },
          {
            key: '1-10-8',
            name: this.$t(
              'message.linkis.sideNavList.function.children.configManagement'
            ),
            path: '/console/configManagement',
          },
        ],
      },
      urmSideNavList: {
        key: '1',
        name: this.$t('message.linkis.sideNavList.function.name'),
        padding: 0,
        icon: 'ios-options',
        children: [
          {
            key: '1-9-1',
            name: this.$t(
              'message.linkis.sideNavList.function.children.udfFunctionManage'
            ),
            path: '/console/urm/udfManagement',
          },
          {
            key: '1-9-2',
            name: this.$t(
              'message.linkis.sideNavList.function.children.functionManagement'
            ),
            path: '/console/urm/functionManagement',
          },
          {key: '1-9-3', name: this.$t('message.linkis.sideNavList.function.children.udfManager'), path: '/console/udfManager' },
          {key: '1-9-4', name: this.$t('message.linkis.sideNavList.function.children.udfTree'), path: '/console/udfTree' },
          
        ],
      },
      opsTool: {
        key: '1',
        name: this.$t('message.linkis.sideNavList.function.children.opsTool'),
        padding: 0,
        icon: 'ios-options',
        children: [
          {
            key: '1-12-1',
            name: this.$t(
              'message.linkis.sideNavList.function.children.userConfig'
            ),
            path: '/console/userConfig',
          },
        ],
      },
      taskResourceManage: {
        key: '1',
        name: this.$t('message.linkis.sideNavList.function.children.taskResourceManage'),
        padding: 0,
        icon: 'ios-options',
        children: [
          {
            key: '1-13-1',
            name: this.$t(
              'message.linkis.sideNavList.function.children.pythonModule'
            ),
            path: '/console/pythonModule',
          },
        ],
      },
      breadcrumbSecondName: this.$t(
        'message.linkis.sideNavList.function.children.globalHistory'
      ),
    }
  },
  computed: {
    skipPath() {
      let path = ''
      if (this.$route.name === 'viewHistory') path = '/console'
      if (this.$route.name === 'codeDetail') path = '/console/codeQuery'
      if (this.$route.name === 'EngineConnList') path = '/console/ECM'
      return path
    },
    isEmbedInFrame() {
      // If it is introduced by iframe top !== self returns true, used to distinguish between running alone or being introduced(如果是被iframe引入时 top !== self 返回true，用来区分单独跑还是被引入)
      return top !== self
    },
    noLayout() {
      return this.$route.meta.noLayout
    },
  },
  async created() {
    // Display the title of the page based on the path(根据路径显示页面的标题)
    this.sideNavList.children.forEach((element) => {
      if (element.path === this.$route.path) {
        this.breadcrumbSecondName = element.name
      }
    })
    if (this.$route.name === 'codeDetail') {
      this.breadcrumbSecondName = this.$t(
        'message.linkis.sideNavList.function.children.codeQuery'
      )
      this.crrentItem = '1-11'
    }
    // Get whether it is a historical administrator(获取是否是历史管理员权限)
    await api.fetch('/jobhistory/governanceStationAdmin', 'get').then((res) => {
      this.isLogAdmin = res.admin
      sessionStorage.setItem('canResultSet', res.canResultSet)
      storage.set('isLogAdmin', res.admin, 'session')
    })
    this.isLogAdmin = storage.get('isLogAdmin', 'session');
    // Whether the result set can be downloaded
    await api.fetch('/user/baseinfo', 'get').then((res) => {
      storage.set('resultSetExportEnable', res.resultSetExportEnable ? true : false,'session')
      storage.set('clusterInfo', res.linkisClusterName || '')
      storage.set('engineLogOnlyAdminEnable', res.engineLogOnlyAdminEnable || false)
    })
  },
  methods: {
    getChildMap(key) {
      const map = {
        '1-8': this.datasourceNavList,
        '1-9': this.urmSideNavList,
        '1-10': this.basedataNavList,
        '1-12': this.opsTool,
        '1-13': this.taskResourceManage
      }
      return map[key]
    },
    handleCellClick(index) {
      if (index === '1-8') {
        this.sideNavList.children[6].showSubMenu =
          !this.sideNavList.children[6].showSubMenu
        return
      }
      if (index === '1-9') {
        this.sideNavList.children[7].showSubMenu =
          !this.sideNavList.children[7].showSubMenu
        return
      }
      if (index === '1-13') {
        this.sideNavList.children[8].showSubMenu =
          !this.sideNavList.children[8].showSubMenu
        return
      }
      if (index === '1-10') {
        this.sideNavList.children[9].showSubMenu =
          !this.sideNavList.children[9].showSubMenu
        return
      }
      if (index === '1-12') {
        this.sideNavList.children[11].showSubMenu =
          !this.sideNavList.children[11].showSubMenu
        return
      }
      // index = index.split('-')[0] + '-' + index.split('-')[1]; //防止出现三级菜单
      let activedCellParent
      switch (index) {
        case '1-8-1':
          activedCellParent = this.datasourceNavList
          this.sideNavList.children[6].showSubMenu = false
          break
        case '1-8-2':
          activedCellParent = this.datasourceNavList
          this.sideNavList.children[6].showSubMenu = false
          break
        case '1-9-1':
          activedCellParent = this.urmSideNavList
          this.sideNavList.children[7].showSubMenu = false
          break
        case '1-9-2':
          activedCellParent = this.urmSideNavList
          this.sideNavList.children[7].showSubMenu = false
          break
        case '1-13-1':
          activedCellParent = this.taskResourceManage
          this.sideNavList.children[8].showSubMenu = false
          break
        case '1-10-5':
          activedCellParent = this.basedataNavList
          this.sideNavList.children[9].showSubMenu = false
          break
        case '1-10-6':
          activedCellParent = this.basedataNavList
          this.sideNavList.children[9].showSubMenu = false
          break
        case '1-10-7':
          activedCellParent = this.basedataNavList
          this.sideNavList.children[9].showSubMenu = false
          break
        case '1-10-4':
          activedCellParent = this.basedataNavList
          this.sideNavList.children[9].showSubMenu = false
          break
        case '1-10-3':
          activedCellParent = this.basedataNavList
          this.sideNavList.children[9].showSubMenu = false
          break
        case '1-10-2':
          activedCellParent = this.basedataNavList
          this.sideNavList.children[9].showSubMenu = false
          break
        case '1-10-1':
          activedCellParent = this.basedataNavList
          this.sideNavList.children[9].showSubMenu = false
          break
        default:
          activedCellParent = this.sideNavList
          break
      }
      // const activedCellParent = this.navListMap[index] || this.sideNavList;
      this.crrentItem = index
      const activedCell = activedCellParent.children.find(
        (item) => item.key === index
      )
      this.breadcrumbFirstName = activedCellParent ? activedCellParent.name : '';
      this.breadcrumbSecondName = activedCell ? activedCell.name : '';
      storage.set('lastActiveConsole', activedCell)
      this.$router.push({
        path: activedCell ? activedCell.path : '',
        query: {
          workspaceId: this.$route.query.workspaceId,
        },
      })
    },
    clickToRoute(index) {
      const activedCellParent = this.getChildMap(index.split('-').slice(0, 2).join('-'))
      this.crrentItem = index
      const activedCell = activedCellParent.children.find(
        (item) => item.key === index
      )
      this.breadcrumbFirstName = activedCellParent.name
      this.breadcrumbSecondName = activedCell.name
      storage.set('lastActiveConsole', activedCell)
      this.$router.push({
        path: activedCell.path,
        query: {
          workspaceId: this.$route.query.workspaceId,
        },
      })
    },
    clickResourceTab(value) {
      this.$router.push(`/console/${value}`)
    },
  },
  async beforeRouteEnter(to, from, next) {
    if (to.name === 'FAQ' && from.name === 'Home') {
      next((vm) => {
        vm.breadcrumbFirstName = this.$t(
          'message.linkis.sideNavList.function.name'
        )
        vm.breadcrumbSecondName = this.$t(
          'message.linkis.sideNavList.function.children.globalValiable'
        )
      })
    } else if (
      (to.name === 'Console' && from.name === 'Home') ||
      (to.name === 'Console' && from.name === 'Project') ||
      (to.name === 'Console' && from.name === 'Workflow') ||
      !from.name
    ) {
      const lastActiveConsole = storage.get('lastActiveConsole')
      // If it is historical details, refresh directly(如果为历史详情则直接刷新)
      if (['viewHistory', 'viewHistoryDetail', 'codeDetail'].includes(to.name)) return next()
      try {
        let isLogAdmin = false;
        await api.fetch('/jobhistory/governanceStationAdmin', 'get').then((res) => {
          isLogAdmin = res.admin
          storage.set('isLogAdmin', res.admin, 'session')
          storage.set('isLogHistoryAdmin', res.historyAdmin, 'session')
          storage.set('isLogDeptAdmin', res.deptAdmin, 'session')
        })

        next((vm) => {
          const noAccess = !isLogAdmin && !['1-1', '1-2', '1-3', '1-4', '1-8-1', '1-9-1', '1-9-2', '1-10-7', '1-11'].includes(lastActiveConsole?.key)
          if (lastActiveConsole && !noAccess ) {
            if (
              lastActiveConsole?.key === '1-9-1' ||
              lastActiveConsole?.key === '1-9-2'
            ) {
              vm.clickToRoute(lastActiveConsole?.key)
            } else {
              vm.handleCellClick(lastActiveConsole?.key)
            }
          } else {
            vm.handleCellClick('1-1')
          }
        })
      } catch(err) {
        window.console.warn(err);
      }


    }
    next()
  },
}
</script>
  <style lang="scss" src="@/apps/linkis/assets/styles/console.scss"></style>
  <style lang="scss" scoped>
.crrentItem {
  color: #338cf0;
}
</style>
  <style lang="scss">
.resource-tab {
  top: -6px;
  .ivu-tabs-bar {
    border-bottom: 0;
  }
}
.sub-menu-row {
  position: absolute;
  top: 5px;
  right: 10px;
}
</style>
