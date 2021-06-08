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

'use strict';
export const conf = {
  comments: {
    lineComment: '#',
  },
  brackets: [['{', '}'], ['[', ']'], ['(', ')']],
  autoClosingPairs: [
    { open: '{', close: '}' },
    { open: '[', close: ']' },
    { open: '(', close: ')' },
    { open: '"', close: '"' },
    { open: '\'', close: '\'' },
    { open: '`', close: '`' },
  ],
  surroundingPairs: [
    { open: '{', close: '}' },
    { open: '[', close: ']' },
    { open: '(', close: ')' },
    { open: '"', close: '"' },
    { open: '\'', close: '\'' },
    { open: '`', close: '`' },
  ],
};
export const language = {
  defaultToken: '',
  ignoreCase: true,
  tokenPostfix: '.shell',
  brackets: [
    { token: 'delimiter.bracket', open: '{', close: '}' },
    { token: 'delimiter.parenthesis', open: '(', close: ')' },
    { token: 'delimiter.square', open: '[', close: ']' },
  ],
  keywords: [
    'if',
    'then',
    'do',
    'else',
    'elif',
    'while',
    'until',
    'for',
    'in',
    'esac',
    'fi',
    'fin',
    'fil',
    'done',
    'exit',
    'set',
    'unset',
    'export',
    'function',
  ],
  builtins: [
    'ab',
    'awk',
    'bash',
    'beep',
    'cat',
    'cc',
    'cd',
    'chown',
    'chmod',
    'chroot',
    'clear',
    'cp',
    'curl',
    'cut',
    'diff',
    'echo',
    'find',
    'gawk',
    'gcc',
    'get',
    'git',
    'grep',
    'hg',
    'kill',
    'killall',
    'ln',
    'ls',
    'make',
    'mkdir',
    'openssl',
    'mv',
    'nc',
    'node',
    'npm',
    'ping',
    'ps',
    'restart',
    'rm',
    'rmdir',
    'sed',
    'service',
    'sh',
    'shopt',
    'shred',
    'source',
    'sort',
    'sleep',
    'ssh',
    'start',
    'stop',
    'su',
    'sudo',
    'svn',
    'tee',
    'telnet',
    'top',
    'touch',
    'vi',
    'vim',
    'wall',
    'wc',
    'wget',
    'who',
    'write',
    'yes',
    'zsh',
  ],
  // we include these common regular expressions
  symbols: /[=><!~?&|+\-*\/\^;\.,]+/,
  // The main tokenizer for our languages
  tokenizer: {
    root: [
      { include: '@whitespace' },
      [
        /[a-zA-Z]\w*/,
        {
          cases: {
            '@keywords': 'keyword',
            '@builtins': 'type.identifier',
            '@default': '',
          },
        },
      ],
      { include: '@strings' },
      { include: '@parameters' },
      { include: '@heredoc' },
      [/[{}\[\]()]/, '@brackets'],
      [/-+\w+/, 'attribute.name'],
      [/@symbols/, 'delimiter'],
      { include: '@numbers' },
      [/[,;]/, 'delimiter'],
    ],
    whitespace: [
      [/\s+/, 'white'],
      [/(^#!.*$)/, 'metatag'],
      [/(^#.*$)/, 'comment'],
    ],
    numbers: [
      [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
      [/0[xX][0-9a-fA-F_]*[0-9a-fA-F]/, 'number.hex'],
      [/\d+/, 'number'],
    ],
    // Recognize strings, including those broken across lines
    strings: [
      [/'/, 'string', '@stringBody'],
      [/"/, 'string', '@dblStringBody'],
    ],
    stringBody: [
      [/'/, 'string', '@popall'],
      [/./, 'string'],
    ],
    dblStringBody: [
      [/"/, 'string', '@popall'],
      [/./, 'string'],
    ],
    heredoc: [
      [/(<<[-<]?)(\s*)(['"`]?)([\w\-]+)(['"`]?)/, ['constants', 'white', 'string.heredoc.delimiter', 'string.heredoc', 'string.heredoc.delimiter']],
    ],
    parameters: [
      [/\$\d+/, 'variable.predefined'],
      [/\$\w+/, 'variable'],
      [/\$[*@#?\-$!0_]/, 'variable'],
      [/\$['"{(]\w+['"})]/, 'variable'],
    ],
  },
};

export default {
  config: conf,
  definition: language,
  register(monaco) {
    monaco.languages.register({ id: 'sh' });
    monaco.languages.setLanguageConfiguration('sh', conf);
    monaco.languages.setMonarchTokensProvider('sh', language);
  },
};
