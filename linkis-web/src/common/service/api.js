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
 * Manipulate APIs(操作Api)
 */
import util from '@/common/util';
import axios from 'axios';
import router from '@/router';
import { Message } from 'iview';
import cache from './apiCache';

// what an array is used to store the cancel function and id for each request(什么一个数组用于存储每个请求的取消函数和标识)
let pending = [];
let cancelConfig = null;
let CancelToken = axios.CancelToken;
let removePending = (config) => {
  for (let p = 0; p < pending.length; p++) {
    const params = JSON.stringify(config.params);
    // Cancel if it exists(如果存在则执行取消操作)
    if (pending[p].u === config.url + '&' + config.method + '&' + params) {
      // pending[p].f();// perform cancellation(执行取消操作)
      pending.splice(p, 1);// remove record(移除记录)
    }
  }
};

let cutReq = (config) => {
  for (let p = 0; p < pending.length; p++) {
    const params = JSON.stringify(config.params);
    if (pending[p].u === config.url + '&' + config.method + '&' + params) {
      return true;
    }
  }
};

const instance = axios.create({
  baseURL: process.env.VUE_APP_MN_CONFIG_PREFIX || `${window.location.protocol}//${window.location.host}/api/rest_j/v1/`,
  timeout: 600000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' },
});

instance.interceptors.request.use((config) => {
  // Add internationalization parameters(增加国际化参数)
  config.headers['Content-language'] = localStorage.getItem('locale') || 'zh-CN';
  let flag = cutReq(config);
  // The second same request cannot be made when the last same request is not completed(当上一次相同请求未完成时，无法进行第二次相同请求)
  if (flag === true) {
    removePending(config);
    return config;
  } else {
    const params = JSON.stringify(config.params);
    // It is used to remove when there is an error in the normal request(用于正常请求出现错误时移除)
    cancelConfig = config;
    config.cancelToken = new CancelToken((c) => {
      // Add identity and cancel functions(添加标识和取消函数)
      pending.push({
        u: config.url + '&' + config.method + '&' + params,
        f: c,
      });
    });
    return config;
  }
}, (error) => {
  Promise.reject(error);
});

instance.interceptors.response.use((response) => {
  // Perform the cancellation operation after an ajax response is successful, and remove the completed request from the pending(在一个ajax响应成功后再执行取消操作，把已完成的请求从pending中移除)
  removePending(response.config);
  return response;
}, (error) => {
  // Judgment when an interface exception or timeout occurs(出现接口异常或者超时时的判断)
  if ((error.message && error.message.indexOf('timeout') >= 0) || (error.request && error.request.status !== 200)) {
    for (let p in pending) {
      if (pending[p].u === cancelConfig.url + '&' + cancelConfig.method + '&' + JSON.stringify(cancelConfig.params)) {
        pending.splice(p, 1);// remove record(移除记录)
      }
    }
    // The error information returned by the background is returned first, followed by the interface return(优先返回后台返回的错误信息，其次是接口返回)
    return error.response || error;
  } else if (axios.Cancel) {
    // If it is in pengding state, a prompt will pop up!(如果是pengding状态，弹出提示！)
    return {
      // data: { message: 'Interface requesting! please wait……' }(data: { message: '接口请求中！请稍后……' })
    };
  } else {
    return error;
  }
});

const api = {
  instance: instance,
  error: {
    '-1': function(res) {
      if (res.data && res.data.enableSSO && res.data.SSOURL) {
        return window.location.replace(res.data.SSOURL);
      }
      router.push('/login');
      throw new Error('您尚未登录，请先登录!');
    },
  },
  constructionOfResponse: {
    codePath: 'status',
    successCode: '0',
    messagePath: 'message',
    resultPath: 'data',
  },
};

const getData = function(data) {
  let _arr = ['codePath', 'messagePath', 'resultPath'];
  let res = {};
  _arr.forEach((item) => {
    let pathArray = api.constructionOfResponse[item].split('.');
    let result = pathArray.length === 1 && pathArray[0] === '*' ? data : data[pathArray[0]];
    for (let j = 1; j < pathArray.length; j++) {
      result = result[pathArray[j]];
      if (!result) {
        if (j < pathArray.length - 1) {
          console.error(`【FEX】ConstructionOfResponse配置错误：${item}拿到的值是undefined，请检查配置`);
        }
        break;
      }
    }
    res[item] = result;
  });
  return res;
};

const success = function(response) {
  if (util.isNull(api.constructionOfResponse.codePath) || util.isNull(api.constructionOfResponse.successCode) ||
        util.isNull(api.constructionOfResponse.messagePath) || util.isNull(api.constructionOfResponse.resultPath)) {
    console.error('【FEX】Api配置错误: 请调用setConstructionOfResponse来设置API的响应结构');
    return;
  }
  let data;
  if (response) {
    if (util.isString(response.data)) {
      data = JSON.parse(response.data);
    } else if (util.isObject(response.data)) {
      data = response.data;
    } else {
      throw new Error('后台接口异常，请联系开发处理！');
    }
    let res = getData(data);
    let code = res.codePath;
    let message = res.messagePath;
    let result = res.resultPath;
    if (code != api.constructionOfResponse.successCode) {
      if (api.error[code]) {
        api.error[code](response);
        throw new Error('');
      } else {
        throw new Error(message || '后台接口异常，请联系开发处理！');
      }
    }
    if (result) {
      let len = 0
      let hasBigData = Object.values(result).some(item=>{
        if (Array.isArray(item)) {
          len = item.length > len ? item.length : len
          return len > 200
        }
      })
      if (hasBigData) {
        console.log(response.data, '潜在性能问题大数据量', len)
      }
    }

    return result || {};
  }
};

const fail = function(error) {
  let _message = '';
  let response = error.response;
  if (response && api.error[response.status]) {
    api.error[response.status].forEach((fn) => fn(response));
  } else {
    _message = '后台接口异常，请联系开发处理！';
    if (response && response.data) {
      let data;
      if (util.isString(response.data)) {
        data = JSON.parse(response.data);
      } else if (util.isObject(response.data)) {
        data = response.data;
      }
      if (data) {
        let res = getData(data);
        _message = res.messagePath;
      }
    }
  }
  error.message = _message;
  throw error;
};

const param = function(url, data, option) {
  let method = 'post';
  if (util.isNull(url)) {
    return console.error('请传入URL');
  } else if (!util.isNull(url) && util.isNull(data) && util.isNull(option)) {
    option = {
      method: method,
    };
  } else if (!util.isNull(url) && !util.isNull(data) && util.isNull(option)) {
    option = {
      method: method,
    };
    if (util.isString(data)) {
      option.method = data;
    } else if (util.isObject(data)) {
      option.data = data;
    }
  } else if (!util.isNull(url) && !util.isNull(data) && !util.isNull(option)) {
    if (!util.isObject(data)) {
      data = {};
    }
    if (util.isString(option)) {
      option = {
        method: option,
      };
    } else if (util.isObject(option)) {
      option.method = option.method || method;
    } else {
      option = {
        method: method,
      };
    }
    if (option.method == 'get' || option.method == 'delete' || option.method == 'head' || option.method == 'options') {
      option.params = data;
    }
    if (option.method == 'post' || option.method == 'put' || option.method == 'patch') {
      option.data = data;
    }
  }
  // cacheOptions interface data cache {time} When the time is 0, the data cached in the memory will not be cleaned up after the request(cacheOptions接口数据缓存 {time} time为0则请求之后缓存在内存里的数据不清理)
  if (option.cacheOptions) {
    option.adapter = cache(option.cacheOptions)
  }
  option.url = url;

  return instance.request(option);
};

const action = function(url, data, option) {
  return param(url, data, option)
    .then(success, fail)
    .then(function(response) {
      return response;
    })
    .catch(function(error) {
      error.message && Message.error(error.message);
      throw error;
    });
};

api.fetch = action;

api.option = function(option) {
  if (option.root) {
    instance.defaults.baseURL = option.root;
  }
  if (option.timeout && util.isNumber(option.timeout)) {
    instance.defaults.timeout = option.timeout;
  }
  if (option.config && util.isObject(option.config)) {
    Object.keys(option.config).forEach(function(key) {
      instance.defaults[key] = option.config[key];
    });
  }
};

api.setError = function(option) {
  if (option && util.isObject(option)) {
    util.merge(api.error, option);
  }
};

api.setResponse = function(constructionOfResponse) {
  this.constructionOfResponse = constructionOfResponse;
};

export default api;
