/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.metadata.restful.api;

import com.webank.wedatasphere.linkis.metadata.restful.remote.DataSourceRestfulRemote;
import com.webank.wedatasphere.linkis.metadata.service.DataSourceService;
import com.webank.wedatasphere.linkis.metadata.util.Constants;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by shanhuang on 9/13/18.
 */
@Path("datasource")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component
public class DataSourceRestfulApi implements DataSourceRestfulRemote {

    private static final Log logger = LogFactory.getLog(DataSourceRestfulApi.class);

    @Autowired
    DataSourceService dataSourceService;

    @GET
    @Path("options")
    public Response listOptions(@QueryParam("type") String type, @QueryParam("path") String path, @Context HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        JsonNode options = null;
        if(Constants.SCRIPT.equals(type)) {
            try {
                options = dataSourceService.getDirContent(path, userName);
            } catch (Exception e) {
                logger.error("Failed to get file directory(获取文件目录失败)：", e);
                return Message.messageToResponse(Message.error("Failed to get file directory(获取文件目录失败)", e));
            }
        }else if(Constants.TABLE.equals(type) && StringUtils.isBlank(path)) {
            try {
                options = dataSourceService.getDbs(userName);
            } catch (Exception e) {
                logger.error("Failed to get database(获取数据库失败)：", e);
                return Message.messageToResponse(Message.error("Failed to get database(获取数据库失败)", e));
            }
        }else if(Constants.TABLE.equals(type)) {
            try {
                options = dataSourceService.queryTables(path, userName);
            } catch (Exception e) {
                logger.error("Failed to get data table（获取数据表失败）：", e);
                return Message.messageToResponse(Message.error("Failed to get data table（获取数据表失败）", e));
            }
        }
        return Message.messageToResponse(Message.ok("").data("options", options));
    }

    @GET
    @Path("dbs")
    public Response queryDatabaseInfo(@Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode dbs = dataSourceService.getDbs(userName);
            return Message.messageToResponse(Message.ok("").data("dbs", dbs));
        } catch (Exception e) {
            logger.error("Failed to get database(获取数据库失败)：", e);
            return Message.messageToResponse(Message.error("Failed to get database(获取数据库失败)", e));
        }
    }

    @GET
    @Path("all")
    public Response queryDbsWithTables(@Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode dbs = dataSourceService.getDbsWithTables(userName);
            return Message.messageToResponse(Message.ok("").data("dbs", dbs));
        } catch (Exception e) {
            logger.error("Failed to get allDbs and tables(获取数据全部的库表信息失败)：", e);
            return Message.messageToResponse(Message.error("Failed to get allDbs and tables(获取数据全部的库表信息失败)", e));
        }
    }


    @GET
    @Path("tables")
    public Response queryTables(@QueryParam("database") String database, @Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode tables = dataSourceService.queryTables(database, userName);
            return Message.messageToResponse(Message.ok("").data("tables", tables));
        } catch (Exception e) {
            logger.error("Failed to get tables(获取数据表信息失败)：", e);
            return Message.messageToResponse(Message.error("Failed to get tables(获取数据表信息失败)", e));
        }
    }

    @GET
    @Path("columns")
    public Response queryTableMeta(@QueryParam("database") String database,  @QueryParam("table") String table, @Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode columns = dataSourceService.queryTableMeta(database, table, userName);
            return Message.messageToResponse(Message.ok("").data("columns", columns));
        } catch (Exception e) {
            logger.error("Failed to get data table structure(获取数据表结构失败):", e);
            return Message.messageToResponse(Message.error("Failed to get data table structure(获取数据表结构失败)", e));
        }
    }

    @GET
    @Path("size")
    public Response sizeOf(@QueryParam("database") String database,  @QueryParam("table") String table, @QueryParam("partition") String partition, @Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            JsonNode sizeNode;
            if (partition == null){
                sizeNode = dataSourceService.getTableSize(database, table, userName);
            } else {
                sizeNode = dataSourceService.getPartitionSize(database, table, partition, userName);
            }
            return Message.messageToResponse(Message.ok("").data("sizeInfo", sizeNode));
        } catch (Exception e) {
            logger.error("Failed to get table partition size(获取表分区大小失败):", e);
            return Message.messageToResponse(Message.error("Failed to get table partition size(获取表分区大小失败)", e));
        }
    }

    @GET
    @Path("partitions")
    public Response partitions(@QueryParam("database") String database,  @QueryParam("table") String table, @Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try{
            JsonNode partitionNode = dataSourceService.getPartitions(database, table, userName);
            return Message.messageToResponse(Message.ok("").data("partitionInfo", partitionNode));
        } catch (Exception e) {
            logger.error("Failed to get table partition(获取表分区失败):", e);
            return Message.messageToResponse(Message.error("Failed to get table partition(获取表分区失败)", e));
        }
    }
}
