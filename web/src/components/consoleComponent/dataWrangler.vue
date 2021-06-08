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
