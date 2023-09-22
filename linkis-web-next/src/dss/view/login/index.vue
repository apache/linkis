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
  <div class="login">
    <i class="login-bg" />
    <div class="login-main">
      <Form ref="loginForm" :model="loginForm" :rules="ruleInline">
        <FormItem>
          <span class="login-title">
            {{ $t('message.common.login.loginTitle') }}
          </span>
        </FormItem>
        <FormItem prop="user">
          <div class="label">{{ $t('message.linkis.userName') }}</div>
          <FInput class="login-input" v-model="loginForm.user" type="text"
            :placeholder="$t('message.common.login.userName')" size="large" />
        </FormItem>
        <FormItem prop="password">
          <div class="label password">{{ $t('message.linkis.password') }}</div>
          <FInput class="login-input" v-model="loginForm.password" type="password"
            :placeholder="$t('message.common.login.passwordHint')" size="large" />
          <FCheckbox v-model="rememberUserNameAndPass" class="remember-user-name">
            {{ $t('message.common.login.remenber') }}
          </FCheckbox>
        </FormItem>
        <FormItem>
          <FButton class="login-btn" :loading="loading" type="primary" long size="large">
            {{ $t('message.common.login.login') }}
          </FButton>
        </FormItem>
      </Form>
    </div>
  </div>
</template>
<script lang="ts">
import api from '@/service/api';
import { FMessage } from '@fesjs/fes-design';
import { defineComponent } from 'vue';
import storage from '@/helper/storage'

export default defineComponent({
  setup() {
    return {
      loading: false,
      loginForm: {
        user: '',
        password: ''
      },
      ruleInline: {
        user: [
          {
            required: true,
            message: 'message.common.login.userName',
            trigger: 'blur'
          }
          // {type: 'string', pattern: /^[0-9a-zA-Z\.\-_]{4,16}$/, message: '无效的用户名！', trigger: 'change'},
        ],
        password: [
          {
            required: true,
            message: 'message.common.login.password',
            trigger: 'blur'
          }
          // {type: 'string', pattern: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,18}$/, message: '请输入6至12位的密码', trigger: 'change'},
        ]
      },
      rememberUserNameAndPass: false,
      publicKeyData: null
    }

  },
  created() {
    let userNameAndPass = null
    storage.get('saveUserNameAndPass', 'local')
    if (userNameAndPass) {
      this.rememberUserNameAndPass = true
      this.loginForm.user = userNameAndPass.split('&')[0]
      this.loginForm.password = userNameAndPass.split('&')[1]
    }
    // getPublicKey()
  },
  methods: {
    // Get public key interface(获取公钥接口)
    getPublicKey() {
      api.fetch('/user/publicKey', 'get').then((res: any) => {
        this.publicKeyData = res
      })
    },
    handleSubmit(name: string) {
      this.$refs[name].validate(async (valid: boolean) => {
        if (valid) {
          this.loading = true
          if (!this.rememberUserNameAndPass) {
            storage.remove('saveUserNameAndPass', 'local')
          }
          this.loginForm.user = this.loginForm.user.toLocaleLowerCase()
          // Need to determine whether the password needs to be encrypted(需要判断是否需要给密码加密)
          let password = this.loginForm.password
          let params = {}
          if (this.publicKeyData && this.publicKeyData.enableLoginEncrypt) {
            const key = `-----BEGIN PUBLIC KEY-----${this.publicKeyData.publicKey}-----END PUBLIC KEY-----`
            const encryptor = new JSEncrypt()
            encryptor.setPublicKey(key)
            password = encryptor.encrypt(this.loginForm.password)
            params = {
              userName: this.loginForm.user,
              password
            }
          } else {
            params = {
              userName: this.loginForm.user,
              password
            }
          }
          // Log in to clear the local cache(登录清掉本地缓存)
          // After logging out twice in a row, data will be lost, so it is necessary to judge whether it has been saved and not used.(连续两次退出登录后，会导致数据丢失，所以得判断是否已存切没有使用)
          let tabs = (await tab.get()) || []
          const tablist = storage.get(this.loginForm.user + 'tabs', 'local')
          if (!tablist || tablist.length <= 0) {
            storage.set(this.loginForm.user + 'tabs', tabs, 'local')
          }
          Object.keys(config.stores).map((key) => {
            db.db[key].clear()
          })
          api
            .fetch(`/user/login`, params)
            .then((rst) => {
              this.loading = false
              storage.set('userName', rst.userName, 'session')
              storage.set(
                'enableWatermark',
                rst.enableWatermark ? true : false,
                'session'
              )
              // save username(保存用户名)
              if (this.rememberUserNameAndPass) {
                // storage.set(
                //   'saveUserNameAndPass',
                //   `${this.loginForm.user}&${this.loginForm.password}`,
                //   'local'
                // )
              }
              if (rst) {
                // this.userName = rst.userName
                this.$router.push({ path: '/console' })
                FMessage.success(
                  this.$t('message.common.login.loginSuccess')
                )
              }
            })
            .catch((err) => {
              if (this.rememberUserNameAndPass) {
                storage.set(
                  'saveUserNameAndPass',
                  `${this.loginForm.user}&${this.loginForm.password}`,
                  'local'
                )
              }
              if (err.message.indexOf('已经登录，请先退出再进行登录') !== -1) {
                this.getPageHomeUrl().then(() => {
                  this.$router.push({ path: '/' })
                })
              }
              this.loading = false
            })
        } else {
          FMessage.error(this.$t('message.common.login.vaildFaild'))
        }
      })
    },
    // clear local cache(清楚本地缓存)
    clearSession() {
      storage.clear()
    }
  }
})

</script>
<style scoped>
@import '../../../style/variable.less';

.login {
  height: 100%;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: #001C40;
  display: flex;
  align-items: center;
  justify-content: flex-end;

  .login-bg {
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    background: url('@/dss/assets/images/loginbgc.svg') no-repeat;
    background-position: 55% 40%;
    background-color: #001C40;
  }

  .login-main {
    position: relative;
    width: 450px;
    height: 427px;
    background-color: #fff;
    margin-right: 9.5%;
    padding: 50px;
    border-radius: 6px;
    box-shadow: 2px 2px 40px 0px rgba(0, 0, 0, 0.9);
    z-index: 10;

    .login-title {
      font-size: 20px;
      font-weight: 400px;
      display: block;
      text-align: center;
      color: #044B93;
      font-weight: 600;
    }

    .label {
      font-size: 14px;
      line-height: 32px;
      margin-top: 20px;
    }

    .login-input {
      height: 50px;
      border-radius: 4px;
      padding-left: 15px;
      color: #515a6e;
      border: 1px solid #DEE4EC;

    }

    .login-btn {
      position: absolute;
      bottom: 50px;
      left: 50px;
      width: 357px;
    }

    :deep(span.fes-input-inner) {
      display: block;
      height: 48px;
      line-height: 48px;
      width: 100%;
      padding: auto 6px;
    }

    :deep(.fes-input.login-input) {
      padding: 0;
    }

    .remember-user-name {
      margin: 5px 0 0 10px;
    }

    .ivu-form-item {
      margin-bottom: 20px;
    }
  }

  .radioGroup {
    padding-left: 20px;
    display: block;

    .ivu-radio-group-item {
      width: 100%;
    }
  }
}
</style>
