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
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidateException;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.metadatamanager.common.MdmConfiguration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/data_source/op",produces = {"application/json"})
public class DataSourceOperateRestfulApi {

    @Autowired
    private MetadataOperateService metadataOperateService;

    @Autowired
    private DataSourceRelateService dataSourceRelateService;

    @Autowired
    private DataSourceInfoService dataSourceInfoService;

    @Autowired
    private ParameterValidator parameterValidator;

    @Autowired
    private Validator beanValidator;

    private MultiPartFormDataTransformer formDataTransformer;

    @PostConstruct
    public void initRestful(){
        this.formDataTransformer = FormDataTransformerFactory.buildCustom();
    }

    @RequestMapping(value = "/connect/json",method = RequestMethod.POST)
    public Message connect(DataSource dataSource,
                            HttpServletRequest request) throws ParameterValidateException {
        String operator = SecurityFilter.getLoginUsername(request);
        //Bean validation
        Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
        if(result.size() > 0){
            throw new ConstraintViolationException(result);
        }
        doConnect(operator, dataSource);
        return Message.ok().data("ok", true);
    }

    @RequestMapping(value = "/connect/form",method = RequestMethod.POST)
    public Message connect(FormDataMultiPart multiPartForm,
                             HttpServletRequest request) throws ErrorException {
        String operator = SecurityFilter.getLoginUsername(request);
        DataSource dataSource = formDataTransformer.transformToObject(multiPartForm, DataSource.class, beanValidator);
        doConnect(operator, dataSource);
        return Message.ok().data("ok", true);
    }

    /**
     * Build a connection
     * @param dataSource
     */
    protected void doConnect(String operator, DataSource dataSource) throws ParameterValidateException {
        if(null != dataSource.getDataSourceEnvId()){
            dataSourceInfoService.addEnvParamsToDataSource(dataSource.getDataSourceEnvId(), dataSource);
        }
        //Validate connect parameters
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
        dataSource.setKeyDefinitions(keyDefinitionList);
        Map<String,Object> connectParams = dataSource.getConnectParams();
        parameterValidator.validate(keyDefinitionList, connectParams);
        DataSourceType dataSourceType = dataSourceRelateService.getDataSourceType(dataSource.getDataSourceTypeId());
        metadataOperateService.doRemoteConnect(MdmConfiguration.METADATA_SERVICE_APPLICATION.getValue()
                        + (StringUtils.isNotBlank(dataSourceType.getName())?("-" +dataSourceType.getName().toLowerCase()) : ""),
                operator, dataSource.getConnectParams());
    }
}
