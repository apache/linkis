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

import storage from '@/common/helper/storage';
import tree from '@/apps/scriptis/service/db/tree.js';
export default {
  data() {
    return {}
  },

  created() {
    // Refreshing the page still needs to clear the cache, because the number structure in scriptis is cached. If it is not clear, the background update will not be able to see the refresh page(刷新页面还是需要清缓存的，因为scriptis里的数结构都是缓存，如果不清，后台更新了刷新页面也看不到)
    this.clearSession();
  },
  mounted() {
    document.addEventListener('copy', this.copyAction, false);
  },
  beforeDestroy() {
    document.removeEventListener('copy', this.copyAction, false);
  },
  methods: {
    copyAction(event) {
      // The clipboardData object in Google Chrome exists in the event event, which is used to obtain the data in the clipboard, which can only be monitored during the copy operation.(谷歌浏览器中的clipboardData对象存在event事件里，用于获取剪贴板中的数据，只有在复制操作过程中才能监听到)
      const string = event.clipboardData.getData('text/plain') || event.target.value || event.target.outerText;
      storage.set('copyString', string);
    },
    clearSession() {
      tree.remove('scriptTree');
      tree.remove('hdfsTree');
      storage.set('shareRootPath', '');
      storage.set('hdfsRootPath', '');
      tree.remove('hiveTree');
      tree.remove('udfTree');
      tree.remove('functionTree');
      storage.set('copyString', '');
      // Refresh basic user information without clearing the login(刷新不用清登录的用户基础信息)
      // storage.set('baseInfo', '');
    },
  }
}
