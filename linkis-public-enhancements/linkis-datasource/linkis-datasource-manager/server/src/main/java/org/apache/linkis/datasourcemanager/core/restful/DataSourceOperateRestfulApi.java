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

package org.apache.linkis.datasourcemanager.core.restful;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceType;
import org.apache.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import org.apache.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.datasourcemanager.core.service.MetadataOperateService;
import org.apache.linkis.datasourcemanager.core.service.hooks.DataSourceParamsHook;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidateException;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.metadata.query.common.MdmConfiguration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static org.apache.linkis.datasourcemanager.common.errorcode.LinkisDatasourceManagerErrorCodeSummary.ENVID_ATYPICAL;

@Api(tags = "data source operate restful api")
@RestController
@RequestMapping(
    value = "/data-source-manager/op/",
    produces = {"application/json"})
public class DataSourceOperateRestfulApi {

  @Autowired private MetadataOperateService metadataOperateService;

  @Autowired private DataSourceRelateService dataSourceRelateService;

  @Autowired private DataSourceInfoService dataSourceInfoService;

  @Autowired private ParameterValidator parameterValidator;

  @Autowired private Validator beanValidator;

  @Autowired private List<DataSourceParamsHook> dataSourceParamsHooks = new ArrayList<>();

  private MultiPartFormDataTransformer formDataTransformer;

  @PostConstruct
  public void initRestful() {
    this.formDataTransformer = FormDataTransformerFactory.buildCustom();
  }

  @ApiOperation(value = "connect", notes = "connect", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"dataSource"})
  @RequestMapping(value = "/connect/json", method = RequestMethod.POST)
  public Message connect(@RequestBody DataSource dataSource, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String operator = ModuleUserUtils.getOperationUser(request, "do connect");
          // Bean validation
          Set<ConstraintViolation<DataSource>> result =
              beanValidator.validate(dataSource, Default.class);
          if (result.size() > 0) {
            throw new ConstraintViolationException(result);
          }
          doConnect(operator, dataSource);
          return Message.ok().data("ok", true);
        },
        "");
  }

  /**
   * Build a connection
   *
   * @param dataSource
   */
  protected void doConnect(String operator, DataSource dataSource) throws ErrorException {
    if (dataSource.getConnectParams().containsKey("envId")) {
      try {
        dataSourceInfoService.addEnvParamsToDataSource(
            Long.parseLong(dataSource.getConnectParams().get("envId").toString()), dataSource);
      } catch (Exception e) {
        throw new ParameterValidateException(ENVID_ATYPICAL.getErrorDesc() + e);
      }
    }
    List<DataSourceParamKeyDefinition> keyDefinitionList =
        dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
    dataSource.setKeyDefinitions(keyDefinitionList);
    Map<String, Object> connectParams = dataSource.getConnectParams();
    parameterValidator.validate(keyDefinitionList, connectParams);
    // For connecting, also need to handle the parameters
    for (DataSourceParamsHook hook : dataSourceParamsHooks) {
      hook.beforePersist(connectParams, keyDefinitionList);
    }
    DataSourceType dataSourceType =
        dataSourceRelateService.getDataSourceType(dataSource.getDataSourceTypeId());
    metadataOperateService.doRemoteConnect(
        MdmConfiguration.METADATA_SERVICE_APPLICATION.getValue(),
        dataSourceType.getName().toLowerCase(),
        operator,
        dataSource.getConnectParams());
  }
}
