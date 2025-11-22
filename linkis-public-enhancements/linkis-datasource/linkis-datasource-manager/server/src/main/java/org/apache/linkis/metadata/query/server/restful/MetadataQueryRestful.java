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
import org.apache.linkis.metadata.query.common.domain.GenerateSqlInfo;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadata.query.common.exception.MetaMethodInvokeException;
import org.apache.linkis.metadata.query.server.service.MetadataQueryService;
import org.apache.linkis.metadata.query.server.utils.MetadataUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "metadata query")
@RestController
@RequestMapping(value = "/metadataQuery")
public class MetadataQueryRestful {

  private static final Logger logger = LoggerFactory.getLogger(MetadataQueryRestful.class);

  @Autowired private MetadataQueryService metadataQueryService;

  @RequestMapping(value = "/getConnectionInfo", method = RequestMethod.GET)
  public Message getConnectionInfo(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam("system") String system,
      HttpServletRequest request) {
    try {
      if (StringUtils.isBlank(system)) {
        return Message.error("'system' is missing[缺少系统名]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(
              request, "getConnectionInfo, dataSourceName:" + dataSourceName);
      Map<String, String> queryParams =
          request.getParameterMap().entrySet().stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, entry -> StringUtils.join(entry.getValue(), ",")));
      Map<String, String> info =
          metadataQueryService.getConnectionInfoByDsName(
              dataSourceName, queryParams, system, userName);
      return Message.ok().data("info", info);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get connection info [获得连接信息失败], name: ["
              + dataSourceName
              + "], system:["
              + system
              + "]",
          e);
    }
  }

  @ApiOperation(value = "getDatabases", notes = "get databases", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "envId", required = false, dataType = "String"),
    @ApiImplicitParam(name = "system", required = true, dataType = "String")
  })
  @RequestMapping(value = "/getDatabases", method = RequestMethod.GET)
  public Message getDatabases(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam("system") String system,
      HttpServletRequest request) {
    try {
      if (StringUtils.isBlank(system)) {
        return Message.error("'system' is missing[缺少系统名]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(
              request, "getDatabases, dataSourceName:" + dataSourceName);
      List<String> databases =
          metadataQueryService.getDatabasesByDsNameAndEnvId(
              dataSourceName, system, userName, envId);
      return Message.ok().data("dbs", databases);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get database list[获取库信息失败], name:["
              + dataSourceName
              + "], system:["
              + system
              + "]",
          e);
    }
  }

  @ApiOperation(value = "getTables", notes = "get tables", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "envId", required = false, dataType = "String"),
    @ApiImplicitParam(name = "system", required = true, dataType = "String"),
    @ApiImplicitParam(name = "database", required = true, dataType = "String")
  })
  @RequestMapping(value = "/getTables", method = RequestMethod.GET)
  public Message getTables(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam("database") String database,
      @RequestParam("system") String system,
      HttpServletRequest request) {
    try {
      if (StringUtils.isBlank(system)) {
        return Message.error("'system' is missing[缺少系统名]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(system).matches()) {
        return Message.error("'system' is invalid[系统名错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(database).matches()) {
        return Message.error("'database' is invalid[数据库名称错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(request, "getTables, dataSourceName:" + dataSourceName);
      List<String> tables =
          metadataQueryService.getTablesByDsNameAndEnvId(
              dataSourceName, database, system, userName, envId);
      return Message.ok().data("tables", tables);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get table list[获取表信息失败], name:["
              + dataSourceName
              + "]"
              + ", system:["
              + system
              + "], database:["
              + database
              + "]",
          e);
    }
  }

  @ApiOperation(value = "getTableProps", notes = "get table props", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "system", required = true, dataType = "String"),
    @ApiImplicitParam(name = "database", required = true, dataType = "String"),
    @ApiImplicitParam(name = "table", required = true, dataType = "String")
  })
  @RequestMapping(value = "/getTableProps", method = RequestMethod.GET)
  public Message getTableProps(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam("database") String database,
      @RequestParam("table") String table,
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
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(
              request, "getTableProps, dataSourceName:" + dataSourceName);
      Map<String, String> tableProps =
          metadataQueryService.getTablePropsByDsName(
              dataSourceName, database, table, system, userName);
      return Message.ok().data("props", tableProps);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get table properties[获取表参数信息失败], name:["
              + dataSourceName
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

  @ApiOperation(value = "getPartitions", notes = "get partitions", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "system", required = true, dataType = "String"),
    @ApiImplicitParam(name = "database", required = true, dataType = "String"),
    @ApiImplicitParam(name = "table", required = true, dataType = "String")
  })
  @RequestMapping(value = "/getPartitions", method = RequestMethod.GET)
  public Message getPartitions(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam("database") String database,
      @RequestParam("table") String table,
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
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }

      String userName =
          ModuleUserUtils.getOperationUser(
              request, "getPartitions, dataSourceName:" + dataSourceName);
      MetaPartitionInfo partitionInfo =
          metadataQueryService.getPartitionsByDsName(
              dataSourceName, database, table, system, traverse, userName);
      return Message.ok().data("partitions", partitionInfo);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get partitions[获取表分区信息失败], name:["
              + dataSourceName
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

  @ApiOperation(
      value = "getPartitionProps",
      notes = "get partition props",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "system", required = true, dataType = "String"),
    @ApiImplicitParam(name = "database", required = true, dataType = "String"),
    @ApiImplicitParam(name = "table", required = true, dataType = "String"),
    @ApiImplicitParam(name = "partition", required = true, dataType = "String")
  })
  @RequestMapping(value = "getPartitionProps", method = RequestMethod.GET)
  public Message getPartitionProps(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam("database") String database,
      @RequestParam("table") String table,
      @RequestParam("partition") String partition,
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
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }
      if (!MetadataUtils.nameRegexPattern.matcher(partition).matches()) {
        return Message.error("'partition' is invalid[partition错误]");
      }
      String userName =
          ModuleUserUtils.getOperationUser(
              request, "getPartitionProps, dataSourceName:" + dataSourceName);
      Map<String, String> partitionProps =
          metadataQueryService.getPartitionPropsByDsName(
              dataSourceName, database, table, partition, system, userName);
      return Message.ok().data("props", partitionProps);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get partition properties[获取分区参数信息失败], name:["
              + dataSourceName
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

  @ApiOperation(value = "getColumns", notes = "get columns", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "envId", required = false, dataType = "String"),
    @ApiImplicitParam(name = "system", required = true, dataType = "String"),
    @ApiImplicitParam(name = "database", required = true, dataType = "String"),
    @ApiImplicitParam(name = "table", required = true, dataType = "String")
  })
  @RequestMapping(value = "/getColumns", method = RequestMethod.GET)
  public Message getColumns(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam("database") String database,
      @RequestParam("table") String table,
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
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }

      String userName =
          ModuleUserUtils.getOperationUser(request, "getColumns, dataSourceName:" + dataSourceName);

      List<MetaColumnInfo> columns =
          metadataQueryService.getColumnsByDsNameAndEnvId(
              dataSourceName, database, table, system, userName, envId);
      return Message.ok().data("columns", columns);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to get column list[获取表字段信息失败], name:["
              + dataSourceName
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

  @ApiOperation(value = "getSparkDdlSql", notes = "get spark ddl sql", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "envId", required = false, dataType = "String"),
    @ApiImplicitParam(name = "system", required = true, dataType = "String"),
    @ApiImplicitParam(name = "database", required = true, dataType = "String"),
    @ApiImplicitParam(name = "table", required = true, dataType = "String")
  })
  @RequestMapping(value = "/getSparkSql", method = RequestMethod.GET)
  public Message getSparkSql(
      @RequestParam("dataSourceName") String dataSourceName,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam("database") String database,
      @RequestParam("table") String table,
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
      if (!MetadataUtils.nameRegexPattern.matcher(dataSourceName).matches()) {
        return Message.error("'dataSourceName' is invalid[数据源错误]");
      }

      String userName =
          ModuleUserUtils.getOperationUser(
              request, "getSparkDdlSql, dataSourceName:" + dataSourceName);

      GenerateSqlInfo sparkSql =
          metadataQueryService.getSparkSqlByDsNameAndEnvId(
              dataSourceName, database, table, system, userName, envId);
      return Message.ok().data("sparkSql", sparkSql);
    } catch (Exception e) {
      return errorToResponseMessage(
          "Fail to spark sql[获取getSparkSql信息失败], name:["
              + dataSourceName
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
      if (logger.isDebugEnabled()) {
        String argumentJson = null;
        try {
          argumentJson = Json.toJson(invokeException.getArgs(), null);
        } catch (Exception je) {
          // Ignore
        }
        logger.trace(
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
    logger.error(uiMessage, e);
    return Message.error(uiMessage);
  }
}
