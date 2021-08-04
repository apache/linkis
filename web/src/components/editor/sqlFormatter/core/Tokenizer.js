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

import isEmpty from 'lodash/isEmpty';
import escapeRegExp from 'lodash/escapeRegExp';
import tokenTypes from './tokenTypes';

/**
 *
 */
export default class Tokenizer {
  /**
   * @param {Object} cfg
   *  @param {String[]} cfg.reservedWords Reserved words in SQL
   *  @param {String[]} cfg.reservedToplevelWords Words that are set to new line separately
   *  @param {String[]} cfg.reservedNewlineWords Words that are set to newline
   *  @param {String[]} cfg.stringTypes String types to enable: "", '', ``, [], N''
   *  @param {String[]} cfg.openParens Opening parentheses to enable, like (, [
   *  @param {String[]} cfg.closeParens Closing parentheses to enable, like ), ]
   *  @param {String[]} cfg.indexedPlaceholderTypes Prefixes for indexed placeholders, like ?
   *  @param {String[]} cfg.namedPlaceholderTypes Prefixes for named placeholders, like @ and :
   *  @param {String[]} cfg.lineCommentTypes Line comments to enable, like # and --
   *  @param {String[]} cfg.specialWordChars Special chars that can be found inside of words, like @ and #
   */
  constructor(cfg) {
    this.WHITESPACE_REGEX = /^(\s+)/;
    this.NUMBER_REGEX = /^((-\s*)?[0-9]+(\.[0-9]+)?|0x[0-9a-fA-F]+|0b[01]+)\b/;
    this.OPERATOR_REGEX = /^(!=|<>|==|<=|>=|!<|!>|\|\||::|->>|->|~~\*|~~|!~~\*|!~~|~\*|!~\*|!~|.)/;

    this.BLOCK_COMMENT_REGEX = /^(\/\*[^]*?(?:\*\/|$))/;
    this.PARAMS_REGEX = cfg.paramsPattern;
    this.LINE_COMMENT_REGEX = this.createLineCommentRegex(cfg.lineCommentTypes);

    this.RESERVED_TOPLEVEL_REGEX = this.createReservedWordRegex(cfg.reservedToplevelWords);
    this.RESERVED_NEWLINE_REGEX = this.createReservedWordRegex(cfg.reservedNewlineWords);
    this.RESERVED_PLAIN_REGEX = this.createReservedWordRegex(cfg.reservedWords);

    this.WORD_REGEX = this.createWordRegex(cfg.specialWordChars);
    this.STRING_REGEX = this.createStringRegex(cfg.stringTypes);

    this.OPEN_PAREN_REGEX = this.createParenRegex(cfg.openParens);
    this.CLOSE_PAREN_REGEX = this.createParenRegex(cfg.closeParens);

    this.INDEXED_PLACEHOLDER_REGEX = this.createPlaceholderRegex(cfg.indexedPlaceholderTypes, '[0-9]*');
    this.IDENT_NAMED_PLACEHOLDER_REGEX = this.createPlaceholderRegex(cfg.namedPlaceholderTypes, '[a-zA-Z0-9._$]+');
    this.STRING_NAMED_PLACEHOLDER_REGEX = this.createPlaceholderRegex(
      cfg.namedPlaceholderTypes,
      this.createStringPattern(cfg.stringTypes)
    );
  }

  /**
   *
   * @param {*} lineCommentTypes
   * @return {*}
   */
  createLineCommentRegex(lineCommentTypes) {
    return new RegExp(`^((?:${lineCommentTypes.map((c) => escapeRegExp(c)).join('|')}).*?(?:\n|\r\n|$))`);
  }

  /**
   *
   * @param {*} reservedWords
   * @return {*}
   */
  createReservedWordRegex(reservedWords) {
    const reservedWordsPattern = reservedWords.join('|').replace(/ /g, '\\s+');
    return new RegExp(`^(${reservedWordsPattern})\\b`, 'i');
  }

  /**
   *
   * @param {*} specialChars
   * @return {*}
   */
  createWordRegex(specialChars = []) {
    return new RegExp(`^([\\w${specialChars.join('')}]+)`);
  }

  /**
   *
   * @param {*} stringTypes
   * @return {*}
   */
  createStringRegex(stringTypes) {
    return new RegExp(
      '^(' + this.createStringPattern(stringTypes) + ')'
    );
  }

  /**
   * This enables the following string patterns:
   * 1.backtick quoted string using `` to escape
   * 2.square bracket quoted string (SQL Server) using ]] to escape
   * 3.double quoted string using "" or \" to escape
   * 4.single quoted string using '' or \' to escape
   * 5.national character quoted string using N'' or N\' to escape
   * @param {*} stringTypes
   * @return {*}
   */
  createStringPattern(stringTypes) {
    const patterns = {
      '``': '((`[^`]*($|`))+)',
      '[]': '((\\[[^\\]]*($|\\]))(\\][^\\]]*($|\\]))*)',
      '""': '(("[^"\\\\]*(?:\\\\.[^"\\\\]*)*("|$))+)',
      '\'\'': '((\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*(\'|$))+)',
      'N\'\'': '((N\'[^N\'\\\\]*(?:\\\\.[^N\'\\\\]*)*(\'|$))+)',
    };

    return stringTypes.map((t) => patterns[t]).join('|');
  }

  /**
   *
   * @param {*} parens
   * @return {*}
   */
  createParenRegex(parens) {
    return new RegExp(
      '^(' + parens.map((p) => this.escapeParen(p)).join('|') + ')',
      'i'
    );
  }

  /**
   *
   * @param {*} paren
   * @return {*}
   */
  escapeParen(paren) {
    if (paren.length === 1) {
      // A single punctuation character
      return escapeRegExp(paren);
    } else {
      // longer word
      return '\\b' + paren + '\\b';
    }
  }

  /**
   *
   * @param {*} types
   * @param {*} pattern
   * @return {*}
   */
  createPlaceholderRegex(types, pattern) {
    if (isEmpty(types)) {
      return false;
    }
    const typesRegex = types.map(escapeRegExp).join('|');

    return new RegExp(`^((?:${typesRegex})(?:${pattern}))`);
  }

  /**
   * Takes a SQL string and breaks it into tokens.
   * Each token is an object with type and value.
   *
   * @param {String} input The SQL string
   * @return {Object[]} tokens An array of tokens.
   *  @return {String} token.type
   *  @return {String} token.value
   */
  tokenize(input) {
    const tokens = [];
    let token;

    // Keep processing the string until it is empty
    while (input.length) {
      // Get the next token and the token type
      token = this.getNextToken(input, token);
      // Advance the string
      input = input.substring(token.value.length);

      tokens.push(token);
    }
    return tokens;
  }

  /**
   *
   * @param {*} input
   * @param {*} previousToken
   * @return {*}
   */
  getNextToken(input, previousToken) {
    return this.getWhitespaceToken(input) ||
      this.getCommentToken(input) ||
      this.getStringToken(input) ||
      this.getOpenParenToken(input) ||
      this.getCloseParenToken(input) ||
      this.getParamsToken(input) ||
      this.getPlaceholderToken(input) ||
      this.getNumberToken(input) ||
      this.getReservedWordToken(input, previousToken) ||
      this.getWordToken(input) ||
      this.getOperatorToken(input);
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getWhitespaceToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.WHITESPACE,
      regex: this.WHITESPACE_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getCommentToken(input) {
    return this.getLineCommentToken(input) || this.getBlockCommentToken(input);
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getLineCommentToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.LINE_COMMENT,
      regex: this.LINE_COMMENT_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getBlockCommentToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.BLOCK_COMMENT,
      regex: this.BLOCK_COMMENT_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getStringToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.STRING,
      regex: this.STRING_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getOpenParenToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.OPEN_PAREN,
      regex: this.OPEN_PAREN_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getCloseParenToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.CLOSE_PAREN,
      regex: this.CLOSE_PAREN_REGEX,
    });
  }

  getParamsToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.WORD,
      regex: this.PARAMS_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getPlaceholderToken(input) {
    return this.getIdentNamedPlaceholderToken(input) ||
      this.getStringNamedPlaceholderToken(input) ||
      this.getIndexedPlaceholderToken(input);
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getIdentNamedPlaceholderToken(input) {
    return this.getPlaceholderTokenWithKey({
      input,
      regex: this.IDENT_NAMED_PLACEHOLDER_REGEX,
      parseKey: (v) => v.slice(1),
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getStringNamedPlaceholderToken(input) {
    return this.getPlaceholderTokenWithKey({
      input,
      regex: this.STRING_NAMED_PLACEHOLDER_REGEX,
      parseKey: (v) => this.getEscapedPlaceholderKey({
        key: v.slice(2, -1),
        quoteChar: v.slice(-1)
      }),
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getIndexedPlaceholderToken(input) {
    return this.getPlaceholderTokenWithKey({
      input,
      regex: this.INDEXED_PLACEHOLDER_REGEX,
      parseKey: (v) => v.slice(1),
    });
  }

  /**
   *
   * @param {*} param0
   * @return {*}
   */
  getPlaceholderTokenWithKey({
    input,
    regex,
    parseKey
  }) {
    const token = this.getTokenOnFirstMatch({
      input,
      regex,
      type: tokenTypes.PLACEHOLDER
    });
    if (token) {
      token.key = parseKey(token.value);
    }
    return token;
  }

  /**
   *
   * @param {*} param0
   * @return {*}
   */
  getEscapedPlaceholderKey({
    key,
    quoteChar
  }) {
    return key.replace(new RegExp(escapeRegExp('\\') + quoteChar, 'g'), quoteChar);
  }

  /**
   * Decimal, binary, or hex numbers
   * @param {*} input
   * @return {*}
   */
  getNumberToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.NUMBER,
      regex: this.NUMBER_REGEX,
    });
  }

  /**
   * Punctuation and symbols
   * @param {*} input
   * @return {*}
   */
  getOperatorToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.OPERATOR,
      regex: this.OPERATOR_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @param {*} previousToken
   * @return {*}
   */
  getReservedWordToken(input, previousToken) {
    // A reserved word cannot be preceded by a "."
    // this makes it so in "mytable.from", "from" is not considered a reserved word
    if (previousToken && previousToken.value && previousToken.value === '.') {
      return;
    }
    return this.getToplevelReservedToken(input) || this.getNewlineReservedToken(input) || this.getPlainReservedToken(input);
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getToplevelReservedToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.RESERVED_TOPLEVEL,
      regex: this.RESERVED_TOPLEVEL_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getNewlineReservedToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.RESERVED_NEWLINE,
      regex: this.RESERVED_NEWLINE_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getPlainReservedToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.RESERVED,
      regex: this.RESERVED_PLAIN_REGEX,
    });
  }

  /**
   *
   * @param {*} input
   * @return {*}
   */
  getWordToken(input) {
    return this.getTokenOnFirstMatch({
      input,
      type: tokenTypes.WORD,
      regex: this.WORD_REGEX,
    });
  }

  /**
   *
   * @param {*} param0
   * @return {*}
   */
  getTokenOnFirstMatch({
    input,
    type,
    regex
  }) {
    const matches = input.match(regex);

    if (matches) {
      return {
        type,
        value: matches[1]
      };
    }
  }
}
