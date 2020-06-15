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
import com.webank.wedatasphere.linkis.filesystem.bml.BMLHelper;
import com.webank.wedatasphere.linkis.filesystem.bml.BMLScriptReader;
import com.webank.wedatasphere.linkis.filesystem.bml.BMLScriptWriter;
import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.linkis.storage.script.ScriptMetaData;
import com.webank.wedatasphere.linkis.storage.script.ScriptRecord;
import com.webank.wedatasphere.linkis.storage.script.Variable;
import com.webank.wedatasphere.linkis.storage.script.VariableParser;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
                                      @QueryParam("fileName") String fileName,String userNameRpc) throws IOException, WorkSpaceException {
        String userName = req == null?userNameRpc: SecurityFilter.getLoginUsername(req);
        Map<String, Object> query = bmlHelper.query(userName,resourceId,version);
        if(fileName ==null) fileName = "test_faker.sql";  // TODO: 2019/5/31 通过物料库获取 文件类型
        BMLScriptReader reader = BMLScriptReader.getBMLScriptReader((InputStream) query.get("stream"), fileName);
        ScriptMetaData metaData = (ScriptMetaData) reader.getMetaData();
        Map<String, Object> params = VariableParser.getMap(metaData.getMetaData());
        StringBuilder scriptContent = new StringBuilder();
        while (reader.hasNext()){
            ScriptRecord record = (ScriptRecord) reader.getRecord();
            scriptContent.append(record.getLine() +"\n");
        }
        Message message = null;
        try {
            message = new Gson().fromJson(scriptContent.toString(), Message.class);
        }catch (Exception e){
            return Message.messageToResponse(Message.ok().data("scriptContent",scriptContent.toString()).data("metadata",params));
        }
        if(message.getStatus() != 0){
            throw new WorkSpaceException(message.getMessage());
        }
        return Message.messageToResponse(Message.ok().data("scriptContent",scriptContent.toString()).data("metadata",params));
    }

    @POST
    @Path("/saveScriptToBML")
    public Response saveScriptToBML(@Context HttpServletRequest req, @RequestBody Map<String, Object> json) throws IOException {
        String userName = req == null?(String) json.get("userName"): SecurityFilter.getLoginUsername(req);
        String scriptContent = (String) json.get("scriptContent");
        Map<String, Object> params = (Map<String, Object>)json.get("metadata");
        String fileName = (String) json.get("fileName");
        String resourceId = (String) json.get("resourceId");
        BMLScriptWriter writer = BMLScriptWriter.getBMLScriptWriter(fileName);
        Variable[] v = VariableParser.getVariables(params);
        List<Variable> variableList = Arrays.stream(v).filter(var -> !StringUtils.isEmpty(var.value())).collect(Collectors.toList());
        writer.addMetaData(new ScriptMetaData(variableList.toArray(new Variable[0])));
        writer.addRecord(new ScriptRecord(scriptContent));
        InputStream inputStream = writer.getInputStream();
        String version=null;
        if(resourceId == null){
            // TODO: 2019/5/28 新增文件
            Map<String, Object> bmlResponse = bmlHelper.upload(userName, inputStream, fileName);
            resourceId = bmlResponse.get("resourceId").toString();
            version = bmlResponse.get("version").toString();
        }else {
            // TODO: 2019/5/28 更新文件
            Map<String, Object> bmlResponse = bmlHelper.update(userName, resourceId, inputStream);
            resourceId = bmlResponse.get("resourceId").toString();
            version = bmlResponse.get("version").toString();
        }
        // TODO: 2019/5/28 close 流
        if(inputStream != null) inputStream.close();
        return Message.messageToResponse(Message.ok().data("resourceId",resourceId).data("version",version));
    }
}
