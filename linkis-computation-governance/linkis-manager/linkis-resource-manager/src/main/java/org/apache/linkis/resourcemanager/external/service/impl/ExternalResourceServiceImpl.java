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
 
package org.apache.linkis.resourcemanager.external.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.cluster.ClusterLabel;
import org.apache.linkis.resourcemanager.domain.RMLabelContainer;
import org.apache.linkis.resourcemanager.exception.RMErrorException;
import org.apache.linkis.resourcemanager.external.dao.ExternalResourceProviderDao;
import org.apache.linkis.resourcemanager.external.domain.ExternalAppInfo;
import org.apache.linkis.resourcemanager.external.domain.ExternalResourceIdentifier;
import org.apache.linkis.resourcemanager.external.domain.ExternalResourceProvider;
import org.apache.linkis.resourcemanager.external.parser.ExternalResourceIdentifierParser;
import org.apache.linkis.resourcemanager.external.parser.YarnResourceIdentifierParser;
import org.apache.linkis.resourcemanager.external.request.ExternalResourceRequester;
import org.apache.linkis.resourcemanager.external.service.ExternalResourceService;
import org.apache.linkis.resourcemanager.external.yarn.YarnResourceRequester;
import org.apache.linkis.resourcemanager.utils.RMConfiguration;
import org.apache.linkis.resourcemanager.utils.RMUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class ExternalResourceServiceImpl implements ExternalResourceService, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ExternalResourceProviderDao providerDao;

    ExternalResourceIdentifierParser[] identifierParsers;

    ExternalResourceRequester[] resourceRequesters;

    private LoadingCache<String, List<ExternalResourceProvider>> providerCache = CacheBuilder.newBuilder().maximumSize(20)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .refreshAfterWrite(RMUtils.EXTERNAL_RESOURCE_REFRESH_TIME().getValue().toLong(), TimeUnit.MINUTES)
    .build( new CacheLoader<String, List<ExternalResourceProvider>>() {

        @Override
        public List<ExternalResourceProvider> load(String resourceType) {
            return providerDao.selectByResourceType(resourceType);
        }
    });

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
                logger.warn("failed to request external resource provider, retryNum {}", times,  e);
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
        try {
            List<ExternalResourceProvider> providers = providerCache.get(resourceType.toString());
            for (ExternalResourceProvider provider : providers) {
                if (provider.getName().equals(realClusterLabel.getClusterName())) {
                    return provider;
                }
            }
        } catch (ExecutionException e) {
            throw new RMErrorException(110013, "No suitable ExternalResourceProvider found for cluster: " + realClusterLabel.getClusterName(), e);
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
