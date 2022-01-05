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
 
package org.apache.linkis.instance.label.restful;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.entity.InstanceInfo;
import org.apache.linkis.instance.label.service.conf.InstanceConfigration;
import org.apache.linkis.instance.label.service.impl.DefaultInsLabelService;
import org.apache.linkis.instance.label.utils.EntityParser;
import org.apache.linkis.instance.label.vo.InstanceInfoVo;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(path = "/microservice")
public class InstanceRestful {

    private final static Log logger = LogFactory.getLog(InstanceRestful.class);

    private LabelBuilderFactory labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

    @Autowired
    private DefaultInsLabelService insLabelService;

    @RequestMapping(path = "/allInstance",method = RequestMethod.GET)
    public Message listAllInstanceWithLabel( HttpServletRequest req){
        String username = SecurityFilter.getLoginUsername(req);
        logger.info("start to get all instance informations.....");
        List<InstanceInfo> instances = insLabelService.listAllInstanceWithLabel();
        insLabelService.markInstanceLabel(instances);
        List<InstanceInfoVo> instanceVos = EntityParser.parseToInstanceVo(instances);
        logger.info("Done, all instance:" + instances);
        return Message.ok().data("instances",instanceVos);
    }

    @RequestMapping(path = "/instanceLabel",method = RequestMethod.PUT)
    public Message upDateInstanceLabel( HttpServletRequest req, @RequestBody JsonNode jsonNode) throws Exception {
        String username = SecurityFilter.getLoginUsername(req);
        String[] adminArray = InstanceConfigration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
        if(adminArray != null && !Arrays.asList(adminArray).contains(username)){
            throw new Exception("only admin can modify instance label(只有管理员才能修改标签)");
        }
        String instanceName = jsonNode.get("instance").asText();
        String instanceType = jsonNode.get("applicationName").asText();
        if(StringUtils.isEmpty(instanceName)){
            return Message.error("instance cannot be empty(实例名不能为空");
        }
        if(StringUtils.isEmpty(instanceType)){
            return Message.error("instance cannot be empty(实例类型不能为空");
        }
        JsonNode labelsNode = jsonNode.get("labels");
        Iterator<JsonNode> labelKeyIterator =  labelsNode.iterator();
        ServiceInstance instance = ServiceInstance.apply(instanceType,instanceName);
        List<Label<? extends Label<?>>> labels = new ArrayList<>();
        Set<String> keyList = LabelUtils.listAllUserModifiableLabel();
        Set<String> labelKeySet = new HashSet<>();
        //Traverse all labelKeys and take out labelValue.
        while(labelKeyIterator.hasNext()){
            JsonNode label = labelKeyIterator.next();
            String labelKey = label.get("labelKey").asText();
            String labelStringValue = label.get("stringValue").asText();
            if(labelStringValue != null && keyList.contains(labelKey)){
                Label realLabel = labelBuilderFactory.createLabel(labelKey,labelStringValue);
                if(realLabel instanceof UserModifiable) {
                    ((UserModifiable) realLabel).valueCheck(labelStringValue);
                }
                labelKeySet.add(labelKey);
                labels.add(realLabel);
            }
        }
        if(labelKeySet.size() != labels.size()){
           throw new Exception("Failed to update label, include repeat label(更新label失败，包含重复label)");
        }
        insLabelService.refreshLabelsToInstance(labels,instance);
        InstanceInfo instanceInfo = insLabelService.getInstanceInfoByServiceInstance(instance);
        instanceInfo.setUpdateTime(new Date());
        insLabelService.updateInstance(instanceInfo);
        return Message.ok("success").data("labels",labels);
    }

    @RequestMapping(path = "/modifiableLabelKey",method = RequestMethod.GET)
    public Message listAllModifiableLabelKey( HttpServletRequest req){
        Set<String> keyList = LabelUtils.listAllUserModifiableLabel();
        return Message.ok().data("keyList",keyList);
    }

    @RequestMapping(path = "/eurekaURL",method = RequestMethod.GET)
    public Message getEurekaURL( HttpServletRequest request) throws Exception {
        String eurekaURL = insLabelService.getEurekaURL();
        return Message.ok().data("url", eurekaURL);
    }

}
