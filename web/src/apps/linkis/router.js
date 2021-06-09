/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
export const subAppRoutes = {
  path: '',
  name: 'layout',
  component: () => import('./view/layout.vue'),
  redirect: '/console',
  meta: {
    title: 'DataSphere Studio',
    publicPage: true, // 权限公开
  },
  children: []
}

export default [
  {
    path: 'console',
    name: 'Console',
    redirect: '/console/globalHistory',
    meta: {
      title: 'linkis console',
      publicPage: true,
    },
    component: () =>
      import('./view/linkis/index.vue'),
    children: [{
      name: 'globalHistory',
      path: 'globalHistory',
      component: () =>
        import('./module/globalHistoryManagement/index.vue'),
      meta: {
        title: 'Global History',
        publicPage: true,
      },
    }, {
      name: 'viewHistory',
      path: 'viewHistory',
      component: () =>
        import('./module/globalHistoryManagement/viewHistory.vue'),
      meta: {
        title: 'viewHistory',
        publicPage: true,
      },
    }, {
      name: 'resource',
      path: 'resource',
      component: () =>
        import('./module/resourceManagement/index.vue'),
      meta: {
        title: 'resource',
        publicPage: true,
      },
    },
    {
      name: 'setting',
      path: 'setting',
      component: () =>
        import('./module/setting/setting.vue'),
      meta: {
        title: 'setting',
        publicPage: true,
      },
    },{
      name: 'ECM',
      path: 'ECM',
      component: () =>
        import('./module/ECM/index.vue'),
      meta: {
        title: 'ECM',
        publicPage: true,
      },
    },{
      name: 'EngineConnList',
      path: 'EngineConnList',
      component: () =>
        import('./module/ECM/engineConn.vue'),
      meta: {
        title: 'EngineConn',
        publicPage: true,
      },
    }, {
      name: 'globalValiable',
      path: 'globalValiable',
      component: () =>
        import('./module/globalValiable/index.vue'),
      meta: {
        title: 'Global Valiable',
        publicPage: true,
      },
    }, {
      name: 'FAQ',
      path: 'FAQ',
      component: () =>
        import('./module/FAQ/index.vue'),
      meta: {
        title: 'FAQ',
        publicPage: true,
      },
    },
    {
      name: 'microService',
      path: 'microService',
      component: () =>
        import('./module/microServiceManagement/index.vue'),
      meta: {
        title: 'microServiceManagement',
        publicPage: true,
      },
    }
    ],
  },
]
