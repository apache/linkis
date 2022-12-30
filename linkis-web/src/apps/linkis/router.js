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

export const subAppRoutes = {
  path: '',
  name: 'layout',
  component: () => import('./view/layout.vue'),
  redirect: '/console',
  meta: {
    title: 'Linkis',
    publicPage: true, // Permission disclosure(权限公开)
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
    }, {
      name: 'resourceEngineConnList',
      path: 'resourceEngineConnList',
      component: () =>
        import('./module/resourceManagement/engineConn.vue'),
      meta: {
        title: '历史引擎信息',
        publicPage: true,
      },
    }, {
      name: 'setting',
      path: 'setting',
      component: () =>
        import('./module/setting/setting.vue'),
      meta: {
        title: 'setting',
        publicPage: true,
      },
    }, {
      name: 'ECM',
      path: 'ECM',
      component: () =>
        import('./module/ECM/index.vue'),
      meta: {
        title: 'ECM',
        publicPage: true,
      },
    }, {
      name: 'UdfManagement',
      path: 'urm/udfManagement',
      component: () =>
        import('../URM/module/udfManagement/index.vue'),
      meta: {
        title: 'UdfManagement',
        publicPage: true,
      },
    }, {
      name: 'FunctionManagement',
      path: 'urm/functionManagement',
      component: () =>
        import('../URM/module/functionManagement/index.vue'),
      meta: {
        title: 'FunctionManagement',
        publicPage: true,
      },
    }, {
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
    },
    {
      name: 'datasource',
      path: 'datasource',
      component: () =>
        import('./module/datasource/index.vue'),
      meta: {
        title: 'datasourceManagement',
        publicPage: true,
      },
    },
    {
      name: 'tenantTagManagement',
      path: 'tenantTagManagement',
      component: () =>
        import('./module/tenantTagManagement/index.vue'),
      meta: {
        title: 'tenantTagManagement',
        publicPage: true,
      }
    },
    {
      name: 'errorCode',
      path: 'errorCode',
      component: () =>
        import('./module/errorCode/index.vue'),
      meta: {
        title: 'errorCode',
        publicPage: true,
      },
    },
    {
      name: 'ipListManagement',
      path: 'ipListManagement',
      component: () =>
        import('./module/ipListManagement/index.vue'),
      meta: {
        title: 'ipListManagement',
        publicPage: true,
      },
    },
    {
      name: 'gatewayAuthToken',
      path: 'gatewayAuthToken',
      component: () =>
        import('./module/gatewayAuthToken/index.vue'),
      meta: {
        title: 'gatewayAuthToken',
        publicPage: true,
      },
    },
    {
      name: 'rmExternalResourceProvider',
      path: 'rmExternalResourceProvider',
      component: () =>
        import('./module/rmExternalResourceProvider/index.vue'),
      meta: {
        title: 'rmExternalResourceProvider',
        publicPage: true,
      },
    },
    {
      name: 'udfManager',
      path: 'udfManager',
      component: () =>
        import('./module/udfManager/index.vue'),
      meta: {
        title: 'udfManager',
        publicPage: true,
      },
    },
    {
      name: 'udfTree',
      path: 'udfTree',
      component: () =>
        import('./module/udfTree/index.vue'),
      meta: {
        title: 'udfTree',
        publicPage: true,
      },
    },
    {
      name: 'datasourceAccess',
      path: 'datasourceAccess',
      component: () =>
        import('./module/datasourceAccess/index.vue'),
      meta: {
        title: 'datasourceAccess',
        publicPage: true,
      },
    },
    {
      name: 'datasourceEnv',
      path: 'datasourceEnv',
      component: () =>
        import('./module/datasourceEnv/index.vue'),
      meta: {
        title: 'datasourceEnv',
        publicPage: true,
      },
    },
    {
      name: 'datasourceType',
      path: 'datasourceType',
      component: () =>
        import('./module/datasourceType/index.vue'),
      meta: {
        title: 'datasourceType',
        publicPage: true,
      }
    },
    {
      name: 'datasourceTypeKey',
      path: 'datasourceTypeKey',
      component: () =>
        import('./module/datasourceTypeKey/index.vue'),
      meta: {
        title: 'datasourceTypeKey',
        publicPage: true,
      }
    },
    {
      name: 'EnginePluginManagement',
      path: 'EnginePluginManagement',
      component: () =>
        import('./module/EnginePluginManagement/index.vue'),
      meta: {
        title: 'EnginePluginManagement',
        publicPage: true,
      },
    }
    ],
  },
]
