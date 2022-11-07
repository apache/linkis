package org.apache.linkis.basedatamanager.server.restful;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigKey;
import org.apache.linkis.basedatamanager.server.request.ConfigurationTemplateSaveRequest;
import org.apache.linkis.basedatamanager.server.response.EngineLabelResponse;
import org.apache.linkis.basedatamanager.server.service.ConfigurationTemplateService;
import org.apache.linkis.server.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;

/**
 * This module is designed to manage configuration parameter templates
 */
@RestController
@RequestMapping(path = "/basedata-manager/configuration-template")
public class ConfigurationTemplateRestfulApi {

    @Autowired
    ConfigurationTemplateService configurationTemplateService;

    @ApiOperation(value = "save", notes = "save a configuration template", httpMethod = "POST")
    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public Message add(@RequestBody ConfigurationTemplateSaveRequest request) {
        if (Objects.isNull(request) || StringUtils.isEmpty(request.getCategoryName()) || StringUtils.isEmpty(request.getKey()) ||
                StringUtils.isEmpty(request.getEngineConnType()) || StringUtils.isEmpty(request.getName()) ||
                StringUtils.isEmpty(request.getTreeName())) {
            throw new InvalidParameterException("please check your parameter.");
        }
        Boolean flag = configurationTemplateService.saveConfigurationTemplate(request);
        return Message.ok("").data("success: ", flag);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "long", name = "keyId", value = "")
    })
    @ApiOperation(value = "delete", notes = "delete a configuration template", httpMethod = "DELETE")
    @RequestMapping(path = "/{keyId}", method = RequestMethod.DELETE)
    public Message delete(@PathVariable("keyId") Long keyId) {
        Boolean flag = configurationTemplateService.deleteConfigurationTemplate(keyId);
        return Message.ok("").data("success: ", flag);
    }

    @ApiOperation(value = "engin-list", notes = "get all engine list", httpMethod = "GET")
    @RequestMapping(path = "/engin-list", method = RequestMethod.GET)
    public Message getEngineList() {
        List<EngineLabelResponse> engineList = configurationTemplateService.getEngineList();
        return Message.ok("").data("success: ", engineList);
    }

    @ApiOperation(value = "template-list-by-label", notes = "get template list by label", httpMethod = "GET")
    @RequestMapping(path = "/template-list-by-label", method = RequestMethod.GET)
    public Message getTemplateListByLabelId(@RequestParam String engineLabelId) {
        if (StringUtils.isEmpty(engineLabelId)) {
            throw new InvalidParameterException("please check your parameter.");
        }
        List<ConfigurationConfigKey> configKeyList = configurationTemplateService.getTemplateListByLabelId(engineLabelId);
        return Message.ok("").data("success: ", configKeyList);
    }
}
