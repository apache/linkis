<template>
  <div
    :class="getClass"
    class="we-panel"
  >
    <slot/>
    <div
      v-for="i in itemLength"
      :key="i">
      <div
        v-if="i>1"
        :style="getDashStyle(i)"
        class="we-panel-dash"
        @mousedown.stop="handleMouseDown(i, $event)"/>
    </div>
  </div>
</template>
<script>
import util from '@/common/util';
import elementResizeEvent from '@/common/helper/elementResizeEvent';
export default {
  props: {
    diretion: {
      type: String,
      default: 'horizontal', // vertical垂直、horizontal水平
    },
  },
  data() {
    return {
      items: [],
      clientWidth: 0,
      clientHeight: 0,
      currentChange: 0,
      readyToAllocationWidth: 0,
      readyToAllocationHeight: 0,
      readyToAllocationItemLength: 0,
      moveIndex: null,
      requestFrameIds: []
    };
  },
  computed: {
    getClass() {
      return `we-panel-${this.diretion}`;
    },
    itemLength() {
      return this.items.length;
    },
    fristItem() {
      let fristItem;
      if (this.moveIndex) {
        fristItem = this.items.filter((item) => {
          return item.index === this.moveIndex - 1;
        })[0];
      }
      return fristItem;
    },
    secondItem() {
      let secondItem;
      if (this.moveIndex) {
        secondItem = this.items.filter((item) => {
          return item.index == this.moveIndex;
        })[0];
      }
      return secondItem;
    },
  },
  mounted() {
    window.addEventListener('mousemove', this.handleMouseMove);
    window.addEventListener('mouseup', this.handleMouseUp);
    elementResizeEvent.bind(this.$el, this.resize);
  },
  beforeDestroy: function() {
    window.removeEventListener('mousemove', this.handleMouseMove);
    window.removeEventListener('mouseup', this.handleMouseUp);
    elementResizeEvent.unbind(this.$el);
    this.requestFrameIds.forEach((id) => {
      elementResizeEvent.cancelAnimationFrame(id);
    })
  },
  methods: {
    resize() {
      this.clientWidth = this.$el.clientWidth;
      this.clientHeight = this.$el.clientHeight;
      this.initItem();
    },
    initItem() {
      if (!this.clientWidth && !this.clientHeight) {
        this.clientWidth = this.$el.clientWidth;
        this.clientHeight = this.$el.clientHeight;
      }
      this.readyToAllocationItemLength = 0;
      if (this.diretion == 'horizontal') {
        // 如果item设置了width，则根据width计算出它的currentWidth。如果没设置width，则平均 总宽度减去已经设置过的宽度
        this.readyToAllocationWidth = this.clientWidth;
        this.items.forEach((element) => {
          // element.index = this.$children.indexOf(element) + 1;
          if (element.width) {
            let currentWidth = element.width;
            if (util.isString(currentWidth)) {
              currentWidth = Number(currentWidth);
            }
            if (currentWidth < 1) {
              currentWidth = this.clientWidth * currentWidth;
            }
            element.currentWidth = currentWidth;
            this.readyToAllocationWidth -= element.currentWidth;
          } else {
            this.readyToAllocationItemLength += 1;
          }
        });
        this.items.forEach((element) => {
          if (element && !element.width) {
            element.currentWidth = this.readyToAllocationWidth / this.readyToAllocationItemLength;
          }
        });
      }
      if (this.diretion == 'vertical') {
        // 高度同上
        this.readyToAllocationHeight = this.clientHeight;
        this.items.forEach((element) => {
          if (element.height) {
            let currentHeight = element.height;
            if (util.isString(currentHeight)) {
              currentHeight = Number(currentHeight);
            }
            if (currentHeight < 1) {
              currentHeight = this.clientHeight * currentHeight;
            }
            element.currentHeight = currentHeight;
            this.readyToAllocationHeight -= element.currentHeight;
          } else {
            this.readyToAllocationItemLength += 1;
          }
        });
        this.items.forEach((element) => {
          if (!element.height) {
            element.currentHeight = this.readyToAllocationHeight / this.readyToAllocationItemLength;
          }
        });
      }
    },
    addItem(item) {
      this.items.push(item);
      if (this.items.length == this.$children.length) {
        this.initItem();
      }
    },
    removeItem(item) {
      let index = this.items.indexOf(item);
      if (index != -1) {
        this.items.splice(index, 1);
      }
      this.initItem();
    },
    getLeft(index) {
      let width = 0;
      this.items.forEach((element) => {
        if (element.index < index) {
          width += element.cWidth;
        }
      });
      return width;
    },
    getTop(index) {
      let height = 0;
      this.items.forEach((element) => {
        if (element.index < index) {
          height += element.cHeight;
        }
      });
      return height;
    },
    getDashStyle(index) {
      let diretion = this.diretion;
      let base = 4;
      if (diretion == 'horizontal') {
        return {
          left: this.getLeft(index) - base / 2 + 'px',
          top: 0,
          bottom: 0,
          width: base + 'px',
        };
      }
      if (diretion == 'vertical') {
        return {
          top: this.getTop(index) - base / 2 + 'px',
          left: 0,
          right: 0,
          height: base + 'px',
        };
      }
    },
    handleMouseDown(index, event) {
      this.moveIndex = index;
      const mouseEvent = this.getStdMouseEvent(event);
      this.startX = mouseEvent.positionX;
      this.startY = mouseEvent.positionY;
      this.$emit('on-move-start', event);
    },
    handleMouseMove(event) {
      if (this.moveIndex) {
        this.$emit('on-moving', event);
        event.stopPropagation();
        event.preventDefault();
        const mouseEvent = this.getStdMouseEvent(event);
        this.currentX = mouseEvent.positionX;
        this.currentY = mouseEvent.positionY;
        let variable = '';
        if (this.diretion == 'horizontal') {
          this.currentChange = this.currentX - this.startX;
          variable = 'currentWidth';
        }
        if (this.diretion == 'vertical') {
          this.currentChange = this.currentY - this.startY;
          variable = 'currentHeight';
        }
        let fristSize = this.fristItem[variable] + this.currentChange;
        let secondSize = this.secondItem[variable] - this.currentChange;
        if (this.fristItem.minSize && fristSize < this.fristItem.minSize) {
          this.currentChange = this.fristItem.minSize - this.fristItem[variable];
        } else if (this.fristItem.maxSize && fristSize > this.fristItem.maxSize) {
          this.currentChange = this.fristItem.maxSize - this.fristItem[variable];
        } else if (this.secondItem.minSize && secondSize < this.secondItem.minSize) {
          this.currentChange = this.secondItem[variable] - this.secondItem.minSize;
        } else if (this.secondItem.maxSize && secondSize > this.secondItem.maxSize) {
          this.currentChange = this.secondItem[variable] - this.secondItem.maxSize;
        } else if (fristSize < 10) {
          // 最小宽度为10，不要就要拖的重合了
          this.currentChange = 10 - this.fristItem[variable];
        } else if (secondSize < 10) {
          this.currentChange = this.secondItem[variable] - 10;
        }
        const AnimationFrameId = elementResizeEvent.requestAnimationFrame(() => {
          if (this.currentChange) {
            this.fristItem.currentChange = this.currentChange;
            this.secondItem.currentChange = -this.currentChange;
          }
        });
        this.requestFrameIds.push(AnimationFrameId);
      }
    },
    handleMouseUp(event) {
      if (this.moveIndex) {
        this.$emit('on-move-end', event);
        event.stopPropagation();
        event.preventDefault();
        let variable = '';
        if (this.diretion == 'horizontal') {
          variable = 'currentWidth';
        }
        if (this.diretion == 'vertical') {
          variable = 'currentHeight';
        }
        const AnimationFrameId = elementResizeEvent.requestAnimationFrame(() => {
          // 停止后记录更新当前的currentWidth或者currentHeight
          let fristSize = this.fristItem[variable] + this.currentChange;
          let secondSize = this.secondItem[variable] - this.currentChange;
          this.fristItem[variable] = fristSize;
          this.fristItem.currentChange = 0;
          this.secondItem[variable] = secondSize;
          this.secondItem.currentChange = 0;
          this.moveIndex = null;
          this.startX = null;
          this.startY = null;
          this.currentX = null;
          this.currentY = null;
          this.currentChange = null;
        });
        this.requestFrameIds.push(AnimationFrameId);
      }
    },
    getStdMouseEvent(event) {
      return {
        positionX: event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft,
        positionY: event.clientY + document.body.scrollTop + document.documentElement.scrollTop,
      };
    },
  },
};
</script>
