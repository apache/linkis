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
  <div style="height:100%">
    <Input
      v-model="searchText"
      :placeholder="$t('message.common.filter')"
      suffix="ios-search"
      size="small"
      class="log-search"/>
    <we-editor
      style="height: calc(100% - 60px);"
      ref="logEditor"
      v-model="curCode"
      type="log"/>
  </div>
</template>
<script>
import api from '@/common/service/api'
import { filter, forEach } from 'lodash'
export default {
  name: 'codeDetail',
  data() {
    return {
      code: '',
      searchText: '',
    }
  },
  async mounted() {
    const id = this.$route.query.id
    const res = await api.fetch(`/jobhistory/es/task/${id}`, 'get');
    this.code = res.task.executionCode
  },
  computed: {
    curCode: {
      get() {
        let MatchText = '';
        let originCode = this.code;
        const val = this.searchText;
        if (!originCode) return MatchText;
        if (val) {
          // 这部分代码是为了让正则表达式不报错，所以在特殊符号前面加上\
          let formatedVal = '';
          forEach(val, (o) => {
            if (/^[\w\u4e00-\u9fa5]$/.test(o)) {
              formatedVal += o;
            } else {
              formatedVal += `\\${o}`;
            }
          });
          // 全局和不区分大小写模式，正则是匹配searchText前后出了换行符之外的字符
          let regexp = new RegExp(`.*${formatedVal}.*`);
          MatchText = filter(originCode.split('\n'), (item) => {
            return regexp.test(item);
          }).join('\n');
          regexp = null;
        } else {
          MatchText = originCode;
        }
        return MatchText;
      },
      set() {

      }
    }
  }
}
</script>
