/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.linkis.metadata.query.server.utils.MetadataUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceId).matches()) {
        return Message.error("'dataSourceId' is invalid[数据源错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(request, "getDatabases, dataSourceId:" + dataSourceId);
      List<String> databases =
          metadataAppService.getDatabasesByDsId(dataSourceId, system, userName);
      return Message.ok().data("dbs", databases);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get database list[获取库信息失败], id:[" + dataSourceId + "], system:[" + system + "]",
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
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(database).matches()) {
        return Message.error("'database' is invalid[数据库名称错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceId).matches()) {
        return Message.error("'dataSourceId' is invalid[数据源错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(request, "getTables, dataSourceId:" + dataSourceId);
      List<String> tables =
          metadataAppService.getTablesByDsId(dataSourceId, database, system, userName);
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
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(database).matches()) {
        return Message.error("'database' is invalid[数据库名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(table).matches()) {
        return Message.error("'table' is invalid[表名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceId).matches()) {
        return Message.error("'dataSourceId' is invalid[数据源错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(request, "getTableProps, dataSourceId:" + dataSourceId);
      Map<String, String> tableProps =
          metadataAppService.getTablePropsByDsId(dataSourceId, database, table, system, userName);
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
      @RequestParam(name = "traverse", required = false, defaultValue = "false") Boolean traverse,
      HttpServletRequest request) {
    try {
      if (StringUtils.isBlank(system)) {
        return Message.error("'system' is missing[缺少系统名]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(database).matches()) {
        return Message.error("'database' is invalid[数据库名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(table).matches()) {
        return Message.error("'table' is invalid[表名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceId).matches()) {
        return Message.error("'dataSourceId' is invalid[数据源错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(request, "getPartitions, dataSourceId:" + dataSourceId);
      MetaPartitionInfo partitionInfo =
          metadataAppService.getPartitionsByDsId(
              dataSourceId, database, table, system, traverse, userName);
      return Message.ok().data("partitions", partitionInfo).data("props", partitionInfo);
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
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(database).matches()) {
        return Message.error("'database' is invalid[数据库名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(table).matches()) {
        return Message.error("'table' is invalid[表名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceId).matches()) {
        return Message.error("'dataSourceId' is invalid[数据源错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(partition).matches()) {
        return Message.error("'partition' is invalid[partition错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(
              request, "getPartitionProps, dataSourceId:" + dataSourceId);
      Map<String, String> partitionProps =
          metadataAppService.getPartitionPropsByDsId(
              dataSourceId, database, table, partition, system, userName);
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
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(database).matches()) {
        return Message.error("'database' is invalid[数据库名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(table).matches()) {
        return Message.error("'table' is invalid[表名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceId).matches()) {
        return Message.error("'dataSourceId' is invalid[数据源错误]");
      }

      String userName =
          ModuleUserUtils.getOperationUser(request, "getColumns, dataSourceId:" + dataSourceId);
      List<MetaColumnInfo> columns =
          metadataAppService.getColumnsByDsId(dataSourceId, database, table, system, userName);
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
      uiMessage += " possible reason[可能原因]: (" + invokeException.getCause().getMessage() + ")";
    } else {
      if (e instanceof ErrorException) {
        uiMessage += " possible reason[可能原因]: (" + e.getMessage() + ")";
      }
    }
    LOG.error(uiMessage, e);
    return Message.error(uiMessage);
  }
}
