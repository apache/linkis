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
  <div>
    <iframe
      id="iframeName"
      :src="visualSrc"
      frameborder="0"
      width="100%"
      :height="height"
      v-if="isSkip"/>
    <div
      style="display: flex; justify-content: center; align-items: center;"
      :style="{'height': height + 'px'}"
      v-else>
      <span>{{ info }}</span>
    </div>
  </div>
</template>
<script>
import module from './index';
import mixin from '@/common/service/mixin';
import util from '@/common/util';
export default {
  mixins: [mixin],
  data() {
    return {
      visualSrc: null,
      isSkip: false,
      info: this.$t('message.linkis.jumpPage'),
      height: 0
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      if (this.$route.query.isSkip) {
        const errCode = this.$route.query.errCode;
        const addr = module.data.ENVIR === 'dev' ? 'test.com' : window.location.host;
        this.visualSrc = `http://${addr}/dss/help/errorcode/${errCode}.html`;
        this.isSkip = true;
      } else {
        this.isSkip = false;
        this.info = this.$t('message.linkis.jumpPage');
        this.linkTo();
      }
      this.height = this.$parent.$el.clientHeight - 168
    },
    linkTo() {
      const faqUrl="https://linkis.apache.org/zh-CN/faq/main/"
      util.windowOpen(faqUrl)
    },
  },
};
</script>
