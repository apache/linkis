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
  <Form :model="searchBar" :rules="ruleInline" inline @submit.native.prevent>
    <FormItem prop="instance" :label="`${$t('message.linkis.instanceName')}`">
      <Input :maxlength="50" v-model="searchBar.instance" style="width:80px" :placeholder="$t('message.linkis.instanceName')"/>
    </FormItem>
    <FormItem prop="nodeHealthy" :label="$t('message.linkis.formItems.status.label')">
      <Select v-model="searchBar.nodeHealthy" style="width:120px" clearable>
        <Option
          v-for="(item) in statusList"
          :label="item"
          :value="item"
          :key="item"/>
      </Select>
    </FormItem>
    <FormItem prop="owner" :label="$t('message.linkis.initiator')" >
      <Select  v-model="searchBar.owner" style="width:120px" clearable>
        <Option
          v-for="(item) in ownerList"
          :label="item"
          :value="item"
          :key="item"/>
      </Select>
    </FormItem>
    <FormItem v-if="engineTypes.length" prop="engineType" :label="$t('message.linkis.tableColumns.engineType')" >
      <Select  v-model="searchBar.engineType" style="width:120px" clearable>
        <Option
          v-for="(item) in engineTypes"
          :label="item"
          :value="item"
          :key="item"/>
      </Select>
    </FormItem>
    <FormItem prop="tenant" :label="$t('message.linkis.tenant')" >
      <Input :maxlength="50" v-model="searchBar.tenant" style="width:120px" :placeholder="$t('message.linkis.inputTenant')"/>
    </FormItem>
    <FormItem>
      <Button type="primary" @click="search">
        {{ $t('message.linkis.search') }}
      </Button>
      <Button v-if="stopbtn" type="error" @click="stop"  style="margin-left:20px">
        {{ $t('message.linkis.stop') }}
      </Button>
    </FormItem>
  </Form>

</template>
<script>

export default {
  components: {
  },
  props: {
    statusList: {
      type: Array,
      default: () => []
    },
    ownerList: {
      type: Array,
      default: () => []
    },
    engineTypes: {
      type: Array,
      default: () => []
    },
    stopbtn: {
      type: Boolean
    }
  },
  data() {
    return {
      ruleInline: {},
      searchBar: {
        instance: "",
        nodeHealthy: "",
        owner: "",
        tenant: "",
      },
    };
  },
  computed: {
  },
  created() {

  },
  mounted() {

  },
  activated() {
  },
  methods: {
    search() {
      this.$emit("search", this.searchBar)
    },
    stop() {
      this.$emit("stop")
    }
  },
};
</script>
<style lang="scss" scoped>
  .ivu-form {
    display: flex;
    .ivu-form-item {
      display: flex;
      margin-right: 8px;
      margin-bottom: 8px;
      flex: none;
    }
  }
</style>
