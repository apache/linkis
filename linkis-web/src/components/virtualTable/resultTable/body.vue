<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<template>
  <div>
    <div class="list-view-phantom" :style="{
      width: contentWidth + 'px'
    }">
    </div>
    <div ref="content" class="list-view-content" :class="{ header:type === 'header' }">
      <div class="list-view-item" :style="{
        width: sizeAndOffsetCahce[startIndex + index].size + 'px'
      }" v-for="(col, index) in visibleData" :key="index">
        <slot :col="col" :index="index"></slot>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    cache: {
      type: Object,
      default: () => {
        return {};
      },
    },
    isListenScroll: {
      type: Boolean,
      default: false,
    },
    data: {
      type: Array,
      required: true,
    },

    estimatedItemSize: {
      type: Number,
      default: 30,
    },

    itemSizeGetter: {
      type: Function,
    },
    header: {
      type: Array,
      default: () => [],
    },
    type: String,
  },

  data() {
    return {
      viewWidth: 0,
      lastMeasuredIndex: -1,
      startIndex: 0,
      sizeAndOffsetCahce: {},
      visibleData: [],
      ops: {
        bar: {
          background: 'rgb(24, 144, 255)',
          keepShow: true,
          minSize: 0.1,
        },
        rail: {
          border: '1px solid #cecece',
          size: '20px',
        },
        scrollButton: {
          enable: true,
          background: '#cecece',
        },
      },
      cacheVH: {
        v: {},
        h: {
          scrollLeft: 0,
        },
      },
      isDivideWidth: false,
      divideWidth: 0,
    };
  },
  computed: {
    contentWidth() {
      const {
        header,
        lastMeasuredIndex,
        estimatedItemSize
      } = this;
      let itemCount = header.length;
      if (lastMeasuredIndex >= 0) {
        const lastMeasuredSizeAndOffset = this.getLastMeasuredSizeAndOffset();
        return lastMeasuredSizeAndOffset.offset + lastMeasuredSizeAndOffset.size + (itemCount - 1 -
            lastMeasuredIndex) * estimatedItemSize;
      } else {
        return itemCount * estimatedItemSize;
      }
    },
  },
  watch: {
    data: {
      handler() {
        this.resize();
      },
    },
  },
  mounted() {
    this.setItemWidth();
  },
  methods: {
    setItemWidth(isDataChange) {
      this.viewWidth = this.$refs.content.clientWidth;
      let count = 0;
      this.header.forEach((item, index) => {
        count += this.getItemSizeAndOffset(this.startIndex + index).size;
      });
      // Proportional widths are used if the sum of all columns with assigned widths is less than the width of the table(如果所有已分配宽度的列宽度和小于表格的宽度，就使用等比宽度)
      if (count && count <= this.viewWidth) {
        this.isDivideWidth = true;
        this.divideWidth = this.viewWidth / this.header.length;
        let offset = 0;
        for (const i in this.sizeAndOffsetCahce) {
          if (Object.prototype.hasOwnProperty.call(this.sizeAndOffsetCahce, i)) {
            this.sizeAndOffsetCahce[i] = {
              size: this.divideWidth,
              offset,
            };
            this.cache[i] = this.divideWidth;
            offset += this.divideWidth;
          }
        }
      }
      if (isDataChange) {
        let {
          v,
          h
        } = this.cacheVH;
        this.handleScroll(v, h);
      } else {
        this.updateVisibleData();
      }
    },
    getItemSizeAndOffset(index) {
      const {
        lastMeasuredIndex,
        sizeAndOffsetCahce,
        // data,
        itemSizeGetter,
        header
      } = this;
      if (lastMeasuredIndex >= index) {
        return sizeAndOffsetCahce[index];
      }
      let offset = 0;
      if (lastMeasuredIndex >= 0) {
        const lastMeasured = sizeAndOffsetCahce[lastMeasuredIndex];
        if (lastMeasured) {
          offset = lastMeasured.offset + lastMeasured.size;
        }
      }
      for (let i = lastMeasuredIndex + 1; i <= index; i++) {
        let item = header[i];
        let size;
        if (this.isDivideWidth) {
          size = this.divideWidth;
          this.cache[i] = size;
        } else {
          if (this.cache[i]) {
            size = this.cache[i];
          } else {
            size = itemSizeGetter.call(null, item, i);
            this.cache[i] = size;
          }
        }
        sizeAndOffsetCahce[i] = {
          size,
          offset,
        };
        offset += size;
      }
      if (index > lastMeasuredIndex) {
        this.lastMeasuredIndex = index;
      }
      return sizeAndOffsetCahce[index];
    },

    getLastMeasuredSizeAndOffset() {
      return this.lastMeasuredIndex >= 0 ? this.sizeAndOffsetCahce[this.lastMeasuredIndex] : {
        offset: 0,
        size: 0
      };
    },

    findNearestItemIndex(scrollLeft) {
      const {
        data
      } = this;
      let total = 0;
      for (let i = 0, j = data.length; i < j; i++) {
        const size = this.getItemSizeAndOffset(i).size;
        total += size;
        if (total >= scrollLeft || i === j - 1) {
          return i;
        }
      }

      return 0;
    },

    updateVisibleData(scrollLeft) {
      let canScrollWidth = this.contentWidth - this.viewWidth;
      scrollLeft = scrollLeft || 0;
      if (scrollLeft > canScrollWidth) {
        scrollLeft = canScrollWidth;
      }
      const start = this.findNearestItemIndex(scrollLeft);
      const end = this.findNearestItemIndex(scrollLeft + (this.$el.clientWidth || 1400));
      this.visibleData = this.data.slice(start, Math.min(end + 3, this.data.length));
      if (this.visibleData.length < 1) return
      this.startIndex = start;
      this.$refs.content.style.webkitTransform = `translate3d(${this.getItemSizeAndOffset(start).offset}px, 0, 0)`;
    },

    handleScroll(v, h) {
      const {
        scrollLeft
      } = h;
      this.cacheVH = {
        v,
        h
      };
      this.$emit('on-scroll', {
        v,
        h
      });
      this.updateVisibleData(scrollLeft);
    },

    resize() {
      this.reset();
      this.setItemWidth(true);
    },

    reset() {
      this.viewWidth = 0;
      this.lastMeasuredIndex = -1;
      this.startIndex = 0;
      this.sizeAndOffsetCahce = {};
      this.visibleData = [];
      this.isDivideWidth = false;
      this.divideWidth = 0;
    },
  },
  beforeDestroy(){

  }
};

</script>
<style>
  .list-view-phantom {
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    z-index: -1;
    height: 100%;
  }

  .list-view-content {
    display: flex;
    left: 0;
    right: 0;
    top: 0;
    position: absolute;
    height: 100%;
  }

  .list-view-item {
    height: 100%;
    color: #666;
    box-sizing: border-box;
    flex-shrink: 0;
    text-align: center;
  }

</style>
