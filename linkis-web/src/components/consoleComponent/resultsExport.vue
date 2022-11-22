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
    class="results-export"
    width="450">
    <div slot="header">
      <span>{{ $t('message.common.resultsExport.header') }}</span>
    </div>
    <div class="results-export-content">
      <Form
        ref="resultsExport"
        :model="exportOption"
        :rules="rules"
        :label-width="80">
        <FormItem
          :label="$t('message.common.resultsExport.formItems.name.label')"
          prop="name">
          <Input
            v-model="exportOption.name"
            :placeholder="$t('message.common.resultsExport.formItems.name.placeholder')"
            style="width: 320px;"></Input>
          <Icon
            :title="$t('message.common.resultsExport.formItems.name.title')"
            type="md-help-circle"
            class="results-export-content-help"
          ></Icon>
        </FormItem>
        <FormItem
          :label="$t('message.common.toolbar.exportFormat')"
          prop="format">
          <RadioGroup v-model="exportOption.format">
            <Radio label="1">CSV</Radio>
            <Radio label="2">Excel</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem
          :label="$t('message.common.resultsExport.formItems.path.label')"
          prop="path">
          <directory-dialog
            :filter-node="filterNode"
            :path="exportOption.path"
            :tree="fileTree"
            :load-data-fn="loadDataFn"
            fs-type="script"
            :title="$t('message.common.resultsExport.formItems.path.title')"
            @set-node="setNode"/>
        </FormItem>
        <FormItem v-if="script.resultList && script.resultList.length>1 && exportOption.format === '2'">
          <Checkbox v-model="isAll" :disabled="!allDisbled">{{ $t('message.common.resultsExport.formItems.path.isSheet') }}</Checkbox>
        </FormItem>
      </Form>
    </div>
    <div slot="footer">
      <Button
        v-show="show"
        type="primary"
        @click.stop="submit">{{ $t('message.common.ok') }}</Button>
    </div>
  </Modal>
</template>
<script>
import { isEmpty, cloneDeep, throttle} from 'lodash';
import util from '@/common/util';
import directoryDialog from '@/components/directoryDialog/index.vue';
export default {
  name: 'ResultsExport',
  components: {
    directoryDialog,
  },
  props: {
    currentPath: {
      type: String,
      default: '',
    },
    script: [Object],
    dispatch: {
      type: Function,
      required: true
    }
  },
  data() {
    return {
      isAll: false,
      show: false,
      exportOption: {
        name: '',
        path: '',
        format: '1'
      },
      fileTree: [],
      hdfsComponent: null,
      loadDataFn: () => {},
      rules: {
        name: [
          { required: true, message: this.$t('message.common.resultsExport.rules.name.required'), trigger: 'blur' },
          { min: 1, max: 200, message: this.$t('message.common.resultsExport.rules.name.lengthLimit'), trigger: 'change' },
          { type: 'string', pattern: /^[\w\u4e00-\u9fa5]+$/, message: this.$t('message.common.resultsExport.rules.name.letterTypeLimit'), trigger: 'change' },
        ],
        path: [
          { required: true, message: this.$t('message.common.resultsExport.rules.path.required'), trigger: 'change' },
        ],
      },
    };
  },
  computed: {
    allDisbled() {
      return ['hql', 'sql', 'py'].includes(this.script.runType);
    }
  },
  methods: {
    open() {
      this.show = true;
      this.reset();
      const fileName = this.script.fileName.replace(/\./g, '_');
      this.exportOption.name = `${fileName}__${Date.now()}`;
      this.setFileTree();
    },
    setFileTree() {
      if (isEmpty(this.fileTree)) {
        // Fetch indexedDB cache(取indexedDB缓存)
        this.dispatch('IndexedDB:getTree', {
          name: 'scriptTree',
          cb: (res) => {
            if (!res || (res && res.value.length <= 0)) {
              this.dispatch('WorkSidebar:showTree', (f) => {
                this.hdfsComponent = f;
                f.getRootPath(() => {
                  f.getTree((tree) => {
                    if (tree) {
                      this.fileTree.push(tree);
                      this.loadDataFn = f.loadDataFn;
                    }
                  });
                });
              });
            } else {
              this.fileTree = cloneDeep(res.value);
              if (this.hdfsComponent) {
                this.loadDataFn = this.hdfsComponent.loadDataFn;
              } else {
                this.dispatch('WorkSidebar:showTree', (f) => {
                  this.hdfsComponent = f;
                  this.loadDataFn = f.loadDataFn;
                });
              }
            }
          }
        })
      }
    },
    filterNode(node) {
      return !node.isLeaf;
    },
    setNode(node) {
      this.exportOption.path = node.path;
    },
    submit() {
      this.$refs.resultsExport.validate((valid) => {
        if (!valid) return false;
        this.show = false;
        throttle(this.exportConfirm(), 500);
      })
    },
    exportConfirm() {
      const tabName = `new_stor_${Date.now()}.out`;
      // Whether to export all the result set is added. When export all is selected, the source path does not have a suffix file.(导出结果集增加是否导出全部，当选择导出全部时，来源路径不带后缀文件)
      let temPath = this.currentPath;
      if (this.isAll) {
        temPath = temPath.substring(0, temPath.lastIndexOf('/'));
      }
      const exportOptionName = this.exportOption.format === '2' ?  `${this.exportOption.name}.xlsx`: `${this.exportOption.name}.csv`
      const code = `from ${temPath} to ${this.exportOption.path}/${exportOptionName}`;
      const md5Path = util.md5(tabName);
      this.dispatch('Workbench:add', {
        id: md5Path,
        filename: tabName,
        filepath: '',
        // saveAs represents a temporary script that needs to be closed or saved when saved(saveAs表示临时脚本，需要关闭或保存时另存)
        saveAs: true,
        noLoadCache: true,
        code,
      }, (f) => {
        if (!f) {
          return;
        }
        this.$nextTick(() => {
          this.dispatch('Workbench:run', { id: md5Path });
        });
      });
    },
    reset() {
      this.exportOption = {
        name: '',
        path: '',
        format: '1',
      };
      this.fileTree = [];
    },
  },
};
</script>
<style lang="scss" scoped>
  .results-export {
    .results-export-content-help {
        cursor: pointer;
        font-size: 14px;
    }
}
</style>
