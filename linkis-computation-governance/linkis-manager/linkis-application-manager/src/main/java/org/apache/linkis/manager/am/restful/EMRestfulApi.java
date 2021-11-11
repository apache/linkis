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
 
package org.apache.linkis.manager.am.restful;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.converter.DefaultMetricsConverter;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.service.em.EMInfoService;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.am.vo.EMNodeVo;
import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy;
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;


@RequestMapping(path = "/linkisManager", produces = {"application/json"})
@RestController
public class EMRestfulApi {

    @Autowired
    private EMInfoService emInfoService;

    @Autowired
    private NodeLabelService nodeLabelService;

    @Autowired
    private DefaultMetricsConverter defaultMetricsConverter;

    private LabelBuilderFactory stdLabelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

    private Logger logger = LoggerFactory.getLogger(EMRestfulApi.class);


    //todo add healthInfo
    @RequestMapping(path = "/listAllEMs", method = RequestMethod.GET)
    public Message listAllEMs(HttpServletRequest req,
                              @RequestParam(value = "instance",required = false) String instance,
                              @RequestParam(value = "nodeHealthy",required = false) String nodeHealthy,
                              @RequestParam(value = "owner",required = false)String owner) throws AMErrorException {
        String userName = SecurityFilter.getLoginUsername(req);
        String[] adminArray = AMConfiguration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
        if(adminArray != null && !Arrays.asList(adminArray).contains(userName)){
            throw new AMErrorException(210003,"only admin can search ECMs(只有管理员才能查询ECM)");
        }
        EMNode[] allEM = emInfoService.getAllEM();
        ArrayList<EMNodeVo> allEMVo = AMUtils.copyToEMVo(allEM);
        ArrayList<EMNodeVo> allEMVoFilter1 = allEMVo;
        if(CollectionUtils.isNotEmpty(allEMVoFilter1) && !StringUtils.isEmpty(instance)){
            allEMVoFilter1 = (ArrayList<EMNodeVo>) allEMVoFilter1.stream().filter(em -> {return em.getInstance().contains(instance);}).collect(Collectors.toList());
        }
        ArrayList<EMNodeVo> allEMVoFilter2 = allEMVoFilter1;
        if(CollectionUtils.isNotEmpty(allEMVoFilter2) && !StringUtils.isEmpty(nodeHealthy)){
            allEMVoFilter2 = (ArrayList<EMNodeVo>) allEMVoFilter2.stream().filter(em -> {return em.getNodeHealthy() != null ? em.getNodeHealthy().equals(NodeHealthy.valueOf(nodeHealthy)) : true;}).collect(Collectors.toList());
        }
        ArrayList<EMNodeVo> allEMVoFilter3 = allEMVoFilter2;
        if(CollectionUtils.isNotEmpty(allEMVoFilter3) && !StringUtils.isEmpty(owner)){
            allEMVoFilter3 = (ArrayList<EMNodeVo>) allEMVoFilter3.stream().filter(em ->{return em.getOwner().equalsIgnoreCase(owner);}).collect(Collectors.toList());
        }
        return Message.ok().data("EMs", allEMVoFilter3);
    }

    @RequestMapping(path = "/listAllECMHealthyStatus", method = RequestMethod.GET)
    public Message listAllNodeHealthyStatus( HttpServletRequest req,
                                             @RequestParam(value = "onlyEditable",required = false) Boolean onlyEditable){
        NodeHealthy[] nodeHealthy = NodeHealthy.values();
        if(onlyEditable){
            nodeHealthy = new NodeHealthy[]{NodeHealthy.Healthy, NodeHealthy.UnHealthy,
                    NodeHealthy.WARN, NodeHealthy.StockAvailable, NodeHealthy.StockUnavailable};
        }
        return Message.ok().data("nodeHealthy", nodeHealthy);
    }

    @RequestMapping(path = "/modifyEMInfo", method = RequestMethod.PUT)
    @Transactional(rollbackFor = Exception.class)
    public Message modifyEMInfo( HttpServletRequest req, @RequestBody JsonNode jsonNode) throws AMErrorException, LabelErrorException {
        String username = SecurityFilter.getLoginUsername(req);
        String[] adminArray = AMConfiguration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
        if(adminArray != null && !Arrays.asList(adminArray).contains(username)){
            throw new AMErrorException(210003,"only admin can modify ecm information(只有管理员才能修改EM信息)");
        }
        String applicationName = jsonNode.get("applicationName").asText();
        String instance = jsonNode.get("instance").asText();
        if(StringUtils.isEmpty(applicationName)){
            throw new AMErrorException(AMErrorCode.QUERY_PARAM_NULL.getCode(), "applicationName cannot be null(请求参数applicationName不能为空)");
        }
        if(StringUtils.isEmpty(instance)){
            throw new AMErrorException(AMErrorCode.QUERY_PARAM_NULL.getCode(), "instance cannot be null(请求参数instance不能为空)");
        }
        ServiceInstance serviceInstance = ServiceInstance.apply(applicationName,instance);
        if(serviceInstance == null){
            throw new AMErrorException(AMErrorCode.QUERY_PARAM_NULL.getCode(),"serviceInstance:" + applicationName + " non-existent(服务实例" + applicationName + "不存在)");
        }
        String healthyStatus = jsonNode.get("emStatus").asText();
        if(healthyStatus != null && NodeHealthy.valueOf(healthyStatus) != null){
            NodeHealthyInfo nodeHealthyInfo = new NodeHealthyInfo();
            nodeHealthyInfo.setNodeHealthy(NodeHealthy.valueOf(healthyStatus));
            emInfoService.updateEMInfo(serviceInstance, defaultMetricsConverter.convertHealthyInfo(nodeHealthyInfo));
        }
        JsonNode labels = jsonNode.get("labels");
        Set<String> labelKeySet = new HashSet<>();
        if(labels != null){
            ArrayList<Label<?>> newLabelList = new ArrayList<>();
            Iterator<JsonNode> iterator = labels.iterator();
            while(iterator.hasNext()){
                JsonNode label = iterator.next();
                String labelKey = label.get("labelKey").asText();
                String stringValue = label.get("stringValue").asText();
                Label newLabel = stdLabelBuilderFactory.createLabel(labelKey, stringValue);
                if(newLabel instanceof UserModifiable) {
                    ((UserModifiable) newLabel).valueCheck(stringValue);
                }
                labelKeySet.add(labelKey);
                newLabelList.add(newLabel);
            }
            if(labelKeySet.size() != newLabelList.size()){
                throw new AMErrorException(210003, "Failed to update label, include repeat labels(更新label失败，包含重复label)");
            }
            nodeLabelService.updateLabelsToNode(serviceInstance, newLabelList);
            logger.info("success to update label of instance: " + serviceInstance.getInstance());
        }
        return Message.ok("修改EM信息成功");
    }

}
