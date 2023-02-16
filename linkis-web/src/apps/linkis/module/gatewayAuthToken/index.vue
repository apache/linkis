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
    <Row class="search-bar" type="flex">
      <Col span="6">
        <span :style="{ whiteSpace: 'nowrap', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.basedataManagement.searchLabel')">{{$t('message.linkis.basedataManagement.searchLabel')}}</span>
        <Input v-model="searchName" clearable suffix="ios-search" class="input" :placeholder="$t('message.linkis.basedataManagement.gatewayAuthToken.searchPlaceholder')"></Input>
      </Col>
      <Col span="3">
        <Button type="primary" class="Button" @click="load()">{{
          $t('message.linkis.basedataManagement.search')
        }}
        </Button>
        <Button type="success" class="Button" style="margin-left: 10px" @click="onAdd()">{{
          $t('message.linkis.basedataManagement.add')
        }}
        </Button>
      </Col>
    </Row>
    <Table border size="small" align="center" :columns="tableColumnNum" :data="pageDatalist"
      class="table-content mytable">
      <template slot-scope="{ row,index }" slot="action">
        <ButtonGroup size="small">
          <Button
            :disabled="row.expire"
            size="small"
            type="primary"
            @click="onTableEdit(row, index)"
          >{{ $t('message.linkis.basedataManagement.edit') }}
          </Button
          >
          <Button
            :disabled="row.expire"
            size="small"
            type="primary"
            @click="onTableDelete(row, index)"
          >
            {{ $t('message.linkis.basedataManagement.remove') }}
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
    <Modal
      width="800"
      class="modal"
      v-model="modalShow"
      :title="modalAddMode=='add'? $t('message.linkis.basedataManagement.add') : $t('message.linkis.basedataManagement.edit')"
      :loading="modalLoading"
    >
      <div slot="footer">
        <Button type="text" size="large" @click="onModalCancel()">{{$t('message.linkis.basedataManagement.modal.cancel')}}</Button>
        <Button type="primary" size="large" @click="onModalOk('userConfirm')">{{$t('message.linkis.basedataManagement.modal.confirm')}}</Button>
      </div>
      <ErrorCodeForm ref="errorCodeForm" :data="modalEditData"></ErrorCodeForm>
    </Modal>
  </div>
</template>
<script>
import mixin from '@/common/service/mixin';
import ErrorCodeForm from './EditForm/index'
import {add, del, edit, getList} from "./service";
import {formatDate} from "iview/src/components/date-picker/util";
export default {
  mixins: [mixin],
  components: {ErrorCodeForm},
  data() {
    return {
      searchName: "",
      page: {
        totalSize: 0,
        pageSize: 10,
        pageNow: 1,
      },
      tableColumnNum: [
        {
          title: "ID",
          key: 'id',
          width: 100,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.tokenName'),
          key: 'tokenName',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalUsers'),
          key: 'legalUsers',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalHosts'),
          key: 'legalHosts',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.elapseDay'),
          key: 'elapseDay',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.businessOwner'),
          key: 'businessOwner',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.updateBy'),
          key: 'updateBy',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.createTime'),
          key: 'createTime',
          minWidth: 50,
          tooltip: true,
          align: 'center',
          render: (h,params)=>{
            return h('div',
              formatDate(new Date(params.row.createTime),'yyyy-MM-dd hh:mm')
            )
          }
        },
        {
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.updateTime'),
          key: 'updateTime',
          minWidth: 50,
          tooltip: true,
          align: 'center',
          render: (h,params)=>{
            return h('div',
              formatDate(new Date(params.row.createTime),'yyyy-MM-dd hh:mm')
            )
          }
        },
        {
          title: this.$t('message.linkis.basedataManagement.action'),
          width: 150,
          slot: 'action',
          align: 'center',
        },

      ],
      pageDatalist: [],
      modalShow: false,
      modalAddMode: 'add',
      modalEditData: {},
      modalLoading: false
    };
  },
  created() {
    this.load()
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      window.console.log(this.$route.query.isSkip);
    },
    load() {
      let params = {
        searchName: this.searchName,
        currentPage: this.page.pageNow,
        pageSize: this.page.pageSize
      }
      getList(params).then((data) => {
        this.pageDatalist = data.list.list
        this.page.totalSize = data.list.total
      })
    },
    changePage(value) {
      this.page.pageNow = value
      this.load()
    },
    onAdd(){
      this.$refs.editForm.formModel.resetFields()
      this.modalAddMode = 'add'
      this.modalShow = true
    },
    onTableEdit(row){
      if(row.elapseDay === -1) {
        row.permanentlyValid = true;
        this.$refs.editForm.formModel.rule[5].hidden = true;
      }
      this.$refs.editForm.formModel.setValue(row)
      this.modalAddMode = 'edit'
      this.modalShow = true
    },
    onTableDelete(row){

      this.$Modal.confirm({
        title: this.$t('message.linkis.basedataManagement.modal.modalTitle'),
        content: this.$t('message.linkis.basedataManagement.modal.modalDelete', {name: row.tokenName}),
        onOk: ()=>{
          let params = {
            id: row.id
          }
          del(params).then((data)=>{
            if(data.result) {
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalDeleteSuccess')
              })
            }else{
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalDeleteFail')
              })
            }
            this.load()
          })
        }
      })

    },
    onModalOk(){
      this.$refs.errorCodeForm.formModel.submit((formData)=>{
        this.modalLoading = true
        if(this.modalAddMode=='add') {
          add(formData).then((data)=>{
            window.console.log(data)
            if(data.result) {
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalAddSuccess')
              })
            }else{
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalAddFail')
              })
            }
            this.load()
          })
        }else {
          edit(formData).then((data)=>{
            window.console.log(data)
            if(data.result) {
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalEditSuccess')
              })
            }else{
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalEditFail')
              })
            }
            this.load()
          })
        }
        this.modalLoading=false
        this.modalShow = false
      })
    },
    onModalCancel(){
      this.modalLoading=false
      this.modalShow = false
    }
  },
};
</script>

<style lang="scss" src="./index.scss" scoped>
</style>

<style lang="scss">
.mytable {
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
