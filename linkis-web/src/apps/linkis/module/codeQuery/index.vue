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
  <div class="code-query">
    <Form class="code-query-searchbar" :model="searchBar" inline label-position="left" style="width: 99%" @submit.native.prevent>
      <FormItem prop="executionCode" :label="$t('message.linkis.codeQuery.executionCode')" :label-width="60" style="width: 100%; margin-bottom: 5px">
        <Input
          v-model="searchBar.executionCode"
          :placeholder="$t('message.linkis.codeQuery.placeholder.executionCode')"
        ></Input>
      </FormItem>
    </Form>
    <div style="font-style: italic; color: #cccccc;">{{$t('message.linkis.codeQuery.searchRange')}}</div>
    <Form class="code-query-searchbar" :model="searchBar" inline label-position="left">
      <!-- <FormItem prop="executionCode" :label="$t('message.linkis.codeQuery.executionCode')" :label-width="60">
        <Input
          v-model="searchBar.executionCode"
          :placeholder="$t('message.linkis.codeQuery.placeholder.executionCode')"
        ></Input>
      </FormItem> -->
      <FormItem prop="shortcut" :label="$t('message.linkis.codeQuery.dateRange')" :label-width="60">
        <DatePicker
          :transfer="true"
          class="datepicker"
          :options="shortcutOpt"
          v-model="searchBar.shortcut"
          type="daterange"
          placement="bottom-start"
          format="yyyy-MM-dd"
          :placeholder="$t('message.linkis.codeQuery.placeholder.dateRange')"
          style="width: 180px"
          :editable="false"
        />
      </FormItem>

      <FormItem prop="creator" :label="$t('message.linkis.formItems.creator.label')" :label-width="38">
        <Input
          :maxlength="50"
          v-model.trim="searchBar.creator"
          :placeholder="$t('message.linkis.formItems.creator.placeholder')"
          style="width:90px;"
        />
      </FormItem>

      <FormItem prop="engineType" :label="$t('message.linkis.formItems.engineType')" :label-width="60">
        <Select v-model="searchBar.engineType" style="width: 90px">
          <Option v-for="(item) in getEngineTypes" :label="item === 'all' ? $t('message.linkis.engineTypes.all'): item" :value="item" :key="item" />
        </Select>
      </FormItem>
      <FormItem prop="status" :label="$t('message.linkis.formItems.status.label')" :label-width="38">
        <Select v-model="searchBar.status" style="width: 90px">
          <Option
            v-for="(item) in statusType"
            :label="item.label"
            :value="item.value"
            :key="item.value"
          />
        </Select>
      </FormItem>
      <FormItem>
        <Button
          type="primary"
          @click="confirmSearch"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.search') }}</Button>
        <Button
          type="warning"
          @click="reset"
          style="margin-right: 10px;"
        >{{ $t('message.linkis.clearSearch') }}</Button>
        <Button
          type="primary"
          @click="switchAdmin"
          v-if="isLogAdmin"
        >{{ isAdminModel ? $t('message.linkis.generalView') : $t('message.linkis.manageView') }}</Button>
      </FormItem>
    </Form>
    <Table
      border
      size="small"
      align="center"
      :columns="tableColumns"
      :data="tableData"
      :loading="tableLoading"
      class="table-content data-source-table"
      :no-data-text="this.noDataText">

    </Table>
    <Page
      :page-size="page.pageSize"
      :current="page.pageNow"
      :total="page.totalPage"
      @on-change="changePage"
      size="small"
      show-elevator
      :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
      style="position: absoulute; bottom: 10px; overflow: hidden; text-align: center;"
    ></Page>
  </div>
</template>
<script>
import api from '@/common/service/api'
import { cloneDeep } from 'lodash'
export default {
  name: 'codeQuery',
  data() {
    return {
      tableData: [],
      beforeSearch: true,
      searchBar: {
        executionCode: "",
        shortcut: [],
        status: "",
      },
      searchParams: {
        executionCode: "",
        shortcut: [],
        status: "",
      },
      tableLoading: false,
      isLogAdmin: false,
      isAdminModel: false,
      shortcutOpt: {
        shortcuts: [
          {
            text: this.$t('message.linkis.shortcuts.week'),
            value() {
              const end = new Date(new Date().toLocaleDateString())
              const start = new Date(new Date().toLocaleDateString())
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 8)
              end.setTime(end.getTime() - 3600 * 1000 * 24 * 1)
              return [start, end]
            }
          },
          {
            text: this.$t('message.linkis.shortcuts.month'),
            value() {
              const end = new Date(new Date().toLocaleDateString())
              const start = new Date(new Date().toLocaleDateString())
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 31)
              end.setTime(end.getTime() - 3600 * 1000 * 24 * 1)
              return [start, end]
            }
          },
          {
            text: this.$t('message.linkis.shortcuts.threeMonths'),
            value() {
              const end = new Date(new Date().toLocaleDateString())
              const start = new Date(new Date().toLocaleDateString())
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 91)
              end.setTime(end.getTime() - 3600 * 1000 * 24 * 1)
              return [start, end]
            }
          }
        ],
        disabledDate(date) {
          return date.valueOf() > Date.now() - 86400000
        }
      },
      getEngineTypes: [],
      statusType: [
        {
          label: this.$t('message.linkis.statusType.all'),
          value: 'all'
        },
        {
          label: this.$t('message.linkis.statusType.inited'),
          value: 'Inited'
        },
        {
          label: this.$t('message.linkis.statusType.running'),
          value: 'Running'
        },
        {
          label: this.$t('message.linkis.statusType.succeed'),
          value: 'Succeed'
        },
        {
          label: this.$t('message.linkis.statusType.cancelled'),
          value: 'Cancelled'
        },
        {
          label: this.$t('message.linkis.statusType.failed'),
          value: 'Failed'
        },
        {
          label: this.$t('message.linkis.statusType.scheduled'),
          value: 'Scheduled'
        },
        {
          label: this.$t('message.linkis.statusType.timeout'),
          value: 'Timeout'
        }
      ],
      tableColumns: [
        {
          title: this.$t('message.linkis.tenantTagManagement.action'),
          key: 'action',
          width: 100,
          align: 'center',
          render: (h, params) => {
            return h('div', [
              h('Button', {
                // props: {
                //   type: 'default',
                //   size: 'small'
                // },
                class: {
                  'render-btn': true,
                },
                on: {
                  click: () => {
                    this.check(params.row.id)
                  }
                }
              }, this.$t('message.linkis.codeQuery.check'))
            ]);
          }
        },
        {
          title: this.$t('message.linkis.codeQuery.id'),
          key: 'id',
          width: 200,
          align: 'center',
        },
        {
          title: this.$t('message.linkis.codeQuery.code'),
          key: 'executionCode',
          width: 990,
          ellipsis: true,
          align: 'center',
          render: (h, params) => {
            return h('span', {
              props: {
                type: 'text',
                size: 'small'
              },
              domProps: {
                innerHTML: params.row.executionCode
              }
            })
          }
        },
        {
          title: this.$t('message.linkis.codeQuery.status'),
          key: 'status',
          width: 350,
          ellipsis: true,
          tooltip: true,
          align: 'center',
          render: (h, params) => {
            return h('Tag', {
              props: {
                color: this.colorGetter(params.row.status)
              }
            }, this.$t(`message.linkis.statusType.${params.row.status.toLowerCase()}`))
          }
        },
      ],
      page: {
        pageSize: 10,
        pageNow: 1,
        totalPage: 0,
      },
      userName: '',
    }
  },
  computed: {
    noDataText() {
      if(this.beforeSearch) {
        return this.$t('message.linkis.noDataTextBeforeSearch')
      }
      return this.$t('message.linkis.noDataTextAfterSearch')
    },
  },
  methods: {
    confirmSearch() {
      if(this.tableLoading) return;
      this.searchParams = cloneDeep(this.searchBar)
      this.page.pageNow = 1
      this.search()
    },
    switchAdmin() {
      if (!this.tableLoading) {
        this.isAdminModel = !this.isAdminModel
        // this.searchParams = cloneDeep(this.searchBar)
        // this.page.pageNow = 1
        // this.search()
      }
    },
    async reset() {
      this.searchBar = {
        executionCode: "",
        shortcut: [],
        status: "",
      };
      this.searchParams = {
        executionCode: "",
        shortcut: [],
        status: "",
      };
      this.beforeSearch = true;
      this.tableData = [];
    },
    async changePage(val) {
      this.page.pageNow = val;
      await this.search();
    },
    async search() {
      try {
        this.tableLoading = true
        if(!this.searchParams.executionCode) {
          this.$Message.warning(this.$t('message.linkis.codeQuery.inputCode'))
          this.beforeSearch = true;
          throw Error(this.$t('message.linkis.codeQuery.inputCode'));
        }
        this.beforeSearch = false;
        const { executionCode, shortcut, status, engineType,creator } = this.searchParams;
        const { pageNow, pageSize } = this.page;
        const params = {
          executionCode,
          startDate: Date.parse(shortcut[0]),
          endDate: Date.parse(shortcut[1]) + 86399999,
          isAdminView: this.isAdminModel,
          pageNow,
          pageSize,
          status,
          engineType,
          creator
        }
        if (!shortcut[0]) {
          delete params.startDate
          delete params.endDate
        }
        if (!status || status === 'all') {
          delete params.status
        }
        if (!engineType || engineType === 'all') {
          delete params.engineType
        }
        if (!creator) {
          delete params.creator
        }

        const res = await api.fetch(`/jobhistory/es/search`, params, 'get');
        this.tableData = res.tasks.content
        this.page.totalPage = res.tasks.totalElements
        this.tableLoading = false
      } catch (err) {
        this.tableLoading = false
      }
    },
    check(id) {
      const query = {
        id
      }
      // set storage
      sessionStorage.setItem('code-search', JSON.stringify(this.searchParams));
      sessionStorage.setItem('code-use-cache', true);
      sessionStorage.setItem('code-search-page', JSON.stringify(this.page));
      sessionStorage.setItem('code-search-admin', this.isAdminModel);

      this.$router.push({
        path: '/console/codeDetail',
        query
      })
    },
    colorGetter(status) {
      switch (status) {
        case 'Running':
          return 'yellow';
        case 'Succeed':
          return 'green';
        case 'Failed':
          return 'red';
        default:
          break;
      }
    },
  },
  created() {
    // 获取是否是历史管理员权限
    api.fetch('/jobhistory/governanceStationAdmin', 'get').then(res => {
      this.isLogAdmin = res.admin
    })
    api.fetch('/configuration/engineType', 'get').then(res => {
      this.getEngineTypes = ['all', ...res.engineType]
    })
  },
  beforeRouteEnter(to, from, next) {
    if(from.name !== 'codeDetail') {
      sessionStorage.removeItem('code-search');
      sessionStorage.removeItem('code-search-page');
      sessionStorage.removeItem('code-search-admin');
      sessionStorage.setItem('code-use-cache', false);
    }
    next();
  },
  mounted() {

    if(sessionStorage.getItem('code-use-cache') === 'true') {
      this.searchParams = JSON.parse(sessionStorage.getItem('code-search'));
      this.searchBar = JSON.parse(sessionStorage.getItem('code-search'));
      this.page = JSON.parse(sessionStorage.getItem('code-search-page'));
      this.isAdminModel = JSON.parse(sessionStorage.getItem('code-search-admin'));
      if(sessionStorage.getItem('isLogAdmin') !== 'true') {
        this.isAdminModel = false;
      }
      this.search();
    }
  }
};
</script>
<style lang="scss" src="./index.scss" scoped></style>
<style lang="scss" scoped>
/deep/ .ivu-btn {
  &:hover {
    border-color: transparent;
  }
  border-color: transparent;
}
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
