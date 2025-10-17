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
export function isGetterProp(instance, key) {
    if (key in instance) {
        let p = instance;
        let propDesc;
        while (p && !propDesc) {
            propDesc = Object.getOwnPropertyDescriptor(p, key);
            p = Object.getPrototypeOf(p);
        }
        // only getter
        return !!propDesc && propDesc.get && !propDesc.set;
    }
    return false;
}

export function getAllMethodAndProperties(obj) {
    let props = [];

    do {
        const l = Object.getOwnPropertyNames(obj)
            .concat(Object.getOwnPropertySymbols(obj).map((s) => s.toString()))
            .sort()
            .filter(
                (p, i, arr) =>
                    !['constructor', '_globalCtx'].includes(p) && // not the constructor
                    (i === 0 || p !== arr[i - 1]) && // not overriding in this prototype
                    !props.includes(p), // not overridden in a child
            );
        props = props.concat(l);
    } while (
        // eslint-disable-next-line no-cond-assign
        (obj = Object.getPrototypeOf(obj)) && // walk-up the prototype chain
        Object.getPrototypeOf(obj) // not the the Object prototype methods (hasOwnProperty, etc...)
    );

    return props.reverse();
}
