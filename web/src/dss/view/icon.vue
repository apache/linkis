<template>
  <div class="icon-content">
    <ul>
      <li v-for="item in list" :key="item" @click="copy(item)" class="icon-style">
        <SvgIcon :icon-class="item"/>
        <div>{{item}}</div>
      </li>
    </ul>
  </div>
</template>
<script>
const req = require.context('@/components/svgIcon/svg', true, /\.svg$/);
const reg = /.*\/(.*)\.svg/;
const list = req.keys().map(item => {
  reg.test(item);
  return RegExp.$1;
})
export default {
  data() {
    return {
      list,
    }
  },
  methods: {
    copy(data){
      let inputEl = document.createElement('input');
      inputEl.value = `<SvgIcon style="font-size: 1rem;" color="#444444" icon-class="${data}"/>`;
      document.body.appendChild(inputEl);
      inputEl.select(); // 选择对象;
      document.execCommand("Copy"); // 执行浏览器复制命令
      this.$Message.info('复制成功');
      inputEl.remove();
    },
  }
};
</script>
<style lang="scss" scoped>
  .icon-style {
    background: #9c9c9c;
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: center;
    margin: 5px;
    width: calc(10% - 10px);
    font-size: 2rem;
    color: #444444
  }
  .icon-style div {
    font-size: 0.8rem;
  }
</style>
