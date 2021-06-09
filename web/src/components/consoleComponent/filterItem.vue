<template>
  <li
    class="filter-view-item">
    <Icon
      type="ios-checkbox-outline"
      v-if="item.checked"
      size="14"
      color="#000"></Icon>
    <Icon
      type="ios-square-outline"
      size="14"
      color="#000"
      v-else></Icon>
    <span
      class="filter-view-item-text"
      :title="item.title">{{ item.title }}</span>
  </li>
</template>
<script>
export default {
  name: 'FilterItem',
  props: {
    item: Object
  }
}
</script>
<style lang="scss" scoped>
  .filter-view-item {
    line-height: 28px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .filter-view-item-text {
      width: 130px;
      margin-left: 4px;
      display: inline-block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      vertical-align: middle;
    }
  }
</style>
