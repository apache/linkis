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
  <div class="visual-analysis" v-if="project">
    <iframe id="iframe" :src="visualSrc" frameborder="0"></iframe>
    <Modal
      v-model="project.show"
      :title="$t('message.common.saveToWidget')"
      @on-ok="confirm"
      @on-cancel="cancel"
    >
      <Select v-model="project.id">
        <Option
          v-for="item in apps"
          :value="item.id"
          :key="item.id"
          style="width: 200px;"
        >{{ item.name }}</Option>
      </Select>
    </Modal>
  </div>
</template>
<script>
import api from "@/common/service/api";
import mixin from '@/common/service/mixin';
export default {
  props: {
    visualParams: {
      type: Object,
      required: true
    }
  },
  mixins: [mixin],
  data() {
    let { projectId } = this.visualParams;
    return {
      project: {
        show: false,
        id: projectId,
        data: {}
      },
      projectid: null,
      apps: []
    };
  },
  computed: {
    visualSrc() {
      let { json } = this.visualParams;
      const dwraisUrl = this.getVsBiUrl();
      const srcPre = `${dwraisUrl}/dss/visualis/#/project/${this.projectid}/widget`;
      let viewJson = {
        ...json,
        params: {}
      };
      viewJson = JSON.stringify(viewJson);
      if (this.projectid) {
        return json ? `${srcPre}/add?view=${viewJson}` : "";
      }else{
        return ''
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener("message", this.fn, false);
  },
  created() {
    api.fetch("/visualis/project/default", "get").then(res => {
      this.projectid = res.project.id;
    });
  },
  mounted() {
    this.getproject();
    this.hiddenArrow();
    this.fn = ev => {
      if (typeof ev.data === "string") {
        try {
          let data = JSON.parse(ev.data);
          if (data.type === "saveWidget") {
            this.queryApplication();
            this.project.show = true;
            delete data.type;
            this.project.data = data;
          }
        } catch (error) {
          window.console.error(error);
        }
      }
    };
    window.addEventListener("message", this.fn, false);
  },
  methods: {
    getproject() {},
    async queryApplication() {
      let data = await api.fetch("/application/list");
      this.apps = data.applications;
    },
    saveWidgetToDws(data) {
      data = Object.assign(data, {
        projectId: this.project.id
      });
      const vsBiUrl = this.getVsBiUrl();
      api.fetch(`${vsBiUrl}/api/rest_j/v1/visualis/widgets`, data).then(() => {
        this.$Message.success(this.$t("message.common.saveSuccess"));
      });
    },
    confirm() {
      this.saveWidgetToDws(this.project.data);
    },
    cancel() {},
    hiddenArrow() {
      let iframe = document.getElementById("iframe");
      let iwindow = iframe.contentWindow;
      iwindow.onload = function() {
        let dom = iwindow.document.querySelector(".anticon.anticon-left");
        dom.style.display = "none";
      };
    }
  }
};
</script>
<style lang="scss" scoped>
.visual-analysis {
  overflow-y: scroll;
  iframe {
    width: 100%;
    height: 100%;
    min-height: 400px;
  }
}
</style>
