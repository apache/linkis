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
  <div class="table-warp">
    <form-create
      :rule="rule"
      v-model="formModel"
      :option="options"
      :value.sync="formData"
      ref="formRef"
    />
  </div>
</template>

<script>
import {getAll} from "../service";
export default {
  props: {
    mode: String,
    data: Object,
  },
  data() {
    return {
      formModel: {},
      formData: {},
      options: {
        submitBtn: false,
      },
      avoidMultiTime: new Date().getTime(),
      formRef: null,
      rule: [
        {
          type: 'hidden',
          title: "id",
          field: 'id',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: false,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}"id"`,
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.udfTree.name'),
          field: 'name',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.udfTree.name'),
              trigger: 'blur',
            },
            {
              max: 100,
              message: this.$t('message.linkis.basedataManagement.categoryMaxLength'),
              trigger: 'change',
            },
          ],
        },
        {
          type: 'select',
          title: this.$t('message.linkis.basedataManagement.udfTree.category'),
          field: 'category',
          value: '',
          options: [
            {"value": "udf", "label": "UDF", "disabled": false},
            {"value": "function", "label": "函数", "disabled": false},
          ],
          props: {
            multiple: false,
            placeholder: "请选择",
            notFoundText: "无匹配数据",
            placement: "bottom",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.udfTree.category'),
              trigger: 'blur',
            },
          ],
          on: {
            'on-change': this.handleChange
          }
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.udfTree.userName'),
          field: 'userName',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.udfTree.userName'),
              trigger: 'blur',
            },
          ],
          on: {
            'on-change': this.handleChange
          }
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.udfTree.description'),
          field: 'description',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}`+this.$t('message.linkis.basedataManagement.udfTree.description'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'tree',
          title: this.$t('message.linkis.basedataManagement.udfTree.parent'),
          field: 'parent',
          info: this.$t('message.linkis.basedataManagement.udfTree.parentInfo'),
          value: "",
          props: {
            placeholder: "",
            data: [],
          },
          nativeEmit: ['click'],
          on: {
            'on-select-change': (val) => {
              this.formData.parent = val[0].id;
            }
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.udfTree.parent'),
              trigger: 'blur',
              type: 'number'
            },
          ],
        }
      ]
    }
  },
  methods: {
    async handleFetch(category, userName) {
      if(!category || !userName) {
        this.rule[this.rule.length-1].props.data = [];
        return;
      }
      try {
        await getAll({ category, searchName: userName }).then(res=>{
          let list = res.list;
          this.formatTree(list, this.rule[this.rule.length - 1].value)
          this.rule[this.rule.length-1].props.data = list;
        })
      } catch(err) {
        // console.warn(err);
      }
      
    },
    async handleChange() {
      this.rule[this.rule.length-1].value = '';
      this.rule[this.rule.length-1].props.data = [];
      if(this.rule[3].value && this.rule[2].value) {
        await this.handleFetch(this.rule[2].value, this.rule[3].value)
      }
    },
    async parentClick () {
      const { category, userName } = this.$refs.formRef.value
      if(!category || !userName) {
        if(new Date().getTime() - this.avoidMultiTime > 3000) {
          this.$Message.warning(this.$t('message.linkis.needPre'))
          this.avoidMultiTime = new Date().getTime()
        } 
      }
    },
    formatTree (tree, curId) {
      let expand = false;
      tree.forEach((item) => {
        
        item.title = item.name;
        item.children = item.childrenList
        if(item.children && item.children.length > 0) {
          item.expand = this.formatTree(item.children, curId)
          if(item.expand) {
            expand = item.expand
          }
          
        }
        if(curId === item.id) {
          item.selected = true;
          expand = true;
        }
      })
      return expand
    }
  },
  mounted() {
    this.formModel.on('native-parent-click', this.parentClick)
  },
  watch: {
    data: {
      handler(newVal) {
        this.handleFetch(newVal.category, newVal.userName)
      },
      deep: true,
      immediate: true
    },
  }
}
</script>
<style lang="scss" scoped>

/deep/ .ivu-tree {
  .ivu-tree-children {
    li {
      margin: 0px
    }
  }
}
</style>
