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
  <div class="microService">
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <Row class="search-bar">
      <Col span="9">
        <span :style="{minWidth: '80px', marginRight: '8px', fontSize: '14px', lineHeight: '32px'}">{{$t('message.linkis.instanceName')}}</span>
        <Input
          v-model="instance"
          suffix="ios-search"
          class="input"
          :placeholder="$t('message.linkis.instanceName')"
          @on-enter="searchAction"
        ></Input>
      </Col>
      <Col span="9" class="search-item">
        <span class="lable">{{
          $t("message.linkis.tableColumns.engineType")
        }}</span>
        <Select v-model="applicationName" class="input">
          <Option v-for="item in typeList" :key="item" :value="item">{{
            item
          }}</Option>
        </Select>
      </Col>
      <Col span="6" class="search-item">
        <Button class="search" type="primary" @click="searchAction">{{
          $t("message.linkis.find")
        }}</Button>
        <!-- 跳转 -->
        <Button class="jump" type="primary" @click="handleTabsJump">{{ $t("message.linkis.serviceRegistryCenter") }}</Button>
      </Col>
    </Row>
    <Table
      border
      :columns="tableColumnNum"
      :data="pageDatalist"
      class="table-content micro-service-table"
    >
      <template slot-scope="{ row }" slot="instance">
        <span>{{ row.instance }}</span>
      </template>
      <template slot-scope="{ row }" slot="updateTime">
        <span>{{ dateFormatter(row.updateTime) }}</span>
      </template>
      <template slot-scope="{ row }" slot="createTime">
        <span>{{ dateFormatter(row.createTime) }}</span>
      </template>
      <template slot-scope="{ row }" slot="labels">
        <div class="tag-box">
          <Tooltip
            v-for="(item, index) in row.labels"
            :key="index"
            :content="`${item.stringValue}`"
            placement="top"
          >
            <Tag class="tag-item" type="border" color="primary">{{
              `${item.stringValue}`
            }}</Tag>
          </Tooltip>
        </div>
      </template>
      <template slot-scope="{ row, index }" slot="action">
        <Button
          type="primary"
          size= 'small'
          @click="modify(row, index)"
        >{{ $t("message.linkis.edit") }}</Button
        >
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
        :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
        @on-change="change"
        @on-page-size-change="changeSize"
      />
    </div>
    <Modal
      v-model="modifyShow"
      :title="$t('message.linkis.edit')"
      @on-ok="modifyOk"
      :ok-text="$t('message.common.ok')"
    >
      <Form v-model="modifyData" :label-width="100">
        <FormItem :label="`${$t('message.linkis.instanceName')}`">
          <Input disabled v-model="modifyData.instance"></Input>
        </FormItem>
        <FormItem
          class="addTagClass"
          :label="`${$t('message.linkis.tableColumns.label')}`"
        >
          <WbTag
            :tagList="modifyData.labels"
            :selectList="keyList"
            @addEnter="addEnter"
            @onCloseTag="onCloseTag"
            @editEnter="editEnter"
          ></WbTag>
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>
<script>
import api from "@/common/service/api";
import WbTag from "@/apps/linkis/components/tag";
import moment from "moment";
export default {
  name: 'microServiceManagement',
  components: {
    WbTag,
  },
  data() {
    return {
      loading: false,
      instance: "",
      applicationName: "",
      typeList: [],
      modifyShow: false,
      modifyData: {
        instance: "",
        applicationName: "",
        labels: [],
      },
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1,
      },
      keyList: [],
      tableColumnNum: [
        {
          title: this.$t("message.linkis.instanceName"),
          key: "instance",
          align: "center",
          slot: "instance",
          minWidth: 120,
        },
        {
          title: this.$t("message.linkis.tableColumns.label"),
          key: "labels",
          slot: "labels",
          align: "center",
          tooltip: true,
          minWidth: 100,
        },
        {
          title: this.$t("message.linkis.tableColumns.serveType"),
          key: "applicationName",
          align: "center",
          tooltip: true,
          minWidth: 100,
        },
        {
          title: this.$t("message.linkis.tableColumns.createdTime"),
          key: "createTime",
          align: "center",
          tooltip: true,
          minWidth: 120,
          slot: "createTime",
        },
        {
          title: this.$t("message.linkis.tableColumns.updateTime"),
          key: "updateTime",
          align: "center",
          tooltip: true,
          minWidth: 120,
          slot: "updateTime",
        },
        {
          title: this.$t("message.linkis.tableColumns.control.title"),
          slot: "action",
          align: "center",
          minWidth: 80,
        },
      ],
      tableData: [], // data for presentation(用于展示的数据)
      allInstance: [], // All data temporarily stored for filtering(暂存的所有数据，用于做筛选)
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
    this.getKeyList();
    this.getAllInstance();
  },
  methods: {
    // Get all modifiable labelKeys(获取所有可修改的labelKey)
    getKeyList() {
      api.fetch("/microservice/modifiableLabelKey", "get").then((res) => {
        let list = res.keyList || [];
        this.keyList = list.map((item) => {
          return {
            lable: item,
            value: item,
          };
        });
      });
    },
    // Get all microservices(获取所有的微服务)
    getAllInstance() {
      this.loading = true;
      api.fetch("/microservice/allInstance", "get").then((res) => {
        this.allInstance = [...res.instances] || [];
        this.tableData = [...res.instances] || [];
        this.typeList = [];
        res.instances.forEach((item) => {
          if (this.typeList.indexOf(item.applicationName) === -1) {
            this.typeList.push(item.applicationName);
          }
        });
        this.page.totalSize = this.tableData.length;
        this.loading = false;
      });
    },
    searchAction() {
      this.search(this.instance?.replace(/ /g, '') || '', this.applicationName);
    },
    //Jump to Microservices(跳转到微服务)
    handleTabsJump() {
      api.fetch("/microservice/serviceRegistryURL", "get").then((rst) => {
        window.open(rst.url, "_blank");
      });
    },
    // Click to search to filter the data(点击搜索 对数据进行筛选)
    search(instance="", applicationName="") {
      if (instance || applicationName) {
        this.tableData = this.allInstance.filter((item) => {
          return (
            item.instance.match(instance) &&
            item.applicationName.match(applicationName)
          );
        });
      } else {
        this.tableData = [...this.allInstance];
      }
      this.page.pageNow = 1;
      this.page.totalSize = this.tableData.length;
    },
    // Display the modification popup and initialize the data(显示修改弹框并初始化数据)
    modify(data) {
      this.modifyShow = true;
      this.modifyData.instance = data.instance;
      this.modifyData.labels = data.labels.map((item) => {
        return {
          key: item.labelKey,
          value: item.stringValue,
          modifiable: item.modifiable || false,
        };
      });
      this.modifyData.applicationName = data.applicationName;
    },
    // Update the label information of a single microservice instance(更新单个微服务实例的label信息)
    modifyOk() {
      let param = JSON.parse(JSON.stringify(this.modifyData));
      param.labels = param.labels.map((item) => {
        return {
          labelKey: item.key,
          stringValue: item.value,
        };
      });
      api.fetch("/microservice/instanceLabel", { ...param }, "put").then(() => {
        this.$Message.success(this.$t("message.linkis.editedSuccess"));
        this.getAllInstance();
      });
    },
    // add tag(添加tag)
    addEnter(key, value) {
      this.modifyData.labels.push({ key, value });
    },

    // Edit tags(修改标签)
    editEnter(editInputKey, editInputValue,editedInputValue) {
      let index = this.modifyData.labels.findIndex((item)=>{
        return  item.value === editInputValue && item.key === editInputKey
      })
      this.modifyData.labels.splice(index,1,{key: editInputKey,modifiable: true,value: editedInputValue})
    },

    // delete tag(删除tag)
    onCloseTag(name, index) {
      this.modifyData.labels.splice(index, 1);
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
      return moment(date).format("YYYY-MM-DD HH:mm:ss");
    },
  },
};
</script>
<style lang="scss" src="./index.scss" scoped></style>

<style lang="scss">
.micro-service-table {
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
