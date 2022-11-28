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


import VueRouter from "vue-router";
import { routes } from './dynamic-apps'

// Solve the error of repeated click routing jump(解决重复点击路由跳转报错)
const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}
const router = new VueRouter({
  routes
});

router.beforeEach((to, from, next) => {
  if (to.meta) {
    // Add parameters to the route to control the display of the corresponding header(给路由添加参数，控制显示对应header)
    if (to.meta.header) {
      to.query.showHeader = to.meta.header
    }
    if (to.meta.publicPage) {
      // Public pages do not need permission control (404, 500)(公共页面不需要权限控制（404，500）)
      next();
    } else if( to.path != '/workspace') {
      next('/workspace');
    } else {
      next()
    }
  }
});

router.afterEach((to) => {
  if (to.meta) {
    document.title = to.meta.title || 'Linkis';
  }
});

export default router;
