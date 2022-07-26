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
  <Modal
    v-model="show"
    width="360"
    class-name="delete-modal">
    <p
      slot="header"
      class="delete-modal-header">
      <Icon type="ios-information-circle"/>
      <span>{{$t('message.common.deleteDialog.waring')}}</span>
    </p>
    <div class="delete-modal-content">
      <p>
        <span>{{ label }}</span>
        <span class="delete-modal-content-type">{{ type }}</span>
        <span class="delete-modal-content-name">{{ name }}</span>
      </p>
      <p>{{$t('message.common.deleteDialog.isNext')}}</p>
    </div>
    <div slot="footer">
      <Button
        :loading="loading"
        type="error"
        size="large"
        long
        @click="del">{{ [$t('message.common.deleteDialog.engine'), $t('message.common.deleteDialog.task'), $t('message.common.deleteDialog.engineAndTask')].indexOf(type) !== -1 ? $t('message.common.deleteDialog.action', {type}) : $t('message.common.delete') }}</Button>
    </div>
  </Modal>
</template>
<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: false,
      type: '',
      name: '',
    };
  },
  computed: {
    label() {
      return [this.$t('message.common.deleteDialog.engine'), this.$t('message.common.deleteDialog.task'), this.$t('message.common.deleteDialog.engineAndTask')].indexOf(this.type) !== -1 ? this.$t('message.common.deleteDialog.overThe') : this.$t('message.common.delete');
    },
  },
  watch: {
    'loading': function(val) {
      if (!val) {
        this.show = false;
      }
    },
  },
  methods: {
    open(opt) {
      this.show = true;
      let { type, name } = opt;
      this.type = type;
      this.name = name;
    },
    close() {
      this.show = false;
    },
    del() {
      this.$emit('delete', this.type);
    },
  },
};
</script>
<style lang="scss" src="./index.scss">
</style>
