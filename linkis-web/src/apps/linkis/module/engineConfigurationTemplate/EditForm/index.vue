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
import { cloneDeep } from 'lodash'
export default {
  props: {
    mode: String,
    data: Object,
    typeOptions: Array
  },
  data() {
    return {
      keyToName: {},
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
          value: ''
        },
        {
          type: "select",
          field: "engineLabelId",
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineLabelId'),
          value: 1,
          options: [],
          hidden: false,
          validate: [
            {
              required: true,
              type: 'number',
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineLabelId')
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.name'),
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
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.name'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.key'),
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
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.key'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.defaultValue'),
          field: 'defaultValue',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.defaultValue'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.description'),
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
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.description'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: "radio",
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.advanced'),
          field: 'advanced',
          value: '',
          options: [
            {value: 0, label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.no'),},
            {value: 1, label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.yes'),},
          ]
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineConnType'),
          field: 'engineConnType',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.engineConnType'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: "radio",
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.hidden'),
          field: 'hidden',
          value: '',
          options: [
            {value: 0, label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.no'),},
            {value: 1, label: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.yes'),},
          ]
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.level'),
          field: 'level',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.level'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateRange'),
          field: 'validateRange',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateRange'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateType'),
          field: 'validateType',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.validateType'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.treeName'),
          field: 'treeName',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.engineConfigurationTemplate.treeName'),
              trigger: 'blur',
            },
          ],
        }

      ]
    }
  },
  created() {
    this.getData(this.data)
  },
  methods: {
    getData(data){
      this.formData = cloneDeep(data);
    },
  },
  watch: {
    data: {
      handler(newV) {
        this.getData(newV)
        window.console.log(newV)
      },
      deep: true
    },
    formData: {
      handler(){

      },
      deep: true
    }
  },
}
</script>
