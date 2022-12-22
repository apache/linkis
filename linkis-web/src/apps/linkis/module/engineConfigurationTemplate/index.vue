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
    <Row>
      <Col>
        <Button type="success" class="enginBtn" @click="onTableEdit()">{{$t('message.linkis.basedataManagement.add')}}</Button>
      </Col>
    </Row>
    <Row class="search-bar" type="flex">
      <Col span="4">
        <Card style="margin-right: 10px;">
          <template #title>
            {{$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineLabelList')}}
          </template>
          <Menu width="auto" @on-select="queryEngineTemplateList">
            <MenuItem v-for="item in engineList" :key="item.labelId" :name="item.labelId">
              {{item.engineName}}
            </MenuItem>
          </Menu>
        </Card>
      </Col>
      <!--<Col>
        <div>
          <Button type="success" class="enginBtn" @click="onTableEdit()">{{$t('message.linkis.basedataManagement.add')}}</Button>
          <Dropdown >
            <Button type="primary" class="enginBtn">
              {{$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineLabelList')}}
              <Icon type="ios-arrow-down"></Icon>
            </Button>
            <template #list>
              <DropdownMenu>
                <p class="drowdownItem" v-for="engName in engineList" :key="engName.labelId" @click="queryEngineTemplateList(engName.labelId)">{{engName.engineName}}</p>
            </template>
          </Dropdown>
        </div>
      </Col>-->
      <Col span="20">
        <Card>
          <Table stripe :columns="tableColumnNum" :data="showInTableList" height="600">
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
      title="修改"
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
      //console.log(this.engineList)
    })
    this.queryEngineTemplateList(this.labelId); //默认获取全局设置
  },
  data(){
    return {
      modalEditData: {},
      labelId: 5,
      modalShow: false,
      engineList: [],
      engineTemplateList: [],
      showInTableList: [],
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
          minWidth: 50,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.description'),
          key: 'description',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineConnType'),
          key: 'engineConnType',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.hidden'),
          key: 'hidden',
          tooltip: true,
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
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.level'),
          key: 'level',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.name'),
          key: 'name',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.treeName'),
          key: 'treeName',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateRange'),
          key: 'validateRange',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateType'),
          key: 'validateType',
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.basedataManagement.action'),
          width: 150,
          slot: 'action',
          align: 'center',
        },

      ],
    }
  },
  methods: {
    queryEngineTemplateList(labelId){
      this.labelId = labelId;
      queryEngineByID(labelId).then((data) => {
        this.engineTemplateList = data['success: '];
        this.convertTableData();
      })
    },
    onTableDelete(row){
      deleteTemplateByID(row.id).then((data) => {
        if(data['success: ']) {this.$Message.success('删除成功'); this.queryEngineTemplateList(this.labelId);}
        else this.$Message.error('删除失败');
      });
    },
    onTableEdit(row){
      if(!row) row = {};
      else row.engineLabelId = this.labelId;
      this.engineList.forEach(ele => {
        return this.$refs.editForm.rule[0].options.push({value: +ele.labelId, label: ele.engineName});
      })
      this.modalShow = true;
      let tmp = this.engineTemplateList.find(ele => ele.id == row.id);
      tmp.engineLabelId = this.labelId;
      this.modalEditData = tmp;

    },
    onModalOk(){
      //console.log(this.$refs['editForm'].formData);
      this.modalShow = false;
      changeTemplate(this.$refs['editForm'].formData).then((data) => {
        if(data['success: ']) {this.$Message.success('修改成功'); this.queryEngineTemplateList(this.labelId);}
        else this.$Message.error('修改失败');
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
