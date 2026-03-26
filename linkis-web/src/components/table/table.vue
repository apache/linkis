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
  <div style="position:relative;height:100%">
    <div
      class="table-div"
      :style="{height:`${tableHeightCalc}px`}"
    >
      <div
        id="topDiv"
        class="table-top"
        :style="{right:`${(scrollBarWidthCalc())}px`}"
        @dblclick="headDblclick"
      >
        <table
          id="topTable"
          cellpadding="0"
          cellspacing="0"
          border="0"
          style="width:100%;table-layout:fixed;"
        >
          <tr>
            <td
              v-for="(top,index) in columns"
              class="top-td"
              :key="index"
              :data-col-index="index"
              :style="{width: top.width? `${top.width}px` : 'auto'}"
            >
              <span :data-col-index="index" :title="top.colHeadHoverTitle">{{top.title?top.title:""}}</span>
              <span
                :class="'sort-icon'"
                v-if="top.sortable"
              >
                <i class="ivu-icon ivu-icon-md-arrow-dropup"
                  :class="{'on': sortType.index === index && sortType.type === 'asc'}" @click="handleSort(index, 'asc')"></i>
                <i class="ivu-icon ivu-icon-md-arrow-dropdown"
                  :class="{'on': sortType.index === index && sortType.type === 'desc'}" @click="handleSort(index, 'desc')"></i>
              </span>
            </td>
          </tr>
        </table>
      </div>
      <div
        id="bottomDiv"
        class="table-bottom"
        @scroll.stop="handleScroll"
      >
        <div :style="{height:`${dataTop}px`}"></div>
        <table
          id="bottomTable"
          v-if="showTableList.length"
          cellpadding="0"
          cellspacing="0"
          border="0"
          style="width:100%;table-layout:fixed;overflow:hidden"
          :style="{height:`${loadedNum*tdHeight}px`}"
        >
          <tbody>
            <tr
              v-for="(items,indexs) in showTableList"
              @click="rowClick(items,indexs+dataTop/tdHeight)"
              @dblclick="rowDblclick(items,indexs+dataTop/tdHeight+1)"
              :key="indexs"
              :style="{'line-height':`${tdHeight}px`}"
              :class="selectIndex==indexs?'trselect':'trhover'"
            >
              <td
                class="bottom-td"
                v-if="columns[0].type=='index'"
                :style="{width: columns[0].width? `${columns[0].width}px` : 'auto', height:`${tdHeight}px`}"
              >
                {{indexs+dataTop/tdHeight+1}}</td>
              <td
                class="bottom-td"
                v-if="columns[0].type=='select'"
              ></td>
              <template v-for="(item,index) in columnsBottom">
                <td
                  v-if="item.key"
                  :key="`${index}-${indexs}`"
                  class="bottom-td"
                  :class="[hasLineBreak(item.logic==undefined?items[item.key]:item.logic(items)) ? '' : 'hint--right hint--rounded']"
                  :style="{width: item.width?`${item.width}px`:'auto'}"
                  :aria-label="item.logic==undefined?items[item.key]:item.logic(items)"
                >
                  <p v-if="hasLineBreak(item.logic==undefined?items[item.key]:item.logic(items))" style="width: 100%" class="wrap">{{item.logic==undefined?items[item.key]:item.logic(items)}}</p>
                  <div v-else class="scroll" :aria-label="item.logic==undefined?items[item.key]:item.logic(items)" :style="{width: '100%', height:`${tdHeight}px`}">{{item.logic==undefined?items[item.key]:item.logic(items)}}</div>
                
                </td>
                <td
                  v-if="item.slot"
                  class="bottom-td"
                  :key="index"
                  :style="{width: item.width?`${item.width}px`:'auto', height:`${tdHeight}px`}"
                >
                  <slot :name="item.slot" :row="items" :index="indexs+dataTop/tdHeight+1"></slot>
                </td>
              </template>
            </tr>
          </tbody>
        </table>
        <div v-show="showTableList.length < 1" class="no-data-tip" :style="noDataStyle">
          暂无数据
        </div>
        <div :style="{height:`${tableOtherBottom}px`}"></div>
      </div>
    </div>
    <div
      class="table-bottom-load"
      v-show="showLoad"
      :style="{right:`${(scrollBarWidthCalc())}px`,top:'40px',height:`${tableHeightCalc-40}px`}"
    >
      <svg
        class="icon loading "
        aria-hidden="true"
      >
        <use xlink:href="#icon-jiazai"></use>
      </svg>
      <div class="msg">加载中,请稍候</div>
    </div>
  </div>
</template>

<script>
export default {
  name: "WbTable",
  props: {
    loadNum: {
      //Default number of rows to load(默认加载行数)
      type: [Number, String],
      default() {
        return 50;
      }
    },
    tdHeight: {
      //table row height(表格行高)
      type: [Number, String],
      default() {
        return 40;
      }
    },
    tableHeight: {
      //table height(表格高度)
      type: [Number, String],
    },
    tableList: {
      //All tabular data(所有表格数据)
      type: Array,
      default() {
        return [];
      }
    },
    columns: {
      //All table matching rules(所有表格匹配规则)
      type: Array,
      default() {
        return [];
      }
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    highlightRow: {},
    outerSort: {}, // If external sorting, clicking the sorting icon in the header only triggers the event(若外部排序则点击表头排序图标只触发事件)
    scrollBarWidth: {
      type: Number,
      default(){
        return 8
      }
    }
  },
  data() {
    return {
      showLoad: false,
      showTableList: [], //table data actually displayed(实际显示的表格数据)
      loadedNum: 0, //The amount of data actually rendered(实际渲染的数据数量)
      dataTotal: 0, //total data(总数据条数)
      dataTop: 0, //The height of the top of the rendered data(渲染数据顶部的高度)
      scrollTop: 0, //scroll up and down distance(滚动上下的距离)
      scrollHeight: 0, //The height of the data scroll(数据滚动的高度)
      handleScroll: null,
      selectIndex: -1, //selected row(选择的行)
      sortType: {},
      noDataStyle: {
        width: '100%'
      }
    };
  },
  computed: {
    tableOtherBottom() {
      //The bottom height of the remaining data in the table(表格剩余数据底部高度)
      return (
        this.dataTotal * this.tdHeight -
        this.dataTop -
        this.loadedNum * this.tdHeight
      );
    },
    columnsBottom() {
      if (this.columns[0].type != undefined) {
        return this.columns.slice(1, this.columns.length);
      } else {
        return this.columns;
      }
    },
    // Calculate the height when the actual data of the table is lower than the number of rendered bars(计算表格实际数据低于渲染条数时的高度)
    tableHeightCalc() {
      let dataHeightt = (this.tdHeight + 1) * this.loadedNum + 43;// 43 is the header and reserved height, +1 is the border(43为表头和预留高度, +1 为border)
      if(this.tableHeight) return this.tableHeight;
      if(this.tableList.length <= 0) return 100;
      return dataHeightt
    }
  },
  methods: {
    /**
     * @typedef {Object} Options -configuration item(配置项)
     * @property {Boolean} leading -Whether an additional trigger is required to start(开始是否需要额外触发一次)
     * @property {this} context -context(上下文)
     **/
    //Use Proxy to implement function anti-shake(使用Proxy实现函数防抖)
    proxy(
      func,
      time,
      options = {
        leading: true,
        context: null
      }
    ) {
      let timer;
      let _this = this;
      let handler = {
        apply(target, _, args) {
          //proxy function call(代理函数调用)
          let bottomScroll = document.getElementById("bottomDiv");
          let topScroll = document.getElementById("topDiv");
          if (bottomScroll.scrollTop == _this.scrollTop) {
            //scroll left and right(左右滚动)
            _this.handleScrollLeft(topScroll, bottomScroll);
            return;
          }
          // The same as the core logic of closure implementation(和闭包实现核心逻辑相同)
          if (!options.leading) {
            if (timer) return;
            timer = setTimeout(() => {
              timer = null;
              Reflect.apply(func, options.context, args);
            }, time);
          } else {
            if (timer) {
              _this.needLoad(bottomScroll);
              clearTimeout(timer);
            }
            timer = setTimeout(() => {
              Reflect.apply(func, options.context, args);
            }, time);
          }
        }
      };
      return new Proxy(func, handler);
    },
    //Whether to show loading(是否显示加载中)
    needLoad(bottomScroll) {
      if (
        Math.abs(bottomScroll.scrollTop - this.scrollTop) >
        this.tdHeight * this.loadNum
      ) {
        this.showLoad = true; //show loading(显示加载中)
        this.scrollTop = bottomScroll.scrollTop;
      }
    },
    //scroll processing(滚动处理)
    scrollProcessing() {
      // const last = $event && $event.last;
      const bottomScroll = document.getElementById("bottomDiv");
      // const topScroll = document.getElementById("topDiv");
      const direction = bottomScroll.scrollTop >= this.scrollTop; //scroll direction(滚动方向)
      // if(this.needLoad(last,bottomScroll))return;
      //Record the last scroll down position(记录上一次向下滚动的位置)
      this.scrollTop = bottomScroll.scrollTop;
      direction ? this.handleScrollBottom() : this.handleScrollTop();
      this.showLoad = false;
    },
    //scroll bar up(滚动条向上滚动)
    handleScrollTop() {
      if (this.dataTop < this.scrollTop) {
        //scroll down should be called if the last scroll position is above the data(如果最后滚动位置在数据上方应该调用向下滚动)
        this.handleScrollBottom();
        return;
      }
      //If the loaded data is less than the default amount of data loaded(如果加载的数据小于默认加载的数据量)
      if (this.dataTotal > this.loadNum) {
        const computeHeight = this.dataTop; //The height at which the data needs to be processed(数据需要处理的时候的高度)
        const maxHeigth = computeHeight - this.loadNum * this.tdHeight; //No need to clear all data height(不需要清除所有数据的高度)
        if (this.scrollTop < computeHeight && this.scrollTop >= maxHeigth) {
          //If the total data is greater than the already rendered data(如果数据总数大于已经渲染的数据)
          const dataTopNum = parseInt(this.dataTop / this.tdHeight); //Number of top bars of data(数据顶部条数)
          dataTopNum - this.loadNum >= 0
            ? this.dataProcessing(
              this.loadNum,
              this.loadedNum - this.loadNum,
              "top"
            )
            : this.dataProcessing(dataTopNum, dataTopNum, "top");
        } else if (this.scrollTop < maxHeigth) {
          const scrollNum = parseInt(this.scrollTop / this.tdHeight); //滚动的位置在第几条数据
          scrollNum - this.loadNum >= 0
            ? this.dataProcessing(this.loadNum * 2, scrollNum, "topAll")
            : this.dataProcessing(
              scrollNum + this.loadNum,
              scrollNum,
              "topAll"
            );
        }
      }
    },
    hasLineBreak(content) {
      if (content.split(/\n/).length > 1) {
        return true
      }
      return false
    },

    //scroll bar scroll down(滚动条向下滚动)
    handleScrollBottom() {
      if (this.dataTop > this.scrollTop) {
        this.handleScrollTop();
        return;
      }
      const computeHeight =
        this.dataTop + this.loadedNum * this.tdHeight - this.tableHeightCalc; //The height at which the data needs to be processed(数据需要处理的时候的高度)
      const maxHeight = computeHeight + this.tdHeight * this.loadNum; //No need to clear all data height(不需要清除所有数据的高度)
      if (this.scrollTop > computeHeight && this.scrollTop <= maxHeight) {
        //If the scroll height reaches the bottom height of the data display(如果滚动高度到达数据显示底部高度)
        if (this.dataTotal > this.loadedNum) {
          const dataTopNum = parseInt(this.dataTop / this.tdHeight); //Number of top bars of data(数据顶部条数)
          const total = dataTopNum + this.loadedNum + this.loadNum;
          const otherTotal = this.dataTotal - (dataTopNum + this.loadedNum);
          total <= this.dataTotal
            ? this.dataProcessing(
              this.loadedNum - this.loadNum,
              this.loadNum,
              "bottom"
            )
            : this.dataProcessing(otherTotal, otherTotal, "bottom");
        }
      } else if (this.scrollTop > maxHeight) {
        let scrollNum = parseInt(this.scrollTop / this.tdHeight); //The scroll position is in the first few data(滚动的位置在第几条数据)
        scrollNum + this.loadNum <= this.dataTotal
          ? this.dataProcessing(scrollNum, this.loadNum * 2, "bottomAll")
          : this.dataProcessing(
            scrollNum,
            this.dataTotal - scrollNum + this.loadNum,
            "bottomAll"
          );
      }
    },
    //scroll bar scroll left and right(滚动条左右滚动)
    handleScrollLeft(topScroll, bottomScroll) {
      //The top header scrolls with the bottom(顶部表头跟随底部滚动)
      topScroll.scrollTo(bottomScroll.scrollLeft, topScroll.pageYOffset);
    },
    //Data processing when scrolling up and down(上下滚动时数据处理)
    dataProcessing(topNum, bottomNum, type) {
      const topPosition = parseInt(this.dataTop / this.tdHeight);
      if (type === "top") {
        this.showTableList.splice(this.loadedNum - bottomNum, bottomNum); //减去底部数据
        for (let i = 1; i <= topNum; i++) {
          //plus top data(加上顶部数据)
          const indexNum = topPosition - i;
          this.showTableList.unshift(this.tableList[indexNum]);
        }
        this.loadedNum = this.loadedNum + topNum - bottomNum; //Recalculate the actual number of rendered data(重新计算实际渲染数据条数)
        this.dataTop = this.dataTop - topNum * this.tdHeight; //Recalculate the height of the rendered data(重新计算渲染数据的高度)
        document.getElementById("bottomDiv").scrollTop =
          document.getElementById("bottomDiv").scrollTop +
          bottomNum * this.tdHeight;
        // this.scrollTop = document.getElementById("bottomDiv").scrollTop;
      } else if (type == "bottom") {
        this.showTableList.splice(0, topNum); //minus top data(减去顶部数据)
        for (let i = 0; i < bottomNum; i++) {
          //plus bottom data(加上底部数据)
          const indexNum = topPosition + this.loadedNum + i;
          this.showTableList.push(this.tableList[indexNum]);
        }
        this.loadedNum = this.loadedNum - topNum + bottomNum; //Recalculate the actual number of rendered data(重新计算实际渲染数据条数)
        this.dataTop = this.dataTop + topNum * this.tdHeight; //Recalculate the height of the rendered data(重新计算渲染数据的高度)
        document.getElementById("bottomDiv").scrollTop =
          document.getElementById("bottomDiv").scrollTop -
          topNum * this.tdHeight;
        // this.scrollTop = document.getElementById("bottomDiv").scrollTop;
      } else if (type == "bottomAll") {
        this.showTableList = []; //minus top data(减去顶部数据)
        let scrollNum = topNum;
        for (let i = 0; i < bottomNum; i++) {
          //plus bottom data(加上底部数据)
          let indexNum = scrollNum - this.loadNum + i;
          this.showTableList.push(this.tableList[indexNum]);
        }
        this.loadedNum = bottomNum; //Recalculate the actual number of rendered data(重新计算实际渲染数据条数)
        this.dataTop = (scrollNum - this.loadNum) * this.tdHeight; //Recalculate the height of the rendered data(重新计算渲染数据的高度)
        // this.scrollTop = document.getElementById("bottomDiv").scrollTop;
      } else if (type == "topAll") {
        this.showTableList = []; //minus top data(减去顶部数据)
        let scrollNum = bottomNum;
        for (let i = 0; i < topNum; i++) {
          //plus bottom data(加上底部数据)
          let indexNum = scrollNum - topNum + this.loadNum + i;
          this.showTableList.push(this.tableList[indexNum]);
        }
        this.loadedNum = topNum; //Recalculate the actual number of rendered data(重新计算实际渲染数据条数)
        this.dataTop = (scrollNum - topNum + this.loadNum) * this.tdHeight; //Recalculate the height of the rendered data(重新计算渲染数据的高度)
        // this.scrollTop = document.getElementById("bottomDiv").scrollTop;
      }
      this.showLoad = false;
    },
    rowClick(item, index) {
      if (this.highlightRow !== undefined) {
        this.selectIndex = index;
      }
      this.$emit("on-current-change", item, index);
    },
    rowDblclick(index, item) {
      this.$emit("on-row-dblclick", item, index);
    },
    headDblclick(e) {
      if (e && e.target && e.target.dataset.colIndex) {
        let index = e.target.dataset.colIndex
        this.$emit("on-head-dblclick", this.columns[index], index);
        const selection = window.getSelection();
        selection.selectAllChildren(e.target);
      } else {
        window.getSelection().removeAllRanges();
      }
    },
    //sort(排序)
    handleSort(index, type) {
      let column = this.columns[index];
      let key = column.key;
      if (this.sortType.type === type && this.sortType.index === index) {
        type = 'normal';
      }
      this.sortType = {
        index,
        type
      };
      if (this.outerSort !== undefined) {
        this.$emit("on-sort-change", {
          column,
          key,
          order: type
        });
        return
      }

      this.tableList.sort((a, b) => {
        if (column.sortMethod) {
          return column.sortMethod(a[key], b[key], type);
        } else {
          if (type === "asc") {
            return a[key] > b[key] ? 1 : -1;
          } else if (type === "desc") {
            return a[key] < b[key] ? 1 : -1;
          }
        }
      });
    },
    initTable() {
      document.getElementById("bottomDiv") &&
        (document.getElementById("bottomDiv").scrollTop = 0);
      // Later, if you want to switch the tab to save the scroll bar state, you have to record the scroll distance and assign it again.(后面如果要做切换tab保存滚动条状态，得记录滚动距离再赋值)
      // document.getElementById("bottomDiv") &&
      //   (document.getElementById("bottomDiv").scrollLeft = 0);
      this.loadedNum = 0; //The amount of data actually rendered(实际渲染的数据数量)
      this.dataTotal = 0; //total data(总数据条数)
      this.dataTop = 0; //The height of the top of the rendered data(渲染数据顶部的高度)
      this.scrollTop = 0; //scroll up and down distance(滚动上下的距离)
      this.showTableList = [];
      if (this.tableList.length > 0) {
        this.dataTotal = this.tableList.length; //get data length(获取数据长度)
        if (this.dataTotal >= this.loadNum) {
          //Determine whether the data length is greater than the set length of each rendering(判断数据长度是否大于设置的每次渲染长度)
          this.loadedNum = this.loadNum; //Set the actual number of rendered bars(设置实际渲染条数)
          for (var i = 0; i < this.loadNum; i++) {
            let data = this.tableList[i];
            this.showTableList.push(data);
          }
        } else if (this.dataTotal < this.loadNum) {
          this.loadedNum = this.dataTotal;
          for (let i = 0; i < this.dataTotal; i++) {
            let data = this.tableList[i];
            this.showTableList.push(data);
          }
        }
      }
      if(this.showTableList.length < 1) {
        this.$nextTick(()=>{
          this.getNoDataStyle()
        })
      }

    },
    getNoDataStyle() {
      let topTable = document.getElementById("topTable");
      if (topTable) {
        this.noDataStyle.width =  topTable.clientWidth + 'px'
      }
    },
    // Determine whether a vertical scroll bar appears(判断是否出现竖直滚动条)
    scrollBarWidthCalc() {
      let bottomDiv = document.getElementById("bottomDiv");
      return  bottomDiv && bottomDiv.scrollHeight > bottomDiv.clientHeight ? this.scrollBarWidth : 0
    },
  },
  created() {
    this.handleScroll = this.proxy(this.scrollProcessing, 240, {
      leading: true,
      context: this
    });
  },
  mounted() {
    this.initTable();
  },
  watch: {
    tableList: {
      handler(newValue, oldValue) {
        this.initTable();
        if (oldValue) {
          this.scrollProcessing();
        }
      }
    },
    tableHeight(newValue) {
      if (newValue) {
        this.scrollProcessing(); //table height change recalculation(表格高度改变重新计算)
      }
    }
  }
};
</script>
<style  scoped>
@import "./table.css";
</style>
