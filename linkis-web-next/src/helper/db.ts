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

import Dexie from 'dexie';
/**
 * wrap db operator
 * @class DB
 */
export default class DB {
    db: Dexie;
    whereClause: any;
    collection: any;
    errHandler: any;
    /**
     * Creates an instance of DB.
     * @param {*} name
     * @param {Object} stores
     * @param {Number} version
     * @memberof DB
     */
    constructor(name: string, stores: any, version = 1) {
        this.db = new Dexie(name);
        this.db.version(version).stores(stores);
    }
    /**
     *
     * @param {*} stores
     * @param {*} version
     * @memberof DB
     */
    updateVersion(stores: any, version: number) {
        try {
            this.db.version(version).stores(stores);
        } catch (e) {
            this._errorCatch(e);
        }
    }
    /**
     * wrap put method
     * If an object with the same primary key already exists,
     * it will be replaced with the given object.
     * If it does not exist, it will be added.
     * @param {String} table
     * @param {Object | Array} fields
     * @param {*} id
     * @memberof DB
     */
    async put(table, fields) {
        const promiseArr = [];
        if (Array.isArray(fields)) {
            fields.forEach((item) => {
                promiseArr.push(this.db[table].put(item));
            });
        } else {
            promiseArr.push(this.db[table].put(fields));
        }
        try {
            return await Promise.all(promiseArr);
        } catch (e) {
            return this._errorCatch(e);
        }
    }
    /**
     * wrap get method
     * @param {String} table
     * @param {Array} keys
     * @memberof DB
     */
    async get(table, keys) {
        const promiseArr = [];
        if (Array.isArray(keys)) {
            keys.forEach((item) => {
                promiseArr.push(this.db[table].get(item));
            });
        } else {
            promiseArr.push(this.db[table].get(keys));
        }
        try {
            return await Promise.all(promiseArr);
        } catch (e) {
            return this._errorCatch(e);
        }
    }
    /**
     *
     * @param {*} table
     * @param {String | Object} clause
     * @memberof DB
     * @return {this}
     */
    where(table, clause) {
        this.whereClause = this.db[table].where(clause);
        return this;
    }
    /**
     *
     * @param {*} table
     * @param {*} key
     * @param {*} changes
     * @return {updated}
     * @memberof DB
     */
    async update(table, key, changes) {
        try {
            return await this.db[table].update(key, changes);
        } catch (e) {
            return this._errorCatch(e);
        }
    }
    /**
     *
     * @param {String | Object} clause
     * @memberof DB
     * @return {this}
     */
    equals(clause) {
        this.collection = this.whereClause.where(clause);
        return this;
    }
    /**
     *
     * @param {String | Array} clause
     * @memberof DB
     * @return {this}
     */
    anyOf(clause) {
        this.collection = this.whereClause.where(clause);
        return this;
    }
    /**
     *
     * @param {Function} cb
     * @memberof DB
     * @return {Promise}
     */
    async first(cb) {
        try {
            return await this.collection.first(cb);
        } catch (e) {
            return this._errorCatch(e);
        }
    }
    /**
     * db or table or collection delete
     * @memberof DB
     * @return {Promise}
     */
    async delete(...args) {
        const len = args.length;
        const table = args[0];
        let key = args[1];
        try {
            if (len === 0) {
                return await this.collection.delete();
            } else if (len === 2) {
                return await this.db[table].delete(key);
            }
        } catch (e) {
            return this._errorCatch(e);
        }
    }
    /**
     * @param {*} table
     * @memberof DB
     * @return {Promise}
     */
    async toArray(table) {
        try {
            if (table) {
                this.collection = this.db[table];
            }
            return await this.collection.toArray();
        } catch (e) {
            return this._errorCatch(e);
        }
    }
    /**
     * wrap catch error
     * @param {*} e
     * @memberof DB
     * @return {Promise}
     */
    _errorCatch(e: any) {
        this.errHandler && this.errHandler(e.message);
        return Promise.resolve(null);
    }
}
