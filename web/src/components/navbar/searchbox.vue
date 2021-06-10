<template>
  <div class="we-search-wrap">
    <input
      ref="searchbox"
      :value="currentValue"
      :placeholder="$t('message.common.tableRow.search')"
      type="text"
      @input="$emit('input', $event.target.value)"
      @blur="$emit('on-blur', $event.target.value)" >
  </div>
</template>
<script>

export default {
  name: 'WeSearchBox',
  props: {
    value: String,
  },
  data() {
    return {
      currentValue: this.value,
    };
  },
  watch: {
    value(val) {
      this.currentValue = val;
    },
  },
  methods: {
    onfocus() {
      this.$refs.searchbox.focus();
    },
  },
};
</script>
