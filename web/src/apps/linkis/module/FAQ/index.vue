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
      util.windowOpen(this.getFAQUrl())
    },
  },
};
</script>
