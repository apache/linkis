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
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <div class="resource-title">
      <span class="title-text" >{{$t('message.linkis.resourceManagement.resourceUsage')}}</span>
      <Icon class="title-icon" @click="refreshResource" type="md-refresh"></Icon>
    </div>
    <div class="noData" v-if="progressDataList.length <= 0">No data, try refresh</div>
    <!-- 进度条 -->
    <template  v-for="item in progressDataList" >
      <WbProgress @expandChange="expandChange" :key="item.id" :progressData="item">
      </WbProgress>
      <template v-for="subItem in item.engineTypes">
        <WbProgress v-if="item.expand" :key="subItem.id" @expandChange="expandChange" :progressData="subItem" :children="true"></WbProgress>
      </template>
    </template>
    <!-- 应用列表 -->
    <template v-if="isShowTable">
      <div class="resource-title appListTitle">
        {{$t('message.linkis.resourceManagement.applicationList')}}
      </div>
      <!-- 应用列表标签 -->
      <div class="appListTag">
        <div class="tagName">
          <span>{{$t('message.linkis.tableColumns.label')}}：</span>
          <Tag v-for="(item, index) in tagTitle" :key="index" color="primary">{{item}}</Tag>
        </div>
        <div class="resourceList">
          <span>{{$t('message.linkis.resources')}}：</span>
          <span v-if="applicationList.usedResource">
            <Tag color="success">{{`${calcCompany(applicationList.usedResource.cores)}cores,${calcCompany(applicationList.usedResource.memory, true)}G`}}(used)</Tag>
            <Tag color="error">{{`${calcCompany(applicationList.maxResource.cores)}cores,${calcCompany(applicationList.maxResource.memory, true)}G`}}(max)</Tag>
            <Tag color="warning">{{`${calcCompany(applicationList.minResource.cores)}cores,${calcCompany(applicationList.minResource.memory, true)}G`}}(min)</Tag>
          </span>
        </div>
        <div class="instanceNum" >
          <span>{{$t('message.linkis.instanceNum')}}：</span>
          <span v-if="applicationList.usedResource">{{calcCompany(applicationList.usedResource.instance)}} / {{calcCompany(applicationList.maxResource.instance)}}</span>
        </div>
      </div>
      <Table  class="table-content" border :width="tableWidth" :columns="columns" :data="tableData">
        <template slot-scope="{row}" slot="engineInstance">
          <span>{{`${row.engineType}:${row.instance}`}}</span>
        </template>
        <template slot-scope="{row}" slot="usedResource">
          <span>Linkis:({{`${calcCompany(row.resource.usedResource.cores)}cores,${calcCompany(row.resource.usedResource.memory, true)}G`}})</span>
        </template>
        <template slot-scope="{row}" slot="maxResource">
          <span>Linkis:({{`${calcCompany(row.resource.maxResource.cores)}cores,${calcCompany(row.resource.maxResource.memory, true)}G`}})</span>
        </template>
        <template slot-scope="{row}" slot="minResource">
          <span>Linkis:({{`${calcCompany(row.resource.minResource.cores)}cores,${calcCompany(row.resource.minResource.memory, true)}G`}})</span>
        </template>
        <template slot-scope="{row}" slot="startTime">
          <span>{{ timeFormat(row) }}</span>
        </template>
      </Table>
      <div class="page-bar">
        <Page
          ref="page"
          :total="page.totalSize"
          :page-size-opts="page.sizeOpts"
          :page-size="page.pageSize"
          :current="page.pageNow"
          class-name="page"
          size="small"
          show-total
          show-sizer
          @on-change="change"
          @on-page-size-change="changeSize" />
      </div>
    </template>
    <Modal
      @on-ok="submitTagEdit"
      :title="$t('message.linkis.tagEdit')"
      v-model="isTagEdit"
      :mask-closable="false">
      <Form :label-width="80" :model="formItem">
        <FormItem :label="`${$t('message.linkis.instanceName')}：`">
          <Input disabled v-model="formItem.instance" ></Input>
        </FormItem>
        <FormItem class="addTagClass" :label="`${$t('message.linkis.tableColumns.label')}：`">
          <WbTag :tagList="formItem.labels" :selectList="keyList" @addEnter="addEnter" @onCloseTag="onCloseTag"></WbTag>
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>
<script>
import WbProgress from '@/apps/linkis/components/progress';
import moment from "moment";
import api from '@/common/service/api';
import WbTag from '@/apps/linkis/components/tag';
export default {
  name: 'resourceManagement',
  data() {
    return {
      keyList: [],
      isChildren: false,
      stopList: [],
      loading: false,
      isLogAdmin: false, //账户是否有管理员权限
      formItem: {
        instance: '',
        labels: [],
        applicationName: '',
      },
      tagTitle: [],
      applicationList: {},
      addTag: true, // 是否显示添加tag按钮
      addTagForm: { // 新增标签的form表单
        key: '',
        value: ''
      },
      currentEngineData: {}, // 当前点击的引擎列表
      currentParentsData: {}, // 当前点击的主引擎
      isShowTable: false,
      addTagFormRule: { // 验证规则
        key: [
          { required: true, message: this.$t('message.linkis.keyTip'), trigger: 'blur' }
        ]
      },
      progressDataList: [],
      tableWidth: 0,
      // 开启标签修改弹框
      isTagEdit: false,
      tableData: [],
      page: {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      },
      columns: [
        {
          title: this.$t('message.linkis.tableColumns.engineInstance'),
          key: 'engineInstance',
          minWidth: 150,
          className: 'table-project-column',
          slot: 'engineInstance'
        },
        {
          title: this.$t('message.linkis.tableColumns.engineType'),
          key: 'engineType',  
          minWidth: 100,
          className: 'table-project-column'
        },
        {
          title: this.$t('message.linkis.tableColumns.status'),
          key: 'status',
          minWidth: 100,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.label'),
          key: 'label',
          minWidth: 150,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.usedResources'),
          key: 'usedResource',
          className: 'table-project-column',
          slot: 'usedResource',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.maximumAvailableResources'),
          key: 'maxResource',
          slot: 'maxResource',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.minimumAvailableResources'),
          key: 'minResource',
          slot: 'minResource',
          minWidth: 150,
          className: 'table-project-column',
        },
        {
          title: this.$t('message.linkis.tableColumns.requestApplicationName'),
          key: 'owner',
          className: 'table-project-column',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.startTime'),
          key: 'startTime',
          className: 'table-project-column',
          slot: 'startTime',
          minWidth: 150,
        },
        {
          title: this.$t('message.linkis.tableColumns.control.title'),
          key: 'action',
          width: '215',
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  type: 'error',
                  size: 'small',
                  disabled: params.row.isStop
                },
                style: {
                  marginRight: '5px',
                },
                on: {
                  click: () => {
                    this.$Modal.confirm({
                      title: this.$t('message.linkis.stop'),
                      content: this.$t('message.linkis.stopEngineTip'),
                      onOk: () => {
                        let data = [];
                        data.push({
                          engineType: 'EngineConn', // 当期需求是写死此参数
                          engineInstance: params.row.instance,
                        });
                        api.fetch(`/linkisManager/rm/enginekill`, data).then(() => {
                          // 由于引擎关闭有延迟所以记录引擎，做前端列表筛选，刷新页面或刷新
                          this.stopList.push(params.row.instance);
                          this.initExpandList();
                          this.$Message.success({
                            background: true,
                            content: 'Stop Success！！'
                          });
                        }).catch((err) => {
                          console.err(err)
                        });
                      }
                    })
                  }
                }
              }, this.$t('message.linkis.stop')),
              /*               h('Button', {
                props: {
                  type: 'primary',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.isTagEdit = true;
                    let obj = {};
                    console.log(params)
                    obj.instance = params.row.instance;
                    let labels = params.row.labels || [];
                    // 将标签数据转换成组件可渲染格式
                    obj.labels = labels.map(item => {
                      return {
                        key: item.labelKey,
                        value: item.stringValue,
                      }
                    })
                    obj.applicationName = params.row.applicationName;
                    this.formItem = Object.assign(this.formItem, obj)
                  }
                }
              }, this.$t('message.linkis.tagEdit')) */
            ]);
          }
        }
      ],
    }
  },
  components: {
    WbProgress,
    WbTag,
  },
  computed: {
    pageDatalist() {// 展示的数据
      return this.tableData.filter((item, index) => {
        return (this.page.pageNow - 1) * this.page.pageSize <= index && index < this.page.pageNow * this.page.pageSize;
      })
    },
  },
  created() {
    // 获取是否是历史管理员权限
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then((res) => {
      this.isLogAdmin = res.admin;
    })
    this.initExpandList();
    this.getKeyList();
  },
  methods: {
    // 刷新进度条
    refreshResource() {
      this.stopList = []; // 初始化停止列表，由于引擎关闭有延迟固设置此参数做判断
      this.initExpandList();
    },
    // 初始化主要数据
    initData() {
      this.page = {
        totalSize: 0,
        sizeOpts: [15, 30, 45],
        pageSize: 15,
        pageNow: 1
      };
      this.currentEngineData = {}; // 当前点击的引擎列表
      this.currentParentsData = {}; // 当前点击的主引擎
      this.isShowTable = false;
      this.progressDataList = [];
      this.tableData = [];
    },
    // 获取所有可修改的labelKey
    getKeyList() {
      api.fetch('/microservice/modifiableLabelKey', 'get').then((res) => {
        let list = res.keyList || [];
        this.keyList = list.map(item => {
          return {
            lable: item,
            value: item
          }
        })
      })
    },
    // 初始化引擎列表
    async initExpandList() {
      // 初始化数据
      this.initData();
      // 获取引擎数据
      this.loading = true;
      try {
        let engines = await api.fetch('/linkisManager/rm/userresources','post') || {};
        // 获取使用的引擎资源列表
        let enginesList = engines.userResources || [];
        enginesList.forEach((userItem, userIndex) => {
          userItem.id = new Date().valueOf() + userIndex * 1000; // 设置唯一id,将时间多乘以2000防止运行过慢导致的id重复
          userItem.expand = userIndex === 0 ? true : false;
          userItem.title = userItem.userCreator;
          // 统计获取使用量和未使用量的总量数据
          let usedResourceMemoryTotal = 0;
          let usedResourceCoresTotal = 0;
          let usedResourceInstanceTotal = 0;
          let leftResourceMemoryTotal = 0;
          let leftResourceCoresTotal = 0;
          let leftResourceInstanceTotal = 0;
          // 处理engineTypes，计算百分比
          userItem.engineTypes.forEach((engineItem,engineIndex) => {
            // 获取使用量和未使用量的对象数据
            let usedResource = engineItem.resource.usedResource || {};
            let leftResource = engineItem.resource.leftResource || {};
            // 分别获取各个的memory，cores，instance
            // usedResource
            let usedResourceMemory = usedResource.memory > 0 ? usedResource.memory : 0;
            let usedResourceCores = usedResource.cores > 0 ? usedResource.cores : 0;
            let usedResourceInstance = usedResource.instance > 0 ? usedResource.instance : 0;
            // 统计总的使用量
            usedResourceMemoryTotal += usedResourceMemory;
            usedResourceCoresTotal += usedResourceCores;
            usedResourceInstanceTotal += usedResourceInstance;
            // leftResource
            let leftResourceMemory = leftResource.memory > 0 ? leftResource.memory : 0;
            let leftResourceCores = leftResource.cores > 0 ? leftResource.cores : 0;
            let leftResourceInstance = leftResource.instance > 0 ? leftResource.instance : 0;
            // 统计总的未使用量
            leftResourceMemoryTotal += leftResourceMemory;
            leftResourceCoresTotal += leftResourceCores;
            leftResourceInstanceTotal += leftResourceInstance;
            // 计算各个的比例
            let usedResourceMemoryPercent = leftResourceMemory === 0 ? 0 : (usedResourceMemory / (usedResourceMemory + leftResourceMemory)).toFixed(2);
            let usedResourceCoresPercent = leftResourceCores === 0 ? 0 : (usedResourceCores / (usedResourceCores + leftResourceCores)).toFixed(2);
            let usedResourceInstancePercent = leftResourceInstance === 0 ? 0 : (usedResourceInstance / (usedResourceInstance + leftResourceInstance)).toFixed(2);
            // 取这三个比例中最大的进行显示
            let max = Math.max(usedResourceMemoryPercent, usedResourceCoresPercent, usedResourceInstancePercent) * 100 + '%';
            engineItem.percent =  max;
            engineItem.expand = false;
            engineItem.title = engineItem.engineType;
            engineItem.id = new Date().valueOf() + (engineIndex + 1 + userIndex ) * 2000; // 设置唯一id,将时间多乘以2000防止运行过慢导致的id重复
          })
          // 计算各个总量的比例
          let usedResourceMemoryTotalPercent = leftResourceMemoryTotal === 0 ? 0 : (usedResourceMemoryTotal / (usedResourceMemoryTotal + leftResourceMemoryTotal)).toFixed(2);
          let usedResourceCoresTotalPercent = leftResourceCoresTotal === 0 ? 0 : (usedResourceCoresTotal / (usedResourceCoresTotal + leftResourceCoresTotal)).toFixed(2);
          let usedResourceInstanceTotalPercent = leftResourceInstanceTotal === 0 ? 0 : (usedResourceInstanceTotal / (usedResourceInstanceTotal + leftResourceInstanceTotal)).toFixed(2);
          // 取这三个比例中最大的进行显示
          let max = Math.max(usedResourceMemoryTotalPercent, usedResourceCoresTotalPercent, usedResourceInstanceTotalPercent) * 100 + '%';
          userItem.percent = max;
        })
        this.progressDataList = enginesList || [];
        if(enginesList.length) await this.expandChange(enginesList[0])
        this.loading = false;
      } catch (err) {
        console.log(err)
        this.loading = false;
      }
    },
    // 展开和关闭
    async expandChange(item, isChildren = false, isRefresh = false) {
      // 显示表格
      this.isShowTable = true;
      this.isChildren = isChildren;
      // 如果点击的不为子列表则缓存下当前数据
      // 如果两次点击同一元素则阻止
      if(item.id === this.currentEngineData.id && !isRefresh) return;
      // if(item.id === this.currentParentsData.id && !isChildren) return;
      this.currentEngineData = item;
      this.loading = true;
      // 资源标签显示
      this.tagTitle = [];
      if(!isChildren) {
        this.currentParentsData = item;
        this.tagTitle = Object.assign(this.tagTitle, [item.userCreator])
      } else {
        this.tagTitle = Object.assign(this.tagTitle, [this.currentParentsData.userCreator, item.engineType])
      }
      let parameter = {
        userCreator: this.currentParentsData.userCreator,
        engineType: isChildren ? this.currentEngineData.engineType : undefined
      };
      try {
        let engines = await api.fetch('/linkisManager/rm/applicationlist', parameter, 'post') || {};
        this.loading = false;
        let engineInstances = engines.applications[0].applicationList;
        this.applicationList = engineInstances || {};
        let tableData = engineInstances.engineInstances || [];
        // 刷选是否有已经停止的引擎
        if(this.stopList.length && tableData.length) tableData = tableData.map(item => {
          if(this.stopList.includes(item.instance)) item.isStop = true;
          return item;
        })
        this.tableData = tableData;
        //更新formItem.applicationName 的数据
        this.formItem.applicationName = this.tableData[0].engineType; 
        this.page.totalSize = this.tableData.length;
      } catch (errorMsg) {
        console.error(errorMsg);
        this.loading = false;
      }
    },
    // 添加tag
    addEnter (key, value) {
      this.formItem.labels.push({ key, value });
    },
    // 删除tag
    onCloseTag (name, index) {
      this.formItem.labels.splice(index, 1);
    },
    //  提交修改
    submitTagEdit() {
      let param = JSON.parse(JSON.stringify(this.formItem));
      param.applicationName = this.tableData[0].engineType
      param.labels = param.labels.map(item => {
        return {
          labelKey: item.key,
          stringValue: item.value,
        }
      })
      api.fetch('/linkisManager/modifyEngineInfo', param, 'put').then(() => {
        this.isTagEdit = false;
        this.$Message.success(this.$t('message.linkis.editedSuccess'));
        this.expandChange(this.currentEngineData, this.isChildren, true);
      }).catch(() => {
        this.isTagEdit = false;
      })
    },
    // 切换分页
    change(val) {
      this.page.pageNow = val;
    },
    // 页容量变化
    changeSize(val) {
      this.page.pageSize = val;
      this.page.pageNow = 1;
    },
    // 时间格式转换
    timeFormat(row) {
      return moment(new Date(row.startTime)).format('YYYY-MM-DD HH:mm:ss')
    },
    calcCompany(num, isCompany = false) {
      let data = num > 0 ? num : 0;
      if(isCompany) {
        return data / 1024 / 1024 / 1024;
      }
      return data;
    },
  }
}
</script>

<style src="./index.scss" lang="scss" scoped></style>
<style lang="scss" scoped>
  .noData {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%,-50%)
  }

</style>
