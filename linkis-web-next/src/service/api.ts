/* eslint-disable */
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
import { FMessage } from '@fesjs/fes-design';
import axios from 'axios';
import { h } from 'vue';
import util from '@/util';
import cache from './apiCache';

// what an array is used to store the cancel function and id for each request(一个用于存储每个请求的取消函数和标识的数组数组)
const pending: Array<{ u: string; f: (message?: string) => unknown }> = [];
let cancelConfig: any = null;
const { CancelToken } = axios;
const removePending = (config: Record<string, any>) => {
    for (let p = 0; p < pending.length; p++) {
        const params = JSON.stringify(config.params);
        if (pending[p].u === `${config.url}&${config.method}&${params}`) {
            pending.splice(p, 1); // remove record(移除记录)
        }
    }
};

const cutReq = (config: Record<string, any>) => {
    for (let p = 0; p < pending.length; p++) {
        const params = JSON.stringify(config.params);
        if (pending[p].u === `${config.url}&${config.method}&${params}`) {
            return true;
        }
    }
};

const instance = axios.create({
    baseURL:
        import.meta.env.VUE_APP_MN_CONFIG_PREFIX ||
        `${window.location.protocol}//${window.location.host}/api/rest_j/v1/`,
    timeout: 600000,
    withCredentials: true,
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
});

instance.interceptors.request.use(
    (config) => {
        // Add internationalization parameters(增加国际化参数)
        config.headers['Content-language'] =
            localStorage.getItem('locale') || 'zh-CN';
        const flag = cutReq(config);
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
                    u: `${config.url}&${config.method}&${params}`,
                    f: c,
                });
            });
            return config;
        }
    },
    (error) => {
        Promise.reject(error);
    },
);

instance.interceptors.response.use(
    (response) => {
        // Perform the cancellation operation after an ajax response is successful, and remove the completed request from the pending(在一个ajax响应成功后再执行取消操作，把已完成的请求从pending中移除)
        removePending(response.config);
        return response;
    },
    (error) => {
        // Judgment when an interface exception or timeout occurs(出现接口异常或者超时时的判断)
        if (
            (error.message && error.message.indexOf('timeout') >= 0) ||
            (error.request && error.request.status !== 200)
        ) {
            for (let p = 0; p < pending.length; p++) {
                if (
                    pending[p].u ===
                    `${cancelConfig.url}&${
                        cancelConfig.method
                    }&${JSON.stringify(cancelConfig.params)}`
                ) {
                    pending.splice(p, 1); // remove record(移除记录)
                }
            }
            // The error information returned by the background is returned first, followed by the interface return(优先返回后台返回的错误信息，其次是接口返回)
            return error.response || error;
        } else if (axios.Cancel) {
            return {};
        } else {
            return error;
        }
    },
);

interface Response {
    data: {
        enableSSO: boolean;
        SSOURL: string | URL;
    };
    config: {
        url: string | URL;
        data: string;
    };
}

interface API {
    instance: typeof instance;
    error: Record<
        string,
        Array<(res: Response, ...args: unknown[]) => unknown | void> | ((res: Response, ...args: unknown[]) => unknown | void)
    >;
    constructionOfResponse: {
        codePath: string;
        successCode: string;
        messagePath: string;
        resultPath: string;
    };
    fetch?: any;
    option?: any;
    setError?: any;
    setResponse?: any;
}

const api: API = {
    instance,
    error: {
        '-1': function (res: Response) {
            if (res.data && res.data.enableSSO && res.data.SSOURL) {
                return window.location.replace(res.data.SSOURL);
            }

            const isLoginPath = window.location.hash === '#/login';
            if (!isLoginPath) {
                window.location.hash = '#/login';
                throw new Error('您尚未登录，请先登录!');
            }
        },
    },
    constructionOfResponse: {
        codePath: 'status',
        successCode: '0',
        messagePath: 'message',
        resultPath: 'data',
    },
};

const getData = function (data: any) {
    const _arr = ['codePath', 'messagePath', 'resultPath'];
    const res: {[key: string]: any} = {};
    _arr.forEach((item) => {
        const pathArray = (api.constructionOfResponse as {[key: string]: any})[item].split('.');
        let result =
            pathArray.length === 1 && pathArray[0] === '*'
                ? data
                : data[pathArray[0]];
        for (let j = 1; j < pathArray.length; j++) {
            result = result[pathArray[j]];
            if (!result) {
                if (j < pathArray.length - 1) {
                    window.console.error(
                        `【FEX】ConstructionOfResponse配置错误：${item}拿到的值是undefined，请检查配置`,
                    );
                }
                break;
            }
        }
        res[item] = result;
    });
    return res;
};

const success = function (response: any) {
    if (
        util.isNull(api.constructionOfResponse.codePath) ||
        util.isNull(api.constructionOfResponse.successCode) ||
        util.isNull(api.constructionOfResponse.messagePath) ||
        util.isNull(api.constructionOfResponse.resultPath)
    ) {
        window.console.error(
            '【FEX】Api配置错误: 请调用setConstructionOfResponse来设置API的响应结构',
        );
        return;
    }
    let data;
    if (response) {
        const linkis_errorMsgTip = (
            sessionStorage.getItem('linkis.errorMsgTip') || ''
        ).replace(/%s/g, response.config.url);
        if (util.isString(response.data)) {
            data = JSON.parse(response.data);
        } else if (util.isObject(response.data)) {
            data = response.data;
        } else {
            throw new Error(linkis_errorMsgTip);
        }
        const res: any = getData(data);
        const code = res.codePath;
        const message = res.messagePath;
        const result = res.resultPath;
        const errorMsgTip = result ? result.errorMsgTip : '';
        if (errorMsgTip) {
            sessionStorage.setItem('linkis.errorMsgTip', errorMsgTip);
        }
        if (code != api.constructionOfResponse.successCode) {
            if (typeof api.error[code] === 'function') {
                api.error[code](response);
                throw new Error('');
            } else {
                throw new Error(message || linkis_errorMsgTip);
            }
        }
        if (result) {
            let len = 0;
            const hasBigData = Object.values(result).some((item) => {
                if (Array.isArray(item)) {
                    len = item.length > len ? item.length : len;
                    return len > 200;
                }
            });
            if (hasBigData) {
                window.console.log(response.data, '潜在性能问题大数据量', len);
            }
        }
        return result || {};
    }
};

const fail = function (error: any) {
    let _message = '';
    const { response } = error;
    if (response && Array.isArray(api.error[response.status])) {
        (api.error[response.status] as Array<(res: Response, ...args: unknown[]) => unknown | void>)
            .forEach((fn) => fn(response));
    } else {
        if (response && response.config)
            _message =
                (sessionStorage.getItem('linkis.errorMsgTip') || '').replace(
                    /%s/g,
                    response.config.url,
                );
        if (response && response.data) {
            let data;
            if (util.isString(response.data)) {
                data = JSON.parse(response.data);
            } else if (util.isObject(response.data)) {
                data = response.data;
            }
            if (data) {
                const res = getData(data);
                _message = res.messagePath;
            }
        }
    }
    error.message = _message;
    throw error;
};

const param = function (url: string | URL, data: any, option: any) {
    const method = 'post';
    if (util.isNull(url)) {
        return window.console.error('请传入URL');
    } else if (!util.isNull(url) && util.isNull(data) && util.isNull(option)) {
        option = {
            method,
        };
    } else if (!util.isNull(url) && !util.isNull(data) && util.isNull(option)) {
        option = {
            method,
        };
        if (util.isString(data)) {
            option.method = data;
        } else if (util.isObject(data)) {
            option.data = data;
        }
    } else if (
        !util.isNull(url) &&
        !util.isNull(data) &&
        !util.isNull(option)
    ) {
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
                method,
            };
        }
        if (
            option.method === 'get' ||
            option.method === 'delete' ||
            option.method === 'head' ||
            option.method === 'options'
        ) {
            option.params = data;
        }
        if (
            option.method === 'post' ||
            option.method === 'put' ||
            option.method === 'patch'
        ) {
            option.data = data;
        }
    }
    // cacheOptions interface data cache {time} When the time is 0, the data cached in the memory will not be cleaned up after the request(cacheOptions接口数据缓存 {time} time为0则请求之后缓存在内存里的数据不清理)
    if (option.cacheOptions) {
        option.adapter = cache(option.cacheOptions);
    }
    option.url = url;

    return instance.request(option);
};

const action = function (url: string | URL, data: any, option: any) {
    return param(url, data, option)
        ?.then(success, fail)
        .then((response: unknown) => response)
        .catch((error: Error) => {
            if (error.message) {
                const urlReg =
                    /(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?/g;
                const result = error.message.match(urlReg);
                result
                    ? FMessage.error({
                        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                        // @ts-ignore
                        content: () => {
                            const context = error.message.split(result[0]);
                            return h('span', [
                                context[0],
                                h(
                                    'a',
                                    {
                                        domProps: {
                                            href: result?.[0],
                                            target: '_blank',
                                        },
                                    },
                                    result?.[0],
                                ),
                                context[1],
                            ]);
                        },
                      })
                    : FMessage.error({
                          duration: 1.5,
                          content: error.message,
                      });
            }
            throw error;
        });
};

api.fetch = action;

api.option = function (option: Record<string, any>) {
    if (option?.root) {
        instance.defaults.baseURL = option.root;
    }
    if (option.timeout && util.isNumber(option.timeout)) {
        instance.defaults.timeout = option.timeout;
    }
    if (option.config && util.isObject(option.config)) {
        Object.keys(option.config).forEach((key) => {
            (instance.defaults as { [key: string]: any })[key] = option.config[key];
        });
    }
};

api.setError = function (option: unknown) {
    if (option && util.isObject(option)) {
        util.merge(api.error, option);
    }
};

api.setResponse = function (constructionOfResponse: any) {
    this.constructionOfResponse = constructionOfResponse;
};

export default api;
