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


/**
 * will cover(会覆盖)
 *
 * @return {Object}
 */
export function merge(...arg) {
  let base = arg[0];
  if (!base) return;
  [].forEach.call(arg, function(item, index) {
    if (index > 0) {
      for (let attrname in item) {
        if (Object.prototype.hasOwnProperty.call(item, attrname)) {
          base[attrname] = item[attrname];
        }
      }
    }
  });
  return base;
}

/**
 * won't overwrite(不会覆盖)
 *
 * @return {Object}
 */
export function extend(...arg) {
  let base = arg[0];
  if (!base) return;
  [].forEach.call(arg, function(item, index) {
    if (index > 0) {
      for (let attrname in item) {
        if (Object.prototype.hasOwnProperty.call(item, attrname)) {
          if (base[attrname] !== undefined) {
            base[attrname] = item[attrname];
          }
        }
      }
    }
  });
  return base;
}
