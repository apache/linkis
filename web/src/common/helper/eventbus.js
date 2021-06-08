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

/**
 *  事件bus
 */
class Eventbus {
  /**
     * 构造器
     */
  constructor() {
    this.storage = {};
  }

  /**
     * 
     * @param {*} arr 
     * @param {*} x 
     * @return {Object}
     */
  findIndex(arr, x) {
    return arr.findIndex((item) => item === x);
  }

  /**
     * 注册事件
     * @param {*} name 
     * @param {*} fn 
     * @return {undefined}
     */
  on(name, fn) {
    if (Object.prototype.toString.call(this.storage[name]) === '[object Array]') {
      this.storage[name].push(fn);
    } else {
      this.storage[name] = [fn];
    }
  }

  /**
    * 删除事件
    * @param {*} name 
    * @param {*} fn 
    * @return {undefined}
    */
  off(name, fn) {
    if (this.storage[name]) {
      let i = this.findIndex(this.storage[name], fn);
      if (i !== -1) this.storage[name].splice(i, 1);
    }
  }

  /**
     * 清空指定name的所有事件
     * @param {*} name 
     * @return {Object}
     */
  clear(name) {
    if (name) {
      this.storage[name] = [];
    } else {
      this.storage = {};
    }
    return this.storage;
  }

  /**
     * 触发事件
     * @param {*} name 
     * @param {*} payload  参数
     * @param {*} cb      回调函数 
     * @return {undefined}
     */
  emit(name, payload, cb) {
    let fns = this.storage[name];
    if (fns) {
      if (fns.length > 1) {
        let arr = [];
        this.storage[name].forEach((f) => {
          arr.push(Promise.resolve(f(payload, cb)));
        });
        return Promise.all(arr);
      } else if (fns.length === 1) {
        return Promise.resolve(fns[0](payload, cb));
      }
    }
  }
}

let eventbus = new Eventbus();


export {
  eventbus as
  default,
  eventbus,
  Eventbus,
};
