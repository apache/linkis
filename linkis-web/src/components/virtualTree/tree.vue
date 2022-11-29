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
  <virtual-list
    ref="virtualList"
    wtag="ul"
    class="tree-list-wrap"
    :size="size"
    :remain="remain"
    :stat="start"
    :item="item"
    :itemcount="total"
    :itemprops="getItemprops"/>
</template>
<script>
/**
 * virtual tree component(虚拟树组件)
 * Pass in tree data, flatten the interior and render it with a virtual scrolling list(传入树形数据，内部扁平化并使用虚拟滚动列表渲染)
 * You can customize the render function to control node rendering(可以自定义render函数控制节点渲染)
 * Node isVisible===false does not render(节点isVisible===false则不渲染)
 * ! Each node requires a unique key value identification field, which can be configured through keyText(每个节点需要唯一键值标识字段，可以通过keyText配置)
 * ! The height must be set to calculate the number of virtual scroll rendering bars(必须设置height用于计算虚拟滚动渲染条数)
 * Example(示例)
 * <virtual-tree
      :list="treeData"
      :render="renderNode"
      :open="openNode"
      :height="height"
      @we-click="onClick"
      @we-contextmenu="onContextMenu"
      @we-open-node="openNodeChange"
      @we-dblclick="handledbclick"/>

 * list为树形数据 [{
            _id: 'xxxx',
            name: 'hive',
            iconCls: 'md-arrow-dropright',
            children: [...]
        }]
 */
import virtualList from '@/components/virtualList';
import item from './item';
export default {
  name: 'VirtualTree',
  components: {
    virtualList,
  },
  props: {
    list: {
      type: Array,
      default: () => [],
    },
    render: { // custom node rendering(自定义节点渲染)
      type: Function
    },
    open: Object, // 展开节点key eg: {xxxx:true,xx:true}
    size: { // row height(行高)
      type: Number,
      default: 24
    },
    height: { // tree height(树高度)
      type: Number,
      require: true
    },
    keyText: { // Node data unique id(节点数据唯一id)
      type: String,
      default: '_id'
    }
  },
  watch: {
    list() {
      this.refresData()
    }
  },
  data() {
    return {
      total: 0,
      remain: 0,
      item,
      start: 0
    };
  },
  methods: {
    clickNode(index) {
      let item = this.showData[index];
      let now = Date.now();
      // double click(双击)
      if (this.lastClick === item[this.keyText] && (now - this.lastClickTime || now) < 300) {
        return
      }
      this.lastClickTime = Date.now();
      this.lastClick = item[this.keyText];
      let openNode = {...this.open}
      if (!item.isLeaf) {
        if (openNode[item[this.keyText]]) {
          delete openNode[item[this.keyText]]
        } else {
          openNode[item[this.keyText]] = true
        }
        this.$emit('we-open-node', openNode)
      }
      if (item.loaded) {
        this.refresData(openNode)
      }
      this.$emit('we-click', {item})
    },
    dbClickNode(index) {
      let item = this.showData[index];
      this.$emit('we-dblclick', {item})
    },
    contextmenu(ev, index){
      let item = this.showData[index];
      this.$emit('we-contextmenu', {ev, item})
    },
    refresData(openNode) {
      let showData = []
      openNode = openNode || {...this.open}
      // Tree data flattening(树形数据扁平化)
      let formatTree = (list, level = 1) => {
        list.forEach(item => {
          // After flattening the data, the list only retains the data required for rendering(扁平处理数据后列表只保留渲染所需数据)
          // isVisible false does not render(不渲染)
          if (item.isVisible !== false) {
            showData.push({...item, level, isLeaf: !item.children, children: []});
            if (item.children && item.children.length) {
              if (openNode[item[this.keyText]]) {
                formatTree(item.children, level + 1)
              }
            }
          }
        })
      }
      formatTree(this.list)
      this.showData = showData
      this.total = showData.length
      let rows
      if (this.height) {
        rows =  Math.floor(this.height / this.size)
      }
      this.remain =  this.showData.length > rows ? rows : this.showData.length
      this.$refs.virtualList.forceRender();
    },
    getItemprops(index) {
      return {
        key: index,
        props: {
          item: this.showData[index],
          render: this.render
        },
        nativeOn: {
          click: (ev) => {
            ev.stopPropagation()
            this.clickNode(index)
          },
          dblclick: (ev)=>{
            ev.stopPropagation()
            ev.preventDefault()
            this.dbClickNode(index)
          },
          contextmenu: (ev)=> {
            ev.stopPropagation()
            ev.preventDefault()
            this.contextmenu(ev, index)
          }
        }
      }
    }
  }
};
</script>
