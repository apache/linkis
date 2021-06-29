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

import Formatter from '../core/Formatter';
import Tokenizer from '../core/Tokenizer';

const reservedWords = [
  'ALL', 'ALTER', 'ANALYZE', 'AND', 'ANY', 'ARRAY', 'AS', 'ASC',
  'BEGIN', 'BETWEEN', 'BINARY', 'BOOLEAN', 'BREAK', 'BUCKET', 'BUILD', 'BY',
  'CALL', 'CASE', 'CAST', 'CLUSTER', 'COLLATE', 'COLLECTION', 'COMMIT', 'CONNECT', 'CONTINUE', 'CORRELATE', 'COVER', 'CREATE',
  'DATABASE', 'DATASET', 'DATASTORE', 'DECLARE', 'DECREMENT', 'DELETE', 'DERIVED', 'DESC', 'DESCRIBE', 'DISTINCT', 'DO', 'DROP',
  'EACH', 'ELEMENT', 'ELSE', 'END', 'EVERY', 'EXCEPT', 'EXCLUDE', 'EXECUTE', 'EXISTS', 'EXPLAIN',
  'FALSE', 'FETCH', 'FIRST', 'FLATTEN', 'FOR', 'FORCE', 'FROM', 'FUNCTION',
  'GRANT', 'GROUP', 'GSI',
  'HAVING',
  'IF', 'IGNORE', 'ILIKE', 'IN', 'INCLUDE', 'INCREMENT', 'INDEX', 'INFER', 'INLINE', 'INNER', 'INSERT', 'INTERSECT', 'INTO', 'IS',
  'JOIN',
  'KEY', 'KEYS', 'KEYSPACE', 'KNOWN',
  'LAST', 'LEFT', 'LET', 'LETTING', 'LIKE', 'LIMIT', 'LSM',
  'MAP', 'MAPPING', 'MATCHED', 'MATERIALIZED', 'MERGE', 'MINUS', 'MISSING',
  'NAMESPACE', 'NEST', 'NOT', 'NULL', 'NUMBER',
  'OBJECT', 'OFFSET', 'ON', 'OPTION', 'OR', 'ORDER', 'OUTER', 'OVER',
  'PARSE', 'PARTITION', 'PASSWORD', 'PATH', 'POOL', 'PREPARE', 'PRIMARY', 'PRIVATE', 'PRIVILEGE', 'PROCEDURE', 'PUBLIC',
  'RAW', 'REALM', 'REDUCE', 'RENAME', 'RETURN', 'RETURNING', 'REVOKE', 'RIGHT', 'ROLE', 'ROLLBACK',
  'SATISFIES', 'SCHEMA', 'SELECT', 'SELF', 'SEMI', 'SET', 'SHOW', 'SOME', 'START', 'STATISTICS', 'STRING', 'SYSTEM',
  'THEN', 'TO', 'TRANSACTION', 'TRIGGER', 'TRUE', 'TRUNCATE',
  'UNDER', 'UNION', 'UNIQUE', 'UNKNOWN', 'UNNEST', 'UNSET', 'UPDATE', 'UPSERT', 'USE', 'USER', 'USING',
  'VALIDATE', 'VALUE', 'VALUED', 'VALUES', 'VIA', 'VIEW',
  'WHEN', 'WHERE', 'WHILE', 'WITH', 'WITHIN', 'WORK',
  'XOR',
];

const reservedToplevelWords = [
  'DELETE FROM',
  'EXCEPT ALL', 'EXCEPT', 'EXPLAIN DELETE FROM', 'EXPLAIN UPDATE', 'EXPLAIN UPSERT',
  'FROM',
  'GROUP BY',
  'HAVING',
  'INFER', 'INSERT INTO', 'INTERSECT ALL', 'INTERSECT',
  'LET', 'LIMIT',
  'MERGE',
  'NEST',
  'ORDER BY',
  'PREPARE',
  'SELECT', 'SET CURRENT SCHEMA', 'SET SCHEMA', 'SET',
  'UNION ALL', 'UNION', 'UNNEST', 'UPDATE', 'UPSERT', 'USE KEYS',
  'VALUES',
  'WHERE',
];

const reservedNewlineWords = [
  'AND',
  'INNER JOIN',
  'JOIN',
  'LEFT JOIN',
  'LEFT OUTER JOIN',
  'OR', 'OUTER JOIN',
  'RIGHT JOIN', 'RIGHT OUTER JOIN',
  'XOR',
];

let tokenizer;

/**
 *
 */
export default class N1qlFormatter {
  /**
     * @param {Object} cfg Different set of configurations
     */
  constructor(cfg) {
    this.cfg = cfg;
  }

  /**
     * Format the whitespace in a N1QL string to make it easier to read
     *
     * @param {String} query The N1QL string
     * @return {String} formatted string
     */
  format(query) {
    if (!tokenizer) {
      tokenizer = new Tokenizer({
        reservedWords,
        reservedToplevelWords,
        reservedNewlineWords,
        stringTypes: [`""`, '\'\'', '``'],
        openParens: ['(', '[', '{'],
        closeParens: [')', ']', '}'],
        namedPlaceholderTypes: ['$'],
        lineCommentTypes: ['#', '--'],
      });
    }
    return new Formatter(this.cfg, tokenizer).format(query);
  }
}
