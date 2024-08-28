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

import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
import dssRouter from '@/dss/dssRouter';

// 设置路由规则
const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'root',
        redirect: '/console/globalHistoryManagement',
        meta: {
            title: 'Linkis',
            publicPage: true, // Permission disclosure(权限公开)
        },
        children: [
            {
                path: '/console',
                name: 'layout',
                redirect: '/console/globalHistoryManagement',
                component: () => import('@/layout.vue'),
                meta: {
                    title: 'Console',
                    publicPage: true,
                },
                children: [
                    {
                        name: 'globalHistoryManagement',
                        path: '/console/globalHistoryManagement',
                        component: () =>
                            import('@/pages/globalHistoryManagement/index.vue'),
                        meta: {
                            title: 'Global History Management',
                            publicPage: true,
                        },
                    },
                    {
                        name: 'resource',
                        path: '/console/resource',
                        redirect: '/console/resource/manage',
                        component: () => import('@/pages/resource/index.vue'),
                        meta: {
                            title: 'Resource',
                            publicPage: true,
                        },
                        children: [
                            {
                                name: 'history',
                                path: '/console/resource/history',
                                component: () =>
                                    import('@/pages/resource/history/index.vue'),
                                meta: {
                                    title: 'Resource History',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'manage',
                                path: '/console/resource/manage',
                                component: () =>
                                    import('@/pages/resource/manage/index.vue'),
                                meta: {
                                    title: 'Resource Manage',
                                    publicPage: true,
                                },
                            },
                        ],
                    },
                    {
                        name: 'parameterConfig',
                        path: '/console/parameterConfig',
                        component: () => import('@/pages/parameterConfig/index.vue'),
                        meta: {
                            title: 'Parameter Config',
                            publicPage: true,
                        },
                        children: [],
                    },
                    {
                        name: 'globalVariables',
                        path: '/console/globalVariables',
                        component: () => import('@/pages/globalVariables/index.vue'),
                        meta: {
                            title: 'Global Variables',
                            publicPage: true,
                        },
                        children: [],
                    },
                    {
                        name: 'microServiceManagement',
                        path: '/console/microServiceManagement',
                        component: () => import('@/pages/microServiceManagement/index.vue'),
                        meta: {
                            title: 'Micro Service Management',
                            publicPage: true,
                        },
                        children: [],
                    },
                ]
            },
        ],
    },
    ...dssRouter,
];

// 设置路由
const router = createRouter({
    routes,
    history: createWebHashHistory(),
});

// 导出路由
export default router;
