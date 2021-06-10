<template>
  <div
    class="progress-content"
    @click="expandChange"
    :class="{'children': children}">
    <span
      class="item-progress"
      :style="{'width': progressData.percent}"></span>
    <div class="text-content">
      <Icon
        v-if="!children"
        class="item-icon"
        :type="!progressData.expand? 'md-arrow-dropup' : 'md-arrow-dropdown'"
      ></Icon>
      <span class="item-label">{{ progressData.title }}</span>
      <span class="item-progress-label">{{ progressData.percent }}</span>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    progressData: {
      type: Object,
      default: () => {}
    },
    children: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {

    }
  },
  methods: {
    expandChange() {
      // 返回父组件更改展开状态
      if(!this.children) this.progressData.expand = !this.progressData.expand;
      this.$emit('expandChange', this.progressData, this.children)
    }
  }
}
</script>
<style src="./index.scss" lang="scss" scoped></style>

