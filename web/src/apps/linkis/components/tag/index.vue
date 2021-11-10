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
  <div class="linkiesTagModal">
    <Tooltip
      v-for="item in tagList"
      :key="`${item.key}${item.value}`"
      :content="`${item.key}-${item.value}`"
      placement="top"
    >
      <Tag
        ref="tag"
        v-if="
          !clickValue.includes(item.value)
        "
        :name="`${item.key}${item.value}`"
        :closable ="item.modifiable ? true : false"
        type="dot"
        :checkable="item.modifiable ? true : false"
        :color="item.modifiable ? 'primary' : 'default'"
        @on-close="handleClose2"
        @on-change="handleTagChange"
      >{{ item.key }}-{{ item.value }}
      </Tag>
      <!-- 编辑标签 -->
      <div class="addAndCancel" v-else>
        <Input ref="editInputValue" :value="item.value" style="width: 200px" @on-enter="exitTags">
          <Select ref="editInputKey" :value="item.key"  disabled  slot="prepend" style="width: 80px">
            <Option
              v-for="item in selectList"
              :key="item.value"
              :value="item.value"
            >{{ item.lable }}</Option
            >
          </Select>
        </Input>
        <ButtonGroup style="margin-left: 10px">
          <Button type="primary" @click="exitTags">
            {{ $t("message.linkis.submit") }}
          </Button>
          <Button @click="handleExitTagsCancel">
            {{ $t("message.linkis.cancel") }}
          </Button>
        </ButtonGroup>
      </div>
    </Tooltip>



    <!-- 新增标签 -->
    <div class="addAndCancel" v-if="adding">
      <Input v-model="value2" style="width: 200px" @on-enter="change">
        <Select v-model="value1" slot="prepend" style="width: 80px">
          <Option
            v-for="item in selectList"
            :key="item.value"
            :value="item.value"
          >{{ item.lable }}</Option
          >
        </Select>
      </Input>
      <ButtonGroup style="margin-left: 10px">
        <Button type="primary" @click="change">
          {{ $t("message.linkis.submit") }}
        </Button>
        <Button @click="adding = false">
          {{ $t("message.linkis.cancel") }}
        </Button>
      </ButtonGroup>
    </div>
    <Button
      v-if="!adding"
      class="addTags"
      icon="ios-add"
      type="dashed"
      size="small"
      @click="handleAdd"
    >{{ $t("message.linkis.addTags") }}</Button
    >
  </div>
</template>
<script>
export default {
  name: "ECMTag",
  props: {
    // tag渲染列表数据
    tagList: {
      type: Array,
      default: () => [],
    },
    // 默认的key值
    currentKey: {
      type: String,
      default: "http",
    },
    // 可选的key值
    selectList: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      value2: "", // input
      value1: this.currentKey, // select
      editValue: "",
      adding: false,
      clickValue: "",
      tagClickValue: "",
      inputCurrentValue: [],
      editing: false,
      exitValue: "",
    };
  },
  computed: {},
  /*  watch: {
    clickValue() {
      this.inputCurrentValue = this.tagList.find((item) => {
        return item.key + item.value === this.clickValue;
      });
    },
  }, */
  methods: {
    handleAdd() {
      this.adding = true;
      this.value2 = "";
    },

    handleClose2(event, name) {
      const index = this.tagList.findIndex(
        (item) => `${item.key}${item.value}` === name
      );
      this.$emit("onCloseTag", name, index, event);
    },
    change() {
      /* if(this.value1 && this.value2) {
        this.$emit('addEnter', this.value1, this.value2)
        this.adding = false;
      } else {
        this.$Message.error( "标签内容不能为空！");
      } */
      let reg = /[`~!#$%^&*()_\+=<>?:"{}|~！#￥%……&*（）={}|《》？：“”【】、；‘’，。、\s+]/g;
      if (reg.test(this.value2)) {
        this.$Message.error("标签内容不能为特殊符号和空格！");
      } else if (this.value2.length >= 16) {
        this.$Message.error("标签内容长度不超过16！");
      } else if (this.value1 && this.value2) {
        this.$emit("addEnter", this.value1, this.value2);
        this.adding = false;
      }
    },

    handleTagChange(cheacked, name) {
      this.editing = true;
      this.clickValue = name;
      // input 聚焦
      this.$nextTick(()=> {
        this.$refs.editInputValue[0].$refs.input.focus()
      })
    },
    //编辑确认
    exitTags() {
      // 修改前的值
      let editInputKey = this.$refs.editInputKey[0].value;
      let editInputValue = this.$refs.editInputValue[0].value;
      //当前修改之后的值
      let editedInputValue = this.$refs.editInputValue[0].$refs.input.value;
      // console.log(editInputKey, editInputValue,editedInputValue);
      let reg = /[`~!#$%^&*()_\+=<>?:"{}|~！#￥%……&*（）={}|《》？：“”【】、；‘’，。、\s+]/g;
      if (reg.test(editedInputValue)) {
        this.$Message.error("标签内容不能为特殊符号和空格！");
      } else if (editedInputValue.length >= 16) {
        this.$Message.error("标签内容长度不超过16！");
      } else if (editInputKey && editedInputValue) {
        this.$emit("editEnter", editInputKey, editInputValue,editedInputValue);
        this.clickValue = '';
      }
    },

    handleExitTagsCancel() {
      this.clickValue = ''
    }
  },
};
</script>
<style lang="scss" scoped>
.linkiesTagModal {
  /deep/ .ivu-tag {
    vertical-align: middle;
    /deep/ .ivu-tag-text {
      display: inline-block;
      max-width: 140px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      vertical-align: middle;
    }
  }
  /deep/ .ivu-tooltip-inner {
    max-width: 100%;
  }
  .addAndCancel {
    display: flex;
    align-items: center;
  }
  .ecmBtn {
    color: #fff;
    background-color: #2d8cf0;
    border: 0px;
    border-color: #2d8cf0;
    height: 24px;
    vertical-align: middle;
    line-height: 24px;
    padding: 0 5px;
    border-radius: 4px;
    margin: 0px 10px;
  }
  .addTags{
    margin-left: 5px;
  }
}
</style>
