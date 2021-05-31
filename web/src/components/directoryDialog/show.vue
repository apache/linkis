<template>
  <Modal
    v-model="show"
    class="we-import-dialog"
    width="360">
    <div slot="header">
      <span>{{ title }}</span>
    </div>
    <div class="we-import-dialog-content">
      <span>{{$t('message.common.path')}}</span>
      <directory-dialog
        :tree="tree"
        :load-data-fn="loadDataFn"
        :filter-node="filterNode"
        :path="path"
        @set-node="setNode"/>
    </div>
    <div slot="footer">
      <Button
        type="primary"
        @click="submit">{{$t('message.common.ok')}}</Button>
    </div>
  </Modal>
</template>
<script>
import directoryDialog from './index.vue';
export default {
  name: 'ShowDialog',
  components: {
    directoryDialog,
  },
  props: {
    filterNode: Function,
    loadDataFn: Function,
    path: {
      type: String,
      require: true,
    },
    fsType: {
      type: String,
      default: 'file',
    },
    tree: {
      type: Array,
      require: true,
      default: () => [],
    },
    title: String,
  },
  data() {
    return {
      show: false,
      node: null,
    };
  },
  methods: {
    open() {
      this.show = true;
    },
    setNode(node) {
      this.node = node;
      this.$emit('set-node', node, this.fsType);
    },
    submit() {
      this.show = false;
      this.$emit('import', this.node);
    },
  },
};
</script>
