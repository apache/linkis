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
  <Form :model="searchBar" :rules="ruleInline" inline>
    <FormItem prop="instance" :label="`${$t('message.linkis.instanceName')}`">
      <Input :maxlength="50" v-model="searchBar.instance" :placeholder="$t('message.linkis.instanceName')"/>
    </FormItem>
    <FormItem prop="owner" :label="$t('message.linkis.initiator')" >
      <Input :maxlength="50" v-model="searchBar.owner" style="width:100px" clearable :placeholder="$t('message.linkis.inputOwnerHint')"/>
    </FormItem>
    <FormItem prop="shortcut" :label="$t('message.linkis.formItems.date.label')">
      <DatePicker
        :transfer="true"
        class="datepicker"
        :options="shortcutOpt"
        v-model="searchBar.shortcut"
        type="daterange"
        placement="bottom-start"
        format="yyyy-MM-dd"
        :placeholder="$t('message.linkis.formItems.date.placeholder')"
        style="width: 190px"
        :editable="false"
      />
    </FormItem>
    <FormItem prop="engineType" :label="$t('message.linkis.tableColumns.engineType')" >
      <Select  v-model="searchBar.engineType" style="width:80px" clearable>
        <Option
          v-for="(item) in engineTypes"
          :label="item"
          :value="item"
          :key="item"/>
      </Select>
    </FormItem>
    <FormItem prop="status" :label="$t('message.linkis.tableColumns.status')" >
      <Select  v-model="searchBar.status" style="width:100px" clearable>
        <Option
          v-for="(item) in statusList"
          :label="item"
          :value="item"
          :key="item"/>
      </Select>
    </FormItem>
    <FormItem>
      <Button type="primary" @click="search(false)">
        {{ $t('message.linkis.search') }}
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
    engineTypes: {
      type: Array,
      default: () => []
    },
    stopbtn: {
      type: Boolean
    },
    page: {
      type: Object
    }
  },
  data() {
    const today = new Date(new Date().toLocaleDateString())
    return {
      ruleInline: {},
      searchBar: {
        instance: "",
        engineType: "",
        owner: "",
        shortcut: [today, today],
        status: "",
      },
      shortcutOpt: {
        shortcuts: [
          {
            text: this.$t('message.linkis.shortcuts.week'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              return [start, end]
            }
          },
          {
            text: this.$t('message.linkis.shortcuts.month'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
              return [start, end]
            }
          },
          {
            text: this.$t('message.linkis.shortcuts.threeMonths'),
            value() {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
              return [start, end]
            }
          }
        ]
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
    search(isChangingPage) {
      if (!isChangingPage) this.page.pageNow = 1
      this.searchBar.shortcut[0] = this.searchBar.shortcut[0] ? new Date(this.searchBar.shortcut[0].setHours(0, 0, 0, 0)) : this.searchBar.shortcut[0]
      this.searchBar.shortcut[1] = this.searchBar.shortcut[1] ? new Date(this.searchBar.shortcut[1].setHours(23, 59, 59, 0)): this.searchBar.shortcut[1]
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
