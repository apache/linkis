<template>
  <div class="nav-menu">
    <div class="nav-menu-left">
      <template v-for="(item, index) in menuList">
        <div
          class="nav-menu-left-item"
          :key="index"
          :class="{'actived':current && item.id === current.id}"
          @mouseover.stop="onMouseOver(item)">
          <!-- <p class="nav-menu-left-sub-title">{{item.subTitle}}</p> -->
          <p class="nav-menu-left-main-title">{{item.title}}</p>
          <Icon
            size="14"
            type="ios-arrow-forward"
            class="nav-menu-left-icon"
            v-if="item.appInstances"></Icon>
        </div>
      </template>
    </div>
    <div
      class="nav-menu-right"
      v-if="current">
      <div
        class="nav-menu-right-item"
        :key="current.id">
        <div class="nav-menu-right-item-title">{{current.title}}</div>
        <div
          v-for="item in current.appInstances"
          class="nav-menu-right-item-child"
          :key="item.title"
          @click.stop="handleClick(item)">
          <SvgIcon class='nav-menu-right-item-icon' :icon-class="iconSplit(item.icon)[0]" :color="iconSplit(item.icon)[1]"/>
          <span>{{item.title}}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import mixin from '@/common/service/mixin';
export default {
  data() {
    return {
      current: null
    }
  },
  props: {
    menuList: {
      type: Array,
      default: () => []
    }
  },
  mixins: [ mixin ],
  methods: {
    iconSplit(icon){
      if(icon){
        return icon.split('|')
      }
      return ['','']
    },
    onMouseOver(item) {
      if (item.appInstances) {
        return this.current = item;
      }
      this.current = null;
    },
    handleClick(child) {
      if (child.name) {
        let query = this.$route.query;
        this.gotoCommonIframe(child.name, query);
      } else {
        this.$Message.warning(this.$t('message.common.warning.comingSoon'));
      }
      this.$emit('click', child);
    }
  },
}
</script>
<style lang="scss" src="./index.scss">
</style>
