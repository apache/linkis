<template>
  <ul
    v-show="isShow"
    class="second-menu">
    <slot/>
  </ul>
</template>
<script>
export default {
  name: 'WeSubMenu',
  data() {
    return {
      isShow: false,
    };
  },
  mounted() {
    this.initEvent();
  },
  methods: {
    initEvent() {
      this.$on('we-menu-over', this.handleOver);
      this.$on('we-menu-leave', this.handleOut);
    },
    handleOver() {
      this.isShow = true;
    },
    handleOut() {
      this.isShow = false;
    },
  },
};
</script>
