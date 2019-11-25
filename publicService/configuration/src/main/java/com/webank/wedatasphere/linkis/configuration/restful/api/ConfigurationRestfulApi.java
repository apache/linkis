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

package com.webank.wedatasphere.linkis.configuration.restful.api;

import com.webank.wedatasphere.linkis.configuration.entity.ConfigKey;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigKeyUser;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigKeyValueVO;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigTree;
import com.webank.wedatasphere.linkis.configuration.service.ConfigurationService;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by allenlliu on 2018/10/17.
 */
@Component
@Path("/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationRestfulApi {

    @Autowired
    ConfigurationService configurationService;

    ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/getFullTreesByAppName")
    public Response getFullTreesByAppName(@Context HttpServletRequest req, @QueryParam("appName") String appName, @QueryParam("creator") String creator) {
        String username = SecurityFilter.getLoginUsername(req);
        List<ConfigTree> configTrees = configurationService.getFullTree(appName,username,creator);
        return Message.messageToResponse(Message.ok().data("fullTree", configTrees));
    }

    @POST
    @Path("/saveFullTree")
    public Response saveFullTree(@Context HttpServletRequest req, JsonNode json) throws IOException {
        List fullTrees = mapper.readValue(json.get("fullTree"), List.class);
        String username = SecurityFilter.getLoginUsername(req);
        for (Object o : fullTrees) {
            String s = BDPJettyServerHelper.gson().toJson(o);
            ConfigTree fullTree = BDPJettyServerHelper.gson().fromJson(s, ConfigTree.class);
            recursiveFullTree(fullTree,username);
        }
        Message message = Message.ok();
        return Message.messageToResponse(message);
    }

    private void recursiveFullTree(ConfigTree fullTree,String userName){
        List<ConfigKeyValueVO> settings = fullTree.getSettings();
/*        for (Map.Entry<String, String> stringStringEntry : kvs.entrySet()) {
            ConfigKey key =  BDPJettyServerHelper.gson().fromJson(stringStringEntry.getKey(), ConfigKey.class);
            ConfigKeyUser value = BDPJettyServerHelper.gson().fromJson(stringStringEntry.getValue(), ConfigKeyUser.class);
            configurationService.updateUserValue(key,value);
        }*/
        for (ConfigKeyValueVO setting : settings) {
            configurationService.updateUserValue(setting);
        }
        List<ConfigTree> childrens = fullTree.getChildrens();
        if (!childrens.isEmpty()){
            for (ConfigTree children : childrens) {
                recursiveFullTree(children,userName);
            }
        }
    }


}
