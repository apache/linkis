import { ApplyPluginsType, plugin } from '@fesjs/fes';
import { ref, shallowRef } from 'vue';

import { createRequest } from '@qlin/request';

function getRequestInstance() {
    const defaultConfig = plugin.applyPlugins({
        key: 'request',
        type: ApplyPluginsType.modify,
        initialValue: {
            timeout: 10000,
        },
    });

    return createRequest(defaultConfig);
}

let currentRequest;

export function rawRequest(url, data, options = {}) {
    if (typeof options === 'string') {
        options = {
            method: options,
        };
    }
    if (!currentRequest) {
        currentRequest = getRequestInstance();
    }
    return currentRequest(url, data, options);
}

export async function request(url, data, options = {}) {
    const response = await rawRequest(url, data, options);
    return response.data;
}

request.version = '4.0.0';

function isPromiseLike(obj) {
    return !!obj && typeof obj === 'object' && typeof obj.then === 'function';
}

export function useRequest(url, data, options = {}) {
    const loadingRef = ref(true);
    const errorRef = ref(null);
    const dataRef = shallowRef(null);
    let promise;
    if (isPromiseLike(url)) {
        promise = url;
    }
    else {
        promise = request(url, data, options);
    }
    promise
        .then((res) => {
            dataRef.value = res;
        })
        .catch((error) => {
            errorRef.value = error;
        })
        .finally(() => {
            loadingRef.value = false;
        });
    return {
        loading: loadingRef,
        error: errorRef,
        data: dataRef,
    };
}
