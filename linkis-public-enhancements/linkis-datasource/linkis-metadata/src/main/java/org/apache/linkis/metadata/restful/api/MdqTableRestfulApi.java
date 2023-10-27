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

import org.apache.linkis.metadata.ddl.ImportDDLCreator;
import org.apache.linkis.metadata.ddl.ScalaDDLCreator;
import org.apache.linkis.metadata.domain.mdq.bo.MdqTableBO;
import org.apache.linkis.metadata.domain.mdq.bo.MdqTableImportInfoBO;
import org.apache.linkis.metadata.domain.mdq.vo.MdqTableBaseInfoVO;
import org.apache.linkis.metadata.domain.mdq.vo.MdqTableFieldsInfoVO;
import org.apache.linkis.metadata.domain.mdq.vo.MdqTablePartitionStatisticInfoVO;
import org.apache.linkis.metadata.domain.mdq.vo.MdqTableStatisticInfoVO;
import org.apache.linkis.metadata.exception.MdqIllegalParamException;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;
import org.apache.linkis.metadata.service.MdqService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "mdq table api")
@RestController
@RequestMapping(path = "/datasource")
public class MdqTableRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(MdqTableRestfulApi.class);

  private static final String ASC = "asc";

  @Autowired private MdqService mdqService;
  ObjectMapper mapper = new ObjectMapper();

  @ApiOperation(value = "getTableBaseInfo", notes = "get table base info", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", required = false, dataType = "String", value = "database"),
    @ApiImplicitParam(name = "tableName", dataType = "String")
  })
  @RequestMapping(path = "getTableBaseInfo", method = RequestMethod.GET)
  public Message getTableBaseInfo(
      @RequestParam(value = "database", required = false) String database,
      @RequestParam(value = "tableName", required = false) String tableName,
      HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "getTableBaseInfo " + tableName);
    MetadataQueryParam queryParam =
        MetadataQueryParam.of(userName).withDbName(database).withTableName(tableName);
    MdqTableBaseInfoVO tableBaseInfo = mdqService.getTableBaseInfoFromHive(queryParam);
    return Message.ok().data("tableBaseInfo", tableBaseInfo);
  }

  @ApiOperation(
      value = "getTableFieldsInfo",
      notes = "get table fields info",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", required = false, dataType = "String", value = "database"),
    @ApiImplicitParam(name = "tableName", dataType = "String")
  })
  @RequestMapping(path = "getTableFieldsInfo", method = RequestMethod.GET)
  public Message getTableFieldsInfo(
      @RequestParam(value = "database", required = false) String database,
      @RequestParam(value = "tableName", required = false) String tableName,
      HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "getTableFieldsInfo " + tableName);
    MetadataQueryParam queryParam =
        MetadataQueryParam.of(userName).withDbName(database).withTableName(tableName);
    List<MdqTableFieldsInfoVO> tableFieldsInfo = mdqService.getTableFieldsInfoFromHive(queryParam);
    return Message.ok().data("tableFieldsInfo", tableFieldsInfo);
  }

  @ApiOperation(
      value = "getTableStatisticInfo",
      notes = "get table statistic info",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", required = false, dataType = "String", value = "database"),
    @ApiImplicitParam(name = "tableName", dataType = "String"),
    @ApiImplicitParam(name = "pageNow", required = true, dataType = "String", value = "page now"),
    @ApiImplicitParam(name = "pageSize", required = true, dataType = "String", value = "page size"),
    @ApiImplicitParam(name = "partitionSort", required = true, dataType = "String")
  })
  @RequestMapping(path = "getTableStatisticInfo", method = RequestMethod.GET)
  public Message getTableStatisticInfo(
      @RequestParam(value = "database", required = false) String database,
      @RequestParam(value = "tableName", required = false) String tableName,
      @RequestParam(value = "pageNow", defaultValue = "1") int pageNow,
      @RequestParam(value = "pageSize", defaultValue = "1000") int pageSize,
      @RequestParam(value = "partitionSort", defaultValue = "desc") String partitionSort,
      HttpServletRequest req)
      throws IOException {
    String userName = ModuleUserUtils.getOperationUser(req, "getTableStatisticInfo " + tableName);
    MetadataQueryParam queryParam =
        MetadataQueryParam.of(userName).withDbName(database).withTableName(tableName);
    MdqTableStatisticInfoVO tableStatisticInfo =
        mdqService.getTableStatisticInfo(queryParam, partitionSort);
    int totalSize = 0;
    List<MdqTablePartitionStatisticInfoVO> partitionPage;
    List<MdqTablePartitionStatisticInfoVO> partitions = tableStatisticInfo.getPartitions();
    if (partitions != null && !partitions.isEmpty()) {
      // 排序
      if (ASC.equals(partitionSort)) {
        partitions =
            partitions.stream()
                .sorted(Comparator.comparing(MdqTablePartitionStatisticInfoVO::getName))
                .collect(Collectors.toList());
      } else {
        partitions =
            partitions.stream()
                .sorted(Comparator.comparing(MdqTablePartitionStatisticInfoVO::getName).reversed())
                .collect(Collectors.toList());
      }
      if (pageNow <= 0 || pageSize <= 0) {
        pageNow = 1;
        pageSize = 1000;
      }
      totalSize = partitions.size();
      int start = (pageNow - 1) * pageSize;
      int end = pageNow * pageSize; // subList 这里不用-1
      if (start > totalSize) {
        partitionPage = new ArrayList<>();
      } else if (end > totalSize) {
        partitionPage = partitions.subList(start, totalSize);
      } else {
        partitionPage = partitions.subList(start, end);
      }
      tableStatisticInfo.setPartitions(partitionPage);
    }
    Message data =
        Message.ok()
            .data("tableStatisticInfo", tableStatisticInfo)
            .data("totalSize", totalSize)
            .data("pageNow", pageNow)
            .data("pageSize", pageSize);
    return data;
  }

  @ApiOperation(
      value = "getPartitionStatisticInfo",
      notes = "get partition statistic info",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "database", required = false, dataType = "String", value = "database"),
    @ApiImplicitParam(name = "tableName", dataType = "String"),
    @ApiImplicitParam(name = "partitionSort", dataType = "String")
  })
  @RequestMapping(path = "getPartitionStatisticInfo", method = RequestMethod.GET)
  public Message getPartitionStatisticInfo(
      @RequestParam(value = "database", required = false) String database,
      @RequestParam(value = "tableName", required = false) String tableName,
      @RequestParam(value = "partitionPath", required = false) String partitionName,
      HttpServletRequest req)
      throws IOException, MdqIllegalParamException {
    String userName =
        ModuleUserUtils.getOperationUser(req, "getPartitionStatisticInfo " + tableName);
    MetadataQueryParam queryParam =
        MetadataQueryParam.of(userName).withDbName(database).withTableName(tableName);
    MdqTablePartitionStatisticInfoVO partition =
        mdqService.getPartitionStatisticInfo(queryParam, partitionName);
    return Message.ok().data("partitionStatisticInfo", partition);
  }

  @ApiOperation(value = "active", notes = "active", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "tableId", dataType = "String", value = "table id")})
  @RequestMapping(path = "active", method = RequestMethod.GET)
  public Message active(
      @RequestParam(value = "tableId", required = false) Long tableId, HttpServletRequest req) {
    mdqService.activateTable(tableId);
    return Message.ok();
  }

  @ApiOperation(value = "persistTable", notes = "persist table", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "persistTable", method = RequestMethod.POST)
  public Message persistTable(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException {
    String userName = ModuleUserUtils.getOperationUser(req, "persistTable ");
    MdqTableBO table = mapper.treeToValue(json.get("table"), MdqTableBO.class);
    mdqService.persistTable(table, userName);
    return Message.ok();
  }

  @ApiOperation(value = "displaySql", notes = "display sql", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "displaysql", method = RequestMethod.POST)
  public Message displaySql(HttpServletRequest request, @RequestBody JsonNode json) {
    String userName = ModuleUserUtils.getOperationUser(request, "displaysql ");
    logger.info("display sql for user {} ", userName);
    StringBuilder sb = new StringBuilder();
    String retSql = "";
    MdqTableBO tableBO = null;
    try {
      tableBO = mapper.treeToValue(json.get("table"), MdqTableBO.class);
      MdqTableImportInfoBO importInfo = tableBO.getImportInfo();
      if (importInfo != null) {
        retSql = ImportDDLCreator.createDDL(tableBO, userName);
      } else {
        retSql = ScalaDDLCreator.createDDL(tableBO, userName);
      }
    } catch (Exception e) {
      logger.error("json parse to bean failed", e);
      Message message = Message.error("display ddl failed");
      return message;
    }
    String tableName = tableBO.getTableBaseInfo().getBase().getName();
    String dbName = tableBO.getTableBaseInfo().getBase().getDatabase();
    String retStr = "意书后台正在为您生成新建库表: " + dbName + "." + tableName + "的DDL语句,请点击建表按钮进行执行";
    Message message = Message.ok(retStr);
    message.setMethod("/api/datasource/display");
    message.data("sql", retSql);
    return message;
  }
}
