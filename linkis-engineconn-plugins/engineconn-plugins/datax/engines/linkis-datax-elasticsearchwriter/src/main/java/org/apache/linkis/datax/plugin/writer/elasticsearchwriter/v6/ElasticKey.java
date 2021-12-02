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

package org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6;

/**
 * @author davidhua
 * 2019/8/12
 */
public final class ElasticKey {
    /**
     * endPoints
     */
    static final String ENDPOINTS = "elasticUrls";
    /**
     * username
     */
    static final String USERNAME = "username";
    /**
     * password
     */
    static final String PASSWORD = "password";
    /**
     * index(index name)
     */
    static final String INDEX_NAME = "index";
    /**
     * type(index type)
     */
    static final String INDEX_TYPE = "type";
    /**
     * column(props column)
     */
    static final String PROPS_COLUMN = "column";
    /**
     * column-> {name:'xx'}
     */
    static final String PROPS_COLUMN_NAME = "name";
    /**
     * column-> {type:'xxx'}
     */
    static final String PROPS_COLUMN_TYPE = "type";
    /**
     * column-> {timezone:'xxx'}
     */
    static final String PROPS_COLUMN_TIMEZONE = "timezone";
    /**
     * format-> {format: 'format'}
     */
    static final String PROPS_COLUMN_FORMAT = "format";
    /**
     * cleanUp
     */
    static final String CLEANUP = "cleanUp";
    /**
     * settings(index settings)
     */
    static final String SETTINGS = "settings";
    /**
     * clientConfig
     */
    static final String CLIENT_CONFIG = "clientConfig";
    /**
     * clientConfig -> maxPoolSize
     */
    static final String CLIENT_CONFIG_POOL_SIZE = "maxPoolSize";
    /**
     * clientConfig -> sockTimeout
     */
    static final String CLIENT_CONFIG_SOCKET_TIMEOUT = "sockTimeout";
    /**
     * clientConfig -> connTimeout
     */
    static final String CLIENT_CONFIG_CONN_TIMEOUT = "connTimeout";
    /**
     * clientConfig -> timeout
     */
    static final String CLIENT_CONFIG_REQ_TIMEOUT = "timeout";
    /**
     * clientConfig -> masterTimeout
     */
    static final String CLIENT_CONFIG_MASTER_TIMEOUT = "masterTimeout";
    /**
     * bulkPerTask
     */
    static final String BULK_PER_TASK = "bulkPerTask";
    /**
     * bulkActions
     */
    static final String BULK_ACTIONS = "bulkActions";

    /**
     * To split multiple level properties name
     */
    static final String COLUMN_NAME_SEPARATOR = "columnNameSeparator";
}
