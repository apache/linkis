<template>
  <li 
    class="node-item v-ellipsis"
    :class="{[item.iconCls]:true, [`level-${item.level}`]: true}"
  >
    <template v-if="render">
      <render-node
        :render="render"
        :item="item"/>
    </template>
    <template v-else>
      {{item.name}}
    </template>
  </li>
</template>
<script>
import RenderNode from './renderNode';
export default {
  name: 'VirtualTreeNode',
  components: {
    RenderNode
  },
  props: {
    item: Object,
    render: Function
  }
}
</script>
<style scoped>
.node-item {
  padding-left: 48px;
}
.node-item.level-1 {
  padding-left: 0;
}
.node-item.level-2 {
  padding-left: 16px;
}
.node-item.level-3 {
  padding-left: 32px;
}
.node-item.level-4 {
  padding-left: 48px;
}
</style>