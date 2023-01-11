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
          type: 'select',
          title: this.$t('message.linkis.basedataManagement.udfTree.parent'),
          field: 'parent',
          info: this.$t('message.linkis.basedataManagement.udfTree.parentInfo'),
          value: "",
          props: {
            placeholder: "",
          },
          options: [],
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
  created() {
    getAll().then(res=>{
      let list = res.list.map(m=>{
        return {label: m.name,value: m.id}
      });
      list = [{label: "Root",value: -1},...list]
      this.rule[this.rule.length-1].options = list
    })
  },
}
</script>
