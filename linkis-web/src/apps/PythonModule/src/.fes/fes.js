
import {
    createApp,
} from 'vue';
import { plugin } from './core/plugin';
import './core/pluginRegister';
import { ApplyPluginsType } from 'C:/Users/chandlermei/Desktop/letgo-code (1)/node_modules/@fesjs/runtime';
import { getRoutes } from './core/routes/routes';
import DefaultContainer from './defaultContainer.vue';



import '../global.less';

const renderClient = (opts = {}) => {
    const { plugin, routes, rootElement } = opts;
    const rootContainer = plugin.applyPlugins({
        type: ApplyPluginsType.modify,
        key: 'rootContainer',
        initialValue: DefaultContainer,
        args: {
            routes: routes,
            plugin: plugin
        }
    });

    const app = createApp(rootContainer);

    plugin.applyPlugins({
        key: 'onAppCreated',
        type: ApplyPluginsType.event,
        args: { app, routes },
    });

    if (rootElement) {
        app.mount(rootElement);
    }
    return app;
}

const getClientRender = (args = {}) => plugin.applyPlugins({
  key: 'render',
  type: ApplyPluginsType.compose,
  initialValue: () => {
    const opts = plugin.applyPlugins({
      key: 'modifyClientRenderOpts',
      type: ApplyPluginsType.modify,
      initialValue: {
        routes: args.routes || getRoutes(),
        plugin,
        rootElement: '#app',
        defaultTitle: `fes.js`,
      },
    });
    return renderClient(opts);
  },
  args,
});

const clientRender = getClientRender();

const app = clientRender();



