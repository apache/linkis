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
package com.webank.wedatasphere.linkis.filesystem.restful.api;

import com.google.gson.Gson;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.filesystem.bml.BMLHelper;
import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException;
import com.webank.wedatasphere.linkis.filesystem.exception.WorkspaceExceptionManager;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.linkis.storage.script.*;
import com.webank.wedatasphere.linkis.storage.script.writer.StorageScriptFsWriter;
import com.webank.wedatasphere.linkis.storage.source.FileSource;
import com.webank.wedatasphere.linkis.storage.source.FileSource$;
import org.apache.commons.math3.util.Pair;
import org.apache.http.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by patinousward
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON})
@Component
@Path("filesystem")
public class BMLFsRestfulApi {

    @Autowired
    BMLHelper bmlHelper;

    @GET
    @Path("/openScriptFromBML")
    public Response openScriptFromBML(@Context HttpServletRequest req,
                                      @QueryParam("resourceId") String resourceId,
                                      @QueryParam("version") String version,
                                      @QueryParam("creator") String creator,
                                      @QueryParam("projectName") String projectName,
                                      @DefaultValue("test.sql") @QueryParam("fileName") String fileName) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        Map<String, Object> query = bmlHelper.query(userName, resourceId, version);
        InputStream inputStream = (InputStream) query.get("stream");
        try (FileSource fileSource = FileSource$.MODULE$.create(new FsPath(fileName), inputStream)) {
            Pair<Object, ArrayList<String[]>> collect = fileSource.collect();
            Message message;
            try {
                message = new Gson().fromJson(collect.getSecond().get(0)[0], Message.class);
                if (message == null) throw WorkspaceExceptionManager.createException(80019);
            } catch (Exception e) {
                return Message.messageToResponse(Message.ok().data("scriptContent", collect.getSecond().get(0)[0]).data("metadata", collect.getFirst()));
            }
            if (message.getStatus() != 0) {
                throw new WorkSpaceException(80020, message.getMessage());
            }
            return Message.messageToResponse(Message.ok().data("scriptContent", collect.getSecond().get(0)[0]).data("metadata", collect.getFirst()));
        }
    }


    @GET
    @Path("/product/openScriptFromBML")
    public Response openScriptFromProductBML(@Context HttpServletRequest req,
                                      @QueryParam("resourceId") String resourceId,
                                      @QueryParam("version") String version,
                                      @QueryParam("creator") String creator,
                                      @DefaultValue("test.sql") @QueryParam("fileName") String fileName) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        if (!StringUtils.isEmpty(creator)){
            userName = creator;
        }
        Map<String, Object> query = bmlHelper.query(userName, resourceId, version);
        InputStream inputStream = (InputStream) query.get("stream");
        try (FileSource fileSource = FileSource$.MODULE$.create(new FsPath(fileName), inputStream)) {
            Pair<Object, ArrayList<String[]>> collect = fileSource.collect();
            Message message;
            try {
                message = new Gson().fromJson(collect.getSecond().get(0)[0], Message.class);
                if (message == null) {
                    throw WorkspaceExceptionManager.createException(80019);
                }
            } catch (Exception e) {
                return Message.messageToResponse(Message.ok().data("scriptContent", collect.getSecond().get(0)[0]).data("metadata", collect.getFirst()));
            }
            if (message.getStatus() != 0) {
                throw new WorkSpaceException(80020, message.getMessage());
            }
            return Message.messageToResponse(Message.ok().data("scriptContent", collect.getSecond().get(0)[0]).data("metadata", collect.getFirst()));
        }
    }



    @POST
    @Path("/saveScriptToBML")
    public Response saveScriptToBML(@Context HttpServletRequest req, @RequestBody Map<String, Object> json) throws IOException {
        String userName = SecurityFilter.getLoginUsername(req);
        String scriptContent = (String) json.get("scriptContent");
        Map<String, Object> params = (Map<String, Object>) json.get("metadata");
        String fileName = (String) json.get("fileName");
        String resourceId = (String) json.get("resourceId");
        String creator = (String)json.get("creator");
        String projectName = (String)json.get("projectName");
        ScriptFsWriter writer = StorageScriptFsWriter.getScriptFsWriter(new FsPath(fileName), Consts.UTF_8.toString(), null);
        Variable[] v = VariableParser.getVariables(params);
        List<Variable> variableList = Arrays.stream(v).filter(var -> !StringUtils.isEmpty(var.value())).collect(Collectors.toList());
        writer.addMetaData(new ScriptMetaData(variableList.toArray(new Variable[0])));
        writer.addRecord(new ScriptRecord(scriptContent));
        try (InputStream inputStream = writer.getInputStream()) {
            String version;
            if (resourceId == null) {
                //  新增文件
                Map<String, Object> bmlResponse = new HashMap<>();
                if (!StringUtils.isEmpty(projectName)) {
                    bmlResponse = bmlHelper.upload(userName, inputStream, fileName, projectName);
                }else{
                    bmlResponse = bmlHelper.upload(userName, inputStream, fileName);
                }
                resourceId = bmlResponse.get("resourceId").toString();
                version = bmlResponse.get("version").toString();
            } else {
                //  更新文件
                Map<String, Object> bmlResponse = bmlHelper.update(userName, resourceId, inputStream);
                resourceId = bmlResponse.get("resourceId").toString();
                version = bmlResponse.get("version").toString();
            }
            return Message.messageToResponse(Message.ok().data("resourceId", resourceId).data("version", version));
        }
    }
}
