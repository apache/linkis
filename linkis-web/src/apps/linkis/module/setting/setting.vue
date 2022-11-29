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
    <!-- The tab component only does switch selection(tab组件只做切换选择) -->
    <Tabs
      class="tabs"
      v-model="currentTabName"
      @on-click="clickTabChange"
      @on-tab-remove="removeTab"
      :before-remove="showConfirm"
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
      <!-- Apply edit button(应用编辑按钮) -->
      <Button @click="handleTabsEdit" size="small" slot="extra" v-show="isLogAdmin" :class="{active:  iseditListItem || isdeleteListItem}">{{
        iseditListItem || isdeleteListItem ? $t("message.linkis.ConfirmEdit") : $t("message.linkis.editContents")
      }}</Button>
      <Button @click="handleTabsAdd" size="small" type="primary" slot="extra" v-show="isLogAdmin">{{
        $t("message.linkis.addAppType")
      }}</Button>
      <Button @click="handleEngineAdd" size="small" type="primary" slot="extra" v-show="isLogAdmin">{{
        $t("message.linkis.addEngineType")
      }}</Button>
    </Tabs>
    <!-- Parameter details(参数详情) -->
    <div v-if="fullTree && fullTree.length" class="setting-content">
      <div class="setting-content-header">
        <Button @click="toggleAdvance" :class="{active:isAdvancedShow}"
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
    <!-- Engine description modification(引擎描述修改) -->
    <Modal
      @on-ok="addEngineDesc"
      :title="$t('message.linkis.editDescriptionEngineConfig')"
      v-model="isEditChildCategory"
      :mask-closable="false"
    >
      <!-- Editing of engine descriptions(引擎描述的编辑) -->
      <Form
        :label-width="90"
        ref="formValidate"
        :model="editEngineFromItem"
        :rules="ruleValidate"
      >
        <!-- Add the name of the engine(新增引擎的名称) -->
        <FormItem :label="`${$t('message.linkis.tableColumns.engineType')}：`">
          <Select v-model="editEngineFromItem.type" disabled>
            <Option :value="editEngineFromItem.type">{{
              editEngineFromItem.type
            }}</Option>
          </Select>
        </FormItem>
        <!-- Engine version information(引擎版本信息) -->
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

    <!-- Added parameter configuration modal(新增参数配置modal) -->
    <Modal
      @on-ok="addParameterSet"
      :title="$t('message.linkis.addParameterConfig')"
      v-model="isChildCategory"
      :mask-closable="false"
    >
      <!-- Editing of engine descriptions(引擎描述的编辑) -->
      <Form
        :label-width="90"
        ref="formValidate"
        :model="childCategoryFormItem"
        :rules="ruleValidate"
      >
        <!-- Add the name of the engine(新增引擎的名称) -->
        <FormItem :label="`${$t('message.linkis.tableColumns.engineType')}：`">
          <Select v-model="childCategoryFormItem.type">
            <Option :value="item" v-for="item in engineType" :key="item.key">{{
              item
            }}</Option>
          </Select>
        </FormItem>
        <!-- Engine version information(引擎版本信息) -->
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
    <!-- 新增应用类型modal(新增应用类型modal) -->
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
      activeMenu: "", // currently open directory(当前打开目录)
      currentTabName: "",
      fullTree: [],
      engineType: [], //engine type data(引擎类型数据)
      loading: false,
      unValidMsg: {},
      isAdvancedShow: false,
      isChildCategory: false,
      isEditChildCategory: false, //Dialog for editing the engine(编辑引擎的对话框)
      isAddApptype: false,  //New application dialog(新增应用的对话框)
      isTypeCard: false, //Whether the card type is enabled in the tab bar(tab栏是否开启card类型)
      iseditListItem: false, //Whether the engine editing function under the application is enabled(应用下的引擎编辑功能是否开启)
      isdeleteListItem: false, //应用下的引擎删除功能是否开启(应用下的引擎删除功能是否开启)
      isOpenAdd: false, //Whether the engine addition function under the application is enabled(应用下的引擎添加功能是否开启)
      tabLeft: 0,
      isLogAdmin: false,  //Whether the user has administrator privileges(用户是否有管理员权限)
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
    this.subCategory = {}
    // get settings directory(获取设置目录)
    api.fetch("/configuration/getCategory", "get").then((rst) => {
      this.menuList = rst.Category || [];
      this.$nextTick(() => {
        this.getAppVariable(this.menuList[0].categoryName || "");
        this.currentTabName = `${this.menuList[0].categoryName}`;
        if (this.menuList[0] && this.menuList[0].childCategory && this.menuList[0].childCategory.length) {
          this.subCategory = {
            [this.menuList[0].categoryName]: this.menuList[0].childCategory[0]
          }
        }
      });
    });
    //Get all engine types(获取所有引擎类型)
    api.fetch("/configuration/engineType", "get").then((rst) => {
      this.engineType = rst.engineType;
    });
    setTimeout(() => {
      this.isLogAdmin = storage.get("isLogAdmin");
    }, 1500)
  },

  methods: {
    getMenuList() {
      api.fetch("/configuration/getCategory", "get").then((rst) => {
        this.menuList = rst.Category || [];
        this.$nextTick(() => {
          if (this.currentTabName) {
            this.getAppVariable(this.currentTabName || "");
            this.currentTabName = `${this.currentTabName}`;
          } else {
            this.getAppVariable(this.menuList[0].categoryName || "");
            this.currentTabName = `${this.menuList[0].categoryName}`;
          }
        });
      });
    },

    getAppVariable(type = '') {
      this.activeMenu = type;
      let parameter = type.split("-"); // cut directory name(切割目录name)
      // If there is only a first-level directory, it will directly return ['creator'], if it is a second-level directory, ['creator', 'engineType', 'version'](如果只有一级目录则直接返回['creator'],如果为二级目录则['creator', 'engineType', 'version'])
      api
        .fetch(
          "/configuration/getFullTreesByAppName",
          {
            creator: parameter[0], // Specify a first-level directory(指定一级目录)
            engineType: parameter[1], // Specify the engine (secondary directory) if there is only a first-level directory, it will be automatically undefined and no parameters will be passed(指定引擎（二级目录）如果只有一级目录则自动为undefined不会传参)
            version: parameter[2], // The corresponding engine currently only supports the corresponding version. For example, spark will pass version-2.4.3. If there is only a first-level directory, it will be automatically undefined and no parameters will be passed.(对应的引擎目前只支持对应的版本，如spark就传version-2.4.3，如果只有一级目录则自动为undefined不会传参)
          },
          "get"
        )
        .then((rst) => {
          this.loading = false;
          //Whether the currently active tab is the same tab as the clicked engine(当前活动的tab 是否和点击的引擎同一个tab)
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
          engineType: this.subCategory[this.currentTabName] ? this.subCategory[this.currentTabName].categoryName : null
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
    // tag switch trigger(tag切换触发)
    clickTabChange(name) {
      this.currentTabName = name;
      // Initialize display data, filter index(初始化显示数据，筛选index)
      let index = this.menuList.findIndex((item) => item.categoryName === name);
      if (index !== -1) {
        let menuListItem = this.menuList[index];
        let type = ''
        // Determine whether there is a sub-item, and if it exists, splicing(判断是否存在子项，如果存在就进行拼接)
        if (menuListItem.childCategory && menuListItem.childCategory.length) {
          if (!this.subCategory[menuListItem.categoryName]) {
            this.subCategory[menuListItem.categoryName] = menuListItem.childCategory[0]
          }
          type = `${menuListItem.categoryName}-${menuListItem.childCategory[0].categoryName}`
        } else {
          type =  menuListItem.categoryName
        }
        if (this.subCategory[menuListItem.categoryName] && this.subCategory[menuListItem.categoryName]._subCategoryType) {
          type =  this.subCategory[menuListItem.categoryName]._subCategoryType
        }
        this.getAppVariable(type);
      } else {
        this.$Message.error("Failed");
      }
    },
    addChildCategory() {
      this.isChildCategory = true;
    },
    // Click on sub item settings(点击子项设置)
    clickChildCategory(title, index, item) {
      // Record the display of the sub-items of the current tab. If you want to cache the details of the sub-items, store them in getAppVariable instead.(记录当前tab的子项显示,如果想缓存子项详情则改为getAppVariable内存储)
      this.subCategory[this.currentTabName]= {
        ...item,
        _subCategoryType: title
      };
      this.getAppVariable(title);
    },
    //editing engine(编辑引擎)
    editChildCategory(index, item) {
      this.isEditChildCategory = true;
      this.editEngineFromItem.type = item.categoryName.split("-")[0];
      this.editEngineFromItem.version = item.categoryName.split("-")[1];
      this.editEngineFromItem.categoryId = item.categoryId;
      this.editEngineFromItem.categoryName =
        item.fatherCategoryName + "-" + item.categoryName;
    },
    //Edit engine submission(编辑引擎提交)
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
          // this.getMenuList(); //Call getMenuList to re-render the newly added menuList data(调用getMenuList 重新渲染新增的menuList数据)
          this.$Message.success(`修改描述成功`);
        });
    },

    // Delete subitem settings (delete engine under application)(删除子项设置(删除应用下的引擎))
    async deleteChildCategory(index, item) {
      let confirm = await this.showConfirm(index, {
        content: '删除引擎将会删除该应用下此引擎的所有配置，且不可恢复(对所有用户生效)'
      })
      if (confirm) {
        await api
          .fetch(
            "/configuration/deleteCategory",
            { categoryId: item.categoryId },
            "post"
          )
        this.getMenuList(); //Call getMenuList to re-render the newly added menuList data(调用getMenuList 重新渲染新增的menuList数据)
      }
    },
    // Click Edit Directory to display the Delete button for the application and engine(点击编辑目录 显示应用和引擎的删除按钮)
    handleTabsEdit() {
      this.isTypeCard = !this.isTypeCard;
      this.isdeleteListItem = !this.isdeleteListItem;
      this.iseditListItem = !this.iseditListItem;
      this.isOpenAdd = !this.isOpenAdd;
    },

    //delete directory application(删除目录应用)
    removeTab(name) {
      //Find the data in the menuList of the tab you clicked to delete(找到点击删除的tab 在menuList里的数据)
      let menuItem = this.menuList.find((item) => item.categoryName === name);
      //Determine whether the current active tab is the same as the tab currently clicked to delete(判断当前的活动tab是否和当前点击删除的tab一样)
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
            this.getMenuList(); //Call getMenuList to re-render the newly added menuList data(调用getMenuList 重新渲染新增的menuList数据)
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
            this.getMenuList(); //Call getMenuList to re-render the newly added menuList data(调用getMenuList 重新渲染新增的menuList数据)
            this.$Message.success(`删除${name}应用成功`);
          });
      }
    },

    showConfirm(index, options = {}) {
      return new Promise((resolve, reject)=> {
        this.$Modal.confirm({
          title: "提示",
          content: '删除应用将会删除该应用下所有的引擎配置，且不可恢复(对所有用户生效)',
          onOk: () => {
            resolve(true)
          },
          onCancel: () => {
            reject('cancel close')
          },
          ...options
        })
      })
    },

    // Display the new application type modal(显示新增应用类型modal)
    handleTabsAdd() {
      this.isAddApptype = true;
    },
    handleEngineAdd() {
      this.isChildCategory = true;
    },
    // Add engine configuration(新增引擎配置)
    addParameterSet() {
      /* Verify the entered engine version information(对输入的 引擎版本信息进行校验) */
      this.$refs.formValidate.validate((valid) => {
        if (valid) {
          const currentTab = this.menuList.find((item) => {
            return item.categoryName === this.currentTabName;
          });
          api
            .fetch(
              "/configuration/createSecondCategory",
              {
                categoryId: currentTab ? currentTab.categoryId: '',
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
    // Add application type(新增应用类型)
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
          this.getMenuList(); //Call getMenuList to re-render the newly added menuList data(调用getMenuList 重新渲染新增的menuList数据)
        });

    },
  },
};
</script>
<style src="./index.scss" lang="scss"></style>
