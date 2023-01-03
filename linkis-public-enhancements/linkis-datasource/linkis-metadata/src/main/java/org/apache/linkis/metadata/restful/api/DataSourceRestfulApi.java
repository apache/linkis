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

package org.apache.linkis.metadata.restful.api;

import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;
import org.apache.linkis.metadata.restful.remote.DataSourceRestfulRemote;
import org.apache.linkis.metadata.service.DataSourceService;
import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "data source restful api")
@RestController
@RequestMapping(path = "/datasource")
public class DataSourceRestfulApi implements DataSourceRestfulRemote {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceRestfulApi.class);

  @Autowired DataSourceService dataSourceService;

  @Autowired HiveMetaWithPermissionService hiveMetaWithPermissionService;

  @ApiOperation(
      value = "queryDatabaseInfo",
      notes = "query database info",
      response = Message.class)
  @Override
  @RequestMapping(path = "dbs", method = RequestMethod.GET)
  public Message queryDatabaseInfo(HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "get dbs");
    try {
      JsonNode dbs = dataSourceService.getDbs(userName);
      return Message.ok("").data("dbs", dbs);
    } catch (Exception e) {
      logger.error("Failed to get database(获取数据库失败)", e);
      return Message.error("Failed to get database(获取数据库失败)", e);
    }
  }

  @ApiOperation(
      value = "queryPartitionExists",
      notes = "query partition exists",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", dataType = "String", value = "database"),
    @ApiImplicitParam(name = "table", dataType = "String", value = "table"),
    @ApiImplicitParam(name = "partition", dataType = "String", value = "table")
  })
  @RequestMapping(path = "partitionExists", method = RequestMethod.GET)
  public Message partitionExists(
      @RequestParam(value = "database") String database,
      @RequestParam(value = "table") String table,
      @RequestParam(value = "partition") String partition,
      HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "query partition exists");
    try {
      if (StringUtils.isBlank(database)) {
        return Message.error("'database' is missing[缺少数据库]");
      }
      if (StringUtils.isBlank(table)) {
        return Message.error("'table' is missing[缺少表名]");
      }
      if (StringUtils.isBlank(partition)) {
        return Message.error("'partition' is missing[缺少分区名]");
      }
      MetadataQueryParam queryParam =
          MetadataQueryParam.of(userName)
              .withDbName(database)
              .withTableName(table)
              .withPartitionName(partition);
      boolean res = dataSourceService.partitionExists(queryParam);
      return Message.ok("").data("partitionExists", res);
    } catch (Exception e) {
      logger.error("Failed to examine whether a partition exists(检查分区是否存在失败)", e);
      return Message.error("Failed to examine whether a partition exists (检查分区是否存在失败)", e);
    }
  }

  @ApiOperation(
      value = "queryDbsWithTables",
      notes = "query dbs with tables",
      response = Message.class)
  @Override
  @RequestMapping(path = "all", method = RequestMethod.GET)
  public Message queryDbsWithTables(HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "get all db and tables");
    try {
      JsonNode dbs = dataSourceService.getDbsWithTables(userName);
      return Message.ok("").data("dbs", dbs);
    } catch (Exception e) {
      logger.error("Failed to queryDbsWithTables", e);
      return Message.error("Failed to queryDbsWithTables", e);
    }
  }

  @ApiOperation(value = "queryTables", notes = "query tables", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", dataType = "String", value = "database")
  })
  @Override
  @RequestMapping(path = "tables", method = RequestMethod.GET)
  public Message queryTables(
      @RequestParam(value = "database", required = false) String database, HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "get tables");
    MetadataQueryParam queryParam = MetadataQueryParam.of(userName).withDbName(database);
    try {
      JsonNode tables = dataSourceService.queryTables(queryParam);
      return Message.ok("").data("tables", tables);
    } catch (Exception e) {
      logger.error("Failed to queryTables", e);
      return Message.error("Failed to queryTables", e);
    }
  }

  @ApiOperation(value = "queryTableMeta", notes = "query table meta", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", dataType = "String", value = "database"),
    @ApiImplicitParam(name = "table", dataType = "String", value = "table")
  })
  @Override
  @RequestMapping(path = "columns", method = RequestMethod.GET)
  public Message queryTableMeta(
      @RequestParam(value = "database", required = false) String database,
      @RequestParam(value = "table", required = false) String table,
      HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "get columns of table " + table);
    MetadataQueryParam queryParam =
        MetadataQueryParam.of(userName).withDbName(database).withTableName(table);
    try {
      JsonNode columns =
          hiveMetaWithPermissionService.getColumnsByDbTableNameAndOptionalUserName(queryParam);
      return Message.ok("").data("columns", columns);
    } catch (Exception e) {
      logger.error("Failed to get data table structure(获取数据表结构失败)", e);
      return Message.error("Failed to get data table structure(获取数据表结构失败)", e);
    }
  }

  @ApiOperation(value = "sizeOf", notes = "size Of", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", dataType = "String", value = "database"),
    @ApiImplicitParam(name = "table", dataType = "String", value = "table"),
    @ApiImplicitParam(name = "partition", dataType = "String")
  })
  @Override
  @RequestMapping(path = "size", method = RequestMethod.GET)
  public Message sizeOf(
      @RequestParam(value = "database", required = false) String database,
      @RequestParam(value = "table", required = false) String table,
      @RequestParam(value = "partition", required = false) String partition,
      HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "get size ");
    MetadataQueryParam queryParam =
        MetadataQueryParam.of(userName)
            .withDbName(database)
            .withTableName(table)
            .withPartitionName(partition);
    try {
      JsonNode sizeNode;
      if (StringUtils.isBlank(partition)) {
        sizeNode = dataSourceService.getTableSize(queryParam);
      } else {
        sizeNode = dataSourceService.getPartitionSize(queryParam);
      }
      return Message.ok("").data("sizeInfo", sizeNode);
    } catch (Exception e) {
      logger.error("Failed to get table partition size(获取表分区大小失败)", e);
      return Message.error("Failed to get table partition size(获取表分区大小失败)", e);
    }
  }

  @ApiOperation(value = "partitions", notes = "partitions", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", dataType = "String", value = "database"),
    @ApiImplicitParam(name = "table", dataType = "String", value = "table")
  })
  @Override
  @RequestMapping(path = "partitions", method = RequestMethod.GET)
  public Message partitions(
      @RequestParam(value = "database", required = false) String database,
      @RequestParam(value = "table", required = false) String table,
      HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "get partitions of " + table);
    MetadataQueryParam queryParam =
        MetadataQueryParam.of(userName).withDbName(database).withTableName(table);
    try {
      JsonNode partitionNode = dataSourceService.getPartitions(queryParam);
      return Message.ok("").data("partitionInfo", partitionNode);
    } catch (Exception e) {
      logger.error("Failed to get table partition(获取表分区失败)", e);
      return Message.error("Failed to get table partition(获取表分区失败)", e);
    }
  }
}
