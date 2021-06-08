<template>
  <div class="layout-body" :class="{ 'layout-top': !showHeader}">
    <layout-header
      v-show="showHeader"
      @clear-session="clearSession"
      @set-init="setInit"></layout-header>
    <keep-alive v-if="isInit">
      <router-view
        v-if="$route.meta.keepAlive"/>
    </keep-alive>
    <router-view
      v-if="!$route.meta.keepAlive"/>
    <layout-footer v-show="showFooter"/>
  </div>
</template>
<script>
import headerModule from '@/dss/module/header';
import footerModule from '@/dss/module/footer';
import layoutMixin from '@/common/service/layoutMixin.js';
export default {
  components: {
    layoutFooter: footerModule.component,
    layoutHeader: headerModule.component
  },
  data() {
    return {
      isInit: false
    }
  },
  mixins: [layoutMixin],
  computed: {
    showHeader() {
      return this.$route.query.noHeader || location.search.indexOf('noHeader') < 0 
    },
    showFooter() {
      return this.$route.query.noFooter || location.search.indexOf('noFooter') < 0
    }
  },
  methods: {
    setInit() {
      this.isInit = true;
    },
  },
};
</script>
