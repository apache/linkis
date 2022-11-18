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
      v-model="fApi"
      :option="options"
      :value.sync="formData"
    />
  </div>
</template>
<script>
import _, { merge, mergeWith } from 'lodash'
import api from '@/common/service/api'
import { getKeyDefine, getDataSourceByIdAndVersion } from '../dataSourceApi'
// import {mysql, hive} from '../datasource';

const type = {
  TEXT: { type: 'input' },
  NUMBER: { type: 'input', props: { type: 'number', } },
  PASSWORD: { type: 'input', props: { type: 'password' } },
  EMAIL: { type: 'input', props: { type: 'email' } },
  DATE: { type: 'input', props: { type: 'date' } },
  TEL: { type: 'input', props: { type: 'tel' } },
  TEXTAREA: { type: 'input', props: { type: 'textarea' } },
  URL: { type: 'input', props: { type: 'url' } },
  FILE: (self, source, data) => {
    return {
      type: 'upload',
      props: {
        uploadType: 'file',
        action: data.dataSource,
        allowRemove: true,
        maxLength: 1,
        beforeUpload: () => {
          return true
        },
        onSuccess: (res, file) => {
          self.file = file
          source.props.value = res.data ? res.data.resourceId : res.data
          file.url = res.id
          console.log("resourceId:"+res.data.resourceId);
        },
      },
    }
  },
  SELECT: { type: 'select', props: { placement: 'bottom' } },
}
const typesMap = {
  valueType: (data, source, self) => {
    if (type[data.valueType]) {
      if (typeof type[data.valueType] === 'function') {
        return type[data.valueType](self, source, data)
      }
      return type[data.valueType]
    } else {
      return { type: data.valueType }
    }
  },
  name: 'title',

  defaultValue: 'value',
  // dataSource: 'options',
  dataSource: (data, source, self) => {
    const fApi = self.fApi
    try {
      return { options: JSON.parse(data.dataSource) }
    } catch (error) {
      if (!(data.valueType == 'upload' || data.valueType == 'FILE')) {
        api.fetch(data.dataSource, {}, 'get').then((result) => {
          delete source.options
          source.options = result.envList.map((item) => {
            return { label: item.envName, value: '' + item.id }
          })
          // console.log('self.rule',self.rule)
          fApi.refreshOptions()
        })
        return { options: [] }
      }
    }
  },
  key: 'field',
  description: (data) => {
    return { props: { placeholder: data.description } }
  },
  require: (data) => {
    if (data.require) {
      if (data.type == 'upload' || data.type == 'FILE') {
        return {
          validate: [
            {
              required: true,
              message: `请输入${data.name}`,
              trigger: 'blur',
              validator: function (rule, value, callback) {
                if (value.length < 1) callback(false)
              },
            },
          ],
        }
      }

      return {
        validate: [
          { required: true, message: `请输入${data.name}`, trigger: 'blur' },
        ],
      }
    } else return null
  },
  valueRegex: (data) => {
    if (data.valueRegex) {
      return {
        validate: [
          {
            pattern: new RegExp(data.valueRegex),
            message: '不符合规则',
            trigger: 'blur',
          },
        ],
      }
    } else return null
  },
  // valueRegex: (data)=>{
  //   if(data.valueRegex)
  //     return {validate: { pattern: new RegExp(data.valueRegex), message: '不符合规则', trigger: 'blur' }}
  //   else return null
  // },

  refId: 'refId',
  refValue: 'refValue',
  id: 'id',
}
export default {
  props: {
    data: Object,
    type: String,
  },
  data() {
    return {
      sourceConnectData: {},
      fApi: {},
      loading: false,
      formData: { file: 'adn' },
      options: {
        submitBtn: false,
      },
      rule: [
        {
          type: 'input',
          title: this.$t('message.linkis.datasource.sourceName'),
          field: 'dataSourceName',
          value: '',
          props: {
            placeholder: this.$t('message.linkis.datasource.sourceName')
          },
          validate: [
            {
              required: true,
              message: `${this.$t(
                'message.linkis.datasource.pleaseInput'
              )}${this.$t('message.linkis.datasource.sourceName')}`,
              trigger: 'blur',
            },
          ],
        },
        {
          type: 'input',
          title: this.$t('message.linkis.datasource.sourceDec'),
          field: 'dataSourceDesc',
          value: '',
          props: {
            placeholder: this.$t('message.linkis.datasource.sourceDec')
          },
        },
        {
          type: 'input',
          title: this.$t('message.linkis.datasource.label'),
          field: 'labels',
          value: '',
          props: {
            placeholder: this.$t('message.linkis.datasource.label')
          },
        },
      ],
    }
  },
  created() {
    this.loading = true

    this.getDataSource(this.data)

    getKeyDefine(this.data.dataSourceTypeId).then((data) => {
      this.loading = false
      this.transformData(data.keyDefine)
    })
  },
  watch: {
    data: {
      handler(newV) {
        this.getDataSource(newV)
      },
      deep: true,
    },
  },
  methods: {
    getDataSource(newV) {
      if (this.data.id) {
        getDataSourceByIdAndVersion(newV.id, newV.versionId || 0).then(
          (result) => {
            const mConnect = result.info.connectParams
            this.sourceConnectData = mConnect
            delete result.info.connectParams
            this.dataSrc = { ...result.info, ...mConnect }
            this.formData = { ...result.info, ...mConnect }
          }
        )
      } else {
        const connectParams = newV.connectParams
        delete newV.connectParams
        this.formData = { ...newV, ...connectParams }
        this.dataSrc = { ...newV, ...connectParams }
      }
    },
    transformData(keyDefinitions) {
      const tempData = []

      keyDefinitions.forEach((obj) => {
        let item = {}
        Object.keys(obj).forEach((keyName) => {
          switch (typeof typesMap[keyName]) {
            case 'object':
              item = merge({}, item, typesMap[keyName])
              break

            case 'function':
              item = mergeWith(
                item,
                typesMap[keyName](obj, item, this),
                function (objValue, srcValue) {
                  if (_.isArray(objValue)) {
                    return objValue.concat(srcValue)
                  }
                }
              )
              break

            case 'string':
              item[typesMap[keyName]] = obj[keyName]
              break
          }
        })
        tempData.push(item)
      })

      const insertParent = (id, child) => {
        let parent = tempData.find((item) => id == item.id)
        if (parent && child) {
          if (!parent.control || parent.control.length === 0) {
            parent.control = [
              //不存在新建
              {
                value: child.refValue,
                rule: [{ ...child }],
              },
            ]
          } else {
            let index = parent.control.findIndex(
              (item) => item.value + '' === child.refValue + ''
            )
            if (index > -1) {
              parent.control[index].rule.push({ ...child })
            } else {
              parent.control.push({
                value: child.refValue,
                rule: [{ ...child }],
              })
            }
          }
        }
      }

      for (var i = 0; i < tempData.length; i++) {
        let item = tempData[i]
        if (item.refId && item.refId > 0) {
          let children = tempData.splice(i, 1)
          i--
          insertParent(item.refId, children[0])
        }
      }
      this.rule = this.rule.concat(tempData)
      let disabled = this.type === this.$t('message.linkis.datasource.watch')
      this.rule.forEach((item) => {
        item.props.disabled = disabled;
      })
      return tempData
    },
  },
}
</script>

<style lang="scss" scoped src="./index.scss"></style>
