/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.configuration.restful.api;

import org.apache.linkis.configuration.conf.Configuration;
import org.apache.linkis.configuration.entity.*;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.CategoryService;
import org.apache.linkis.configuration.service.ConfigKeyService;
import org.apache.linkis.configuration.service.ConfigurationService;
import org.apache.linkis.configuration.util.ConfigurationConfiguration;
import org.apache.linkis.configuration.util.JsonNodeUtil;
import org.apache.linkis.configuration.util.LabelEntityParser;
import org.apache.linkis.configuration.validate.ValidatorManager;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.configuration.errorcode.LinkisConfigurationErrorCodeSummary.*;

@Api(tags = "parameter configuration")
@RestController
@RequestMapping(path = "/configuration")
public class ConfigurationRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationRestfulApi.class);

  @Autowired private ConfigurationService configurationService;

  @Autowired private CategoryService categoryService;

  @Autowired private ConfigKeyService configKeyService;

  @Autowired private ValidatorManager validatorManager;

  ObjectMapper mapper = new ObjectMapper();

  private static final String NULL = "null";

  @ApiOperation(value = "addKeyForEngine", notes = "add key for engine", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "engineType", dataType = "String"),
    @ApiImplicitParam(name = "version", required = false, dataType = "String", value = "version"),
    @ApiImplicitParam(name = "token, required = false", dataType = "String", value = "token"),
    @ApiImplicitParam(name = "keyJson", required = false, dataType = "String", value = "key json")
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
      throw new ConfigurationException(PARAMS_CANNOT_BE_EMPTY.getErrorDesc());
    }
    ModuleUserUtils.getOperationUser(
        req,
        MessageFormat.format(
            "addKeyForEngine,engineType:{0},version:{1},token:{2}", engineType, version, token));
    // todo 检验token
    if (!token.equals(ConfigurationConfiguration.COPYKEYTOKEN)) {
      throw new ConfigurationException(TOKEN_IS_ERROR.getErrorDesc());
    }
    ConfigKey configKey = BDPJettyServerHelper.gson().fromJson(keyJson, ConfigKey.class);
    configurationService.addKeyForEngine(engineType, version, configKey);
    // TODO: 2019/12/30  configKey参数校验
    return Message.ok();
  }

  @ApiOperation(
      value = "getFullTreesByAppName",
      notes = "get full trees by app name",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "engineType", dataType = "String"),
    @ApiImplicitParam(name = "version", dataType = "String", value = "version"),
    @ApiImplicitParam(name = "creator", dataType = "String", value = "creator")
  })
  @RequestMapping(path = "/getFullTreesByAppName", method = RequestMethod.GET)
  public Message getFullTreesByAppName(
      HttpServletRequest req,
      @RequestParam(value = "engineType", required = false) String engineType,
      @RequestParam(value = "version", required = false) String version,
      @RequestParam(value = "creator", required = false) String creator)
      throws ConfigurationException {
    if (creator != null
        && (creator.equals(Configuration.GLOBAL_CONF_CHN_NAME())
            || creator.equals(Configuration.GLOBAL_CONF_CHN_OLDNAME())
            || creator.equals(Configuration.GLOBAL_CONF_CHN_EN_NAME()))) {
      engineType = "*";
      version = "*";
      creator = "*";
    }
    String username =
        ModuleUserUtils.getOperationUser(
            req,
            MessageFormat.format(
                "ConfigurationException,engineType:{0},version:{1}", engineType, version));
    List labelList =
        LabelEntityParser.generateUserCreatorEngineTypeLabelList(
            username, creator, engineType, version);
    ArrayList<ConfigTree> configTrees =
        configurationService.getFullTreeByLabelList(
            labelList, true, req.getHeader("Content-Language"));

    return Message.ok().data("fullTree", configTrees);
  }

  @ApiOperation(value = "getCategory", notes = "get category", response = Message.class)
  @RequestMapping(path = "/getCategory", method = RequestMethod.GET)
  public Message getCategory(HttpServletRequest req) {
    List<CategoryLabelVo> categoryLabelList =
        categoryService.getAllCategory(req.getHeader("Content-Language"));

    return Message.ok().data("Category", categoryLabelList);
  }

  @ApiOperation(
      value = "getItemList",
      notes = "get configuration list by engineType",
      response = Message.class)
  @RequestMapping(path = "/getItemList", method = RequestMethod.GET)
  public Message getItemList(
      HttpServletRequest req, @RequestParam(value = "engineType") String engineType)
      throws ConfigurationException {
    ModuleUserUtils.getOperationUser(req, "getItemList with engineType:" + engineType);
    // Adding * represents returning all configuration information
    if ("*".equals(engineType)) {
      engineType = null;
    }
    List<ConfigKey> result = configKeyService.getConfigKeyList(engineType);
    List<Map<String, Object>> filterResult = new ArrayList<>();
    for (ConfigKey configKey : result) {
      Map<String, Object> temp = new HashMap<>();
      temp.put("key", configKey.getKey());
      temp.put("name", configKey.getName());
      temp.put("description", configKey.getDescription());
      temp.put("engineType", configKey.getEngineType());
      temp.put("validateType", configKey.getValidateType());
      temp.put("validateRange", configKey.getValidateRange());
      temp.put("boundaryType", configKey.getBoundaryType());
      temp.put("defaultValue", configKey.getDefaultValue());
      temp.put("require", configKey.getTemplateRequired());
      filterResult.add(temp);
    }

    return Message.ok().data("itemList", filterResult);
  }

  @ApiOperation(
      value = "createFirstCategory",
      notes = "create first category",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "categoryName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "description", required = true, dataType = "String"),
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
      throw new ConfigurationException(IS_NULL_CANNOT_BE_ADDED.getErrorDesc());
    }
    if (StringUtils.isEmpty(categoryName) || categoryName.contains("-")) {
      throw new ConfigurationException(CANNOT_BE_INCLUDED.getErrorDesc());
    }
    categoryService.createFirstCategory(categoryName, description);
    return Message.ok();
  }

  @ApiOperation(value = "deleteCategory", notes = "delete category", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "categoryId", required = true, dataType = "String", example = "54")
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

  @ApiOperation(
      value = "createSecondCategory",
      notes = "create second category",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "categoryId", required = true, dataType = "String", example = "39"),
    @ApiImplicitParam(name = "engineType", required = true, dataType = "String", example = "hive"),
    @ApiImplicitParam(name = "version", required = true, dataType = "String", example = "1.2.0"),
    @ApiImplicitParam(name = "description", required = true, dataType = "String"),
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
      throw new ConfigurationException(CREATOR_IS_NULL_CANNOT_BE_ADDED.getErrorDesc());
    }
    if (StringUtils.isEmpty(engineType) || engineType.toLowerCase().equals(NULL)) {
      throw new ConfigurationException(ENGINE_TYPE_IS_NULL.getErrorDesc());
    }
    if (StringUtils.isEmpty(version) || version.toLowerCase().equals(NULL)) {
      version = LabelUtils.COMMON_VALUE;
    }
    categoryService.createSecondCategory(categoryId, engineType, version, description);
    return Message.ok();
  }

  @ApiOperation(value = "saveFullTree", notes = "save full tree", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "creator", required = true, dataType = "String", example = "xwzTest"),
    @ApiImplicitParam(name = "engineType", required = true, dataType = "String", example = "hive"),
    @ApiImplicitParam(name = "fullTree", required = true, dataType = "List", value = "full tree"),
    @ApiImplicitParam(name = "name", required = true, dataType = "String", value = "name"),
    @ApiImplicitParam(name = "description", required = true, dataType = "String"),
    @ApiImplicitParam(name = "settings", required = true, dataType = "List", value = "settings")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/saveFullTree", method = RequestMethod.POST)
  public Message saveFullTree(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException, ConfigurationException {
    List fullTrees = mapper.treeToValue(json.get("fullTree"), List.class);
    String creator = JsonNodeUtil.getStringValue(json.get("creator"));
    String engineType = JsonNodeUtil.getStringValue(json.get("engineType"));
    if (creator != null
        && (creator.equals(Configuration.GLOBAL_CONF_CHN_NAME())
            || creator.equals(Configuration.GLOBAL_CONF_CHN_OLDNAME())
            || creator.equals(Configuration.GLOBAL_CONF_CHN_EN_NAME()))) {
      creator = "*";
    }
    String username = ModuleUserUtils.getOperationUser(req, "saveFullTree");
    ArrayList<ConfigValue> createList = new ArrayList<>();
    ArrayList<ConfigValue> updateList = new ArrayList<>();
    ArrayList<List<ConfigKeyValue>> chekList = new ArrayList<>();
    String sparkConf = "";
    for (Object o : fullTrees) {
      String s = BDPJettyServerHelper.gson().toJson(o);
      ConfigTree fullTree = BDPJettyServerHelper.gson().fromJson(s, ConfigTree.class);
      List<ConfigKeyValue> settings = fullTree.getSettings();
      chekList.add(settings);
      for (ConfigKeyValue configKeyValue : settings) {
        if (configKeyValue.getKey().equals("spark.conf")
            && StringUtils.isNotBlank(configKeyValue.getConfigValue())) {
          sparkConf = configKeyValue.getConfigValue().trim();
          configKeyValue.setConfigValue(sparkConf);
        }
      }
    }
    for (List<ConfigKeyValue> settings : chekList) {
      sparkConfCheck(settings, sparkConf);
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
        throw new ConfigurationException(INCORRECT_FIXED_SUCH.getErrorDesc());
      }
      engine = tmpString[0];
      version = tmpString[1];
    }
    configurationService.updateUserValue(createList, updateList);
    // TODO: Add a refresh cache interface later
    if (StringUtils.isNotBlank(creator) && creator.equals("*")) {
      List<CategoryLabelVo> allCategory = categoryService.getAllCategory(null);
      List<CategoryLabelVo> categoryLabelVos =
          allCategory.stream()
              .filter(s -> s.getCategoryName().equals(Configuration.REMOVE_APPLICATION_CACHE()))
              .map(CategoryLabelVo::getChildCategory)
              .findFirst()
              .get();
      categoryLabelVos.stream()
          .map(CategoryLabelVo::getCategoryName)
          .filter(StringUtils::isNotBlank)
          .forEach(
              info -> {
                String[] tmpString = info.split("-");
                if (tmpString.length == 2) {
                  String engineName = tmpString[0];
                  String engineVersion = tmpString[1];
                  logger.info(
                      "Config remove engine cache:engineName:{},engineVersion:{}",
                      engineName,
                      engineVersion);
                  configurationService.clearAMCacheConf(
                      username,
                      Configuration.REMOVE_APPLICATION_CACHE(),
                      engineName,
                      engineVersion);
                }
              });
      configurationService.clearAMCacheConf(username, creator, null, null);
    } else {
      configurationService.clearAMCacheConf(username, creator, engine, version);
    }
    return Message.ok();
  }

  private void sparkConfCheck(List<ConfigKeyValue> settings, String sparkConf)
      throws ConfigurationException {
    if (StringUtils.isNotBlank(sparkConf)) {
      // Check if there are any duplicates in spark. conf
      // spark.conf : spark.shuffle.compress=ture;spark.executor.memory=4g
      String[] split = sparkConf.split(";");
      int setSize =
          Arrays.stream(split).map(s -> s.split("=")[0].trim()).collect(Collectors.toSet()).size();
      int listSize =
          Arrays.stream(split).map(s -> s.split("=")[0].trim()).collect(Collectors.toList()).size();
      if (listSize != setSize) {
        throw new ConfigurationException("Spark.conf contains duplicate keys");
      }
      // Check if there are any duplicates in the spark.conf configuration and other individual
      for (String keyValue : split) {
        String key = keyValue.split("=")[0].trim();
        boolean matchResult =
            settings.stream().anyMatch(settingKey -> key.equals(settingKey.getKey()));
        if (matchResult) {
          throw new ConfigurationException(
              "Saved key is duplicated with the spark conf key , key :" + key);
        }
      }
    }
  }

  @ApiOperation(
      value = "listAllEngineType",
      notes = "list all engine type",
      response = Message.class)
  @RequestMapping(path = "/engineType", method = RequestMethod.GET)
  public Message listAllEngineType(HttpServletRequest request) {
    String[] engineType = configurationService.listAllEngineType();
    return Message.ok().data("engineType", engineType);
  }

  @ApiOperation(
      value = "updateCategoryInfo",
      notes = "update category info",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "description", required = true, dataType = "String"),
    @ApiImplicitParam(name = "categoryId", required = true, dataType = "String")
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
      throw new ConfigurationException(INCOMPLETE_RECONFIRM.getErrorDesc());
    }
    if (description != null) {
      categoryService.updateCategory(categoryId, description);
    }
    return Message.ok();
  }

  @ApiOperation(value = "rpcTest", notes = "rpc test", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "creator", dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "engineType", dataType = "String"),
    @ApiImplicitParam(name = "username", dataType = "String"),
    @ApiImplicitParam(name = "version", required = false, dataType = "String", value = "version")
  })
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
    if (!org.apache.linkis.common.conf.Configuration.isAdmin(userName)) {
      throw new ConfigurationException(ONLY_ADMIN_PERFORM.getErrorDesc());
    }
  }

  @ApiOperation(value = "getKeyValue", notes = "get key value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "engineType", dataType = "String"),
    @ApiImplicitParam(name = "configKey", dataType = "String"),
    @ApiImplicitParam(name = "version", required = false, dataType = "String", value = "version")
  })
  @RequestMapping(path = "/keyvalue", method = RequestMethod.GET)
  public Message getKeyValue(
      HttpServletRequest req,
      @RequestParam(value = "engineType", required = false, defaultValue = "*") String engineType,
      @RequestParam(value = "version", required = false, defaultValue = "*") String version,
      @RequestParam(value = "creator", required = false, defaultValue = "*") String creator,
      @RequestParam(value = "configKey") String configKey)
      throws ConfigurationException {
    String username = ModuleUserUtils.getOperationUser(req, "getKeyValue");
    if (engineType.equals("*") && !version.equals("*")) {
      return Message.error("When engineType is any engine, the version must also be any version");
    }
    List labelList =
        LabelEntityParser.generateUserCreatorEngineTypeLabelList(
            username, creator, engineType, version);

    List<ConfigValue> configValues = configKeyService.getConfigValue(configKey, labelList);
    Message message = Message.ok().data("configValues", configValues);
    if (configValues.size() > 1) {
      message.data(
          "warnMessage", "There are multiple values for the corresponding Key： " + configKey);
    }
    return message;
  }

  @ApiOperation(value = "saveKeyValue", notes = "save key value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "engineType", required = true, dataType = "String"),
    @ApiImplicitParam(name = "version", required = true, dataType = "String", value = "version"),
    @ApiImplicitParam(name = "creator", required = true, dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "configKey", required = true, dataType = "String"),
    @ApiImplicitParam(name = "configValue", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/keyvalue", method = RequestMethod.POST)
  public Message saveKeyValue(HttpServletRequest req, @RequestBody Map<String, Object> json)
      throws ConfigurationException {
    Message message = Message.ok();
    String username = ModuleUserUtils.getOperationUser(req, "saveKey");
    String engineType = (String) json.getOrDefault("engineType", "*");
    String user = (String) json.getOrDefault("user", "");
    String version = (String) json.getOrDefault("version", "*");
    String creator = (String) json.getOrDefault("creator", "*");
    String configKey = (String) json.get("configKey");
    String value = (String) json.get("configValue");
    boolean force = Boolean.parseBoolean(json.getOrDefault("force", "false").toString());
    if (!org.apache.linkis.common.conf.Configuration.isAdmin(username) && !username.equals(user)) {
      return Message.error("Only admin can modify other user configuration data");
    }
    if (engineType.equals("*") && !version.equals("*")) {
      return Message.error("When engineType is any engine, the version must also be any version");
    }
    if (StringUtils.isBlank(configKey)) {
      return Message.error("key cannot be empty");
    }
    if (StringUtils.isNotBlank(user)) {
      username = user;
    }
    List labelList =
        LabelEntityParser.generateUserCreatorEngineTypeLabelList(
            username, creator, engineType, version);

    ConfigKeyValue configKeyValue = new ConfigKeyValue();
    configKeyValue.setKey(configKey);
    configKeyValue.setConfigValue(value);

    try {
      configurationService.paramCheck(configKeyValue);
    } catch (Exception e) {
      if (force && e instanceof ConfigurationException) {
        message.data(
            "msg",
            "The update was successful, but the value verification failed. Please confirm if it has any impact："
                + "（更新成功，但是值校验失败，请确认是否有影响）\n"
                + e.getMessage());
      } else {
        return Message.error(e.getMessage());
      }
    }
    ConfigValue configValue = configKeyService.saveConfigValue(configKeyValue, labelList);
    configurationService.clearAMCacheConf(username, creator, engineType, version);
    return message.data("configValue", configValue);
  }

  @ApiOperation(value = "deleteKeyValue", notes = "delete key value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "engineType", required = true, dataType = "String"),
    @ApiImplicitParam(name = "version", required = true, dataType = "String", value = "version"),
    @ApiImplicitParam(name = "creator", required = true, dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "configKey", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/keyvalue", method = RequestMethod.DELETE)
  public Message deleteKeyValue(HttpServletRequest req, @RequestBody Map<String, Object> json)
      throws ConfigurationException {
    String username = ModuleUserUtils.getOperationUser(req, "deleteKeyValue");
    String engineType = (String) json.getOrDefault("engineType", "*");
    String version = (String) json.getOrDefault("version", "*");
    String creator = (String) json.getOrDefault("creator", "*");
    String configKey = (String) json.get("configKey");
    if (engineType.equals("*") && !version.equals("*")) {
      return Message.error("When engineType is any engine, the version must also be any version");
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

  @ApiOperation(value = "getBaseKeyValue", notes = "get key", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "engineType",
        required = false,
        dataType = "String",
        value = "engineType"),
    @ApiImplicitParam(name = "key", required = false, dataType = "String", value = "key"),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", defaultValue = "1"),
    @ApiImplicitParam(
        name = "pageSize",
        required = false,
        dataType = "Integer",
        defaultValue = "20"),
  })
  @RequestMapping(path = "/baseKeyValue", method = RequestMethod.GET)
  public Message getBaseKeyValue(
      HttpServletRequest req,
      @RequestParam(value = "engineType", required = false) String engineType,
      @RequestParam(value = "key", required = false) String key,
      @RequestParam(value = "pageNow", required = false, defaultValue = "1") Integer pageNow,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize)
      throws ConfigurationException {
    checkAdmin(ModuleUserUtils.getOperationUser(req, "getBaseKeyValue"));
    if (StringUtils.isBlank(engineType)) {
      engineType = null;
    }
    if (StringUtils.isBlank(key)) {
      key = null;
    }
    PageHelper.startPage(pageNow, pageSize);
    List<ConfigKey> list = null;
    try {
      list = configKeyService.getConfigBykey(engineType, key, req.getHeader("Content-Language"));
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<ConfigKey> pageInfo = new PageInfo<>(list);
    long total = pageInfo.getTotal();
    return Message.ok().data("configKeyList", list).data("totalPage", total);
  }

  @ApiOperation(value = "deleteBaseKeyValue", notes = "delete key", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "id", required = true, dataType = "Integer")})
  @RequestMapping(path = "/baseKeyValue", method = RequestMethod.DELETE)
  public Message deleteBaseKeyValue(HttpServletRequest req, @RequestParam(value = "id") Integer id)
      throws ConfigurationException {
    checkAdmin(ModuleUserUtils.getOperationUser(req, "deleteBaseKeyValue  ID:" + id));
    configKeyService.deleteConfigById(id);
    return Message.ok();
  }

  @ApiOperation(value = "saveBaseKeyValue", notes = "save key", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = false, dataType = "Integer", value = "id"),
    @ApiImplicitParam(name = "key", required = true, dataType = "String", value = "key"),
    @ApiImplicitParam(name = "name", required = true, dataType = "String", value = "name"),
    @ApiImplicitParam(
        name = "description",
        required = true,
        dataType = "String",
        value = "description"),
    @ApiImplicitParam(
        name = "defaultValue",
        required = true,
        dataType = "String",
        value = "defaultValue"),
    @ApiImplicitParam(
        name = "validateType",
        required = true,
        dataType = "String",
        value = "validateType"),
    @ApiImplicitParam(
        name = "validateRange",
        required = true,
        dataType = "String",
        value = "validateRange"),
    @ApiImplicitParam(
        name = "boundaryType",
        required = true,
        dataType = "String",
        value = "boundaryType"),
    @ApiImplicitParam(name = "treeName", required = true, dataType = "String", value = "treeName"),
    @ApiImplicitParam(
        name = "engineType",
        required = true,
        dataType = "String",
        value = "engineType"),
    @ApiImplicitParam(name = "enName", required = false, dataType = "String", value = "enName"),
    @ApiImplicitParam(
        name = "enDescription",
        required = false,
        dataType = "String",
        value = "enDescription"),
    @ApiImplicitParam(
        name = "enTreeName",
        required = false,
        dataType = "String",
        value = "enTreeName"),
    @ApiImplicitParam(
        name = "templateRequired",
        required = false,
        dataType = "String",
        value = "1"),
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/baseKeyValue", method = RequestMethod.POST)
  public Message saveBaseKeyValue(HttpServletRequest req, @RequestBody ConfigKey configKey)
      throws ConfigurationException, InstantiationException, IllegalAccessException {
    checkAdmin(ModuleUserUtils.getOperationUser(req, "saveBaseKeyValue"));
    String key = configKey.getKey();
    String name = configKey.getName();
    String treeName = configKey.getTreeName();
    String description = configKey.getDescription();
    Integer boundaryType = configKey.getBoundaryType();
    String defaultValue = configKey.getDefaultValue();
    String validateType = configKey.getValidateType();
    String validateRange = configKey.getValidateRange();
    String engineType = configKey.getEngineType();
    if (StringUtils.isBlank(key)) {
      return Message.error("key cannot be empty");
    }
    if (StringUtils.isBlank(name)) {
      return Message.error("name cannot be empty");
    }
    if (StringUtils.isBlank(description)) {
      return Message.error("description cannot be empty");
    }
    if (StringUtils.isBlank(treeName)) {
      return Message.error("treeName cannot be empty");
    }
    if (StringUtils.isBlank(validateType)) {
      return Message.error("validateType cannot be empty");
    }
    if (!validateType.equals("None") && StringUtils.isBlank(validateRange)) {
      return Message.error("validateRange cannot be empty");
    }
    if (null == boundaryType) {
      return Message.error("boundaryType cannot be empty");
    }
    if (StringUtils.isNotEmpty(defaultValue)
        && !validatorManager
            .getOrCreateValidator(validateType)
            .validate(defaultValue, validateRange)) {
      String msg =
          MessageFormat.format(
              "Parameter configValue verification failed(参数defaultValue校验失败):"
                  + "key:{0}, ValidateType:{1}, ValidateRange:{2},ConfigValue:{3}",
              key, validateType, validateRange, defaultValue);
      throw new ConfigurationException(msg);
    }
    if (null == configKey.getId()) {
      List<ConfigKey> configBykey =
          configKeyService.getConfigBykey(engineType, key, req.getHeader("Content-Language"));
      if (CollectionUtils.isNotEmpty(configBykey)) {
        return Message.error("The engine has the same key: " + key);
      }
      configKeyService.saveConfigKey(configKey);
    } else {
      configKey.setId(configKey.getId());
      configKeyService.updateConfigKey(configKey);
    }
    return Message.ok().data("configKey", configKey);
  }

  @ApiOperation(value = "getUserkeyvalue", notes = "get key", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "engineType",
        required = false,
        dataType = "String",
        value = "engineType"),
    @ApiImplicitParam(name = "key", required = false, dataType = "String", value = "key"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "user", required = false, dataType = "String", value = "user"),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", defaultValue = "1"),
    @ApiImplicitParam(
        name = "pageSize",
        required = false,
        dataType = "Integer",
        defaultValue = "20"),
  })
  @RequestMapping(path = "/userKeyValue", method = RequestMethod.GET)
  public Message getUserKeyValue(
      HttpServletRequest req,
      @RequestParam(value = "engineType", required = false) String engineType,
      @RequestParam(value = "key", required = false) String key,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "pageNow", required = false, defaultValue = "1") Integer pageNow,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize)
      throws ConfigurationException {
    String username = ModuleUserUtils.getOperationUser(req, "getUserKeyValue");
    if (StringUtils.isBlank(engineType)) {
      engineType = null;
    }
    if (StringUtils.isBlank(key)) {
      key = null;
    }
    if (StringUtils.isBlank(creator)) {
      creator = null;
    }
    if (StringUtils.isBlank(user)) {
      user = null;
    }

    if (!org.apache.linkis.common.conf.Configuration.isAdmin(username) && !username.equals(user)) {
      return Message.error("Only admin can query other user configuration data");
    }

    PageHelper.startPage(pageNow, pageSize);
    List<ConfigUserValue> list;
    try {
      list = configKeyService.getUserConfigValue(engineType, key, creator, user);
    } finally {
      PageHelper.clearPage();
    }
    PageInfo<ConfigUserValue> pageInfo = new PageInfo<>(list);
    long total = pageInfo.getTotal();
    return Message.ok().data("configValueList", list).data("totalPage", total);
  }
}
