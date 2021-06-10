<template>
  <div class="layout-body" :class="{ 'layout-top': showHeader}">
    <layout-header
      v-show="showHeader"
      @clear-session="clearSession"/>
    <router-view/>
    <layout-footer v-show="showFooter"/>
  </div>
</template>
<script>
import headerModule from '../module/header';
import footerModule from '@/dss/module/footer';
import layoutMixin from '@/common/service/layoutMixin.js';
export default {
  components: {
    layoutFooter: footerModule.component,
    layoutHeader: headerModule.component,
  },
  mixins: [layoutMixin],
  computed: {
    showHeader() {
      return this.$route.query.noHeader || location.search.indexOf('noHeader') < 0
    },
    showFooter() {
      return this.$route.query.noFooter || location.search.indexOf('noFooter') < 0
    }
  }
};
</script>
