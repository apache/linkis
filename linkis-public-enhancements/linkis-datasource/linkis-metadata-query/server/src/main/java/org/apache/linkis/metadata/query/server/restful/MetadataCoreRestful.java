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

package org.apache.linkis.metadata.query.server.restful;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadata.query.common.exception.MetaMethodInvokeException;
import org.apache.linkis.metadata.query.server.service.MetadataQueryService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/metadatamanager")
@Deprecated
public class MetadataCoreRestful {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataCoreRestful.class);

    @Autowired private MetadataQueryService metadataAppService;

    @RequestMapping(value = "/dbs/{dataSourceId}", method = RequestMethod.GET)
    public Message getDatabases(
            @PathVariable("dataSourceId") String dataSourceId,
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

    @RequestMapping(value = "/tables/{dataSourceId}/db/{database}", method = RequestMethod.GET)
    public Message getTables(
            @PathVariable("dataSourceId") String dataSourceId,
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
            value = "/props/{dataSourceId}/db/{database}/table/{table}",
            method = RequestMethod.GET)
    public Message getTableProps(
            @PathVariable("dataSourceId") String dataSourceId,
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

    // Note Result field[`props`] name is inaccurate
    @RequestMapping(
            value = "/partitions/{dataSourceId}/db/{database}/table/{table}",
            method = RequestMethod.GET)
    public Message getPartitions(
            @PathVariable("dataSourceId") String dataSourceId,
            @PathVariable("database") String database,
            @PathVariable("table") String table,
            @RequestParam("system") String system,
            @RequestParam(name = "traverse", required = false, defaultValue = "false")
                    Boolean traverse,
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
                            traverse,
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
            value = "/props/{dataSourceId}/db/{database}/table/{table}/partition/{partition}",
            method = RequestMethod.GET)
    public Message getPartitionProps(
            @PathVariable("dataSourceId") String dataSourceId,
            @PathVariable("database") String database,
            @PathVariable("table") String table,
            @PathVariable("partition") String partition,
            @RequestParam("system") String system,
            HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(system)) {
                return Message.error("'system' is missing[缺少系统名]");
            }
            Map<String, String> partitionProps =
                    metadataAppService.getPartitionPropsByDsId(
                            dataSourceId,
                            database,
                            table,
                            partition,
                            system,
                            SecurityFilter.getLoginUsername(request));
            return Message.ok().data("props", partitionProps);
        } catch (Exception e) {
            return errorToResponseMessage(
                    "Fail to get partition properties[获取分区参数信息失败], id:["
                            + dataSourceId
                            + "]"
                            + ", system:["
                            + system
                            + "], database:["
                            + database
                            + "], table:["
                            + table
                            + "], partition:["
                            + partition
                            + "]",
                    e);
        }
    }

    @RequestMapping(
            value = "/columns/{dataSourceId}/db/{database}/table/{table}",
            method = RequestMethod.GET)
    public Message getColumns(
            @PathVariable("dataSourceId") String dataSourceId,
            @PathVariable("database") String database,
            @PathVariable("table") String table,
            @RequestParam("system") String system,
            HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(system)) {
                return Message.error("'system' is missing[缺少系统名]");
            }
            List<MetaColumnInfo> columns =
                    metadataAppService.getColumnsByDsId(
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
        }
        LOG.error(uiMessage, e);
        return Message.error(uiMessage);
    }
}
