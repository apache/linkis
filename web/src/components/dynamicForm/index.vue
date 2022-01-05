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
  <div class="dynamic-form">
    <div class="dynamic-form-header-wrap">
      <h4 class="dynamic-form-header-title">{{ title }}</h4>
    </div>
    <Form ref="dynamicForm" :model="formDynamic" class="dynamic-form-content">
      <FormItem v-for="(item, index) in formDynamic.list" :key="index">
        <Row>
          <Col :lg="2" :md="2" class="tc">{{ index + 1 }}</Col>
          <Col :lg="8" :md="8">
            <!-- 这里必须和上面的model挂钩，prop是以model为基础的路径
                例如第一个key的实际路径是formDynamic.list[0].key，他的prop就是'list.' + index + '.key'
                当一个FormItem里面有两个需要验证表单元素时，每一个子表单元素需要用FormItem包着
            且prop和rules需要写在子FormItem上，rules可以是object或array-->
            <FormItem
              :prop="'list.' + index + '.key'"
              :rules="[{required: true,message: $t('message.common.dynamicForm.rule.emptyKey'),trigger: 'blur'},{min: 1, max: 128, message: $t('message.common.dynamicForm.rule.lengthLimit'), trigger: 'blur'},{type: 'string', pattern: /^[a-zA-z][^\s\u4e00-\u9fa5]*$/, message: $t('message.common.dynamicForm.rule.nameVaild'), trigger: 'blur'},{validator:validateKey,trigger: 'blur'}]"
            >
              <Input
                v-model="item.key"
                type="text"
                :placeholder="$t('message.common.dynamicForm.namePlaceholder', {title})"
                @on-change="onInputChange"
              ></Input>
            </FormItem>
          </Col>
          <Col :lg="2" :md="2" class="tc">=</Col>
          <Col :lg="8" :md="8">
            <FormItem
              :prop="'list.' + index + '.value'"
              :rules="[{required: true, message: $t('message.common.dynamicForm.rule.emptyValue'), trigger: 'blur'},{min: 1, max: 128, message: $t('message.common.dynamicForm.rule.lengthLimit'), trigger: 'blur'}]"
            >
              <Input
                v-model="item.value"
                type="text"
                :placeholder="$t('message.common.dynamicForm.placeholderInput')"
                @on-change="onInputChange"
              ></Input>
            </FormItem>
          </Col>
          <Col span="4" class="tc">
            <SvgIcon class="dynamic-form-close" icon-class="delete" @click.stop="handleDelete(index)"/>
          </Col>
        </Row>
      </FormItem>
    </Form>
    <Button type="dashed" icon="md-add" long @click="handleAdd">添加</Button>
  </div>
</template>
<script>
export default {
  props: {
    title: String,
    list: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      formDynamic: {
        list: []
      },
      validateKey: (rule, value, callback) => {
        const prop = rule.field;
        // 拿到当前编辑的item的index
        const current = prop.slice(
          prop.indexOf(".") + 1,
          prop.lastIndexOf(".")
        );
        // 必须当key相等，而且index不等的时候才是repeat
        const findRepeat = this.formDynamic.list.find((item, index) => {
          return item.key === value && current != index;
        });
        if (findRepeat) {
          callback(new Error(this.$t("message.common.dynamicForm.someKey")));
        }
        callback();
      }
    };
  },
  watch: {
    list() {
      this.formDynamic.list = this.list;
    }
  },
  mounted() {
    this.formDynamic.list = this.list;
  },
  methods: {
    handleAdd() {
      if (this.formDynamic.list.length) {
        this.$refs.dynamicForm.validate(valid => {
          if (valid) {
            this.formDynamic.list.push({
              key: "",
              value: ""
            });
          } else {
            this.$Message.warning(this.$t("message.common.failedNotice"));
          }
        });
      } else {
        this.formDynamic.list.push({
          key: "",
          value: ""
        });
      }
    },
    handleDelete(index) {
      this.formDynamic.list.splice(index, 1);
      this.$emit("change", this.formDynamic.list);
    },
    onInputChange() {
      this.$emit("change", this.formDynamic.list);
    }
  }
};
</script>
<style lang="scss" src="./index.scss"></style>
