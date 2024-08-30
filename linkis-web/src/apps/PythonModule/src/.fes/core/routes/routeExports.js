import { createApp } from 'vue';
import { createRouter as createVueRouter, createWebHashHistory, ApplyPluginsType } from 'C:/Users/chandlermei/Desktop/letgo-code (1)/node_modules/@fesjs/runtime';
import { plugin } from '../plugin';
import { updateInitialState } from '../../initialState';

const ROUTER_BASE = '';
let router = null;
let history = null;
export const createRouter = (routes) => {
  const createHistory = plugin.applyPlugins({
    key: 'modifyCreateHistory',
    type: ApplyPluginsType.modify,
    args: {
      base: ROUTER_BASE
    },
    initialValue: createWebHashHistory,
  });
  // 修改routes
  plugin.applyPlugins({
    key: 'patchRoutes',
    type: ApplyPluginsType.event,
    args: { routes },
  });
  const route = plugin.applyPlugins({
    key: 'modifyRoute',
    type: ApplyPluginsType.modify,
    initialValue: {
      base: ROUTER_BASE,
      routes: routes,
      createHistory: createHistory
    },
  });
  
  history = route['createHistory']?.(route.base);
  router = createVueRouter({
    history,
    routes: route.routes
  });

  let isInit = false
  router.beforeEach(async (to, from, next) => {
    if(isInit){
      return next()
    }
    isInit = true
    const beforeRenderConfig = plugin.applyPlugins({
      key: "beforeRender",
      type: ApplyPluginsType.modify,
      initialValue: {
          loading: null,
          action: null
      },
    });
    if (typeof beforeRenderConfig.action !== "function") {
      return next();
    }
    const rootElement = document.createElement('div');
    document.body.appendChild(rootElement)
    const app = createApp(beforeRenderConfig.loading);
    app.mount(rootElement);
    try {
        const initialState = await beforeRenderConfig.action({router, history});
        updateInitialState(initialState || {})
        next();
    } catch(e){
        next(false);
        window.console.error(`[fes] beforeRender执行出现异常：`);
        window.console.error(e);
    }
    app.unmount();
    app._container.innerHTML = '';
    document.body.removeChild(rootElement);
  })

  plugin.applyPlugins({
    key: 'onRouterCreated',
    type: ApplyPluginsType.event,
    args: { router, history },
  });

  return router;
};

export const getRouter = ()=>{
    if(!router){
        window.console.warn(`[preset-build-in] router is null`)
    }
    return router;
}

export const getHistory = ()=>{
    if(!history){
        window.console.warn(`[preset-build-in] history is null`)
    }
    return history;
}

export const destroyRouter = ()=>{
    router = null;
    history = null;
}

export const defineRouteMeta = (param)=>{
    return param
}
