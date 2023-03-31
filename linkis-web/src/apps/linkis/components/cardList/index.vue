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
  <div class="setListCard" >
    <!-- switch left(左切换) -->
    <Icon class="switch" type="ios-arrow-back" @click="getLeft" />
    <!-- Intermediate content(中间内容) -->
    <div class="container" ref="row">
      <ul class="containerUl" :style="`transform: translateX(${moveNum}px);`">
        <li
          v-for="(item, index) in categoryList"
          :key="index"
          class="cardItem"
          :class="{ currentIndex: currentIndex === index }"
        >
          <div
            @click="
              clickChangeItem(`${menuName}-${item.categoryName}`, index, item)
            "
          >
            <div class="cardItemTitle">
              {{ `${menuName}-${item.categoryName}` }}
            </div>
            <div class="cardItemDesc">
              {{ item.description || $t("message.linkis.noDescription") }}
            </div>
            <!-- TODO To be connected to the specific display(待对接具体展示) -->
            <div>{{ item.tag || item.categoryName }}</div>
          </div>
          <!-- engine editor(引擎编辑) -->
          <i
            class="material-icons edit"
            v-if="iseditListItem"
            @click="editListItem(index, item)"
            style="font-size: 14px">edit</i>
          <!-- engine removal(引擎删除) -->
          <i
            class="material-icons close"
            v-if="isdeleteListItem"
            @click="deleteListItem(index, item)"
            style="font-size: 16px">delete</i>
        </li>
        <!-- add -->
        <li v-if="isOpenAdd" class="cardItem add" @click="addList">
          <i class="material-icons add" >add</i>
        </li>
      </ul>
    </div>
    <!-- switch right(右切换) -->
    <Icon class="switch" type="ios-arrow-forward" @click="getRight" />
  </div>
</template>
<script>
import 'material-design-icons/iconfont/material-icons.css';
export default {
  name: "cardList",
  props: {
    // title of tab switch(tab切换的标题)
    menuName: {
      type: String,
      default: "",
    },
    // Display list data in the middle(中间显示list数据)
    categoryList: {
      type: Array,
      default: () => [],
    },
    // Whether to enable new features(是否开启新增功能)
    isOpenAdd: {
      type: Boolean,
      default: false,
    },
    // The current item and the current highlighted list item(当前项及当前的高亮列表项)
    currentItem: {
      type: Number,
      default: 0,
    },
    currentTabName: {
      type: String,
      default: "",
    },
    //Whether to enable the engine editing function(是否开启引擎编辑功能)
    iseditListItem: {
      type: Boolean,
      default: false,
    },
    //Whether to enable the engine deletion function(是否开启引擎删除功能)
    isdeleteListItem: {
      type: Boolean,
      default: false,
    },
    currentCardIndex: {
      type: Number,
      default: 0
    },
  },
  data() {
    return {
      ulWidth: 0, // List container length(列表容器长度)
      liWidthTotal: 0, // total length of the list(列表总长)
      moveNum: 0, // Moving distance(移动距离)
    };
  },
  computed: {
    // currently highlighted(当前高亮)
    currentIndex: {
      get() {
        return this.currentCardIndex;
      },
      set(val) {
        this.$emit('update:currentCardIndex', val);
      }
    }
  },
  watch: {
    categoryList() {
      this.liWidthTotal = (this.categoryList.length + 1) * (225 + 20);
    },
    currentIndex(cur, old) {
      if (cur > old && cur === (this.categoryList.length - 1)) {
        this.getRight('last')
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.getRowWidth);
  },
  mounted() {
    this.getRowWidth();
    // Monitor window changes(监听窗口变化)
    window.addEventListener("resize", this.getRowWidth);
    // Calculate the total length of the list(计算列表的总长度)
    this.liWidthTotal = (this.categoryList.length + 1) * (225 + 20);
  },
  methods: {
    // Get the width of the ul(获取ul的宽度)
    getRowWidth() {
      this.ulWidth = this.$refs.row.offsetWidth;
      if (this.ulWidth >= this.liWidthTotal) this.moveNum = 0;
    },
    // Click left to switch(点击左切换)
    getLeft() {
      // Determine the total length of all li and the length of ul, and make a judgment operation(判断所有li总长度与ul的长度，并作出判断操作)
      // If the length of ul is less than the total length of li, make a detailed judgment(如果ul长度小于li总长，则进行详细判断)
      if (this.ulWidth < this.liWidthTotal) {
        // If it is negative, it has moved to the left(如果为负数则已经向左移动过)
        if (this.moveNum < 0) {
          // If the offset is less than a single length of li, restore directly(如果偏移量小于li单个长度直接还原)
          if (Math.abs(this.moveNum) <= 245) {
            this.moveNum = 0;
          } else {
            this.moveNum = this.moveNum + 245;
          }
        }
      } else {
        // If the length of ul is greater than or equal to the total of li, click left and right to do nothing(如果ul长度大于等于li总，则点击左右不做操作)
        return;
      }
    },
    // Click right to switch(点击右切换)
    getRight(type = 'last') {
      // Determine the total length of all li and the length of ul, and make a judgment operation(判断所有li总长度与ul的长度，并作出判断操作)
      // If the length of ul is less than the total length of li, make a detailed judgment(如果ul长度小于li总长，则进行详细判断)
      this.getRowWidth()
      if (this.ulWidth < this.liWidthTotal) {
        // movable quantity(可移动数量)
        let canMoveNum = this.liWidthTotal - this.ulWidth;
        if (Math.abs(this.moveNum) <= canMoveNum) {
          // Slide it to the far right
          if (type === 'last') {
            this.moveNum = -canMoveNum;
            return;
          }
          // If the offset is greater than the offset length, it is directly the maximum value(如果偏移量大于可偏移长度直接为最大值)
          let nextWidth = Math.abs(this.moveNum) + 245;
          if (nextWidth <= canMoveNum) {
            this.moveNum = this.moveNum - 245;
          } else {
            this.moveNum = -canMoveNum - 1;
          }
        }
      } else {
        // If the length of ul is greater than or equal to the total of li, click left and right to do nothing(如果ul长度大于等于li总，则点击左右不做操作)
        return;
      }
    },
    // Click the Add button(点击添加按钮)
    addList() {
      this.$emit("on-add");
    },
    // edit(编辑)
    editListItem(index, item) {
      this.$emit("on-edit", index, item);
    },
    // delete(删除)
    deleteListItem(index, item) {
      this.$emit("on-delete", index, item);
    },
    // click one of the(点击其中一项)
    clickChangeItem(title, index, item) {
      // highlight(高亮)
      this.currentIndex = index;
      this.$emit("on-click", title, index, item);
    },
  },
};
</script>
<style src="./index.scss" lang="scss" scoped></style>

