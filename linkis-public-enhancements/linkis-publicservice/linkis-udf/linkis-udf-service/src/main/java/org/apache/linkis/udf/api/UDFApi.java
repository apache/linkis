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

import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFTree;
import org.apache.linkis.udf.excepiton.UDFException;
import org.apache.linkis.udf.service.UDFService;
import org.apache.linkis.udf.service.UDFTreeService;
import org.apache.linkis.udf.utils.ConstantVar;
import org.apache.linkis.udf.vo.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.linkis.udf.utils.ConstantVar.*;

@Api(tags = "UDF管理")
@RestController
@RequestMapping(path = "udf")
public class UDFApi {

    private static final Logger logger = LoggerFactory.getLogger(UDFApi.class);
    private static final Set<String> specialTypes = Sets.newHashSet(ConstantVar.specialTypes);

    @Autowired private UDFService udfService;

    @Autowired private UDFTreeService udfTreeService;

    ObjectMapper mapper = new ObjectMapper();

    @ApiOperation(value = "udf树形菜单", notes = "获取udf树形菜单详细信息", response = Message.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "path", dataType = "String", value = "请求路径")})
    @RequestMapping(path = "all", method = RequestMethod.POST)
    public Message allUDF(HttpServletRequest req, String jsonString) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "get all udfs ");
            if (!StringUtils.isEmpty(jsonString)) {
                Map<String, Object> json = mapper.reader(Map.class).readValue(jsonString);
                String type = (String) json.getOrDefault("type", "self");
                Long treeId = ((Integer) json.getOrDefault("treeId", -1)).longValue();
                String category = ((String) json.getOrDefault("category", "all"));

                List<UDFInfoVo> allInfo = Lists.newArrayList();
                UDFTree udfTree = udfTreeService.getTreeById(treeId, userName, type, category);
                fetchUdfInfoRecursively(allInfo, udfTree, userName);

                udfTree.setUdfInfos(allInfo);
                udfTree.setChildrens(Lists.newArrayList());
                message = Message.ok();
                message.data("udfTree", udfTree);
            } else {
                List<UDFInfoVo> allInfo = udfService.getAllUDFSByUserName(userName);

                UDFTree udfTree = new UDFTree();

                udfTree.setUdfInfos(allInfo);
                udfTree.setChildrens(Lists.newArrayList());
                message = Message.ok();
                message.data("udfTree", udfTree);
            }

        } catch (Throwable e) {
            logger.error("Failed to list Tree: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    private void fetchUdfInfoRecursively(List<UDFInfoVo> allInfo, UDFTree udfTree, String realUser)
            throws Throwable {
        if (CollectionUtils.isNotEmpty(udfTree.getUdfInfos())) {
            for (UDFInfoVo udfInfo : udfTree.getUdfInfos()) {
                if (udfInfo.getLoad()) {
                    allInfo.add(udfInfo);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(udfTree.getChildrens())) {
            for (UDFTree childTree : udfTree.getChildrens()) {
                UDFTree childTreeDetail = null;
                if (specialTypes.contains(childTree.getUserName())) {
                    childTreeDetail =
                            udfTreeService.getTreeById(
                                    childTree.getId(),
                                    realUser,
                                    childTree.getUserName(),
                                    childTree.getCategory());
                } else {
                    childTreeDetail =
                            udfTreeService.getTreeById(
                                    childTree.getId(), realUser, "self", childTree.getCategory());
                }
                fetchUdfInfoRecursively(allInfo, childTreeDetail, realUser);
            }
        }
    }

    @ApiOperation(value = "函数列表", notes = "获取函数列表", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "category", dataType = "String", value = "类别"),
        @ApiImplicitParam(name = "treeId", dataType = "String", value = "树形菜单Id"),
        @ApiImplicitParam(name = "type", dataType = "String", value = "函数类型，比如expire,self,share等类型")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "list", method = RequestMethod.POST)
    public Message listUDF(HttpServletRequest req, @RequestBody Map<String, Object> json) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "list udfs ");
            String type = (String) json.getOrDefault("type", SELF_USER);
            Long treeId = ((Integer) json.getOrDefault("treeId", -1)).longValue();
            String category = ((String) json.getOrDefault("category", ALL));
            UDFTree udfTree = udfTreeService.getTreeById(treeId, userName, type, category);
            message = Message.ok();
            message.data("udfTree", udfTree);
        } catch (Throwable e) {
            logger.error("Failed to list Tree: ", e);
            message = Message.error(e.getMessage());
        }

        return message;
    }

    @ApiOperation(value = "新增", notes = "新增", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "createUser",
                required = false,
                dataType = "String",
                value = "创建者"),
        @ApiImplicitParam(
                name = "udfName",
                required = false,
                dataType = "String",
                value = "udfName"),
        @ApiImplicitParam(
                name = "udfType",
                required = false,
                dataType = "Integer",
                value = "udfType"),
        @ApiImplicitParam(
                name = "isExpire",
                required = false,
                dataType = "Boolean",
                value = "是否失效"),
        @ApiImplicitParam(
                name = "isShared",
                required = false,
                dataType = "Boolean",
                value = "是否共享"),
        @ApiImplicitParam(name = "treeId", required = false, dataType = "Long", value = "treeId"),
        @ApiImplicitParam(name = "sys", required = false, dataType = "String", value = "sys"),
        @ApiImplicitParam(
                name = "clusterName",
                required = false,
                dataType = "String",
                value = "clusterName"),
        @ApiImplicitParam(name = "createTime", required = false, dataType = "Date", value = "创建时间"),
        @ApiImplicitParam(name = "updateTime", required = false, dataType = "Date", value = "更新时间"),
        @ApiImplicitParam(
                name = "path",
                required = false,
                dataType = "String",
                value = "仅存储用户上一次上传的路径 作提示用"),
        @ApiImplicitParam(
                name = "registerFormat",
                required = false,
                dataType = "String",
                value = "注册执行地址"),
        @ApiImplicitParam(
                name = "useFormat",
                required = false,
                dataType = "String",
                value = "使用格式"),
        @ApiImplicitParam(
                name = "description",
                required = false,
                dataType = "String",
                value = "描述"),
        @ApiImplicitParam(
                name = "directory",
                required = false,
                dataType = "String",
                value = "分类，个人函数一级目录"),
        @ApiImplicitParam(name = "isLoad", required = false, dataType = "Boolean", value = "是否负载")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "add", method = RequestMethod.POST)
    public Message addUDF(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "add udf ");
            UDFAddVo udfvo = mapper.treeToValue(json.get("udfAddVo"), UDFAddVo.class);
            udfvo.setCreateUser(userName);
            udfvo.setCreateTime(new Date());
            udfvo.setUpdateTime(new Date());
            udfService.addUDF(udfvo, userName);
            message = Message.ok();
            //            message.data("udf", udfvo);
        } catch (Exception e) {
            logger.error("Failed to add UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "更新", notes = "更新", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, dataType = "Long", value = "id"),
        @ApiImplicitParam(
                name = "udfName",
                required = false,
                dataType = "String",
                value = "udfName"),
        @ApiImplicitParam(
                name = "udfType",
                required = false,
                dataType = "Integer",
                value = "udfType"),
        @ApiImplicitParam(
                name = "path",
                required = false,
                dataType = "String",
                value = "仅存储用户上一次上传的路径 作提示用"),
        @ApiImplicitParam(
                name = "registerFormat",
                required = false,
                dataType = "String",
                value = "注册执行地址"),
        @ApiImplicitParam(
                name = "useFormat",
                required = false,
                dataType = "String",
                value = "使用格式"),
        @ApiImplicitParam(
                name = "description",
                required = false,
                dataType = "String",
                value = "描述"),
        @ApiImplicitParam(name = "isLoad", required = false, dataType = "Boolean", value = "是否负载")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "update", method = RequestMethod.POST)
    public Message updateUDF(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "update udf ");
            UDFUpdateVo udfUpdateVo =
                    mapper.treeToValue(json.get("udfUpdateVo"), UDFUpdateVo.class);
            udfService.updateUDF(udfUpdateVo, userName);
            message = Message.ok();
            //            message.data("udf", udfUpdateVo);
        } catch (Exception e) {
            logger.error("Failed to update UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "删除", notes = "删除", response = Message.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "id", dataType = "Long", value = "id")})
    @RequestMapping(path = "delete/{id}", method = RequestMethod.POST)
    public Message deleteUDF(HttpServletRequest req, @PathVariable("id") Long id) {
        String userName = ModuleUserUtils.getOperationUser(req, "delete udf " + id);
        Message message = null;
        try {
            verifyOperationUser(userName, id);
            udfService.deleteUDF(id, userName);
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to delete UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "是否负载", notes = "是否负载", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfId", required = false, dataType = "Long", value = "udfId"),
        @ApiImplicitParam(name = "isLoad", required = false, dataType = "Boolean", value = "是否负载")
    })
    @RequestMapping(path = "isload", method = RequestMethod.GET)
    public Message isLoad(
            HttpServletRequest req,
            @RequestParam(value = "udfId", required = false) Long udfId,
            @RequestParam(value = "isLoad", required = false) Boolean isLoad) {
        String userName = ModuleUserUtils.getOperationUser(req, "isload ");
        Message message = null;
        try {
            if (isLoad) {
                udfService.addLoadInfo(udfId, userName);
            } else {
                udfService.deleteLoadInfo(udfId, userName);
            }
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to isLoad UDF: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "tree新增", notes = "tree新增", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, dataType = "Long", value = "id"),
        @ApiImplicitParam(name = "parent", required = false, dataType = "Long", value = "parent"),
        @ApiImplicitParam(name = "name", required = false, dataType = "String", value = "name"),
        @ApiImplicitParam(
                name = "userName",
                required = false,
                dataType = "String",
                value = "userName"),
        @ApiImplicitParam(
                name = "description",
                required = false,
                dataType = "String",
                value = "description"),
        @ApiImplicitParam(
                name = "createTime",
                required = false,
                dataType = "Date",
                value = "createTime"),
        @ApiImplicitParam(
                name = "updateTime",
                required = false,
                dataType = "Date",
                value = "updateTime"),
        @ApiImplicitParam(
                name = "clusterName",
                required = false,
                dataType = "String",
                value = "clusterName"),
        @ApiImplicitParam(
                name = "category",
                required = false,
                dataType = "String",
                value = "category"),
        @ApiImplicitParam(
                name = "udfInfos",
                required = false,
                dataType = "List<UDFInfoVo>",
                value = "udfInfos"),
        @ApiImplicitParam(
                name = "childrens",
                required = false,
                dataType = "List<UDFTree>",
                value = "childrens")
    })
    @ApiOperationSupport(ignoreParameters = {"udfTree"})
    @RequestMapping(path = "/tree/add", method = RequestMethod.POST)
    public Message addTree(HttpServletRequest req, @RequestBody UDFTree udfTree) {
        String userName =
                ModuleUserUtils.getOperationUser(req, "add udf tree " + udfTree.getName());
        Message message = null;
        try {
            udfTree.setCreateTime(new Date());
            udfTree.setUpdateTime(new Date());
            udfTree.setUserName(userName);
            udfTree = udfTreeService.addTree(udfTree, userName);
            message = Message.ok();
            message.data("udfTree", udfTree);
        } catch (Throwable e) {
            logger.error("Failed to add Tree: ", e);
            message = Message.error(e.getMessage());
        }

        return message;
    }

    @ApiOperation(value = "tree更新", notes = "tree更新", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, dataType = "Long", value = "id"),
        @ApiImplicitParam(name = "parent", required = false, dataType = "Long", value = "parent"),
        @ApiImplicitParam(name = "name", required = false, dataType = "String", value = "name"),
        @ApiImplicitParam(
                name = "userName",
                required = false,
                dataType = "String",
                value = "userName"),
        @ApiImplicitParam(
                name = "description",
                required = false,
                dataType = "String",
                value = "description"),
        @ApiImplicitParam(
                name = "createTime",
                required = false,
                dataType = "Date",
                value = "createTime"),
        @ApiImplicitParam(
                name = "updateTime",
                required = false,
                dataType = "Date",
                value = "updateTime"),
        @ApiImplicitParam(
                name = "clusterName",
                required = false,
                dataType = "String",
                value = "clusterName"),
        @ApiImplicitParam(
                name = "category",
                required = false,
                dataType = "String",
                value = "category"),
        @ApiImplicitParam(
                name = "udfInfos",
                required = false,
                dataType = "List<UDFInfoVo>",
                value = "udfInfos"),
        @ApiImplicitParam(
                name = "childrens",
                required = false,
                dataType = "List<UDFTree>",
                value = "childrens")
    })
    @ApiOperationSupport(ignoreParameters = {"udfTree"})
    @RequestMapping(path = "/tree/update", method = RequestMethod.POST)
    public Message updateTree(HttpServletRequest req, @RequestBody UDFTree udfTree) {
        String userName =
                ModuleUserUtils.getOperationUser(req, "update udf tree " + udfTree.getName());
        Message message = null;
        try {
            udfTree.setUpdateTime(new Date());
            udfTree.setUserName(userName);
            udfTree = udfTreeService.updateTree(udfTree, userName);
            message = Message.ok();
            message.data("udfTree", udfTree);
        } catch (Throwable e) {
            logger.error("Failed to update Tree: ", e);
            message = Message.error(e.getMessage());
        }

        return message;
    }

    @ApiOperation(value = "tree删除", notes = "tree删除", response = Message.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "id", dataType = "Long", value = "id")})
    @RequestMapping(path = "/tree/delete/{id}", method = RequestMethod.GET)
    public Message deleteTree(HttpServletRequest req, @PathVariable("id") Long id) {
        String userName = ModuleUserUtils.getOperationUser(req, "delete udf tree " + id);
        Message message = null;
        try {
            udfTreeService.deleteTree(id, userName);
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to delete Tree: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "证实", notes = "证明…是真实的", response = Message.class)
    @RequestMapping(path = "/authenticate", method = RequestMethod.POST)
    public Message Authenticate(HttpServletRequest req) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("UserName is Empty!");
            }
            Boolean boo = udfService.isUDFManager(userName);
            message = Message.ok();
            message.data("isUDFManager", boo);
        } catch (Throwable e) {
            logger.error("Failed to authenticate identification: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "设置过期", notes = "设置过期", response = Message.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "udfId", dataType = "Long", value = "udfId")})
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/setExpire", method = RequestMethod.POST)
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.DEFAULT,
            rollbackFor = Throwable.class)
    public Message setExpire(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            Long udfId = json.get("udfId").longValue();
            if (StringUtils.isEmpty(udfId)) {
                throw new UDFException("udfId is Empty!");
            }
            String userName = ModuleUserUtils.getOperationUser(req, "set expire udf " + udfId);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("UserName is Empty!");
            }

            verifyOperationUser(userName, udfId);
            udfService.setUdfExpire(udfId, userName);
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to setExpire: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "共有UDF", notes = "共有UDF", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfInfo", dataType = "UDFInfo", value = "udfInfo"),
        @ApiImplicitParam(name = "sharedUsers", dataType = "List", value = "sharedUsers")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/shareUDF", method = RequestMethod.POST)
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.DEFAULT,
            rollbackFor = Throwable.class)
    public Message shareUDF(HttpServletRequest req, @RequestBody JsonNode json) throws Throwable {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("UserName is Empty!");
            }
            if (!udfService.isUDFManager(userName)) {
                throw new UDFException("Only manager can share udf!");
            }
            List<String> userList = mapper.treeToValue(json.get("sharedUsers"), List.class);
            if (userList == null) {
                throw new UDFException("userList cat not be null!");
            }
            Set<String> sharedUserSet = new HashSet<>(userList);
            UDFInfo udfInfo = mapper.treeToValue(json.get("udfInfo"), UDFInfo.class);
            udfInfo = verifyOperationUser(userName, udfInfo.getId());
            //            if (udfInfo.getUdfType() == UDF_JAR) {
            //                throw new UDFException("jar类型UDF不支持共享");
            //            }
            // Verify shared user identity(校验分享的用户身份)
            udfService.checkSharedUsers(sharedUserSet, userName, udfInfo.getUdfName());

            Set<String> oldsharedUsers =
                    new HashSet<>(udfService.getAllSharedUsersByUdfId(userName, udfInfo.getId()));
            Set<String> temp = new HashSet<>(sharedUserSet);
            temp.retainAll(oldsharedUsers);
            sharedUserSet.removeAll(temp);
            oldsharedUsers.removeAll(temp);
            udfService.removeSharedUser(oldsharedUsers, udfInfo.getId());
            udfService.addSharedUser(sharedUserSet, udfInfo.getId());
            // 第一次共享，发布最新版本
            if (!Boolean.TRUE.equals(udfInfo.getShared())) {
                udfService.publishLatestUdf(udfInfo.getId());
            }
            udfService.setUDFSharedInfo(true, udfInfo.getId());
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to share: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "获取SharedUsers", notes = "获取SharedUsers", response = Message.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "udfId", dataType = "long", value = "udfId")})
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/getSharedUsers", method = RequestMethod.POST)
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.DEFAULT,
            rollbackFor = Throwable.class)
    public Message getSharedUsers(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("UserName is Empty!");
            }
            long udfId = json.get("udfId").longValue();
            List<String> shareUsers = udfService.getAllSharedUsersByUdfId(userName, udfId);
            message = Message.ok();
            message.data("sharedUsers", shareUsers);
        } catch (Throwable e) {
            logger.error("Failed to setExpire: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    /**
     * udf handover
     *
     * @param req
     * @param json
     * @return
     */
    @ApiOperation(value = "获取SharedUsers", notes = "获取SharedUsers", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfId", dataType = "long", value = "udfId"),
        @ApiImplicitParam(name = "handoverUser", dataType = "String", value = "切换用户")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/handover", method = RequestMethod.POST)
    public Message handoverUDF(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            long udfId = json.get("udfId").longValue();
            String handoverUser = json.get("handoverUser").textValue();
            if (StringUtils.isEmpty(handoverUser)) {
                throw new UDFException("The handover user can't be null!");
            }
            String userName =
                    ModuleUserUtils.getOperationUser(
                            req, String.join(",", "hand over udf", "" + udfId, handoverUser));
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            UDFInfo udfInfo = verifyOperationUser(userName, udfId);
            if (udfService.isUDFManager(udfInfo.getCreateUser())
                    && !udfService.isUDFManager(handoverUser)) {
                throw new UDFException(
                        "Admin users cannot hand over UDFs to regular users.(管理员用户不能移交UDF给普通用户！)");
            }
            udfService.handoverUdf(udfId, handoverUser);
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to handover udf: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    /**
     * 校验操作用户是否和udf创建用户一致
     *
     * @param userName
     * @param udfId
     * @throws UDFException
     */
    private UDFInfo verifyOperationUser(String userName, long udfId) throws UDFException {
        UDFInfo udfInfo = udfService.getUDFById(udfId, userName);
        if (udfInfo == null) {
            throw new UDFException("can't find udf by this id!");
        }
        if (!udfInfo.getCreateUser().equals(userName)) {
            throw new UDFException(
                    "createUser must be consistent with the operation user(创建用户必须和操作用户一致)");
        }
        return udfInfo;
    }

    @ApiOperation(value = "出版", notes = "出版", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfId", dataType = "long", value = "udfId"),
        @ApiImplicitParam(name = "version", dataType = "String", value = "version")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/publish", method = RequestMethod.POST)
    public Message publishUDF(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            if (!udfService.isUDFManager(userName)) {
                throw new UDFException("only manager can publish udf!");
            }
            long udfId = json.get("udfId").longValue();
            String version = json.get("version").textValue();
            verifyOperationUser(userName, udfId);
            udfService.publishUdf(udfId, version);
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to publish udf: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "回退版本", notes = "回退版本", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfId", dataType = "long", value = "udfId"),
        @ApiImplicitParam(name = "version", dataType = "String", value = "version")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/rollback", method = RequestMethod.POST)
    public Message rollbackUDF(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            long udfId = json.get("udfId").longValue();
            String version = json.get("version").textValue();
            verifyOperationUser(userName, udfId);
            udfService.rollbackUDF(udfId, version, userName);
            message = Message.ok();
        } catch (Throwable e) {
            logger.error("Failed to rollback udf: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "版本列表", notes = "版本列表", response = Message.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "udfId", dataType = "long", value = "udfId")})
    @RequestMapping(path = "/versionList", method = RequestMethod.GET)
    public Message versionList(HttpServletRequest req, @RequestParam("udfId") long udfId) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            List<UDFVersionVo> versionList = udfService.getUdfVersionList(udfId);
            message = Message.ok();
            message.data("versionList", versionList);
        } catch (Throwable e) {
            logger.error("Failed to get udf versionList: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    /**
     * manager pages
     *
     * @param req
     * @param jsonNode
     * @return
     */
    @ApiOperation(value = "UDF列表", notes = "获取UDF列表", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfName", required = false, dataType = "String", value = "udf名称"),
        @ApiImplicitParam(name = "curPage", required = false, dataType = "Integer", value = "页码"),
        @ApiImplicitParam(
                name = "pageSize",
                required = false,
                dataType = "Integer",
                value = "页面大小"),
        @ApiImplicitParam(name = "udfType", required = false, dataType = "String", value = "udf类型")
    })
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/managerPages", method = RequestMethod.POST)
    public Message managerPages(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            String udfName =
                    jsonNode.get("udfName") == null ? null : jsonNode.get("udfName").textValue();
            String udfType = jsonNode.get("udfType").textValue();
            String createUser =
                    jsonNode.get("createUser") == null
                            ? null
                            : jsonNode.get("createUser").textValue();
            int curPage = jsonNode.get("curPage").intValue();
            int pageSize = jsonNode.get("pageSize").intValue();
            Collection<Integer> udfTypes = null;
            if (!StringUtils.isEmpty(udfType)) {
                udfTypes =
                        Arrays.stream(udfType.split(","))
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
            }
            PageInfo<UDFAddVo> pageInfo =
                    udfService.getManagerPages(udfName, udfTypes, userName, curPage, pageSize);
            message = Message.ok();
            message.data("infoList", pageInfo.getList());
            message.data("totalPage", pageInfo.getPages());
            message.data("total", pageInfo.getTotal());
        } catch (Throwable e) {
            logger.error("Failed to get udf infoList: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "下载UDF", notes = "根据版本参数下载UDF", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfId", required = false, dataType = "long", value = "udfId"),
        @ApiImplicitParam(
                name = "version",
                required = false,
                dataType = "String",
                value = "version")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/downloadUdf", method = RequestMethod.POST)
    public Message downloadUdf(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {

            long udfId = json.get("udfId").longValue();
            String version = json.get("version").textValue();
            String userName = ModuleUserUtils.getOperationUser(req, "downloadUdf " + udfId);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            String content = udfService.downLoadUDF(udfId, version, userName);
            message = Message.ok();
            message.data("content", content);
        } catch (Throwable e) {
            logger.error("Failed to download udf: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "下载到本地", notes = "根据版本参数下载UDF到本地", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "udfId", required = false, dataType = "long", value = "udfId"),
        @ApiImplicitParam(
                name = "version",
                required = false,
                dataType = "String",
                value = "version")
    })
    @ApiOperationSupport(ignoreParameters = {"json"})
    @RequestMapping(path = "/downloadToLocal", method = RequestMethod.POST)
    public void downloadToLocal(
            HttpServletRequest req, HttpServletResponse response, @RequestBody JsonNode json)
            throws IOException {
        PrintWriter writer = null;
        InputStream is = null;
        BufferedInputStream fis = null;
        BufferedOutputStream outputStream = null;
        try {

            long udfId = json.get("udfId").longValue();
            String version = json.get("version").textValue();
            String userName = ModuleUserUtils.getOperationUser(req, "downloadUdf " + udfId);
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            DownloadVo downloadVo = udfService.downloadToLocal(udfId, version, userName);
            is = downloadVo.getInputStream();
            fis = new BufferedInputStream(is);
            // 清空response
            response.reset();
            response.setCharacterEncoding("UTF-8");
            // Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            // attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader(
                    "Content-Disposition", "attachment;filename=" + downloadVo.getFileName());
            //            response.addHeader("Content-Length", "" + file.length());
            outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            byte[] buffer = new byte[1024];
            int hasRead = 0;
            while ((hasRead = fis.read(buffer, 0, 1024)) != -1) {
                outputStream.write(buffer, 0, hasRead);
            }
        } catch (Exception e) {
            logger.error("download failed", e);
            response.reset();
            response.setCharacterEncoding(Consts.UTF_8.toString());
            response.setContentType("text/plain; charset=utf-8");
            writer = response.getWriter();
            writer.append("error(错误):" + e.getMessage());
            writer.flush();
        } finally {
            if (outputStream != null) {
                outputStream.flush();
            }
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(writer);
        }
    }

    @ApiOperation(value = "所有UdfUsers", notes = "所有UdfUsers", response = Message.class)
    @RequestMapping(path = "/allUdfUsers", method = RequestMethod.GET)
    public Message allUdfUsers(HttpServletRequest req, @RequestBody JsonNode json) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "allUdfUsers ");
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            List<String> udfUsers = udfService.allUdfUsers();
            message = Message.ok();
            message.data("udfUsers", udfUsers);
        } catch (Throwable e) {
            logger.error("Failed to get udf users: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }

    @ApiOperation(value = "获取用户目录", notes = "获取用户目录", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "category",
                dataType = "String",
                value = "获取指定集合类型用户目录，如类型为UDF即获取该类型下的用户目录")
    })
    @Deprecated
    @RequestMapping(path = "/userDirectory", method = RequestMethod.GET)
    public Message getUserDirectory(
            HttpServletRequest req, @RequestParam("category") String category) {
        Message message = null;
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "userDirectory ");
            if (StringUtils.isEmpty(userName)) {
                throw new UDFException("username is empty!");
            }
            List<String> userDirectory = udfService.getUserDirectory(userName, category);
            message = Message.ok();
            message.data("userDirectory", userDirectory);
        } catch (Throwable e) {
            logger.error("Failed to get user directory: ", e);
            message = Message.error(e.getMessage());
        }
        return message;
    }
}
