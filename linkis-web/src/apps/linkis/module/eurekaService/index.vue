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
  <div class="eurekaService">
    <Spin
      v-if="loading"
      size="large"
      fix
    />
    <Row class="search-bar">
      <Col span="5" class="search-item">
        <span class="lable">{{ $t('message.linkis.serviceName') }}</span>
        <Input
          v-model="app"
          class="input"
          :placeholder="$t('message.linkis.serviceName')"
          @on-enter="searchAction"
        ></Input>
      </Col>
      <Col
        span="5"
        class="search-item"
      >
        <span class="lable">{{
          $t("message.linkis.IP")
        }}</span>
        <Input
          v-model="ip"
          class="input"
          :placeholder="$t('message.linkis.IP')"
          @on-enter="searchAction"
        ></Input>
      </Col>
      <Col
        span="5"
        class="search-item"
      >
        <span class="lable">{{
          $t("message.linkis.hostname")
        }}</span>
        <Input
          v-model="hostName"
          class="input"
          :placeholder="$t('message.linkis.hostname')"
          @on-enter="searchAction"
        ></Input>
      </Col>
      <Col
        span="5"
        class="search-item"
      >
        <span class="lable">{{
          $t("message.linkis.versionInfo")
        }}</span>
        <Input
          v-model="version"
          class="input"
          :placeholder="$t('message.linkis.versionInfo')"
          @on-enter="searchAction"
        ></Input>
      </Col>
      <Col span="4" class="search-item">
        <Button class="search" type="primary" @click="searchAction">{{
          $t("message.linkis.find")
        }}</Button>
      </Col>
    </Row>
    <Table
      border
      :columns="tableColumnNum"
      :data="pageDatalist"
      class="table-content eureka-service-table"
    >
      <template
        slot-scope="{ row }"
        slot="serviceAddress"
      >
        <span>{{ row.ipAddr + ':' + row.metadata['management.port'] }}</span>
      </template>
      <template
        slot-scope="{ row }"
        slot="registryTime"
      >
        <span>{{ dateFormatter(row.leaseInfo ? row.leaseInfo.registrationTimestamp : '') }}</span>
      </template>
      <template
        slot-scope="{ row }"
        slot="versionInfo"
      >
        <span>{{ row.metadata ? row.metadata['linkis.app.version'] || '--' : '--' }}</span>
      </template>
    </Table>
    <div class="page-bar">
      <Page
        ref="page"
        :total="page.totalSize"
        :page-size-opts="page.sizeOpts"
        :page-size="page.pageSize"
        :current="page.pageNow"
        class-name="page"
        size="small"
        show-total
        show-sizer
        :prev-text="$t('message.linkis.previousPage')"
        :next-text="$t('message.linkis.nextPage')"
        @on-change="change"
        @on-page-size-change="changeSize"
      />
    </div>
  </div>
</template>
<script>
import api from "@/common/service/api";
import moment from "moment";
export default {
  name: 'microServiceManagement',
  components: {},
  data() {
    return {
      loading: false,
      app: "",
      ip: "",
      hostName: "",
      version: "",
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1,
      },
      tableColumnNum: [
        {
          title: this.$t("message.linkis.serviceName"),
          key: "app",
          align: "center",
          minWidth: 120,
        },
        {
          title: this.$t("message.linkis.tableColumns.serviceAddress"),
          key: "serviceAddress",
          slot: "serviceAddress",
          align: "center",
          minWidth: 100,
        },
        {
          title: this.$t("message.linkis.tableColumns.hostname"),
          key: "hostName",
          align: "center",
          minWidth: 100,
        },
        {
          title: this.$t("message.linkis.tableColumns.status"),
          key: "status",
          align: "center",
          minWidth: 120,
        },
        {
          title: this.$t("message.linkis.tableColumns.registryTime"),
          key: "registryTime",
          slot: "registryTime",
          align: "center",
          minWidth: 100,
        },
        {
          title: this.$t("message.linkis.tableColumns.versionInfo"),
          key: "versionInfo",
          slot: "versionInfo",
          align: "center",
          minWidth: 120,
        },
      ],
      tableData: [], // data for presentation(用于展示的数据)
      allTableData: [], // All data temporarily stored for filtering(暂存的所有数据，用于做筛选)
    };
  },
  computed: {
    pageDatalist() {
      // Displayed data(展示的数据)
      return this.tableData.filter((item, index) => {
        return (
          (this.page.pageNow - 1) * this.page.pageSize <= index &&
          index < this.page.pageNow * this.page.pageSize
        );
      });
    },
  },
  created() {
    this.getAllInstance();
  },
  methods: {
    // Get all microservices(获取所有的微服务)
    getAllInstance() {
      this.loading = true;
      api.fetch("/microservice/serviceInstances", "get").then((res) => {
        this.allTableData = res.list.map(item=> item.instanceInfo) || [];
        this.tableData = res.list.map(item=> item.instanceInfo)  || [];
        this.page.totalSize = this.tableData.length;
        this.loading = false;
      });
    },
    searchAction() {
      this.search(this.app.replace(/ /g, '') || '', this.ip.replace(/ /g, '') || '', this.hostName.replace(/ /g, '') || '', this.version.replace(/ /g, '') || '');
    },
    // Click to search to filter the data(点击搜索 对数据进行筛选)
    search(app = "", ip = "", hostName = "", version = "") {
      this.loading = true;
      api.fetch("/microservice/serviceInstances", { serviceName: app, ip, hostName, version }, "get").then((res) => {
        this.allTableData = res.list.map(item => item.instanceInfo) || [];
        this.tableData = res.list.map(item => item.instanceInfo) || [];
        this.page.totalSize = this.tableData.length;
        this.loading = false;
      });
      this.page.pageNow = 1;
    },
    // Toggle pagination(切换分页)
    change(val) {
      this.page.pageNow = val;
    },
    // page size change(页容量变化)
    changeSize(val) {
      this.page.pageSize = val;
      this.page.pageNow = 1;
    },
    // Convert time format(转换时间格式)
    dateFormatter(date) {
      return date ? moment(date).format("YYYY-MM-DD HH:mm:ss") : '--';
    },
  },
};
</script>
<style lang="scss" src="./index.scss" scoped></style>
  
<style lang="scss">

.eureka-service-table {
  border: 0;
  height: calc(100% - 110px);

  .ivu-table:before {
    height: 0
  }

  .ivu-table:after {
    width: 0
  }

  .ivu-table {
    height: auto;
    border: 1px solid #dcdee2;
  }
}

</style>
  