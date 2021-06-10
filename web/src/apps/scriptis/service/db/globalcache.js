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

import { isNil, remove } from 'lodash';
import { Basic } from '@/common/service/db/index.js';
/**
 * @class Globalcache
 * @extends {Basic}
 */
class Globalcache extends Basic {
  /**
     *Creates an instance of Globalcache.
     * @param {*} table
     * @param {*} db
     * @memberof Globalcache
     */
  constructor(table) {
    super(table);
  }

  /**
     * @param {*} args
     * @return {*}
     */
  async setCache(args) {
    let cache = await this.getCache(args.key);
    let cacheToUpdate = args;
    if (!isNil(cache)) {
      cacheToUpdate = { _id: cache._id, ...cacheToUpdate };
    }
    return this.add(cacheToUpdate);
  }

  /**
     * @param {*} key
     * @return {*}
     */
  async getCache(key) {
    let caches = await this.get(key) || [];
    return caches[0];
  }

  /**
     * @param {*} args
     * @return {*}
     */
  async removeCache(args) {
    let cache = await this.getCache(args.id);
    let tabList = [];
    if (!isNil(cache)) {
      tabList = cache.tabList;
      remove(tabList, (n) => n === args.tabId);
    }
    return this.update(args.id, { key: args.id, tabList: tabList });
  }

  /**
     * @param {*} args
     * @return {*}
     */
  async updateCache(args) {
    if (args.work) {
      let cache = await this.getCache(args.id);
      const id = args.work.id;
      let tabList = [];
      if (!isNil(cache)) {
        tabList = cache.tabList;
        if (cache.tabList.indexOf(id) === -1) {
          tabList.push(id);
        }
      } else {
        tabList.push(id);
      }
      return this.update(args.id, { key: args.id, tabList: tabList });
    }
    if (args.fnList) {
      this.update(args.id, { key: args.id, fnList: args.fnList });
    }
    if (args.variableList) {
      this.update(args.id, { key: args.id, variableList: args.variableList });
    }
    return;
  }
}
const globalcache = new Globalcache('globalCache');

export default globalcache;
