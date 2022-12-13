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
    <Table class="table-content" border :columns="cols" :data="pageList" :loading="loading"></Table>
    <Page
      style="text-align:center;margin-top:10px"
      :total="pageSetting.total"
      :page-size="pageSetting.pageSize"
      :current="pageSetting.current"
      size="small"
      show-total
      show-elevator
      @on-change="changePage"
    />
    <Modal v-model="showContent" footer-hide width="80%" :title="$t('message.linkis.udf.viewSourceCode')" class="view-code">
      <Spin v-if="loadingContent" size="large" fix />
      <Input
        v-show="!loadingContent"
        type="textarea"
        v-model="content"
        :disabled="true"
        :autosize="{ minRows: 5, maxRows: 20 }"
      />
    </Modal>
  </div>
</template>
<script>
import moment from 'moment'
import api from '@/common/service/api'
import axios from 'axios'
export default {
  props: {
    row: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      cols: [
        {
          title: this.$t('message.linkis.udf.version'),
          key: 'bmlResourceVersion',
          align: 'center',
          width: 95
        },
        {
          title: this.$t('message.linkis.udf.publish'),
          key: 'publish',
          align: 'center',
          width: 75
        },
        {
          title: this.$t('message.linkis.udf.status'),
          key: 'status',
          align: 'center',
          width: 80
        },
        {
          title: this.$t('message.linkis.udf.notes'),
          key: 'description',
          align: 'center',
          width: 170
        },
        {
          title: this.$t('message.linkis.udf.lastModifyTime'),
          key: 'createTimeFormat',
          align: 'center',
          width: 160
        },
        {
          title: this.$t('message.linkis.udf.creator'),
          key: 'createUser',
          align: 'center',
          width: 120
        }, {
          title: this.$t('message.linkis.udf.action.title'),
          align: 'center',
          width: 285,
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  size: 'small',
                },
                style: {
                  marginRight: '5px',
                },
                on: {
                  click: () => {
                    this.back(params.row)
                  }
                }
              }, this.$t('message.linkis.udf.createNewVersion')),
              h('Button', {
                props: {
                  size: 'small',
                },
                style: {
                  marginRight: '5px',
                  display: this.row && this.row.operationStatus && this.row.operationStatus.canPublish ? 'inline-block' : 'none'
                },
                on: {
                  click: () => {
                    this.publish(params.row)
                  }
                }
              }, this.$t('message.linkis.udf.publish')),
              h('Button', {
                props: {
                  size: 'small',
                },
                style: {
                  marginRight: '5px',
                },
                on: {
                  click: () => {
                    this.download(params.row)
                  }
                }
              }, this.$t('message.linkis.udf.download')),
              h('Button', {
                props: {
                  size: 'small',
                },
                style: {
                  marginRight: '5px',
                  display: this.row.udfType === 0 ? 'none' : 'inline-block'
                },
                on: {
                  click: () => {
                    this.viewCode(params.row)
                  }
                }
              }, this.$t('message.linkis.udf.viewSourceCode'))
            ]);
          }
        }],
      data: [],
      loading: false,
      loadingContent: false,
      content: '',
      showContent: false,
      pageSetting: {
        total: 0,
        pageSize: 10,
        current: 1
      }
    }
  },
  watch: {
    "row": function () {
      this.fetchList()
    }
  },
  computed: {
    pageList() {
      return this.data.slice((this.pageSetting.current - 1) * this.pageSetting.pageSize, this.pageSetting.current * this.pageSetting.pageSize)
    }
  },
  mounted() {
  },
  methods: {
    fetchList() {
      if (this.row && this.row.id) {
        this.loading = true
        api
          .fetch('/udf/versionList', {
            udfId: this.row.id
          }, 'get')
          .then((res) => {
            this.data = (res.versionList || []).map(it => {
              it.status = it.expire ? this.$t('message.linkis.udf.expire') : this.$t('message.linkis.udf.normal')
              it.publish = it.published ? this.$t('message.linkis.udf.yes') : this.$t('message.linkis.udf.no')
              it.createTimeFormat = moment(it.createTime).format('YYYY-MM-DD HH:mm:ss')
              return it
            })
            this.pageSetting.total = this.data.length
            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
      }
    },
    viewCode(row) {
      this.showContent = true
      this.loadingContent = true
      api
        .fetch('/udf/downloadUdf', {
          udfId: row.udfId,
          version: row.bmlResourceVersion
        }, 'post')
        .then((data) => {
          this.content = (data && data.content) || ''
          this.loadingContent = false
        })
        .catch(() => {
          this.showContent = false
          this.loadingContent = false
        })
    },
    back(row) {
      this.loading = true
      api
        .fetch('/udf/rollback', {
          udfId: row.udfId,
          version: row.bmlResourceVersion
        }, 'post')
        .then(() => {
          this.fetchList()
          this.$Message.success(this.$t('message.linkis.udf.success'))
          this.$emit('refresh-list')
        })
        .catch(() => {
          this.loading = false
        })
    },
    publish(row) {
      this.loading = true
      api
        .fetch('/udf/publish', {
          udfId: row.udfId,
          version: row.bmlResourceVersion
        }, 'post')
        .then(() => {
          this.$Message.success(this.$t('message.linkis.udf.success'))
          this.fetchList()
        })
        .catch(() => {
          this.loading = false
        })
    },
    download(row) {
      axios({
        url: process.env.VUE_APP_MN_CONFIG_PREFIX || `http://${window.location.host}/api/rest_j/v1/udf/downloadToLocal`,
        method: 'post',
        responseType: 'blob',
        data: {
          udfId: row.udfId,
          version: row.bmlResourceVersion
        }
      })
        .then((res) => {
          let blob = res.data
          let url = window.URL.createObjectURL(blob);
          let l = document.createElement('a')
          l.href = url;
          let name = row.path.split('/').pop()
          l.download = name
          document.body.appendChild(l);
          l.click()
          window.URL.revokeObjectURL(url)
          this.$Message.success(this.$t('message.linkis.udf.success'))
        })
        .catch(() => {
          this.loading = false
        })
    },
    changePage(page) {
      this.pageSetting.current = page
    }
  }
};
</script>
