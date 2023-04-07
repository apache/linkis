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
  <div class="function-management">
    <Form class="function-management-searchbar" @submit.native.prevent :model="searchBar" inline>
      <FormItem prop="functionName" :label="$t('message.linkis.udf.functionName')">
        <Input
          v-model="searchBar.functionName"
          :placeholder="$t('message.linkis.udf.functionName')"
          style="width:120px;"
        ></Input>
      </FormItem>
      <Divider type="vertical" class="divider" />
      <FormItem prop="functionType" :label="$t('message.linkis.udf.functionType')">
        <Select v-model="searchBar.functionType" style="width:120px;">
          <Option :label="$t('message.linkis.all')" value="3,4" key="3,4" />
          <Option label="python" value="3" key="3" />
          <Option label="scala" value="4" key="4" />
        </Select>
      </FormItem>
      <Divider type="vertical" class="divider" />
      <!-- <FormItem prop="creator" :label="$t('message.linkis.udf.creator')">
        <Select v-model="searchBar.creator" style="width:120px;">
          <Option
            v-for="(item) in getCreators"
            :label="item === 'all' ? '全部': item"
            :value="item.value"
            :key="item.value"
          />
        </Select>
      </FormItem> -->
      <!-- <Divider type="vertical" class="divider" /> -->
      <FormItem>
        <Button
          type="primary"
          @click="search()"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.udf.search') }}</Button>
        <Button
          type="success"
          @click="showAddModal(true)"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.udf.addFunction') }}</Button>
      </FormItem>
    </Form>
    <div>
      <div class="function-management-table" :style="{'height': moduleHeight+'px'}">
        <Icon v-show="isLoading" type="ios-loading" size="30" class="function-management-loading" />
        <history-table
          v-if="!isLoading"
          :columns="column"
          :data="list"
          :height="moduleHeight"
          :no-data-text="$t('message.linkis.noDataText')"
          border
          stripe
          @checkall="checkChange"
          @select-change="selectChange"
        />
      </div>
      <div class="function-management-page">
        <Page
          :total="pageSetting.total"
          :page-size="pageSetting.pageSize"
          :current="pageSetting.current"
          size="small"
          show-total
          show-elevator
          @on-change="changePage"
          :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
        />
      </div>
    </div>
    <!-- 新增函数 -->
    <add-function-modal
      ref="fn"
      :loading="loading"
      :tree="shareTree"
      :load-data-fn="loadShareTreeFn"
      :filter-node="filterNode"
      :is-udf="false"
      @add="addFunction"
      @update="updateFunction"
    />
    <!-- 移交 -->
    <Modal
      :title="$t('message.linkis.udf.changeuser')"
      v-model="changUserModal"
      :mask-closable="false"
      @on-visible-change="changUserModalChange"
    >
      <span>{{$t('message.linkis.udf.changeUserTo')}}</span>
      <Select ref="userSelect" v-model="handleUser" filterable
        v-if="allUsers.length"
        :remoteMethod="filterSelect"
        @on-query-change="queryChange"
        :placeholder="$t('message.linkis.udf.inputUser')" style="width:200px;">
        <Option
          v-for="(item) in udfUsers"
          :label="item"
          :value="item"
          :key="item"
        />
      </Select>
      <Input v-if="!allUsers.length" v-model="handleUser" :placeholder="$t('message.linkis.udf.inputUser')" style="width: 250px" />
      <div slot="footer">
        <Button @click="changUserModal=false">{{$t('message.linkis.udf.cancel')}}</Button>
        <Button type="primary" :disabled="!this.handleUser" @click="changeUser">{{$t('message.linkis.udf.confirm')}}</Button>
      </div>
    </Modal>
    <Modal
      :title="$t('message.linkis.udf.share')"
      v-model="shareModal"
      :mask-closable="false"
      @on-ok="share"
    >
      <span>{{$t('message.linkis.udf.shareUser')}}</span>
      <Input
        v-model="sharedUsers"
        type="textarea"
        :autosize="{ minRows: 2, maxRows: 5 }"
        :placeholder="$t('message.linkis.udf.separateWithCommas')"
      />
    </Modal>
    <Modal
      :title="$t('message.linkis.udf.versionList')"
      v-model="vlistModal"
      width="1024"
      :mask-closable="false"
      @on-visible-change="vModalChange"
    >
      <vlist :row="handleRow" @refresh-list="search"/>
    </Modal>
  </div>
</template>
<script>
import moment from 'moment';
import storage from '@/common/helper/storage'
import table from '@/components/virtualTable'
import mixin from '@/common/service/mixin'
import api from '@/common/service/api'
import addFunctionModal from './addFunctionModal'
import vlist from './vlist'
export default {
  name: 'FunctionManagement',
  components: {
    historyTable: table.historyTable,
    addFunctionModal,
    vlist
  },
  mixins: [mixin],
  data() {
    return {
      list: [],
      column: [],
      getFunctionTypes: [],
      getCreators: [],
      isLoading: false,
      pageSetting: {
        total: 0,
        pageSize: 25,
        current: 1
      },
      searchBar: {
        functionName: '',
        functionType: '3,4',
        creator: '',
      },
      inputType: 'number',
      moduleHeight: 300,
      addFunctionModal: {
        functionName: '',
        functionType: '',
        availableCluster: ''
      },
      changUserModal: false,
      handleUser: '',
      loading: false,
      shareTree: [],
      shareModal: false,
      vlistModal: false,
      sharedUsers: '',
      handleRow: {},
      udfUsers: [],
      allUsers: []
    }
  },
  created() {
    // 获取函数类型
    // api.fetch('/configuration/engineType', 'get').then(res => {
    //   window.console.log('res.engineType: ', res.engineType);
    //   this.getFunctionTypes = ['all', ...res.engineType]
    // })
    // this.getFunctionTypes = ['all', '0', '1,2']
    // 获取创建者
    // api.fetch('/configuration/engineType', 'get').then(res => {
    //   this.getCreators = ['all', ...res.engineType]
    // })
    // 所有用户列表，移交时选择用户
  },
  mounted() {
    this.init()
    this.moduleHeight = this.$parent.$el.clientHeight - 228
    // 监听窗口变化，获取浏览器宽高
    window.addEventListener('resize', this.getHeight)
    this.getRootPath(() => {
      this.getTree((tree) => {
        if (tree) {
          this.shareTree.push(tree);
        }
      });
    });
  },
  beforeDestroy() {
    // 监听窗口变化，获取浏览器宽高
    storage.set('last-fnsearchbar-status', this.searchBar)
    window.removeEventListener('resize', this.getHeight)
  },
  activated() {
    this.init()
  },
  methods: {
    getRootPath(cb) {
      this.rootPath = storage.get('shareRootPath', 'session');
      if (!this.rootPath) {
        api.fetch(`/filesystem/getUserRootPath`, {
          pathType: 'file',
        }, 'get').then((rst) => {
          if (rst.userLocalRootPath) {
            storage.set('shareRootPath', rst.userLocalRootPath, 'session');
            this.rootPath = rst.userLocalRootPath;
            cb(true);
          } else {
            this.$Message.warning(this.$t('message.scripts.warning.getRootPath'));
            cb(false);
          }
        }).catch(() => {
          cb(false);
        });
      } else {
        cb(true);
      }
    },
    getTree(cb) {
      const timeout = setTimeout(() => {
        this.compLoading = true;
      }, 2000);
      api.fetch(`/filesystem/getDirFileTrees`, {
        path: this.rootPath,
      }, 'get')
        .then((rst) => {
          clearTimeout(timeout);
          this.compLoading = false;
          if (rst) {
            const tree = rst.dirFileTrees;
            cb(tree);
            // 树结构存储到indexedDB
          }
          this.timeoutFlag = true;
        }).catch(() => {
          this.compLoading = false;
          cb(false);
        });
    },
    loadShareTreeFn(node, cb) {
      this.treeLoading = true;
      const timeout = setTimeout(() => {
        this.compLoading = true;
      }, 2000);
      api.fetch(
        `/filesystem/getDirFileTrees`, {
          path: node.data.path,
        },
        'get'
      ).then((rst) => {
        clearTimeout(timeout);
        this.compLoading = false;
        this.treeLoading = false;
        const tree = rst.dirFileTrees.children;
        cb(tree);
        // this.$nextTick(() => {
        // seesion存储改为indexDB存储过程中，将存储的数据结构改变，以前是纯数组，现在是对象里的value值判断，所以得改项目里其他用到缓存地方的判断
        // fileTree没有变动，应该不用更新到indexDB，由于树组件打开子节点会清空
        // this.dispatch('IndexedDB:appendTree', { treeId: 'scriptTree', value: this.fileTree });
        // });
      }).catch(() => {
        clearTimeout(timeout);
        this.compLoading = false;
        this.treeLoading = false;
      })
    },
    getHeight() {
      this.moduleHeight = this.$parent.$el.clientHeight - 228
    },
    init() {
      const lastSearch = storage.get('last-fnsearchbar-status')
      if (lastSearch && lastSearch.shortcut) {
        if (lastSearch.shortcut[0] && lastSearch.shortcut[1]) {
          lastSearch.shortcut = [new Date(lastSearch.shortcut[0]), new Date(lastSearch.shortcut[1])]
        } else {
          const today = new Date(new Date().toLocaleDateString())
          lastSearch.shortcut = [today, today]
        }
        this.searchBar = lastSearch
      }
      this.search()
    },

    getParams() {
      const params = {
        udfName: this.searchBar.functionName,
        udfType: this.searchBar.functionType,
        createUser: this.searchBar.creator,
        curPage: this.pageSetting.current,
        pageSize: this.pageSetting.pageSize,
      }
      return params
    },
    changePage(page) {
      this.pageSetting.current = page
      this.search(page)
    },
    search(page) {
      this.pageSetting.current = page || 1
      this.isLoading = true
      const params = this.getParams()
      this.column = this.getColumns()
      api
        .fetch('/udf/managerPages', params, 'post')
        .then(rst => {
          this.pageSetting.total = rst.total
          this.isLoading = false
          this.list = (rst.infoList || []).map(it=>{
            it.udfTypeText = it.udfType === 3 ? 'python' : 'scala'
            it.status = it.expire ? this.$t('message.linkis.udf.expire') : this.$t('message.linkis.udf.normal')
            it.createTimeFormat = moment(it.createTime).format('YYYY-MM-DD HH:mm:ss')
            return it
          })
        })
        .catch(() => {
          this.list = [{}]
          this.isLoading = false
        })
    },
    showAddModal(show, data) {
      if(show) {
        this.$refs.fn.open(data)
      } else {
        this.$refs.fn.close()
      }
    },
    // 新增函数
    addFunction(data) {
      if (this.loading) return
      this.loading = true
      const params = {
        udfName: data.name,
        udfType: data.udfType,
        description: data.description,
        path: data.path,
        // shared: true,
        useFormat: data.useFormat,
        // expire: true,
        load: data.defaultLoad,
        registerFormat: data.registerFormat,
        sys: 'IDE',
        clusterName: data.clusterName,
        directory: data.directory
      }
      api
        .fetch('/udf/add', {udfAddVo: params}, 'post')
        .then(() => {
          this.showAddModal(false)
          this.search()
          this.isLoading = false
          this.loading = false
        })
        .catch(() => {
          // this.list = [{}]
          this.isLoading = false
          this.loading = false
        })
    },
    // 更新
    updateFunction(data) {
      if (this.loading) return
      this.loading = true
      const params = {
        udfUpdateVo: {
          id: this.handleRow.id,
          udfName: data.name,
          udfType: data.udfType,
          description: data.description,
          path: data.path,
          useFormat: data.useFormat,
          registerFormat: data.registerFormat,
          // sys: 'all',
          // clusterName: data.clusterName,
          // directory: data.directory
        }
      }
      api
        .fetch('/udf/update', params, 'post')
        .then(() => {
          this.showAddModal(false)
          this.isLoading = false
          this.loading = false
          this.search()
          this.$Message.success(this.$t('message.linkis.udf.success'));
        })
        .catch(() => {
          this.isLoading = false
          this.loading = false
        })
    },
    checkChange(v) {
      this.list = this.list.map(it => {
        it.checked = !it.disabled && v
        return it
      })
    },
    selectChange() {
      this.list = this.list.slice(0)
    },
    getColumns() {
      const column = [
        {
          title: this.$t('message.linkis.udf.functionName'),
          key: 'udfName',
          align: 'center',
          width: 150,
        },
        {
          title: this.$t('message.linkis.udf.functionType'),
          key: 'udfTypeText',
          align: 'center',
          width: 90
        },
        {
          title: this.$t('message.linkis.udf.status'),
          key: 'status',
          align: 'center',
          width: 80,
        },
        {
          title: this.$t('message.linkis.udf.availableCluster'),
          key: 'clusterName',
          align: 'center',
          width: 120,
        },
        {
          title: this.$t('message.linkis.udf.class'),
          key: 'directory',
          align: 'center',
          width: 100,
        },
        {
          title: this.$t('message.linkis.udf.functionDescription'),
          key: 'description',
          align: 'center',
          width: 150,
          // 溢出以...显示
          ellipsis: true
        },
        {
          title: this.$t('message.linkis.udf.lastModifyTime'),
          key: 'createTimeFormat',
          align: 'center',
          width: 180,
        },
        {
          title: this.$t('message.linkis.udf.creator'),
          key: 'createUser',
          align: 'center',
          width: 120,
        },
        {
          title: this.$t('message.linkis.udf.action.title'),
          key: 'action',
          align: 'center',
          width: 360,
          renderType: 'button',
          renderParams: [
            {
              label: this.$t('message.linkis.udf.action.edit'),
              action: this.edit,
              match: (v, row)=>{  return row.operationStatus.canUpdate},
              style: {
                backgroundColor: '#2d8cf0',
                color: '#fff',
                padding: '1px 7px 2px 7px !important',
                marginRight: '5px'
              }
            },
            {
              label: this.$t('message.linkis.udf.action.vlist'),
              action: this.vlist,
              style: {
                backgroundColor: '#2d8cf0',
                color: '#fff',
                padding: '1px 7px 2px 7px !important',
                marginRight: '5px'
              }
            },
            {
              label: this.$t('message.linkis.udf.action.share'),
              action: this.share,
              match: (v, row)=>{  return row.operationStatus.canShare},
              style: {
                backgroundColor: '#2d8cf0',
                color: '#fff',
                padding: '1px 7px 2px 7px !important',
                marginRight: '5px'
              }
            },
            {
              label: this.$t('message.linkis.udf.action.changeuser'),
              action: this.changeUser,
              match: (v, row)=>{  return row.operationStatus.canHandover},
              style: {
                backgroundColor: '#2d8cf0',
                color: '#fff',
                padding: '1px 7px 2px 7px !important',
                marginRight: '5px'
              }
            },
            {
              label: this.$t('message.linkis.udf.action.delete'),
              action: this.delete,
              match: (v, row)=>{  return row.operationStatus.canDelete},
              style: {
                backgroundColor: '#ed4014',
                color: '#fff',
                padding: '1px 7px 2px 7px !important',
                marginRight: '5px'
              }
            },
            {
              label: this.$t('message.linkis.udf.action.expire'),
              action: this.expire,
              match: (v, row)=>{  return row.operationStatus.canExpire},
              style: {
                backgroundColor: '#ff9900',
                color: '#fff',
                padding: '1px 7px 2px 7px !important'
              }
            },
          ]
        },
      ]
      return column
    },
    edit(args) {
      this.handleRow = args.row
      this.showAddModal(true, args.row)
    },
    delete(args) {
      if (!args.row) return
      this.$Modal.confirm({
        title: this.$t('message.linkis.modal.modalTitle'),
        content: this.$t('message.linkis.modal.modalDelete', {envName: args.row.udfName}),
        onOk: ()=>{
          api
            .fetch(`/udf/delete/${args.row.id}`, {}, 'post')
            .then(() => {
              this.search()
              this.$Message.success(this.$t('message.linkis.udf.success'));
            })
            .catch(() => {

            })
        }
      })
    },
    vlist(args) {
      this.handleRow = args.row
      this.vlistModal = true
    },
    share(args) {
      if (!this.shareModal && args) {
        this.sharedUsers = ''
        this.shareModal = true
        this.handleRow = args.row
        this.getShareUsers().then(res=> {
          this.sharedUsers = Array.isArray(res.sharedUsers) ? res.sharedUsers.join(',') : ''
        })
      } else {
        const params = {
          udfInfo: {
            id: this.handleRow.id,
            udfName: this.handleRow.name,
            udfType: this.handleRow.type
          },
          sharedUsers: this.sharedUsers.split(',').map(it=>it.trim()).filter(it=>!!it)
        }
        this.shareModal = false
        api
          .fetch('/udf/shareUDF', params, 'post')
          .then(() => {
            this.$Message.success(this.$t('message.linkis.editedSuccess'))
            this.search()
          })
          .catch(() => {

          })
      }
    },
    expire(args) {
      this.handleRow = args.row
      api
        .fetch('/udf/setExpire', {udfId: this.handleRow.id}, 'post')
        .then(() => {
          this.$Message.success(this.$t('message.linkis.editedSuccess'))
          this.search()
        })
        .catch(() => {

        })
    },
    changeUser(args) {
      if (!this.changUserModal && args) {
        this.handleUser = ''
        this.changUserModal = true
        this.handleRow = args.row

        api.fetch('/dss/framework/workspace/listAllUsers', 'get').then(res => {
          let allUsers =  (res.users || []).map(it => it.username)
          this.udfUsers = allUsers.slice(0, 150)
          this.allUsers = allUsers
        }).catch(()=>{
          this.udfUsers = [];
          this.allUsers = [];
        })
      } else {
        const params = {
          udfId: this.handleRow.id,
          handoverUser: this.handleUser
        }
        this.changUserModal = false
        api
          .fetch('/udf/handover', params, 'post')
          .then(() => {
            this.$Message.success(this.$t('message.linkis.editedSuccess'))
            this.search()
          })
          .catch(() => {

          })
      }
    },
    getShareUsers() {
      return api.fetch('/udf/getSharedUsers', {udfId: this.handleRow.id}, 'post')
    },
    filterSelect(query) {
      let options = this.allUsers.filter(it=> it.indexOf(query) > -1).slice(0, 150)
      this.udfUsers = options
      return new Promise((resolve)=>{
        resolve(options)
      })
    },
    filterNode(node) {
      const name = node.label;
      const tabSuffix = name.substr(name.lastIndexOf('.'), name.length);
      return !node.isLeaf || (node.isLeaf && (tabSuffix === '.py' || tabSuffix === '.scala'));
    },
    queryChange(q) {
      if (!q) {
        this.changUserModalChange(true)
      }
    },
    vModalChange(v) {
      if (v===false) {
        this.handleRow = {}
      }
    },
    changUserModalChange(v) {
      if (v) {
        this.$refs.userSelect && this.$refs.userSelect.setQuery(null);
        this.handleUser = ''
        let options = this.allUsers.slice(0, 150)
        this.udfUsers = options
      }
    }
  }
}
</script>
<style src="./index.scss" lang="scss"></style>
