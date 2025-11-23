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
import { computed, reactive, shallowReactive } from 'vue';
import { getAllMethodAndProperties } from './shared.js';

export function markClassReactive(target, filter) {
    const members = getAllMethodAndProperties(target).filter((member) => {
        if (filter) return filter(member);

        return true;
    });

    const state = reactive(
        members.reduce((acc, cur) => {
            acc[cur] = target[cur];
            return acc;
        }, {}),
    );
    members.forEach((key) => {
        Object.defineProperty(target, key, {
            get() {
                return state[key];
            },
            set(value) {
                state[key] = value;
            },
        });
    });
    return target;
}

export function markShallowReactive(target, properties) {
    const state = shallowReactive(properties);
    Object.keys(properties).forEach((key) => {
        Object.defineProperty(target, key, {
            get() {
                return state[key];
            },
            set(value) {
                state[key] = value;
            },
        });
    });
    return target;
}

export function markReactive(target, properties) {
    const state = reactive(properties);
    Object.keys(properties).forEach((key) => {
        Object.defineProperty(target, key, {
            get() {
                return state[key];
            },
            set(value) {
                state[key] = value;
            },
        });
    });
    return target;
}

export function markComputed(target, properties) {
    const prototype = Object.getPrototypeOf(target);
    properties.forEach((key) => {
        const descriptor = Object.getOwnPropertyDescriptor(prototype, key);
        if (descriptor?.get) {
            const tmp = computed(descriptor.get.bind(target));
            Object.defineProperty(target, key, {
                get() {
                    return tmp.value;
                },
            });
        }
    });
    return target;
}
