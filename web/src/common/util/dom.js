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

const inBrowser =
    typeof window !== 'undefined' &&
    Object.prototype.toString.call(window) !== '[object Object]';
export const UA = inBrowser && window.navigator.userAgent.toLowerCase();
export const isIE = UA && UA.indexOf('trident') > 0;
export const isIE9 = UA && UA.indexOf('msie 9.0') > 0;

/**
 * For IE9 compat: when both class and :class are present
 * getAttribute('class') returns wrong value...
 *
 * @param {Element} el
 * @return {String}
 */
export function getClass(el) {
  let classname = el.className;
  if (typeof classname === 'object') {
    classname = classname.baseVal || '';
  }
  return classname;
}

/**
 * 判断dom节点是否有某样式
 * 
 * @param {Element} el
 * @param {String} name
 * @return {boolean} 
 */
export function hasClass(el, name) {
  if (!el) return;
  let className = getClass(el);
  let classes = className.split(' ');
  return classes.indexOf(name) != -1;
}

/**
 * In IE9, setAttribute('class') will result in empty class
 * if the element also has the :class attribute; However in
 * PhantomJS, setting `className` does not work on SVG elements...
 * So we have to do a conditional check here.
 *
 * @param {Element} el
 * @param {String} cls
 */
export function setClass(el, cls) {
  /* istanbul ignore if */
  if (isIE9 && !/svg$/.test(el.namespaceURI)) {
    el.className = cls;
  } else {
    el.setAttribute('class', cls);
  }
}

/**
 * Add class with compatibility for IE & SVG
 *
 * @param {Element} el
 * @param {String} cls
 */
export function addClass(el, cls) {
  if (el.classList) {
    el.classList.add(cls);
  } else {
    let cur = ' ' + getClass(el) + ' ';
    if (cur.indexOf(' ' + cls + ' ') < 0) {
      setClass(el, (cur + cls).trim());
    }
  }
}

/**
 * Remove class with compatibility for IE & SVG
 *
 * @param {Element} el
 * @param {String} cls
 */
export function removeClass(el, cls) {
  if (el.classList) {
    el.classList.remove(cls);
  } else {
    let cur = ' ' + getClass(el) + ' ';
    let tar = ' ' + cls + ' ';
    while (cur.indexOf(tar) >= 0) {
      cur = cur.replace(tar, ' ');
    }
    setClass(el, cur.trim());
  }
  if (!el.className) {
    el.removeAttribute('class');
  }
}

/**
 * 从jquery扣过来的，递归去算
 *
 * @param {Element} a
 * @param {Element} b
 * @return {boolean}
 */
export function contains(a, b) {
  let adown = a.nodeType === 9 ? a.documentElement : a;
        
  let bup = b && b.parentNode;
  return a === bup || !!(bup && bup.nodeType === 1 && adown.contains(bup));
}
