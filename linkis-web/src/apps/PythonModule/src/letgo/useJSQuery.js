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
