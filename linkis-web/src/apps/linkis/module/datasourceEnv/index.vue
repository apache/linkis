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
        <Input v-model="searchName" clearable suffix="ios-search" class="input" :placeholder="$t('message.linkis.basedataManagement.datasourceEnv.searchPlaceholder')"></Input>
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
      <Col span="15">
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
            style="margin-right: 5px"
          >{{ $t('message.linkis.basedataManagement.edit') }}
          </Button
          >
          <Button
            :disabled="row.expire"
            size="small"
            type="error"
            @click="onTableDelete(row, index)"
          >
            {{ $t('message.linkis.basedataManagement.remove') }}
          </Button>
        </ButtonGroup>
      </template>
    </Table>
    <Modal
      width="800"
      class="modal"
      v-model="modalShow"
      :title="modalAddMode=='add' ? $t('message.linkis.basedataManagement.add') : $t('message.linkis.basedataManagement.edit')"
      :loading="modalLoading"
    >
      <div slot="footer">
        <Button type="text" size="large" @click="onModalCancel()">{{$t('message.linkis.basedataManagement.modal.cancel')}}</Button>
        <Button type="primary" size="large" @click="onModalOk('userConfirm')">{{$t('message.linkis.basedataManagement.modal.confirm')}}</Button>
      </div>
      <EditForm ref="editForm" :data="modalEditData" :typeOptions="datasourceTypeOptions"></EditForm>
    </Modal>
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
import mixin from '@/common/service/mixin';
import EditForm from './EditForm/index'
import {add, del, edit, getList, getAllEnv} from "./service";
import {formatDate} from "iview/src/components/date-picker/util";
export default {
  mixins: [mixin],
  components: {EditForm},
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
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.envName'),
          key: 'envName',
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.envDesc'),
          key: 'envDesc',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.name'),
          key: 'name',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.parameter'),
          key: 'parameter',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.createTime'),
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
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.updateTime'),
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
          title: this.$t('message.linkis.datasource.action'),
          width: 150,
          slot: 'action',
          align: 'center',
        },

      ],
      pageDatalist: [],
      // allEnv: [],
      modalShow: false,
      modalAddMode: 'add',
      modalEditData: {
        createTime: '',
        createUser: '',
        datasourceTypeId: '',
        envDesc: '',
        envName: '',
        id: '',
        modifyTime: '',
        modifyUser: '',
        parameter: {},
        uris: '',
        keytab: '',
        principle: '',
        hadoopConf: '',
        _index: '',
        _rowKey: ''
      },
      modalLoading: false,
      datasourceTypeOptions: []
    };
  },
  created() {
    // this.load()
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.load();
    },
    load() {
      let params = {
        searchName: this.searchName,
        currentPage: this.page.pageNow,
        pageSize: this.page.pageSize
      }
      getAllEnv().then((res) => {
        let options = [...res.typeList].sort((a, b) => a.id - b.id)
          .map(item => { return {value: +item.id, label: item.name, disabled: !['hive', 'kafka'].includes(item.name)}})
        this.datasourceTypeOptions= options
        // 获取列表
        getList(params).then((data) => {
          this.pageDatalist = data.list.list
          this.page.totalSize = data.list.total
          // 类型ID转类型名称
          this.pageDatalist.map(item => {
            let filter = options.filter(optionsItem=>{
              return optionsItem.value === item.datasourceTypeId
            })
            item.name = filter[0]?.label || '';
          })
        })
      })

    },
    changePage(value) {
      this.page.pageNow = value
      this.load()
    },
    onAdd(){
      this.clearForm();
      this.modalAddMode = 'add'
      this.modalShow = true
    },
    onTableEdit(row){
      row.hasKeyTab = JSON.parse(row.parameter).keytab ? true : false;
      this.modalEditData = {...row}
      // format parameter for modal
      if (this.modalEditData.parameter) {
        this.modalEditData.parameter = JSON.parse(this.modalEditData.parameter)
        const { uris, principle, keytab, hadoopConf } = this.modalEditData.parameter
        this.modalEditData.uris = uris;
        this.modalEditData.principle = principle;
        this.modalEditData.keytab = keytab;
        this.modalEditData.hadoopConf = hadoopConf;
      }
      this.modalAddMode = 'edit'
      this.modalShow = true
    },
    onTableDelete(row){
      this.$Modal.confirm({
        title: this.$t('message.linkis.basedataManagement.modal.modalTitle'),
        content: this.$t('message.linkis.basedataManagement.modal.modalDelete', {name: row.envName}),
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
          })
          this.load()
        }
      })

    },
    clearForm(){
      for(let key in this.modalEditData) {
        this.modalEditData[key] = ''
        this.modalEditData.parameter = {}
        window.console.log(key);
      }
      this.modalEditData.hasKeyTab = false;
    },
    onModalOk(){
      this.$refs.editForm.formModel.submit((formData)=>{
        if (!('parameter' in formData)) {
          formData['parameter'] = {}
        }

        if('pic' in formData) delete formData['pic'];
        delete formData._index
        delete formData._rowKey
        this.modalLoading = true
        const { uris, keytab, principle, hadoopConf } = formData;
        if (formData.hasKeyTab) {
          // inject props to parameter
          formData.parameter = {
            keytab, principle
          }
        }
        formData.parameter.uris = uris
        formData.parameter.hadoopConf = hadoopConf
        formData.parameter = JSON.stringify(formData.parameter)
        if('hasKeyTab' in formData) delete formData['hasKeyTab'];
        if('principle' in formData) delete formData['principle'];
        if('hadoopConf' in formData) delete formData['hadoopConf'];
        if('keytab' in formData) delete formData['keytab'];
        if('uris' in formData) delete formData['uris'];
        if(this.modalAddMode=='add') {
          add(formData).then((data)=>{
            //window.console.log(data)
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
          })
        }else {
          edit(formData).then((data)=>{
            //window.console.log(data)
            if(data.result) {
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalEditSuccess')
              })
              this.load()
            }else{
              this.$Message.success({
                duration: 3,
                content: this.$t('message.linkis.basedataManagement.modal.modalEditFail')
              })
            }
          })
        }
        window.console.log(formData);
        this.modalLoading=false
        this.modalShow = false
      })
    },
    onModalCancel(){
      this.modalLoading = false
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
