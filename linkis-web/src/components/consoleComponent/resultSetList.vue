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
    class="set ivu-select ivu-select-single ivu-select-small"
    v-clickoutside="handleOutsideClick">
    <div
      tabindex="0"
      @click.stop="showList"
      class="ivu-select-selection">
      <div class="">
        <span class="ivu-select-selected-value">{{$t('message.common.resultList')}}{{ +current + 1 }}</span>
        <i class="ivu-icon ivu-icon-ios-arrow-down ivu-select-arrow"></i>
      </div>
    </div>
    <div
      v-if="show"
      class="ivu-select-dropdown"
      x-placement="bottom-start"
      @click.stop="changeSet">
      <virtual-list
        class="ivu-select-dropdown-list list"
        v-if="list.length>1"
        ref="vsl"
        :size="18"
        :remain="list.length > 8 ? 8 : list.length"
        wtag="ul"
        style="overflow-x:hidden">
        <li
          v-for="(item, index) in list"
          :class="{current: current-0 === index}"
          :data-index="index"
          :key="item.path">{{$t('message.common.resultList')}}{{ index + 1 }}</li>
      </virtual-list>
    </div>
  </div>
</template>
<script>
import virtualList from '@/components/virtualList';
import clickoutside from '@/common/helper/clickoutside';
export default {
  name: 'ResultSetList',
  directives: {
    clickoutside,
  },
  components: {
    virtualList,
  },
  props: {
    list: {
      type: Array,
      default: () => [],
    },
    current: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      resultList: this.list.sort((a, b) => {
        return +(a.name.split('.')[0].substring(1)) - +(b.name.split('.')[0].substring(1))
      }),
      show: false,
    };
  },
  watch: {
    list(v) {
      this.resultList = v || [];
    },
  },
  methods: {
    changeSet(e) {
      if (e.target) {
        let index = e.target.getAttribute('data-index');
        if (index) {
          this.$emit('change', index);
          this.show = false;
        }
      }
    },
    showList() {
      this.show = !this.show;
    },
    handleOutsideClick() {
      this.show = false;
    },
  },
};
</script>
<style lang="scss" scoped>
.set {
  position: relative;
}
.ivu-select-dropdown {
  min-width: 90px;
  position: absolute;
  will-change: top, left, transform;
  transform-origin: center top;
  top: -5px;
  left: 0px;
  transform: translate(0, -100%);
  z-index: 9999;
  overflow: hidden;
}
.list {
  font-size: 12px;
  height:18px;
  line-height: 18px;
  li {
    padding-left: 8px;
  }
  li:hover {
    background-color: #e9e9e9;
  }
  .current{
    color: #2d8cf0;
  }
}
</style>
