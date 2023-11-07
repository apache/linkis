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

/*eslint-disable */
import dayjs from 'dayjs';
import { isPlainObject, forEach, forIn } from 'lodash';
/**
 *
 * conversion log(转换日志)
 *
 * @return {Object}
 */
function convertLog(logs: string | Array<string>) {
    let logMap = {
        all: '',
        error: '',
        warning: '',
        info: '',
    };
    let newMap: Record<string, string> = {};
    if (typeof logs === 'string') {
        newMap = {
            all: logs,
        };
    } else if (Array.isArray(logs)) {
        let keysArr = ['error', 'warning', 'info', 'all'];
        logs.forEach((log, index) => {
            newMap[keysArr[index] as string] = log;
        });
    } else if (isPlainObject(logs)) {
        newMap = logs;
    }

    return Object.assign(logMap, newMap);
}

/**
 * Convert Timestamp Difference(转换时间戳差值)
 * @param {*} runningTime
 * @return {*}
 */
function convertTimestamp(runningTime: number, t: (msg: string) => string) {
    // const time = Math.floor(runningTime / 1000);
    const time = +(runningTime / 1000).toFixed(1);
    if (time <= 0) {
        return `0${t('message.common.time.second')}`;
    } else if (time < 60) {
        return `${time}${t('message.common.time.second')}`;
    } else if (time < 3600) {
        return `${(time / 60).toFixed(2)}${t('message.common.time.minute')}`;
    } else if (time < 86400) {
        return `${(time / 3600).toFixed(2)}${t('message.common.time.hour')}`;
    }
    return `${(time / 86400).toFixed(2)}${t('message.common.time.day')}`;
}

/**
 * sort(排序)
 * @param {*} a first parameter(第一个参数)
 * @param {*} b second parameter(第两个参数)
 * @param {*} type ,possibly desc and asc(类型，可能是desc和asc)
 */
function sort(a: any, b: any, type: 'desc' | unknown) {
    const sortString = (a: string, b: string, type: 'desc' | unknown) => {
        for (let i = 0; i < a.length; i++) {
            if (a[i] !== b[i]) {
                const aAcsii = a.charCodeAt(i);
                const bAcsii = b.charCodeAt(i);
                const returnS =
                    type === 'desc' ? bAcsii - aAcsii : aAcsii - bAcsii;
                return returnS;
            }
        }
    };
    const fa = parseInt(a, 10);
    const fb = parseInt(b, 10);
    if (!isNaN(fa) && !isNaN(fb)) {
        if (
            fa.toString().length === a.toString().length &&
            fb.toString().length === b.toString().length
        ) {
            return type === 'desc' ? b - a : a - b;
        } else if (!isNaN(Number(a)) && !isNaN(Number(b))) {
            return type === 'desc' ? b - a : a - b;
        } else {
            return sortString(a, b, type);
        }
    } else {
        return sortString(a, b, type);
    }
}

/**
 * Convert array to object key:value form(转换数组为对象key:value形式)
 * @param {*} arr
 * @return {*}
 */
function convertArrayToObject(arr: any[]) {
    const obj: Record<string, any> = {};
    forEach(arr, (item: { key: string; value: any }) => {
        obj[item.key] = item.value;
    });
    return obj;
}

/**
 * Convert object to array form(转换对象为数组形式)
 * @param {*} obj
 */
function convertObjectToArray(obj: Record<string, string>) {
    const arr: any[] = [];
    forIn(obj, (value, key) => {
        arr.push({
            key,
            value,
        });
    });
    return arr;
}

/**
 * Convert the array to an array of the form [{key1:value1},{key2,value2}](转换数组为数组[{key1:value1},{key2,value2}]形式)
 * @param {*} arr
 * @return {*}
 */
function convertArrayToMap(arr: any[]) {
    const tmp: any[] = [];
    forEach(arr, (item: { key: string; value: any }) => {
        const obj: Record<string, any> = {};
        obj[item.key] = item.value;
        tmp.push(obj);
    });
    return tmp;
}
const convertList = {
    lifecycle: [
        {
            value: 0,
            label: '永久',
        },
        {
            value: 1,
            label: '当天有效',
        },
        {
            value: 2,
            label: '一周有效',
        },
        {
            value: 3,
            label: '一月有效',
        },
        {
            value: 4,
            label: '半年有效',
        },
    ],
    modelLevel: [
        {
            value: 0,
            label: 'ODS(原始数据层)',
        },
        {
            value: 1,
            label: 'DWD(明细数据层)',
        },
        {
            value: 2,
            label: 'DWS(汇总数据层)',
        },
        {
            value: 3,
            label: 'ADS(应用数据层)',
        },
    ],
    useWay: [
        {
            value: 0,
            label: '一次写多次读',
        },
        {
            value: 1,
            label: '增删改查',
        },
        {
            value: 2,
            label: '多次覆盖写',
        },
        {
            value: 3,
            label: '一次写偶尔读',
        },
    ],
};

/**
 * format value(格式化值)
 * @param {*} item
 * @param {*} field
 * @return {*} return
 */
function formatValue(item: any, field: { key: string; type: string }) {
    const value = item[field.key];
    let formatted = value;
    switch (field.type) {
        case 'boolean':
            formatted = value ? '是' : '否';
            break;
        case 'timestramp':
            formatted =
                value == '0' || !value
                    ? 0
                    : dayjs.unix(value).format('YYYY-MM-DD HH:mm:ss');
            break;
        case 'convert':
            if (!item[field.key] && item[field.key] !== 0) {
                return value;
            }
            const convertItem = (convertList as Record<string, any>)[field.key];
            formatted = convertItem[item[field.key]].label;
            break;
        case 'booleanString':
            formatted = value === 'Y' ? '是' : '否';
            break;
    }
    return formatted;
}

export default {
    formatValue,
    convertArrayToMap,
    convertObjectToArray,
    convertArrayToObject,
    sort,
    convertTimestamp,
    convertLog,
};
