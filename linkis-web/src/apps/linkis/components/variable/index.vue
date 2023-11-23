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
    v-if="variable"
    class="we-variable">
    <div class="we-variable-header">
      <div>
        <div class="we-variable-header-title">
          <span>{{ variable.name }}</span>
          <span>({{ variable.settings.length }})</span>
        </div>
        <span
          class="we-variable-header-control"
          @click="handleControl">{{ controlLabel }}</span>
      </div>
    </div>
    <div
      v-show="!isHide"
      class="we-variable-content">
      <template v-for="(item, index) in variable.settings">
        <div
          :key="index"
          :title="`${item.description}${item.specialLimit ? $t('message.linkis.setting.maxLimit', { limit: item.specialLimit.maxValue }) : ''}`"
          class="we-variable-content-item"
          v-if="getItemIsShow(item)">
          <span class="we-variable-content-label-group">
            <span>{{ item.name }}</span>
            <span class="we-variable-content-label-key">[{{ item.key }}]</span>
          </span>
          <Select
            v-if="item.validateType === 'OFT'"
            class="iview-select"
            :placeholder="item.defaultValue ? `${$t('message.linkis.defaultValue')}:${item.defaultValue}` : $t('message.linkis.noDefaultValue')"
            v-model="item.configValue">
            <!-- 返回 validateRange  为json字符串，转义-->
            <Option
              v-for="(validateItem, i) in JSON.parse(item.validateRange)"
              :key="i"
              :value="validateItem">{{ validateItem }}</Option>
          </Select>
          <textarea
            v-model="item.configValue"
            :placeholder="item.defaultValue ? `${$t('message.linkis.defaultValue')}:${item.defaultValue}` : $t('message.linkis.noDefaultValue')"
            class="two-lines we-variable-content-input"
            :class="{'un-valid': unValid && unValid.key === item.key}"
            v-else-if="item.key === 'spark.conf'">
         </textarea>
          <input
            v-model="item.configValue"
            :placeholder="item.defaultValue ? `${$t('message.linkis.defaultValue')}:${item.defaultValue}` : $t('message.linkis.noDefaultValue')"
            type="text"
            class="we-variable-content-input"
            :class="{'un-valid': unValid && unValid.key === item.key}"
            v-else>
          <span
            v-if="unValid && unValid.key === item.key"
            class="we-warning-bar">{{ unValid.msg }}</span>
        </div>
      </template>
    </div>
    <!--<div
      v-show="!isHide"
      class="we-variable-bottom">
      <div @click="add">
        <Icon
          type="ios-add"
          color="#2d8cf0"
          size="20"/>
        <span class="we-variable-bottom-add">{{ $t('message.linkis.addVariable') }}</span>
      </div>
    </div>
    -->
  </div>
</template>
<script>
export default {
  name: 'variable',
  props: {
    variable: Object,
    unValidMsg: Object,
    isAdvancedShow: Boolean,
  },
  data() {
    return {
      isHide: false,
      controlLabel: this.$t('message.linkis.fold'),
      unValid: null,
    };
  },
  watch: {
    unValidMsg(val) {
      this.setUnValidMsg(val);
    },
  },
  methods: {
    handleControl() {
      this.isHide = !this.isHide;
      this.controlLabel = this.isHide ? this.$t('message.linkis.unfold') : this.$t('message.linkis.fold');
    },
    add() {
    },
    handleOk(item) {
      this.$emit('add-item', item, this.variable, () => {
        item.isNew = false;
      });
    },
    handleDelete(item) {
      this.$emit('remove-item', item, this.variable, () => {
      });
    },
    setUnValidMsg({ key, msg }) {
      this.unValid = {
        key,
        msg,
      };
    },
    getItemIsShow(item) {
      if (item.hidden) {
        return !item.hidden;
      }
      if (item.advanced && this.isAdvancedShow) {
        return true;
      } else if (item.advanced && !this.isAdvancedShow) {
        return false;
      }
      return true;
    },
  },
};
</script>
<style lang="scss" src="./index.scss">
</style>
