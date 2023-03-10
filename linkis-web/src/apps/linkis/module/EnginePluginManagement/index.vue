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
      :title="`${currentEngineType}${$t('message.linkis.EnginePluginManagement.versionList')} 当前版本${currentEngineVersion}`"
    >
      <Table
        border
        size="small"
        align="center"
        :columns="versionTableColumnNum"
        :data="this.currentVersionList"
        max-height="300"
        class="table-content"
      >
        <template slot-scope="{ row }" slot="action">
          <ButtonGroup size="small" :key="row.resourceVersion">
            <Button
              v-if="row.resourceVersion != currentResourcebmlVersion"
              size="primary"
              type="text"
              @click="onRollback(row)"
            >

              {{ $t('message.linkis.EnginePluginManagement.rollback') }}
            </Button>
            <!-- <Button
              v-if="row.resourceVersion != currentResourcebmlVersion"
              size="primary"
              type="error"
              @click="deleteCurrentbml(row)"
            >
              {{ $t('message.linkis.EnginePluginManagement.deleteCurrentbml') }}
            </Button> -->

          </ButtonGroup>
        </template>
      </Table>
      <div slot="footer">
        <div>
          <Button @click="showVersionList = false">{{
            $t('message.linkis.cancel')
          }}</Button>
        </div>
      </div>
    </Modal>
    <Modal
      width="800"
      class="modal"
      v-model="showFileOperate"
      :title="`${actionType} ${ecType}`"
    >
      <Spin size="large" fix v-if="loadingForm"></Spin>
      <div style="height: 200px">
        <form style="width: 200px;height: 200px" v-show="false">
          <input ref="upload" type="file" @change="getFile($event)" accept=".zip"/>
        </form>
        <Button type="default" style="width: 100%; height: 70%; font-size: 50px;" size="large" :icon="hasFile ? 'ios-cloud-done-outline': 'ios-cloud-upload-outline'" @click="handleClick"></Button>
        <span style="margin-top: 30px; text-align: center; display: inline-block; width:100%; font-size:14px">{{!hasFile ? $t('message.linkis.EnginePluginManagement.upload') : $t('message.linkis.EnginePluginManagement.hasUpload')}}</span>
        <div style="justify-content:center; margin-top:5px; display: flex; align-item: center;">
          <span style="display:inline-block; margin-right: 10px">{{$t('message.linkis.EnginePluginManagement.force')}}</span>
          <RadioGroup v-model="force">
            <Radio label="yes">{{$t('message.linkis.EnginePluginManagement.yes')}}</Radio>
            <Radio label="no">{{$t('message.linkis.EnginePluginManagement.no')}}</Radio>
          </RadioGroup>
        </div>

      </div>

      <div slot="footer">
        <div class="footer">
          <div>
            <Button
              type="primary"
              @click="handleCancel"
            >{{ $t('message.linkis.cancel') }}</Button>
            <Button type="primary" @click="onSubmit" :disabled="!hasFile" :loading="isUploading">{{
              $t('message.common.ok')}}</Button>
          </div>
        </div>
      </div>
    </Modal>
    <Row class="search-bar" type="flex" justify="start">
      <Col span="5">
        <div class="search-item">
          <span class="lable" :title="$t('message.linkis.EnginePluginManagement.engineConnType')">{{ $t('message.linkis.EnginePluginManagement.engineConnType') }}</span>
          <Select  v-model="ecType" clearable style="width: 180px; margin-left: 5px;">
            <Option
              v-for="(item) in typeList"
              :label="item"
              :value="item"
              :key="item"/>
          </Select>
        </div>
      </Col>
      <Col span="5">
        <div class="search-item">
          <span class="lable" :title="$t('message.linkis.EnginePluginManagement.engineConnVersion')">{{ $t('message.linkis.EnginePluginManagement.engineConnVersion') }}</span>
          <Select  v-model="version" clearable style="width: 180px; margin-left: 5px;">
            <Option
              v-for="(item) in typeVersionList"
              :label="item"
              :value="item"
              :key="item"/>
          </Select>
        </div>
      </Col>
      <Col span="12">
        <Button type="primary" class="button" :style="{width: '60px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="initECMList()">{{
          $t('message.linkis.search') }}</Button>
        <Button type="primary" :style="{width: '60px', marginRight: '5px', padding: '5px'}" @click="resetSearch">{{
          $t('message.linkis.EnginePluginManagement.Reset')}}</Button>
        <!-- <Button type="primary" :style="{width: '120px', marginRight: '5px', padding: '5px'}" @click="createOrUpdate(1)">{{
          $t('message.linkis.EnginePluginManagement.update')}}</Button>
        <Button type="error" :style="{width: '120px', marginRight: '5px', padding: '5px'}" @click="deleteBML">{{
          $t('message.linkis.EnginePluginManagement.delete')}}</Button> -->
        <!-- <Button type="primary" :style="{width: '90px', marginRight: '5px', padding: '5px'}" @click="createOrUpdate(0)">{{
          $t('message.linkis.EnginePluginManagement.create') }}</Button> -->
      </Col>
    </Row>
    <Table
      border
      size="small"
      align="center"
      :columns="tableColumnNum"
      :data="pageDatalist"
      :loading="tableLoading"
      class="table-content engineplugin-management-table"
    >
      <template slot-scope="{ row, index }" slot="bmlResourceVersion">
        <Button
          size="small"
          type="primary"
          :disabled="row.expire"
          @click="openVersionList(row, index)"
        >{{ `${row.bmlResourceVersion || '-'}` }}</Button
        >
      </template>
      <template slot-scope="{ row }" slot="action">
        <Button
          size="small"
          type="primary"
          style="margin-right: 5px;"
          @click="() => tableActionHandler(row, 'upd')"
        >{{ $t('message.linkis.EnginePluginManagement.updateFileOnly') }}</Button
        >
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
import api from '@/common/service/api';
import moment from 'moment';
export default {
  data() {
    return {
      ecType: null,
      version: null,
      typeList: [],
      typeVersionList: [],
      page: {
        totalSize: 0,
        pageSize: 8,
        pageNow: 1,
      },
      file: null,
      currentEnginpluginData: null,
      showFileOperate: false,
      actionType: '',
      actionNum: '',
      loadingForm: false,
      tableLoading: false,
      updWay: '',
      hasFile: false,
      isUploading: false,
      force: 'no',
      tableColumnNum: [
        {
          title: "ID",
          key: 'id',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.engineConnType'),
          key: 'engineConnType',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.engineConnVersion'),
          key: 'version',
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.fileName'),
          key: 'fileName',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.fileSize'),
          key: 'fileSize',
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            if (!params.row.fileSize || isNaN(Number(params.row.fileSize)) ) {
              return h('span', '');
            }
            let size = +params.row.fileSize;
            let format = size >= 1024 * 1024 ?
              Math.floor(size / (1024 * 1024)) + 'MB' :
              size >= 1024 ?
                Math.floor(size / 1024) + 'KB' :
                size + 'B';
            return h('span', format);
          }
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.lastModified'),
          key: 'lastModified',
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            let time = moment(new Date(params.row.lastModified)).format('YYYY-MM-DD HH:mm:ss')
            return h('span', time);
          }
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.bmlResourceId'),
          key: 'bmlResourceId',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.bmlResourceVersion'),
          key: 'bmlResourceVersion',
          slot: 'bmlResourceVersion',
          tooltip: true,
          align: 'center',
          minWidth: 30,
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.lastUpdateTime'),
          key: 'lastUpdateTime',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.createTime'),
          key: 'createTime',
          tooltip: true,
          align: 'center',
        },
        // {
        //   title: this.$t('message.linkis.EnginePluginManagement.action'),
        //   slot: 'action',
        //   minWidth: 20,
        //   align: 'center',
        // },
      ],
      pageDatalist: [],
      currentVersionList: [],
      currentEngineType: null,
      currentEngineVersion: null,
      currentResourcebmlVersion: null,
      showVersionList: false,
      versionTableColumnNum: [
        {
          title: this.$t('message.linkis.EnginePluginManagement.resourceVersion'),
          key: 'resourceVersion',
          tooltip: true,
          minWidth: 60,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.user'),
          key: 'user',
          tooltip: true,
          minWidth: 60,
          align: 'center',
        },
        // {
        //   title: this.$t('message.linkis.EnginePluginManagement.action'),
        //   key: 'action',
        //   tooltip: true,
        //   minWidth: 120,
        //   align: 'center',
        //   slot: 'action',
        // },
      ],
    }
  },
  watch: {
    ecType(newName, oldName) {
      window.console.log(oldName);
      if(!newName) return;
      this.getTypeVersionList(newName)
    }
  },
  created() {
    this.getTypeList();
    this.initECMList();
  },
  methods: {
    async initECMList() {
      let paramsData = {
        "ecType": this.ecType,
        "version": this.version,
        "pageSize": this.page.pageSize,
        "currentPage": this.page.pageNow
      }
      let ECM = await api.fetch('/engineplugin/list',paramsData,'get') || {};
      this.pageDatalist = ECM.queryList || []
      this.page.totalSize=ECM.totalPage
    },

    async getTypeList(){
      let typeListTem = await api.fetch('/engineplugin/getTypeList','get') || {};
      this.typeList=typeListTem.typeList
    },
    async getTypeVersionList(ecType){
      let urlTemp = '/engineplugin/getTypeVersionList/'+ ecType
      let typeVersionListTem = await api.fetch(urlTemp, 'get') || {};
      this.typeVersionList=typeVersionListTem.queryList
      this.version=(typeVersionListTem.queryList)[0]
    },
    resetSearch(){
      this.ecType = null
      this.version=null
      this.initECMList();
    },
    getFile(event) {
      this.file = event.target.files[0];
      this.hasFile = true;
    },
    onSubmit() {
      var formData = new FormData();
      let force = this.force === 'yes';
      if(this.actionNum === 0){
        formData.append('file', this.file);
        this.isUploading = true;
        api.fetch('/engineplugin/uploadEnginePluginBML', formData, {method: 'post', 'Content-Type': 'multipart/form-data'}).then(response => {
          window.console.log(response);
          this.$Message.success(response.msg);
          this.getTypeList();
          this.showFileOperate = false;
        }).catch(e => {
          this.isUploading = false
          window.console.log(e);
          this.$Message.error(e);
          this.showFileOperate = false;
        })

      }else if(this.actionNum === 1){
        formData.append('file', this.file);
        formData.append('ecType', this.ecType);
        formData.append('version', this.version);
        formData.append('force', force);
        this.isUploading = true;
        api.fetch('/engineplugin/updateEnginePluginBML', formData, {methed: 'post', 'Content-Type': 'multipart/form-data'}).then(response => {
          this.$Message.success(response.msg);
          if(this.updWay === 'table') {
            this.ecType = '';
            this.version = '';
            this.updWay = '';
          }
          this.getTypeList();
          this.initECMList();
          this.showFileOperate = false;
          this.isUploading = false;
        }).catch(e => {
          window.console.log(e);
          this.$Message.error(e);
          this.showFileOperate = false;
          this.isUploading = false;
        })

      }

    },
    handleCancel() {
      this.showFileOperate = false;
      if(this.updWay === 'table') {
        this.ecType = '';
        this.version = '';
        this.updWay = '';
      }
    },
    deleteBML(){
      var th=this;
      var reqList=[]
      th.pageDatalist.forEach(it => {
        if(it.engineConnType == th.ecType && it.version == th.version){
          reqList.push(it.bmlResourceId);
        }
      })
      api.fetch('/bml/deleteResources', {'resourceIds': reqList}, 'post').then(response => {
        window.console.log(response);
        api.fetch('/engineplugin/deleteEnginePluginBML', {'ecType': th.ecType, 'version': th.version}, 'get').then(response2 => {
          th.getTypeList();
          th.resetSearch();
          this.$Message.success(response2.msg);
        }).catch(e2 => {
          window.console.log(e2);
          this.$Message.error(e2);
        })
      }).catch(e => {
        th.$Message.error(e);
        window.console.log(e);
      })

      //暂存后续相关bug修复后修改
      // var th=this;
      // api.fetch('/engineplugin/deleteEnginePluginBML', {'ecType': th.ecType, 'version': th.version}, 'get').then(response => {
      //   this.$Message.success(response.mes);
      // }).catch(e => {
      //   window.console.log(e);
      //   this.$Message.error(e);
      // })
    },
    createOrUpdate(num) {
      this.actionNum = num
      if(num === 0){
        this.actionType=this.$t('message.linkis.EnginePluginManagement.create')
      }else if(num === 1){
        this.actionType=this.$t('message.linkis.EnginePluginManagement.update')
      }
      this.showFileOperate = true
    },
    async tableActionHandler(row, type) {
      window.console.log(row);
      if(type === 'upd') {
        this.ecType = row.engineConnType;
        this.version = row.version;
        this.updWay = 'table';
        this.createOrUpdate(1);
      }
    },
    async openVersionList(row) {
      this.currentEnginpluginData = row
      this.currentEngineType=row.engineConnType;
      this.currentEngineVersion=row.version;
      this.currentResourcebmlVersion=row.bmlResourceVersion;
      this.currentVersionList=[]
      // this.getVersionListBybmlResourceId()
      var th=this;
      await api.fetch('/bml/getVersions', {'resourceId': th.currentEnginpluginData.bmlResourceId}, 'get').then(response => {
        let userName=response.ResourceVersions.user;
        for (let index = 0; index < response.ResourceVersions.versions.length; index++) {
          let element = {};
          element.resourceVersion = response.ResourceVersions.versions[index].version;
          element.user=userName;
          th.currentVersionList.push(element);
        }
        th.showVersionList = true
      }).catch(e => {
        th.$Message.error(e);
        window.console.log(e);
      })
      window.console.log(th.currentVersionList);
    },
    changePage(value) {
      this.page.pageNow = value
      this.initECMList()
    },
    onRollback(data) {
      this.showVersionList = false
      var reqData = {};
      reqData.bmlResourceVersion=data.resourceVersion;
      reqData.bmlResourceId=this.currentEnginpluginData.bmlResourceId;
      reqData.createTime=this.currentEnginpluginData.createTime;
      reqData.engineConnType=this.currentEnginpluginData.engineConnType;
      reqData.fileName=this.currentEnginpluginData.fileName;
      reqData.fileSize=this.currentEnginpluginData.fileSize;
      reqData.lastModified=this.currentEnginpluginData.lastModified;
      reqData.lastUpdateTime=this.currentEnginpluginData.lastUpdateTime;
      reqData.version=this.currentEnginpluginData.version;
      api.fetch('/engineplugin/rollBack', reqData).then(response => {
        this.initECMList();
        this.$Message.success('onRollback Success'+response)
      }).catch(e => {
        this.$Message.error(e);
      })
    },
    handleClick () {
      this.$refs.upload.click();
    }
  },
}
</script>
<style lang="scss" src="./index.scss" scoped></style>

<style lang="scss">
.engineplugin-management-table {
  border: 0;
  height: calc(100% - 110px);
  width: 100%;

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
