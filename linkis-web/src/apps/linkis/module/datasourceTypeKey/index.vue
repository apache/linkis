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
    <div class="search-bar" style="display: flex;align-items: flex-start">

      <span :style="{ whiteSpace: 'nowrap', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.basedataManagement.datasourceTypeKey.searchName')">{{$t('message.linkis.basedataManagement.datasourceTypeKey.searchName')}}</span>
      <Input style="width: 150px" v-model="searchName" clearable class="input" :placeholder="$t('message.linkis.basedataManagement.datasourceTypeKey.searchPlaceholder')"></Input>

      <span :style="{ whiteSpace: 'nowrap',marginLeft:'5px', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.basedataManagement.datasourceTypeKey.searchType')">{{$t('message.linkis.basedataManagement.datasourceTypeKey.searchType')}}</span>
      <Select style="width: 150px;margin-right: 15px" v-model="datasourceType" >
        <Option value="-1" selected>{{$t('message.linkis.basedataManagement.datasourceTypeKey.all')}}</Option>
        <Option v-for="item in datasourceTypeOptions" :value="item.value" :key="item.value">{{item.label}}</Option>
      </Select>

      <Button type="primary" class="Button" @click="load()">{{
        $t('message.linkis.basedataManagement.search')
      }}
      </Button>
      <Button type="success" class="Button" style="margin-left: 10px" @click="onAdd()">{{
        $t('message.linkis.basedataManagement.add')
      }}
      </Button>

    </div>
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
      <EditForm ref="editForm"></EditForm>
    </Modal>
  </div>
</template>
<script>
import mixin from '@/common/service/mixin';
import EditForm from './EditForm/index'
import {add, del, edit, getList} from "./service";
import {formatDate} from "iview/src/components/date-picker/util";
import {getAllEnv} from "../datasourceEnv/service";
export default {
  mixins: [mixin],
  components: {EditForm},
  data() {
    return {
      searchName: "",
      datasourceType: "-1",
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
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.dataSourceType'),
          key: 'dataSourceTypeId',
          minWidth: 50,
          tooltip: true,
          align: 'center',
          render: (h,params)=>{
            return h('div',
              this.getDatasourceType( params.row.dataSourceTypeId)
            )
          }
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.key'),
          key: 'key',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.name'),
          key: 'name',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.nameEn'),
          key: 'nameEn',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.valueType'),
          key: 'valueType',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.description'),
          key: 'description',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.descriptionEn'),
          key: 'descriptionEn',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.createTime'),
          key: 'createTime',
          minWidth: 50,
          tooltip: true,
          align: 'center',
          render: (h,params)=>{
            return h('div',
              formatDate(new Date(params.row.createTime),'yyyy-MM-dd hh:mm:ss')
            )
          }
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.updateTime'),
          key: 'updateTime',
          minWidth: 50,
          tooltip: true,
          align: 'center',
          render: (h,params)=>{
            return h('div',
              formatDate(new Date(params.row.createTime),'yyyy-MM-dd hh:mm:ss')
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
      modalLoading: false,
      datasourceTypeOptions: []
    };
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      let params = {
        searchName: this.searchName,
        dataSourceTypeId: this.datasourceType==="-1"?"":this.datasourceType,
        currentPage: this.page.pageNow,
        pageSize: this.page.pageSize
      }

      //拉取分类
      let res = await getAllEnv()
      let options = [...res.typeList].sort((a, b) => a.id - b.id)
        .map(item => { return {value: +item.id, label: item.name}})
      this.datasourceTypeOptions= options

      //拉取列表
      let data = await getList(params)
      this.pageDatalist = data.list.list
      this.page.totalSize = data.list.total
    },
    getDatasourceType(id){
      if(this.datasourceTypeOptions.length>1){
        let datasourceType =  this.datasourceTypeOptions.filter(m=>m.value === id)
        if(datasourceType.length>0){
          return datasourceType[0].label
        }
      }
      return "loadding"
    },
    changePage(value) {
      this.page.pageNow = value
      this.load()
    },
    onAdd(){
      this.$refs.editForm.rule[2].options=this.datasourceTypeOptions
      this.$refs.editForm.formModel.resetFields()
      this.modalAddMode = 'add'
      this.modalShow = true
    },
    onTableEdit(row){
      this.$refs.editForm.rule[2].options=this.datasourceTypeOptions
      this.$refs.editForm.formModel.setValue(row)
      this.modalAddMode = 'edit'
      this.modalShow = true
    },
    onTableDelete(row){
      this.$Modal.confirm({
        title: this.$t('message.linkis.basedataManagement.modal.modalTitle'),
        content: this.$t('message.linkis.basedataManagement.modal.modalFormat').format(row.name),
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
      this.$refs.editForm.formModel.submit((formData)=>{
        delete formData._index
        delete formData._rowKey
        this.modalLoading = true
        if(this.modalAddMode=='add') {
          add(formData).then((data)=>{
            console.log(data)
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
    },
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
