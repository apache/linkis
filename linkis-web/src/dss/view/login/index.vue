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
  <div
    class="login"
    @keyup.enter.stop.prevent="handleSubmit('loginForm')">
    <i class="login-bg"/>
    <div class="login-main">
      <Form
        ref="loginForm"
        :model="loginForm"
        :rules="ruleInline">
        <FormItem>
          <span class="login-title">{{$t('message.common.login.loginTitle')}}</span>
        </FormItem>
        <FormItem prop="user">
          <div class="label">{{$t('message.linkis.userName')}}</div>
          <Input
            v-model="loginForm.user"
            type="text"
            :placeholder="$t('message.common.login.userName')"
            size="large"/>
        </FormItem>
        <FormItem prop="password">
          <div class="label">{{$t('message.linkis.password')}}</div>
          <Input
            v-model="loginForm.password"
            type="password"
            :placeholder="$t('message.common.login.passwordHint')"
            size="large" />
          <Checkbox
            v-model="rememberUserNameAndPass"
            class="remember-user-name"
            style="">{{$t('message.common.login.remenber')}}</Checkbox>
        </FormItem>
        <FormItem>
          <Button
            :loading="loading"
            type="primary"
            long
            size="large"
            @click="handleSubmit('loginForm')">{{$t('message.common.login.login')}}</Button>
        </FormItem>
      </Form>
    </div>
  </div>
</template>
<script>
import api from '@/common/service/api';
import storage from '@/common/helper/storage';
import { db } from '@/common/service/db/index.js';
import { config } from '@/common/config/db.js';
import JSEncrypt from 'jsencrypt';
import tab from '@/apps/scriptis/service/db/tab.js';
export default {
  data() {
    return {
      loading: false,
      loginForm: {
        user: '',
        password: '',
      },
      ruleInline: {
        user: [
          { required: true, message: this.$t('message.common.login.userName'), trigger: 'blur' },
          // {type: 'string', pattern: /^[0-9a-zA-Z\.\-_]{4,16}$/, message: '无效的用户名！', trigger: 'change'},
        ],
        password: [
          { required: true, message: this.$t('message.common.login.password'), trigger: 'blur' },
          // {type: 'string', pattern: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,18}$/, message: '请输入6至12位的密码', trigger: 'change'},
        ],
      },
      rememberUserNameAndPass: false,
      publicKeyData: null
    };
  },
  created() {
    let userNameAndPass = storage.get('saveUserNameAndPass', 'local');
    if (userNameAndPass) {
      this.rememberUserNameAndPass = true;
      this.loginForm.user = userNameAndPass.split('&')[0];
      this.loginForm.password = userNameAndPass.split('&')[1];
    }
    this.getPublicKey();
  },
  mounted() {
  },
  methods: {
    // Get public key interface(获取公钥接口)
    getPublicKey() {
      api.fetch('/user/publicKey', 'get').then((res) => {
        this.publicKeyData = res;
      })
    },
    handleSubmit(name) {
      this.$refs[name].validate(async (valid) => {
        if (valid) {
          this.loading = true;
          if (!this.rememberUserNameAndPass) {
            storage.remove('saveUserNameAndPass', 'local');
          }
          this.loginForm.user = this.loginForm.user.toLocaleLowerCase();
          // Need to determine whether the password needs to be encrypted(需要判断是否需要给密码加密)
          let password = this.loginForm.password;
          let params = {};
          if (this.publicKeyData && this.publicKeyData.enableLoginEncrypt) {
            const key = `-----BEGIN PUBLIC KEY-----${this.publicKeyData.publicKey}-----END PUBLIC KEY-----`;
            const encryptor = new JSEncrypt()
            encryptor.setPublicKey(key)
            password = encryptor.encrypt(this.loginForm.password);
            params = {
              userName: this.loginForm.user,
              password
            };
          } else {
            params = {
              userName: this.loginForm.user,
              password
            };
          }
          // Log in to clear the local cache(登录清掉本地缓存)
          // After logging out twice in a row, data will be lost, so it is necessary to judge whether it has been saved and not used.(连续两次退出登录后，会导致数据丢失，所以得判断是否已存切没有使用)
          let tabs = await tab.get() || [];
          const tablist = storage.get(this.loginForm.user + 'tabs', 'local');
          if (!tablist || tablist.length <= 0) {
            storage.set(this.loginForm.user + 'tabs', tabs, 'local');
          }
          Object.keys(config.stores).map((key) => {
            db.db[key].clear();
          })
          api
            .fetch(`/user/login`, params)
            .then((rst) => {
              this.loading = false;
              storage.set('userName',rst.userName,'session')
              storage.set('enableWatermark',rst.enableWatermark ? true : false,'session')
              // save username(保存用户名)
              if (this.rememberUserNameAndPass) {
                storage.set('saveUserNameAndPass', `${this.loginForm.user}&${this.loginForm.password}`, 'local');
              }
              if (rst) {
                this.userName = rst.userName;
                this.$router.push({path: '/console'});
                this.$Message.success(this.$t('message.common.login.loginSuccess'));
              }
            })
            .catch((err) => {
              if (this.rememberUserNameAndPass) {
                storage.set('saveUserNameAndPass', `${this.loginForm.user}&${this.loginForm.password}`, 'local');
              }
              if (err.message.indexOf('已经登录，请先退出再进行登录') !== -1) {
                this.getPageHomeUrl().then(() => {
                  this.$router.push({path: '/'});
                })
              }
              this.loading = false;
            });
        } else {
          this.$Message.error(this.$t('message.common.login.vaildFaild'));
        }
      });
    },
    // clear local cache(清楚本地缓存)
    clearSession() {
      storage.clear();
    },
  },
};
</script>
<style lang="scss" src="@/dss/assets/styles/login.scss"></style>
