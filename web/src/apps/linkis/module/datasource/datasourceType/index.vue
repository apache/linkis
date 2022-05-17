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