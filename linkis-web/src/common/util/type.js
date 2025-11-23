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


const objectToString = Object.prototype.toString;
const OBJECT_STRING = '[object Object]';

/**
 * Is it a normal object(是否是普通对象)
 * @param {any} obj
 * @return {Boolean}
 */
export function isPlainObject(obj) {
  return objectToString.call(obj) === OBJECT_STRING;
}

/**
 * Is it a number(是否是数字)
 * @param {any} value
 * @return {Boolean}
 */
export function isNumber(value) {
  return typeof value === 'number';
}

/**
 * Is it a date(是否是日期)
 * @param {any} value
 * @return {Boolean}
 */
export function isDate(value) {
  return objectToString.call(value) === '[object Date]';
}

/**
 * Is it a function(是否是函数)
 * @param {any} value
 * @return {Boolean}
 */
export function isFunction(value) {
  return typeof value === 'function';
}

/**
 * Is it a function(是否是函数)
 * @param {any} value
 * @return {Boolean}
 */
export function isObject(value) {
  let type = typeof value;
  return !!value && (type == 'object' || type == 'function');
}

/**
 * Is it an array(是否是数组)
 * @param {any} value
 * @return {Boolean}
 */
export function isArray(value) {
  return Array.isArray(value);
}

/**
 * Is it like an object(是否像对象)
 * @param {any} value
 * @return {Boolean}
 */
export function isObjectLike(value) {
  return !!value && typeof value == 'object';
}

/**
 * is it a string(是否是字符串)
 * @param {any} value
 * @return {Boolean}
 */
export function isString(value) {
  return typeof value == 'string' ||
        (!isArray(value) && isObjectLike(value) && objectToString.call(value) == '[object String]');
}

/**
 * is it empty(是否是空的)
 * @param {any} value
 * @return {Boolean}
 */
export function isNull(value) {
  return value === undefined || value === null || value === '';
}
