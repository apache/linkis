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
  <div class="we-tag">
    <div
      class="we-tag-content"
      v-for="(item, index) in formattedTag"
      :key="index">
      <span class="we-input-label">{{item}}</span>
      <Icon
        type="ios-close"
        @click.stop="handleDelete(item)"
        class="we-tag-content-close"></Icon>
    </div>
    <div>
      <button
        class="we-tag-add-btn"
        @click.stop="changeAddTag"
        v-if="!isAddInputShow">{{newLabel}}</button>
        
      <input
        v-else
        ref="addInput"
        class="we-tag-add-input"
        v-model="addLabel"
        type="text"
        @blur="handleBlur"
        @keyup.enter.stop.prevent="handleEnter"
      />
    </div>
  </div>
</template>
<script>
export default {
  name: 'weTag',
  props: {
    tagList: String,
    newLabel: {
      type: String,
      default: '新增标签',
    },
  },
  data() {
    return {
      isAddInputShow: false,
      addLabel: '',
    }
  },

  computed: {
    formattedTag() {
      if (this.tagList) {
        return this.tagList.split(',');
      }
      return [];
    }
  },
  methods: {
    changeAddTag() {
      this.isAddInputShow = true;
      this.$nextTick(() => {
        this.$refs.addInput.focus();
      })
    },
    handleBlur() {
      if (this.addLabel) {
        this.$emit('add-tag', this.addLabel);
      }
      this.isAddInputShow = false;
      this.addLabel = '';
    },
    handleEnter() {
      this.isAddInputShow = false;
    },
    handleDelete(item) {
      this.$emit('delete-tag', item);
    },

  }
}
</script>
<style lang="scss" src="./index.scss">
</style>
