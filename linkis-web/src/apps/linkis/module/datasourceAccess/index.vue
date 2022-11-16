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
  <div>
    <Row class="search-bar" type="flex">
      <Col span="6">
        <Input v-model="searchName" clearable suffix="ios-search" class="input" placeholder="搜索"></Input>
      </Col>
      <Col span="3">
        <Button type="primary" class="Button" @click="load()">{{
          $t('message.linkis.search')
        }}
        </Button>
        <Button type="success" class="Button" style="margin-left: 10px" @click="onAdd()">{{
          $t('message.linkis.basedata.add')
        }}
        </Button>
      </Col>
      <Col span="15">
      </Col>
    </Row>
    <div style="height: 600px">
      <Table border size="small" align="center" :columns="tableColumnNum" :data="pageDatalist" max-height="420"
        class="table-content">
        <template slot-scope="{ row,index }" slot="action">
          <ButtonGroup size="small">
            <Button
              :disabled="row.expire"
              size="small"
              type="primary"
              @click="onTableEdit(row, index)"
            >{{ $t('message.linkis.edit') }}
            </Button
            >
            <Button
              :disabled="row.expire"
              size="small"
              type="primary"
              @click="onTableDelete(row, index)"
            >
              {{ $t('message.linkis.basedata.remove') }}
            </Button>
          </ButtonGroup>
        </template>
      </Table>
    </div>
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
      :title="modalAddMode=='add' ? $t('message.linkis.basedata.add') : $t('message.linkis.basedata.edit')"
      :loading="modalLoading"
    >
      <div slot="footer">
        <Button type="text" size="large" @click="onModalCancel()">取消</Button>
        <Button type="primary" size="large" @click="onModalOk('userConfirm')">确定</Button>
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
          title: "表ID",
          key: 'tableId',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: "访问者",
          key: 'visitor',
          tooltip: true,
          align: 'center',
        },
        {
          title: "字段",
          key: 'fields',
          tooltip: true,
          align: 'center',
        },
        {
          title: "应用ID",
          key: 'applicationId',
          tooltip: true,
          align: 'center',
        },
        {
          title: "访问时间",
          key: 'accessTime',
          minWidth: 50,
          tooltip: true,
          align: 'center',
          render: (h,params)=>{
            return h('div',
              formatDate(new Date(params.row.accessTime),'yyyy-MM-dd hh:mm')
            )
          }
        },
        {
          title: this.$t('message.linkis.datasource.action'),
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
      console.log(this.$route.query.isSkip);
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
      this.modalEditData={
        id: "",
        errorCode: "",
        errorDesc: "",
        errorRegex: '',
      }
      this.modalAddMode = 'add'
      this.modalShow = true
    },
    onTableEdit(row){
      row.tableId = row.tableId+""
      row.applicationId= row.applicationId+""
      this.modalEditData = row
      this.modalAddMode = 'edit'
      this.modalShow = true
    },
    onTableDelete(row){

      this.$Modal.confirm({
        title: "提示信息",
        content: "确认是否删除该记录?",
        onOk: ()=>{
          let params = {
            id: row.id
          }
          del(params).then((data)=>{
            if(data.result) {
              this.$Message.success({
                duration: 3,
                content: "删除成功"
              })
            }else{
              this.$Message.success({
                duration: 3,
                content: "删除失败"
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
            if(data.result) {
              this.$Message.success({
                duration: 3,
                content: "添加成功"
              })
            }else{
              this.$Message.success({
                duration: 3,
                content: "添加失败"
              })
            }
            this.load()
          })
        }else {
          edit(formData).then((data)=>{
            if(data.result) {
              this.$Message.success({
                duration: 3,
                content: "编辑成功"
              })
              this.load()
            }else{
              this.$Message.success({
                duration: 3,
                content: "编辑失败"
              })
            }
          })
          this.load()
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
