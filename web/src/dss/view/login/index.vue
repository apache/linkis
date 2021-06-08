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
          <div class="label">用户名</div>
          <Input
            v-model="loginForm.user"
            type="text"
            :placeholder="$t('message.common.login.userName')"
            size="large"/>
        </FormItem>
        <FormItem prop="password">
          <div class="label">密码</div>
          <Input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
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
    <Modal
      v-model="showProxyList"
      :loading="selectProxyLoading"
      title="请选择需要的角色进入或取消直接进入"
      @on-ok="onOk"
      @on-cancel="onCancel">
      <RadioGroup class="radioGroup" v-model="proxyUser" vertical>
        <Radio v-for="(item, index) in proxyList" :key="index" :label="item"></Radio>
      </RadioGroup>
    </Modal>
  </div>
</template>
<script>
import api from '@/common/service/api';
import storage from '@/common/helper/storage';
// 【dss-scriptis】前端执行接口降级
// import socket from '@/apps/scriptis/module/webSocket';
import { db } from '@/common/service/db/index.js';
import { config } from '@/common/config/db.js';
import { RSA } from '@/common/util/ras.js';
import util from '@/common/util/';
import tab from '@/apps/scriptis/service/db/tab.js';
export default {
  data() {
    return {
      loading: false,
      loginForm: {
        user: '',
        password: '',
      },
      proxyList: [],
      selectProxyLoading: true,
      showProxyList: false,
      proxyUser: '',
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
    // 获取登录后的url调转
    getPageHomeUrl() {
      const currentModules = util.currentModules();
      return api.fetch(`${this.$API_PATH.WORKSPACE_PATH}getWorkspaceHomePage`, {
        micro_module: currentModules.microModule || 'dss'
      }, 'get').then((res) => {
        return res.workspaceHomePage.homePageUrl;
      }).catch(() => {
        return '/'
      });
    },
    // 获取公钥接口
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
          // 需要判断是否需要给密码加密
          let password = this.loginForm.password;
          let params = {};
          if (this.publicKeyData && this.publicKeyData.enableLoginEncrypt) {
            const key = RSA.getPublicKey(`-----BEGIN PUBLIC KEY-----${this.publicKeyData.publicKey}-----END PUBLIC KEY-----`);
            password = RSA.encrypt(this.loginForm.password, key);
            params = {
              userName: this.loginForm.user,
              passwdEncrypt: password,
            };
          } else {
            params = {
              userName: this.loginForm.user,
              password: password,
            };
          }
          // 登录清掉本地缓存
          // 保留Scripts页面打开的tab页面
          // 连续两次退出登录后，会导致数据丢失，所以得判断是否已存切没有使用
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
              // 保存用户名
              if (this.rememberUserNameAndPass) {
                storage.set('saveUserNameAndPass', `${this.loginForm.user}&${this.loginForm.password}`, 'local');
              }
              if (rst) {
                // 获取代理用户列表并选择代理用户
                this.getProxyList(rst.userName, (proxy) => {
                  // 判断代理用户是否开启或存在，如果请求错误直接按原逻辑走
                  if(proxy && proxy.proxyEnable && proxy.proxyUsers && proxy.proxyUsers.length > 0) {
                    this.proxyList = proxy.proxyUsers;
                    this.userName = rst.userName;
                    this.showProxyList = true;
                  } else {
                    // 登录之后需要获取当前用户的调转首页的路径
                    this.getPageHomeUrl().then(() => {
                      // this.$router.push({path: res});
                      // 直接跳转管理台页面
                      this.$router.push({path: '/console'});
                      this.$Message.success(this.$t('message.common.login.loginSuccess'));
                    })
                  }
                })
              }
            })
            .catch((err) => {
              if (this.rememberUserNameAndPass) {
                storage.set('saveUserNameAndPass', `${this.loginForm.user}&${this.loginForm.password}`, 'local');
              }
              if (err.message.indexOf('已经登录，请先退出再进行登录') !== -1) {
                this.getPageHomeUrl().then((res) => {
                  this.$router.push({path: res});
                })
              }
              this.loading = false;
            });
        } else {
          this.$Message.error(this.$t('message.common.login.vaildFaild'));
        }
      });
    },
    // 弹出代理用户选择模态框操作
    onOk() {
      // 判断用户是否选择代理用户
      if(!this.proxyUser) {
        this.selectProxyLoading = false;
        this.$nextTick(() => {this.selectProxyLoading = true})
        return this.$Message.warning(this.$t('message.common.login.selectProxyTip'))
      }
      let params = {userName: this.userName, proxyUser: this.proxyUser};
      this.showProxyList = false;
      this.bindProxyList(params)
    },
    onCancel() {
      // 登录之后需要获取当前用户的调转首页的路径
      let params = {userName: this.userName, proxyUser: this.userName};
      this.bindProxyList(params)
      this.showProxyList = false;
    },
    // 获取代理用户列表
    getProxyList(data, callback) {
      if(data) {
        api.fetch('/user/proxyInfo?userName=' + data, 'get').then((res) => {
          callback(res);
        }).catch((err) => {
          window.console.error(err);
          callback(false);
        });
      } else {
        throw new Error('用户名无效！');
      }
    },
    // 绑定用户代理接口
    bindProxyList(params) {
      api.fetch(`/user/bindUser`, params).then(res => {
        if(res) {
          // 登录之后需要获取当前用户的调转首页的路径
          this.getPageHomeUrl().then((urlRes) => {
            this.$router.push({ path: urlRes })
            this.$Message.success(this.$t('message.common.login.loginSuccess'))
          })
        } else {
          this.$Message.error(res.message)
        }
      }).catch(() => {
        throw new Error('绑定失败！')
      })
    },
    // 清楚本地缓存
    clearSession() {
      storage.clear();
    },
  },
};
</script>
<style lang="scss" src="@/dss/assets/styles/login.scss"></style>
