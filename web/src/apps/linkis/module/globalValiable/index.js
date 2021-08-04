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

import api from '@/common/service/api';
export default {
  name: 'GlobalValiable',
  events: ['getGlobalVariable'],
  dispatchs: {
    IndexedDB: ['updateGlobalCache'],
  },
  methods: {
    getGlobalVariable() {
      return new Promise((resolve, reject) => {
        api.fetch('/variable/listGlobalVariable', 'get').then((res) => {
          resolve(res.globalVariables);
        }).catch(() => {
          reject('未获取到全局变量信息，脚本联想词功能可能存在异常！可刷新重试！');
        });
      });
    },
  },
  component: () =>
    import ('./index.vue'),
};
