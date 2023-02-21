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
      formModel: {
        elapseDay: 1,
        permanentlyValid: false,
      },
      // formData: {},
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
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.tokenName'),
          field: 'tokenName',
          info: 'Token Name,Example: TEST-AUTH',
          value: '',
          props: {
            placeholder: "eg. TEST-AUTH",
          },
          validate: [
            {
              required: true,
              pattern: /^[A-Za-z]+-[A-Za-z]+$/g,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}`+this.$t('message.linkis.basedataManagement.gatewayAuthToken.tokenName'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalUsers'),
          field: 'legalUsers',
          value: '',
          info: this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalUsersInfo'),
          props: {
            placeholder: "eg. *",
          },
          validate: [
            {
              required: true,
              validator: (rule, value)=>{
                return new Promise((resolve, reject)=>{
                  if(!value){
                    reject(this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalUsersValidate.empty'))
                  }
                  if(value<-1){
                    reject(this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalUsersValidate.format'))
                  }
                  resolve()
                })
              },
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalHosts'),
          field: 'legalHosts',
          value: '',
          info: this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalHostsInfo'),
          props: {
            placeholder: "eg. *",
          },
          validate: [
            {
              required: true,
              validator: (rule, value)=>{
                return new Promise((resolve, reject)=>{
                  if(!value){
                    reject(this.$t('message.linkis.basedataManagement.gatewayAuthToken.legalHostsInfoValidate.empty'))
                  }
                  resolve()
                })
              },
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'radio',
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.permanentlyValid'),
          field: "permanentlyValid",
          value: false,
          hidden: false,
          options: [
            {value: false,label: "否",disabled: false},
            {value: true,label: "是",disabled: false},
          ],
          on: {
            'on-change': () => {
              this.rule[5].hidden = !this.rule[5].hidden;
              if(!this.rule[5].hidden) {
                this.rule[5].value = 1;
              } else {
                this.rule[5].value = -1;
              }
            }
          }
        },
        {
          type: 'inputNumber',
          title: this.$t('message.linkis.basedataManagement.gatewayAuthToken.elapseDay'),
          field: 'elapseDay',
          value: 1,
          hidden: false,
          info: this.$t('message.linkis.basedataManagement.gatewayAuthToken.info'),
          props: {
            placeholder: "eg . 1",
          },
          validate: [
            {
              required: true,
              validator: (rule, value)=>{
                return new Promise((resolve, reject)=>{
                  if(!value){
                    reject(this.$t('message.linkis.basedataManagement.gatewayAuthToken.elapseDayValidate.empty'))
                  }
                  if(!this.formModel.permanentlyValid && value < 1) {
                    reject(this.$t('message.linkis.basedataManagement.gatewayAuthToken.elapseDayValidate.GT0'))
                  }
                  resolve()
                })
              },
              trigger: 'blur',
            },
          ],
        }
      ]
    }
  },
}
</script>
