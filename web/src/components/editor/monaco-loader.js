/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import hql from './languages/hql';
import log from './languages/log';
import sas from './languages/sas';
import sh from './languages/sh';
import out from './languages/out';
import defaultView from './theme/defaultView';
import logview from './theme/logView';
import hqlKeyword from './keyword/hql';
import pythonKeyword from './keyword/python';
import sasKeyword from './keyword/sas';
import shKeyword from './keyword/sh';

import * as monaco from 'monaco-editor';

const languagesList = monaco.languages.getLanguages();
const findLang = find(languagesList, (lang) => {
  return lang.id === 'hql';
});
if (!findLang) {
  // 注册languages
  hql.register(monaco);
  log.register(monaco);
  sas.register(monaco);
  sh.register(monaco);
  out.register(monaco);
  // 注册theme
  defaultView.register(monaco);
  logview.register(monaco);

  // 注册关键字联想
  hqlKeyword.register(monaco);
  pythonKeyword.register(monaco);
  sasKeyword.register(monaco);
  shKeyword.register(monaco);
}

export default monaco;
