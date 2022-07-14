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

package org.apache.linkis.configuration.restful.api;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.configuration.entity.*;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.CategoryService;
import org.apache.linkis.configuration.service.ConfigKeyService;
import org.apache.linkis.configuration.service.ConfigurationService;
import org.apache.linkis.configuration.util.ConfigurationConfiguration;
import org.apache.linkis.configuration.util.JsonNodeUtil;
import org.apache.linkis.configuration.util.LabelEntityParser;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*@Api(tags = "参数配置")*/
@Api(tags = "Parameter_Configuration")
@RestController
@RequestMapping(path = "/configuration")
public class ConfigurationRestfulApi {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationRestfulApi.class);

    @Autowired private ConfigurationService configurationService;

    @Autowired private CategoryService categoryService;

    @Autowired private ConfigKeyService configKeyService;

    ObjectMapper mapper = new ObjectMapper();

    private static final String NULL = "null";

    /*    @ApiOperation(value = "添加KeyForEngine", notes = "添加KeyForEngine", response = Message.class)*/
    @ApiOperation(value = "AddKeyForEngine", notes = "Add_Key_For_Engine", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "engineType", dataType = "String", value = "Engine_Type"),
        @ApiImplicitParam(name = "version", dataType = "String", value = "Version"),
        @ApiImplicitParam(name = "token", dataType = "String", value = "Token"),
        @ApiImplicitParam(name = "keyJson", dataType = "String", value = "Key_Json")
    })
    @RequestMapping(path = "/addKeyForEngine", method = RequestMethod.GET)
    public Message addKeyForEngine(
            HttpServletRequest req,
            @RequestParam(value = "engineType", required = false) String engineType,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "keyJson", required = false) String keyJson)
            throws ConfigurationException {
        if (StringUtils.isBlank(engineType)
                || StringUtils.isBlank(version)
                || StringUtils.isBlank(token)) {
            throw new ConfigurationException("params cannot be empty!");
        }
        // todo 检验token
        if (!token.equals(ConfigurationConfiguration.COPYKEYTOKEN)) {
            throw new ConfigurationException("token is error");
        }
        ConfigKey configKey = BDPJettyServerHelper.gson().fromJson(keyJson, ConfigKey.class);
        configurationService.addKeyForEngine(engineType, version, configKey);
        // TODO: 2019/12/30  configKey参数校验
        return Message.ok();
    }

    /*    @ApiOperation(value = "队列资源", notes = "参数配置中的队列资源模块返回队列资源的列及值", response = Message.class)*/
    @ApiOperation(
            value = "GetFullTreesByAppName",
            notes = "Get_Full_Trees_By_App_Name",
            response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "engineType", dataType = "String", value = "Engine_Type"),
        @ApiImplicitParam(name = "version", dataType = "String", value = "Version"),
        @ApiImplicitParam(name = "creator", dataType = "String", value = "Creator")
    })
    @RequestMapping(path = "/getFullTreesByAppName", method = RequestMethod.GET)
    public Message getFullTreesByAppName(
            HttpServletRequest req,
            @RequestParam(value = "engineType", required = false) String engineType,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "creator", required = false) String creator)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(req, "getFullTreesByAppName");
        if (creator != null && (creator.equals("通用设置") || creator.equals("全局设置"))) {
            engineType = "*";
            version = "*";
            creator = "*";
        }
        List labelList =
                LabelEntityParser.generateUserCreatorEngineTypeLabelList(
                        username, creator, engineType, version);
        ArrayList<ConfigTree> configTrees =
                configurationService.getFullTreeByLabelList(labelList, true);
        return Message.ok().data("fullTree", configTrees);
    }

    /*@ApiOperation(value = "应用类型", notes = "参数配置中应用类型标签", response = Message.class)*/
    @ApiOperation(value = "GetCategory", notes = "Get_Category", response = Message.class)
    @RequestMapping(path = "/getCategory", method = RequestMethod.GET)
    public Message getCategory(HttpServletRequest req) {
        List<CategoryLabelVo> categoryLabelList = categoryService.getAllCategory();
        return Message.ok().data("Category", categoryLabelList);
    }

    /*@ApiOperation(value = "新增应用类型", notes = "新增应用类型标签", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "categoryName", dataType = "String", required = true, value = "引用类型标签名称"),
        @ApiImplicitParam(name = "description", dataType = "STring", required = true, value = "描述")
    })*/
    @ApiOperation(
            value = "CreateFirstCategory",
            notes = "Create_First_Category",
            response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "categoryName",
                dataType = "String",
                required = true,
                value = "Category_Name"),
        @ApiImplicitParam(
                name = "description",
                dataType = "STring",
                required = true,
                value = "Description")
    })
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/createFirstCategory", method = RequestMethod.POST)
    public Message createFirstCategory(HttpServletRequest request, @RequestBody JsonNode jsonNode)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(request, "createFirstCategory");
        checkAdmin(username);
        String categoryName = jsonNode.get("categoryName").asText();
        String description = jsonNode.get("description").asText();
        if (StringUtils.isEmpty(categoryName) || categoryName.equals(NULL)) {
            throw new ConfigurationException("categoryName is null, cannot be added");
        }
        if (StringUtils.isEmpty(categoryName) || categoryName.contains("-")) {
            throw new ConfigurationException("categoryName cannot be included '-'");
        }
        categoryService.createFirstCategory(categoryName, description);
        return Message.ok();
    }

    /*@ApiOperation(value = "删除配置", notes = "删除参数配置", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "categoryId", dataType = "String", required = true, value = "参数配置Id")
    })*/
    @ApiOperation(value = "DeleteCategory", notes = "Delete_Category", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "categoryId",
                dataType = "String",
                required = true,
                value = "Category_Id")
    })
    @ApiOperationSupport(ignoreParameters = "jsonNode")
    @RequestMapping(path = "/deleteCategory", method = RequestMethod.POST)
    public Message deleteCategory(HttpServletRequest request, @RequestBody JsonNode jsonNode)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(request, "deleteCategory");
        checkAdmin(username);
        Integer categoryId = jsonNode.get("categoryId").asInt();
        categoryService.deleteCategory(categoryId);
        return Message.ok();
    }

    /*@ApiOperation(value = "新增参数配置", notes = "添加参数配置", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "categoryId", dataType = "String", required = true, value = "参数配置Id"),
        @ApiImplicitParam(name = "engineType", dataType = "String", required = true, value = "引擎类型"),
        @ApiImplicitParam(name = "version", dataType = "String", required = true, value = "版本号"),
        @ApiImplicitParam(name = "description", dataType = "String", required = true, value = "描述"),
    })*/
    @ApiOperation(
            value = "CreateSecondCategory",
            notes = "Create_Second_Category",
            response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "categoryId",
                dataType = "String",
                required = true,
                value = "Category_Id"),
        @ApiImplicitParam(
                name = "engineType",
                dataType = "String",
                required = true,
                value = "Engine_Type"),
        @ApiImplicitParam(
                name = "version",
                dataType = "String",
                required = true,
                value = "Version"),
        @ApiImplicitParam(
                name = "description",
                dataType = "String",
                required = true,
                value = "Description"),
    })
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/createSecondCategory", method = RequestMethod.POST)
    public Message createSecondCategory(HttpServletRequest request, @RequestBody JsonNode jsonNode)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(request, "createSecondCategory");
        checkAdmin(username);
        Integer categoryId = jsonNode.get("categoryId").asInt();
        String engineType = jsonNode.get("engineType").asText();
        String version = jsonNode.get("version").asText();
        String description = jsonNode.get("description").asText();
        if (categoryId <= 0) {
            throw new ConfigurationException("creator is null, cannot be added");
        }
        if (StringUtils.isEmpty(engineType) || engineType.toLowerCase().equals(NULL)) {
            throw new ConfigurationException("engine type is null, cannot be added");
        }
        if (StringUtils.isEmpty(version) || version.toLowerCase().equals(NULL)) {
            version = LabelUtils.COMMON_VALUE;
        }
        categoryService.createSecondCategory(categoryId, engineType, version, description);
        return Message.ok();
    }

    /*@ApiOperation(value = "保存队列资源", notes = "保存队列资源", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "creator", dataType = "String", required = true, value = "应用类型名称"),
        @ApiImplicitParam(name = "engineType", dataType = "String", required = true, value = "引擎类型"),
        @ApiImplicitParam(name = "fullTree", dataType = "List", required = true, value = "应用类型下的详细信息"),
        @ApiImplicitParam(name = "name", dataType = "String", required = true, value = "队列资源名称,属于fullTree中的内容"),
        @ApiImplicitParam(name = "description", dataType = "String", required = true, value = "描述，属于fullTree中的内容"),
        @ApiImplicitParam(name = "settings", dataType = "List", required = true, value = "队列资源中的详细内容，属于fullTree中的内容")
    })*/
    @ApiOperation(value = "SaveFullTree", notes = "Save_Full_Tree", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "creator",
                dataType = "String",
                required = true,
                value = "Creator"),
        @ApiImplicitParam(
                name = "engineType",
                dataType = "String",
                required = true,
                value = "Engine_Type"),
        @ApiImplicitParam(
                name = "fullTree",
                dataType = "List",
                required = true,
                value = "Full_Tree"),
        @ApiImplicitParam(name = "name", dataType = "String", required = true, value = "Name"),
        @ApiImplicitParam(
                name = "description",
                dataType = "String",
                required = true,
                value = "Description"),
        @ApiImplicitParam(name = "settings", dataType = "List", required = true, value = "Settings")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/saveFullTree", method = RequestMethod.POST)
    public Message saveFullTree(HttpServletRequest req, @RequestBody JsonNode json)
            throws IOException, ConfigurationException {
        List fullTrees = mapper.treeToValue(json.get("fullTree"), List.class);
        String creator = JsonNodeUtil.getStringValue(json.get("creator"));
        String engineType = JsonNodeUtil.getStringValue(json.get("engineType"));
        if (creator != null && (creator.equals("通用设置") || creator.equals("全局设置"))) {
            creator = "*";
        }
        String username = ModuleUserUtils.getOperationUser(req, "saveFullTree");
        ArrayList<ConfigValue> createList = new ArrayList<>();
        ArrayList<ConfigValue> updateList = new ArrayList<>();
        for (Object o : fullTrees) {
            String s = BDPJettyServerHelper.gson().toJson(o);
            ConfigTree fullTree = BDPJettyServerHelper.gson().fromJson(s, ConfigTree.class);
            List<ConfigKeyValue> settings = fullTree.getSettings();
            Integer userLabelId =
                    configurationService.checkAndCreateUserLabel(settings, username, creator);
            for (ConfigKeyValue setting : settings) {
                configurationService.updateUserValue(setting, userLabelId, createList, updateList);
            }
        }
        String engine = null;
        String version = null;
        if (engineType != null) {
            String[] tmpString = engineType.split("-");
            if (tmpString.length != 2) {
                throw new ConfigurationException(
                        "The saved engine type parameter is incorrect, please send it in a fixed format, such as spark-2.4.3(保存的引擎类型参数有误，请按照固定格式传送，例如spark-2.4.3)");
            }
            engine = tmpString[0];
            version = tmpString[1];
        }
        configurationService.updateUserValue(createList, updateList);
        configurationService.clearAMCacheConf(username, creator, engine, version);
        Message message = Message.ok();
        return message;
    }

    /*@ApiOperation(value = "引擎类型列表", notes = "获取引擎类型列表", response = Message.class)*/
    @ApiOperation(
            value = "ListAllEngineType",
            notes = "List_All_Engine_Type",
            response = Message.class)
    @RequestMapping(path = "/engineType", method = RequestMethod.GET)
    public Message listAllEngineType(HttpServletRequest request) {
        String[] engineType = configurationService.listAllEngineType();
        return Message.ok().data("engineType", engineType);
    }

    /*@ApiOperation(value = "更新类别信息", notes = "更新类别信息", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "description", dataType = "String", required = true, value = "description"),
        @ApiImplicitParam(name = "categoryId", dataType = "String", required = true, value = "categoryId")
    })*/
    @ApiOperation(
            value = "UpdateCategoryInfo",
            notes = "Update_Category_Info",
            response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "description",
                dataType = "String",
                required = true,
                value = "Description"),
        @ApiImplicitParam(
                name = "categoryId",
                dataType = "String",
                required = true,
                value = "Category_Id")
    })
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/updateCategoryInfo", method = RequestMethod.POST)
    public Message updateCategoryInfo(HttpServletRequest request, @RequestBody JsonNode jsonNode)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(request, "updateCategoryInfo");
        checkAdmin(username);
        String description = null;
        Integer categoryId = null;
        try {
            description = jsonNode.get("description").asText();
            categoryId = jsonNode.get("categoryId").asInt();
        } catch (Exception e) {
            throw new ConfigurationException("请求参数不完整，请重新确认");
        }
        if (description != null) {
            categoryService.updateCategory(categoryId, description);
        }
        return Message.ok();
    }

    /*@ApiOperation(value = "rpc测试", notes = "rpc测试", response = Message.class)*/
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "creator",
                dataType = "String",
                required = true,
                value = "Creator"),
        @ApiImplicitParam(
                name = "engineType",
                dataType = "String",
                required = true,
                value = "Engine_Type"),
        @ApiImplicitParam(
                name = "username",
                dataType = "String",
                required = true,
                value = "User_Name"),
        @ApiImplicitParam(name = "version", dataType = "String", required = true, value = "Version")
    })
    @ApiOperation(value = "RpcTest", notes = "Rpc_Test", response = Message.class)
    @RequestMapping(path = "/rpcTest", method = RequestMethod.GET)
    public Message rpcTest(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "creator", required = false) String creator,
            @RequestParam(value = "engineType", required = false) String engineType,
            @RequestParam(value = "version", required = false) String version) {
        configurationService.queryGlobalConfig(username);
        EngineTypeLabel engineTypeLabel = new EngineTypeLabel();
        engineTypeLabel.setVersion(version);
        engineTypeLabel.setEngineType(engineType);
        configurationService.queryDefaultEngineConfig(engineTypeLabel);
        UserCreatorLabel userCreatorLabel = new UserCreatorLabel();
        userCreatorLabel.setCreator(creator);
        userCreatorLabel.setUser(username);
        configurationService.queryConfig(userCreatorLabel, engineTypeLabel, "wds.linkis.rm");
        Message message = Message.ok();
        return message;
    }

    private void checkAdmin(String userName) throws ConfigurationException {
        if (!Configuration.isAdmin(userName)) {
            throw new ConfigurationException("only admin can modify category(只有管理员才能修改目录)");
        }
    }

    /*@ApiOperation(value = "GetKeyValue", notes = "Get_Key_Value", response = Message.class)*/
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "creator",
                dataType = "String",
                required = true,
                value = "Creator"),
        @ApiImplicitParam(
                name = "engineType",
                dataType = "String",
                required = true,
                value = "Engine_Type"),
        @ApiImplicitParam(
                name = "configKey",
                dataType = "String",
                required = true,
                value = "Config_Key"),
        @ApiImplicitParam(name = "version", dataType = "String", required = true, value = "Version")
    })
    @RequestMapping(path = "/keyvalue", method = RequestMethod.GET)
    public Message getKeyValue(
            HttpServletRequest req,
            @RequestParam(value = "engineType", required = false, defaultValue = "*")
                    String engineType,
            @RequestParam(value = "version", required = false, defaultValue = "*") String version,
            @RequestParam(value = "creator", required = false, defaultValue = "*") String creator,
            @RequestParam(value = "configKey") String configKey)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(req, "saveKey");
        if (engineType.equals("*") && !version.equals("*")) {
            return Message.error(
                    "When engineType is any engine, the version must also be any version");
        }
        List labelList =
                LabelEntityParser.generateUserCreatorEngineTypeLabelList(
                        username, creator, engineType, version);

        List<ConfigValue> configValues = configKeyService.getConfigValue(configKey, labelList);
        Message message = Message.ok().data("configValues", configValues);
        if (configValues.size() > 1) {
            message.data(
                    "warnMessage",
                    "There are multiple values for the corresponding Key： " + configKey);
        }
        return message;
    }

    /*@ApiOperation(value = "保存键值", notes = "保存键值", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "engineType", dataType = "String", required = true, value = "engineType"),
        @ApiImplicitParam(name = "version", dataType = "String", required = true, value = "version"),
        @ApiImplicitParam(name = "creator", dataType = "String", required = true, value = "creator"),
        @ApiImplicitParam(name = "configKey", dataType = "String", required = true, value = "configKey"),
        @ApiImplicitParam(name = "configValue", dataType = "String", required = true, value = "configValue")
    })*/
    @ApiOperation(value = "SaveKeyValue", notes = "Save_Key_Value", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "engineType",
                dataType = "String",
                required = true,
                value = "Engine_Type"),
        @ApiImplicitParam(
                name = "version",
                dataType = "String",
                required = true,
                value = "Version"),
        @ApiImplicitParam(
                name = "creator",
                dataType = "String",
                required = true,
                value = "Creator"),
        @ApiImplicitParam(
                name = "configKey",
                dataType = "String",
                required = true,
                value = "Config_Key"),
        @ApiImplicitParam(
                name = "configValue",
                dataType = "String",
                required = true,
                value = "Config_Value")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/keyvalue", method = RequestMethod.POST)
    public Message saveKeyValue(HttpServletRequest req, @RequestBody Map<String, Object> json)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(req, "saveKey");
        String engineType = (String) json.getOrDefault("engineType", "*");
        String version = (String) json.getOrDefault("version", "*");
        String creator = (String) json.getOrDefault("creator", "*");
        String configKey = (String) json.get("configKey");
        String value = (String) json.get("configValue");
        if (engineType.equals("*") && !version.equals("*")) {
            return Message.error(
                    "When engineType is any engine, the version must also be any version");
        }
        if (StringUtils.isBlank(configKey) || StringUtils.isBlank(value)) {
            return Message.error("key or value cannot be empty");
        }
        List labelList =
                LabelEntityParser.generateUserCreatorEngineTypeLabelList(
                        username, creator, engineType, version);

        ConfigKeyValue configKeyValue = new ConfigKeyValue();
        configKeyValue.setKey(configKey);
        configKeyValue.setConfigValue(value);

        ConfigValue configValue = configKeyService.saveConfigValue(configKeyValue, labelList);
        configurationService.clearAMCacheConf(username, creator, engineType, version);
        return Message.ok().data("configValue", configValue);
    }

    /*@ApiOperation(value = "删除键值", notes = "删除键值", response = Message.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "engineType", dataType = "String", required = true, value = "Engine_Type"),
            @ApiImplicitParam(name = "version", dataType = "String", required = true, value = "Version"),
            @ApiImplicitParam(name = "creator", dataType = "String", required = true, value = "Creator"),
            @ApiImplicitParam(name = "configKey", dataType = "String", required = true, value = "Config_Key")
    })*/
    @ApiOperation(value = "DeleteKeyValue", notes = "Delete_Key_Value", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "engineType",
                dataType = "String",
                required = true,
                value = "Engine_Type"),
        @ApiImplicitParam(
                name = "version",
                dataType = "String",
                required = true,
                value = "Version"),
        @ApiImplicitParam(
                name = "creator",
                dataType = "String",
                required = true,
                value = "Creator"),
        @ApiImplicitParam(
                name = "configKey",
                dataType = "String",
                required = true,
                value = "Config_Key")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/keyvalue", method = RequestMethod.DELETE)
    public Message deleteKeyValue(HttpServletRequest req, @RequestBody Map<String, Object> json)
            throws ConfigurationException {
        String username = ModuleUserUtils.getOperationUser(req, "saveKey");
        String engineType = (String) json.getOrDefault("engineType", "*");
        String version = (String) json.getOrDefault("version", "*");
        String creator = (String) json.getOrDefault("creator", "*");
        String configKey = (String) json.get("configKey");
        if (engineType.equals("*") && !version.equals("*")) {
            return Message.error(
                    "When engineType is any engine, the version must also be any version");
        }
        if (StringUtils.isBlank(configKey)) {
            return Message.error("key cannot be empty");
        }
        List labelList =
                LabelEntityParser.generateUserCreatorEngineTypeLabelList(
                        username, creator, engineType, version);
        List<ConfigValue> configValues = configKeyService.deleteConfigValue(configKey, labelList);
        return Message.ok().data("configValues", configValues);
    }
}
