<template>
  <div
    ref="table"
    :style="style"
    class="we-table">
    <div class="we-table-outflow">
      <div
        class="we-table-header">
        <template-header
          @sort-click="handleSortClick"
          @dbl-click="handleDblClick"
          ref="headerCom"
          class="header-list"
          :cache="cache"
          :item-size-getter="itemSizeGetter"
          :estimated-item-size="30"
          :data="headRows">
        </template-header>
      </div>
      <div
        ref="tableBody"
        class="we-table-body">
        <template-list
          ref="bodyCom"
          :cache="cache"
          :is-listen-scroll="true"
          :item-size-getter="itemSizeGetter"
          :estimated-item-size="30"
          :data="bodyRows"
          :header="headRows"
          @on-scroll="changeScrollLeft"
          @on-click="onColumnClick">
        </template-list>
      </div>
    </div>
  </div>
</template>
<script>
import templateList from './list.vue';
import templateHeader from './header.vue';

const prefixCls = 'we-table';
export default {
  name: prefixCls,
  components: {
    templateList,
    templateHeader,
  },
  props: {
    data: Object,
    width: Number,
    height: [Number, String]
  },
  data() {
    return {
      cache: {},
      sort: {
        sorting: false,
        column: null,
        type: 'normal',
        start: 0,
      },
      bodyRows: [],
      headRows: []
    };
  },
  computed: {
    style() {
      const style = {
      };
      if (this.width) {
        // style.width = this.width + 'px';
      }
      if (this.height) {
        style.height = this.height + 'px';
      }
      return style;
    },
  },
  watch: {
    'data': {
      handler(val) {
        this.resize();
        this.headRows = val.headRows;
        this.bodyRows = this.revert(val.bodyRows);
      },
      immediate: true,
      deep: true
    },

  },
  mounted() {
  },
  methods: {
    changeScrollLeft({ v, h }) {
      this.$refs.headerCom.$refs.list.scrollLeft = h.scrollLeft;
      this.$refs.headerCom.handleScroll(v, h);
    },
    itemSizeGetter(item, i) {
      let div = document.createElement('div');
      document.body.appendChild(div);
      div.style.paddingLeft = '20px';
      div.style.paddingRight = '54px';
      div.style.position = 'absolute';
      div.style.top = '-9999px';
      div.style.left = '-9999px';
      div.innerHTML = item.content;
      let width = div.offsetWidth;
      document.body.removeChild(div);
      this.cache[i] = width;
      if (width > 300) {
        width = 300;
      }
      return width;
    },
    revert(data) {
      let newData = [];
      let firstRowLen = data[0] ? data[0].length : 0;
      for (let i = 0; i < firstRowLen; i++) {
        newData[i] = [];
        for (let j = 0; j < data.length; j++) {
          newData[i][j] = data[j][i];
        }
      }
      return newData;
    },
    computeSortClass(currentHead, type) {
      return [
        `${prefixCls}-sort-caret`,
        type,
        {
          [`${prefixCls}-sort`]: (this.sort.column === currentHead && this.sort.type === type),
        },
      ];
    },
    handleSortClick(args) {
      this.$emit('on-sort-change', args);
    },
    handleDblClick(col) {
      this.$emit('dbl-click', col);
    },
    onColumnClick(index) {
      this.$emit('on-click', index);
    },
    resize() {
      this.cache = {};
    }
  },
};
</script>
<style lang="scss" src="../index.scss"></style>
<style>
.list-view {
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;
  color: #333;
}
</style>
