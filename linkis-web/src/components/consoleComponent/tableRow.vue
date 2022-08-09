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
    width="700"
    class="table-row">
    <div
      slot="header">
      <div class="table-row-header">
        <span>{{$t('message.common.tableRow.detail')}}</span>
        <Input
          size="small"
          class="table-row-input"
          v-model="searchText"
          :placeholder="$t('message.common.tableRow.search')"></Input>
      </div>
    </div>
    <div>
      <Table
        class="table-row-table"
        stripe
        :columns="columns"
        :data="filterRow"
        :max-height="500"></Table>
    </div>
    <div slot="footer">
    </div>
  </Modal>
</template>
<script>
import { debounce } from 'lodash';
export default {
  props: {
    row: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      show: false,
      columns: [{
        type: 'index',
        width: 60,
        align: 'center',
      }, {
        title: this.$t('message.common.tableRow.columnName'),
        key: 'columnName',
      }, {
        title: this.$t('message.common.tableRow.value'),
        key: 'value',
      }, {
        title: this.$t('message.common.tableRow.dataType'),
        key: 'dataType',
      }],
      formattedRow: [],
      filterRow: [],
      searchText: '',
    };
  },
  watch: {
    searchText(val) {
      if (val) {
        this.filter(this);
      } else {
        this.filterRow = this.formattedRow;
      }
    },
  },
  methods: {
    open() {
      this.show = true;
      this.format();
    },
    format() {
      this.formattedRow = [];
      this.filterRow = this.formattedRow = this.row;
    },
    filter: debounce((that) => {
      const regexp = new RegExp(`.*${that.searchText}.*`);
      that.filterRow = that.formattedRow.filter((item) => {
        return regexp.test(item.columnName) || regexp.test(item.value) || regexp.test(item.dataType);
      });
    }, 500),
  },
};
</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
  .table-row {
    .table-row-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-right: 26px;
      padding-left: 6px;
      .table-row-input {
        width: 200px;
        float: right;
      }
    }
    .table-row-table {
      .ivu-table th {
        background-color: $table-thead-blue-bg;
        color: $body-background;
      }
      .ivu-table .is-null {
        color: $error-color;
        font-style: italic;
      }
    }
  }
</style>

