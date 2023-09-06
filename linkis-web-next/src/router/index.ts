import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';

// 设置路由规则
const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'layout',
        redirect: '/console/globalHistoryManagement',
        component: () => import('@/layout.vue'),
        meta: {
            title: 'Linkis',
            publicPage: true, // Permission disclosure(权限公开)
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
                    title: 'resource',
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
        ],
    },
];

// 设置路由
const router = createRouter({
    routes,
    history: createWebHashHistory(),
});

// 导出路由
export default router;
