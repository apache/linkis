<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<template>
  <div></div>
</template>
<script>
import api from '@/common/service/api';
import storage from '@/common/helper/storage';
export default {
  data() {
    return {};
  },
  created() {
    if (this.$route.query.code) {
      api.fetch('/user/oauth-login', { code: this.$route.query.code }, 'post').then((res) => {
        if (res) {
          this.userName = res.userName;
          storage.set('userName', res.userName, 'session')
          storage.set('enableWatermark', res.enableWatermark ? true : false, 'session')
          this.$router.push({ path: '/console' });
          this.$Message.success(this.$t('message.common.login.loginSuccess'));
        }
      }).catch((err) => {
        if (err.message.indexOf('已经登录，请先退出再进行登录') !== -1) {
          this.getPageHomeUrl().then(() => {
            this.$router.push({ path: '/' });
          })
        } else {
          this.$Message.error(this.$t('message.common.login.vaildFaild'));
          this.$router.push({ path: '/login' })
        }
      });
    }
  },
  mounted() {
  },
  methods: {
  },
};
</script>
