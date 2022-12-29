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

import axios from 'axios'

// data storage(数据存储)
export const cache = {
  data: {},
  set(key, data) {
    this.data[key] = data
  },
  get(key) {
    return this.data[key]
  },
  clear(key) {
    delete this.data[key]
  }
}

// Create a unique key value(建立唯一的key值)
export const buildUniqueUrl = (url, method, params = {}, data = {}) => {
  const paramStr = (obj) => {
    if (toString.call(obj) === '[object Object]') {
      return JSON.stringify(Object.keys(obj).sort().reduce((result, key) => {
        result[key] = obj[key]
        return result
      }, {}))
    } else {
      return JSON.stringify(obj)
    }
  }
  url += `?${paramStr(params)}&${paramStr(data)}&${method}`
  return url
}

// prevent duplicate requests(防止重复请求)
export default (options = {}) => async config => {
  const defaultOptions = {
    time: 0, // Set to 0, do not clear the cache(设置为0，不清除缓存)
    ...options
  }
  const index = buildUniqueUrl(config.url, config.method, config.params, config.data)
  let responsePromise = cache.get(index)
  if (!responsePromise) {
    responsePromise = (async () => {
      try {
        const response = await axios.defaults.adapter(config)
        return Promise.resolve(response)
      } catch (reason) {
        cache.clear(index)
        return Promise.reject(reason)
      }
    })()
    cache.set(index, responsePromise)
    if (defaultOptions.time !== 0) {
      setTimeout(() => {
        cache.clear(index)
      }, defaultOptions.time)
    }
  }
  return responsePromise.then(data => JSON.parse(JSON.stringify(data))) // To prevent data source pollution(为防止数据源污染)
}
