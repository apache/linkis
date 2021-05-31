import { merge } from 'lodash'
import routes, { subAppRoutes } from "./dss/router"
// 根据module参数配置要打包的应用，生成的虚拟模块
const apps = require('dynamic-modules')
// 处理路由，单个应用可以有独立的顶层路由配置layout，header，footer
// length ===1 为单个子应用独立运行，否则为dss，包含多个子应用
let subRoutes = subAppRoutes
const appsRoutes = Object.values(apps.appsRoutes)
if (apps.modules.length === 1) {
  if (appsRoutes[0].subAppRoutes) {
    subRoutes = appsRoutes[0].subAppRoutes
  }
}
/**
 * * 工作流子应用：workflows和scriptis一起打包
 * * npm run build --module=scriptis,workflows --micro_module=workflows
 * * 数据服务子应用：apiServices和workspace一起打包
 * * npm run build --module=apiServices,workspace --micro_module=apiServices
 * ! micro_module参数要和module里值一样，否则找不到
 */
if (apps.microModule) {
  if (apps.appsRoutes[apps.microModule].subAppRoutes) {
    subRoutes = apps.appsRoutes[apps.microModule].subAppRoutes
  }
    
  if (apps.microModule === 'workflows') {
    subRoutes.redirect =  '/process'
  }

}
if (appsRoutes) {
  appsRoutes.forEach(route => {
    if (apps.microModule === 'apiServices' && route.apiServicesRoutes) {
      subRoutes.children = subRoutes.children.concat(route.apiServicesRoutes)
    } else {
      subRoutes.children = subRoutes.children.concat(route.default)
    }
  });
}

routes.unshift(subRoutes)

// 公共国际化
const i18n = {
  'en': require('./common/i18n/en.json'),
  'zh-CN': require('./common/i18n/zh.json')
}

// 处理国际化
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