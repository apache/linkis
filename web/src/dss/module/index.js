import mixinDispatch from '@/common/service/moduleMixin';

const requireComponent = require.context(
  // 其组件目录的相对路径
  './',
  // 是否查询其子目录
  true,
  /([a-z|A-Z])+\/index\.js$/
);

const requireComponentVue = require.context(
  // 其组件目录的相对路径
  './',
  // 是否查询其子目录
  true,
  /([a-z|A-Z])+.vue$/
);

mixinDispatch(requireComponent, requireComponentVue)