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

import com.webank.wedatasphere.linkis.configuration.entity.CategoryLabelVo;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigKey;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigKeyValue;
import com.webank.wedatasphere.linkis.configuration.entity.ConfigTree;
import com.webank.wedatasphere.linkis.configuration.exception.ConfigurationException;
import com.webank.wedatasphere.linkis.configuration.service.ConfigurationService;
import com.webank.wedatasphere.linkis.configuration.util.ConfigurationConfiguration;
import com.webank.wedatasphere.linkis.configuration.util.LabelEntityParser;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.UserCreatorLabel;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationRestfulApi {

    @Autowired
    ConfigurationService configurationService;

    ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/addKeyForEngine")
    public Response addKeyForEngine(@Context HttpServletRequest req,
                                   @QueryParam("engineType") String engineType,
                                   @QueryParam("version") String version,
                                   @QueryParam("token") String token,
                                   @QueryParam("keyJson") String keyJson) throws ConfigurationException {
        String username = SecurityFilter.getLoginUsername(req);
        if(StringUtils.isEmpty(engineType) || StringUtils.isEmpty(version) || StringUtils.isEmpty(token)){
            throw new ConfigurationException("params cannot be empty!");
        }
        //todo 检验token
        if(!token.equals(ConfigurationConfiguration.COPYKEYTOKEN)){
            throw new ConfigurationException("token is error");
        }
        ConfigKey configKey = BDPJettyServerHelper.gson().fromJson(keyJson, ConfigKey.class);
        configurationService.addKeyForEngine(engineType,version,configKey);
        // TODO: 2019/12/30  configKey参数校验
        return Message.messageToResponse(Message.ok());
    }


    //TODO addKeyForCreator

//    @GET
//    @Path("/addKeyForCreator")
//    public Response addKeyForCreator(@Context HttpServletRequest req,
//                                    @QueryParam("engineType") String engineType,
//                                    @QueryParam("creator") String creator,
//                                    @QueryParam("token") String token,
//                                    @QueryParam("keyJson") String keyJson) throws ConfigurationException {
//        String username = SecurityFilter.getLoginUsername(req);
//        if(StringUtils.isEmpty(engineType) || StringUtils.isEmpty(creator) || StringUtils.isEmpty(token)){
//            throw new ConfigurationException("params cannot be empty!");
//        }
//        //todo 检验token
//        if(!token.equals(ConfigurationConfiguration.COPYKEYTOKEN)){
//            throw new ConfigurationException("token is error");
//        }
//        List<ConfigKey> keys = configurationService.listKeyByCreatorAndAppName(creator,engineType);
//        if(keys.isEmpty()){
//            //判断和copyKeyFromIDE相反,只允许在有key的情况下添加
//            throw new ConfigurationException(creator + ":" + engineType +  ",keys is empty ,cannot add key");
//        }
//        ConfigKey configKey = BDPJettyServerHelper.gson().fromJson(keyJson, ConfigKey.class);
//        // TODO: 2019/12/30  configKey参数校验
//        configurationService.addKey(creator,engineType,configKey);
//        return Message.messageToResponse(Message.ok());
//    }



    //TODO copyKey

//    @GET
//    @Path("/copyKeyFromIDE")
//    public Response copyKeyFromIDE(@Context HttpServletRequest req,
//                                  @QueryParam("appName") String appName,
//                                  @QueryParam("creator") String creator,
//                                  @QueryParam("token") String token) throws ConfigurationException {
//        String username = SecurityFilter.getLoginUsername(req);
//        if(StringUtils.isEmpty(appName) || StringUtils.isEmpty(creator) || StringUtils.isEmpty(token)){
//            throw new ConfigurationException("params cannot be empty!");
//        }
//        //todo检验token
//        if(!token.equals(ConfigurationConfiguration.COPYKEYTOKEN)){
//            throw new ConfigurationException("token is error");
//        }
//        List<ConfigKey> keys = configurationService.listKeyByCreatorAndAppName(creator,appName);
//        if(!keys.isEmpty()){
//            throw new ConfigurationException(creator + ":" + appName +  ",keys is no empty, cannot copy key");
//        }
//        configurationService.insertCreator(creator);
//        List<ConfigKey> IDEkeys = configurationService.listKeyByCreatorAndAppName("IDE",appName);
//        if (IDEkeys.isEmpty()) {
//            throw new ConfigurationException("IDE:"+ appName + ",cannot find any key to copy");
//        }
//        IDEkeys.forEach(k ->configurationService.copyKeyFromIDE(k,creator,appName));
//        return Message.messageToResponse(Message.ok());
//    }

    @GET
    @Path("/getFullTreesByAppName")
    public Response getFullTreesByAppName(@Context HttpServletRequest req, @QueryParam("engineType") String engineType, @QueryParam("version") String version, @QueryParam("creator") String creator) throws ConfigurationException {
        String username = SecurityFilter.getLoginUsername(req);
        if(creator != null && (creator.equals("通用设置") || creator.equals("全局设置"))){
            engineType = "*";
            version = "*";
            creator = "*";
        }
        List labelList = LabelEntityParser.generateUserCreatorEngineTypeLabelList(username, creator, engineType, version);
        ArrayList<ConfigTree> configTrees = configurationService.getFullTreeByLabelList(labelList,true);
        return Message.messageToResponse(Message.ok().data("fullTree", configTrees));
    }

    @GET
    @Path("/getCategory")
    public Response getCategory(@Context HttpServletRequest req){
        String username = SecurityFilter.getLoginUsername(req);
        List<CategoryLabelVo> categoryLabelList = configurationService.getCategory();
        return Message.messageToResponse(Message.ok().data("Category", categoryLabelList));
    }

    /*TODO add restfule interface about createCategory and deleteCategory*/

    @POST
    @Path("/saveFullTree")
    public Response saveFullTree(@Context HttpServletRequest req, JsonNode json) throws IOException {
        List fullTrees = mapper.readValue(json.get("fullTree"), List.class);
        String username = SecurityFilter.getLoginUsername(req);
        for (Object o : fullTrees) {
            String s = BDPJettyServerHelper.gson().toJson(o);
            ConfigTree fullTree = BDPJettyServerHelper.gson().fromJson(s, ConfigTree.class);
            List<ConfigKeyValue> settings = fullTree.getSettings();
            for (ConfigKeyValue setting : settings) {
                configurationService.updateUserValue(setting);
            }
        }
        Message message = Message.ok();
        return Message.messageToResponse(message);
    }

    @GET
    @Path("/rpcTest")
    public Response rpcTest(@QueryParam("username") String username, @QueryParam("creator") String creator, @QueryParam("engineType") String engineType,@QueryParam("version") String version){
        configurationService.queryGlobalConfig(username);
        EngineTypeLabel engineTypeLabel = new EngineTypeLabel();
        engineTypeLabel.setVersion(version);
        engineTypeLabel.setEngineType(engineType);
        configurationService.queryDefaultEngineConfig(engineTypeLabel);
        UserCreatorLabel userCreatorLabel = new UserCreatorLabel();
        userCreatorLabel.setCreator(creator);
        userCreatorLabel.setUser(username);
        configurationService.queryConfig(userCreatorLabel,engineTypeLabel,"wds.linkis.rm");
        Message message = Message.ok();
        return Message.messageToResponse(message);
    }

}
