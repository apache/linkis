/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.resourcemanager.external.service.impl;

import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource;
import com.webank.wedatasphere.linkis.manager.common.entity.resource.ResourceType;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.entity.cluster.ClusterLabel;
import com.webank.wedatasphere.linkis.resourcemanager.domain.RMLabelContainer;
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMErrorException;
import com.webank.wedatasphere.linkis.resourcemanager.external.dao.ExternalResourceProviderDao;
import com.webank.wedatasphere.linkis.resourcemanager.external.domain.ExternalAppInfo;
import com.webank.wedatasphere.linkis.resourcemanager.external.domain.ExternalResourceIdentifier;
import com.webank.wedatasphere.linkis.resourcemanager.external.domain.ExternalResourceProvider;
import com.webank.wedatasphere.linkis.resourcemanager.external.parser.ExternalResourceIdentifierParser;
import com.webank.wedatasphere.linkis.resourcemanager.external.parser.YarnResourceIdentifierParser;
import com.webank.wedatasphere.linkis.resourcemanager.external.request.ExternalResourceRequester;
import com.webank.wedatasphere.linkis.resourcemanager.external.service.ExternalResourceService;
import com.webank.wedatasphere.linkis.resourcemanager.external.yarn.YarnResourceRequester;
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMConfiguration;
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class ExternalResourceServiceImpl implements ExternalResourceService, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ExternalResourceProviderDao providerDao;

    ExternalResourceIdentifierParser[] identifierParsers;

    ExternalResourceRequester[] resourceRequesters;

    @Override
    public void afterPropertiesSet() throws Exception {
        resourceRequesters = new ExternalResourceRequester[]{
                new YarnResourceRequester()
        };

        identifierParsers = new ExternalResourceIdentifierParser[]{
                new YarnResourceIdentifierParser()
        };
    }

    @Override
    public NodeResource getResource(ResourceType resourceType, RMLabelContainer labelContainer, Map<String, Object> identifierMap) throws RMErrorException {
        ExternalResourceIdentifier identifier = getIdentifierParser(resourceType).parse(identifierMap);
        return getResource(resourceType, labelContainer, identifier);
    }

    @Override
    public NodeResource getResource(ResourceType resourceType, RMLabelContainer labelContainer, ExternalResourceIdentifier identifier) throws RMErrorException {
        ExternalResourceProvider provider = chooseProvider(resourceType, labelContainer);
        ExternalResourceRequester externalResourceRequester = getRequester(resourceType);
        NodeResource resource  = (NodeResource) retry((Integer) RMConfiguration.EXTERNAL_RETRY_NUM().getValue(), (i)-> externalResourceRequester.requestResourceInfo(identifier, provider));
        return resource;
    }

    @Override
    public List<ExternalAppInfo> getAppInfo(ResourceType resourceType, RMLabelContainer labelContainer, Map<String, Object> identifierMap) throws RMErrorException {
        ExternalResourceIdentifier identifier = getIdentifierParser(resourceType).parse(identifierMap);
        return getAppInfo(resourceType, labelContainer, identifier);
    }

    @Override
    public List<ExternalAppInfo> getAppInfo(ResourceType resourceType, RMLabelContainer labelContainer, ExternalResourceIdentifier identifier) throws RMErrorException {
        ExternalResourceProvider provider = chooseProvider(resourceType, labelContainer);
        ExternalResourceRequester externalResourceRequester = getRequester(resourceType);
        List<ExternalAppInfo> appInfos = (List<ExternalAppInfo>) retry((Integer) RMConfiguration.EXTERNAL_RETRY_NUM().getValue(), (i) -> externalResourceRequester.requestAppInfo(identifier, provider));
        return appInfos;
    }

    private Object retry(int retryNum, Function function) throws RMErrorException {
        int times = 0;
        String errorMsg = "Failed to request external resource";
        while(times < retryNum){
            try{
                return function.apply(null);
            } catch (Exception e){
                errorMsg = "Failed to request external resource" + ExceptionUtils.getRootCauseMessage(e);
                logger.warn("failed to request external resource provider", e);
                times ++;
            }
        }
        throw new RMErrorException(11006, errorMsg);
    }

    private ExternalResourceProvider chooseProvider(ResourceType resourceType, RMLabelContainer labelContainer) throws RMErrorException {
        Label label = labelContainer.find(ClusterLabel.class);
        ClusterLabel realClusterLabel = null;
        if (label == null) {
            realClusterLabel = LabelBuilderFactoryContext.getLabelBuilderFactory().createLabel(ClusterLabel.class);
            realClusterLabel.setClusterName(RMConfiguration.DEFAULT_YARN_CLUSTER_NAME().getValue());
            realClusterLabel.setClusterType(RMConfiguration.DEFAULT_YARN_TYPE().getValue());
        } else {
            realClusterLabel = (ClusterLabel) label;
        }
        List<ExternalResourceProvider> providers = providerDao.selectByResourceType(resourceType.toString());
        for (ExternalResourceProvider provider : providers) {
            if (provider.getName().equals(realClusterLabel.getClusterName())) {
                return provider;
            }
        }
        throw new RMErrorException(110013, "No suitable ExternalResourceProvider found for cluster: " + realClusterLabel.getClusterName());
    }

    private ExternalResourceRequester getRequester(ResourceType resourceType) throws RMErrorException {
        for (ExternalResourceRequester externalResourceRequester : resourceRequesters) {
            if(externalResourceRequester.getResourceType().equals(resourceType)){
                return externalResourceRequester;
            }
        }
        throw new RMErrorException(110012, "No ExternalResourceRequester found for resource type: " + resourceType);
    }

    private ExternalResourceIdentifierParser getIdentifierParser(ResourceType resourceType) throws RMErrorException {
        for (ExternalResourceIdentifierParser identifierParser : identifierParsers) {
            if(identifierParser.getResourceType().equals(resourceType)){
                return identifierParser;
            }
        }
        throw new RMErrorException(110012, "No ExternalResourceIdentifierParser found for resource type: " + resourceType);
    }

}
