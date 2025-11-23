/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*eslint-disable */
  /*
  * Determine whether the current independent application and environment list(判断当前是否独立应用和环境列表)
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
