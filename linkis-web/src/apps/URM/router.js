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
  redirect: '/urm',
  meta: {
    title: 'Linkis',
    publicPage: true, // Permission disclosure(权限公开)
  },
  children: []
}

export default [
  {
    path: 'urm',
    name: 'URM',
    redirect: '/urm/udfManagement',
    component: () => import('./view/urm/index.vue'),
    meta: {
      title: 'linkis',
      publicPage: true,
    },
    children: [{
      name: 'udfManagement',
      path: 'udfManagement',
      component: () =>
        import('./module/udfManagement/index.vue'),
      meta: {
        title: 'udfManagement',
        publicPage: true,
      },
    },
    {
      name: 'functionManagement',
      path: 'functionManagement',
      component: () =>
        import('./module/functionManagement/index.vue'),
      meta: {
        title: 'functionManagement',
        publicPage: true,
      },
    }]
  }
]
