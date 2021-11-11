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
 
package org.apache.linkis.udf.api;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFTree;
import org.apache.linkis.udf.excepiton.UDFException;
import org.apache.linkis.udf.service.UDFService;
import org.apache.linkis.udf.service.UDFTreeService;
import org.apache.linkis.udf.utils.ConstantVar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(path = "udf")
public class UDFApi {

    private static final Logger logger = Logger.getLogger(UDFApi.class);
    private static final Set<String> specialTypes = Sets.newHashSet("bdp", "sys", "share");

    @Autowired
    private UDFService udfService;

    @Autowired
    private UDFTreeService udfTreeService;

    ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(path = "all",method = RequestMethod.POST)
    public Message allUDF(HttpServletRequest req, String jsonString){
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            if(!StringUtils.isEmpty(jsonString)){
                Map<String,Object> json = mapper.reader(Map.class).readValue(jsonString);
                String type = (String) json.getOrDefault("type", "self");
                Long treeId = ((Integer) json.getOrDefault("treeId", -1)).longValue();
                String category = ((String) json.getOrDefault("category", "all"));

                List<UDFInfo> allInfo = Lists.newArrayList();
                UDFTree udfTree = udfTreeService.getTreeById(treeId,userName, type, category);
                fetchUdfInfoRecursively(allInfo, udfTree,userName);

                udfTree.setUdfInfos(allInfo);
                udfTree.setChildrens(Lists.newArrayList());
                message = Message.ok();
                message.data("udfTree", udfTree);
            } else {
                List<UDFInfo> allInfo = Lists.newArrayList();

                UDFTree udfTree = udfTreeService.getTreeById(-1L,userName, "self", "udf");
                fetchUdfInfoRecursively(allInfo, udfTree,userName);

                udfTree = udfTreeService.getTreeById(-1L,userName, "self", "function");
                fetchUdfInfoRecursively(allInfo, udfTree,userName);

                udfTree.setUdfInfos(allInfo);
                udfTree.setChildrens(Lists.newArrayList());
                message = Message.ok();
                message.data("udfTree", udfTree);
            }

        } catch (Throwable e){
            logger.error("Failed to list Tree: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    private void fetchUdfInfoRecursively(List<UDFInfo> allInfo, UDFTree udfTree, String realUser) throws Throwable{
        if(CollectionUtils.isNotEmpty(udfTree.getUdfInfos())){
            for(UDFInfo udfInfo : udfTree.getUdfInfos()){
                if(udfInfo.getLoad()){
                    allInfo.add(udfInfo);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(udfTree.getChildrens())){
            for(UDFTree childTree : udfTree.getChildrens()){
                UDFTree childTreeDetail = null;
                if(specialTypes.contains(childTree.getUserName())){
                    childTreeDetail = udfTreeService.getTreeById(childTree.getId(), realUser, childTree.getUserName(), childTree.getCategory());
                } else {
                    childTreeDetail = udfTreeService.getTreeById(childTree.getId(), realUser, "self", childTree.getCategory());
                }
                fetchUdfInfoRecursively(allInfo, childTreeDetail,realUser);
            }
        }
    }

    @RequestMapping(path = "list",method = RequestMethod.POST)
    public Message listUDF(HttpServletRequest req,@RequestBody Map<String,Object> json){
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            String type = (String) json.getOrDefault("type", "self");
            Long treeId = ((Integer) json.getOrDefault("treeId", -1)).longValue();
            String category = ((String) json.getOrDefault("category", "all"));
            UDFTree udfTree = udfTreeService.getTreeById(treeId,userName, type, category);
            message = Message.ok();
            message.data("udfTree", udfTree);
        } catch (Throwable e){
            logger.error("Failed to list Tree: ", e);
            message = Message.error(e.getMessage());
        }

        return message;
    }

    @RequestMapping(path = "add",method = RequestMethod.POST)
    public Message addUDF(HttpServletRequest req,@RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            UDFInfo udfInfo = mapper.treeToValue(json.get("udfInfo"), UDFInfo.class);
            udfInfo.setCreateUser(userName);
            udfInfo.setCreateTime(new Date());
            udfInfo.setUpdateTime(new Date());
            udfService.addUDF(udfInfo, userName);
            message = Message.ok();
            message.data("udf", udfInfo);
        } catch (Exception e) {
            logger.error("Failed to add UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "update",method = RequestMethod.POST)
    public Message updateUDF(HttpServletRequest req,@RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            UDFInfo udfInfo = mapper.treeToValue(json.get("udfInfo"), UDFInfo.class);
            udfInfo.setCreateUser(userName);
            udfInfo.setUpdateTime(new Date());
            udfService.updateUDF(udfInfo, userName);
            message = Message.ok();
            message.data("udf", udfInfo);
        } catch (Exception e) {
            logger.error("Failed to update UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "delete/{id}",method = RequestMethod.GET)
    public Message deleteUDF(HttpServletRequest req,@PathVariable("id") Long id){
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = null;
        try {
            udfService.deleteUDF(id, userName);
            message = Message.ok();
        } catch (Throwable e){
            logger.error("Failed to delete UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "isload",method = RequestMethod.GET)
    public Message isLoad(HttpServletRequest req,
        @RequestParam(value="udfId",required=false) Long udfId,
        @RequestParam(value="isLoad",required=false) Boolean isLoad){
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = null;
        try {
            if(isLoad){
                udfService.addLoadInfo(udfId, userName);
            } else {
                udfService.deleteLoadInfo(udfId, userName);
            }
            message = Message.ok();
        } catch (Throwable e){
            logger.error("Failed to isLoad UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "/tree/add",method = RequestMethod.POST)
    public Message addTree(HttpServletRequest req,@RequestBody UDFTree udfTree){
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = null;
        try {
            udfTree.setCreateTime(new Date());
            udfTree.setUpdateTime(new Date());
            udfTree.setUserName(userName);
            udfTree =  udfTreeService.addTree(udfTree, userName);
            message = Message.ok();
            message.data("udfTree", udfTree);
        } catch (Throwable e){
            logger.error("Failed to add Tree: ", e);
            message = Message.error(e.getMessage());
        }

        return message;
    }

    @RequestMapping(path = "/tree/update",method = RequestMethod.POST)
    public Message updateTree(HttpServletRequest req,@RequestBody UDFTree udfTree){
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = null;
        try {
            udfTree.setUpdateTime(new Date());
            udfTree.setUserName(userName);
            udfTree =  udfTreeService.updateTree(udfTree, userName);
            message = Message.ok();
            message.data("udfTree", udfTree);
        } catch (Throwable e){
            logger.error("Failed to update Tree: ", e);
            message = Message.error(e.getMessage());
        }

        return message;
    }

    @RequestMapping(path = "/tree/delete/{id}",method = RequestMethod.GET)
    public Message deleteTree(HttpServletRequest req,@PathVariable("id") Long id){
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = null;
        try {
            udfTreeService.deleteTree(id, userName);
            message = Message.ok();
        } catch (Throwable e){
            logger.error("Failed to delete Tree: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "/authenticate",method = RequestMethod.POST)
    public Message Authenticate(HttpServletRequest req){
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            Boolean boo = udfService.isUDFManager(userName);
            message = Message.ok();
            message.data("isUDFManager", boo);
        }catch (Throwable e){
            logger.error("Failed to authenticate identification: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "/setExpire",method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,rollbackFor = Throwable.class)
    public Message setExpire(HttpServletRequest req,@RequestBody JsonNode json){
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            Long udfId =json.get("udfId").longValue();
            if (StringUtils.isEmpty(udfId)){
                throw new UDFException("udfId is Empty!");
            }
            String udfName =  json.get("udfName").textValue();
            if (StringUtils.isEmpty(udfName)){
                throw new UDFException("udfName is Empty!");
            }
            UDFInfo udfInfo = udfService.getUDFById(udfId, userName);
            if(!userName.equals(udfInfo.getCreateUser())){
                throw new UDFException("您不是该UDF的创建者，没有权限进行设置操作。");
            }
            Long shareUDFId=udfService.getAllShareUDFInfoIdByUDFId(userName,udfName);
            if(shareUDFId != null){
                if(shareUDFId.equals(udfId)){
                    throw new UDFException("请操作该共享函数对应的个人函数。");
                }
                udfService.setSharedUDFInfoExpire(shareUDFId);
                udfService.setUDFSharedInfo(false,udfId);
            }
            message = Message.ok();
        }catch (Throwable e){
            logger.error("Failed to setExpire: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "/shareUDF",method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,rollbackFor = Throwable.class)
    public Message shareUDF(HttpServletRequest req,@RequestBody JsonNode json)throws Throwable{
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            List<String> sharedUsers = mapper.treeToValue(json.get("sharedUsers"), List.class);
            if (CollectionUtils.isEmpty(sharedUsers)){
                throw new UDFException("SharedUsers is Empty!");
            }
            //Verify shared user identity(校验分享的用户身份)
            udfService.checkSharedUsers(sharedUsers,userName);//Throws an exception without passing the checksum (---no deduplication--)(不通过校验则抛异常(---没有去重--))
            //Verify that the udf function has been shared(校验udf函数是否已经被分享)
            UDFInfo udfInfo = mapper.treeToValue(json.get("udfInfo"), UDFInfo.class);
            Long shareParentId = json.get("shareParentId").asLong();


            String category = udfInfo.getUdfType() == 3 || udfInfo.getUdfType() == 4 ? ConstantVar.FUNCTION : ConstantVar.UDF;
            UDFTree sharedTree = udfTreeService.getSharedTree(category);
            if (sharedTree == null){
                throw new UDFException("No shared directories!(没有共享目录!)");
            }

            //Find udfinfo share and expiration information based on request parameters(根据请求参数查出udfinfo的分享和过期信息)
            Boolean isShared = udfInfo.getShared();
            if (isShared){
                throw  new UDFException("This file is being shared!(该文件正在分享中!)");
            }

            //Verify sharing path---plus timestamp, it should not be repeated(校验分享路径---加上时间戳,应该不会重复)
            //Copy the file to a shared directory(将文件拷贝到共享目录下)
            FsPath sharedPath = udfService.copySharedUdfFile(userName, udfInfo);


            UDFInfo sharedUDFInfo = udfService.createSharedUdfInfo(udfInfo, shareParentId, sharedPath);
            sharedUDFInfo = udfService.addSharedUDFInfo(sharedUDFInfo);
            sharedUsers.add(userName);
            udfService.addUDFSharedUsers(sharedUsers,sharedUDFInfo.getId());
            //Change the is_shared property of the field(改变字段的is_shared属性)
            udfService.setUDFSharedInfo(true,udfInfo.getId());
            message = Message.ok();
        }catch (Throwable e){
            logger.error("Failed to share: ", e);
            message = Message.error(e.toString());
        }
        return message;
    }

    @RequestMapping(path = "/getSharedUsers",method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,rollbackFor = Throwable.class)
    public Message getSharedUsers(HttpServletRequest req,@RequestBody JsonNode json){
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            String udfName =  json.get("udfName").textValue();
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("udfName is Empty!");
            }
            List<String> shareUsers = udfService.getAllgetSharedUsersByUDFIdAndUseName(userName,udfName);
            message = Message.ok();
            message.data("shareUsers", shareUsers);
        }catch (Throwable e){
            logger.error("Failed to setExpire: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @RequestMapping(path = "/updateSharedUsers",method = RequestMethod.POST)
    public Message updateSharedUsers(HttpServletRequest req,@RequestBody JsonNode json){
        Message message = null;

        try {
            List<String> sharedUsers = mapper.treeToValue(json.get("sharedUsers"), List.class);
            if (CollectionUtils.isEmpty(sharedUsers)){
                throw new UDFException("SharedUsers is Empty!");
            }
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            //Verify shared user identity(校验分享的用户身份)
            udfService.checkSharedUsers(sharedUsers,userName);//Throws an exception without passing the checksum (---no deduplication--)(不通过校验则抛异常(---没有去重--))
            String udfName =  json.get("udfName").textValue();
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("udfName is Empty!");
            }
            Long udfId=udfService.getAllShareUDFInfoIdByUDFId(userName,udfName);
            UDFInfo udfInfo = udfService.getUDFById(udfId, userName);
            if(udfInfo == null || !userName.equals(udfInfo.getCreateUser())){
                throw new UDFException("只有函数创建人才可以进行此操作。");
            }

            List<String> OldsharedUsers = udfService.getAllgetSharedUsersByUDFIdAndUseName(userName,udfName);
            List<String> temp = new ArrayList<>();
            temp.addAll(sharedUsers);
            temp.retainAll(OldsharedUsers);
            sharedUsers.removeAll(temp);
            OldsharedUsers.removeAll(temp);
            udfService.addSharedUser(sharedUsers,udfId);
            udfService.removeSharedUser(OldsharedUsers,udfId);
            message = Message.ok();
        }catch (Throwable e){
            logger.error("Failed to updateSharedUsers: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }
}