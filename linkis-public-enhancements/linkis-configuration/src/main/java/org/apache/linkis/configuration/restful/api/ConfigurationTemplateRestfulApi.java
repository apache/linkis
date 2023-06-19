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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.JsonUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.linkis.configuration.errorcode.LinkisConfigurationErrorCodeSummary.*;

@Api(tags = "configuration template")
@RestController
@RequestMapping(path = "/configuration/template")
public class ConfigurationTemplateRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationTemplateRestfulApi.class);

  @Autowired private ConfigurationService configurationService;

  @Autowired private CategoryService categoryService;

  @Autowired private ConfigKeyService configKeyService;

  @ApiOperation(value = "updateKeyMapping", notes = "query engineconn info list", response = Message.class)
  @ApiImplicitParams({
          @ApiImplicitParam(name = "templateUid", dataType = "String", required = true, value = "templateUid"),
          @ApiImplicitParam(name = "templateName", dataType = "String", required = true, value = "engine type"),
          @ApiImplicitParam(name = "engineType", dataType = "String", required = true,value = "String"),
          @ApiImplicitParam(name = "operator", dataType = "String", value = "operator"),
          @ApiImplicitParam(name = "isFullMode", dataType = "Boolbean", value = "isFullMode"),
          @ApiImplicitParam(name = "itemList", dataType = "Array", value = "itemList"),
  })
  @RequestMapping(path = "/updateKeyMapping", method = RequestMethod.POST)
  public Message updateKeyMapping(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
    String username = ModuleUserUtils.getOperationUser(req, "updateKeyMapping");
    String token = ModuleUserUtils.getToken(req);
    // check special admin token
    if (StringUtils.isNotBlank(token)) {
      if (!Configuration.isAdminToken(token)) {
        logger.warn("Token:{} has no permission to updateKeyMapping.", token);
        return Message.error("Token:" + token + " has no permission to updateKeyMapping.");
      }
    } else if (!Configuration.isAdmin(username)) {
      logger.warn("User:{} has no permission to updateKeyMapping.", username);
      return Message.error("User:" + username + " has no permission to updateKeyMapping.");
    }

    String templateUid = jsonNode.get("templateUid").asText();
    String templateName = jsonNode.get("templateName").asText();
    String engineType = jsonNode.get("engineType").asText();
    String operator = jsonNode.get("operator").asText();

    if (StringUtils.isBlank(templateUid)) {
      return Message.error("parameters:templateUid can not be empty(请求参数【templateUid】不能为空)");
    }
    if (StringUtils.isBlank(templateName)) {
      return Message.error("parameters:templateName can not be empty(请求参数【templateName】不能为空)");
    }
    if (StringUtils.isBlank(engineType)) {
      return Message.error("parameters:engineType can not be empty(请求参数【engineType】不能为空)");
    }
    if (StringUtils.isBlank(operator)) {
      return Message.error("parameters:operator can not be empty(请求参数【operator】不能为空)");
    }
    boolean isFullMode =true;
    try {
      isFullMode=jsonNode.get("isFullMode").asBoolean();
      logger.info("will update by param isFullMode:"+isFullMode);
    }
    catch (Exception e)
    {
      logger.info("will update by default isFullMode:"+isFullMode);
    }


    JsonNode itemParms = jsonNode.get("itemList");
    List<TemplateConfigKeyVo> confKeyList = new ArrayList<>();
    if (itemParms != null && !itemParms.isNull()) {
      try {
        confKeyList =
                JsonUtils.jackson()
                        .readValue(itemParms.toString(), new TypeReference<List<TemplateConfigKeyVo>>() {});
      } catch (JsonProcessingException e) {
        return Message.error("parameters:itemList parsing failed(请求参数【itemList】解析失败)");
      }
    }


  }
}
