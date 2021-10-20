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
import org.apache.linkis.metadata.service.MdqService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping(path = "/datasource")
public class MdqTableRestfulApi {

    private static final Logger logger = LoggerFactory.getLogger(MdqTableRestfulApi.class);

    private static final String ASC = "asc";

    @Autowired
    private MdqService mdqService;
    ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(path = "getTableBaseInfo",method = RequestMethod.GET)
    public Message getTableBaseInfo(@RequestParam(value="database",required=false) String database, @RequestParam(value="tableName",required=false) String tableName,
                                      HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        MdqTableBaseInfoVO tableBaseInfo;
        if (mdqService.isExistInMdq(database, tableName, userName)) {
            tableBaseInfo = mdqService.getTableBaseInfoFromMdq(database, tableName, userName);
        } else {
            tableBaseInfo = mdqService.getTableBaseInfoFromHive(database, tableName, userName);
        }
        return Message.ok().data("tableBaseInfo", tableBaseInfo);
    }

    @RequestMapping(path = "getTableFieldsInfo",method = RequestMethod.GET)
    public Message getTableFieldsInfo(@RequestParam(value="database",required=false) String database, @RequestParam(value="tableName",required=false) String tableName,
                                        HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<MdqTableFieldsInfoVO> tableFieldsInfo;
        if (mdqService.isExistInMdq(database, tableName, userName)) {
            tableFieldsInfo = mdqService.getTableFieldsInfoFromMdq(database, tableName, userName);
        } else {
            tableFieldsInfo = mdqService.getTableFieldsInfoFromHive(database, tableName, userName);
        }
        return Message.ok().data("tableFieldsInfo", tableFieldsInfo);
    }

    @RequestMapping(path = "getTableStatisticInfo",method = RequestMethod.GET)
    public Message getTableStatisticInfo( @RequestParam(value="database",required=false) String database,
                                          @RequestParam(value="tableName",required=false) String tableName,
                                          @RequestParam(value="pageNow",defaultValue="1") int pageNow,
                                          @RequestParam(value="pageSize",defaultValue="1000") int pageSize,
                                          @RequestParam(value="partitionSort",defaultValue="desc") String partitionSort,
                                           HttpServletRequest req) throws IOException {
        String userName = SecurityFilter.getLoginUsername(req);
        MdqTableStatisticInfoVO tableStatisticInfo = mdqService.getTableStatisticInfo(database, tableName, userName);
        int totalSize = 0;
        List<MdqTablePartitionStatisticInfoVO> partitionPage;
        List<MdqTablePartitionStatisticInfoVO> partitions = tableStatisticInfo.getPartitions();
        if (partitions != null && !partitions.isEmpty()) {
            //排序
            if (ASC.equals(partitionSort)) {
                partitions = partitions.stream().sorted(Comparator.comparing(MdqTablePartitionStatisticInfoVO::getName))
                        .collect(Collectors.toList());
            } else {
                partitions = partitions.stream().sorted(Comparator.comparing(MdqTablePartitionStatisticInfoVO::getName)
                        .reversed()).collect(Collectors.toList());
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
        Message data = Message.ok().data("tableStatisticInfo", tableStatisticInfo)
                .data("totalSize", totalSize)
                .data("pageNow", pageNow)
                .data("pageSize", pageSize);
        return data;
    }

    @RequestMapping(path = "getPartitionStatisticInfo",method = RequestMethod.GET)
    public Message getPartitionStatisticInfo(@RequestParam(value="database",required=false) String database, @RequestParam(value="tableName",required=false) String tableName,
                                              @RequestParam(value="partitionPath",required=false) String partitionName,
                                               HttpServletRequest req) throws IOException, MdqIllegalParamException {
        String userName = SecurityFilter.getLoginUsername(req);
        MdqTablePartitionStatisticInfoVO partition = mdqService.getPartitionStatisticInfo(database, tableName, userName, partitionName);
        return Message.ok().data("partitionStatisticInfo", partition);
    }

    @RequestMapping(path = "active",method = RequestMethod.GET)
    public Message active(@RequestParam(value="tableId",required=false) Long tableId,  HttpServletRequest req) {
        mdqService.activateTable(tableId);
        return Message.ok();
    }

    @RequestMapping(path = "persistTable",method = RequestMethod.POST)
    public Message persistTable( HttpServletRequest req, @RequestBody JsonNode json) throws IOException {
        String userName = SecurityFilter.getLoginUsername(req);
        MdqTableBO table = mapper.treeToValue(json.get("table"), MdqTableBO.class);
        mdqService.persistTable(table, userName);
        return Message.ok();
    }


    @RequestMapping(path = "displaysql",method = RequestMethod.POST)
    public Message displaySql( HttpServletRequest request, @RequestBody JsonNode json) {
        String userName = SecurityFilter.getLoginUsername(request);
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
