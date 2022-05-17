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
    class="waterMask" 
    ref="root">
    <canvas 
      ref="single" 
      class="single"/>
    <canvas 
      ref="repeat" />
  </div>
</template>
<script>
export default {
  props: {
    text: {
      type: String,
      default: '', 
    },
  },
  data() {
    return {
      single: null,
      repeat: null,
      root: null,
    };
  },
  mounted() {
    this.single = this.$refs.single;
    this.repeat = this.$refs.repeat;
    this.root = this.$refs.root;
    this.updateCanvas(this.text);
  },
  methods: {
    generateSingle(text) {
      const w = 400;
      const h = 100;
      this.single.width = w;
      this.single.height = h;
      this.single.style.width = w + 'px';
      this.single.style.height = h + 'px';
      const singleCtx = this.single.getContext('2d');
      singleCtx.clearRect(0, 0, w, h);
      singleCtx.font = '12px 宋体';
      singleCtx.rotate(-10 * Math.PI / 180);
      singleCtx.fillStyle = 'rgba(0,0,0,0.2)';
      singleCtx.fillText(text, 10, 85); 
    },
    generateRepeat() {
      const root = this.root;
      const w = root.clientWidth;
      const h = root.clientHeight;
      const repeat = this.repeat;
      repeat.width = w;
      repeat.height = h;
      const repeatCtx = repeat.getContext('2d');
      repeatCtx.clearRect(0, 0, w, h);
      const pat = repeatCtx.createPattern(this.single, 'repeat'); 
      repeatCtx.fillStyle = pat;  
      repeatCtx.fillRect(0, 0, w, h);
    },
    updateCanvas(text) {
      this.generateSingle(text);
      this.generateRepeat();
    },
  },
};
</script>
<style src="./index.css"></style>
