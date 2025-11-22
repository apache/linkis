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
  <div class="tenantManagement">
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <Row class="search-bar">
      <Col span="4" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.ipListManagement.userName')">{{$t('message.linkis.ipListManagement.userName')}}</span>
        <Input
          v-model="queryData.user"
          class="input"
          clearable
          :placeholder="$t('message.linkis.ipListManagement.inputUser')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="4" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.ipListManagement.appName')">{{$t('message.linkis.ipListManagement.appName')}}</span>
        <Input
          v-model="queryData.creator"
          class="input"
          clearable
          :placeholder="$t('message.linkis.ipListManagement.inputApp')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="4" class="search-item">
        <span :style="{minWidth: '60px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.formItems.engineType')">{{$t('message.linkis.formItems.engineType')}}</span>
        <Select v-model="queryData.engineType" filterable clearable style="width: calc(100% - 85px);">
          <Option v-for="item in engineTypeOptions" :value="item.value" :key="item.value" :label="item.label"/>
        </Select>
      </Col>
      <Col span="4" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}" :title="$t('message.linkis.userConfig.configVKey')" >{{$t('message.linkis.userConfig.configVKey')}}</span>
        <Input
          v-model="queryData.key"
          class="input"
          clearable
          :placeholder="$t('message.linkis.userConfig.configValuePro')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="8">
        <Button type="primary" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="search">{{
          $t('message.linkis.ipListManagement.search')
        }}</Button>
        <Button type="warning" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="clearSearch">{{
          $t('message.linkis.ipListManagement.clear')
        }}</Button>
        <Button type="success" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="createTenant">{{
          $t('message.linkis.ipListManagement.create')
        }}</Button>
      </Col>
    </Row>
    <Table
      border
      size="small"
      align="center"
      :columns="tableColumns"
      :data="datalist"
      :loading="tableLoading"
      class="table-content data-source-table">

    </Table>
    <Page
      :page-size="page.pageSize"
      :current="page.pageNow"
      :total="page.totalPage"
      show-total
      @on-change="changePage"
      size="small"
      show-elevator
      :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
      style="position: absoulute; bottom: 10px; overflow: hidden; text-align: center;"
    ></Page>
    <Modal
      v-model="showCreateModal"
      class="modal"
      @on-cancel="cancel">
      <div class="form">
        <Form ref="createTenantForm" :model="modalData" label-position="left" :label-width="100" :rules="modalDataRule">
          <FormItem :label="$t('message.linkis.ipListManagement.userName')" prop="user">
            <Input class="input" v-model="modalData.user" :disabled="isEdit" clearable  style="width: 319px" :placeholder="$t('message.linkis.ipListManagement.inputUser')"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.ipListManagement.appName')" prop="creator">
            <Input class="input" v-model="modalData.creator"  :disabled="isEdit" clearable style="width: 319px" :placeholder="$t('message.linkis.ipListManagement.inputApp')"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.formItems.engineType')" prop="engineType">
            <Select v-model="modalData.engineType" clearable :disabled="isEdit" filterable @on-change="changeEngineType" style="width: 319px;">
              <Option v-for="item in allEngineTypeOptions" :value="item.value" :key="item.value" :label="item.label"/>
            </Select>
          </FormItem>
          <FormItem :label="$t('message.linkis.EnginePluginManagement.engineConnVersion')" prop="version">
            <Select v-model="modalData.version" :disabled="noVersionSelectConfig || isEdit" filterable clearable @on-open-change="fetchVersionOption"  @on-change="changFormVal" style="width: 319px;">
              <Option v-for="item in versionOption" :value="item.value" :key="item.value" >{{item.label}}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('message.linkis.userConfig.configVKey')" prop="configKey">
            <Select v-model="modalData.configKey" :disabled="noSelectConfig || isEdit" filterable clearable @on-open-change="fetchConfigOption" @on-select="onSelect" style="width: 319px;">
              <Option v-for="item in configOption" :key="item.value" :value="item.value" >{{item.label}}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('message.linkis.userConfig.configValue')" :rules="{required: !!defaultValue , message: $t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'}" prop="configValue">
            <Input class="input" v-model="modalData.configValue" clearable  style="width: 319px" :placeholder="$t('message.linkis.userConfig.configValuePro')"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.userConfig.isForceVerify')" prop="force">
            <RadioGroup v-model="modalData.force">
              <Radio label="false">{{$t('message.linkis.userConfig.yes')}}</Radio>
              <Radio label="true">{{$t('message.linkis.userConfig.no')}}</Radio>
            </RadioGroup>
          </FormItem>
        </Form>
      </div>
      <div slot="footer">
        <Button @click="cancel">{{$t('message.linkis.ipListManagement.Cancel')}}</Button>
        <Button type="primary" :disabled="btnDisabled" @click="addUserConfig" :loading="isRequesting">{{$t('message.common.ok')}}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import storage from "@/common/helper/storage";
import api from '@/common/service/api'
export default {
  name: 'userConfig',
  data() {
    return {
      loading: false,
      queryData: {
        user: '',
        creator: '',
        engineType: '',
        key: ''
      },
      confirmQuery: {
        user: '',
        creator: '',
        engineType: '',
        key: ''
      },
      tableColumns: [
        {
          title: this.$t('message.linkis.ipListManagement.userName'),
          key: 'user',
          width: 150,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.appName'),
          key: 'creator',
          width: 140,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.formItems.engineType'),
          key: 'engineType',
          width: 110,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.EnginePluginManagement.engineConnVersion'),
          key: 'version',
          width: 130,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.userConfig.configVKey'),
          key: 'key',
          width: 260,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.userConfig.keyName'),
          key: 'name',
          width: 260,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.userConfig.defaultValue'),
          key: 'defaultValue',
          width: 240,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.userConfig.configValue'),
          key: 'configValue',
          width: 240,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.ipListManagement.action'),
          key: 'action',
          width: 100,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  type: 'primary',
                  size: 'small'
                },
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.edit(params.row)
                  }
                }
              }, this.$t('message.linkis.ipListManagement.edit'))
            ]);
          }
        }
      ],
      datalist: [],
      tableLoading: false,
      showCreateModal: false,
      modalData: {
        user: '',
        creator: '',
        engineType: '',
        version: '',
        configKey: '',
        configValue: '',
        force: "false",
        configKeyId: ''
      },
      modalDataRule: {
        user: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_\*]+$/, message: this.$t('message.linkis.ipListManagement.contentError'), type: 'string'}
        ],
        creator: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_*]+$/, message: this.$t('message.linkis.ipListManagement.contentError'), type: 'string'}
        ],
        engineType: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'}
        ],
        version: [
          {required: true, message: this.$t('message.linkis.ipListManagement.notEmpty'), trigger: 'blur'}
        ],
        configKey: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'}
        ]
      },
      btnDisabled: false,
      mode: 'create',
      page: {
        pageSize: 10,
        pageNow: 1,
        totalPage: 0,
      },
      userName: '',
      isRequesting: false,
      engineTypeOptions: [],
      allEngineTypeOptions: [],
      configOption: [],
      versionOption: [],
      defaultValue: '',
    }
  },
  computed: {
    isEdit () {
      return this.mode === 'edit'
    },
    noVersionSelectConfig () {
      return !(this.modalData.engineType) || this.isEdit;
    },
    noSelectConfig () {
      const valid = !(this.modalData.user && this.modalData.creator && this.modalData.engineType && this.modalData.version)
      !valid && this.changFormVal();
      return valid;
    }
  },
  methods: {
    async getTableData() {
      try {
        this.tableLoading = true;
        let params = {};
        const keys = Object.keys(this.confirmQuery);
        for (let i = 0; i< keys.length; i++) {
          if(this.confirmQuery[keys[i]]) {
            params[[keys[i]]] = this.confirmQuery[keys[i]];
          }
        }
        params.pageNow = this.page.pageNow;
        params.pageSize = this.page.pageSize;
        const res = await api.fetch("/configuration/userKeyValue", params, "get")

        this.datalist = res.configValueList.map((item) => {
          const labelValue = item.labelValue;
          const baseArr = labelValue.split(',');
          const userArr = baseArr[0].split('-');
          const engineArr = baseArr[1].split('-');
          item.user = userArr[0];
          item.creator = userArr[1];
          item.engineType = engineArr[0];
          item.version = engineArr[1];
          return item;
        });
        this.page.totalPage = res.totalPage;
        this.tableLoading = false;
      } catch(err) {
        this.tableLoading = false;
      }

    },
    async getEngineOptionsData() {
      try {
        api.fetch("/configuration/engineType", {}, "get")
          .then((res) => {
            res.engineType = res.engineType.map((item) => {
              return {label: item, value: item};
            });
            this.engineTypeOptions = [...res.engineType];
            this.allEngineTypeOptions = [...res.engineType];
            this.allEngineTypeOptions.unshift({label: this.$t('message.linkis.EnginePluginManagement.globalConfig'),value: '*'});
          })
      } catch(err) {
        return;
      }
    },
    async init() {
      this.loading = true;
      await this.getTableData();
      this.loading = false;
    },
    async clearSearch() {
      this.queryData = {
        user: '',
        creator: ''
      };
      this.confirmQuery = {
        user: '',
        creator: ''
      }
      this.page.pageNow = 1;
      await this.getTableData()
    },
    async createTenant () {
      this.showCreateModal = true;
      this.mode = 'create';
    },
    cancel() {
      this.showCreateModal = false;
      this.modalData = {
        user: '',
        creator: '',
        engineType: '',
        version: '',
        configKey: '',
        configValue: '',
        configKeyId: '',
        force: 'false'
      }
      this.configOption = [];
      this.versionOption = [];
    },
    addUserConfig() {
      if(this.isRequesting) return;
      const target = '/configuration/keyvalue'
      this.$refs.createTenantForm.validate(async (valid) => {
        if(valid) {
          this.clearSearch();
          try {
            if(this.mode !== 'edit') {
              this.page.pageNow = 1;
            }
            this.isRequesting = true
            const { force, configKeyId } = this.modalData
            await api.fetch(target, {...this.modalData, force: JSON.parse(force), configKeyId: String(configKeyId)}, "post")
            await this.getTableData();
            this.cancel();
            this.$Message.success(this.$t('message.linkis.udf.success'));
            this.isRequesting = false
          } catch(err) {
            this.isRequesting = false
          }
        } else {
          this.$Message.error(this.$t('message.linkis.error.validate'));
        }
      })

    },
    edit(data) {
      const {
        user, creator, engineType, version, key, configValue, defaultValue, configKeyId
      } = data;
      this.configOption.push({value: key, label: key});
      this.defaultValue = defaultValue
      this.modalData = {
        user, creator, engineType, version, configKey: key, configValue, force: 'false', configKeyId
      };
      this.versionOption = [{label: version, value: version}];
      this.showCreateModal = true;
      this.mode = 'edit';
    },
    ipListValidator(rule, val, cb) {
      if (!val) {
        cb(new Error(this.$t('message.linkis.ipListManagement.notEmpty')));
      }
      if(val === '*') {
        cb();
      }
      const ipArr = val.split(',');
      ipArr.forEach(ip => {
        if(!/^((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/.test(ip)) {
          cb(new Error(this.$t('message.linkis.ipListManagement.ipContentError')));
        }
      })
      cb();
    },
    async changePage(val) {
      this.page.pageNow = val;
      await this.getTableData();
    },
    async search() {
      const { user, creator, engineType, key } = this.queryData;
      this.confirmQuery = { user, creator, engineType, key };
      this.page.pageNow = 1;
      await this.getTableData()
    },
    changeEngineType(val) {
      if (val === '*') {
        this.modalData.version = "*";
        this.versionOption = [{label: this.$t('message.linkis.all'),value: '*'}];
      } else {
        this.modalData.version = "";
        this.versionOption = [];
      }
      this.modalData.configKey = '';
    },
    fetchVersionOption(val) {
      if (!val || this.isEdit) return;
      api.fetch(`/engineplugin/getTypeVersionList/${this.modalData.engineType}`, {}, 'get').then((res) => {
        let keyArr = [];
        res.queryList.forEach((item) => {
          if (!keyArr.includes(item)) {
            keyArr.push({label: item, value: item});
          }
        });
        if (this.modalData.version === "*") {
          keyArr.unshift({label: this.$t('message.linkis.all'),value: '*'});
        }
        this.versionOption = keyArr;
      })
    },
    changFormVal() {
      if (this.isEdit) return;
      this.modalData.configKey = '';
    },
    fetchConfigOption(val) {
      if (!val || this.isEdit) return;
      const {user, creator, engineType, version} = this.modalData;
      api.fetch('/configuration/getItemList', {user, creator, engineType, version, pageSize: 10000}, 'get').then((res) => {
        let keyArr = [];
        res.itemList.forEach((item) => {
          if (!keyArr.includes(item.key)) {
            keyArr.push({label: item.key, value: item.key, defaultValue: item.defaultValue});
          }
        });
        this.configOption = keyArr;
      })
    },
    onSelect(val) {
      this.defaultValue = this.configOption.find((item) => {
        return item.value === val
      })?.defaultValue
    }
  },
  created() {
    this.userName = storage.get('userName') || storage.get('baseInfo', 'local')?.username || '';
    this.init();
    this.getEngineOptionsData();
  }

};
</script>
<style lang="scss" src="./index.scss" scoped></style>
<style lang="scss" scoped>

.modal {

  .input-area {
    padding: 20px 50px;
    .item {
      display: flex;
      margin-bottom: 10px;
      .input {
        width: calc(100% - 66px);
      }
    }
  }
}
.search-bar .ivu-input-wrapper {
  width: calc(100% - 75px);
}
</style>
<style lang="scss">
.ivu-tooltip-popper {
  .ivu-tooltip-content {
    .ivu-tooltip-inner-with-width {
      word-wrap: break-word;
    }
  }
}

</style>
