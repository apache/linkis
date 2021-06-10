<template>
  <div
    :style="getStyle"
    class="we-panel-item">
    <slot :width="currentWidth"/>
  </div>
</template>
<script>
import util from '@/common/util';
export default {
  props: {
    min: {
      type: [Number, String],
    },
    max: {
      type: [Number, String],
    },
    width: {
      type: [Number, String],
    },
    height: {
      type: [Number, String],
    },
    index: Number,
  },
  data() {
    return {
      parent: this.$parent,
      currentWidth: 0,
      currentHeight: 0,
      currentChange: 0,
      isFullScreen: false,
      isDiy: false,
      diyStyle: null,
    };
  },
  computed: {
    minSize() {
      let min = this.min;
      if (util.isString(min)) {
        min = Number(min);
      }
      if (min < 1) {
        let diretion = this.parent.diretion;
        if (diretion == 'horizontal') {
          min = this.parent.clientWidth * min;
        }
        if (diretion == 'vertical') {
          min = this.parent.clientHeight * min;
        }
      }
      return min;
    },
    maxSize() {
      let max = this.max;
      if (util.isString(max)) {
        max = Number(max);
      }
      if (max < 1) {
        let diretion = this.parent.diretion;
        if (diretion == 'horizontal') {
          max = this.parent.clientWidth * max;
        }
        if (diretion == 'vertical') {
          max = this.parent.clientHeight * max;
        }
      }
      return max;
    },
    cWidth() {
      let x = this.currentWidth + this.currentChange;
      return x;
    },
    cHeight() {
      return this.currentHeight + this.currentChange;
    },
    left() {
      return this.parent.getLeft(this.index);
    },
    top() {
      return this.parent.getTop(this.index);
    },
    getStyle() {
      let styleAttr;
      if (!this.isFullScreen) {
        let diretion = this.parent.diretion;
        if (diretion == 'horizontal') {
          styleAttr = {
            left: this.left + 'px',
            top: 0,
            bottom: 0,
            width: this.cWidth + 'px',
          };
        }
        if (diretion == 'vertical') {
          styleAttr = {
            top: this.top + 'px',
            left: 0,
            right: 0,
            height: this.cHeight + 'px',
          };
        }
      } else if (this.isDiy) {
        styleAttr = Object.assign({
          'position': 'fixed',
          'z-index': 100,
        }, this.diyStyle);
      } else {
        styleAttr = {
          'position': 'fixed',
          'left': 0,
          'right': 0,
          'top': 0,
          'bottom': 0,
          'z-index': 100,
        };
      }
      return styleAttr;
    },
  },
  watch: {
    height() {
      this.parent.initItem();
    },
    width() {
      this.parent.initItem();
    },
    cWidth() {
      this.$emit('on-change', {
        height: this.cHeight,
        width: this.cWidth,
      });
    },
    cHeight() {
      this.$emit('on-change', {
        height: this.cHeight,
        width: this.cWidth,
      });
    },
  },
  mounted() {
    this.parent.addItem(this);
  },
  beforeDestroy() {
    this.parent.removeItem(this);
  },
  methods: {
    fullScreen(option) {
      this.isDiy = option.isDiy;
      this.diyStyle = option.diyStyle;
      this.isFullScreen = true;
    },
    releaseFullScreen() {
      this.isFullScreen = false;
    },
  },
};
</script>
