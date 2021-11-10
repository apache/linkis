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
  <div class="visual-analysis" v-if="visualSrc">
    <iframe id="iframe" :src="visualSrc" frameborder="0"></iframe>
  </div>
</template>
<script>
import mixin from '@/common/service/mixin';
export default {
  props: {
    dataWranglerParams: {
      type: Object,
      required: true
    }
  },
  mixins: [mixin],
  data() {
    return {}
  },
  computed: {
    visualSrc() {
      if (this.dataWranglerParams) {
        let params = '';
        Object.keys(this.dataWranglerParams).map((key) => {
          params += `${key}=${JSON.stringify(this.dataWranglerParams[key])}&`
        })
        params += 'noGoingBack=true';
        const dwraisUrl = this.getVsBiUrl('datawrangler');
        const srcPre = `${dwraisUrl}/#/sheet/add?${params}`;
        return srcPre;
      } else {
        return ''
      }
    }
  },
};
</script>
<style lang="scss" scoped>
.visual-analysis {
  height: 100%;
  iframe {
    width: 100%;
    height: -webkit-fill-available;
  }
}
</style>
