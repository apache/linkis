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
  <div
    class="circle-progress"
    :style="{'width': width, 'height': height}">
    <svg viewBox="0 0 104 104">
      <path
        d="M 52 52 m 0 -47 a 47 47 0 1 1 0 94 a 47 47 0 1 1 0 -94"
        class="circle-progress__track">
      </path>
      <path
        d="M 52 52 m 0 -47 a 47 47 0 1 1 0 94 a 47 47 0 1 1 0 -94"
        class="circle-progress__path"
        :style="{'stroke-dashoffset': (1-percent)*299.08+'px'}">
      </path>
    </svg>
    <span class="circle-progress__perent">{{ formattedPercent }}%</span>
    <span class="circle-progress__text">{{ used }}{{ suffixe }}/{{ max }}{{ suffixe }}</span>
    <span class="circle-progress__title">{{ title }}</span>
  </div>
</template>
<script>
export default {
  props: {
    percent: Number,
    used: String,
    max: String,
    suffixe: String,
    title: String,
    width: String,
    height: String,
  },
  computed: {
    formattedPercent() {
      return (this.percent * 100).toFixed(0);
    },
  },
};
</script>
<style lang="scss" src="./index.scss">

</style>
