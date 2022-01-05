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

import org.apache.linkis.metadatamanager.common.domain.MetaColumnInfo;
import org.apache.linkis.metadatamanager.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadatamanager.server.service.MetadataAppService;
import org.apache.linkis.server.Message;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/metadata")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component
public class MetadataCoreRestful {

    @Autowired
    private MetadataAppService metadataAppService;

    @GET
    @Path("/dbs/{data_source_id}")
    public Response getDatabases(@PathParam("data_source_id")String dataSourceId,
                                 @QueryParam("system")String system){
        try{
            if(StringUtils.isBlank(system)){
                return Message.messageToResponse(Message.error("'system' is missing[缺少系统名]"));
            }
            List<String> databases = metadataAppService.getDatabasesByDsId(dataSourceId, system);
            return Message.messageToResponse(Message.ok().data("dbs", databases));
        }catch(Exception e){
            return Message.messageToResponse(
                    Message.error("Fail to get database list[获取库信息失败], id:[" + dataSourceId +"], system:[" + system + "]", e));
        }
    }

    @GET
    @Path("/tables/{data_source_id}/db/{database}")
    public Response getTables(@PathParam("data_source_id")String dataSourceId,
                              @PathParam("database")String database,
                              @QueryParam("system")String system){
        try{
            if(StringUtils.isBlank(system)){
                return Message.messageToResponse(Message.error("'system' is missing[缺少系统名]"));
            }
            List<String> tables = metadataAppService.getTablesByDsId(dataSourceId, database, system);
            return Message.messageToResponse(Message.ok().data("tables", tables));
        }catch(Exception e){
            return Message.messageToResponse(
                    Message.error("Fail to get table list[获取表信息失败], id:[" + dataSourceId +"]" +
                            ", system:[" + system + "], database:[" +database +"]", e));
        }
    }

    @GET
    @Path("/props/{data_source_id}/db/{database}/table/{table}")
    public Response getTableProps(@PathParam("data_source_id")String dataSourceId,
                                  @PathParam("database")String database,
                                  @PathParam("table") String table,
                                  @QueryParam("system")String system){
        try{
            if(StringUtils.isBlank(system)){
                return Message.messageToResponse(Message.error("'system' is missing[缺少系统名]"));
            }
            Map<String, String> tableProps = metadataAppService.getTablePropsByDsId(dataSourceId, database, table, system);
            return Message.messageToResponse(Message.ok().data("props", tableProps));
        }catch(Exception e){
            return Message.messageToResponse(
                    Message.error("Fail to get table properties[获取表参数信息失败], id:[" + dataSourceId +"]" +
                            ", system:[" + system + "], database:[" +database +"], table:[" + table +"]", e));
        }
    }

    @GET
    @Path("/partitions/{data_source_id}/db/{database}/table/{table}")
    public Response getPartitions(@PathParam("data_source_id")String dataSourceId,
                                  @PathParam("database")String database,
                                  @PathParam("table") String table,
                                  @QueryParam("system")String system){
        try{
            if(StringUtils.isBlank(system)){
                return Message.messageToResponse(Message.error("'system' is missing[缺少系统名]"));
            }
            MetaPartitionInfo partitionInfo = metadataAppService.getPartitionsByDsId(dataSourceId, database, table, system);
            return Message.messageToResponse(Message.ok().data("props", partitionInfo));
        }catch(Exception e){
            return Message.messageToResponse(
                    Message.error("Fail to get partitions[获取表分区信息失败], id:[" + dataSourceId +"]" +
                            ", system:[" + system + "], database:[" +database +"], table:[" + table +"]"));
        }
    }

    @GET
    @Path("/columns/{data_source_id}/db/{database}/table/{table}")
    public Response getColumns(@PathParam("data_source_id")String dataSourceId,
                               @PathParam("database")String database,
                               @PathParam("table") String table,
                               @QueryParam("system")String system){
        try{
            if(StringUtils.isBlank(system)){
                return Message.messageToResponse(Message.error("'system' is missing[缺少系统名]"));
            }
            List<MetaColumnInfo> columns = metadataAppService.getColumns(dataSourceId, database, table, system);
            return Message.messageToResponse(Message.ok().data("columns", columns));
        }catch(Exception e){
            return Message.messageToResponse(
                    Message.error("Fail to get column list[获取表字段信息失败], id:[" + dataSourceId +"]" +
                            ", system:[" + system + "], database:[" +database +"], table:[" + table +"]", e));
        }
    }

}
