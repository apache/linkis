import { plugin } from './plugin';
import * as Plugin_0 from 'C:/Users/chandlermei/Desktop/letgo-code (1)/src/app.jsx';
import * as Plugin_1 from '@@/core/routes/runtime.js';

function handleDefaultExport(pluginExports) {
  // 避免编译警告
  const defaultKey = 'default';
  if (pluginExports[defaultKey]) {
    const {default: defaultExport, ...otherExports} = pluginExports;
    return {
      ...defaultExport,
      ...otherExports
    }
  }
  return pluginExports;
}

  plugin.register({
    apply: handleDefaultExport(Plugin_0),
    path: 'C:/Users/chandlermei/Desktop/letgo-code (1)/src/app.jsx',
  });
  plugin.register({
    apply: handleDefaultExport(Plugin_1),
    path: '@@/core/routes/runtime.js',
  });
