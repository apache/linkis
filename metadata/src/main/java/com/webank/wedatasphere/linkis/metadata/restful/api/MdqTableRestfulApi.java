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


import com.webank.wedatasphere.linkis.metadata.ddl.ImportDDLCreator;
import com.webank.wedatasphere.linkis.metadata.ddl.ScalaDDLCreator;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableBO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableImportInfoBO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableBaseInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableFieldsInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableStatisticInfoVO;
import com.webank.wedatasphere.linkis.metadata.service.MdqService;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("datasource")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component
public class MdqTableRestfulApi {

    private static final Logger logger = LoggerFactory.getLogger(MdqTableRestfulApi.class);

    @Autowired
    private MdqService mdqService;
    ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("getTableBaseInfo")
    public Response getTableBaseInfo(@QueryParam("database") String database, @QueryParam("tableName") String tableName,
                                     @Context HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        MdqTableBaseInfoVO tableBaseInfo;
        if (mdqService.isExistInMdq(database, tableName, userName)) {
            tableBaseInfo = mdqService.getTableBaseInfoFromMdq(database, tableName, userName);
        } else {
            tableBaseInfo = mdqService.getTableBaseInfoFromHive(database, tableName, userName);
        }
        return Message.messageToResponse(Message.ok().data("tableBaseInfo",tableBaseInfo));
    }

    @GET
    @Path("getTableFieldsInfo")
    public Response getTableFieldsInfo(@QueryParam("database") String database, @QueryParam("tableName") String tableName,
                                       @Context HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<MdqTableFieldsInfoVO> tableFieldsInfo;
        if (mdqService.isExistInMdq(database, tableName, userName)) {
            tableFieldsInfo = mdqService.getTableFieldsInfoFromMdq(database, tableName, userName);
        } else {
            tableFieldsInfo = mdqService.getTableFieldsInfoFromHive(database, tableName, userName);
        }
        return Message.messageToResponse(Message.ok().data("tableFieldsInfo",tableFieldsInfo));
    }

    @GET
    @Path("getTableStatisticInfo")
    public Response getTableStatisticInfo(@QueryParam("database") String database, @QueryParam("tableName") String tableName,
                                          @Context HttpServletRequest req) throws IOException {
        String userName = SecurityFilter.getLoginUsername(req);
        MdqTableStatisticInfoVO tableStatisticInfo = mdqService.getTableStatisticInfo(database, tableName, userName);
        return Message.messageToResponse(Message.ok().data("tableStatisticInfo",tableStatisticInfo));
    }

    @GET
    @Path("active")
    public Response active(@QueryParam("tableId") Long tableId,@Context HttpServletRequest req) {
        mdqService.activateTable(tableId);
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("persistTable")
    public Response persistTable(@Context HttpServletRequest req,JsonNode json) throws IOException {
        String userName = SecurityFilter.getLoginUsername(req);
        MdqTableBO table = mapper.readValue(json.get("table"), MdqTableBO.class);
        mdqService.persistTable(table,userName);
        return Message.messageToResponse(Message.ok());
    }


    @POST
    @Path("displaysql")
    public Response displaySql(@Context HttpServletRequest request, JsonNode json){
        String userName = SecurityFilter.getLoginUsername(request);
        logger.info("display sql for user {} ", userName);
        StringBuilder sb = new StringBuilder();
        String retSql = "";
        MdqTableBO tableBO = null;
        try{
            tableBO = mapper.readValue(json.get("table"), MdqTableBO.class);
            MdqTableImportInfoBO importInfo = tableBO.getImportInfo();
            if (importInfo != null){
                retSql = ImportDDLCreator.createDDL(tableBO, userName);
            }else{
                retSql = ScalaDDLCreator.createDDL(tableBO, userName);
            }
        }catch(Exception e){
            logger.error("json parse to bean failed",e);
            Message message = Message.error("display ddl failed");
            return Message.messageToResponse(message);
        }
        String tableName = tableBO.getTableBaseInfo().getBase().getName();
        String dbName = tableBO.getTableBaseInfo().getBase().getDatabase();
        String retStr = "意书后台正在为您生成新建库表: " + dbName + "." + tableName + "的DDL语句,请点击建表按钮进行执行";
        Message message = Message.ok(retStr);
        message.setMethod("/api/datasource/display");
        message.data("sql", retSql);
        return Message.messageToResponse(message);
    }

}
