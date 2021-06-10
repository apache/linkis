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

import { getReturnList } from '../util';
const shellKeywordInfoProposals = [
  {
    label: 'if',
    documentation: 'Keywords',
    insertText: 'if',
    detail: 'Keywords',
  },
  {
    label: 'then',
    documentation: 'Keywords',
    insertText: 'then',
    detail: 'Keywords',
  },
  {
    label: 'do',
    documentation: 'Keywords',
    insertText: 'do',
    detail: 'Keywords',
  },
  {
    label: 'else',
    documentation: 'Keywords',
    insertText: 'else',
    detail: 'Keywords',
  },
  {
    label: 'elif',
    documentation: 'Keywords',
    insertText: 'elif',
    detail: 'Keywords',
  },
  {
    label: 'while',
    documentation: 'Keywords',
    insertText: 'while',
    detail: 'Keywords',
  },
  {
    label: 'until',
    documentation: 'Keywords',
    insertText: 'until',
    detail: 'Keywords',
  },
  {
    label: 'for',
    documentation: 'Keywords',
    insertText: 'for',
    detail: 'Keywords',
  },
  {
    label: 'in',
    documentation: 'Keywords',
    insertText: 'in',
    detail: 'Keywords',
  },
  {
    label: 'esac',
    documentation: 'Keywords',
    insertText: 'esac',
    detail: 'Keywords',
  },
  {
    label: 'fi',
    documentation: 'Keywords',
    insertText: 'fi',
    detail: 'Keywords',
  },
  {
    label: 'fin',
    documentation: 'Keywords',
    insertText: 'fin',
    detail: 'Keywords',
  },
  {
    label: 'fil',
    documentation: 'Keywords',
    insertText: 'fil',
    detail: 'Keywords',
  },
  {
    label: 'done',
    documentation: 'Keywords',
    insertText: 'done',
    detail: 'Keywords',
  },
  {
    label: 'exit',
    documentation: 'Keywords',
    insertText: 'exit',
    detail: 'Keywords',
  },
  {
    label: 'set',
    documentation: 'Keywords',
    insertText: 'set',
    detail: 'Keywords',
  },
  {
    label: 'unset',
    documentation: 'Keywords',
    insertText: 'unset',
    detail: 'Keywords',
  },
  {
    label: 'export',
    documentation: 'Keywords',
    insertText: 'export',
    detail: 'Keywords',
  },
  {
    label: 'function',
    documentation: 'Keywords',
    insertText: 'function',
    detail: 'Keywords',
  },
  {
    label: 'awk',
    documentation: 'Builtin',
    insertText: 'awk',
    detail: 'Builtin',
  },
  {

    label: 'ab',
    documentation: 'Builtin',
    insertText: 'ab',
    detail: 'Builtin',
  },
  {

    label: 'bash',
    documentation: 'Builtin',
    insertText: 'bash',
    detail: 'Builtin',
  },
  {

    label: 'beep',
    documentation: 'Builtin',
    insertText: 'beep',
    detail: 'Builtin',
  },
  {

    label: 'cat',
    documentation: 'Builtin',
    insertText: 'cat',
    detail: 'Builtin',
  },
  {

    label: 'cc',
    documentation: 'Builtin',
    insertText: 'cc',
    detail: 'Builtin',
  },
  {

    label: 'cd',
    documentation: 'Builtin',
    insertText: 'cd',
    detail: 'Builtin',
  },
  {

    label: 'chown',
    documentation: 'Builtin',
    insertText: 'chown',
    detail: 'Builtin',
  },
  {

    label: 'chmod',
    documentation: 'Builtin',
    insertText: 'chmod',
    detail: 'Builtin',
  },
  {

    label: 'chroot',
    documentation: 'Builtin',
    insertText: 'chroot',
    detail: 'Builtin',
  },
  {

    label: 'clear',
    documentation: 'Builtin',
    insertText: 'clear',
    detail: 'Builtin',
  },
  {

    label: 'cp',
    documentation: 'Builtin',
    insertText: 'cp',
    detail: 'Builtin',
  },
  {

    label: 'curl',
    documentation: 'Builtin',
    insertText: 'curl',
    detail: 'Builtin',
  },
  {

    label: 'cut',
    documentation: 'Builtin',
    insertText: 'cut',
    detail: 'Builtin',
  },
  {

    label: 'diff',
    documentation: 'Builtin',
    insertText: 'diff',
    detail: 'Builtin',
  },
  {

    label: 'echo',
    documentation: 'Builtin',
    insertText: 'echo',
    detail: 'Builtin',
  },
  {

    label: 'find',
    documentation: 'Builtin',
    insertText: 'find',
    detail: 'Builtin',
  },
  {

    label: 'gawk',
    documentation: 'Builtin',
    insertText: 'gawk',
    detail: 'Builtin',
  },
  {

    label: 'gcc',
    documentation: 'Builtin',
    insertText: 'gcc',
    detail: 'Builtin',
  },
  {

    label: 'get',
    documentation: 'Builtin',
    insertText: 'get',
    detail: 'Builtin',
  },
  {

    label: 'git',
    documentation: 'Builtin',
    insertText: 'git',
    detail: 'Builtin',
  },
  {

    label: 'grep',
    documentation: 'Builtin',
    insertText: 'grep',
    detail: 'Builtin',
  },
  {

    label: 'hg',
    documentation: 'Builtin',
    insertText: 'hg',
    detail: 'Builtin',
  },
  {

    label: 'kill',
    documentation: 'Builtin',
    insertText: 'kill',
    detail: 'Builtin',
  },
  {

    label: 'killall',
    documentation: 'Builtin',
    insertText: 'killall',
    detail: 'Builtin',
  },
  {

    label: 'ln',
    documentation: 'Builtin',
    insertText: 'ln',
    detail: 'Builtin',
  },
  {

    label: 'ls',
    documentation: 'Builtin',
    insertText: 'ls',
    detail: 'Builtin',
  },
  {

    label: 'make',
    documentation: 'Builtin',
    insertText: 'make',
    detail: 'Builtin',
  },
  {

    label: 'mkdir',
    documentation: 'Builtin',
    insertText: 'mkdir',
    detail: 'Builtin',
  },
  {

    label: 'openssl',
    documentation: 'Builtin',
    insertText: 'openssl',
    detail: 'Builtin',
  },
  {

    label: 'mv',
    documentation: 'Builtin',
    insertText: 'mv',
    detail: 'Builtin',
  },
  {

    label: 'nc',
    documentation: 'Builtin',
    insertText: 'nc',
    detail: 'Builtin',
  },
  {

    label: 'node',
    documentation: 'Builtin',
    insertText: 'node',
    detail: 'Builtin',
  },
  {

    label: 'npm',
    documentation: 'Builtin',
    insertText: 'npm',
    detail: 'Builtin',
  },
  {

    label: 'ping',
    documentation: 'Builtin',
    insertText: 'ping',
    detail: 'Builtin',
  },
  {

    label: 'ps',
    documentation: 'Builtin',
    insertText: 'ps',
    detail: 'Builtin',
  },
  {

    label: 'restart',
    documentation: 'Builtin',
    insertText: 'restart',
    detail: 'Builtin',
  },
  {

    label: 'rm',
    documentation: 'Builtin',
    insertText: 'rm',
    detail: 'Builtin',
  },
  {

    label: 'rmdir',
    documentation: 'Builtin',
    insertText: 'rmdir',
    detail: 'Builtin',
  },
  {

    label: 'sed',
    documentation: 'Builtin',
    insertText: 'sed',
    detail: 'Builtin',
  },
  {

    label: 'service',
    documentation: 'Builtin',
    insertText: 'service',
    detail: 'Builtin',
  },
  {

    label: 'sh',
    documentation: 'Builtin',
    insertText: 'sh',
    detail: 'Builtin',
  },
  {

    label: 'shopt',
    documentation: 'Builtin',
    insertText: 'shopt',
    detail: 'Builtin',
  },
  {

    label: 'shred',
    documentation: 'Builtin',
    insertText: 'shred',
    detail: 'Builtin',
  },
  {

    label: 'source',
    documentation: 'Builtin',
    insertText: 'source',
    detail: 'Builtin',
  },
  {

    label: 'sort',
    documentation: 'Builtin',
    insertText: 'sort',
    detail: 'Builtin',
  },
  {

    label: 'sleep',
    documentation: 'Builtin',
    insertText: 'sleep',
    detail: 'Builtin',
  },
  {

    label: 'ssh',
    documentation: 'Builtin',
    insertText: 'ssh',
    detail: 'Builtin',
  },
  {

    label: 'start',
    documentation: 'Builtin',
    insertText: 'start',
    detail: 'Builtin',
  },
  {

    label: 'stop',
    documentation: 'Builtin',
    insertText: 'stop',
    detail: 'Builtin',
  },
  {

    label: 'su',
    documentation: 'Builtin',
    insertText: 'su',
    detail: 'Builtin',
  },
  {

    label: 'sudo',
    documentation: 'Builtin',
    insertText: 'sudo',
    detail: 'Builtin',
  },
  {

    label: 'svn',
    documentation: 'Builtin',
    insertText: 'svn',
    detail: 'Builtin',
  },
  {

    label: 'tee',
    documentation: 'Builtin',
    insertText: 'tee',
    detail: 'Builtin',
  },
  {

    label: 'telnet',
    documentation: 'Builtin',
    insertText: 'telnet',
    detail: 'Builtin',
  },
  {

    label: 'top',
    documentation: 'Builtin',
    insertText: 'top',
    detail: 'Builtin',
  },
  {

    label: 'touch',
    documentation: 'Builtin',
    insertText: 'touch',
    detail: 'Builtin',
  },
  {

    label: 'vi',
    documentation: 'Builtin',
    insertText: 'vi',
    detail: 'Builtin',
  },
  {

    label: 'vim',
    documentation: 'Builtin',
    insertText: 'vim',
    detail: 'Builtin',
  },
  {

    label: 'wall',
    documentation: 'Builtin',
    insertText: 'wall',
    detail: 'Builtin',
  },
  {

    label: 'wc',
    documentation: 'Builtin',
    insertText: 'wc',
    detail: 'Builtin',
  },
  {

    label: 'wget',
    documentation: 'Builtin',
    insertText: 'wget',
    detail: 'Builtin',
  },
  {

    label: 'who',
    documentation: 'Builtin',
    insertText: 'who',
    detail: 'Builtin',
  },
  {

    label: 'write',
    documentation: 'Builtin',
    insertText: 'write',
    detail: 'Builtin',
  },
  {

    label: 'yes',
    documentation: 'Builtin',
    insertText: 'yes',
    detail: 'Builtin',
  },
  {
    label: 'zsh',
    documentation: 'Builtin',
    insertText: 'zsh',
    detail: 'Builtin',
  },
];

export default {
  async register(monaco) {
    const shellProposals = shellKeywordInfoProposals.map((item) => ({
      label: item.label.toLowerCase(),
      kind: monaco.languages.CompletionItemKind.Keyword,
      insertText: item.insertText.toLowerCase(),
      detail: item.detail,
      documentation: item.documentation,
    }));

    monaco.languages.registerCompletionItemProvider('sh', {
      triggerCharacters: 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._'.split(''),
      async provideCompletionItems(model, position) {
        const textUntilPosition = model.getValueInRange({
          startLineNumber: position.lineNumber,
          startColumn: 1,
          endLineNumber: position.lineNumber,
          endColumn: position.column,
        });
        const keywordMatch = textUntilPosition.match(/([^"]*)?$/i);
        if (keywordMatch) {
          const matchList = keywordMatch[0].split(' ');
          const match = matchList[matchList.length - 1];
          const list = getReturnList(match, shellProposals, 'insertText');
          return list;
        }
        return [];
      },
    });
  },
};
