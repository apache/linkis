<template>
  <li
    class="ctx-item"
    @click="handleClick"
    @mouseover="handleOver"
    @mouseout="handleOut">
    <slot/>
  </li>
</template>
<script>
import emitter from '@/common/helper/emitter';

export default {
  name: 'WeMenuItem',
  mixins: [emitter],
  methods: {
    handleClick(e) {
      this.$emit('select', e);
    },
    handleOver(e) {
      this.broadcast('WeSubMenu', 'we-menu-over', e);
    },
    handleOut(e) {
      this.broadcast('WeSubMenu', 'we-menu-leave', e);
    },
  },
};
</script>
