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

const objectToString = Object.prototype.toString;
const OBJECT_STRING = '[object Object]';

/**
 * 是否是普通对象
 * @param {any} obj
 * @return {Boolean}
 */
export function isPlainObject(obj) {
  return objectToString.call(obj) === OBJECT_STRING;
}

/**
 * 是否是数字
 * @param {any} value
 * @return {Boolean}
 */
export function isNumber(value) {
  return typeof value === 'number';
}

/**
 * 是否是日期
 * @param {any} value
 * @return {Boolean}
 */
export function isDate(value) {
  return objectToString.call(value) === '[object Date]';
}

/**
 * 是否是函数
 * @param {any} value
 * @return {Boolean}
 */
export function isFunction(value) {
  return typeof value === 'function';
}

/**
 * 是否是函数
 * @param {any} value
 * @return {Boolean}
 */
export function isObject(value) {
  let type = typeof value;
  return !!value && (type == 'object' || type == 'function');
}

/**
 * 是否是数组
 * @param {any} value
 * @return {Boolean}
 */
export function isArray(value) {
  return Array.isArray(value);
}

/**
 * 是否像对象
 * @param {any} value
 * @return {Boolean}
 */
export function isObjectLike(value) {
  return !!value && typeof value == 'object';
}

/**
 * 是否是字符串
 * @param {any} value
 * @return {Boolean}
 */
export function isString(value) {
  return typeof value == 'string' ||
        (!isArray(value) && isObjectLike(value) && objectToString.call(value) == '[object String]');
}

/**
 * 是否是空的
 * @param {any} value
 * @return {Boolean}
 */
export function isNull(value) {
  return value === undefined || value === null || value === '';
}
