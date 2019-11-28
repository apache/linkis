/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.udf.api;



import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.linkis.udf.entity.UDFInfo;
import com.webank.wedatasphere.linkis.udf.entity.UDFTree;
import com.webank.wedatasphere.linkis.udf.excepiton.UDFException;
import com.webank.wedatasphere.linkis.udf.service.UDFService;
import com.webank.wedatasphere.linkis.udf.service.UDFTreeService;
import com.webank.wedatasphere.linkis.udf.utils.ConstantVar;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by johnnwang on 8/11/18.
 */
@Path("udf")
@Component
public class UDFApi {

    private static final Logger logger = Logger.getLogger(UDFApi.class);
    private static final Set<String> specialTypes = Sets.newHashSet("bdp", "sys", "share");

    @Autowired
    private UDFService udfService;

    @Autowired
    private UDFTreeService udfTreeService;

    ObjectMapper mapper = new ObjectMapper();

    @POST
    @Path("all")
    public Response allUDF(@Context HttpServletRequest req, String jsonString){
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
                fetchUdfInfoRecursively(allInfo, udfTree);

                udfTree.setUdfInfos(allInfo);
                udfTree.setChildrens(Lists.newArrayList());
                message = Message.ok();
                message.data("udfTree", udfTree);
            } else {
                List<UDFInfo> allInfo = Lists.newArrayList();

                UDFTree udfTree = udfTreeService.getTreeById(-1L,userName, "self", "udf");
                fetchUdfInfoRecursively(allInfo, udfTree);

                udfTree = udfTreeService.getTreeById(-1L,userName, "self", "function");
                fetchUdfInfoRecursively(allInfo, udfTree);

                udfTree.setUdfInfos(allInfo);
                udfTree.setChildrens(Lists.newArrayList());
                message = Message.ok();
                message.data("udfTree", udfTree);
            }

        } catch (Throwable e){
            logger.error("Failed to list Tree: ", e);
            message = Message.error(e.getMessage());
        }
        return  Message.messageToResponse(message);
    }

    private void fetchUdfInfoRecursively(List<UDFInfo> allInfo, UDFTree udfTree) throws Throwable{
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
                    childTreeDetail = udfTreeService.getTreeById(childTree.getId(), childTree.getUserName(), childTree.getUserName(), childTree.getCategory());
                } else {
                    childTreeDetail = udfTreeService.getTreeById(childTree.getId(), childTree.getUserName(), "self", childTree.getCategory());
                }
                fetchUdfInfoRecursively(allInfo, childTreeDetail);
            }
        }
    }

    @POST
    @Path("list")
    public Response listUDF(@Context HttpServletRequest req, Map<String,Object> json){
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

        return  Message.messageToResponse(message);
    }

    @POST
    @Path("add")
    public Response addUDF(@Context HttpServletRequest req,  JsonNode json) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            UDFInfo udfInfo = mapper.readValue(json.get("udfInfo"), UDFInfo.class);
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
        return  Message.messageToResponse(message);
    }

    @POST
    @Path("update")
    public Response updateUDF(@Context HttpServletRequest req,  JsonNode json) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            UDFInfo udfInfo = mapper.readValue(json.get("udfInfo"), UDFInfo.class);
            udfInfo.setCreateUser(userName);
            udfInfo.setUpdateTime(new Date());
            udfService.updateUDF(udfInfo, userName);
            message = Message.ok();
            message.data("udf", udfInfo);
        } catch (Exception e) {
            logger.error("Failed to update UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return  Message.messageToResponse(message);
    }

    @GET
    @Path("delete/{id}")
    public Response deleteUDF(@Context HttpServletRequest req,@PathParam("id") Long id){
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = null;
        try {
            udfService.deleteUDF(id, userName);
            message = Message.ok();
        } catch (Throwable e){
            logger.error("Failed to delete UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return  Message.messageToResponse(message);
    }

    @GET
    @Path("isload")
    public Response isLoad(@Context HttpServletRequest req,
                           @QueryParam("udfId") Long udfId,@QueryParam("isLoad") Boolean isLoad){
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
        return  Message.messageToResponse(message);
    }

    @POST
    @Path("/tree/add")
    public Response addTree(@Context HttpServletRequest req, UDFTree udfTree){
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

        return  Message.messageToResponse(message);
    }

    @POST
    @Path("/tree/update")
    public Response updateTree(@Context HttpServletRequest req, UDFTree udfTree){
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

        return  Message.messageToResponse(message);
    }

    @GET
    @Path("/tree/delete/{id}")
    public Response deleteTree(@Context HttpServletRequest req,@PathParam("id") Long id){
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = null;
        try {
            udfTreeService.deleteTree(id, userName);
            message = Message.ok();
        } catch (Throwable e){
            logger.error("Failed to delete Tree: ", e);
            message = Message.error(e.getMessage());
        }
        return  Message.messageToResponse(message);
    }

    @POST
    @Path("/authenticate")
    public Response Authenticate(@Context HttpServletRequest req,  JsonNode json){
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
        return Message.messageToResponse(message);
    }

    @POST
    @Path("/setExpire")
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,rollbackFor = Throwable.class)
    public Response setExpire(@Context HttpServletRequest req,  JsonNode json){
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            Long udfId =json.get("udfId").getLongValue();
            if (StringUtils.isEmpty(udfId)){
                throw new UDFException("udfId is Empty!");
            }
            String udfName =  json.get("udfName").getTextValue();
            if (StringUtils.isEmpty(udfName)){
                throw new UDFException("udfName is Empty!");
            }
            Long shareUDFId=udfService.getAllShareUDFInfoIdByUDFId(userName,udfName);
            udfService.setSharedUDFInfoExpire(shareUDFId);
            udfService.setUDFSharedInfo(false,udfId);
            message = Message.ok();
        }catch (Throwable e){
            logger.error("Failed to setExpire: ", e);
            message = Message.error(e.getMessage());
        }
        return Message.messageToResponse(message);
    }

    @POST
    @Path("/shareUDF")
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,rollbackFor = Throwable.class)
    public Response shareUDF(@Context HttpServletRequest req,  JsonNode json)throws Throwable{
        Message message = null;
        try {
            String fileName =  json.get("fileName").getTextValue();
            if (StringUtils.isEmpty(fileName)){
                throw new UDFException("fileName is Empty!");
            }
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            List<String> sharedUsers = mapper.readValue(json.get("sharedUsers"), List.class);
            if (CollectionUtils.isEmpty(sharedUsers)){
                throw new UDFException("SharedUsers is Empty!");
            }
            //Verify shared user identity(校验分享的用户身份)
            udfService.checkSharedUsers(sharedUsers,userName);//Throws an exception without passing the checksum (---no deduplication--)(不通过校验则抛异常(---没有去重--))
            //Verify that the udf function has been shared(校验udf函数是否已经被分享)
            UDFInfo udfInfo = mapper.readValue(json.get("udfInfo"), UDFInfo.class);

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

            String sharedPath = fileName +"";
            //Verify sharing path---plus timestamp, it should not be repeated(校验分享路径---加上时间戳,应该不会重复)
            //Copy the file to a shared directory(将文件拷贝到共享目录下)

            UDFInfo sharedUDFInfo = new UDFInfo();
            DateConverter converter = new DateConverter(new Date());
            BeanUtilsBean.getInstance().getConvertUtils().register(converter, Date.class);
            BeanUtils.copyProperties(sharedUDFInfo,udfInfo);
            sharedUDFInfo.setId(null);
            sharedUDFInfo.setCreateTime(new Date());
            sharedUDFInfo.setUpdateTime(new Date());
            sharedUDFInfo.setTreeId(sharedTree.getId());
            sharedUDFInfo.setPath(sharedPath);
            sharedUDFInfo.setShared(true);
            sharedUDFInfo.setExpire(false);
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
        return Message.messageToResponse(message);
    }

    @POST
    @Path("/getSharedUsers")
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,rollbackFor = Throwable.class)
    public Response getSharedUsers(@Context HttpServletRequest req,  JsonNode json){
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            String udfName =  json.get("udfName").getTextValue();
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
        return Message.messageToResponse(message);
    }

    @POST
    @Path("/updateSharedUsers")
    public Response updateSharedUsers(@Context HttpServletRequest req,  JsonNode json){
        Message message = null;

        try {
            List<String> sharedUsers = mapper.readValue(json.get("sharedUsers"), List.class);
            if (CollectionUtils.isEmpty(sharedUsers)){
                throw new UDFException("SharedUsers is Empty!");
            }
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("UserName is Empty!");
            }
            //Verify shared user identity(校验分享的用户身份)
            udfService.checkSharedUsers(sharedUsers,userName);//Throws an exception without passing the checksum (---no deduplication--)(不通过校验则抛异常(---没有去重--))
            String udfName =  json.get("udfName").getTextValue();
            if (StringUtils.isEmpty(userName)){
                throw new UDFException("udfName is Empty!");
            }

            List<String> OldsharedUsers = udfService.getAllgetSharedUsersByUDFIdAndUseName(userName,udfName);
            List<String> temp = new ArrayList<>();
            temp.addAll(sharedUsers);
            temp.retainAll(OldsharedUsers);
            sharedUsers.removeAll(temp);
            OldsharedUsers.removeAll(temp);
            Long udfId=udfService.getAllShareUDFInfoIdByUDFId(userName,udfName);
            udfService.addSharedUser(sharedUsers,udfId);
            udfService.removeSharedUser(OldsharedUsers,udfId);
            message = Message.ok();
        }catch (Throwable e){
            logger.error("Failed to setExpire: ", e);
            message = Message.error(e.getMessage());
        }
        return Message.messageToResponse(message);
    }
}