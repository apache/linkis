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
    />
  </div>
</template>

<script>
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
          title: this.$t('message.linkis.basedataManagement.datasourceType.name'),
          field: 'name',
          value: '',
          props: {
            placeholder: "mysql",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceType.name'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceType.description'),
          field: 'description',
          value: '',
          props: {
            placeholder: "mysql database",
          },
          // validate: [
          //   {
          //     required: true,
          //     message: `${this.$t(
          //       'message.linkis.datasource.pleaseInput'
          //     )} `+this.$t('message.linkis.basedataManagement.datasourceType.description'),
          //     trigger: 'blur',
          //   },
          // ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceType.option'),
          field: 'option',
          value: '',
          props: {
            placeholder: "mysql database",
          },
          // validate: [
          //   {
          //     required: true,
          //     message: `${this.$t(
          //       'message.linkis.datasource.pleaseInput'
          //     )} `+this.$t('message.linkis.basedataManagement.datasourceType.option'),
          //     trigger: 'blur',
          //   },
          // ],
        },
        {
          type: 'select',
          title: this.$t('message.linkis.basedataManagement.datasourceType.classifier'),
          field: 'classifier',
          value: '',
          options: [
            {label: "关系型数据库",value: "关系型数据库"},
            {label: "消息队列",value: "消息队列"},
            {label: "大数据存储",value: "大数据存储"},
            {label: "olap",value: "olap"},
            {label: "分布式全文索引",value: "分布式全文索引"},
          ],
          props: {
            placeholder: "relational database",
            "filterable": true
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceType.classifier'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceType.icon'),
          field: 'icon',
          info: 'example：https://linkis.apache.org/img/logo.png',
          value: '',
          props: {
            placeholder: "eg. https://linkis.apache.org/img/logo.png",
          },
          validate: [
            {
              required: false,
              type: "url",
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceType.icon'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'inputNumber',
          title: this.$t('message.linkis.basedataManagement.datasourceType.layers'),
          field: 'layers',
          value: 0,
          props: {
            placeholder: "eg. 1-3",
            type: "number",
            min: 0
          },
          validate: [
            {
              required: true,
              "mode": "required",
              type: "number",
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceType.layers'),
              trigger: 'change',
            },
            {
              min: 0,
              type: "number",
              message: this.$t('message.linkis.basedataManagement.datasourceType.layersValidate.range'),
              trigger: 'change',
            },
          ],
        }
      ]
    }
  },
}
</script>
