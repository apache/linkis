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
    <!-- 左切换 -->
    <Icon class="switch" type="ios-arrow-back" @click="getLeft" />
    <!-- 中间内容 -->
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
              <SvgIcon
                style="font-size: 16px"
                color="#5580eb"
                icon-class="base"
              />
              {{ `${menuName}-${item.categoryName}` }}
            </div>
            <div class="cardItemDesc">
              {{ item.description || $t("message.linkis.noDescription") }}
            </div>
            <!-- TODO 待对接具体展示 -->
            <div>{{ item.tag || item.categoryName }}</div>
          </div>
          <!-- 引擎编辑 -->
          <SvgIcon
            v-if="iseditListItem"
            @click="editListItem(index, item)"
            class="edit"
            style="font-size: 14px"
            color="#5580eb"
            icon-class="setting"
          />
          <!-- 引擎删除 -->
          <SvgIcon
            v-if="isdeleteListItem"
            @click="deleteListItem(index, item)"
            class="close"
            style="font-size: 16px"
            color="#5580eb"
            icon-class="status-fail"
          />
        </li>
        <!-- add -->
        <li v-if="isOpenAdd" class="cardItem add" @click="addList">
          <SvgIcon icon-class="xingzeng" />
        </li>
      </ul>
    </div>
    <!-- 右切换 -->
    <Icon class="switch" type="ios-arrow-forward" @click="getRight" />
  </div>
</template>
<script>
export default {
  name: "cardList",
  props: {
    // tab切换的标题
    menuName: {
      type: String,
      default: "",
    },
    // 中间显示list数据
    categoryList: {
      type: Array,
      default: () => [],
    },
    // 是否开启新增功能
    isOpenAdd: {
      type: Boolean,
      default: false,
    },
    // 当前项及当前的高亮列表项
    currentItem: {
      type: Number,
      default: 0,
    },
    currentTabName: {
      type: String,
      default: "",
    },
    //是否开启引擎编辑功能
    iseditListItem: {
      type: Boolean,
      default: false,
    },
    //是否开启引擎删除功能
    isdeleteListItem: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      ulWidth: 0, // 列表容器长度
      liWidthTotal: 0, // 列表总长
      moveNum: 0, // 移动距离
      currentIndex: 0, // 当前高亮
    };
  },
  watch: {},
  beforeDestroy() {
    window.removeEventListener("resize", this.getRowWidth);
  },
  mounted() {
    this.$nextTick(() => {
      this.getRowWidth();
      // 监听窗口变化
      window.addEventListener("resize", this.getRowWidth);
      // 计算列表的总长度
      this.liWidthTotal = (this.categoryList.length + 1) * (225 + 20);
    });
  },
  methods: {
    // 获取ul的宽度
    getRowWidth() {
      this.ulWidth = this.$refs.row.offsetWidth;
      if (this.ulWidth >= this.liWidthTotal) this.moveNum = 0;
    },
    // 点击左切换
    getLeft() {
      // 判断所有li总长度与ul的长度，并作出判断操作
      // 如果ul长度小于li总长，则进行详细判断
      if (this.ulWidth < this.liWidthTotal) {
        // 如果为负数则已经向左移动过
        if (this.moveNum < 0) {
          // 如果偏移量小于li单个长度直接还原
          if (Math.abs(this.moveNum) <= 245) {
            this.moveNum = 0;
          } else {
            this.moveNum = this.moveNum + 245;
          }
        }
      } else {
        // 如果ul长度大于等于li总，则点击左右不做操作
        return;
      }
    },
    // 点击右切换
    getRight() {
      // 判断所有li总长度与ul的长度，并作出判断操作
      // 如果ul长度小于li总长，则进行详细判断
      if (this.ulWidth < this.liWidthTotal) {
        // 可移动数量
        let canMoveNum = this.liWidthTotal - this.ulWidth;
        if (Math.abs(this.moveNum) <= canMoveNum) {
          // 如果偏移量大于可偏移长度直接为最大值
          let nextWidth = Math.abs(this.moveNum) + 245;
          if (nextWidth <= canMoveNum) {
            this.moveNum = this.moveNum - 245;
          } else {
            this.moveNum = -canMoveNum - 1;
          }
        }
      } else {
        // 如果ul长度大于等于li总，则点击左右不做操作
        return;
      }
    },
    // 点击添加按钮
    addList() {
      this.$emit("on-add");
    },
    // 编辑
    editListItem(index, item) {
      this.$emit("on-edit", index, item);
    },
    // 删除
    deleteListItem(index, item) {
      this.$emit("on-delete", index, item);
    },
    // 点击其中一项
    clickChangeItem(title, index, item) {
      // 高亮
      this.currentIndex = index;
      this.$emit("on-click", title, index, item);
    },
  },
};
</script>
<style src="./index.scss" lang="scss" scoped></style>

