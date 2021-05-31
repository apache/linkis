<template>
  <div>
    <div class="layout-header">
      <div
        class="layout-header-menu-icon"
      >
        <div class="logo">
          <img
            class="logo-img"
            src="../../assets/images/dssLogo5.png"
            :alt="$t('message.common.logoName')"
          >
          <span class="version">{{sysVersion}}</span>
        </div>
      </div>
      <div
        v-clickoutside="handleOutsideClick"
        :class="{'selected': isUserMenuShow}"
        class="user"
        @click="handleUserClick"
      >
        <span>{{ userName }}</span>
        <Icon v-show="!isUserMenuShow" type="ios-arrow-down" class="user-icon"/>
        <Icon v-show="isUserMenuShow" type="ios-arrow-up" class="user-icon"/>
        <userMenu v-show="isUserMenuShow" @clear-session="clearSession"/>
      </div>
      <ul class="menu">
        <li class="menu-item" @click="goConsole">{{$t("message.common.management")}}</li>
      </ul>
      <div class="icon-group">
        <Icon
          v-if="isSandbox"
          :title="$t('message.common.home')"
          class="book"
          type="ios-chatboxes"
          @click="linkTo('freedback')"
        ></Icon>
      </div>
    </div>
  </div>
</template>
<script>
import { isEmpty } from "lodash";
import storage from "@/common/helper/storage";
import userMenu from "./userMenu.vue";
import clickoutside from "@/common/helper/clickoutside";
import mixin from '@/common/service/mixin';
import util from '@/common/util';
import { GetBaseInfo } from '@/common/service/apiCommonMethod.js';
export default {
  directives: {
    clickoutside
  },
  components: {
    userMenu
  },
  data() {
    return {
      sysVersion: process.env.VUE_APP_VERSION,
      isUserMenuShow: false,
      userName: "",
      isSandbox: process.env.NODE_ENV === "sandbox",
    };
  },
  mixins: [mixin],
  created() {
    this.init();
  },
  methods: {
    init() {
      GetBaseInfo().then(rst => {
        if (!isEmpty(rst)) {
          this.userName = rst.username;
          storage.set("baseInfo", rst, "local");
          this.$router.app.$emit("username", rst.username);
          this.$emit("set-init");
        }
      });
    },
    handleOutsideClick() {
      this.isUserMenuShow = false;
    },
    handleUserClick() {
      this.isUserMenuShow = !this.isUserMenuShow;
    },
    clearSession() {
      this.$emit("clear-session");
    },
    linkTo(type) {
      let url = "";
      if (type === "book") {
        url = `https://github.com/WeBankFinTech/DataSphereStudio/blob/master/docs/zh_CN/ch3/DSS_User_Manual.md`;
      } else if (type === "github") {
        url = `https://github.com/WeBankFinTech/DataSphereStudio`;
      } else if (type === "freedback") {
        url = "https://wj.qq.com/s2/4943071/c037/ ";
        if (localStorage.getItem("locale") === "en") {
          url = "https://wj.qq.com/s2/4943706/5a8b";
        }
      }
      util.windowOpen(url);
    },
    goConsole(){
      this.$router.push({path: '/console',query: Object.assign({}, this.$route.query)});
    },
    goRolesPath() {
      // 根据接口getWorkspaceBaseInfo渲染跳转不同路径
      this.$router.push({path: this.homeRoles.path, query: Object.assign({}, this.$route.query)});
    },
  }
};
</script>
<style lang="scss" scoped src="./index.scss"></style>
