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

package org.apache.linkis.udf.api;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageUtils$;
import org.apache.linkis.udf.conf.Constants;
import org.apache.linkis.udf.entity.PythonModuleInfo;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFTree;
import org.apache.linkis.udf.excepiton.UDFException;
import org.apache.linkis.udf.service.PythonModuleInfoService;
import org.apache.linkis.udf.service.UDFService;
import org.apache.linkis.udf.service.UDFTreeService;
import org.apache.linkis.udf.utils.ConstantVar;
import org.apache.linkis.udf.utils.UdfConfiguration;
import org.apache.linkis.udf.utils.UdfUtils;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
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

import static org.apache.linkis.udf.utils.ConstantVar.*;

@Api(tags = "UDF management")
@RestController
@RequestMapping(path = "udf")
public class UDFRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(UDFRestfulApi.class);
  private static final Set<String> specialTypes = Sets.newHashSet(ConstantVar.specialTypes);

  @Autowired private UDFService udfService;

  @Autowired private UDFTreeService udfTreeService;
  @Autowired private PythonModuleInfoService pythonModuleInfoService;

  ObjectMapper mapper = new ObjectMapper();

  @ApiOperation(value = "allUDF", notes = "all UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "jsonString",
        required = true,
        dataType = "String",
        value = "json string")
  })
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
                  childTree.getId(), realUser, childTree.getUserName(), childTree.getCategory());
        } else {
          childTreeDetail =
              udfTreeService.getTreeById(
                  childTree.getId(), realUser, "self", childTree.getCategory());
        }
        fetchUdfInfoRecursively(allInfo, childTreeDetail, realUser);
      }
    }
  }

  @ApiOperation(value = "listUDF", notes = "list UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "category", required = true, dataType = "String", value = "category"),
    @ApiImplicitParam(name = "treeId", required = true, dataType = "String", value = "tree id"),
    @ApiImplicitParam(
        name = "type",
        dataType = "String",
        value = "Type",
        required = true,
        example = "expire, self, share")
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
    } catch (Exception e) {
      logger.error("Failed to list Tree: ", e);
      message = Message.error(e.getMessage());
    }

    return message;
  }

  @ApiOperation(value = "addUDF", notes = "add UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "createUser",
        dataType = "String",
        value = "create user",
        example = "all"),
    @ApiImplicitParam(name = "udfName", dataType = "String", value = "udf name"),
    @ApiImplicitParam(name = "udfType", dataType = "Integer", value = "udf type"),
    @ApiImplicitParam(name = "isExpire", dataType = "Boolean", value = "is expire"),
    @ApiImplicitParam(name = "isShared", dataType = "Boolean", value = "is shared"),
    @ApiImplicitParam(name = "treeId", dataType = "Long", value = "tree id"),
    @ApiImplicitParam(name = "sys", dataType = "String", value = "sys"),
    @ApiImplicitParam(name = "clusterName", dataType = "String", value = "cluster name"),
    @ApiImplicitParam(name = "createTime", dataType = "Date", value = "create time"),
    @ApiImplicitParam(name = "updateTime", dataType = "Date", value = "update time"),
    @ApiImplicitParam(
        name = "path",
        dataType = "String",
        value = "path",
        example = "file:///mnt/bdap/hadoop/test1012_01.jar"),
    @ApiImplicitParam(
        name = "registerFormat",
        dataType = "String",
        value = "register format",
        example = "create temporary function binbin as \\\"binbin\\\""),
    @ApiImplicitParam(name = "useFormat", dataType = "String", value = "use format"),
    @ApiImplicitParam(name = "description", dataType = "String", value = "description"),
    @ApiImplicitParam(name = "directory", dataType = "String", value = "directory"),
    @ApiImplicitParam(name = "isLoad", dataType = "Boolean", value = "is load")
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
      message = Message.ok().data("udfId", udfService.addUDF(udfvo, userName));
    } catch (Exception e) {
      logger.error("Failed to add UDF: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "updateUDF", notes = "update UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "id",
        example = "51",
        required = true,
        dataType = "Long",
        value = "id"),
    @ApiImplicitParam(
        name = "udfName",
        required = true,
        dataType = "String",
        value = "udf name",
        example = "test2022_2"),
    @ApiImplicitParam(name = "udfType", required = true, dataType = "Integer", value = "udf type"),
    @ApiImplicitParam(
        name = "path",
        required = true,
        dataType = "String",
        value = "path",
        example = "file:///mnt/bdap/hadoop/test.py"),
    @ApiImplicitParam(
        name = "registerFormat",
        required = true,
        dataType = "String",
        value = "register format",
        example = "udf.register(\\\"test2022_2\\\",udf22)"),
    @ApiImplicitParam(
        name = "useFormat",
        required = true,
        dataType = "String",
        value = "use format",
        example = "int test2022_2(int)"),
    @ApiImplicitParam(
        name = "directory",
        required = true,
        dataType = "String",
        value = "directory"),
    @ApiImplicitParam(name = "isLoad", dataType = "Boolean", value = "is load")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "update", method = RequestMethod.POST)
  public Message updateUDF(HttpServletRequest req, @RequestBody JsonNode json) {
    Message message = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "update udf ");
      UDFUpdateVo udfUpdateVo = mapper.treeToValue(json.get("udfUpdateVo"), UDFUpdateVo.class);
      udfService.updateUDF(udfUpdateVo, userName);
      message = Message.ok();
      //            message.data("udf", udfUpdateVo);
    } catch (Exception e) {
      logger.error("Failed to update UDF: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "deleteUDF", notes = "delete UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = true, dataType = "Long", value = "id", example = "8")
  })
  @RequestMapping(path = "delete/{id}", method = RequestMethod.POST)
  public Message deleteUDF(HttpServletRequest req, @PathVariable("id") Long id) {
    String userName = ModuleUserUtils.getOperationUser(req, "delete udf " + id);
    Message message = null;
    try {
      verifyOperationUser(userName, id);
      udfService.deleteUDF(id, userName);
      message = Message.ok();
    } catch (Exception e) {
      logger.error("Failed to delete UDF: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "isLoad", notes = "is load", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "udfId", required = true, dataType = "Long", value = "udf id"),
    @ApiImplicitParam(name = "isLoad", required = true, dataType = "Boolean", value = "is load")
  })
  @RequestMapping(path = "isload", method = RequestMethod.GET)
  public Message isLoad(
      HttpServletRequest req,
      @RequestParam(value = "udfId") Long udfId,
      @RequestParam(value = "isLoad") Boolean isLoad) {
    String userName = ModuleUserUtils.getOperationUser(req, "isload ");
    Message message = null;
    try {
      if (isLoad) {
        udfService.addLoadInfo(udfId, userName);
      } else {
        udfService.deleteLoadInfo(udfId, userName);
      }
      message = Message.ok();
    } catch (Exception e) {
      logger.error("Failed to isLoad UDF: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "AddTree", notes = "Add_Tree", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "Long", value = "id"),
    @ApiImplicitParam(name = "parent", dataType = "Long", value = "parent"),
    @ApiImplicitParam(name = "name", dataType = "String", value = "name"),
    @ApiImplicitParam(name = "userName", dataType = "String", value = "user name"),
    @ApiImplicitParam(name = "description", dataType = "String", value = "description"),
    @ApiImplicitParam(name = "createTime", dataType = "String", value = "create time"),
    @ApiImplicitParam(name = "updateTime", dataType = "String", value = "update time"),
    @ApiImplicitParam(name = "clusterName", dataType = "String", value = "cluster name"),
    @ApiImplicitParam(name = "category", dataType = "String", value = "category"),
    @ApiImplicitParam(name = "udfInfos", dataType = "List<UDFInfoVo>", value = "udf infos"),
    @ApiImplicitParam(name = "childrens", dataType = "List<UDFTree>", value = "childrens")
  })
  @ApiOperationSupport(ignoreParameters = {"udfTree"})
  @RequestMapping(path = "/tree/add", method = RequestMethod.POST)
  public Message addTree(HttpServletRequest req, @RequestBody UDFTree udfTree) {
    String userName = ModuleUserUtils.getOperationUser(req, "add udf tree " + udfTree.getName());
    Message message = null;
    try {
      udfTree.setCreateTime(new Date());
      udfTree.setUpdateTime(new Date());
      udfTree.setUserName(userName);
      udfTree = udfTreeService.addTree(udfTree, userName);
      message = Message.ok();
      message.data("udfTree", udfTree);
    } catch (Exception e) {
      logger.error("Failed to add Tree: ", e);
      message = Message.error(e.getMessage());
    }

    return message;
  }

  @ApiOperation(value = "updateTree", notes = "update tree", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "Long", value = "id"),
    @ApiImplicitParam(name = "parent", dataType = "Long", value = "parent"),
    @ApiImplicitParam(name = "name", dataType = "String", value = "name"),
    @ApiImplicitParam(name = "userName", dataType = "String", value = "user name"),
    @ApiImplicitParam(name = "description", dataType = "String", value = "description"),
    @ApiImplicitParam(name = "createTime", dataType = "String", value = "create time"),
    @ApiImplicitParam(name = "updateTime", dataType = "String", value = "update time"),
    @ApiImplicitParam(name = "clusterName", dataType = "String", value = "cluster name"),
    @ApiImplicitParam(name = "category", dataType = "String", value = "category"),
    @ApiImplicitParam(name = "udfInfos", dataType = "List<UDFInfoVo>", value = "udf infos"),
    @ApiImplicitParam(name = "childrens", dataType = "List<UDFTree>", value = "childrens")
  })
  @ApiOperationSupport(ignoreParameters = {"udfTree"})
  @RequestMapping(path = "/tree/update", method = RequestMethod.POST)
  public Message updateTree(HttpServletRequest req, @RequestBody UDFTree udfTree) {
    String userName = ModuleUserUtils.getOperationUser(req, "update udf tree " + udfTree.getName());
    Message message = null;
    try {
      udfTree.setUpdateTime(new Date());
      udfTree.setUserName(userName);
      udfTree = udfTreeService.updateTree(udfTree, userName);
      message = Message.ok();
      message.data("udfTree", udfTree);
    } catch (Exception e) {
      logger.error("Failed to update Tree: ", e);
      message = Message.error(e.getMessage());
    }

    return message;
  }

  @ApiOperation(value = "deleteTree", notes = "delete tree", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = true, dataType = "Long", value = "id")
  })
  @RequestMapping(path = "/tree/delete/{id}", method = RequestMethod.GET)
  public Message deleteTree(HttpServletRequest req, @PathVariable("id") Long id) {
    String userName = ModuleUserUtils.getOperationUser(req, "delete udf tree " + id);
    Message message = null;
    try {
      udfTreeService.deleteTree(id, userName);
      message = Message.ok();
    } catch (Exception e) {
      logger.error("Failed to delete Tree: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "authenticate", notes = "authenticate", response = Message.class)
  @RequestMapping(path = "/authenticate", method = RequestMethod.POST)
  public Message Authenticate(HttpServletRequest req) {
    Message message = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "Authenticate");
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("UserName is Empty!");
      }
      Boolean boo = udfService.isUDFManager(userName);
      message = Message.ok();
      message.data("isUDFManager", boo);
    } catch (Exception e) {
      logger.error("Failed to authenticate identification: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "setExpire", notes = "set expire", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "udfId", dataType = "Long", value = "udf id")})
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
    } catch (Exception e) {
      logger.error("Failed to setExpire: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "shareUDF", notes = "share uDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "udfInfo", required = true, dataType = "UDFInfo", value = "udf info"),
    @ApiImplicitParam(
        name = "id",
        required = true,
        dataType = "Long",
        value = "id",
        example = "51"),
    @ApiImplicitParam(
        name = "sharedUsers",
        required = true,
        dataType = "List",
        value = "shared users")
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
      String userName = ModuleUserUtils.getOperationUser(req, "shareUDF");
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
    } catch (Exception e) {
      logger.error("Failed to share: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "getSharedUsers", notes = "get shared users", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "udfId", example = "51", dataType = "long", value = "udf id")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/getSharedUsers", method = RequestMethod.POST)
  @Transactional(
      propagation = Propagation.REQUIRED,
      isolation = Isolation.DEFAULT,
      rollbackFor = Throwable.class)
  public Message getSharedUsers(HttpServletRequest req, @RequestBody JsonNode json) {
    Message message = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "getSharedUsers");
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("UserName is Empty!");
      }
      long udfId = json.get("udfId").longValue();
      List<String> shareUsers = udfService.getAllSharedUsersByUdfId(userName, udfId);
      message = Message.ok();
      message.data("sharedUsers", shareUsers);
    } catch (Exception e) {
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
  @ApiOperation(value = "handoverUDF", notes = "handover UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "udfId",
        required = true,
        dataType = "long",
        value = "udf id",
        example = "48"),
    @ApiImplicitParam(
        name = "handoverUser",
        required = true,
        dataType = "String",
        value = "handover user",
        example = "w_jg02")
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
    } catch (Exception e) {
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

  @ApiOperation(value = "publishUDF", notes = "publish UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "udfId", required = true, dataType = "long", value = "udf id"),
    @ApiImplicitParam(name = "version", required = true, dataType = "String", value = "version")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/publish", method = RequestMethod.POST)
  public Message publishUDF(HttpServletRequest req, @RequestBody JsonNode json) {
    Message message = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "publishUDF");
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
    } catch (Exception e) {
      logger.error("Failed to publish udf: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "rollbackUDF", notes = "rollback UDF", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "udfId",
        required = true,
        dataType = "long",
        value = "udf id",
        example = "51"),
    @ApiImplicitParam(
        name = "version",
        required = true,
        dataType = "String",
        value = "version",
        example = "v000002")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/rollback", method = RequestMethod.POST)
  public Message rollbackUDF(HttpServletRequest req, @RequestBody JsonNode json) {
    Message message = null;
    try {
      long udfId = json.get("udfId").longValue();
      String version = json.get("version").textValue();
      String userName =
          ModuleUserUtils.getOperationUser(
              req, MessageFormat.format("rollbackUDF,udfId:{0},version:{1}", udfId, version));
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("username is empty!");
      }
      verifyOperationUser(userName, udfId);
      udfService.rollbackUDF(udfId, version, userName);
      message = Message.ok();
    } catch (Exception e) {
      logger.error("Failed to rollback udf: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "versionList", notes = "version list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "udfId", required = true, dataType = "long", value = "udf id")
  })
  @RequestMapping(path = "/versionList", method = RequestMethod.GET)
  public Message versionList(HttpServletRequest req, @RequestParam("udfId") long udfId) {
    Message message = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "versionList,udfId:" + udfId);
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("username is empty!");
      }
      List<UDFVersionVo> versionList = udfService.getUdfVersionList(udfId);
      message = Message.ok();
      message.data("versionList", versionList);
    } catch (Exception e) {
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
  @ApiOperation(value = "managerPages", notes = "manager pages", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "udfName",
        required = false,
        dataType = "String",
        defaultValue = "",
        value = "udf name",
        example = "udfName"),
    @ApiImplicitParam,
    @ApiImplicitParam(
        name = "pageSize",
        required = false,
        dataType = "Integer",
        value = "page size"),
    @ApiImplicitParam(
        name = "udfType",
        required = false,
        dataType = "String",
        value = "udf type",
        defaultValue = "0,1,2",
        example = "0,1,2")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/managerPages", method = RequestMethod.POST)
  public Message managerPages(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
    Message message = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "managerPages");
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("username is empty!");
      }
      String udfName = jsonNode.get("udfName") == null ? null : jsonNode.get("udfName").textValue();
      String udfType = jsonNode.get("udfType").textValue();
      int curPage = jsonNode.get("curPage").intValue();
      int pageSize = jsonNode.get("pageSize").intValue();
      Collection<Integer> udfTypes = null;
      if (!StringUtils.isEmpty(udfType)) {
        udfTypes =
            Arrays.stream(udfType.split(",")).map(Integer::parseInt).collect(Collectors.toList());
      }
      PageInfo<UDFAddVo> pageInfo =
          udfService.getManagerPages(udfName, udfTypes, userName, curPage, pageSize);
      message = Message.ok();
      message.data("infoList", pageInfo.getList());
      message.data("totalPage", pageInfo.getPages());
      message.data("total", pageInfo.getTotal());
    } catch (Exception e) {
      logger.error("Failed to get udf infoList: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "downloadUdf", notes = "download Udf", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "udfId",
        required = true,
        dataType = "long",
        value = "udf id",
        example = "51"),
    @ApiImplicitParam(
        name = "version",
        required = true,
        dataType = "String",
        value = "version",
        example = "v000003")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/downloadUdf", method = RequestMethod.POST)
  public Message downloadUdf(HttpServletRequest req, @RequestBody JsonNode json) {
    Message message = null;
    try {

      long udfId = json.get("udfId").longValue();
      String version = json.get("version").textValue();
      String userName =
          ModuleUserUtils.getOperationUser(
              req, MessageFormat.format("downloadUdf,udfId:{0},version:{1}", udfId, version));
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("username is empty!");
      }
      String content = udfService.downLoadUDF(udfId, version, userName);
      message = Message.ok();
      message.data("content", content);
    } catch (Exception e) {
      logger.error("Failed to download udf: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "DownloadToLocal", notes = "Download_To_Local", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "udfId",
        required = true,
        dataType = "long",
        value = "udf id",
        example = "51"),
    @ApiImplicitParam(
        name = "version",
        required = true,
        dataType = "String",
        value = "version",
        example = "v000003")
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
      String userName =
          ModuleUserUtils.getOperationUser(
              req, MessageFormat.format("downloadUdf,udfId:{0},version:{1}", udfId, version));
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
      response.addHeader("Content-Disposition", "attachment;filename=" + downloadVo.getFileName());
      //            response.addHeader("Content-Length", "" + file.length());
      outputStream = new BufferedOutputStream(response.getOutputStream()); // NOSONAR
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

  @ApiOperation(value = "allUdfUsers", notes = "all Udf users", response = Message.class)
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
    } catch (Exception e) {
      logger.error("Failed to get udf users: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  @ApiOperation(value = "getUserDirectory", notes = "get user directory", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "category", required = true, dataType = "String", value = "category")
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
    } catch (Exception e) {
      logger.error("Failed to get user directory: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  /** get UDF info by nameList */
  @ApiOperation(value = "getUdfList", notes = "get user directory", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "nameList", required = true, dataType = "String", value = "category"),
    @ApiImplicitParam(
        name = "createUser",
        required = true,
        dataType = "String",
        value = "category"),
  })
  @RequestMapping(path = "/getUdfByNameList", method = RequestMethod.GET)
  public Message getUdfList(
      HttpServletRequest req,
      @RequestParam("nameList") String nameList,
      @RequestParam("createUser") String createUser) {
    Message message = null;
    try {
      //      String userName = ModuleUserUtils.getOperationUser(req, "getUdfByNameList ");
      String userName = "hadoop";
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("username is empty!");
      }
      if (StringUtils.isEmpty(nameList)) {
        throw new UDFException("nameList is empty!");
      }
      if (!UdfConfiguration.nameRegexPattern().matcher(nameList).matches()) {
        throw new UDFException("nameList is invalid!");
      }
      if (StringUtils.isEmpty(createUser)) {
        throw new UDFException("creator is empty!");
      }
      List<String> collect = Arrays.stream(nameList.split(",")).collect(Collectors.toList());
      List<UDFAddVo> udfInfoList = udfService.getUdfByNameList(collect, createUser);
      message = Message.ok().data("infoList", udfInfoList);
    } catch (Throwable e) {
      logger.error("Failed to get user udfinfo : ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  /** get version info by udfName && createUser */
  @ApiOperation(value = "versionInfo", notes = "version list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "udfName", required = true, dataType = "String", value = "udf name"),
    @ApiImplicitParam(
        name = "createUser",
        required = true,
        dataType = "String",
        value = "create user")
  })
  @RequestMapping(path = "/versionInfo", method = RequestMethod.GET)
  public Message versionInfo(
      HttpServletRequest req,
      @RequestParam("udfName") String udfName,
      @RequestParam("createUser") String createUser) {
    Message message = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "versionInfo ");
      if (StringUtils.isEmpty(userName)) {
        throw new UDFException("userName is empty!");
      }
      if (StringUtils.isEmpty(udfName)) {
        throw new UDFException("udfName is empty!");
      }
      if (StringUtils.isEmpty(createUser)) {
        throw new UDFException("createUser is empty!");
      }
      if (!UdfConfiguration.nameRegexPattern().matcher(udfName).matches()) {
        throw new UDFException("udfName is invalid!");
      }
      if (!UdfConfiguration.nameRegexPattern().matcher(createUser).matches()) {
        throw new UDFException("createUser is invalid!");
      }
      UDFVersionVo versionList = udfService.getUdfVersionInfo(udfName, createUser);
      message = Message.ok().data("versionInfo", versionList);
    } catch (Throwable e) {
      logger.error("Failed to get udf versionInfo: ", e);
      message = Message.error(e.getMessage());
    }
    return message;
  }

  /**
   * Python物料查询
   *
   * @param name python模块名称
   * @param engineType 引擎类型(all,spark,python)
   * @param username 用户名
   * @param isLoad 是否加载（0-未加载，1-已加载）
   * @param isExpire 是否过期（0-未过期，1-已过期）
   * @param pageNow 页码
   * @param pageSize 每页展示数据条数
   */
  @RequestMapping(path = "/python-list", method = RequestMethod.GET)
  @ApiOperation(value = "查询Python模块列表", notes = "根据条件查询Python模块信息")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "name", value = "Python模块名称", required = false, dataType = "String"),
    @ApiImplicitParam(
        name = "engineType",
        value = "引擎类型（all, spark, python）",
        required = false,
        dataType = "String"),
    @ApiImplicitParam(name = "username", value = "用户名", required = false, dataType = "String"),
    @ApiImplicitParam(
        name = "isLoad",
        value = "是否加载（0-未加载，1-已加载）",
        required = false,
        dataType = "Integer"),
    @ApiImplicitParam(
        name = "isExpire",
        value = "是否过期（0-未过期，1-已过期）",
        required = false,
        dataType = "Integer"),
    @ApiImplicitParam(name = "pageNow", value = "页码", required = false, dataType = "Integer"),
    @ApiImplicitParam(name = "pageSize", value = "每页展示数据条数", required = false, dataType = "Integer")
  })
  public Message pythonList(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "engineType", required = false) String engineType,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "isLoad", required = false) Integer isLoad,
      @RequestParam(value = "isExpire", required = false) Integer isExpire,
      @RequestParam(value = "pageNow", required = false) Integer pageNow,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      HttpServletRequest req) {

    // 获取登录用户
    String user = ModuleUserUtils.getOperationUser(req, "pythonList");

    // 参数校验
    if (org.apache.commons.lang3.StringUtils.isBlank(name)) name = null;
    if (org.apache.commons.lang3.StringUtils.isBlank(engineType)) engineType = null;
    if (pageNow == null) pageNow = 1;
    if (pageSize == null) pageSize = 10;

    // 根据管理员权限设置username
    if (Configuration.isAdmin(user)) {
      if (username == null) username = null;
    } else {
      username = user;
    }

    // 分页设置
    PageHelper.startPage(pageNow, pageSize);
    try {
      // 执行数据库查询
      PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
      pythonModuleInfo.setName(name);
      pythonModuleInfo.setEngineType(engineType);
      pythonModuleInfo.setCreateUser(username);
      pythonModuleInfo.setIsLoad(isLoad);
      pythonModuleInfo.setIsExpire(0);
      List<PythonModuleInfo> pythonList = pythonModuleInfoService.getByConditions(pythonModuleInfo);
      PageInfo<PythonModuleInfo> pageInfo = new PageInfo<>(pythonList);
      // 封装返回结果
      return Message.ok().data("pythonList", pythonList).data("totalPage", pageInfo.getTotal());
    } finally {
      // 关闭分页
      PageHelper.clearPage();
    }
  }

  /**
   * Python物料删除
   *
   * @param id id
   * @param isExpire 0-未过期，1-已过期
   */
  @RequestMapping(path = "/python-delete", method = RequestMethod.GET)
  @ApiOperation(value = "删除Python模块", notes = "根据模块ID删除Python模块,管理员可以删除任何模块，普通用户只能删除自己创建的模块")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", value = "模块ID", required = true, dataType = "Long"),
    @ApiImplicitParam(
        name = "isExpire",
        value = "模块是否过期（0：未过期，1：已过期）",
        required = true,
        dataType = "int")
  })
  public Message pythonDelete(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "isExpire", required = false) int isExpire,
      HttpServletRequest req,
      HttpServletResponse resp) {
    // 打印审计日志并获取登录用户
    String user = ModuleUserUtils.getOperationUser(req, "pythonDelete");

    // 参数校验
    if (id == null) {
      return Message.error("Invalid parameters: id is null");
    }
    if (isExpire != 0 && isExpire != 1) {
      return Message.error("Invalid parameters: isExpire must be 0 or 1");
    }
    PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
    pythonModuleInfo.setId(id);
    // 根据id查询Python模块信息
    PythonModuleInfo moduleInfo = pythonModuleInfoService.getByUserAndNameAndId(pythonModuleInfo);
    if (moduleInfo == null) {
      return Message.ok(); // 如果不存在则直接返回成功
    }

    // 判断是否是管理员
    if (!Configuration.isAdmin(user)) {
      // 如果不是管理员，检查创建用户是否与当前用户一致
      if (!moduleInfo.getCreateUser().equals(user)) {
        return Message.error("无权删除他人Python模块");
      }
    }

    // 更新Python模块信息
    moduleInfo.setIsExpire(1);
    moduleInfo.setUpdateUser(user);
    moduleInfo.setUpdateTime(new Date());
    // 修改数据库中的模块名称和文件名称
    String newName = moduleInfo.getName() + "_" + System.currentTimeMillis();
    String newPath = moduleInfo.getPath() + "_" + System.currentTimeMillis();
    moduleInfo.setPath(newPath);
    moduleInfo.setName(newName);
    pythonModuleInfoService.updatePythonModuleInfo(moduleInfo);
    return Message.ok();
  }

  /** Python物料新增/更新 */
  @ApiOperation(value = "Python物料新增/更新", notes = "根据传入的Python物料信息新增或更新")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "Python物料新增/更新Request",
        value = "Python物料新增/更新请求体",
        required = false,
        dataType = "PythonModuleInfo")
  })
  @RequestMapping(value = "/python-save", method = RequestMethod.POST)
  public Message request(
      @Nullable @RequestBody PythonModuleInfo pythonModuleInfo,
      HttpServletRequest httpReq,
      HttpServletResponse httpResp) {

    // 获取登录用户
    String userName = ModuleUserUtils.getOperationUser(httpReq, "pythonSave");

    // 入参校验
    if (org.apache.commons.lang3.StringUtils.isBlank(pythonModuleInfo.getName())) {
      return Message.error("模块名称：不能为空");
    }
    if (org.apache.commons.lang3.StringUtils.isBlank(pythonModuleInfo.getPath())) {
      return Message.error("模块物料：不能为空");
    }
    if (org.apache.commons.lang3.StringUtils.isBlank(pythonModuleInfo.getEngineType())) {
      return Message.error("引擎类型：不能为空");
    }
    if (pythonModuleInfo.getIsLoad() == null) {
      return Message.error("是否加载：不能为空");
    }
    if (pythonModuleInfo.getIsExpire() == null) {
      return Message.error("是否过期：不能为空");
    }
    if (org.apache.commons.lang3.StringUtils.isNotBlank(pythonModuleInfo.getPythonModule())) {
      // 使用正则表达式进行校验
      Matcher matcher =
          Pattern.compile("^[a-zA-Z][a-zA-Z0-9,_.-]{0,200}$")
              .matcher(pythonModuleInfo.getPythonModule());
      if (!matcher.matches()) {
        return Message.error("模块名称：只允许英文、数字和英文逗号,点,下划线,横线组成，且长度不超过200个字符");
      }
    }
    String path = pythonModuleInfo.getPath();
    String fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
    if (!pythonModuleInfo.getName().equals(fileName)) {
      return Message.error("模块名称与物料文件名称必须一样");
    }
    // 根据id判断是插入还是更新
    if (pythonModuleInfo.getId() == null) {
      Integer newExpire = pythonModuleInfo.getIsExpire();
      pythonModuleInfo.setCreateUser(userName);
      // 查询未过期的
      pythonModuleInfo.setIsExpire(0);
      PythonModuleInfo moduleInfo = pythonModuleInfoService.getByUserAndNameAndId(pythonModuleInfo);
      // 插入逻辑
      if (moduleInfo != null) {
        return Message.error("模块" + moduleInfo.getName() + "已存在");
      }
      pythonModuleInfo.setCreateTime(new Date());
      pythonModuleInfo.setUpdateTime(new Date());
      pythonModuleInfo.setIsExpire(newExpire);
      pythonModuleInfo.setUpdateUser(userName);
      pythonModuleInfoService.insertPythonModuleInfo(pythonModuleInfo);
      return Message.ok().data("id", pythonModuleInfo.getId());
    } else {
      PythonModuleInfo pythonModuleTmp = new PythonModuleInfo();
      pythonModuleTmp.setId(pythonModuleInfo.getId());
      PythonModuleInfo moduleInfo = pythonModuleInfoService.getByUserAndNameAndId(pythonModuleTmp);
      // 更新逻辑
      if (moduleInfo == null) {
        return Message.error("未找到该Python模块");
      }
      if (!Configuration.isAdmin(userName) && !userName.equals(moduleInfo.getCreateUser())) {
        return Message.error("无权编辑他人Python模块");
      }
      if (moduleInfo.getIsExpire() != 0) {
        return Message.error("当前模块已过期，不允许进行修改操作");
      }
      // 如果模块过期，则修改数据库中的模块名称和文件名称
      if (pythonModuleInfo.getIsExpire() == 1) {
        // 修改数据库中的模块名称和文件名称
        String newName = moduleInfo.getName() + "_" + System.currentTimeMillis();
        String newPath = moduleInfo.getPath() + "_" + System.currentTimeMillis();
        pythonModuleInfo.setPath(newPath);
        pythonModuleInfo.setName(newName);
      }
      pythonModuleInfo.setUpdateUser(userName);
      pythonModuleInfo.setUpdateTime(new Date());
      pythonModuleInfoService.updatePythonModuleInfo(pythonModuleInfo);
    }
    return Message.ok();
  }

  /**
   * python文件是否存在查询
   *
   * @param fileName 文件名称
   */
  @RequestMapping(path = "/python-file-exist", method = RequestMethod.GET)
  @ApiOperation(value = "查询Python文件是否存在", notes = "根据用户名和文件名查询Python模块信息，如果存在则返回true，否则返回false")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "fileName",
        value = "Python文件名",
        required = true,
        dataType = "string",
        paramType = "query"),
    @ApiImplicitParam(
        name = "Authorization",
        value = "Bearer token",
        required = true,
        dataType = "string",
        paramType = "header")
  })
  public Message pythonFileExist(
      @RequestParam(value = "fileName", required = false) String fileName, HttpServletRequest req) {
    // 审计日志打印并获取登录用户
    String userName = ModuleUserUtils.getOperationUser(req, "pythonFileExist");

    // 参数校验
    if (org.apache.commons.lang3.StringUtils.isBlank(fileName)) {
      return Message.error("参数fileName不能为空");
    }
    String fileNameWithoutExtension = fileName.split("\\.")[0];
    if (!fileNameWithoutExtension.matches("^[a-zA-Z][a-zA-Z0-9_-]{0,49}$")) {
      return Message.error("只支持数字字母下划线，中划线，且以字母开头，长度最大50");
    }
    String fileNameWithoutVersion = fileNameWithoutExtension.split("-")[0];
    // 封装PythonModuleInfo对象并查询数据库
    PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
    pythonModuleInfo.setName(fileNameWithoutVersion);
    pythonModuleInfo.setCreateUser(userName);
    PythonModuleInfo moduleInfo = pythonModuleInfoService.getByUserAndNameAndId(pythonModuleInfo);

    // 根据查询结果返回相应信息
    if (moduleInfo == null) {
      return Message.ok().data("result", true);
    } else {
      return Message.error("模块" + fileName + "已存在，如需重新上传请先删除旧的模块");
    }
  }

  @ApiOperation(value = "Python模块上传", notes = "上传Python模块文件并返回文件地址", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "file", required = true, dataType = "MultipartFile", value = "上传的文件"),
    @ApiImplicitParam(name = "fileName", required = true, dataType = "String", value = "文件名称")
  })
  @RequestMapping(path = "/python-upload", method = RequestMethod.POST)
  public Message pythonUpload(
      HttpServletRequest req,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "fileName", required = false) String fileName)
      throws IOException {

    // 获取登录用户
    String username = ModuleUserUtils.getOperationUser(req, "pythonUpload");

    // 校验文件名称
    if (org.apache.commons.lang3.StringUtils.isBlank(fileName)) {
      return Message.error("文件名称不能为空");
    }
    // 获取文件名称
    if (!fileName.matches("^[a-zA-Z][a-zA-Z0-9_.-]{0,49}$")) {
      return Message.error("模块名称错误，仅支持数字字母下划线，且以字母开头，长度最大50");
    }

    // 校验文件类型
    if (!file.getOriginalFilename().endsWith(Constants.FILE_EXTENSION_PY)
        && !file.getOriginalFilename().endsWith(Constants.FILE_EXTENSION_ZIP)
        && !file.getOriginalFilename().endsWith(Constants.FILE_EXTENSION_TAR_GZ)) {
      return Message.error("仅支持.py和.zip和.tar.gz格式模块文件");
    }

    // 校验文件大小
    if (file.getSize() > Constants.MAX_FILE_SIZE_MB) {
      return Message.error("限制最大单个文件50M");
    }

    // tar.gz包依赖检查
    // 获取install_requires中的python模块
    List<String> pythonModules = UdfUtils.getInstallRequestPythonModules(file);
    String dependencies = "";
    if (CollectionUtils.isNotEmpty(pythonModules)) {
      // 收集依赖信息
      dependencies =
          pythonModules.stream().distinct().collect(Collectors.joining(Constants.DELIMITER_COMMA));
      // 收集不存在的依赖
      StringJoiner notExistModulesStr = new StringJoiner(Constants.DELIMITER_COMMA);
      // 检查pyhton环境中模块和数据库是否存在
      pythonModules.stream()
          .filter(s -> !UdfUtils.checkModuleIsExistEnv(s))
          .forEach(
              pythonModule -> {
                PythonModuleInfo pythonModuleInfo = new PythonModuleInfo();
                pythonModuleInfo.setCreateUser(username);
                pythonModuleInfo.setName(pythonModule);
                pythonModuleInfo.setIsLoad(1);
                pythonModuleInfo = pythonModuleInfoService.getByUserAndNameAndId(pythonModuleInfo);
                if (null == pythonModuleInfo) {
                  notExistModulesStr.add(pythonModule);
                }
              });
      if (org.apache.commons.lang3.StringUtils.isNotBlank(notExistModulesStr.toString())) {
        return Message.error("部分依赖未加载，请检查并重新上传依赖包，依赖信息：" + notExistModulesStr);
      }
    }
    // 定义目录路径
    String path = Constants.HDFS_PATH_UDF + username;
    FsPath fsPath = new FsPath(path);
    // 获取文件系统实例
    FileSystem fileSystem = (FileSystem) FSFactory.getFs(fsPath);
    fileSystem.init(null);
    // 确认目录是否存在，不存在则创建新目录
    if (!fileSystem.exists(fsPath)) {
      try {
        fileSystem.mkdirs(fsPath);
        fileSystem.setPermission(fsPath, Constants.FILE_PERMISSION);
      } catch (IOException e) {
        return Message.error("创建目录失败：" + e.getMessage());
      }
    }
    fileSystem.setOwner(fsPath, username);
    // 构建新的文件路径
    String newPath = fsPath.getPath() + FsPath.SEPARATOR + file.getOriginalFilename();
    // 上传文件,tar包需要单独解压处理
    if (!file.getOriginalFilename().endsWith(Constants.FILE_EXTENSION_TAR_GZ)) {
      FsPath fsPathNew = new FsPath(newPath);
      try (InputStream is = file.getInputStream();
          OutputStream outputStream = fileSystem.write(fsPathNew, true)) {
        IOUtils.copy(is, outputStream);
      } catch (IOException e) {
        return Message.error("文件上传失败：" + e.getMessage());
      }
    } else {
      InputStream is = null;
      OutputStream outputStream = null;
      try {
        String packageName = UdfUtils.findPackageName(file.getInputStream());
        if (UdfUtils.checkModuleIsExistEnv(packageName)) {
          return Message.error("python3环境中已存在模块：" + packageName + "请勿重复上传");
        }
        fileName = packageName + Constants.FILE_EXTENSION_ZIP;
        if (org.apache.commons.lang3.StringUtils.isBlank(packageName)) {
          return Message.error("文件上传失败：PKG-INFO 文件不存在");
        }
        is = UdfUtils.getZipInputStreamByTarInputStream(file, packageName);
        newPath = fsPath.getPath() + FsPath.SEPARATOR + fileName;
        FsPath fsPathNew = new FsPath(newPath);
        outputStream = fileSystem.write(fsPathNew, true);
        IOUtils.copy(is, outputStream);
      } catch (Exception e) {
        return Message.error("文件上传失败：" + e.getMessage());
      } finally {
        if (outputStream != null) {
          IOUtils.closeQuietly(outputStream);
        }
        if (is != null) {
          IOUtils.closeQuietly(is);
        }
        fileSystem.close();
      }
    }
    // 返回成功消息并包含文件地址
    return Message.ok()
        .data("filePath", newPath)
        .data("dependencies", dependencies)
        .data("fileName", fileName);
  }

  @ApiImplicitParam(
      name = "path",
      dataType = "String",
      value = "path",
      example = "file:///test-dir/test-sub-dir/test1012_01.py")
  @RequestMapping(path = "/get-register-functions", method = RequestMethod.GET)
  public Message getRegisterFunctions(HttpServletRequest req, @RequestParam("path") String path)
      throws IOException {
    if (StringUtils.endsWithIgnoreCase(path, Constants.FILE_EXTENSION_PY)
        || StringUtils.endsWithIgnoreCase(path, Constants.FILE_EXTENSION_SCALA)) {
      if (StringUtils.startsWithIgnoreCase(path, StorageUtils$.MODULE$.FILE_SCHEMA())) {
        FileSystem fileSystem = null;
        try {
          // 获取登录用户
          String userName = ModuleUserUtils.getOperationUser(req, "get-register-functions");

          FsPath fsPath = new FsPath(path);
          // 获取文件系统实例
          fileSystem = (FileSystem) FSFactory.getFsByProxyUser(fsPath, userName);
          fileSystem.init(null);
          if (fileSystem.canRead(fsPath)) {
            return Message.ok()
                .data("functions", UdfUtils.getRegisterFunctions(fileSystem, fsPath, path));
          } else {
            return Message.error("您没有权限访问该文件");
          }
        } catch (Exception e) {
          return Message.error("解析文件失败，错误信息：" + e);
        } finally {
          if (fileSystem != null) {
            fileSystem.close();
          }
        }
      } else {
        return Message.error("仅支持本地文件");
      }
    } else {
      return Message.error("仅支持.py和.scala文件");
    }
  }
}
