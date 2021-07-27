<template>
  <div class="datasource-type-wrap">

    <div class="classifier" v-for="(item, name) in sourceType" :key="name">
      <h3 class="project-header">
        <span class="header-title" >{{name}}</span>
      </h3>

      <div class="resource-item" v-for="sourceItem in item" :key="sourceItem.name"
        @click="onSelected(sourceItem)">
        <img class="resource-img" :src="sourceItem.icon"/>
        <span class="resource-name">{{sourceItem.name}}</span>
      </div>
    </div>

    <Spin size="large" fix v-if="loading"></Spin>
    
  </div>
</template>

<script>
export default {
  props: {
    onSelected: Function,
    data: Array
  },
  methods: {
    transformTypeData(data){
      const tempObj = {};
      data.forEach(item=>{
        if(!tempObj[item.classifier]){
          tempObj[item.classifier] = [];
        }
        tempObj[item.classifier].push(item)
      })
      this.sourceType = tempObj;
    }
  },
  computed: {
    sourceType: function(){
      const tempObj = {};
      this.data.forEach(item=>{
        if(!tempObj[item.classifier]){
          tempObj[item.classifier] = [];
        }
        tempObj[item.classifier].push(item)
      })
      return tempObj;
    }
  },
  data() {
    return {
      loading: false
    }  
  },
}
</script>

<style lang="scss" scoped src="./index.scss"></style>