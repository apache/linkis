<template>
  <iframe :src="target" frameborder="0" height="95%" width="100%"></iframe>
</template>
<script>
export default {
  name: 'pythonModule',
  data() {
    return {
      target: `./dist/#/index`
    }
  }
}
</script>
