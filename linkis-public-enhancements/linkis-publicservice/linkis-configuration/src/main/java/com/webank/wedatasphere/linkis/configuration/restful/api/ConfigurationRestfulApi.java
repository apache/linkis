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

import com.netflix.discovery.converters.Auto;
import com.webank.wedatasphere.linkis.configuration.entity.*;
import com.webank.wedatasphere.linkis.configuration.exception.ConfigurationException;
import com.webank.wedatasphere.linkis.configuration.service.CategoryService;
import com.webank.wedatasphere.linkis.configuration.service.ConfigurationService;
import com.webank.wedatasphere.linkis.configuration.util.ConfigurationConfiguration;
import com.webank.wedatasphere.linkis.configuration.util.JsonNodeUtil;
import com.webank.wedatasphere.linkis.configuration.util.LabelEntityParser;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.UserCreatorLabel;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json4s.jackson.Json;
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
    private ConfigurationService configurationService;

    @Autowired
    private CategoryService categoryService;

    ObjectMapper mapper = new ObjectMapper();

    private static final String NULL = "null";

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
        List<CategoryLabelVo> categoryLabelList = categoryService.getAllCategory();
        return Message.messageToResponse(Message.ok().data("Category", categoryLabelList));
    }

    @POST
    @Path("/createFirstCategory")
    public Response createFirstCategory(@Context HttpServletRequest request, JsonNode jsonNode) throws ConfigurationException {
        String username = SecurityFilter.getLoginUsername(request);
        String categoryName = jsonNode.get("categoryName").asText();
        String description = jsonNode.get("description").asText();
        if(StringUtils.isEmpty(categoryName) || categoryName.equals(NULL)){
            throw new ConfigurationException("categoryName is null, cannot be added");
        }
        categoryService.createFirstCategory(categoryName, description);
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/deleteCategory")
    public Response deleteCategory(@Context HttpServletRequest request, JsonNode jsonNode){
        String username = SecurityFilter.getLoginUsername(request);
        Integer categoryId = jsonNode.get("categoryId").asInt();
        categoryService.deleteCategory(categoryId);
        return Message.messageToResponse(Message.ok());
    }


    @POST
    @Path("/createSecondCategory")
    public Response createSecondCategory(@Context HttpServletRequest request, JsonNode jsonNode) throws ConfigurationException {
        String username = SecurityFilter.getLoginUsername(request);
        Integer categoryId = jsonNode.get("categoryId").asInt();
        String engineType = jsonNode.get("engineType").asText();
        String version = jsonNode.get("version").asText();
        String description = jsonNode.get("description").asText();
        if(StringUtils.isEmpty(categoryId) || categoryId <= 0){
            throw new ConfigurationException("creator is null, cannot be added");
        }
        if(StringUtils.isEmpty(engineType) || engineType.toLowerCase().equals(NULL)){
            throw new ConfigurationException("engine type is null, cannot be added");
        }
        if(StringUtils.isEmpty(version) || version.toLowerCase().equals(NULL)){
            version = LabelUtils.COMMON_VALUE;
        }
        categoryService.createSecondCategory(categoryId, engineType, version, description);
        return Message.messageToResponse(Message.ok());
    }


    @POST
    @Path("/saveFullTree")
    public Response saveFullTree(@Context HttpServletRequest req, JsonNode json) throws IOException, ConfigurationException {
        List fullTrees = mapper.readValue(json.get("fullTree"), List.class);
        String creator = JsonNodeUtil.getStringValue(json.get("creator"));
        String engineType = JsonNodeUtil.getStringValue(json.get("engineType"));
        if(creator != null && (creator.equals("通用设置") || creator.equals("全局设置"))){
            creator = "*";
        }
        String username = SecurityFilter.getLoginUsername(req);
        ArrayList<ConfigValue> createList = new ArrayList<>();
        ArrayList<ConfigValue> updateList = new ArrayList<>();
        for (Object o : fullTrees) {
            String s = BDPJettyServerHelper.gson().toJson(o);
            ConfigTree fullTree = BDPJettyServerHelper.gson().fromJson(s, ConfigTree.class);
            List<ConfigKeyValue> settings = fullTree.getSettings();
            Integer userLabelId = configurationService.checkAndCreateUserLabel(settings, username, creator);
            for (ConfigKeyValue setting : settings) {
                configurationService.updateUserValue(setting, userLabelId, createList, updateList);
            }
        }
        String engine = null;
        String version = null;
        if(engineType != null){
            String[] tmpString = engineType.split("-");
            if(tmpString.length != 2){
                throw new ConfigurationException("The saved engine type parameter is incorrect, please send it in a fixed format, such as spark-2.4.3(保存的引擎类型参数有误，请按照固定格式传送，例如spark-2.4.3)");
            }
            engine = tmpString[0];
            version = tmpString[1];
        }
        configurationService.updateUserValue(createList, updateList);
        configurationService.clearAMCacheConf(username,creator,engine,version);
        Message message = Message.ok();
        return Message.messageToResponse(message);
    }

    @GET
    @Path("/engineType")
    public Response listAllEngineType(@Context HttpServletRequest request){
        String[] engineType = configurationService.listAllEngineType();
        return Message.messageToResponse(Message.ok().data("engineType",engineType));
    }

    @POST
    @Path("/updateCategoryInfo")
    public Response updateCategoryInfo(@Context HttpServletRequest request, JsonNode jsonNode) throws ConfigurationException {
        String username = SecurityFilter.getLoginUsername(request);
        String description = null;
        Integer categoryId = null;
        try {
            description = jsonNode.get("description").asText();
            categoryId = jsonNode.get("categoryId").asInt();
        }catch (Exception e){
            throw new ConfigurationException("请求参数不完整，请重新确认");
        }
        if(description != null){
            categoryService.updateCategory(categoryId, description);
        }
        return Message.messageToResponse(Message.ok());
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
