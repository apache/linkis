<template>
  <iframe :src="target" frameborder="0" height="95%" width="100%"></iframe>
</template>
<script>
export default {
  name: 'pythonModule',
  data() {
    return {
      target: `./dist/?nocache=${new Date().getTime()}/#/index`,
      // target: `http://10.107.97.166:8088?nocache=${new Date().getTime()}/#/index`
    }
  }
}
</script>
