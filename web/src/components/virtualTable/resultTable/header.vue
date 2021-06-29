<template>
  <div
    class="list-view list-header"
    ref="list">
    <list-body
      ref="body"
      :cache="cache"
      :item-size-getter="itemSizeGetter"
      :estimated-item-size="30"
      :data="data"
      :header="data"
      type="header">
      <template
        slot-scope="{col, index}">
        <div
          class="we-table-header-box"
          @dblclick.prevent.stop="handleDblClick(col)">
          <span
            class="we-table-header-content"
            :title="col.colHeadHoverTitle">{{ col.content }}</span>
          <span
            v-if="col.sortable"
            class="caret-wrapper">
            <i
              :class="computeSortClass(col, 'asc')"
              @click.stop="handleSortClick($event, {col, index}, 'asc')"/>
            <i
              :class="computeSortClass(col, 'desc')"
              @click.stop="handleSortClick($event, {col, index}, 'desc')"/>
          </span>
        </div>
      </template>
    </list-body>
  </div>
</template>
<script>
import listBody from './body.vue';
const prefixCls = 'we-table';
export default {
  components: {
    listBody,
  },
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
  },

  data() {
    return {
      sort: {
        sorting: false,
        column: null,
        type: 'normal',
        index: 0,
      }
    };
  },
  methods: {
    computeSortClass(currentHead, type) {
      return [
        `${prefixCls}-sort-caret`,
        type,
        {
          [`${prefixCls}-sort`]: (this.sort.column === currentHead && this.sort.type === type),
        },
      ];
    },
    handleScroll(v, h) {
      this.$refs.body.handleScroll(v, h);
    },
    // sorting 中不允许再点击
    handleSortClick(event, { col, index }, sortOrder) {
      if (this.sort.sorting) return;
      if (this.sort.type === sortOrder && this.sort.index === index) {
        sortOrder = 'normal';
      }
      this.sort = {
        column: col,
        type: sortOrder,
        sorting: true,
        index
      }
      this.$emit('sort-click', {
        column: col,
        order: sortOrder,
        key: col.content,
        cb: () => {
          this.sort.sorting = false;
        },
      });
    },
    handleDblClick(col) {
      this.$emit('dbl-click', col);
    },
  },
};
</script>
