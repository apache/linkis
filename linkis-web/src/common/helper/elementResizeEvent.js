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


let requestFrame = (function() {
  let raf = window.requestAnimationFrame ||
      window.mozRequestAnimationFrame ||
      window.webkitRequestAnimationFrame ||
      function fallbackRAF(func) {
        return window.setTimeout(func, 20);
      };
  return function requestFrameFunction(func) {
    return raf(func);
  };
})();

let cancelFrame = (function() {
  let cancel = window.cancelAnimationFrame ||
      window.mozCancelAnimationFrame ||
      window.webkitCancelAnimationFrame ||
      window.clearTimeout;
  return function cancelFrameFunction(id) {
    return cancel(id);
  };
})();

/**
 *Monitor size changes(监听大小改变)
 * @param {*} e
 */
function resizeListener(e) {
  let win = e.target || e.srcElement;
  if (win.__resizeRAF__) {
    cancelFrame(win.__resizeRAF__);
  }
  win.__resizeRAF__ = requestFrame(function() {
    let trigger = win.__resizeTrigger__;
    let listeners = trigger && trigger.__resizeListeners__;
    if (listeners) {
      listeners.forEach(function(fn) {
        fn.call(trigger, e);
      });
    }
  });
}

let bind = function(element, fn) {
  let document = window.document;
  let attachEvent = document.attachEvent;

  /**
     *
     */
  function objectLoad() {
    this.contentDocument.defaultView.__resizeTrigger__ = this.__resizeElement__;
    this.contentDocument.defaultView.addEventListener('resize', resizeListener);
    this.contentDocument.defaultView.dispatchEvent(new Event('resize'));
  }

  if (!element.__resizeListeners__) {
    element.__resizeListeners__ = [];
    if (attachEvent) {
      element.__resizeTrigger__ = element;
      element.attachEvent('onresize', resizeListener);
    } else {
      if (getComputedStyle(element).position === 'static') {
        element.style.position = 'relative';
      }
      let obj = (element.__resizeTrigger__ = document.createElement('object'));
      obj.setAttribute(
        'style',
        'display: block; position: absolute; top: 0; left: 0; height: 100%; width: 100%; overflow: hidden; pointer-events: none; z-index: -1; opacity: 0;'
      );
      obj.setAttribute('class', 'resize-sensor');
      obj.__resizeElement__ = element;
      obj.onload = objectLoad;
      obj.type = 'text/html';
      obj.data = 'about:blank';
      element.appendChild(obj);
    }
  }
  element.__resizeListeners__.push(fn);
};

let unbind = function(element, fn) {
  let attachEvent = document.attachEvent;
  let listeners = element.__resizeListeners__ || [];
  if (fn) {
    let index = listeners.indexOf(fn);
    if (index !== -1) {
      listeners.splice(index, 1);
    }
  } else {
    listeners = element.__resizeListeners__ = [];
  }
  if (!listeners.length) {
    if (attachEvent) {
      element.detachEvent('onresize', resizeListener);
    } else if (element.__resizeTrigger__) {
      if (element.__resizeTrigger__.contentDocument) {
        element.__resizeTrigger__.contentDocument.defaultView.removeEventListener(
          'resize',
          resizeListener
        );
        delete element.__resizeTrigger__.contentDocument.defaultView.__resizeTrigger__;
      }
      element.__resizeTrigger__ = !element.removeChild(
        element.__resizeTrigger__
      );
    }
    delete element.__resizeListeners__;
  }
};

export default {
  bind: typeof window === 'undefined' ? bind : bind.bind(window),
  unbind,
  requestAnimationFrame: requestFrame,
  cancelAnimationFrame: cancelFrame
};
