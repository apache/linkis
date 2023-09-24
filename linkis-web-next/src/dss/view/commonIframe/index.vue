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
    <div class="iframeClass">
        <iframe class="iframeClass" v-if="isRefresh" id="iframe" :src="visualSrc" frameborder="0" width="100%"
            :height="height" />
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue';

// import util from '@/common/util/index';
// import api from "@/common/service/api";

export default defineComponent({
    setup() {
        return {
            height: 0,
            visualSrc: '',
            isRefresh: true,
        };
    },
    watch: {
        '$route.query.projectID': async function () {
            await this.getCommonProjectId(
                this.$route.query.type,
                this.$route.query,
            );
            this.getUrl();
            this.reload();
        },
        '$route.query.type': async function () {
            await this.getCommonProjectId(
                this.$route.query.type,
                this.$route.query,
            );
            this.getUrl();
            this.reload();
        },
    },
    mounted() {
        this.getUrl();
        // Set width and height when creating(创建的时候设置宽高)
        this.resize(window.innerHeight);
        // Monitor window changes and get browser width and height(监听窗口变化，获取浏览器宽高)
        window.addEventListener('resize', () => {
            this.resize(window.innerHeight);
        });
    },
    beforeUnmount() {
        window.removeEventListener('resize', () => {
            this.resize(window.innerHeight);
        });
    },
    methods: {
        getCommonProjectId() {
            // return api.fetch(`/dss/getAppjointProjectIDByApplicationName`, {
            //   projectID: query.projectID,
            //   applicationName: type
            // }, 'get').then((res) => {
            //   localStorage.setItem('appJointProjectId', res.appJointProjectID);
            // })
        },
        resize(height: number) {
            this.height = height;
        },
        getUrl() {
            // const { url } = this.$route.query;
            // const appJointProjectId = localStorage.getItem('appJointProjectId');
            // this.visualSrc = util.replaceHolder(url, {
            //   projectId: appJointProjectId,
            //   projectName: this.$route.query.projectName,
            // });
        },
        reload() {
            this.isRefresh = false;
            this.$nextTick(() => (this.isRefresh = true));
        },
    },
});
</script>
<style>
.iframeClass {
    height: 100%;
}
</style>
