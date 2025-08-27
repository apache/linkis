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
                        redirect: '/console/resource/history',
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
                        name: 'ECMManagement',
                        path: '/console/ECMManagement',
                        component: () => import('@/pages/ECMManagement/index.vue'),
                        meta: {
                            title: 'ECMManagement',
                            publicPage: true,
                        },
                        children: [],
                    },
                    {
                        name: 'Engines',
                        path: '/console/ECMManagement/engines/:instance',
                        component: () => import('@/pages/ECMManagement/engine.vue'),
                        meta: {
                            title: 'ECM Engines',
                            publicPage: true,
                        },
                    },
                    {
                        name: 'microServiceManagement',
                        path: '/console/microServiceManagement',
                        component: () => import('@/pages/microServiceManagement/index.vue'),
                        meta: {
                            title: 'Microservice Management',
                            publicPage: true,
                        },
                        children: [],
                    },
                    {
                        name: 'dataSourceManage',
                        path: '/console/dataSourceManage',
                        redirect: '/console/dataSourceManage/dataSourceManagement',
                        meta: {
                            title: 'Data Source Manage',
                            publicPage: true,
                        },
                        children: [
                            {
                                name: 'dataSourceManagement',
                                path: '/console/dataSourceManage/dataSourceManagement',
                                component: () =>
                                    import('@/pages/dataSourceManage/dataSourceManagement/index.vue'),
                                meta: {
                                    title: 'Data Source Management',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'datasourceEnv',
                                path: '/console/dataSourceManage/datasourceEnv',
                                component: () =>
                                    import('@/pages/dataSourceManage/datasourceEnv/index.vue'),
                                meta: {
                                    title: 'Data Source Env',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'datasourceType',
                                path: '/console/dataSourceManage/datasourceType',
                                component: () =>
                                    import('@/pages/dataSourceManage/datasourceType/index.vue'),
                                meta: {
                                    title: 'Data Source Type',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'datasourceAccess',
                                path: '/console/dataSourceManage/datasourceAccess',
                                component: () =>
                                    import('@/pages/dataSourceManage/datasourceAccess/index.vue'),
                                meta: {
                                    title: 'Data Source Access',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'datasourceTypeKey',
                                path: '/console/dataSourceManage/datasourceTypeKey',
                                component: () =>
                                    import('@/pages/dataSourceManage/datasourceTypeKey/index.vue'),
                                meta: {
                                    title: 'Data Source Type Key',
                                    publicPage: true,
                                },
                            },
                        ],
                    },
                    {
                        name: 'udfFunctionTitle',
                        path: '/console/udfFunctionTitle',
                        redirect: '/console/udfFunctionTitle/udfFunctionManage',
                        meta: {
                            title: 'UDF Function',
                            publicPage: true,
                        },
                        children: [
                            {
                                name: 'udfFunctionManage',
                                path: '/console/udfFunctionTitle/udfFunctionManage',
                                component: () =>
                                    import('@/pages/udfFunctionTitle/udfFunctionManage/index.vue'),
                                meta: {
                                    title: 'UDF Management',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'functionManagement',
                                path: '/console/udfFunctionTitle/functionManagement',
                                component: () =>
                                    import('@/pages/udfFunctionTitle/functionManagement/index.vue'),
                                meta: {
                                    title: 'Function Management',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'udfManager',
                                path: '/console/udfFunctionTitle/udfManager',
                                component: () =>
                                    import('@/pages/udfFunctionTitle/udfManager/index.vue'),
                                meta: {
                                    title: 'UDF Manager',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'udfTree',
                                path: '/console/udfFunctionTitle/udfTree',
                                component: () =>
                                    import('@/pages/udfFunctionTitle/udfTree/index.vue'),
                                meta: {
                                    title: 'UDF Tree',
                                    publicPage: true,
                                },
                            },
                        ],
                    },
                    {
                        name: 'basedataManagement',
                        path: '/console/basedataManagement',
                        redirect: '/console/basedataManagement/gatewayAuthToken',
                        meta: {
                            title: 'Base Data Management',
                            publicPage: true,
                        },
                        children: [
                            {
                                name: 'gatewayAuthToken',
                                path: '/console/basedataManagement/gatewayAuthToken',
                                component: () =>
                                    import('@/pages/basedataManagement/gatewayAuthToken/index.vue'),
                                meta: {
                                    title: 'Gateway Auth Token',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'errorCode',
                                path: '/console/basedataManagement/errorCode',
                                component: () =>
                                    import('@/pages/basedataManagement/errorCode/index.vue'),
                                meta: {
                                    title: 'Error Code',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'rmExternalResourceProvider',
                                path: '/console/basedataManagement/rmExternalResourceProvider',
                                component: () =>
                                    import('@/pages/basedataManagement/rmExternalResourceProvider/index.vue'),
                                meta: {
                                    title: 'Rm External Resource Provider',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'EnginePluginManagement',
                                path: '/console/basedataManagement/EnginePluginManagement',
                                component: () =>
                                    import('@/pages/basedataManagement/EnginePluginManagement/index.vue'),
                                meta: {
                                    title: 'Engine Plugin Management',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'tenantTagManagement',
                                path: '/console/basedataManagement/tenantTagManagement',
                                component: () =>
                                    import('@/pages/basedataManagement/tenantTagManagement/index.vue'),
                                meta: {
                                    title: 'Tenant Tag Management',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'ipListManagement',
                                path: '/console/basedataManagement/ipListManagement',
                                component: () =>
                                    import('@/pages/basedataManagement/ipListManagement/index.vue'),
                                meta: {
                                    title: 'Ip List Management',
                                    publicPage: true,
                                },
                            },
                            {
                                name: 'configManagement',
                                path: '/console/basedataManagement/configManagement',
                                component: () =>
                                    import('@/pages/basedataManagement/configManagement/index.vue'),
                                meta: {
                                    title: 'Config Management',
                                    publicPage: true,
                                },
                            },
                        ],
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
