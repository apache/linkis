<template>
  <Modal
    v-model="show"
    width="360"
    class-name="delete-modal">
    <p
      slot="header"
      class="delete-modal-header">
      <Icon type="ios-information-circle"/>
      <span>{{$t('message.common.deleteDialog.waring')}}</span>
    </p>
    <div class="delete-modal-content">
      <p>
        <span>{{ label }}</span>
        <span class="delete-modal-content-type">{{ type }}</span>
        <span class="delete-modal-content-name">{{ name }}</span>
      </p>
      <p>{{$t('message.common.deleteDialog.isNext')}}</p>
    </div>
    <div slot="footer">
      <Button
        :loading="loading"
        type="error"
        size="large"
        long
        @click="del">{{ [$t('message.common.deleteDialog.engine'), $t('message.common.deleteDialog.task'), $t('message.common.deleteDialog.engineAndTask')].indexOf(type) !== -1 ? $t('message.common.deleteDialog.action', {type}) : $t('message.common.delete') }}</Button>
    </div>
  </Modal>
</template>
<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: false,
      type: '',
      name: '',
    };
  },
  computed: {
    label() {
      return [this.$t('message.common.deleteDialog.engine'), this.$t('message.common.deleteDialog.task'), this.$t('message.common.deleteDialog.engineAndTask')].indexOf(this.type) !== -1 ? this.$t('message.common.deleteDialog.overThe') : this.$t('message.common.delete');
    },
  },
  watch: {
    'loading': function(val) {
      if (!val) {
        this.show = false;
      }
    },
  },
  methods: {
    open(opt) {
      this.show = true;
      let { type, name } = opt;
      this.type = type;
      this.name = name;
    },
    close() {
      this.show = false;
    },
    del() {
      this.$emit('delete', this.type);
    },
  },
};
</script>
<style lang="scss" src="./index.scss">
</style>
