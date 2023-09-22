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


import DB from '@/common/helper/db.js';
import { config } from '@/common/config/db.js';
// To update the schema please use db.updateVersion(stores, version)(更新schema请用db.updateVersion(stores, version))
const db = new DB('bdp-ide', config.stores, config.version);
// To change the primary key, you must delete the original store before it can be updated. For other non-primary key modifications, call updateVersion directly.(更改主键要先删掉原来的store才能更新，其他非主键修改直接调用updateVersion)
// db.updateVersion(Object.assign({}, config.stores), config.version + 1);
/**
 * @class basic
 */
class Basic {
  /**
     *Creates an instance of basic.
     * @memberof basic
     * @param {*} table
     */
  constructor(table) {
    this.table = table;
  }

  /**
     * @param {Tab} tab
     * @param {*} id
     * @memberof Tab
     * @return {promise}
     */
  add(tab) {
    return db.put(this.table, tab);
  }
  /**
     * @param {string}key
     * @return {promise} tabList or query tab by key
     * @memberof Tab
     */
  get(key) {
    if (key) {
      return db.get(this.table, key);
    }
    return db.toArray(this.table);
  }
  /**
     *
     *
     * @param {*} key
     * @return {promise}
     * @memberof basic
     */
  remove(key) {
    return db.delete(this.table, key);
  }
  /**
     * @param {*} key
     * @param {*} changes
     * @return {*}
     */
  update(key, changes) {
    return db.update(this.table, key, changes);
  }
  /**
     *
     * @param {*} oldKey
     * @param {*} newKey
     */
  async modifyPrimaryKey(oldKey, newKey) {
    let object = await db.get(this.table, oldKey);
    if (object && object[0]) {
      await db.delete(this.table, oldKey);
      if (this.table === 'tab') {
        object[0].id = newKey;
        object = Object.assign(object, { id: newKey });
      } else {
        object[0].tabId = newKey;
        object = Object.assign(object, { tabId: newKey });
      }
      return db.put(this.table, object);
    }
  }
}
export {
  db,
  Basic,
}
;
