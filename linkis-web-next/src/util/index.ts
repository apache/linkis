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

import qs from 'qs';
import md5 from 'md5';
import objectUtil from './object';
import typeUtil from './type';
import convertUtil from './convert';
// import currentModules from './currentModules';

let util = {
    executeCopy(textValue: string) {
        const input = document.createElement('textarea');
        document.body.appendChild(input);
        input.value = textValue;
        input.select();
        document.execCommand('Copy');
        input.remove();
    },
    md5,
    /**
     * Replace the parameter placeholders in the format of ${projectId} in the url with real parameters(替换url中 形如 ${projectId} 格式的参数占位符为真实参数)
     * ! url should be an escaped link address that conforms to the URI specification. If the parameter is not defined in obj, the final address will lose the parameter(url应该是转义过的符合URI规范的链接地址，参数如未在obj定义则最终地址会丢失该参数)
     */
    replaceHolder(url: string, obj: Record<string, any>) {
        obj = {
            dssurl: location.origin,
            cookies: document.cookie,
            ...obj,
        };
        let dist = url.split('?');
        let params = qs.parse(dist[1]);
        const holderReg = /\$\{([^}]*)}/g;
        let result: Record<string, any> = {};
        dist[0] = dist[0].replace(holderReg, function (a: unknown, b: string) {
            return obj[b];
        });
        if (dist[1]) {
            for (let key in params) {
                const resKey = key.replace(holderReg, function (a, b) {
                    return obj[b];
                });
                result[resKey] = (params[key] as string)?.replace(
                    holderReg,
                    function (a: unknown, b: string) {
                        return obj[b];
                    },
                );
            }
        }
        const distUrl =
            dist.length > 1 ? `${dist[0]}?${qs.stringify(result)}` : dist[0];
        return distUrl;
    },
    // How to open a new tab browser(打开新tab浏览器也方法)
    windowOpen(url: string) {
        const newTab = window.open('about:blank');
        setTimeout(() => {
            const reg =
                /^(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&:/~\+#]*[\w\-\@?^=%&/~\+#])?/;
            if (process.env.NODE_ENV === 'production') {
                if (newTab && reg.test(url)) {
                    newTab.location.href = url;
                }
            } else {
                if (newTab) {
                    newTab.location.href = url;
                }
            }
        }, 500);
    },
    /**
     * generate guid(生成guid)
     */
    guid() {
        let key;
        if (crypto && crypto.getRandomValues) {
            key = (([1e7] as any) + -1e3 + -4e3 + -8e3 + -1e11).replace(
                /[018]/g,
                (c: any) =>
                    (
                        c ^
                        (crypto.getRandomValues(new Uint8Array(1))[0] &
                            (15 >> (c / 4)))
                    ).toString(16),
            );
        } else {
            key = `${new Date().getTime()}.${Math.ceil(Math.random() * 1000)}`;
        }
        return key;
    },
};
objectUtil.merge(util, objectUtil, typeUtil, convertUtil);

export default util as typeof util &
    typeof objectUtil &
    typeof typeUtil &
    typeof convertUtil;
// typeof currentModules;
