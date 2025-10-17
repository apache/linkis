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
