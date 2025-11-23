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
  <div class="global-valiable">
    <Row class="global-valiable-control">
      <Button
        type="primary"
        @click="handleSubmit('formDynamic')"
        :loading="loading">{{ !isEdit ? $t('message.linkis.edit') : $t('message.linkis.save') }}</Button>
      <Button
        @click="handleCancel">{{ $t('message.linkis.cancel') }}</Button>
    </Row>
    <Form
      ref="formDynamic"
      :model="formDynamic"
      :label-width="135"
      class="global-valiable-form"
    >
      <FormItem
        v-for="(item, index) in formDynamic.items"
        :key="index"
        :label="$t('message.linkis.globalValiable') + (index + 1) + '：'">
        <Row>
          <Col span="10">
            <FormItem
              :prop="'items.' + index + '.key'"
              :rules="[
                {required: true,message: $t('message.linkis.rules.first.required', {text: index+1}),trigger: 'blur'},
                {pattern: /^[a-zA-z][^\s\u4e00-\u9fa5]*$/, message: `${$t('message.linkis.rules.first.letterTypeLimit')}`},
                {validator:validateKey,trigger: 'blur'},
              ]">
              <Input
                v-model="item.key"
                type="text"
                :maxlength="50"
                :placeholder="$t('message.linkis.rules.first.placeholder')"
                :disabled="!isEdit"/>
            </FormItem>
          </Col>
          <Col
            span="1"
            class="global-valiable-tc">
            ：
          </Col>
          <Col span="10">
            <FormItem
              :prop="'items.' + index + '.value'"
              :rules="[{required: true, message: $t('message.linkis.rules.second.required', {text: index+1}), trigger: 'blur'}]">
              <Input
                v-model="item.value"
                type="text"
                :maxlength="64"
                :placeholder="$t('message.linkis.rules.second.placeholder')"
                :disabled="!isEdit" />
            </FormItem>
          </Col>
          <Col
            span="3"
            class="global-valiable-remove">
            <Button
              type="error"
              size="small"
              @click="handleRemove(item, index)"
              v-if="isEdit">{{ $t('message.linkis.remove') }}</Button>
          </Col>
        </Row>
      </FormItem>
    </Form>
    <Row class="global-valiable-add">
      <Col span="12">
        <Button
          type="dashed"
          long
          icon="md-add"
          @click="handleAdd"
          v-if="isEdit">{{ $t('message.linkis.addArgs') }}</Button>
      </Col>
    </Row>
    <div
      class="global-valiable-empty"
      v-if="!formDynamic.items.length">{{ $t('message.linkis.emptyDataText') }}</div>
    <detele-modal
      ref="killModal"
      :loading="loading"
      @delete="handleRemove"></detele-modal>
  </div>
</template>
<script>
import api from '@/common/service/api';
import storage from '@/common/helper/storage';
import mixin from '@/common/service/mixin';
import deteleModal from '@/components/deleteDialog';
export default {
  components: {
    deteleModal,
  },
  mixins: [mixin],
  data() {
    return {
      current: null,
      currentIndex: 0,
      formDynamic: {
        items: [],
      },
      loading: false,
      isEdit: false,
      validateKey: (rule, value, callback) => {
        const prop = rule.field;
        // Get the index of the currently edited item(拿到当前编辑的item的index)
        const current = prop.slice(prop.indexOf('.') + 1, prop.lastIndexOf('.'));
        // It must be repeated when the keys are equal and the indexes are not equal(必须当key相等，而且index不等的时候才是repeat)
        const findRepeat = this.formDynamic.items.find((item, index) => {
          return item.key === value && current != index;
        });
        if (findRepeat) {
          callback(new Error(this.$t('message.linkis.sameName')));
        }
        callback();
      },
    };
  },
  mounted() {
    this.getGlobalValiableList();
  },
  methods: {
    getGlobalValiableList(update = false) {
      api.fetch('/variable/listGlobalVariable', 'get').then((res) => {
        this.formDynamic.items = res.globalVariables.map((item) => {
          return Object.assign(item);
        });
        if (update) {
          this.dispatch('IndexedDB:updateGlobalCache', {
            id: this.getUserName(),
            variableList: res.globalVariables,
          });
        }
      });
    },
    handleSubmit(name) {
      if (this.isEdit) {
        if (this.formDynamic.items.length) {
          this.$refs[name].validate((valid) => {
            if (valid) {
              this.save();
            } else {
              this.$Message.error(this.$t('message.linkis.error.validate'));
            }
          });
        } else {
          this.save();
        }
      } else {
        this.isEdit = true;
      }
    },
    save() {
      this.loading = true;
      api.fetch('/variable/saveGlobalVariable ', {
        globalVariables: this.formDynamic.items,
      }).then(() => {
        this.loading = false;
        this.isEdit = false;
        this.$Message.success(this.$t('message.linkis.success.update'));
        this.getGlobalValiableList(true);
        storage.set('need-refresh-proposals-hql', true);
      }).catch(() => {
        this.loading = false;
      });
    },
    handleAdd() {
      this.formDynamic.items.push({
        key: '',
        value: '',
      });
    },
    handleRemove(item, index) {
      this.formDynamic.items.splice(index, 1);
    },
    handleCancel() {
      this.isEdit = false;
    },
  },
};
</script>
<style src="./index.scss" lang="scss"></style>
