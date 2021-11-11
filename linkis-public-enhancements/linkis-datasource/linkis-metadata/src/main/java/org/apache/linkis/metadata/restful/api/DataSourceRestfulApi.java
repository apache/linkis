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

import org.apache.linkis.metadata.restful.remote.DataSourceRestfulRemote;
import org.apache.linkis.metadata.service.DataSourceService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/datasource")
public class DataSourceRestfulApi implements DataSourceRestfulRemote {

    private static final Logger logger = Logger.getLogger(DataSourceRestfulApi.class);

    @Autowired
    DataSourceService dataSourceService;


    @Override
    @RequestMapping(path = "dbs",method = RequestMethod.GET)
    public Message queryDatabaseInfo(HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode dbs = dataSourceService.getDbs(userName);
            return Message.ok("").data("dbs", dbs);
        } catch (Exception e) {
            logger.error("Failed to get database(获取数据库失败)", e);
            return Message.error("Failed to get database(获取数据库失败)", e);
        }
    }

    @Override
    @RequestMapping(path = "all",method = RequestMethod.GET)
    public Message queryDbsWithTables(HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode dbs = dataSourceService.getDbsWithTables(userName);
            return Message.ok("").data("dbs", dbs);
        } catch (Exception e) {
            logger.error("Failed to queryDbsWithTables", e);
            return Message.error("Failed to queryDbsWithTables", e);
        }
    }


    @Override
    @RequestMapping(path = "tables",method = RequestMethod.GET)
    public Message queryTables(@RequestParam(value="database",required=false) String database, HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode tables = dataSourceService.queryTables(database, userName);
            return Message.ok("").data("tables", tables);
        } catch (Exception e) {
            logger.error("Failed to queryTables", e);
            return Message.error("Failed to queryTables", e);
        }
    }

    @Override
    @RequestMapping(path = "columns",method = RequestMethod.GET)
    public Message queryTableMeta(@RequestParam(value="database",required=false) String database,  @RequestParam(value="table",required=false) String table, HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode columns = dataSourceService.queryTableMeta(database, table, userName);
            return Message.ok("").data("columns", columns);
        } catch (Exception e) {
            logger.error("Failed to get data table structure(获取数据表结构失败)", e);
            return Message.error("Failed to get data table structure(获取数据表结构失败)", e);
        }
    }

    @Override
    @RequestMapping(path = "size",method = RequestMethod.GET)
    public Message sizeOf(@RequestParam(value="database",required=false) String database,  @RequestParam(value="table",required=false) String table, @RequestParam(value="partition",required=false) String partition, HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode sizeNode;
            if (StringUtils.isBlank(partition)){
                sizeNode = dataSourceService.getTableSize(database, table, userName);
            } else {
                sizeNode = dataSourceService.getPartitionSize(database, table, partition, userName);
            }
            return Message.ok("").data("sizeInfo", sizeNode);
        } catch (Exception e) {
            logger.error("Failed to get table partition size(获取表分区大小失败)", e);
            return Message.error("Failed to get table partition size(获取表分区大小失败)", e);
        }
    }

    @Override
    @RequestMapping(path = "partitions",method = RequestMethod.GET)
    public Message partitions(@RequestParam(value="database",required=false) String database,  @RequestParam(value="table",required=false) String table, HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try{
            JsonNode partitionNode = dataSourceService.getPartitions(database, table, userName);
            return Message.ok("").data("partitionInfo", partitionNode);
        } catch (Exception e) {
            logger.error("Failed to get table partition(获取表分区失败)", e);
            return Message.error("Failed to get table partition(获取表分区失败)", e);
        }
    }
}
