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
      <Col span="5">
        <Card style="margin-right: 10px;">
          <template #title>
            {{$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineLabelList')}}
          </template>
          <Menu width="auto" @on-select="queryEngineTemplateList">
            <MenuItem v-for="item in engineList" :key="item.labelId" :name="item.labelId">
              <Tag checkable color="success" v-if="item.install=='yes'">已安装</Tag>
              <Tag checkable color="error" v-else>未安装</Tag>
              {{item.engineName}}
            </MenuItem>
          </Menu>
        </Card>
      </Col>
      <Col span="19">
        <Card>
          <Button type="success" class="enginBtn" @click="onTableEdit()">{{$t('message.linkis.basedataManagement.add')}}</Button>
          <Select v-model="model" multiple :max-tag-count="2" style="width: 300px" class="enginBtn">
            <Option v-for="item in showList" :value="item.value" :key="item.value">{{ item.label }}</Option>
          </Select>
          <Table stripe :columns="columns" :data="showInTableList" height="600">
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
        </Card>
      </Col>
    </Row>
    <Modal
      width="800"
      class="modal"
      v-model="modalShow"
      :title="editType == 'add' ? $t('message.linkis.basedataManagement.engineConfigurationTemplate.add') : $t('message.linkis.basedataManagement.engineConfigurationTemplate.edit')"
    >
      <div slot="footer">
        <Button type="text" size="large" @click="onModalCancel()">{{$t('message.linkis.basedataManagement.modal.cancel')}}</Button>
        <Button type="primary" size="large" @click="onModalOk()">{{$t('message.linkis.basedataManagement.modal.confirm')}}</Button>
      </div>
      <EditForm ref="editForm" :data="modalEditData"></EditForm>
    </Modal>
  </div>
</template>
<script>
import mixin from '@/common/service/mixin';
import EditForm from './EditForm/index'
import {getEngineList, queryEngineByID, deleteTemplateByID, changeTemplate} from "./service";
export default {
  mixins: [mixin],
  components: {EditForm},
  created(){
    getEngineList().then((data) => {
      this.engineList = data['success: '];
      window.console.log(this.engineList)
    })
    this.queryEngineTemplateList(this.labelId);
  },
  data(){
    return {
      editType: 'add',
      modalEditData: {},
      labelId: 5,
      modalShow: false,
      engineList: [],
      engineTemplateList: [],
      showInTableList: [],
      showList: [
        {
          value: 'advanced',
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.advanced'),
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.defaultValue'),
          value: 'defaultValue',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.description'),
          value: 'description',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineConnType'),
          value: 'engineConnType',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.hidden'),
          value: 'hidden',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.key'),
          value: 'key',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.level'),
          value: 'level',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.name'),
          value: 'name',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.treeName'),
          value: 'treeName',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateRange'),
          value: 'validateRange',
        },
        {
          label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateType'),
          value: 'validateType',
        }
      ],
      model: ['defaultValue','description','engineConnType','key','name','validateRange','validateType'],
      tableColumnNum: [
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.advanced'),
          key: 'advanced',
          width: 100,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.defaultValue'),
          key: 'defaultValue',
          width: 100,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.description'),
          key: 'description',
          tooltip: true,
          width: 300,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineConnType'),
          key: 'engineConnType',
          tooltip: true,
          width: 100,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.hidden'),
          key: 'hidden',
          tooltip: true,
          width: 100,
          align: 'center',
        },
        /*{
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.id'),
          key: 'id',
          tooltip: true,
          align: 'center',
        },*/
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.key'),
          key: 'key',
          tooltip: true,
          width: 300,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.level'),
          key: 'level',
          tooltip: true,
          width: 100,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.name'),
          key: 'name',
          tooltip: true,
          width: 200,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.treeName'),
          key: 'treeName',
          tooltip: true,
          width: 200,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateRange'),
          key: 'validateRange',
          tooltip: true,
          width: 300,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateType'),
          key: 'validateType',
          tooltip: true,
          width: 100,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.action'),
          key: "action",
          width: 150,
          slot: 'action',
          align: 'center',
        },
      ],
      copyList: []
    }
  },
  computed: {
    columns(){
      return this.tableColumnNum.filter(ele => {
        return this.model.includes(ele.key);
      }).concat([this.tableColumnNum[this.tableColumnNum.length - 1]]);
    }
  },
  methods: {
    handleOpen () {
      this.visible = true;
    },
    handleClose () {
      this.visible = false;
    },
    queryEngineTemplateList(labelId){
      this.labelId = labelId;
      queryEngineByID(labelId).then((data) => {
        this.engineTemplateList = data['success: '];
        this.convertTableData();
      })
    },
    onTableDelete(row){
      deleteTemplateByID(row.id).then((data) => {
        if(data['success: ']) {this.$Message.success(this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.delSuccess')); this.queryEngineTemplateList(this.labelId);}
        else this.$Message.error(this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.delFail'));
      });
    },
    onTableEdit(row){
      if(!row) {row = {}; this.editType = 'add';}
      else {row.engineLabelId = this.labelId; this.editType = 'edit';}
      this.engineList.forEach(ele => {
        return this.$refs.editForm.rule[1].options.push({value: +ele.labelId, label: ele.engineName});
      })
      this.modalShow = true;
      let tmp = this.engineTemplateList.find(ele => ele.id == row.id);
      tmp.engineLabelId = this.labelId;
      this.modalEditData = {...tmp};
    },
    onModalOk(){
      //window.console.log(this.$refs['editForm'].formData);
      this.modalShow = false;
      changeTemplate(this.$refs['editForm'].formData, this.editType).then((data) => {
        if(data['success: ']) {this.$Message.success('message.linkis.basedataManagement.engineConfigurationTemplate.ModSuccess'); this.queryEngineTemplateList(this.labelId);}
        else this.$Message.error('message.linkis.basedataManagement.engineConfigurationTemplate.ModSuccess');
      });
    },
    onModalCancel(){
      this.modalShow = false;
    },
    convertTableData(){
      this.showInTableList = [];
      this.engineTemplateList.forEach(ele => {
        let tmp = {...ele};
        tmp.hidden = tmp.hidden == 0 ? this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.no') : this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.yes')
        tmp.advanced = tmp.advanced == 0 ? this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.no') : this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.yes')
        this.showInTableList.push(tmp);
      })
    }
  }
};
</script>


<style lang="scss">
.drowdownItem{
  padding: 10px;
  cursor: pointer;
}
.drowdownItem:hover{
  background-color: #e8eaec;
}

.enginBtn{
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>
