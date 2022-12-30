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
      <Col span="6" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}">{{$t('message.linkis.tenantTagManagement.userName')}}</span>
        <Input
          v-model="queryData.user"
          class="input"
          :placeholder="$t('message.linkis.tenantTagManagement.inputUser')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="6" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}">{{$t('message.linkis.tenantTagManagement.appName')}}</span>
        <Input
          v-model="queryData.creator"
          class="input"
          :placeholder="$t('message.linkis.tenantTagManagement.inputApp')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="6" class="search-item">
        <span :style="{minWidth: '40px', textOverflow: 'ellipsis', whiteSpace: 'nowrap', overflow: 'hidden', marginRight: '5px', fontSize: '14px', lineHeight: '32px'}">{{$t('message.linkis.tenantTagManagement.tenantTag')}}</span>
        <Input
          v-model="queryData.tenantValue"
          class="input"
          :placeholder="$t('message.linkis.tenantTagManagement.inputTenant')"
          @on-enter="search"
        ></Input>
      </Col>
      <Col span="6">
        <Button type="primary" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="search">{{
          $t('message.linkis.tenantTagManagement.search')
        }}</Button>
        <Button type="primary" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="clearSearch">{{
          $t('message.linkis.tenantTagManagement.clear')
        }}</Button>
        <Button type="primary" class="button" :style="{width: '70px', marginRight: '5px', marginLeft: '5px', padding: '5px'}" @click="createTenant">{{
          $t('message.linkis.tenantTagManagement.create')
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
      @on-change="changePage"
      size="small"
      show-elevator
      :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
      style="margin: 10px; overflow: hidden; text-align: center;"
    ></Page>
    <Modal
      v-model="showCreateModal"
      class="modal"
      @on-cancel="cancel">
      <div class="form">
        <Form ref="createTenantForm" :model="modalData" label-position="left" :label-width="70" :rules="modalDataRule">
          <FormItem :label="$t('message.linkis.tenantTagManagement.userName')" prop="user">
            <Input class="input" v-model="modalData.user" @on-change="handleChange" style="width: 319px"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.tenantTagManagement.appName')" prop="creator">
            <Input class="input" v-model="modalData.creator" @on-change="handleChange" style="width: 319px"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.tenantTagManagement.tenantTag')" prop="tenantValue">
            <Input class="input" v-model="modalData.tenantValue" style="width: 319px"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.tenantTagManagement.createUser')" prop="bussinessUser">
            <Input class="input" v-model="modalData.bussinessUser" style="width: 319px"></Input>
          </FormItem>
          <FormItem :label="$t('message.linkis.tenantTagManagement.desc')" prop="desc">
            <Input class="input" v-model="modalData.desc" style="width: 319px"></Input>
          </FormItem>
        </Form>
        <div style="margin-top: 60px">
          <span style="width: 60px">{{ $t('message.linkis.tenantTagManagement.yourTagMapping') }}</span>
          <Input class="input" v-model="mapping" style="width: 240px; margin-left: 10px" disabled></Input>
          <Button type="primary" @click="checkUserTag" style="margin-left: 10px">{{$t('message.linkis.tenantTagManagement.check')}}</Button>
        </div>
      </div>
      <div slot="footer">
        <Button @click="cancel">{{$t('message.linkis.tenantTagManagement.Cancel')}}</Button>
        <Button type="primary" :disabled="tagIsExist" @click="addTenantTag">{{$t('message.linkis.tenantTagManagement.OK')}}</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import storage from "@/common/helper/storage";
import api from '@/common/service/api'
export default {
  name: 'tenantTagManagement',
  data() {
    return {
      loading: false,
      queryData: {
        user: '',
        creator: '',
        tenantValue: '',
      },
      confirmQuery: {
        user: '',
        creator: '',
        tenantValue: '',
      },
      tableColumns: [
        {
          title: "ID",
          key: 'id',
          width: 150,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.userCreator'),
          key: 'userCreator',
          width: 350,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.tenantTag'),
          key: 'tenantValue',
          width: 350,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.createUser'),
          key: 'bussinessUser',
          width: 350,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.createTime'),
          key: 'createTime',
          width: 245,
          tooltip: true,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.tenantTagManagement.action'),
          key: 'action',
          width: 200,
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
              }, this.$t('message.linkis.tenantTagManagement.edit')),
              h('Button', {
                props: {
                  type: 'error',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.delete(params.row)
                  }
                }
              }, this.$t('message.linkis.tenantTagManagement.delete'))
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
        tenantValue: '',
        bussinessUser: '',
        desc: ''
      },
      modalDataRule: {
        user: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_\*]+$/, message: this.$t('message.linkis.tenantTagManagement.contentError'), type: 'string'}
        ],
        creator: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.tenantTagManagement.contentError1'), type: 'string'}
        ],
        tenantValue: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_\*\-]+$/, message: this.$t('message.linkis.tenantTagManagement.contentError2'), type: 'string'}
        ],
        bussinessUser: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z_]+$/, message: this.$t('message.linkis.tenantTagManagement.contentError1'), type: 'string'}
        ],
        desc: [
          {required: true, message: this.$t('message.linkis.tenantTagManagement.notEmpty'), trigger: 'blur'},
          {type: 'string', max: 100, message: this.$t('message.linkis.tenantTagManagement.maxLen')}
        ]
      },
      tagIsExist: true,
      mode: 'create',
      // 缓存，用于校验
      editData: {},
      page: {
        pageSize: 10,
        pageNow: 1,
        totalPage: 0,
      },
      userName: '',
    }
  },
  computed: {
    mapping () {
      return (this.modalData.user || 'user') + '-' + (this.modalData.creator || 'creator') + '  -->  ' + (this.modalData.tenantValue || 'tenant')
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
        await api.fetch("/configuration/tenant-mapping/query-tenant-list", params, "get")
          .then((res) => {
            this.datalist = res.tenantList.map((item) => {
              item.userCreator = item.user + "-" + item.creator;
              item.createTime = new Date(item.createTime).toLocaleString();
              return item;
            });
            this.page.totalPage = res.totalPage;
          })
        this.tableLoading = false;
      } catch(err) {
        console.log(err);
        this.tableLoading = false;
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
        creator: '',
        tenantValue: '',
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
      this.modalData.bussinessUser = this.userName;
    },
    async checkUserTag() {
      this.$refs.createTenantForm.validate(async (valid) => {
        if(valid) {
          const {user, creator} = this.modalData;
          if(this.mode === 'edit' && user === this.editData.user && creator === this.editData.creator) {
            this.tagIsExist = false;
            return;
          }
          try {
            await api.fetch("/configuration/tenant-mapping/check-user-creator",
              {
                user,
                creator
              }, "get").then((res) => {
              if (res.exist) {
                this.$Message.error(this.$t('message.linkis.tenantTagManagement.userIsExisted'))
              }
              this.tagIsExist = res.exist;
            })
          } catch (err) {
            console.log(err);
            this.cancel();
          }
        }
        else {
          this.$Message.error(this.$t('message.linkis.error.validate'));
        }
      })
    },
    cancel() {
      this.showCreateModal = false;
      this.tagIsExist = true;
      this.modalData = {
        user: '',
        creator: '',
        tenantValue: '',
        bussinessUser: '',
        desc: ''
      };
      this.editData = {
        user: '',
        creator: '',
        tenantValue: '',
        bussinessUser: '',
        desc: ''
      };
      this.$refs.createTenantForm.resetFields();
    },
    addTenantTag() {
      const target = this.mode === 'edit' ? '/configuration/tenant-mapping/update-tenant' : '/configuration/tenant-mapping/create-tenant'
      this.$refs.createTenantForm.validate(async (valid) => {
        if(valid) {
          this.clearSearch();
          try {
            if(this.mode !== 'edit') {
              this.page.pageNow = 1;
            }
            await api.fetch(target, this.modalData, "post").then(async (res) => {
              console.log(res);
              await this.getTableData();
              this.cancel();
              this.$Message.success(this.$t('message.linkis.tenantTagManagement.addSuccessful'));
            });
          } catch(err) {
            this.cancel();
            console.log(err);
          }
        } else {
          this.$Message.error(this.$t('message.linkis.error.validate'));
        }
      })
      
    },
    edit(data) {
      const {
        id, user, creator, tenantValue, bussinessUser, desc
      } = data
      this.modalData = {
        id, user, creator, tenantValue, bussinessUser, desc
      };
      this.editData = {
        id, user, creator, tenantValue, bussinessUser, desc
      };
      this.showCreateModal = true;
      this.mode = 'edit';
    },
    delete(data) {
      this.$Modal.confirm({
        title: this.$t('message.linkis.tenantTagManagement.confirmDel'),
        content: this.$t('message.linkis.tenantTagManagement.isConfirmDel'),
        onOk: async () => {
          await this.confirmDelete(data);
          await this.getTableData();
        },
        onCancel: () => {
          console.log('cancel');
        }
      })
    },
    async confirmDelete(data) {
      try {
        await api.fetch('configuration/tenant-mapping/delete-tenant', {id: data.id}, 'get');
      } catch(err) {
        console.log(err);
      }
    },
    async handleChange() {
      this.tagIsExist = true;
    },
    async changePage(val) {
      this.page.pageNow = val;
      await this.getTableData();
    },
    async search() {
      const { user, creator, tenantValue } = this.queryData;
      this.confirmQuery = { user, creator, tenantValue };
      this.page.pageNow = 1;
      await this.getTableData()
    }
  },
  created() {
    this.userName = storage.get('userName') || storage.get('baseInfo', 'local').username || '';
    this.init();
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

</style>