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
    class="list-view"
    ref="list">
    <vuescroll
      ref="vuescroll"
      :ops="ops"
      @handle-scroll="handleScroll">
      <list-body
        ref="body"
        :cache="cache"
        :item-size-getter="itemSizeGetter"
        :estimated-item-size="30"
        :data="data"
        :header="header"
        @active="changeActive"
        type="body">
        <div
          slot-scope="{col}">
          <div
            v-for="(content, contentIndex) in col"
            :title="content || contentIndex"
            :key="contentIndex"
            class="we-column-item"
            :class="{'null-text': content === 'NULL', 'active': contentIndex === activeRowIndex}"
            @click.stop="columnItemClick(contentIndex)">
            <span class="we-column-item-content">{{ content }}</span>
          </div>
        </div>
      </list-body>
    </vuescroll>
  </div>
</template>
<script>
import listBody from './body.vue';
import vuescroll from 'vuescroll/dist/vuescroll-native';
export default {
  components: {
    vuescroll,
    listBody,
  },
  props: {
    cache: {
      type: Object,
      default: () => {
        return {};
      },
    },
    data: {
      type: Array,
      required: true,
    },
    header: {
      type: Array,
      required: true,
    },

    estimatedItemSize: {
      type: Number,
      default: 30,
    },

    itemSizeGetter: {
      type: Function,
    },
  },
  data() {
    return {
      ops: {
        bar: {
          background: '#cdcdcd',
          keepShow: true,
          minSize: 0.05,
        },
        rail: {
          size: '10px',
          opacity: 1,
          background: '#fff',
        },
        scrollButton: {
          enable: true,
          background: '#cecece',
        },
      },
      activeRowIndex: null,
    };
  },
  methods: {
    handleScroll(v, h) {
      this.$emit('on-scroll', { v, h });
      this.$refs.body.handleScroll(v, h);
    },
    columnItemClick(index) {
      this.activeRowIndex = index;
      this.$emit('on-click', index);
    },
    changeActive() {
      this.activeRowIndex = null;
    }
  },
};
</script>
