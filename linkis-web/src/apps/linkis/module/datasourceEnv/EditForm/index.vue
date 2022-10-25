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
          title: "环境名称",
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
              )}"环境名称"`,
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: "环境描述",
          field: 'envDesc',
          value: '',
          props: {
            placeholder: "",
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}"环境描述"`,
              trigger: 'blur',
            },
          ],
        },
        {
          type: "select",
          field: "datasourceTypeId",
          title: "数据源环境",
          value: 1,
          options: [],
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}"数据源环境"`
            },
          ],
        },
        {
          type: "radio",
          title: "kerboros认证",
          field: "keytab",
          value: false,
          options: [
            {value: false,label: "否",disabled: false},
            {value: true,label: "是",disabled: false},
          ],
          on: {
            'on-change': () => {
              this.rule[5].hidden = !this.rule[5].hidden;
            }
          }
        },
        {
          type: "upload",
          field: "pic",
          title: "keytab",
          value: [],
          hidden: true,
          props: {
            uploadType: 'file',
            action: "/api/rest_j/v1/bml/upload",
            maxLength: 1,
            multiple: false,
            onSuccess: (res) =>{
              let tmpParameter = this.formData.parameter ? JSON.parse(this.formData.parameter) : {};
              tmpParameter.keytab = res.data.resourceId;
              this.formData.parameter = JSON.stringify(tmpParameter);
            }
          },
        },
        {
          type: 'v-jsoneditor',
          title: "参数",
          field: 'parameter',
          value: '',
          props: {
            type: 'form-create',
            height: "280px",
            options: { 
              mode: "code",
              modes: ['code','tree'],
            }
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}"参数"`,
              trigger: 'blur',
            },
          ],
        },
      ]
    }
  },
  created() {
    this.getData(this.data)
  },
  methods: {
    getData(data){
      this.formData = {...data}
      this.formData.parameter = JSON.parse(this.formData.parameter)
    },
    changeSelector(options){
      console.log('test', options)
      this.rule[3].options = [...options];
      options.forEach(ele=> {
        this.keyToName[ele.value] = ele.label;
      })
    },
  },
  watch: {
    data: {
      handler(newV) {
        this.rule[4].hidden = this.keyToName[newV.datasourceTypeId] == 'hive' ? false : true;
        this.rule[5].hidden = !this.formData.keytab;
        if(this.rule[4].hidden) this.rule[5].hidden = true;
        this.getData(newV)
      },
      deep: true,
    },
    formData: {
      handler(newV){
        console.log(this.keyToName)
        this.rule[4].hidden = this.keyToName[newV.datasourceTypeId] == 'hive' ? false : true;
        if(this.rule[4].hidden) this.rule[5].hidden = true;
        else if(this.formData.keytab && newV.datasourceTypeId == 4) this.rule[5].hidden = false;
        else this.rule[5].hidden = true;
      },
      deep: true
    }
  },
}
</script>
