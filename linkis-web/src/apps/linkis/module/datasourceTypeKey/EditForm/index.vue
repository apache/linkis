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
export default {
  props: {
    mode: String,
  },
  data() {
    return {
      formModel: {},
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
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.key'),
          field: 'key',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.key'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'select',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.dataSourceType'),
          field: 'dataSourceTypeId',
          options: [],
          value: '',
          props: {
            placeholder: "请选择",
          },
          validate: [
            {
              required: true,
              type: "number",
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.dataSourceType'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.name'),
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
              )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.name'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.nameEn'),
          field: 'nameEn',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.nameEn'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'select',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.valueType'),
          field: 'valueType',
          options: [{label: "TEXT",value: "TEXT"},{label: "PASSWORD",value: "PASSWORD"},{label: "SELECT",value: "SELECT"}],
          value: '',
          props: {
            placeholder: "请选择",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.valueType'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.valueRegex'),
          field: 'valueRegex',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: false,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.valueRegex'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.defaultValue'),
          field: 'defaultValue',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: false,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.defaultValue'),
              trigger: 'blur',
            },
          ],
        },
        // {
        //   type: 'input',
        //   title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.scope'),
        //   field: 'scope',
        //   value: '',
        //   props: {
        //     placeholder: "",
        //   },
        //   validate: [
        //     {
        //       required: false,
        //       message: `${this.$t(
        //         'message.linkis.datasource.pleaseInput'
        //       )} `+this.$t('message.linkis.basedataManagement.datasourceTypeKey.scope'),
        //       trigger: 'blur',
        //     },
        //   ],
        // },
        {
          type: 'switch',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.require'),
          field: 'require',
          value: 0,
          props: {
            placeholder: "",
            trueValue: 1,
            falseValue: 0,
          }
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.description'),
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
              )} ` +this.$t('message.linkis.basedataManagement.datasourceTypeKey.description'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceTypeKey.descriptionEn'),
          field: 'descriptionEn',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} ` +this.$t('message.linkis.basedataManagement.datasourceTypeKey.descriptionEn'),
              trigger: 'blur',
            },
          ],
        },
      ]
    }
  },
}
</script>
