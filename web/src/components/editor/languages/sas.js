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

import hql from './hql.js';
import sqlFormatter from '../sqlFormatter/sqlFormatter';

const sasDefinition = {
  keywords: [
    '%DO',
    '%UNTIL',
    '%TO',
    '%WHILE',
    '%END',
    '%GLOBAL',
    '%GOTO',
    '%IF',
    '%THEN',
    '%ELSE',
    '%LABEL',
    '%LET',
    '%LOCAL',
    '%MACRO',
    '%MEND',
  ],
};

export default {
  register(monaco) {
    // 继承和合并hql相关的keyword
    const langDefinition = hql.definition;
    langDefinition.keywords.concat(sasDefinition);
    monaco.languages.register({ id: 'sas' });
    monaco.languages.setLanguageConfiguration('sas', hql.config);
    monaco.languages.setMonarchTokensProvider('sas', langDefinition);

    // 处理格式化
    monaco.languages.registerDocumentFormattingEditProvider('sas', {
      provideDocumentFormattingEdits: function(model) {
        let range = model.getFullModelRange();
        let value = model.getValue();
        let newValue = sqlFormatter.format(value);
        return [
          {
            range: range,
            text: newValue,
          },
        ];
      },
    });
  },
};
