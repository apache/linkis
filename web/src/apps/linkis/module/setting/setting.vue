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
  <div class="setting" style="height: 100%">
    <!-- tab组件只做切换选择 -->
    <Tabs
      class="tabs"
      @on-click="clickTabChange"
      @on-tab-remove="removeTab"
      :animated="false"
      :type="isTypeCard ? 'card' : 'line'"
      closable
    >
      <TabPane
        v-for="menu in menuList"
        :key="menu.categoryId"
        :label="menu.categoryName"
        :name="`${menu.categoryName}`"
      >
        <cardList
          v-if="menu.categoryName !== '全局设置'"
          :menuName="menu.categoryName"
          :categoryList="menu.childCategory"
          @on-edit="editChildCategory"
          @on-delete="deleteChildCategory"
          @on-click="clickChildCategory"
          @on-add="addChildCategory"
          :currentTabName="currentTabName"
          :iseditListItem="iseditListItem"
          :isdeleteListItem="isdeleteListItem"
          :isOpenAdd="isOpenAdd"
        >
        </cardList>
      </TabPane>
      <!-- 应用编辑按钮 -->
      <Button @click="handleTabsEdit" size="small" slot="extra" v-show="isLogAdmin">{{
        $t("message.linkis.editContents")
      }}</Button>
      <Button @click="handleTabsAdd" size="small" type="primary" slot="extra" v-show="isLogAdmin">{{
        $t("message.linkis.addAppType")
      }}</Button>
    </Tabs>
    <!-- 参数详情 -->
    <div v-if="fullTree && fullTree.length" class="setting-content">
      <div class="setting-content-header">
        <Button @click="toggleAdvance"
        >{{
          isAdvancedShow
            ? $t("message.linkis.setting.hide")
            : $t("message.linkis.setting.show")
        }}{{ $t("message.linkis.setting.advancedSetting") }}</Button
        >
        <Button
          :loading="loading"
          type="primary"
          class="setting-content-btn"
          @click="save"
        >{{ $t("message.linkis.save") }}</Button
        >
      </div>
      <div class="setting-content-variable" style="height: calc(100% - 45px)">
        <variable
          v-for="(item, index) in fullTree"
          ref="variable"
          :key="index"
          :variable="item"
          @add-item="handleAdd"
          @remove-item="handleDelete"
          :un-valid-msg="unValidMsg"
          :is-advanced-show="isAdvancedShow"
        />
      </div>
    </div>
    <!-- 引擎描述修改 -->
    <Modal
      @on-ok="addEngineDesc"
      :title="$t('message.linkis.editDescriptionEngineConfig')"
      v-model="isEditChildCategory"
      :mask-closable="false"
    >
      <!-- 引擎描述的编辑 -->
      <Form
        :label-width="90"
        ref="formValidate"
        :model="editEngineFromItem"
        :rules="ruleValidate"
      >
        <!-- 新增引擎的名称 -->
        <FormItem :label="`${$t('message.linkis.tableColumns.engineType')}：`">
          <Select v-model="editEngineFromItem.type" disabled>
            <Option :value="editEngineFromItem.type">{{
              editEngineFromItem.type
            }}</Option>
          </Select>
        </FormItem>
        <!-- 引擎版本信息 -->
        <FormItem
          class="addTagClass"
          :label="`${$t('message.linkis.tableColumns.engineVersion')}：`"
          prop="version"
        >
          <Input :value="editEngineFromItem.version" disabled />
        </FormItem>
        <FormItem :label="`${$t('message.linkis.description')}：`" prop="desc">
          <Input
            v-model="editEngineFromItem.desc"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }"
            placeholder="Enter something..."
          />
        </FormItem>
      </Form>
    </Modal>

    <!-- 新增参数配置modal -->
    <Modal
      @on-ok="addParameterSet"
      :title="$t('message.linkis.addParameterConfig')"
      v-model="isChildCategory"
      :mask-closable="false"
    >
      <!-- 引擎描述的编辑 -->
      <Form
        :label-width="90"
        ref="formValidate"
        :model="childCategoryFormItem"
        :rules="ruleValidate"
      >
        <!-- 新增引擎的名称 -->
        <FormItem :label="`${$t('message.linkis.tableColumns.engineType')}：`">
          <Select v-model="childCategoryFormItem.type">
            <Option :value="item" v-for="item in engineType" :key="item.key">{{
              item
            }}</Option>
          </Select>
        </FormItem>
        <!-- 引擎版本信息 -->
        <FormItem
          class="addTagClass"
          :label="`${$t('message.linkis.tableColumns.engineVersion')}：`"
          prop="version"
        >
          <Input v-model="childCategoryFormItem.version" />
        </FormItem>
        <FormItem :label="`${$t('message.linkis.description')}：`" prop="desc">
          <Input
            v-model="childCategoryFormItem.desc"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }"
            placeholder="Enter something..."
          />
        </FormItem>
      </Form>
    </Modal>
    <!-- 新增应用类型modal -->
    <Modal
      @on-ok="addApptype"
      :title="$t('message.linkis.addAppType')"
      v-model="isAddApptype"
      :mask-closable="false"
    >
      <Form :label-width="80">
        <FormItem :label="`${$t('message.linkis.name')}：`">
          <Input v-model="addApptypeFormItem.name" />
        </FormItem>
        <FormItem :label="`${$t('message.linkis.description')}：`" prop="desc">
          <Input
            v-model="addApptypeFormItem.desc"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }"
            placeholder="Enter something..."
          />
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>
<script>
import { orderBy } from "lodash";
import api from "@/common/service/api";
import storage from '@/common/helper/storage';
import variable from "@/apps/linkis/components/variable";
import cardList from "@/apps/linkis/components/cardList";
export default {
  name: "Setting",
  components: {
    variable,
    cardList,
  },
  data() {
    return {
      menuList: [],
      activeMenu: "", // 当前打开目录
      currentTabName: "",
      currentTabId: 0, //当前tab的Id
      fullTree: [],
      engineType: [], //引擎类型数据
      loading: false,
      unValidMsg: {},
      isAdvancedShow: false,
      isChildCategory: false,
      isEditChildCategory: false, //编辑引擎的对话框
      isAddApptype: false,  //新增应用的对话框
      clickChild: {},
      isTypeCard: false, //tab栏是否开启card类型
      iseditListItem: false, //应用下的引擎编辑功能是否开启
      isdeleteListItem: false, //应用下的引擎删除功能是否开启
      isOpenAdd: false, //应用下的引擎添加功能是否开启
      tabLeft: 0,
      isLogAdmin: false,  //用户是否有管理员权限
      childCategoryFormItem: {
        name: "",
        tagList: [],
        version: "",
        type: "",
        desc: "",
      },
      editEngineFromItem: {
        name: "",
        version: "",
        type: "",
        desc: "",
        categoryId: 0,
        categoryName: "",
      },
      addApptypeFormItem: {
        name: "",
        order: "",
        desc: "",
      },
      ruleValidate: {
        version: [
          {
            required: true,
            message: this.$t(
              "message.linkis.tableColumns.engineVersionCannotBeNull"
            ),
            trigger: "blur",
          },
        ],
      },
    };
  },
 
  mounted() {
    this.isLogAdmin = storage.get("isLogAdmin");
    // 获取设置目录
    api.fetch("/configuration/getCategory", "get").then((rst) => {
      this.menuList = rst.Category || [];
      this.$nextTick(() => {
        this.getAppVariable(this.menuList[0].categoryName || "");
        this.currentTabName = `${this.menuList[0].categoryName}`;
      });
    });
    //获取所有引擎类型
    api.fetch("/configuration/engineType", "get").then((rst) => {
      this.engineType = rst.engineType;
    });
  },

  methods: {
    getMenuList() {
      api.fetch("/configuration/getCategory", "get").then((rst) => {
        this.menuList = rst.Category || [];
        this.$nextTick(() => {
          this.getAppVariable(this.currentTabName || "");
          this.currentTabName = `${this.currentTabName}`;
        });
      });
    },

    getAppVariable(type) {
      this.activeMenu = type;
      let parameter = type.split("-"); // 切割目录name
      // 如果只有一级目录则直接返回['creator'],如果为二级目录则['creator', 'engineType', 'version']
      api
        .fetch(
          "/configuration/getFullTreesByAppName",
          {
            creator: parameter[0], // 指定一级目录
            engineType: parameter[1], // 指定引擎（二级目录）如果只有一级目录则自动为undefined不会传参
            version: parameter[2], // 对应的引擎目前只支持对应的版本，如spark就传version-2.4.3，如果只有一级目录则自动为undefined不会传参
          },
          "get"
        )
        .then((rst) => {
          this.loading = false;
          //当前活动的tab 是否和点击的引擎同一个tab
          this.fullTree = rst.fullTree;
          this.fullTree.forEach((item) => {
            item.settings = orderBy(item.settings, ["level"], ["asc"]);
            if (item.settings.length) {
              item.settings.forEach((set) => {
                if (set.validateType === "OFT") {
                  set.validateRangeList = this.formatValidateRange(
                    set.validateRange,
                    set.key
                  );
                }
                if (
                  set.key === "spark.application.pyFiles" ||
                  set.key === "python.application.pyFiles"
                ) {
                  set.placeholder = this.$t("message.linkis.placeholderZip");
                }
              });
            }
          });
        })
        .catch(() => {
          this.loading = false;
        });
    },
    formatValidateRange(value, type) {
      let formatValue = [];
      let tmpList = [];
      try {
        tmpList = JSON.parse(value);
      } catch (e) {
        tmpList = value.slice(1, value.length - 1).split(",");
      }
      tmpList.forEach((item) => {
        formatValue.push({
          name:
            item === "BLANK" && type === "pipeline.out.null.type"
              ? this.$t("message.linkis.emptyString")
              : item,
          value: item,
        });
      });
      return formatValue;
    },
    handleAdd(item, parent, cb) {
      setTimeout(() => {
        cb(true);
      }, 200);
    },
    handleDelete(item, parent, cb) {
      setTimeout(() => {
        cb(true);
      }, 200);
    },
    save() {
      this.loading = true;
      this.checkValid();
      api
        .fetch("/configuration/saveFullTree", {
          fullTree: this.fullTree,
          creator: this.currentTabName,
        })
        .then(() => {
          this.getAppVariable(this.activeMenu);
          this.unValidMsg = {};
          this.$Message.success(this.$t("message.linkis.save"));
        })
        .catch((err) => {
          this.loading = false;
          if (err.message) {
            let key = "";
            let msg = "";
            this.fullTree.forEach((item) => {
              if (item.settings) {
                item.settings.forEach((s) => {
                  if (
                    s.validateType === "OFT" &&
                    Object.prototype.hasOwnProperty.call(s, "validateRangeList")
                  ) {
                    delete s.validateRangeList;
                  }
                  if (err.message.indexOf(s.key) > -1) {
                    msg = s.description;
                    key = s.key;
                  }
                });
              }
            });
            this.unValidMsg = { key, msg };
          }
        });
    },
    checkValid() {
      if (this.activeMenu === "全局设置") {
        this.fullTree.forEach((item) => {
          item.settings.forEach((set) => {
            if (
              set.key === "bdp.dwc.yarnqueue.memory.max" ||
              set.key === "bdp.dwc.client.memory.max"
            ) {
              const unit = set.defaultValue[set.defaultValue.length - 1];
              if (set.configValue) {
                if (
                  set.configValue[set.configValue.length - 1].toLowerCase() !==
                  unit.toLowerCase()
                ) {
                  set.configValue += unit;
                }
              }
            }
          });
        });
      }
    },
    toggleAdvance() {
      this.isAdvancedShow = !this.isAdvancedShow;
    },
    // tag切换触发
    clickTabChange(name) {
      this.currentTabName = name;
      // 切换tab 并更新currentTabId
      this.menuList.map((item) => {
        item.categoryName === name && (this.currentTabId = item.categoryId);
      });
      // 初始化显示数据，筛选index
      let index = this.menuList.findIndex((item) => item.categoryName === name);
      if (index !== -1) {
        let menuListItem = this.menuList[index];
        // 判断是否存在子项，如果存在就进行拼接
        let type =
          menuListItem.childCategory && menuListItem.childCategory.length
            ? `${menuListItem.categoryName}-${menuListItem.childCategory[0].categoryName}`
            : menuListItem.categoryName;
        // 如果this.clickChild存在当前tab的缓存则使用当前缓存，没有则缓存并使用type
        if (this.clickChild[name]) {
          type = this.clickChild[name];
          this.clickChild[name] = type;
        }

        this.getAppVariable(type || "");
      } else {
        this.$Message.error("Failed");
      }
    },
    addChildCategory() {
      this.isChildCategory = true;
    },
    // 点击子项设置
    clickChildCategory(title) {
      // 记录当前tab的子项显示,如果想缓存子项详情则改为getAppVariable内存储
      this.clickChild[this.currentTabName] = title;
      this.getAppVariable(title);
    },
    //编辑引擎
    editChildCategory(index, item) {
      this.isEditChildCategory = true;
      this.editEngineFromItem.type = item.categoryName.split("-")[0];
      this.editEngineFromItem.version = item.categoryName.split("-")[1];
      this.editEngineFromItem.categoryId = item.categoryId;
      this.editEngineFromItem.categoryName =
        item.fatherCategoryName + "-" + item.categoryName;
    },
    //编辑引擎提交
    addEngineDesc() {
      let param = {
        description: this.editEngineFromItem.desc,
        categoryId: this.editEngineFromItem.categoryId,
      };
      api
        .fetch("configuration/updateCategoryInfo", param, "post")
        .then(() => {
          let currentTabItem = this.menuList.find(
            (item) => item.categoryName === this.currentTabName
          );
          currentTabItem.childCategory.map((item) => {
            if (item.categoryId === this.editEngineFromItem.categoryId) {
              item.description = this.editEngineFromItem.desc;
            }
          });
          // this.getMenuList(); //调用getMenuList 重新渲染新增的menuList数据
          this.$Message.success(`修改描述成功`);
        });
    },

    // 删除子项设置(删除应用下的引擎)
    deleteChildCategory(index, item) {
      api
        .fetch(
          "/configuration/deleteCategory",
          { categoryId: item.categoryId },
          "post"
        )
        .then(() => {
          this.getMenuList(); //调用getMenuList 重新渲染新增的menuList数据
        });
    },
    // 点击编辑目录 显示应用和引擎的删除按钮
    handleTabsEdit() {
      this.isTypeCard = !this.isTypeCard;
      this.isdeleteListItem = !this.isdeleteListItem;
      this.iseditListItem = !this.iseditListItem;
      this.isOpenAdd = !this.isOpenAdd;
    },

    //删除目录应用
    removeTab(name) {
      //找到点击删除的tab 在menuList里的数据
      let menuItem = this.menuList.find((item) => item.categoryName === name);
      //判断当前的活动tab是否和当前点击删除的tab一样
      if (this.currentTabName === name) {
        let ItemIndex = this.menuList.findIndex(
          (item) => item.categoryName === this.currentTabName
        );
        this.menuList.splice(ItemIndex, 1);
        api
          .fetch(
            "/configuration/deleteCategory",
            { categoryId: menuItem.categoryId },
            "post"
          )
          .then(() => {
            this.getMenuList(); //调用getMenuList 重新渲染新增的menuList数据
            this.$Message.success(`删除${name}应用成功`);
          });
      } else {
        api
          .fetch(
            "/configuration/deleteCategory",
            { categoryId: menuItem.categoryId },
            "post"
          )
          .then(() => {
            this.getMenuList(); //调用getMenuList 重新渲染新增的menuList数据
            this.$Message.success(`删除${name}应用成功`);
          });
      }
    },

    // 显示新增应用类型modal
    handleTabsAdd() {
      this.isAddApptype = true;
    },
    // 新增引擎配置
    addParameterSet() {
      /* 对输入的 引擎版本信息进行校验 */
      this.$refs.formValidate.validate((valid) => {
        if (valid) {
          api
            .fetch(
              "/configuration/createSecondCategory",
              {
                categoryId: this.currentTabId, //应用Id为后端返回的值
                engineType: this.childCategoryFormItem.type,
                version: this.childCategoryFormItem.version,
                description: this.childCategoryFormItem.desc,
              },
              "post"
            )
            .then(() => {
              this.getMenuList(); //调用getMenuList 重新渲染新增的menuList数据
            });
        } else {
          this.isChildCategory = true;
        }
      });
    },
    // 新增应用类型
    addApptype() {
      
      api
        .fetch(
          "/configuration/createFirstCategory",
          {
            categoryName: this.addApptypeFormItem.name,
            description: this.addApptypeFormItem.desc,
          },
          "post"
        )
        .then(() => {
          this.getMenuList(); //调用getMenuList 重新渲染新增的menuList数据
        });
      
    },
  },
};
</script>
<style src="./index.scss" lang="scss"></style>
