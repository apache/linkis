/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import Vue from 'vue';
import VueI18n from 'vue-i18n'
import en from 'iview/dist/locale/en-US'
import zh from 'iview/dist/locale/zh-CN'
import { i18n } from '../../dynamic-apps'
Vue.use(VueI18n);
// 先判断是否有设置语言，没有就用本地语言
if (localStorage.getItem('locale')) {
  Vue.config.lang = localStorage.getItem('locale');
} else {
  const lang = navigator.language;
  if (lang === 'zh-CN') {
    Vue.config.lang = 'zh-CN';
    localStorage.setItem('locale', 'zh-CN');
  } else {
    Vue.config.lang = 'en';
    localStorage.setItem('locale', 'en');
  }
}
Vue.locale = () => {};
const messages = {
  'en': Object.assign(en, i18n.en),
  'zh-CN': Object.assign(zh, i18n['zh-CN'])
}

export default new VueI18n({
  locale: Vue.config.lang,
  messages
});
