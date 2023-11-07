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
  <div class="icon-content">
    <ul>
      <li v-for="item in list" :key="item" @click="copy(item)" class="icon-style">
        <SvgIcon icon-class="common"/>
        <div>{{item}}</div>
      </li>
    </ul>
  </div>
</template>
<script>
const req = require.context('@/components/svgIcon/svg', true, /\.svg$/);
const reg = /.*\/(.*)\.svg/;
const list = req.keys().map(item => {
  reg.test(item);
  return RegExp.$1;
})
export default {
  data() {
    return {
      list,
    }
  },
  methods: {
    copy(){
      let inputEl = document.createElement('input');
      inputEl.value = `<SvgIcon style="font-size: 1rem;" color="#444444" icon-class="common"/>`;
      document.body.appendChild(inputEl);
      inputEl.select(); // select object(选择对象);
      document.execCommand("Copy"); // Execute the browser copy command(执行浏览器复制命令)
      this.$Message.info('复制成功');
      inputEl.remove();
    },
  }
};
</script>
<style lang="scss" scoped>
  .icon-style {
    background: #9c9c9c;
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: center;
    margin: 5px;
    width: calc(10% - 10px);
    font-size: 2rem;
    color: #444444
  }
  .icon-style div {
    font-size: 0.8rem;
  }
</style>
