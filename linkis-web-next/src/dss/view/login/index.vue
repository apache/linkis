<!-- eslint-disable array-callback-return -->
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
            <!-- :rules="ruleInline" -->
            <FForm ref="loginFormRef" :model="loginForm" labelPosition="top">
                <span class="login-title">
                    {{ $t('message.common.login.loginTitle') }}
                </span>

                <!-- :rules="[
                        {
                            required: true,
                            message: $t('message.common.login.userName'),
                        },
                    ]" -->
                <FFormItem prop="user">
                    <template #label>
                        <div class="label">
                            {{ $t('message.linkis.userName') }}
                        </div>
                    </template>
                    <FInput
                        class="login-input"
                        v-model="loginForm.user"
                        type="text"
                        :placeholder="$t('message.common.login.userName')"
                        size="large"
                    />
                </FFormItem>
                <!-- :rules="[
                        {
                            required: true,
                            message: $t('message.common.login.passwordHint'),
                        },
                    ]" -->
                <FFormItem prop="password">
                    <template #label>
                        <div class="label password">
                            {{ $t('message.linkis.password') }}
                        </div>
                    </template>
                    <FInput
                        class="login-input"
                        v-model="loginForm.password"
                        type="password"
                        :placeholder="$t('message.common.login.passwordHint')"
                        size="large"
                    />
                </FFormItem>
                <FCheckbox
                    v-model="rememberUserNameAndPass"
                    class="remember-user-name"
                >
                    {{ $t('message.common.login.remenber') }}
                </FCheckbox>

                <FButton
                    @click="handleSubmit('loginForm')"
                    class="login-btn"
                    :loading="loading"
                    type="primary"
                    long
                    size="large"
                >
                    {{ $t('message.common.login.login') }}
                </FButton>
            </FForm>
        </div>
    </div>
</template>
<script lang="ts">
import { FMessage } from '@fesjs/fes-design';
import { defineComponent, ref } from 'vue';
import JSEncrypt from 'jsencrypt';
import api from '@/service/api';
import storage from '@/helper/storage';
import tab from '@/scriptis/service/db/tab';
import { db } from '@/service/db';
import { config } from '@/config/db';

export default defineComponent({
    setup() {
        const loginFormRef = ref(null);

        return {
            userName: '',
            loginFormRef,
            loading: false,
            loginForm: {
                user: '',
                password: '',
            },
            ruleInline: {
                user: [
                    {
                        required: true,
                        message: 'message.common.login.userName',
                        trigger: 'blur',
                    },
                    // {type: 'string', pattern: /^[0-9a-zA-Z\.\-_]{4,16}$/, message: '无效的用户名！', trigger: 'change'},
                ],
                password: [
                    {
                        required: true,
                        message: 'message.common.login.password',
                        trigger: 'blur',
                    },
                    // {type: 'string', pattern: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,18}$/, message: '请输入6至12位的密码', trigger: 'change'},
                ],
            },
            rememberUserNameAndPass: false,
            publicKeyData: {
                enableLoginEncrypt: null,
                publicKey: null,
            },
        };
    },
    created() {
        const userNameAndPass = storage.get('saveUserNameAndPass', 'local');
        if (userNameAndPass) {
            this.rememberUserNameAndPass = true;
            [this.loginForm.user, this.loginForm.password] =
                userNameAndPass.split('&');
        }
        this.getPublicKey();
    },
    methods: {
        // Get public key interface(获取公钥接口)
        getPublicKey() {
            api.fetch('/user/publicKey', 'get').then((res: any) => {
                this.publicKeyData = res;
            });
        },
        handleSubmit(name: string) {
            console.log(this.loginForm.user, name);
            // this.loginFormRef.validate((valid: boolean) => {
            // if (valid) {
            this.loading = true;
            if (!this.rememberUserNameAndPass) {
                storage.remove('saveUserNameAndPass', 'local');
            }
            this.loginForm.user = this.loginForm.user.toLocaleLowerCase();
            // Need to determine whether the password needs to be encrypted(需要判断是否需要给密码加密)
            // eslint-disable-next-line prefer-destructuring
            let password = this.loginForm.password;
            let params = {};
            if (this.publicKeyData && this.publicKeyData.enableLoginEncrypt) {
                const key = `-----BEGIN PUBLIC KEY-----${this.publicKeyData.publicKey}-----END PUBLIC KEY-----`;
                const encryptor = new JSEncrypt();
                encryptor.setPublicKey(key);
                password = encryptor.encrypt(this.loginForm.password) as string;
                params = {
                    userName: this.loginForm.user,
                    password,
                };
            } else {
                params = {
                    userName: this.loginForm.user,
                    password,
                };
            }
            // Log in to clear the local cache(登录清掉本地缓存)
            // After logging out twice in a row, data will be lost, so it is necessary to judge whether it has been saved and not used.(连续两次退出登录后，会导致数据丢失，所以得判断是否已存切没有使用)
            tab.get().then((tabs: any) => {
                const tablist = storage.get(
                    `${this.loginForm.user}tabs`,
                    'local',
                );
                if (!tablist || tablist.length <= 0) {
                    storage.set(`${this.loginForm.user}tabs`, tabs, 'local');
                }
                Object.keys(config.stores).map((key) =>
                    (db as any).db?.[key]?.clear(),
                );
                api.fetch('/user/login', params)
                    .then((rst: any) => {
                        this.loading = false;
                        storage.set('userName', rst.userName, 'session');
                        storage.set(
                            'enableWatermark',
                            rst.enableWatermark,
                            'session',
                        );
                        // save username(保存用户名)
                        if (this.rememberUserNameAndPass) {
                            storage.set(
                                'saveUserNameAndPass',
                                `${this.loginForm.user}&${this.loginForm.password}`,
                                'local',
                            );
                        }
                        if (rst) {
                            this.userName = rst.userName;
                            this.$router.push({ path: '/' });
                            FMessage.success(
                                this.$t('message.common.login.loginSuccess'),
                            );
                        }
                    })
                    .catch((err: any) => {
                        if (this.rememberUserNameAndPass) {
                            storage.set(
                                'saveUserNameAndPass',
                                `${this.loginForm.user}&${this.loginForm.password}`,
                                'local',
                            );
                        }
                        if (
                            err.message.indexOf(
                                '已经登录，请先退出再进行登录',
                            ) !== -1
                        ) {
                            // this.getPageHomeUrl().then(() => {
                            this.$router.push({ path: '/' });
                            // });
                        }
                        this.loading = false;
                    });
            });
            // }
            //  else {
            //   console.error(this.$t('message.common.login.vaildFaild'))
            //   FMessage.error(this.$t('message.common.login.vaildFaild'))
            // }
            // })
        },
        // clear local cache
        clearSession() {
            storage.clear();
        },
    },
});
</script>
<style scoped>
@import '../../../style/variable.less';

.login {
    height: 100%;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    background-color: #001c40;
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
        background-color: #001c40;
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
            color: #044b93;
            font-weight: 600;
        }

        .label {
            font-size: 14px;
            line-height: 22px;
            margin-top: 20px;
        }

        .login-input {
            height: 50px;
            border-radius: 4px;
            padding-left: 15px;
            color: #515a6e;
            border: 1px solid #dee4ec;
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
            padding: 0;
        }

        :deep(.fes-input.login-input) {
            padding: 0;
        }

        :deep(.fes-input-inner-el) {
            transform: translateY(-2px);
            padding-left: 12px;
            height: 100%;
        }

        .remember-user-name {
            position: absolute;
            bottom: 106px;
            font-size: 12px;
        }

        .ivu-form-item {
            margin-bottom: 20px;
        }
    }

    :deep(.fes-form-item-label::before) {
        display: none;
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
