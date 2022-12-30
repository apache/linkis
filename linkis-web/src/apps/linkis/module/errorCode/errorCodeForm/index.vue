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
          title: this.$t('message.linkis.basedataManagement.errorCode.errorCode'),
          field: 'errorCode',
          value: '',
          props: {
            placeholder: "示例:01001",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}`+this.$t('message.linkis.basedataManagement.errorCode.errorCode'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.errorCode.errorDesc'),
          field: 'errorDesc',
          value: '',
          props: {
            placeholder: "示例:引擎在启动时被Kill，请联系管理员",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}`+this.$t('message.linkis.basedataManagement.errorCode.errorDesc'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.errorCode.errorRegex'),
          field: 'errorRegex',
          value: '',
          props: {
            placeholder: "示例:[0-9]+ Killed",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}`+this.$t('message.linkis.basedataManagement.errorCode.errorRegex'),
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
      this.formData = {...data}
    }
  },
  watch: {
    data: {
      handler(newV) {
        this.getData(newV)
      },
      deep: true,
    },
  },
}
</script>
