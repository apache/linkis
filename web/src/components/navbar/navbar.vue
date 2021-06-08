<template>
  <div class="we-file-navbar">
    <we-searchbox
      ref="searchbox"
      v-model="searchText"
      :placeholder="placeholder"
      :style="{'width': getWidth()}"/>
    <div
      class="we-file-navbar-nav">
      <Icon
        v-if="nav.isShowNav('export')"
        :size="16"
        type="ios-share-outline"
        :title="$t('message.common.navBar.dataStudio.outTable')"
        class="navbar-cursor"
        @click="exportFrom"/>
      <Icon
        v-if="nav.isShowNav('import')"
        :size="20"
        type="ios-archive-outline"
        :title="$t('message.common.navBar.dataStudio.importHive')"
        class="navbar-cursor"
        @click="importTo"/>
      <Icon
        v-if="nav.isShowNav('newFile')"
        :size="16"
        type="ios-add-circle-outline"
        :title="addTitle"
        class="navbar-cursor"
        @click="addFile"/>
      <Icon
        v-if="nav.isShowNav('refresh')"
        :size="20"
        type="ios-refresh"
        :title="$t('message.common.refresh')"
        class="navbar-cursor"
        @click="refresh"/>
    </div>
  </div>
</template>
<script>
import weSearchbox from './searchbox.vue';
import Nav from './nav.js';
export default {
  components: {
    weSearchbox,
  },
  props: {
    navList: {
      type: Array,
      require: true,
    },
    placeholder: {
      type: String,
      default () {
        // 这里不能写成箭头函数 要拿到this值
        return this.$t('message.common.navBar.dataStudio.searchPlaceholder')
      }
    },
    addTitle: {
      type: String,
      default () {
        return this.$t('message.common.navBar.dataStudio.addTitle')
      }
    },
  },
  data() {
    return {
      nav: null,
      searchText: '',
    };
  },
  watch: {
    searchText: function(value) {
      this.$emit('text-change', value);
    },
  },
  created() {
    this.nav = new Nav({
      navList: this.navList,
    });
  },
  methods: {
    showSearch() {
      this.$nextTick(() => {
        this.$refs.searchbox.onfocus();
      });
    },
    refresh() {
      this.$emit('on-refresh');
    },
    addFile() {
      this.$emit('on-add');
    },
    importTo() {
      this.$emit('on-import');
    },
    exportFrom() {
      this.$emit('on-export');
    },
    getWidth() {
      const len = this.navList.length;
      let px = '23px';
      if (len === 4) {
        px = '62px';
      } else if (len === 3) {
        px = '39px';
      }
      return `calc(100% - ${px})`;
    },
  },
};
</script>
<style lang="scss" src="./index.scss"></style>
