<template>
  <div
    v-clickoutside="handleOutsideClick"
    v-show="isShow"
    :style="ctxStyle"
    :id="`menu-${id}`"
    class="ctx-menu-container"
    @click.stop="handleSelect"
    @contextmenu.stop>
    <div class="ctx">
      <ul
        class="ctx-menu">
        <slot/>
      </ul>
    </div>
  </div>
</template>
<script>
import clickoutside from '@/common/helper/clickoutside';

export default {
  name: 'WeMenu',
  directives: {
    clickoutside,
  },
  props: {
    id: String,
  },
  data() {
    return {
      isShow: false,
      ctxTop: 0,
      ctxLeft: 0,
      ctxBottom: 0,
      ctxRight: 0,
    };
  },
  computed: {
    ctxStyle() {
      const style = {
        top: `${this.ctxTop}px`,
        right: `${this.ctxRight}px`,
        bottom: `${this.ctxBottom}px`,
        left: `${this.ctxLeft}px`,
      };
      if (this.ctxTop === -1) {
        delete style.top;
      }
      if (this.ctxRight === -1) {
        delete style.right;
      }
      if (this.ctxBottom === -1) {
        delete style.bottom;
      }
      if (this.ctxLeft === -1) {
        delete style.left;
      }
      return style;
    },
  },
  methods: {
    setPositionFromEvent(e) {
      const { pageX, pageY } = e;
      // 窗口的高度
      const innerHeight = window.innerHeight;
      const innerwidth = window.innerWidth;
      const top = pageY - (document.body.scrollTop);
      this.ctxTop = top;
      this.ctxLeft = pageX;
      this.ctxBottom = -1;
      this.ctxRight = -1;
      this.$nextTick(() => {
        const menu = document.getElementById(`menu-${this.id}`);
        const menuHeight = menu.scrollHeight;
        const menuWidth = menu.scrollWidth;
        if (top + menuHeight >= innerHeight) {
          this.ctxBottom = innerHeight - pageY + menuHeight;
          this.ctxTop = -1;
        }
        if (pageX + menuWidth >= innerwidth) {
          this.ctxRight = innerwidth - pageX + menuWidth;
          this.ctxLeft = -1;
        }
      });
    },
    handleOutsideClick() {
      this.close();
      this.$emit('we-menu-cancel');
    },
    handleSelect() {
      this.close();
      this.$emit('we-menu-select');
    },
    show() {
      this.isShow = true;
    },
    close() {
      this.isShow = false;
    },
    open(e) {
      this.show();
      this.setPositionFromEvent(e);
    },
  },
};
</script>
<style lang="scss" src="./index.scss"></style>
