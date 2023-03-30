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
  <Modal
    v-model="show"
    :mask-closable="false"
    :class-name="isFullScreen ? 'full-screen udf-modal' : 'udf-modal'"
  >
    <div slot="header">
      <span>{{ title }}</span>
      <span class="full-screen-button" @click="isFullScreen = !isFullScreen">
        <Icon v-if="!isFullScreen" type="ios-expand" />
        <Icon v-if="isFullScreen" type="ios-contract" />
        {{ isFullScreen ? $t('message.linkis.udf.QXQP') : $t('message.linkis.udf.QPZS')}}
      </span>
    </div>
    <Form
      ref="setting"
      :model="setting"
      :rules="rules"
      :label-width="90"
      :style="{'height': modalHeight}"
      class="setting-content">
      <FormItem
        :label="$t('message.linkis.udf.functionName')"
        prop="name">
        <Input v-model="setting.name" :disabled="isUdf && model === 1" />
      </FormItem>
      <FormItem
        :label="$t('message.linkis.udf.functionType')"
        prop="fnType">
        <RadioGroup
          v-model="setting.fnType"
          @on-change="onTypeChange">
          <Radio
            v-if="isUdf"
            :disabled="isUdf && model === 1"
            :label="$t('message.linkis.udf.common')"/>
          <Radio
            v-if="isUdf"
            :disabled="isUdf && model === 1"
            label="Spark"/>
          <Radio
            v-if="!isUdf"
            :label="$t('message.linkis.udf.ZDYHS')"/>
        </RadioGroup>
      </FormItem>
      <FormItem
        v-if="fnCategory.isCommon"
        :label="$t('message.linkis.udf.jarPackage')"
        prop="jarPath">
        <directory-dialog
          :tree="tree"
          :load-data-fn="loadDataFn"
          :filter-node="filterNode"
          :path="fnPath"
          fs-type="share"
          @set-node="setNodePath"/>
      </FormItem>
      <FormItem
        v-if="fnCategory.isSpark"
        :label="$t('message.linkis.udf.scriptPath')"
        prop="sparkPath">
        <directory-dialog
          :tree="tree"
          :load-data-fn="loadDataFn"
          :filter-node="filterNode"
          :path="fnPath"
          fs-type="share"
          @set-node="setNodePath"/>
      </FormItem>
      <FormItem
        v-if="fnCategory.isCustom"
        :label="$t('message.linkis.udf.scriptPath')"
        prop="customPath">
        <directory-dialog
          :tree="tree"
          :load-data-fn="loadDataFn"
          :filter-node="filterNode"
          :path="fnPath"
          fs-type="share"
          @set-node="setNodePath"/>
      </FormItem>
      <FormItem
        v-if="fnType === 0 && fnCategory.isCommon"
        :label="$t('message.linkis.udf.registerFormat')"
        prop="jarPara">
        <Input
          v-model="setting.jarPara"
          :placeholder="$t('message.linkis.udf.registerFunction')"/>
      </FormItem>
      <FormItem
        v-else-if="fnType === 1 && fnCategory.isSpark"
        :label="$t('message.linkis.udf.registerFormat')"
        prop="pyPara">
        <Input
          v-model="setting.pyPara"
          :placeholder="$t('message.linkis.udf.registerFunction')"/>
      </FormItem>
      <FormItem
        v-else-if="fnType === 2 && fnCategory.isSpark"
        :label="$t('message.linkis.udf.registerFormat')"
        class="ivu-form-item-required">
        <div class="format-div">
          <FormItem prop="scalaTypeL">
            <Input
              v-model="setting.scalaTypeL"
              :placeholder="$t('message.linkis.udf.returnType')"
              class="format-input"
              @input="resetRF"/>
          </FormItem>
          <FormItem prop="scalaTypeR">
            <Input
              v-model="setting.scalaTypeR"
              :placeholder="$t('message.linkis.udf.paramType')"
              class="format-input"
              @input="resetRF"/>
          </FormItem>
          <FormItem prop="scalapara">
            <Input
              v-model="setting.scalapara"
              :placeholder="$t('message.linkis.udf.registerFunction')"/>
          </FormItem>
        </div>
      </FormItem>
      <FormItem
        v-if="fnType >= 0 && fnType < 3"
        :label="$t('message.linkis.udf.preview')">
        <span :title="registerFormat">
          <jar-preview
            v-if="fnType === 0 && fnCategory.isCommon"
            :name="setting.name"
            :jar-para="setting.jarPara"/>
          <py-preview
            v-else-if="fnType === 1 && fnCategory.isSpark"
            :name="setting.name"
            :py-para="setting.pyPara"/>
          <scala-preview
            v-else-if="fnType === 2 && fnCategory.isSpark"
            :name="setting.name"
            :type-l="setting.scalaTypeL"
            :type-r="setting.scalaTypeR"
            :para="setting.scalapara"/>
        </span>
      </FormItem>
      <FormItem
        :label="$t('message.linkis.udf.useFormat')"
        class="ivu-form-item-required">
        <div class="format-div">
          <FormItem class="format-item">
            <Input
              v-if="fnType===2"
              v-model="setting.scalaTypeL"
              :placeholder="$t('message.linkis.udf.returnType')"
              disabled
              class="format-input"/>
            <Input
              v-else
              v-model="setting.useFormatParaL"
              :placeholder="$t('message.linkis.udf.returnType')"
              class="format-input"/>
          </FormItem>
          <FormItem class="format-item">
            <Input
              v-if="fnType===2"
              v-model="setting.scalaTypeR"
              :placeholder="$t('message.linkis.udf.inputType')"
              disabled/>
            <Input
              v-else
              v-model="setting.useFormatParaR"
              :placeholder="$t('message.linkis.udf.inputType')"/>
          </FormItem>
        </div>
      </FormItem>
      <FormItem :label="$t('message.linkis.udf.preview')">
        <use-preview
          :name="setting.name"
          :para-l="fnType===2 ? setting.scalaTypeL : setting.useFormatParaL"
          :para-r="fnType===2 ? setting.scalaTypeR : setting.useFormatParaR"
          :title="useFormat"/>
      </FormItem>
      <FormItem :label="$t('message.linkis.udf.functionDescription')">
        <Input
          v-model="setting.description"
          type="textarea"/>
      </FormItem>
      <FormItem :label="$t('message.linkis.udf.class')" prop="directory">
        <Select
          ref="directory"
          v-model="setting.directory"
          filterable
          :remoteMethod="filterAdd"
          @on-query-change="queryChange"
          :disabled="isUdf && model === 1">
          <Option
            v-for="(item) in directories"
            :label="item"
            :value="item"
            :key="item"/>
        </Select>
      </FormItem>
      <FormItem :label="$t('message.linkis.udf.clusterName')">
        <Select v-model="setting.clusterName" :disabled="isUdf && model === 1">
          <Option
            label="all"
            value="all" />
        </Select>
      </FormItem>
    </Form>
    <template slot="footer">
      <Checkbox
        :disabled="isUdf && model === 1"
        v-model="setting.defaultLoad"
        class="fn-default-load">{{ $t('message.linkis.udf.defaultLoad') }}</Checkbox>
      <Button
        :loading="loading"
        type="primary"
        class="fn-sure-btn"
        @click="submitForm('setting')">{{ btnLabel }}</Button>
    </template>
  </Modal>
</template>
<script>
import { get, trim } from 'lodash';
import jarPreview from './jarPreview.vue';
import usePreview from './usePreview.vue';
import pyPreview from './pyPreview.vue';
import scalaPreview from './scalaPreview.vue';
import api from '@/common/service/api'
import directoryDialog from '@/components/directoryDialog/index.vue';

export default {
  components: {
    jarPreview,
    pyPreview,
    scalaPreview,
    usePreview,
    directoryDialog,
  },
  props: {
    loading: {
      type: Boolean,
      default: false,
    },
    tree: {
      type: Array,
      default: () => [],
    },
    isUdf: {
      type: Boolean,
      defalut: true,
    },
    loadDataFn: Function
  },

  data() {
    return {
      fnCategory: {
        isSpark: false,
        isCommon: true,
        isCustom: false,
      },
      TYPELIB: {
        jar: 0,
        Spark: {
          py: 1,
          scala: 2,
        },
        custom: {
          py: 3,
          scala: 4,
        },
      },
      title: '',
      show: false,
      btnLabel: this.$t('message.common.ok'),
      modalHeight: '',
      // 1是修改，0是新增)
      model: 0,
      node: null,
      // 用于记录show的时候scala的注册格式和使用格式
      showScalaRF: '',
      showScalaUF: '',
      setting: {
        name: '',
        fnType: this.$t('message.linkis.udf.common'),
        jarPath: '',
        sparkPath: '',
        customPath: '',
        useFormatParaL: '',
        useFormatParaR: '',
        jarPara: '',
        pyPara: '',
        scalaTypeL: '',
        scalaTypeR: '',
        scalapara: '',
        description: '',
        defaultLoad: true,
        clusterName: 'all',
        directory: ''
      },
      isShareLoading: false,
      rules: {
        name: [
          { required: true, message: this.$t('message.linkis.udf.QSRMC'), trigger: 'blur' },
          {
            min: 1,
            max: 100,
            message: this.$t('message.linkis.udf.CDZ'),
            trigger: 'change',
          },
          {
            type: 'string',
            pattern: /^[a-zA-Z][a-zA-Z0-9_\u4e00-\u9fa5]*$/,
            message: this.$t('message.linkis.udf.BXYZMKT'),
            trigger: 'change',
          },
        ],
        fnType: [
          {
            required: true,
            message: this.$t('message.linkis.udf.QZSXZYZLX'),
            trigger: 'change',
          },
        ],
        jarPath: [
          {
            required: true,
            message: this.$t('message.linkis.udf.QSRWZLJ'),
            trigger: 'change',
          },
          // $t('message.linkis.udf.KHDBJZPP')
          {
            type: 'string',
            validator: this.jarValidator,
            trigger: 'change',
          },
        ],
        sparkPath: [
          {
            required: true,
            message: this.$t('message.linkis.udf.QSRWZLJ'),
            trigger: 'change',
          },
          // $t('message.linkis.udf.KHDBJZPP')
          {
            type: 'string',
            validator: this.pyValidator,
            trigger: 'change',
          },
        ],
        customPath: [
          {
            required: true,
            message: this.$t('message.linkis.udf.QSRWZLJ'),
            trigger: 'change',
          },
          // $t('message.linkis.udf.KHDBJZPP')
          {
            type: 'string',
            validator: this.pyValidator,
            trigger: 'change',
          },
        ],
        jarPara: [
          {
            required: true,
            message: this.$t('message.linkis.udf.SRZCHS'),
            trigger: 'blur',
          },
        ],
        pyPara: [
          {
            required: true,
            message: this.$t('message.linkis.udf.SRZCHS'),
            trigger: 'blur',
          },
        ],
        scalaTypeL: [
          {
            type: 'string',
            required: true,
            message: this.$t('message.linkis.udf.SRFHLX'),
            trigger: 'blur',
          },
        ],
        scalaTypeR: [
          {
            type: 'string',
            required: true,
            message: this.$t('message.linkis.udf.SRCSLX'),
            trigger: 'blur',
          },
        ],
        scalapara: [
          {
            type: 'string',
            required: true,
            message: this.$t('message.linkis.udf.SRZCHS'),
            trigger: 'blur',
          },
        ],
        directory: [
          {
            type: 'string',
            required: true,
            message: this.$t('message.linkis.udf.SRFL'),
            trigger: 'change',
          },
        ]
      },
      directories: [],
      filterNode: () => {},
      // 是否全屏展示弹框
      isFullScreen: false
    };
  },

  computed: {
    fnPath() {
      return this.setting[this.getTypes()];
    },

    fnType() {
      const tmp = this.fnPath;
      const suffix = tmp
        ? tmp.substring(tmp.lastIndexOf('.') + 1, tmp.length)
        : '';
      let prop = '';
      if (this.fnCategory.isCommon) {
        prop = 'jar';
      } else if (this.fnCategory.isSpark) {
        prop = `Spark.${suffix}`;
      } else {
        prop = `custom.${suffix}`;
      }
      return get(this.TYPELIB, prop);
    },

    registerFormat() {
      if (this.fnType === 0) {
        return `create temporary function ${this.setting.name} as "${trim(this.setting.jarPara)}"`;
      } else if (this.fnType === 1) {
        return `udf.register("${this.setting.name}",${
          trim(this.setting.pyPara)
        })`;
      } else if (this.fnType === 2) {
        return `sqlContext.udf.register[${this.firstUpperCase(
          this.setting.scalaTypeL
        )},${this.firstUpperCase(this.setting.scalaTypeR)}]("${
          this.setting.name
        }",${trim(this.setting.scalapara)})`;
      }
      return null;
    },

    useFormat() {
      let type = trim(this.setting.useFormatParaL) || '';
      let input = trim(this.setting.useFormatParaR) || '';
      if (this.setting.useFormatParaL) {
        if (this.fnType === 1 || this.fnType === 3) {
          type = trim(this.setting.useFormatParaL);
        } else if (this.fnType === 2) {
          type = this.firstUpperCase(this.setting.scalaTypeL);
          input = this.firstUpperCase(this.setting.scalaTypeR);
        } else if (this.fnType === 4) {
          type = this.firstUpperCase(this.setting.useFormatParaL);
          input = this.firstUpperCase(this.setting.useFormatParaR);
        }
      }
      return trim(`${type} ${this.setting.name}(${input})`);
    }
  },

  watch: {
    loading: function(val) {
      if (!val) {
        this.show = false;
      }
    },
    'setting.fnType'(val) {
      let map = {};
      map[this.$t('message.linkis.udf.common')] = 'isCommon';
      map['Spark'] = 'isSpark';
      map[this.$t('message.linkis.udf.ZDYHS')] = 'isCustom';
      let type = map[val];
      Object.keys(this.fnCategory).forEach((key) => this.fnCategory[key] = false);
      this.fnCategory[type] = true;
    },
  },

  mounted() {
    // 监听窗口变化，获取浏览器宽高
    window.addEventListener('resize', this.getHeight);
    this.getDir();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.getHeight);
  },
  created() {
    // 创建的时候设置宽高
    this.resize(window.innerHeight);
  },

  methods: {
    getHeight() {
      this.resize(window.innerHeight);
    },
    open(node) {
      this.resetData();
      const titleMap = [this.$t('message.linkis.udf.addFunction'), this.$t('message.linkis.udf.updateFunction')];
      const btnLabelMap = [this.$t('message.common.ok'), this.$t('message.linkis.udf.XG')];
      this.show = true;
      this.node = node;
      if (node) {
        this.model = 1;
        this.init();
      } else {
        this.model = 0;
        this.$refs.directory.setQuery(null);
        this.setting.fnType = this.isUdf ? this.$t('message.linkis.udf.common') : this.$t('message.linkis.udf.ZDYHS');
      }
      this.title = titleMap[this.model];
      this.btnLabel = btnLabelMap[this.model];
      if (this.setting.fnType === 'Spark') {
        this.filterNode = (node) => {
          const name = node.label;
          const tabSuffix = name.substr(name.lastIndexOf('.'), name.length);
          return !node.isLeaf || (node.isLeaf && (tabSuffix === '.py' || tabSuffix === '.scala'));
        };
      } else {
        this.filterNode =  (node) => {
          const name = node.label;
          const tabSuffix = name.substr(name.lastIndexOf('.'), name.length);
          return !node.isLeaf || (node.isLeaf && tabSuffix === '.jar');
        };
      }
    },

    init() {
      let { name, shared, description, path, udfName, directory, udfType, registerFormat, load, useFormat } = this.node;
      let fnType = 'Spark'
      if (udfType === 0) {
        fnType = this.$t('message.linkis.udf.common');
      } else if (udfType > 2) {
        fnType = this.$t('message.linkis.udf.ZDYHS');
      }
      if (registerFormat) {
        this.convertRegisterFormat();
      }
      if (useFormat) {
        this.convertUseFormat(name || udfName);
      }
      this.setting = Object.assign({}, this.setting, {
        name: name || udfName,
        shared,
        description,
        directory,
        fnType,
        defaultLoad: !!load
      });
      this.$nextTick(() => {
        this.$set(this.setting, this.getTypes(), path);
      });
    },

    close() {
      this.show = false;
    },

    convertRegisterFormat() {
      const rf = this.node.registerFormat;
      const conver = (f, s, left, right) => {
        return trim(rf.substring(rf[left](f) + 1, rf[right](s)));
      };
      if (this.node.udfType === 0) {
        this.setting.jarPara = conver('"', '"', 'indexOf', 'lastIndexOf');
      } else if (this.node.udfType === 1) {
        this.setting.pyPara = conver(',', ')', 'indexOf', 'lastIndexOf');
      } else {
        const type = rf.slice(rf.indexOf('[') + 1, rf.indexOf(']'));
        window.console.log(type, rf, '=====');
        // 如果存在多个逗号，就只用使用格式来截取，否则会出现多个类型填入input异常的问题
        if (type.indexOf(',') !== type.lastIndexOf(',')) {
          // there are 2 case:
          // 1. tuple,  return params in ();
          // 2. multi params, the first params is return params
          if (type.indexOf('(') !== -1) {
            // tuple
            this.setting.scalaTypeL = type.slice(type.indexOf('('), type.indexOf(')') + 1)
            this.setting.scalaTypeR = type.slice(type.indexOf(')')+2)
          } else {
            // multi params
            this.setting.scalaTypeL = type.split(',')[0];
            this.setting.scalaTypeR = type.split(',').slice(1).toString();
          }

          this.showScalaRF = this.node.registerFormat;
        } else {
          this.setting.scalaTypeL = conver('[', ',', 'indexOf', 'indexOf');
          this.setting.scalaTypeR = conver(',', ']', 'indexOf', 'indexOf');
        }
        this.setting.scalapara = conver(',', ')', 'lastIndexOf', 'lastIndexOf');
      }
    },

    convertUseFormat(name) {
      const uf = this.node.useFormat;
      const index = uf.indexOf(name+'(');
      if (index > 0) {
        const left = trim(uf.slice(0, index - 1));
        const right = trim(uf.slice(index + name.length + 1, uf.lastIndexOf(')')));
        let type = 'useFormatPara';
        if (this.showScalaRF) {
          this.showScalaUF = this.node.useFormat;
          type = 'scalaType';
        }
        this.$set(this.setting, `${type}L`, left);
        this.$set(this.setting, `${type}R`, right);
      }
    },

    handleShareChange() {
      if (!this.isShareLoading) {
        this.isShareLoading = true;
      }
    },

    submitForm(formName) {
      if (this.fnType === 2) {
        if (!this.setting.scalaTypeL) return this.$Message.warning(this.$t('message.linkis.datasource.pleaseInput') + this.$t('message.linkis.udf.returnType'))
        if (!this.setting.scalaTypeR) return this.$Message.warning(this.$t('message.linkis.datasource.pleaseInput') + this.$t('message.linkis.udf.inputType'))
      } else {
        if (!this.setting.useFormatParaL) return this.$Message.warning(this.$t('message.linkis.datasource.pleaseInput') + this.$t('message.linkis.udf.returnType'))
        if (!this.setting.useFormatParaR) return this.$Message.warning(this.$t('message.linkis.datasource.pleaseInput') + this.$t('message.linkis.udf.inputType'))
      }
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let { name, description, defaultLoad, directory, clusterName} = this.setting;
          let postData = {
            name,
            description,
            path: this.fnPath,
            useFormat: this.useFormat,
            registerFormat: this.registerFormat,
            udfType: this.fnType,
            isLeaf: true,
            directory,
            clusterName
          };
          if (this.model) {
            postData = Object.assign(postData, { shared: false });
            this.$emit('update', postData);
          } else {
            postData = Object.assign(postData, { defaultLoad });
            this.$emit('add', postData);
          }
        }
        return false;
      });
    },

    firstUpperCase(str) {
      // 针对类型input框可能存在多个类型的转换，例如'string,string,int'
      return trim(str).replace(/\w+,*/g, (word) => {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
      });
    },

    resetRF() {
      this.showScalaRF = '';
      this.showScalaUF = '';
    },

    // 设置宽高
    resize(h) {
      this.modalHeight = h - 380 + 'px';
    },

    setNodePath(node) {
      ['jarPath', 'sparkPath', 'customPath'].forEach(item => {
        const temp = item === this.getTypes() ? node.path : '';
        this.$set(this.setting, item, temp);
      })
    },

    getTypes() {
      if (this.fnCategory.isCommon) {
        return 'jarPath';
      } else if (this.fnCategory.isSpark) {
        return 'sparkPath';
      }
      return 'customPath';
    },

    onTypeChange() {
      this.$emit('type-change', this.setting.fnType);
      if (this.setting.fnType === 'Spark') {
        this.filterNode = (node) => {
          const name = node.label;
          const tabSuffix = name.substr(name.lastIndexOf('.'), name.length);
          return !node.isLeaf || (node.isLeaf && (tabSuffix === '.py' || tabSuffix === '.scala'));
        };
      } else {
        this.filterNode =  (node) => {
          const name = node.label;
          const tabSuffix = name.substr(name.lastIndexOf('.'), name.length);
          return !node.isLeaf || (node.isLeaf && tabSuffix === '.jar');
        };
      }
    },

    resetData() {
      this.setting = {
        name: '',
        fnType: '',
        jarPath: '',
        sparkPath: '',
        customPath: '',
        useFormatParaL: '',
        useFormatParaR: '',
        jarPara: '',
        pyPara: '',
        scalaTypeL: '',
        scalaTypeR: '',
        scalapara: '',
        description: '',
        defaultLoad: true,
        directory: '',
        clusterName: 'all'
      };
      this.showScalaRF = '';
      this.showScalaUF = '';
    },

    getDir() {
      api.fetch('/udf/userDirectory', { category: 'udf' }, 'get').then(res => {
        this.directories = res.userDirectory || []
        this.remoteDirectories = res.userDirectory || []
      })
    },
    filterAdd(query) {
      let findIt = this.remoteDirectories.find(it=> it === query)
      return new Promise((resolve)=>{
        let options = []
        if (!findIt) {
          options = [query, ...this.remoteDirectories]
        } else {
          options = [...this.remoteDirectories]
        }
        this.directories = options
        resolve(options)
      })
    },
    queryChange(v) {
      if (!v) {
        this.$refs.directory.setQuery(null)
        this.directories = [...this.remoteDirectories]
      }
    },
    jarValidator(rule, val, cb) {
      if (!val) {
        cb(new Error(this.$t('message.linkis.udf.QSRWZLJ')));
      }
      const fileName = val.split('/')[val.split('/').length - 1]
      if (!/^[\w\u4e00-\u9fa5:.\\/]*(jar)$/.test(fileName)) {
        cb(new Error(this.$t('message.linkis.udf.HZMZC')));
      }
      cb();
    },
    pyValidator(rule, val, cb) {
      if (!val) {
        cb(new Error(this.$t('message.linkis.udf.QSRWZLJ')));
      }
      const fileName = val.split('/')[val.split('/').length - 1]
      if (!/^[\w\u4e00-\u9fa5:.\\/]*(py|scala)$/.test(fileName)) {
        cb(new Error(this.$t('message.linkis.udf.ZCPYSCA')));
      }
      cb();
    }
  },
};
</script>
<style lang="scss" src="./index.scss">

</style>
<style lang="scss">
.udf-modal {
  .full-screen-button {
    position: absolute;
    top: 14px;
    right: 35px;
    cursor: pointer;
    &:hover {
      color: #2d8cf0;
    }
  }

  .ivu-modal-close {
    top: 3px;
  }
}

.full-screen {
  .ivu-modal {
    width: 100% !important;
    height: 100% !important;
    top: 0 !important;

    .ivu-modal-body {
      height: calc(100% - 150px) !important;
      .ivu-form {
        height: 100% !important;
      }
    }

    .ivu-modal-content {
      height: 100% !important;
    }
    .fn-default-load {
      bottom: 70px !important;
    }
  }
}
</style>
