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
  <div
    ref="view"
    class="we-result-view">
    <we-toolbar
      ref="toolbar"
      v-show="result.path"
      :current-path="result.path"
      :all-path="result.allPath"
      :show-filter="tableData.type !== 'normal'"
      :script="script"
      :row="hightLightRow"
      :visualShow="visualShow"
      :dispatch="dispatch"
      :getResultUrl="getResultUrl"
      :comData="comData"
      :result-type="resultType"
      @on-analysis="$emit('on-analysis', arguments[0])"
      @on-filter="handleFilterView" />
    <we-filter-view
      v-if="isFilterViewShow"
      :head-rows="heads"
      :height="resultHeight"
      @on-check="filterList"></we-filter-view>
    <Spin
      v-show="isLoading"
      size="large"
      fix />
    <div
      v-if="resultType === '1'"
      :style="{'height': resultHeight+'px'}"
      class="text-result-div">
      <div v-if="result.bodyRows">
        <!-- The data format is not uniform, first loop the external data, and then loop the internal(数据格式不统一，先循环外部数据，再循环内部 )-->
        <div v-for="(item,index) in result.bodyRows" :key="index">
          <div
            v-for="(row, subindex) in item[0].split('\n')"
            :key="subindex">{{ row }}</div>
        </div>
      </div>
      <span
        v-else
        class="empty-text"></span>
    </div>
    <div
      v-else-if="resultType === '2' && tableData.type && visualShow=== 'table'"
      class="result-table-content"
      :class="{'table-box': tableData.type === 'normal'}">
      <template v-if="tableData.type === 'normal' && !result.hugeData">
        <wb-table
          border
          highlight-row
          outer-sort
          :tableHeight="resultHeight"
          :columns="data.headRows"
          :tableList="data.bodyRows"
          @on-current-change="onRowClick"
          @on-head-dblclick="copyLabel"
          @on-sort-change="sortChange"
          class="result-normal-table">
        </wb-table>
        <we-water-mask
          v-if="isWaterMask"
          :text="watermaskText"
          ref="watermask"></we-water-mask>
      </template>
      <template v-else-if="!result.hugeData">
        <we-table
          ref="resultTable"
          :data="data"
          :width="tableData.width"
          :height="resultHeight"
          :size="tableData.size"
          @dbl-click="copyLabel"
          @on-click="onWeTableRowClick"
          @on-sort-change="sortChange"/>
        <we-water-mask
          v-if="isWaterMask"
          :text="watermaskText"
          ref="watermask"></we-water-mask>
      </template>
      <div v-if="result.hugeData" :style="{height: resultHeight+'px', padding: '15px'}">
        {{ $t('message.linkis.resultSet.prefixText') }}<a :href="`/#/results?resultPath=${resultPath}&fileName=${$route.query.fileName}`" target="_blank">{{ $t('message.linkis.resultSet.linkText') }}</a>
      </div>
    </div>
    <div v-else-if="['visual', 'dataWrangler'].includes(visualShow)  && resultType === '2'">
      <visualAnalysis v-if="visualShow === 'visual' && visualParams"
        :visualParams="visualParams"
        :style="{'height': (resultHeight + 40) +'px'}">
      </visualAnalysis>
      <dataWrangler v-if="visualShow === 'dataWrangler' && dataWranglerParams"
        :dataWranglerParams="dataWranglerParams"
        :style="{'height': resultHeight +'px'}"
      >
      </dataWrangler>
    </div>
    <div v-else-if="resultType === '4'">
      <iframe
        id="iframeName"
        :style="{'height': resultHeight+'px'}"
        :srcdoc="htmlData"
        frameborder="0"
        width="100%"
        @change="iframeOnChange()" />
    </div>
    <div
      v-else-if="resultType === '5'"
      :style="{'height': resultHeight+'px'}"
      class="html-result-div"
      v-html="result.bodyRows[0][0]"/>
    <span
      v-else
      class="empty-text"></span>
    <div class="we-page-container" v-if="visualShow!== 'visual' && visualShow!== 'dataWrangler'">
      <result-set-list
        v-if="script.resultList && script.resultList.length>1"
        :current="script.resultSet - 0"
        :list="script.resultList"
        @change="changeSet">
      </result-set-list>
      <div class="page-hint">
        <Page
          :transfer="true"
          v-if="resultType === '2'"
          ref="page"
          :total="tableData.total"
          :page-size-opts="page.sizeOpts"
          :page-size="page.size"
          :current="page.current"
          class-name="page"
          size="small"
          show-total
          show-sizer
          :prev-text="$t('message.linkis.previousPage')" :next-text="$t('message.linkis.nextPage')"
          @on-change="change"
          @on-page-size-change="changeSize" />
        <span class="hint-text">{{result.total === 5000 ? $t('message.linkis.resultSet.largeResultTips') : ''}}</span>
      </div>
    </div>
    <we-menu
      ref="contextMenu"
      id="file">
      <we-menu-item>
        <span>行转列</span>
      </we-menu-item>
    </we-menu>
  </div>
</template>
<script>
import visualAnalysis from './visualAnalysis.vue';
import util from '@/common/util';
import Table from '@/components/virtualTable';
import WbTable from '@/components/table';
import WeWaterMask from '@/components/watermark';
import WeToolbar from './toolbar.vue';
import elementResizeEvent from '@/common/helper/elementResizeEvent';
import resultSetList from './resultSetList.vue';
import filter from './filter.vue';
import dataWrangler from './dataWrangler.vue';
import mixin from '@/common/service/mixin';
/**
 * Script execution result set tab panel(脚本执行结果集tab面板)
 * ! 1. Shared with the management console console.vue after the workflow node is executed(与工作流节点执行后的管理台console.vue 共用)
 */
export default {
  name: 'scriptResult',
  components: {
    WbTable,
    WeTable: Table.WeTable,
    WeToolbar,
    WeWaterMask,
    resultSetList,
    WeFilterView: filter,
    visualAnalysis,
    dataWrangler
  },
  props: {
    script: [Object],
    visualShow: {
      type: String,
      default: 'table'
    },
    visualParams: Object,
    scriptViewState: Object,
    dataWranglerParams: Object,
    dispatch: {
      type: Function,
      required: true
    },
    getResultUrl: {
      type: String,
      defalut: `filesystem`
    },
    comData: {
      type: Object
    },
  },
  mixins: [mixin],
  data() {
    const username = this.getUserName();
    return {
      isWaterMask: false,
      watermaskText: `${this.$t('message.common.watermaskText')}-${username}`,
      data: {
        headRows: [],
        bodyRows: [],
      },
      heads: [],
      tableData: {
        type: 'normal',
        width: 1000,
        size: 100,
        total: 100,
      },
      page: {
        current: 1,
        size: 50,
        sizeOpts: [20, 50, 80, 100],
      },
      isLoading: false,
      // currently highlighted line(当前高亮的行)
      hightLightRow: null,
      isFilterViewShow: false
    };
  },
  computed: {
    result() {
      let res = {
        headRows: [],
        bodyRows: [],
        type: 'EMPTY',
        total: 0,
        path: '',
        cache: {},
        allPath: []
      };
      if (this.script.resultList && this.script.resultSet !== undefined) {
        res = this.script.resultList[this.script.resultSet].result
        if(res) {
          res.allPath = this.script.resultList.map(result => result.path);
        }
      }
      if(!res && this.script.result){
        res = this.script.result
      }
      return res;
    },
    htmlData() {
      return this.result.bodyRows[0] && this.result.bodyRows[0][0]
    },
    resultType() {
      return this.result.type;
    },
    resultHeight() {
      return this.scriptViewState.bottomContentHeight
    },
    resultPath() {
      let path = ''
      if (this.script.resultList && this.script.resultSet !== undefined) {
        path =  this.script.resultList[this.script.resultSet].path
      }
      return path
    }
  },
  watch: {
    '$route'(val) {
      if (val.path === '/home') {
        this.data.headRows = this.data.headRows.slice();
      }
    },
    script: {
      deep: true,
      handler: function() {
        this.updateData();
      }
    }
  },
  mounted() {
    elementResizeEvent.bind(this.$el, this.resize);
    this.updateData();
    this.initPage();
  },
  beforeDestroy: function() {
    elementResizeEvent.unbind(this.$el);
  },
  methods: {
    initPage() {
      if (this.result.current && this.result.size) {
        this.page = Object.assign({...this.page}, {
          current: this.result.current,
          size: this.result.size,
        });
      }
      this.change(this.page.current);
    },
    sortByCol(col) {
      let sortIndex
      this.data.headRows.map((head, index) => {
        if (head.content === col.content) {
          sortIndex = index
        }
      })
      // 大于50列排序现将要排序的列和原始index保持(大于50列排序现将要排序的列和原始index保持)
      let sortColumnAll = this.originRows.map((row, index) => {
        return {
          originIndex: index,
          value: row[sortIndex]
        }
      })
      // Sort the found columns(将找出的列排序)
      sortColumnAll = this.arraySortByName(sortColumnAll, col.columnType, 'value');// From small to large(从小到大)
      let newRow = [];
      sortColumnAll.map((item, index) => {
        newRow[index] = this.originRows[item.originIndex];
      })
      return newRow;
    },
    sortChange({column, key, order, cb}) {
      let list = []
      if (order === 'normal') {// restore original data(恢复原来数据)
        if (this.tableData.type === 'normal') {
          list = this.result.bodyRows.map((row) => {
            let newItem = {};
            row.forEach((item, index) => {
              Object.assign(newItem, {
                [this.result.headRows[index].columnName]: item,
              });
            });
            return newItem;
          });
        } else {
          list = this.result.bodyRows || [];
        }
      } else {
        if (this.tableData.type === 'normal') {
          list = this.arraySortByName(this.originRows, column.columnType, key);// 从小到大
        } else {
          list = this.sortByCol(column)
        }

        if (order === 'asc') {// 升序
        } else if (order === 'desc') {// 降序
          list.reverse();
        }
      }
      this.originRows = list;
      this.sortType = order;
      // The sorted data is stored(排序后的数据进行存储)
      const tmpResult = this.getResult();
      // restore original fields(还原初始字段)
      tmpResult.sortBodyRows = this.originRows.map(items => {
        return Object.values(items);
      });
      tmpResult.sortType = this.sortType;
      tmpResult.current = this.page.current;
      tmpResult.size = this.pagesize;
      const resultSet = this.script.resultSet || 0;
      this.$set(this.script.resultList[resultSet], 'result', tmpResult);
      this.dispatch('IndexedDB:updateResult', {
        tabId: this.script.id,
        resultSet: resultSet,
        showPanel: this.scriptViewState.showPanel,
        ...this.script.resultList,
      });
      this.pageingData();
      if (cb) {
        cb()
      }
    },
    arraySortByName(list, valueType, key) {
      if (list === undefined || list === null) return [];
      list.sort((a, b) => {
        let strA = a[key];
        let strB = b[key];
        // Who is the illegal value and who is in front(谁为非法值谁在前面)
        if (strA === undefined || strA === null || strA === '' || strA === ' ' || strA === '　' || strA === 'NULL') {
          return -1;
        }
        if (strB === undefined || strB === null || strB === '' || strB === ' ' || strB === '　' || strB === 'NULL') {
          return 1;
        }
        // if integer size(如果为整数型大小)
        if (['int', 'float', 'double', 'long', 'short', 'bigint', 'decimal'].includes(valueType.toLowerCase())) {

          return strA - strB;
        }
        const charAry = strA.split('');
        for (const i in charAry) {
          if ((this.charCompare(strA[i], strB[i]) !== 0)) {
            return this.charCompare(strA[i], strB[i]);
          }
        }
        // If you can't compare it through the above loop comparison, there is no solution, and return -1 directly(如果通过上面的循环对比还比不出来，就无解了，直接返回-1)
        return -1;
      });
      return list;
    },
    charCompare(charA, charB) {
      // Who is the illegal value and who is in front(谁为非法值谁在前面)
      if (charA === undefined || charA === null || charA === '' || charA === ' ' || charA === '　') {
        return -1;
      }
      if (charB === undefined || charB === null || charB === '' || charB === ' ' || charB === '　') {
        return 1;
      }
      return charA.localeCompare(charB);
    },
    notChinese(char) {
      const charCode = char.charCodeAt(0);
      return charCode >= 0 && charCode <= 128;
    },
    updateData() {
      /**
         * result.type
         * TEXT_TYPE: '1'
         * TABLE_TYPE: '2'
         * IO_TYPE: '3'
         * PICTURE_TYPE: '4'
         * HTML_TYPE: '5'
         */
      if (this.result.type === '2') {
        this.tableData.type = this.result.headRows.length > 50 ? 'virtual' : 'normal';
        let headRows = this.result.headRows || [];
        let heads = [];
        let bodyRows = [];
        // Determine whether to sort(判断是否进行过排序)
        switch(this.result.sortType) {
          case 'asc':
          case 'desc':
            this.originRows = this.result.sortBodyRows || [];
            this.sortType = this.result.sortType;
            break;
          default:
            this.originRows = this.result.bodyRows || [];
            break;
        }
        this.tableData.total = this.result.total;
        if (this.tableData.type === 'normal') {
          for (let item of headRows) {
            heads.push({
              title: item.columnName,
              key: item.columnName,
              minWidth: 120,
              width: 120,
              maxWidth: 240,
              className: 'columnClass',
              sortable: 'custom',
              columnType: item.dataType,
              colHeadHoverTitle: item.columnName +'\n'+item.dataType,
              checked: true,
              // render: (h, params) => {
              //   return h('span', {
              //     attrs: {
              //       title: params.row[params.column.title]
              //     }
              //   },params.row[params.column.title])
              // },
              // renderHeader: (h, params) => {
              //   return h('span', {
              //     attrs: {
              //       title: item.dataType,
              //     },
              //     on: {
              //       dblclick: (e) => {
              //         this.copyLabel({
              //           content: params.column.title,
              //         });
              //         return false;
              //       },
              //     },
              //   },
              //   params.column.title
              //   );
              // },
            });
          }
          this.originRows = this.originRows.map((row, i) => {
            let newItem = {};
            row.forEach((item, index) => {
              try {
                Object.assign(newItem, {
                // After the result set data is transformed, it cannot be used directly as a key, and the table field must be used as the only key.(结果集数据改造后不能直接当key使用，得用表字段作为唯一的key)
                  [headRows[index].columnName]: item,
                });
              } catch (error) {
                window.console.error(error, row.length, i);
              }
            });
            return newItem;
          });
        } else {
          for (let i = 0; i < headRows.length; i++) {
            heads.push({
              content: headRows[i].columnName,
              title: headRows[i].columnName,
              sortable: true,
              checked: true,
              columnType: headRows[i].dataType,
              colHeadHoverTitle: headRows[i].columnName +'\n'+headRows[i].dataType,
            });
          }
        }
        this.heads = heads;
        this.data = {
          headRows: heads,
          bodyRows
        };
        this.pageingData();
      }
    },
    pageingData() {
      if (this.originRows && this.originRows.length) {
        let {
          current,
          size,
        } = this.page;
        let maxLen = Math.min(this.tableData.total, current * size);
        let minLen = Math.max(0, (current - 1) * size);
        let newArr = this.originRows.slice(minLen, maxLen);
        this.data.bodyRows = newArr;
      }
    },
    change(page = 1) {
      this.hightLightRow = null;
      this.page.current = page;
      // paging data for storage(分页后的数据进行存储)
      const tmpResult = this.getResult();
      tmpResult.current = this.page.current;
      tmpResult.size = this.pagesize;
      const resultSet = this.script.resultSet || 0;
      if(this.script.resultList && this.script.resultList[resultSet]) this.$set(this.script.resultList[resultSet], 'result', tmpResult);
      this.dispatch('IndexedDB:updateResult', {
        tabId: this.script.id,
        resultSet: resultSet,
        showPanel: this.scriptViewState.showPanel,
        ...this.script.resultList,
      });
      this.pageingData();
    },
    changeSize(size) {
      this.hightLightRow = null;
      this.page.size = size;
      this.page.current = 1;
      // paging data for storage(分页后的数据进行存储)
      const tmpResult = this.getResult();
      tmpResult.current = this.page.current;
      tmpResult.size = this.pagesize;
      const resultSet = this.script.resultSet || 0;
      this.$set(this.script.resultList[resultSet], 'result', tmpResult);
      this.dispatch('IndexedDB:updateResult', {
        tabId: this.script.id,
        resultSet: resultSet,
        showPanel: this.scriptViewState.showPanel,
        ...this.script.resultList,
      });
      this.pageingData();
    },
    resizeWaterMask() {
      if (this.$refs.watermask) {
        this.$refs.watermask.updateCanvas(this.watermaskText);
      }
    },
    resize() {
      this.resizeWaterMask();
      return false;
    },
    changeSet(set) {
      this.isLoading = true;
      this.page.current = 1;
      this.hightLightRow = null;
      this.isFilterViewShow = false;
      this.$emit('on-set-change', {
        currentSet: set,
      }, () => {
        this.isLoading = false;
      });
    },
    copyLabel(head) {
      util.executeCopy(head.content || head.title);
    },
    getResult() {
      const result = Object.assign(this.result, {
        cache: {
        },
        current: this.page.current,
        size: this.page.size,
      });
      return result;
    },
    iframeOnChange() {
      this.$nextTick(() => {
        document.all.iframeName.contentWindow.location.reload();
      });
    },
    onRowClick(currentRow) {
      // Find the current type by column name(通过列命名去查找当前的类型)
      const row = Object.values(currentRow);
      this.rowToColumnData(row);
    },
    onWeTableRowClick(index) {
      const row = this.data.bodyRows[index];
      this.rowToColumnData(row);
    },
    rowToColumnData(row) {
      this.hightLightRow = []
      this.data.headRows.forEach((subItem, index) => {
        const temObject = {
          columnName: subItem.title,
          dataType: subItem.columnType,
          value: row[index]
        };
        this.hightLightRow.push(temObject);
      })
    },
    handleFilterView() {
      this.isFilterViewShow = !this.isFilterViewShow;
    },
    filterList(field) {
      field.checked = !field.checked;
      let rows = [];
      let cols = [];
      let bodyRows = []
      this.heads.forEach((it,index) => {
        if(it.checked) {
          rows.push({...it});
          cols.push(index);
        }
      });

      bodyRows = this.result.bodyRows.map(row=>{
        return row.filter((it,index) => cols.indexOf(index) > -1)
      })

      this.data.headRows = rows;
      this.originRows = bodyRows;
      this.pageingData();
    }
  },
};

</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
.we-result-view {
  width: 100%;
  height: calc(100% - 55px);
  overflow: hidden;
  padding-left: $toolbarWidth;
  background: $body-background;
  .html-result-div {
    overflow-y: auto;
  }
  .result-table-content {
    height: calc(100% - 52px);
    .ivu-table-header {
      width: calc(100% - 8px);
      tr > th:nth-last-of-type(2) {
        border-right: none;
      }
    }
    .result-normal-table {
      border: none;
      border-right: $border-width-base $border-style-base $border-color-base;
      min-width: 100%;
      .ivu-table {
        min-width: 100%;
        .is-null {
          color: $error-color;
          font-style: italic;
        }
      }
    }
  }
  .text-result-div {
    overflow: auto;
    padding: 10px;
  }
  .empty-text {
    position: $relative;
    top: 20px;
    left: 20px;
  }
  .table-box {
    overflow: hidden;
  }
  .we-page-container {
    display: flex;
    width: 100%;
    height: 42px;
    text-align: center;
    margin-top: 10px;
    padding-left: 10px;
    .page-hint {
      display: inline-flex;
      margin: 0 auto;
      align-items: center;
      .page {
        display: inline-block;
      }
      .hint-text {
        display: inline-block;
        margin-left: 5px;
      }
    }

    .set {
      width: 90px;
      // padding-right: 20px;
    }
  }
}
.result-normal-table table {
  min-width: 100%;
}
.result-table-content .list-view-item div[title='NULL'],
.result-table-content td[title='NULL'],
.result-table-content span[title='NULL'] {
  color: #ed4014;
}
.result-normal-table .columnClass {
  height: 30px;
  line-height: 1.25;
  /deep/.ivu-table-cell {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    position: relative;
    overflow: hidden;
    .ivu-table-sort {
      position: absolute;
      top: 50%;
      right: 0;
      transform: translate(-50%,-50%);
      z-index: 90;
      i.on {
        color: #FFFFFF;
      }
    }
  }
}
</style>
