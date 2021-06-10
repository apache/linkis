<template>
  <div class="iframeClass">
    <iframe
      class="iframeClass"
      v-if="isRefresh"
      id="iframe"
      :src="visualSrc"
      frameborder="0"
      width="100%"
      :height="height"/>
  </div>
</template>
<script>
import util from '@/common/util/index';
import api from "@/common/service/api";
export default {
  data() {
    return {
      height: 0,
      visualSrc: '',
      isRefresh: true
    };
  },
  watch: {
    async '$route.query.projectID'() {
      await this.getCommonProjectId(this.$route.query.type, this.$route.query);
      this.getUrl();
      this.reload()
    },
    async '$route.query.type'() {
      await this.getCommonProjectId(this.$route.query.type, this.$route.query);
      this.getUrl();
      this.reload()
    }
  },
  mounted() {
    this.getUrl();
    // 创建的时候设置宽高
    this.resize(window.innerHeight);
    // 监听窗口变化，获取浏览器宽高
    window.addEventListener('resize', this.resize(window.innerHeight));
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resize(window.innerHeight));
  },
  methods: {
    getCommonProjectId(type, query) {
      return api.fetch(`/dss/getAppjointProjectIDByApplicationName`, {
        projectID: query.projectID,
        applicationName: type
      }, 'get').then((res) => {
        localStorage.setItem('appJointProjectId', res.appJointProjectID);
      })
    },
    resize(height) {
      this.height = height;
    },
    getUrl() {
      const url = this.$route.query.url;
      const appJointProjectId = localStorage.getItem('appJointProjectId');
      this.visualSrc = util.replaceHolder(url, {
        projectId: appJointProjectId,
        projectName: this.$route.query.projectName,
      });
    },
    reload() {
      this.isRefresh = false;
      this.$nextTick(() => this.isRefresh = true);
    }
  },
};
</script>
<style>
.iframeClass{
    height: 100%;
}
</style>
