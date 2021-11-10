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
  <div class="we-search-wrap">
    <input
      ref="searchbox"
      :value="currentValue"
      :placeholder="$t('message.common.tableRow.search')"
      type="text"
      @input="$emit('input', $event.target.value)"
      @blur="$emit('on-blur', $event.target.value)" >
  </div>
</template>
<script>

export default {
  name: 'WeSearchBox',
  props: {
    value: String,
  },
  data() {
    return {
      currentValue: this.value,
    };
  },
  watch: {
    value(val) {
      this.currentValue = val;
    },
  },
  methods: {
    onfocus() {
      this.$refs.searchbox.focus();
    },
  },
};
</script>
