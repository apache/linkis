<template>
  <div>
    <ul class="we-progress-toolbar">
      <li
        @click="openPanel('log')">
        <Icon
          type="md-list-box"
          size="14"/>
        <span class="v-toolbar-icon">{{ $t('message.common.log') }}</span>
      <li
        @click="openPanel('diagnosis')"
        v-if="status === 'Running' && application ==='spark'">
        <Icon
          type="md-barcode"
          size="14"/>
        <span class="v-toolbar-icon">智能<br/>诊断</span>
      </li>
    </ul>
  </div>
</template>
<script>
export default {
  props: {
    status: String,
    application: String,
  },
  data() {
    return {
    };
  },
  methods: {
    openPanel(type) {
      this.$emit('open-panel', type);
    },
  },
};
</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
  .we-progress-toolbar {
    width: 40px;
    height: 100%;
    position: absolute;
    margin-left: -40px;
    padding-top: 10px;
    background: #fff;
    border-right: 1px solid #dcdee2;
    li {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 6px 2px;
      cursor: pointer;
      &:hover {
        color: $primary-color;
      }
    }
  }
</style>

