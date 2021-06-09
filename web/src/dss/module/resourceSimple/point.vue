<template>
  <div v-if="pointList.length">
    <span
      v-for="(item, index) in pointList"
      :key="index"
      class="footer-legend">
      <span
        class="footer-legend-point"
        :class="getClass(item.type)"></span>
      <span class="footer-legend-label">{{ item.label }}</span>
    </span>
  </div>
</template>
<script>
export default {
  props: {
    pointList: {
      type: Array,
      defalut: () => [],
    },
  },
  methods: {
    getClass(type) {
      return `point-${type}`;
    },
  },
};
</script>
