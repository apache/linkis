import { reactive, computed } from 'vue';
import { isPlainObject } from 'lodash-es';
import dayjs from 'dayjs';
import _ from 'lodash';
import { createGlobalState } from '@vueuse/core';
import { useRouter, useRoute } from '@fesjs/fes';
import { FMessage, FModal } from '@fesjs/fes-design';

function useLetgoGlobal() {
    const router = useRouter();
    const route = useRoute();

    const $context = reactive({});
    const letgoContext = $context;

    $context.userInfo = {
        user: '',
        uriList: [],
        roleList: [],
    };
    $context.publicPath = process.env.BASE_URL;
    $context.urlParams = computed(() => route.query || {});
    $context.navigateTo = (routeName, params) => {
        if (params && isPlainObject(params)) {
            router.push({
                name: routeName,
                query: params,
            });
        } else {
            router.push({
                name: routeName,
            });
        }
    };
    $context.navigateBack = (routeName, params) => {
        router.back();
    };

    const $utils = {
        FMessage,
        FModal,
        dayjs,
        _,
    };
    const utils = $utils;

    const __globalCtx = {
        $context,
        letgoContext,
        $utils,
        utils,
    };

    const __query_deps = {};

    return new Proxy(__globalCtx, {
        get(obj, prop) {
            if ([].includes(prop) && !__globalCtx[prop].hasBeenCalled)
                __globalCtx[prop].trigger();

            const currentQuery = __query_deps[prop];
            if (currentQuery) {
                currentQuery.forEach((d) => {
                    if (
                        __globalCtx[d].runWhenPageLoads &&
                        !__globalCtx[d].hasBeenCalled
                    ) {
                        __globalCtx[d].trigger();
                    }
                });
            }

            return obj[prop];
        },
    });
}

export const useSharedLetgoGlobal = createGlobalState(useLetgoGlobal);
