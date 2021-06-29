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

import Db2Formatter from './languages/Db2Formatter';
import N1qlFormatter from './languages/N1qlFormatter';
import PlSqlFormatter from './languages/PlSqlFormatter';
import StandardSqlFormatter from './languages/StandardSqlFormatter';

export default {
  /**
     * Format whitespaces in a query to make it easier to read.
     *
     * @param {String} query
     * @param {Object} cfg
     *  @param {String} cfg.language Query language, default is Standard SQL
     *  @param {String} cfg.indent Characters used for indentation, default is "  " (2 spaces)
     *  @param {Object} cfg.params Collection of params for placeholder replacement
     * @return {String}
     */
  format: (query, cfg) => {
    cfg = cfg || {};

    switch (cfg.language) {
      case 'db2':
        return new Db2Formatter(cfg).format(query);
      case 'n1ql':
        return new N1qlFormatter(cfg).format(query);
      case 'pl/sql':
        return new PlSqlFormatter(cfg).format(query);
      case 'sql':
      case undefined:
        return new StandardSqlFormatter(cfg).format(query);
      default:
        throw Error(`Unsupported SQL dialect: ${cfg.language}`);
    }
  },
};
