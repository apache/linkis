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
import { isPlainObject } from 'lodash-es';
import { markReactive } from './reactive';
import { IEnumRunCondition, IEnumCacheType } from './letgoConstants';
import { cacheControl, clearCache } from './cache-control';

class JSQuery {
    constructor(data) {
        markReactive(this, {
            id: data.id,
            query: data.query,
            params: data.params,

            enableCaching: data.enableCaching || false,
            cacheDuration: data.cacheDuration || null,
            cacheType: data.cacheType || IEnumCacheType.RAM,

            enableTransformer: data.enableTransformer || false,
            transformer: data.transformer,
            runWhenPageLoads: data.runWhenPageLoads || false,
            queryTimeout: data.queryTimeout,
            runCondition: data.runCondition || IEnumRunCondition.MANUAL,
            successEvent: data.successEvent || [],
            failureEvent: data.failureEvent || [],
            data: null,
            response: null,
            error: null,
            loading: false,
        });

        this.hasBeenCalled = false;
    }

    timeoutPromise(timeout) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                reject({
                    type: 'TIMEOUT',
                    msg: '请求超时',
                });
            }, timeout);
        });
    }

    formatParams(extraParams) {
        const params = this.params;
        if (!params) {
            return extraParams || null;
        }

        if (isPlainObject(params) && isPlainObject(extraParams))
            return { ...params, ...extraParams };

        return params;
    }

    async trigger(extraParams) {
        if (this.query) {
            try {
                this.hasBeenCalled = true;
                this.loading = true;
                const params = this.formatParams(extraParams);
                const response = await cacheControl(
                    {
                        id: this.id,
                        enableCaching: this.enableCaching,
                        cacheDuration: this.cacheDuration,
                        type: this.cacheType,
                        params,
                    },
                    async () => {
                        if (this.queryTimeout)
                            return Promise.race([
                                this.timeoutPromise(this.queryTimeout),
                                this.query(params),
                            ]);

                        return this.query(params);
                    },
                );

                this.response = response;
                let data = response?.data;
                if (this.enableTransformer && this.transformer)
                    data = await this.transformer(data);

                this.data = data;
                this.error = null;
                this.successEvent.forEach((eventHandler) => {
                    eventHandler(data);
                });
                return this.data;
            } catch (err) {
                this.data = null;
                this.failureEvent.forEach((eventHandler) => {
                    eventHandler(err);
                });
                if (err instanceof Error) this.error = err.message;

                window.console.warn(err);
                throw err;
            } finally {
                this.loading = false;
            }
        }
    }

    clearCache() {
        clearCache(this.id, this.cacheType);
    }

    reset() {
        this.clearCache();
        this.data = null;
        this.error = null;
    }
}

export function useJSQuery(data) {
    const result = new JSQuery(data);

    if (!data._isGlobalQuery && data.runWhenPageLoads) result.trigger();

    return result;
}
