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

import { merge } from 'lodash'
import routes, { subAppRoutes } from "./dss/router"
// Configure the application to be packaged according to the module parameter, and the generated virtual module(根据module参数配置要打包的应用，生成的虚拟模块)
const apps = require('dynamic-modules')
// Handling routing, a single application can have independent top-level routing configuration layout, header, footer(处理路由，单个应用可以有独立的顶层路由配置layout，header，footer)
// length ===1 runs independently for a single sub-app, otherwise dss, including multiple sub-apps(length ===1 为单个子应用独立运行，否则为dss，包含多个子应用)
let subRoutes = subAppRoutes
const appsRoutes = Object.values(apps.appsRoutes)

/**
 * * Workflow sub-application: workflows are packaged with scripts(工作流子应用：workflows和scriptis一起打包)
 * * npm run build --module=scriptis,workflows --micro_module=workflows
 * * Data service sub-application: apiServices and workspace are packaged together(数据服务子应用：apiServices和workspace一起打包)
 * * npm run build --module=apiServices,workspace --micro_module=apiServices
 * ! The micro_module parameter must be the same as the value in the module, otherwise it cannot be found(micro_module参数要和module里值一样，否则找不到)
 */
if (apps.microModule) {
  if (apps.appsRoutes[apps.microModule].subAppRoutes) {
    subRoutes = apps.appsRoutes[apps.microModule].subAppRoutes
  }

}
if (appsRoutes) {
  appsRoutes.forEach(route => {
    subRoutes.children = subRoutes.children.concat(route.default)
  });
}

routes.unshift(subRoutes)

console.log(routes)
// public internationalization(公共国际化)
const i18n = {
  'en': require('./common/i18n/en.json'),
  'zh-CN': require('./common/i18n/zh.json')
}

// handle internationalization(处理国际化)
if (apps.appsI18n) {
  apps.appsI18n.forEach(item => {
    merge(i18n, item)
  });
}

export {
  routes,
  i18n,
  apps
}
