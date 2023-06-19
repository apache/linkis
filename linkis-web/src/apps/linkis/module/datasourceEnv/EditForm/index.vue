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
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.envName'),
          field: 'envName',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceEnv.envName'),
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.envDesc'),
          field: 'envDesc',
          value: '',
          props: {
            placeholder: "",
          },
        },
        //3
        {
          type: "select",
          field: "datasourceTypeId",
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.datasourceType'),
          value: 1,
          options: [],
          validate: [
            {
              required: true,
              type: 'number',
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )} `+this.$t('message.linkis.basedataManagement.datasourceEnv.datasourceType')
            },
          ],
        },
        //4
        {
          type: "radio",
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.keytab'),
          field: "hasKeyTab",
          value: false,
          hidden: true,
          options: [
            {value: false,label: "否",disabled: false},
            {value: true,label: "是",disabled: false},
          ],
          on: {
            'on-change': () => {
              this.rule[8].hidden = !this.rule[4].value;
            }
          }
        },
        //5
        {
          type: "upload",
          field: "pic",
          title: "keytab",
          value: '',
          hidden: true,
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseUpload'
              )}"keytab"`,
              trigger: 'blur',
            },
            {
              validator: this.keyTabValidator,
              trigger: 'blur'
            }
          ],
          props: {
            uploadType: 'file',
            action: "/api/rest_j/v1/bml/upload",
            maxLength: 1,
            multiple: false,
            onSuccess: (res) =>{
              this.formData.keytab = res.data.resourceId;
            }
          },
        },
        //6
        {
          type: 'input',
          title: "keytab",
          field: 'keytab',
          value: '',
          hidden: true,
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseUpload'
              )}"keytab"`,
              trigger: 'blur',
            },
          ],
          props: {
            placeholder: "",
            readonly: true,
            clearable: true,
          },
        },
        //7
        {
          type: 'input',
          title: "uris",
          field: 'uris',
          value: '',
          props: {
            placeholder: 'thrift://127.0.0.1:9083',
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}"uris"`,
              trigger: 'blur',
            },
          ],
        },
        //8
        {
          type: 'input',
          title: "principle",
          field: 'principle',
          value: '',
          hidden: false,
          props: {
            placeholder: 'hadoop@APACHE.COM',
          },
        },
        {
          type: 'v-jsoneditor',
          title: this.$t('message.linkis.basedataManagement.datasourceEnv.parameter'),
          field: 'hadoopConf',
          value: JSON.stringify({}),
          props: {
            type: 'form-create',
            height: "280px",
            options: {
              mode: "code",
              modes: ['code','tree'],
            }
          },
        },
      ]
    }
  },
  created() {
    this.getData(this.data)
  },
  methods: {
    getData(data){
      this.formData = cloneDeep(data);
      // if (this.formData.parameter) {
      //   this.formData.parameter = JSON.parse(this.formData.parameter)
      //   this.formData.uris = this.formData.parameter.uris
      // }
    },
    changeSelector(options){
      this.rule[3].options = [...options];
      options.forEach(ele=> {
        this.keyToName[ele.value] = ele.label;
      })
    },
    hiddenHandler (newV) {
      let dataSourceTypeName = '';
      for(let i = 0;i < this.typeOptions.length; i++) {
        if (this.typeOptions[i].value === newV.datasourceTypeId) {
          dataSourceTypeName = this.typeOptions[i].label
        }
      }
      // radio
      this.rule[4].hidden = !(['hive', 'kafka'].includes(dataSourceTypeName))
      
      // keytab value
      this.rule[6].hidden = !newV.keytab;
      // upload
      this.rule[5].hidden = !this.rule[6].hidden || !['hive', 'kafka'].includes(dataSourceTypeName) || !this.rule[4].value;
      this.rule[8].hidden = (!newV.hasKeyTab || !['hive', 'kafka'].includes(dataSourceTypeName));
      
    },
    keyTabValidator(rule, val, cb) {
      if (!this.formData.keytab) {
        cb(new Error(`${this.$t('message.linkis.datasource.pleaseUpload')}"keytab"`));
      }
      cb();
    },
  },
  watch: {
    typeOptions: {
      handler(newV) {
        this.rule[3].options = newV
      },
      deep: true,
    },
    data: {
      handler(newV) {
        this.hiddenHandler(newV)
        this.getData(newV)
      },
      deep: true,
    },
    formData: {
      handler(newV){
        this.hiddenHandler(newV)
      },
      deep: true
    }
  },
}
</script>
