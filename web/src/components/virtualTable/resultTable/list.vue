<template>
  <div
    class="list-view"
    ref="list">
    <vuescroll
      ref="vuescroll"
      :ops="ops"
      @handle-scroll="handleScroll">
      <list-body
        ref="body"
        :cache="cache"
        :item-size-getter="itemSizeGetter"
        :estimated-item-size="30"
        :data="data"
        :header="header"
        @active="changeActive"
        type="body">
        <div
          slot-scope="{col}">
          <div
            v-for="(content, contentIndex) in col"
            :title="content || contentIndex"
            :key="contentIndex"
            class="we-column-item"
            :class="{'null-text': content === 'NULL', 'active': contentIndex === activeRowIndex}"
            @click.stop="columnItemClick(contentIndex)">
            <span class="we-column-item-content">{{ content }}</span>
          </div>
        </div>
      </list-body>
    </vuescroll>
  </div>
</template>
<script>
import listBody from './body.vue';
import vuescroll from 'vuescroll/dist/vuescroll-native';
export default {
  components: {
    vuescroll,
    listBody,
  },
  props: {
    cache: {
      type: Object,
      default: () => {
        return {};
      },
    },
    data: {
      type: Array,
      required: true,
    },
    header: {
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
      ops: {
        bar: {
          background: '#cdcdcd',
          keepShow: true,
          minSize: 0.05,
        },
        rail: {
          size: '10px',
          opacity: 1,
          background: '#fff',
        },
        scrollButton: {
          enable: true,
          background: '#cecece',
        },
      },
      activeRowIndex: null,
    };
  },
  methods: {
    handleScroll(v, h) {
      this.$emit('on-scroll', { v, h });
      this.$refs.body.handleScroll(v, h);
    },
    columnItemClick(index) {
      this.activeRowIndex = index;
      this.$emit('on-click', index);
    },
    changeActive() {
      this.activeRowIndex = null;
    }
  },
};
</script>
