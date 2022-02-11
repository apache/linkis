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

package org.apache.linkis.metadatamanager.server.restful;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.metadatamanager.common.domain.MetaColumnInfo;
import org.apache.linkis.metadatamanager.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadatamanager.common.exception.MetaMethodInvokeException;
import org.apache.linkis.metadatamanager.server.service.MetadataAppService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(
        value = "/metadatamanager",
        produces = {"application/json"})
public class MetadataCoreRestful {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataCoreRestful.class);

    @Autowired private MetadataAppService metadataAppService;

    @RequestMapping(value = "/dbs/{data_source_id}", method = RequestMethod.GET)
    public Message getDatabases(
            @PathVariable("data_source_id") String dataSourceId,
            @RequestParam("system") String system,
            HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(system)) {
                return Message.error("'system' is missing[缺少系统名]");
            }
            List<String> databases =
                    metadataAppService.getDatabasesByDsId(
                            dataSourceId, system, SecurityFilter.getLoginUsername(request));
            return Message.ok().data("dbs", databases);
        } catch (Exception e) {
            return errorToResponseMessage(
                    "Fail to get database list[获取库信息失败], id:["
                            + dataSourceId
                            + "], system:["
                            + system
                            + "]",
                    e);
        }
    }

    @RequestMapping(value = "/tables/{data_source_id}/db/{database}", method = RequestMethod.GET)
    public Message getTables(
            @PathVariable("data_source_id") String dataSourceId,
            @PathVariable("database") String database,
            @RequestParam("system") String system,
            HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(system)) {
                return Message.error("'system' is missing[缺少系统名]");
            }
            List<String> tables =
                    metadataAppService.getTablesByDsId(
                            dataSourceId,
                            database,
                            system,
                            SecurityFilter.getLoginUsername(request));
            return Message.ok().data("tables", tables);
        } catch (Exception e) {
            return errorToResponseMessage(
                    "Fail to get table list[获取表信息失败], id:["
                            + dataSourceId
                            + "]"
                            + ", system:["
                            + system
                            + "], database:["
                            + database
                            + "]",
                    e);
        }
    }

    @RequestMapping(
            value = "/props/{data_source_id}/db/{database}/table/{table}",
            method = RequestMethod.GET)
    public Message getTableProps(
            @PathVariable("data_source_id") String dataSourceId,
            @PathVariable("database") String database,
            @PathVariable("table") String table,
            @RequestParam("system") String system,
            HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(system)) {
                return Message.error("'system' is missing[缺少系统名]");
            }
            Map<String, String> tableProps =
                    metadataAppService.getTablePropsByDsId(
                            dataSourceId,
                            database,
                            table,
                            system,
                            SecurityFilter.getLoginUsername(request));
            return Message.ok().data("props", tableProps);
        } catch (Exception e) {
            return errorToResponseMessage(
                    "Fail to get table properties[获取表参数信息失败], id:["
                            + dataSourceId
                            + "]"
                            + ", system:["
                            + system
                            + "], database:["
                            + database
                            + "], table:["
                            + table
                            + "]",
                    e);
        }
    }

    @RequestMapping(
            value = "/partitions/{data_source_id}/db/{database}/table/{table}",
            method = RequestMethod.GET)
    public Message getPartitions(
            @PathVariable("data_source_id") String dataSourceId,
            @PathVariable("database") String database,
            @PathVariable("table") String table,
            @RequestParam("system") String system,
            HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(system)) {
                return Message.error("'system' is missing[缺少系统名]");
            }
            MetaPartitionInfo partitionInfo =
                    metadataAppService.getPartitionsByDsId(
                            dataSourceId,
                            database,
                            table,
                            system,
                            SecurityFilter.getLoginUsername(request));
            return Message.ok().data("props", partitionInfo);
        } catch (Exception e) {
            return errorToResponseMessage(
                    "Fail to get partitions[获取表分区信息失败], id:["
                            + dataSourceId
                            + "]"
                            + ", system:["
                            + system
                            + "], database:["
                            + database
                            + "], table:["
                            + table
                            + "]",
                    e);
        }
    }

    @RequestMapping(
            value = "/columns/{data_source_id}/db/{database}/table/{table}",
            method = RequestMethod.GET)
    public Message getColumns(
            @PathVariable("data_source_id") String dataSourceId,
            @PathVariable("database") String database,
            @PathVariable("table") String table,
            @RequestParam("system") String system,
            HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(system)) {
                return Message.error("'system' is missing[缺少系统名]");
            }
            List<MetaColumnInfo> columns =
                    metadataAppService.getColumns(
                            dataSourceId,
                            database,
                            table,
                            system,
                            SecurityFilter.getLoginUsername(request));
            return Message.ok().data("columns", columns);
        } catch (Exception e) {
            return errorToResponseMessage(
                    "Fail to get column list[获取表字段信息失败], id:["
                            + dataSourceId
                            + "]"
                            + ", system:["
                            + system
                            + "], database:["
                            + database
                            + "], table:["
                            + table
                            + "]",
                    e);
        }
    }

    private Message errorToResponseMessage(String uiMessage, Exception e) {
        if (e instanceof MetaMethodInvokeException) {
            MetaMethodInvokeException invokeException = (MetaMethodInvokeException) e;
            if (LOG.isDebugEnabled()) {
                String argumentJson = null;
                try {
                    argumentJson = Json.toJson(invokeException.getArgs(), null);
                } catch (Exception je) {
                    // Ignore
                }
                LOG.trace(
                        uiMessage
                                + " => Method: "
                                + invokeException.getMethod()
                                + ", Arguments:"
                                + argumentJson,
                        e);
            }
            uiMessage +=
                    " possible reason[可能原因]: (" + invokeException.getCause().getMessage() + ")";
        } else {
            if (e instanceof ErrorException) {
                uiMessage += " possible reason[可能原因]: (" + e.getMessage() + ")";
            }
            LOG.error(uiMessage, e);
        }
        return Message.error(uiMessage);
    }
}
