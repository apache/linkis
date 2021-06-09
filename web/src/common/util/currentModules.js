/*eslint-disable */
  /*
  * 判断当前是否独立应用和环境列表
  * @return {Object}
  */
const apps = require('dynamic-modules')
export function currentModules () {
  let obj = {
    linkisOnly: false,
    scriptisOnly: false,
    hasScriptis: false,
    hasLinkis: false,
    microModule: false
  }
  if (apps.microModule) {
    obj.microModule = apps.microModule
  }
  if (apps.modules) {
    if (apps.modules.includes('linkis')) {
      obj.hasLinkis = true;
      obj.linkisOnly = apps.modules.length === 1
    }
    if (apps.modules.includes('scriptis')) {
      obj.hasScriptis = true;
      obj.scriptisOnly = apps.modules.length === 1;
    }
  }
  return obj;
}