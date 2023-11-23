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
  <div :style="{height: '100%', overflow: 'hidden'}">
    <Modal
      width="800"
      class="modal"
      v-model="showVersionList"
      :loading="loadingForm"
      :title="`${actionType}${$t('message.linkis.datasource.versionList')}`"
    >
      <Spin size="large" fix v-if="loadingVersionList"></Spin>
      <Table
        border
        size="small"
        align="center"
        :columns="versionTableColumnNum"
        :data="currentVersionList"
        max-height="300"
        class="table-content"
      >
        <template slot-scope="{ row, index }" slot="action">
          <ButtonGroup size="small" :key="row.versionId">
            <Button
              v-if="row.status == 0"
              size="small"
              type="text"
              @click="onPublish(row)"
            >
              {{ $t('message.linkis.datasource.publish') }}
            </Button>

            <Button size="small" type="text" @click="watch(row, index)">{{
              $t('message.linkis.datasource.watch')
            }}</Button>
            <Button
              v-if="row.status == 2"
              size="small"
              type="text"
              @click="onRollback(row)"
            >
              {{ $t('message.linkis.datasource.rollback') }}
            </Button>

            <Button type="primary" @click="onConnectTestFromVersion(row)">{{
              $t('message.linkis.datasource.connectTest')
            }}</Button>
          </ButtonGroup>
        </template>
      </Table>
      <div slot="footer">
        <div v-if="currentStep === 0">
          <Button @click="showVersionList = false">{{
            $t('message.linkis.cancel')
          }}</Button>
        </div>
      </div>
    </Modal>
    <Modal
      width="800"
      class="modal"
      v-model="showDataSource"
      :title="`${actionType}${$t('message.linkis.datasource.datasourceSrc')}`"
    >
      <Spin size="large" fix v-if="loadingForm"></Spin>
      <DataSourceType
        :data="typeList"
        v-show="currentStep === 0"
        :onSelected="onSelect"
      />
      <DatasourceForm
        :data="currentSourceData"
        ref="datasourceForm"
        v-if="showDataSource && currentStep === 1"
        :type="actionType"
      />

      <div slot="footer">
        <div v-if="currentStep === 0">
          <Button @click="showDataSource = false">{{
            $t('message.linkis.cancel')
          }}</Button>
        </div>

        <div v-else class="footer">
          <div>
            <Button type="primary" @click="onConnectFormTest">{{
              $t('message.linkis.datasource.connectTest')
            }}</Button>
          </div>
          <div>
            <Button
              v-if="actionType == $t('message.linkis.create')"
              @click="stepChange(-1)"
            >{{ $t('message.linkis.prev') }}</Button
            >
            <Button
              v-if="actionType == $t('message.linkis.datasource.watch')"
              type="primary"
              @click="showDataSource = false"
            >{{ $t('message.linkis.close') }}</Button
            >
            <Button v-else type="primary" @click="onSubmit">
              {{ $t('message.common.ok') }}
            </Button>
          </div>
        </div>
      </div>
    </Modal>
    <Row class="search-bar" type="flex" justify="space-around">
      <Col span="6">
        <span :style="{minWidth: '80px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.datasource.sourceName')">{{$t('message.linkis.datasource.sourceName')}}</span>
        <Input
          clearable
          v-model="searchName"
          suffix="ios-search"
          class="input"
          :placeholder="$t('message.linkis.datasource.sourceName')"
          @on-enter="searchList(true)"
        ></Input>
      </Col>
      <Col span="6" class="search-item">
        <span class="lable" :style="{marginLeft: '5px', marginRight: '5px', minWidth: '80px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden'}" :title="$t('message.linkis.datasource.sourceType')">{{ $t('message.linkis.datasource.sourceType') }}</span>
        <Select v-model="dataSourceTypeId" class="input">
          <Option v-for="item in typeList" :value="item.id" :key="item.id">{{
            item.name
          }}</Option>
          <Option value="null">{{
            $t('message.linkis.statusType.all')
          }}</Option>
        </Select>
      </Col>
      <Col span="12">
        <Button type="primary" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="searchList(true)">{{
          $t('message.linkis.search')
        }}</Button>
        <Button type="primary" :style="{width: '90px', marginRight: '5px', padding: '5px'}" @click="createDatasource">{{
          $t('message.linkis.datasource.create')
        }}</Button>
        <Button type="primary" :style="{width: '120px', marginRight: '5px', padding: '5px'}" disabled>{{
          $t('message.linkis.datasource.exports')
        }}</Button>
        <Button type="primary" :style="{width: '120px', padding: '5px'}" disabled>{{
          $t('message.linkis.datasource.imports')
        }}</Button>
      </Col>
    </Row>
    <Table
      border
      size="small"
      align="center"
      :columns="tableColumnNum"
      :data="pageDatalist"
      :loading="tableLoading"
      class="table-content data-source-table"
    >
      <template slot-scope="{ row, index }" slot="version">
        <Button
          size="small"
          type="primary"
          :disabled="row.expire"
          @click="openVersionList(row, index)"
        >{{ `${row.versionId || '-'}` }}</Button
        >
      </template>
      <template slot-scope="{ row, index }" slot="action">
        <ButtonGroup size="small">
          <Button
            :disabled="row.expire"
            size="small"
            type="primary"
            @click="modify(row, index)"
          >{{ $t('message.linkis.edit') }}</Button
          >

          <Button
            :disabled="row.expire"
            size="small"
            type="primary"
            @click="overdue(row)"
          >
            {{ $t('message.linkis.datasource.overdue') }}
          </Button>

          <Button type="primary" @click="onConnectTest(row)">
            {{ $t('message.linkis.datasource.connectTest') }}
          </Button>
        </ButtonGroup>
      </template>
    </Table>
    <div style="margin: 10px; overflow: hidden; textAlign: center">
      <div>
        <Page
          :page-size="page.pageSize"
          :total="page.totalSize"
          :current="page.pageNow"
          @on-change="changePage"
          size="small"
          show-total
          show-elevator
          :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
        ></Page>
      </div>
    </div>
  </div>
</template>
<script>
import DatasourceForm from './datasourceForm/index'
import DataSourceType from './datasourceType/index'
import {
  //createDataSourceForm
  getDataSourceList,
  getDataSourceTypeList,
  getEnvList,
  createDataSource,
  saveConnectParams,
  expire,
  updateDataSource,
  publish,
  connect,
  getDataSourceByIdAndVersion,
  getVersionListByDatasourceId,
} from './dataSourceApi'
// import _ from 'lodash';
const FORM_KEYS = ['dataSourceName', 'dataSourceDesc', 'labels'] //'dataSourceEnvId'
export default {
  components: {
    DatasourceForm,
    DataSourceType,
  },
  data() {
    return {
      currentStep: 0,
      currentSourceData: null,
      dataSourceTypeId: null,
      isCreate: true,
      showDataSource: false,
      searchName: '',
      searchCreator: '',
      actionType: '',
      loadingForm: false,
      tableLoading: false,
      loadingVersionList: false,
      page: {
        totalSize: 0,
        pageSize: 10,
        pageNow: 1,
      },
      tableColumnNum: [
        {
          title: "ID",
          key: 'id',
          width: 60,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.dataSourceName'),
          key: 'dataSourceName',
          width: 240,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.dataSourceType'),
          key: 'dataSourceTypeId',
          width: 150,
          render: (h, params) => {
            let result = {}
            if (this.typeList) {
              result =
                this.typeList.find(
                  (item) => params.row.dataSourceTypeId == item.id
                ) || {}
            }
            return h('span', result.name)
          },
          align: 'center',
        },
        // {
        //   title: this.$t('message.linkis.datasource.dataSourceEnv'),
        //   key: 'dataSourceEnvId',
        //   tooltip: true,
        //   width: 100,
        //   render: (h, params)=>{
        //     let result = {};
        //     if(this.envList){
        //       result = this.envList.find(item => params.row.dataSourceEnvId == item.id) || {envName: '无'};
        //     }
        //     return h('span', result.envName);
        //   },
        //   align: 'center'
        // },
        {
          title: this.$t('message.linkis.datasource.createSystem'),
          key: 'createSystem',
          width: 125,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.status'),
          key: 'status',
          tooltip: true,
          width: 120,
          align: 'center',
          render: (h, params) => {
            let result = {}
            if (this.envList) {
              result =
                this.tableStatusMap.find(
                  (item) => params.row.expire == item.status
                ) || {}
            }
            var color = 'green'
            if (result.status) {
              color = 'red'
            }
            return h('p', { style: { color: color } }, result.name)
          },
        },
        {
          title: this.$t('message.linkis.datasource.label'),
          key: 'labels',
          tooltip: true,
          width: 120,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.version'),
          key: 'version',
          slot: 'version',
          width: 80,
          // render: (h, params)=>{
          //   return h('span', params.row.versionId || '-');
          // },
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.desc'),
          key: 'dataSourceDesc',
          width: 400,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.createUser'),
          key: 'createUser',
          width: 120,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.action'),
          width: 280,
          slot: 'action',
          align: 'center',
        },
      ],
      pageDatalist: [],
      typeList: [],
      versionStatusMap: [
        { status: 1, name: this.$t('message.linkis.datasource.published') },
        { status: 0, name: this.$t('message.linkis.datasource.unpublish') },
        { status: 2, name: this.$t('message.linkis.datasource.cannotPublish') },
      ],
      tableStatusMap: [
        { status: false, name: this.$t('message.linkis.datasource.used') },
        { status: true, name: this.$t('message.linkis.datasource.overdue') },
      ],
      currentVersionList: [],
      showVersionList: false,
      versionTableColumnNum: [
        {
          title: this.$t('message.linkis.datasource.version'),
          key: 'versionId',
          tooltip: true,
          minWidth: 60,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.datasource.status'),
          key: 'status',
          tooltip: true,
          minWidth: 60,
          align: 'center',
          render: (h, params) => {
            let result = {}
            result =
              this.versionStatusMap.find(
                (item) => params.row.status == item.status
              ) || {}
            return h('span', result.name)
          },
        },
        {
          title: this.$t('message.linkis.datasource.versionDec'),
          key: 'comment',
          tooltip: true,
          minWidth: 60,
          align: 'center',
        },
        // {
        //   title: this.$t('message.linkis.datasource.createTime'),
        //   key: 'createTime',
        //   tooltip: true,
        //   minWidth: 60,
        //   align: 'center'
        // },
        {
          title: this.$t('message.linkis.datasource.action'),
          key: 'action',
          tooltip: true,
          minWidth: 160,
          align: 'center',
          slot: 'action',
        },
      ],
    }
  },
  created() {
    getDataSourceTypeList().then((data) => {
      this.typeList = data.typeList

      getEnvList().then((data) => {
        this.envList = data.queryList

        this.searchList()
      })
    })
  },
  methods: {
    overdue(data) {
      expire(data.id).then(() => {
        this.searchList()
      })
    },
    searchList(isSearch) {
      this.loadingTable = true
      if (isSearch) {
        this.page.pageNow = 1
      }
      const data = {
        typeId: this.dataSourceTypeId,
        pageSize: this.page.pageSize,
        currentPage: this.page.pageNow,
        name: this.searchName,
      }

      if (data.typeId == 'null') {
        delete data.typeId
      }

      getDataSourceList({
        ...data,
      }).then((result) => {
        this.tableLoading = false
        this.pageDatalist = result.queryList
        this.page.totalSize = result.totalPage
      })
    },
    getVersionListBySourceId() {
      getVersionListByDatasourceId(this.currentSourceData.id).then((result) => {
        this.currentVersionList = result.versions
        this.currentVersionList.sort((a, b) => {
          if (a.versionId > b.versionId) {
            return -1
          }
          if (a.versionId < b.versionId) {
            return 1
          }
          return 0
        })
        let lastPulishIndex = Infinity
        for (let index = 0; index < this.currentVersionList.length; index++) {
          const element = this.currentVersionList[index]
          if (lastPulishIndex < index) {
            //can't post(不能发布)
            element.status = 2
          } else {
            element.status = 0
          }

          if (this.currentSourceData.publishedVersionId == element.versionId) {
            lastPulishIndex = index
            element.status = 1
          }
        }
      })
    },
    openVersionList(row) {
      this.currentSourceData = JSON.parse(JSON.stringify(row))
      this.getVersionListBySourceId()
      this.showVersionList = true
    },

    changePage(value) {
      this.page.pageNow = value
      this.searchList()
    },
    watch(data) {
      this.actionType = this.$t('message.linkis.datasource.watch')
      this.showDataSource = true
      this.currentSourceData.versionId = data.versionId
      this.currentStep = 1
    },
    modify(data) {
      this.actionType = this.$t('message.linkis.edit')
      this.showDataSource = true
      this.currentSourceData = data
      this.currentStep = 1
    },
    createDatasource() {
      this.currentStep = 0
      this.currentSourceData = null
      this.actionType = this.$t('message.linkis.create')
      this.showDataSource = true
    },
    onSelect(item) {
      this.currentSourceData = { dataSourceTypeId: item.id }
      this.currentStep = 1
    },
    stepChange(step) {
      this.currentStep += step
      if (this.currentStep < 0) {
        this.currentStep = 0
      } else if (this.currentStep > 1) {
        this.currentStep = 1
      }
    },
    tranceFormData(data) {
      const formData = new FormData()
      Object.keys(data).forEach((key) => {
        if (typeof data[key] === 'object') {
          formData.append(key, JSON.stringify(data[key]))
        } else {
          formData.append(key, data[key])
        }
      })
      return formData
    },
    isEqual(obj1, obj2) {
      const isObject = (obj) => {
        return typeof obj === 'object' && obj !== null
      }
      if (!isObject(obj1) || !isObject(obj2)) {
        return obj1 === obj2
      }
      if (obj1 === obj2) {
        return true
      }
      let obj1Keys = Object.keys(obj1)
      let obj2Keys = Object.keys(obj2)
      if (obj1Keys.length !== obj2Keys.length) {
        return false
      }
      for (let key in obj1) {
        const res = this.isEqual(obj1[key], obj2[key])
        if (!res) {
          return false
        }
      }
      // otherwise all equal(否则全相等)
      return true
    },
    onSubmit() {
      this.$refs.datasourceForm.fApi.submit((formData) => {
        const realFormData = {}
        FORM_KEYS.forEach((key) => {
          realFormData[key] = formData[key]
          delete formData[key]
        })
        realFormData.connectParams = formData
        realFormData.createSystem = 'Linkis'
        realFormData.dataSourceTypeId = this.currentSourceData.dataSourceTypeId

        let postDataSource = createDataSource
        let commentMsg = this.$t('message.linkis.datasource.initVersion')
        if (!this.currentSourceData.id) {
          //Add data source(新增数据源)
          postDataSource = createDataSource
        } else {
          postDataSource = updateDataSource
          commentMsg = this.$t('message.linkis.datasource.updateVersion')
        }
        this.loadingForm = true
        postDataSource(realFormData, this.currentSourceData.id)
          .then((data) => {
            this.loadingForm = false
            const sourceId = data.id || data.insertId || data.updateId

            window.console.log()
            if (
              !this.currentSourceData.id ||
              !this.isEqual(
                this.preProcessData(
                  this.$refs.datasourceForm.sourceConnectData
                ),
                formData
              )
            ) {
              // if(this.$refs.datasourceForm.file){
              //   formData.file = this.$refs.datasourceForm.file;
              //   saveConnectParamsForm(sourceId, formData, commentMsg).then(()=>{
              //     this.$Message.success('Success');
              //     this.searchList();
              //   });
              //   this.showDataSource = false;
              // }else {

              // }
              saveConnectParams(sourceId, formData, commentMsg).then(() => {
                this.$Message.success('Success')
                this.searchList()
              })
              this.showDataSource = false
            } else {
              this.$Message.success('Success')
              this.searchList()
              this.showDataSource = false
            }
          })
          .catch(() => {
            this.loadingForm = false
          })
      })
    },
    preProcessData(data) {
      Object.keys(data).forEach((item) => {
        let obj = data[item]
        if (typeof obj === 'undefined' || obj === null || obj === '') {
          delete data[item]
        }
      })
      return data
    },
    onConnectTestFromVersion(data) {
      this.loadingVersionList = true
      getDataSourceByIdAndVersion(data.datasourceId, data.versionId).then(
        (data) => {
          connect(data.info)
            .then(() => {
              this.loadingVersionList = false
              this.$Message.success('Connect Success')
            })
            .catch(() => {
              this.loadingVersionList = false
            })
        }
      )
    },
    onConnectTest(data) {
      this.currentSourceData = data
      this.tableLoading = true
      getDataSourceByIdAndVersion(
        this.currentSourceData.id,
        this.currentSourceData.versionId
      )
        .then((data) => {
          connect(data.info)
            .then(() => {
              this.tableLoading = false
              this.$Message.success('Connect Success')
            })
            .catch(() => {
              this.tableLoading = false
            })
        })
        .catch(() => {
          this.tableLoading = false
        })
    },
    onConnectFormTest() {
      this.$refs.datasourceForm.fApi.submit(() => {
        this.loadingForm = true
        this.$refs.datasourceForm.fApi.submit((formData) => {
          const realFormData = {}
          FORM_KEYS.forEach((key) => {
            realFormData[key] = formData[key]
            delete formData[key]
          })

          realFormData.connectParams = formData
          realFormData.createSystem = 'Linkis'
          realFormData.dataSourceTypeId =
            this.currentSourceData.dataSourceTypeId
          realFormData.dataSourceEnvId = parseInt(realFormData.dataSourceEnvId)
          connect(realFormData)
            .then(() => {
              this.loadingForm = false
              this.$Message.success('Connect Success')
            })
            .catch(() => {
              this.loadingForm = false
            })
        })
      })
    },
    onPublish(data) {
      this.loadingVersionList = true
      publish(data.datasourceId, data.versionId).then(() => {
        this.showVersionList = false
        this.loadingVersionList = false
        this.$Message.success('Publish Success')
        this.searchList()
      })
    },
    onRollback(data) {
      this.showVersionList = false
      const sourceId = data.id || data.datasourceId || data.updateId

      saveConnectParams(
        sourceId,
        data.connectParams,
        this.$t('message.linkis.datasource.commentValue', {
          text: data.versionId,
        })
      ).then(() => {
        this.$Message.success('onRollback Success')
        this.searchList()
      })
    },
  },
}
</script>
<style lang="scss" src="./index.scss" scoped></style>

<style lang="scss">
.data-source-table {
  border: 0;
  height: calc(100% - 110px);
  width: 100%;
  overflow-y: auto;

  .ivu-table:before {
    height: 0
  }

  .ivu-table:after {
    width: 0
  }

  .ivu-table {
    height: auto;
    border: 1px solid #dcdee2;
    width: 100%;
  }
}
</style>
