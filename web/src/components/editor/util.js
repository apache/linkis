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

import { partition, map, filter, isFunction } from 'lodash';
import globalcache from '@/apps/scriptis/service/db/globalcache.js';
import storage from '@/common/helper/storage';
import debug_log from '@/common/util/debug';

/**
 * 去indexDb中获取hive的列表和udf函数列表
 * @param {*} monaco
 * @param {*} lang
 */
const getHiveList = async (monaco, lang) => {
  const userInfoName = storage.get('baseInfo', 'local') ? storage.get('baseInfo', 'local').username : null;
  let dbInfoProposals = [];
  let tableInfoProposals = [];
  let udfProposals = [];
  let variableProposals = [];
  if (userInfoName) {
    const userName = userInfoName;
    const globalCache = await globalcache.getCache(userName);
    if (globalCache) {
      [dbInfoProposals, tableInfoProposals] = partition(map(globalCache.hiveList, (item) => ({
        caption: item.caption,
        label: item.value,
        kind: monaco.languages.CompletionItemKind.Unit,
        insertText: item.value,
        detail: item.meta,
        documentation: item.documentation,
      })), ['detail', 'dbname']);

      udfProposals = getFormatProposalsList(monaco, globalCache.fnList, lang, 'Function', true);
      variableProposals = getFormatProposalsList(monaco, globalCache.variableList, lang, 'Variable', true);
    }
  }
  return {
    dbInfoProposals,
    tableInfoProposals,
    udfProposals,
    variableProposals,
  };
}

/**
 *
 * @param {*} match 匹配到的文本
 * @param {*} proposals 需要过滤的列表
 * @param {*} fieldString 过滤条件中的字段名
 * @param {*} attachMatch 附加的过滤条件
 * @param {*} needSplit 是否需要对insertText进行截取
 * @return {*}
 */
const getReturnList = ({ match, proposals, fieldString, attachMatch, needSplit, position }, monaco) => {
  debug_log('log', 'getReturnList:', match, proposals, fieldString, attachMatch, needSplit)
  if (!match || isFunction(match)) {
    return;
  }
  let replacedStr = '';
  for (let i of match) {
    const reg = /[~'!@#￥$%^&*()-+_=:]/g;
    if (reg.test(i)) {
      replacedStr += `\\${i}`;
    } else {
      replacedStr += i;
    }
  }
  const regexp = new RegExp(`\w*${replacedStr}\w*`, 'i');
  let items = [];
  if (attachMatch && !needSplit) {
    items = filter(proposals, (it) => it[fieldString].startsWith(attachMatch) && regexp.test(it[fieldString]));
  } else if (attachMatch && needSplit) {
    // 这里是对例如create table和drop table的情况进行处理
    proposals.forEach((it) => {
      if (regexp.test(it[fieldString]) && it.label.indexOf(attachMatch[1]) === 0) {
        const text = it.insertText;
        items.push({
          label: it.label,
          documentation: it.documentation,
          insertText: text.slice(text.indexOf(' ') + 1, text.length - 1),
          detail: it.detail,
        });
      }
    });
  } else {
    items = filter(proposals, (it) => regexp.test(it[fieldString]));
  }
  debug_log('log', position)
  if (position && monaco) {
    items.forEach( it => it.range = new monaco.Range(position.lineNumber, position.column - match.length , position.lineNumber, position.column));
  }
  debug_log('log', 'suggestions', proposals.length, items , regexp);
  return {
    isIncomplete: true,
    suggestions: items,
  };
}

/**
 * 对拿到的数据格式化成completionList格式
 * @param {*} monaco 编辑器
 * @param {*} list 格式化列表
 * @param {*} lang 脚本类型
 * @param {*} type 类型（函数或者全局变量）
 * @param {*} isDiy 需要对输入进行定制化
 * @return {*} 格式化后的列表
 */
const getFormatProposalsList = (monaco, list, lang, type, isDiy, isSnippet) => {
  let formatList = [];
  if (!list) return formatList;
  const kind = monaco.languages.CompletionItemKind[type];
  list.forEach((item) => {
    if (isDiy) {
      if (type === 'Function') {
        if (lang === 'hql' || (item.udfType === 1 || item.udfType === 3)) {
          formatList.push({
            label: item.udfName + '()',
            kind,
            insertText: item.udfName + '()',
            detail: item.udfType > 2 ? '方法函数' : 'UDF函数',
            documentation: item.description,
          });
        }
      } else if (type === 'Variable') {
        formatList.push({
          label: item.key,
          kind,
          insertText: item.key,
          detail: '用户自定义的全局变量',
          documentation: `{"${item.key}":"${item.value}"}`,
        });
      }
    } else {
      formatList.push({
        label: item.label.toLowerCase(),
        insertText: item.insertText.toLowerCase(),
        detail: item.detail,
        insertTextRules: isSnippet ? monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet : null,
        documentation: item.documentation,
        kind,
      });
    }
  });
  return formatList;
}

export {
  getHiveList,
  getReturnList,
  getFormatProposalsList,
};
