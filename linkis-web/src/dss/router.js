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
  // redirect: '/newHome',
  // Redirects to the management console page(重定向到 管理台页面)
  redirect: '/console',
  meta: {
    title: 'Linkis',
    publicPage: true, // Permission disclosure(权限公开)
  },
  children: []
}

export default [
  // log view(日志查看)
  {
    path: '/log',
    name: 'log',
    meta: {
      title: 'Log',
      publicPage: true,
    },
    component: () =>
      import('./view/logPage/index.vue')
  },
  {
    path: 'commonIframe',
    name: 'commonIframe',
    meta: {
      title: 'DSS Component',
      publicPage: true,
    },
    component: () =>
      import('./view/commonIframe/index.vue'),
  },
  {
    path: '/login',
    name: 'login',
    meta: {
      title: 'Login',
      publicPage: true,
    },
    component: () =>
      import('./view/login/index.vue'),
  },
  // Public pages, not subject to permission control(公用页面，不受权限控制)
  {
    path: '/500',
    name: 'serverErrorPage',
    meta: {
      title: '服务器错误',
      publicPage: true,
    },
    component: () =>
      import('./view/500.vue'),
  },
  {
    path: '/404',
    name: 'pageNotFound',
    meta: {
      title: '404',
      publicPage: true,
    },
    component: () =>
      import('./view/404.vue'),
  },
  {
    path: '/403',
    name: 'pageForbidden',
    meta: {
      title: '403',
      publicPage: true,
    },
    component: () =>
      import('./view/403.vue'),
  },
  // svg available icon preview(svg可用图标预览)
  {
    path: '/icon',
    name: 'icon',
    meta: {
      title: 'icon',
      publicPage: true,
    },
    component: () =>
      import('./view/icon.vue'),
  },
  {
    path: '*',
    meta: {
      title: 'Linkis',
      publicPage: true,
    },
    component: () =>
      import('./view/404.vue'),
  }
]
