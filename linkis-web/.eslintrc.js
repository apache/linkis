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

module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/essential'
  ],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'key-spacing': ['error'],
    'standard/no-callback-literal': 0,
    'handle-callback-err': 0,
    'no-return-assign': 0,
    'eqeqeq': 0,
    'comma-dangle': 0,
    'semi': 0,
    'space-before-function-paren': 0,
    'keyword-spacing': 0,
    'no-useless-escape': 0,
    'operator-linebreak': 0,
    'indent': [
      'error',
      2,
      {
        'SwitchCase': 1
      }
    ],
    'no-const-assign': 'warn',
    'no-this-before-super': 'warn',
    "no-irregular-whitespace": 0,
    'no-undef': 2,
    'no-unreachable': 'warn',
    'no-unused-vars': 2,
    'constructor-super': 'warn',
    'valid-typeof': 'warn',
    'max-len': 'off',
    'no-trailing-spaces': 'off',
    'require-jsdoc': 'warn',
    'no-invalid-this': 'off',
    'linebreak-style': 0,
    'vue/no-parsing-error': [2, {
      'x-invalid-end-tag': false,
      'invalid-first-character-of-tag-name': false
    }],
    'no-tabs': 0,
    'vue/html-indent': [2, 2, {
      'attribute': 1,
      'closeBracket': 0,
      'alignAttributesVertically': false
    }],
    'vue/require-default-prop': 0,
    'vue/component-name-in-template-casing': 0,
    'vue/html-closing-bracket-spacing': 0,
    'vue/html-closing-bracket-newline': 0,
    'vue/singleline-html-element-content-newline': 0,
    'vue/multiline-html-element-content-newline': 0,
    'vue/attributes-order': 0,
    'vue/html-self-closing': 0,
    'no-useless-constructor': 0,
    'no-mixed-operators': 0,
    'no-new-func': 0,
    'no-template-curly-in-string': 0,
    'no-useless-call': 0,
    // Rules below should be made open after refactoring front-end codes
    "one-var": 0,
    "camelcase": 0,
    "vue/multi-word-component-names": "off",
    "vue/no-reserved-component-names": "off",
    "vue/no-mutating-props": "off"
  },
  parserOptions: {
    "parser": 'babel-eslint',
    "sourceType": "module"
  }
}
