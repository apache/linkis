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

package com.webank.wedatasphere.linkis.instance.label.restful;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.instance.label.entity.InstanceInfo;
import com.webank.wedatasphere.linkis.instance.label.service.conf.InstanceConfigration;
import com.webank.wedatasphere.linkis.instance.label.service.impl.DefaultInsLabelService;
import com.webank.wedatasphere.linkis.instance.label.utils.EntityParser;
import com.webank.wedatasphere.linkis.instance.label.vo.InstanceInfoVo;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactory;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.entity.UserModifiable;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;


@Path("/microservice")
@Component
public class InstanceRestful {

    private final static Log logger = LogFactory.getLog(InstanceRestful.class);

    private LabelBuilderFactory labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

    @Autowired
    private DefaultInsLabelService insLabelService;

    @GET
    @Path("/allInstance")
    public Response listAllInstanceWithLabel(@Context HttpServletRequest req){
        String username = SecurityFilter.getLoginUsername(req);
        logger.info("start to get all instance informations.....");
        List<InstanceInfo> instances = insLabelService.listAllInstanceWithLabel();
        insLabelService.markInstanceLabel(instances);
        List<InstanceInfoVo> instanceVos = EntityParser.parseToInstanceVo(instances);
        logger.info("Done, all instance:" + instances);
        return Message.messageToResponse(Message.ok().data("instances",instanceVos));
    }

    @PUT
    @Path("/instanceLabel")
    public Response upDateInstanceLabel(@Context HttpServletRequest req, JsonNode jsonNode) throws Exception {
        String username = SecurityFilter.getLoginUsername(req);
        String[] adminArray = InstanceConfigration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
        if(adminArray != null && !Arrays.asList(adminArray).contains(username)){
            throw new Exception("only admin can modify instance label(只有管理员才能修改标签)");
        }
        String instanceName = jsonNode.get("instance").asText();
        String instanceType = jsonNode.get("applicationName").asText();
        if(StringUtils.isEmpty(instanceName)){
            return Message.messageToResponse(Message.error("instance cannot be empty(实例名不能为空"));
        }
        if(StringUtils.isEmpty(instanceType)){
            return Message.messageToResponse(Message.error("instance cannot be empty(实例类型不能为空"));
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
        return Message.messageToResponse(Message.ok("success").data("labels",labels));
    }

    @GET
    @Path("/modifiableLabelKey")
    public Response listAllModifiableLabelKey(@Context HttpServletRequest req){
        Set<String> keyList = LabelUtils.listAllUserModifiableLabel();
        return Message.messageToResponse(Message.ok().data("keyList",keyList));
    }

    @GET
    @Path("/eurekaURL")
    public Response getEurekaURL(@Context HttpServletRequest request) throws Exception {
        String eurekaURL = insLabelService.getEurekaURL();
        return Message.messageToResponse(Message.ok().data("url", eurekaURL));
    }

}
