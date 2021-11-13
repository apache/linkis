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
 

import Vue from 'vue'
import iView from 'iview'
import VueRouter from 'vue-router'
import { apps } from './dynamic-apps'
import component from './components'
import App from './dss/view/app.vue'
import router from './router'
import i18n from './common/i18n'
import mixinDispatch from './common/service/moduleMixin'

import API_PATH from './common/config/apiPath.js'
import 'iview/dist/styles/iview.css'

// Icon
import './components/svgIcon/index.js'
import './dss/module/index.js'

// moduleMixin
if (apps.requireComponent) {
  apps.requireComponent.forEach(item=>{
    mixinDispatch(item)
  })
}
if (apps.requireComponentVue) {
  apps.requireComponentVue.forEach(item=>{
    mixinDispatch(undefined, item)
  })
}

Vue.use(VueRouter)
Vue.use(component)
Vue.use(iView, {
  i18n: (key, value) => i18n.t(key, value)
})

Vue.config.productionTip = false
Vue.prototype.$Message.config({
  duration: 3
})
// 全局变量
Vue.prototype.$API_PATH = API_PATH;

new Vue({
  router,
  i18n,
  render: (h) => h(App)
}).$mount('#app')
console.log(`当前环境:${process.env.NODE_ENV}`)
