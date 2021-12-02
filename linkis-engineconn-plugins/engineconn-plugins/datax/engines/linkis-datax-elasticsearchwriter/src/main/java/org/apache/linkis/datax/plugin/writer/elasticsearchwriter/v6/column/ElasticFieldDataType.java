/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6.column;

/**
 * @author davidhua
 * 2019/8/15
 */
public enum ElasticFieldDataType {
    /**
     * type:text
     */
    TEXT,
    /**
     * object
     */
    OBJECT,
    /**
     * type:long(numeric)
     */
    LONG,
    /**
     * type:integer(numeric)
     */
    INTEGER,
    /**
     * type:short(numeric)
     */
    SHORT,
    /**
     * type:byte(numeric)
     */
    BYTE,
    /**
     * type:double(numeric)
     */
    DOUBLE,
    /**
     * type:float(numeric)
     */
    FLOAT,
    /**
     * type:half_float(numeric)
     */
    HALF_FLOAT,
    /**
     * type:scaled_float(numeric)
     */
    SCALED_FLOAT,
    /**
     * type:alias, alternate name for a field
     */
    ALIAS,
    /**
     * type:binary
     */
    BINARY,
    /**
     * type:boolean
     */
    BOOLEAN,
    /**
     * type:date
     */
    DATE,
    /**
     * type:geo_point
     */
    GEO_POINT,
    /**
     * type:geo_shape
     */
    GEO_SHAPE,
    /**
     * type:integer_range, 32-bits
     */
    INTEGER_RANGE,
    /**
     * type:ip
     */
    IP,
    /**
     * type:keyword
     */
    KEYWORD,
    /**
     * type:nested
     */
    NESTED,
    /**
     * type:float_range, 32-bits IEEE 754
     */
    FLOAT_RANGE,
    /**
     * type:long_range, 64-bits
     */
    LONG_RANGE,
    /**
     * type:double_range, 64-bits IEEE 754
     */
    DOUBLE_RANGE,
    /**
     * type:date_range, unsigned 64-bit integer milliseconds
     */
    DATE_RANGE,
    /**
     * type:ip_range, IPv4 or IPv6
     */
    IP_RANGE
}
