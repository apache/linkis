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

package org.apache.linkis.engineplugin.server.restful;

import org.apache.linkis.bml.protocol.Version;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.server.service.EngineConnResourceService;
import org.apache.linkis.engineplugin.server.service.EnginePluginAdminService;
import org.apache.linkis.engineplugin.server.service.RefreshEngineConnResourceRequest;
import org.apache.linkis.engineplugin.vo.EnginePluginBMLVo;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "ECP(engineconn plugin) operation")
@RestController
@RequestMapping(path = "/engineplugin")
public class EnginePluginRestful {

  private static final Logger log = LoggerFactory.getLogger(EnginePluginRestful.class);

  @Autowired private EngineConnResourceService engineConnResourceService;
  @Autowired private EnginePluginAdminService enginePluginAdminService;

  @ApiOperation(
      value = "rollBack",
      notes = "modify the default bmlResourceVersion of the engineplugin",
      response = Message.class)
  @RequestMapping(path = "/rollBack", method = RequestMethod.POST)
  public Message rollBackEnginePlugin(
      HttpServletRequest req, @RequestBody EngineConnBmlResource engineConnBmlResource) {
    String username = ModuleUserUtils.getOperationUser(req, "rollBackEnginePlugin");
    if (Configuration.isAdmin(username)) {
      log.info(
          "{} start to rollBack EnginePlugin {} {}",
          username,
          engineConnBmlResource.getEngineConnType(),
          engineConnBmlResource.getVersion());
      try {
        enginePluginAdminService.rollBackEnginePlugin(engineConnBmlResource);
        log.info(
            "{} finished to rollBack EnginePlugin {} {}",
            username,
            engineConnBmlResource.getEngineConnType(),
            engineConnBmlResource.getVersion());
        return Message.ok();
      } catch (Exception e) {
        return Message.error(e.getMessage());
      }
    } else {
      return Message.error("Only administrators can operate");
    }
  }

  @ApiOperation(
      value = "getVersionList",
      notes = "get all bmlResourceVersion of engineplugin",
      response = Message.class)
  @RequestMapping(path = "/getVersionList", method = RequestMethod.GET)
  public Message getVersionList(
      HttpServletRequest req,
      @RequestParam(value = "ecType", required = false) String ecType,
      @RequestParam(value = "version", required = false) String version,
      @RequestParam(value = "bmlResourceId", required = false) String bmlResourceId) {
    String username = ModuleUserUtils.getOperationUser(req, "getVersionList");
    if (Configuration.isAdmin(username)) {
      log.info("{} start to get all ec resource versionList", username);
      try {
        List<Version> typeList = enginePluginAdminService.getVersionList(username, bmlResourceId);
        log.info("{} finished get all ec resource versionList", username);
        return Message.ok().data("versionList", typeList);
      } catch (Exception e) {
        return Message.error(e.getMessage());
      }
    } else {
      return Message.error("Only administrators can operate");
    }
  }

  @ApiOperation(
      value = "getTypeList",
      notes = "get all types of engineplugin",
      response = Message.class)
  @RequestMapping(path = "/getTypeList", method = RequestMethod.GET)
  public Message getTypeList(HttpServletRequest req) {
    String username = ModuleUserUtils.getOperationUser(req, "getTypeList");
    if (Configuration.isAdmin(username)) {
      log.info("{} start to get all ec resource TypeList", username);
      try {
        List<String> typeList = enginePluginAdminService.getTypeList();
        log.info("{} finished get all ec resource TypeList", username);
        return Message.ok().data("typeList", typeList);
      } catch (Exception e) {
        return Message.error(e.getMessage());
      }
    } else {
      return Message.error("Only administrators can operate");
    }
  }

  @ApiOperation(
      value = "getTypeVersionList",
      notes = "get all versions of the engineplgin type",
      response = Message.class)
  @RequestMapping(path = "/getTypeVersionList/{type}", method = RequestMethod.GET)
  public Message getTypeVersionList(@PathVariable("type") String type, HttpServletRequest req) {
    String username = ModuleUserUtils.getOperationUser(req, "getTypeList");
    if (Configuration.isAdmin(username)) {
      log.info("{} start to get all ec resource TypeList", username);
      try {
        List<String> typeList = enginePluginAdminService.getTypeVersionList(type);
        log.info("{} finished get all ec resource TypeList", username);
        return Message.ok().data("queryList", typeList);
      } catch (Exception e) {
        return Message.error(e.getMessage());
      }
    } else {
      return Message.error("Only administrators can operate");
    }
  }

  @ApiOperation(
      value = "updateEnginePluginBML",
      notes = "Add a new version of the engineplugin",
      response = Message.class)
  @RequestMapping(path = "/updateEnginePluginBML", method = RequestMethod.POST)
  public Message updateEnginePluginBML(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "ecType") String ecType,
      @RequestParam(value = "version") String version,
      @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force,
      HttpServletRequest req) {

    if (StringUtils.isBlank(ecType)) {
      return Message.error("ecType cannot be null");
    } else if (StringUtils.isBlank(version)) {
      return Message.error("version cannot be null");
    }
    if (file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
      String username = ModuleUserUtils.getOperationUser(req, "updateEnginePluginBML");
      if (Configuration.isAdmin(username)) {
        log.info("{} start to update enginePlugin {} {}", username, ecType, version);
        try {
          enginePluginAdminService.uploadToECHome(file);
          RefreshEngineConnResourceRequest refreshEngineConnResourceRequest =
              new RefreshEngineConnResourceRequest();
          refreshEngineConnResourceRequest.setEngineConnType(ecType);
          refreshEngineConnResourceRequest.setVersion(version);
          engineConnResourceService.refresh(refreshEngineConnResourceRequest, force);
        } catch (Exception e) {
          return Message.error(e.getMessage());
        }
        log.info("{} finished to update enginePlugin {} {}", username, ecType, version);
        return Message.ok().data("msg", "upload file success");
      } else {
        return Message.error("Only administrators can operate");
      }
    } else {
      return Message.error("Only support zip format file");
    }
  }

  @ApiOperation(value = "list", notes = "list all engineplugin", response = Message.class)
  @RequestMapping(path = "/list", method = RequestMethod.GET)
  public Message list(
      @RequestParam(value = "ecType", required = false) String ecType,
      @RequestParam(value = "version", required = false) String version,
      @RequestParam(value = "currentPage", required = false) Integer currentPage,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      HttpServletRequest req) {
    String username = ModuleUserUtils.getOperationUser(req, "list");
    if (Configuration.isAdmin(username)) {
      log.info("{} start to list all ec resource", username);
      EnginePluginBMLVo enginePluginBMLVo = new EnginePluginBMLVo(ecType, version);
      enginePluginBMLVo.setCurrentPage(null != currentPage ? currentPage : 1);
      enginePluginBMLVo.setPageSize(null != pageSize ? pageSize : 10);
      PageInfo<EngineConnBmlResource> engineConnBmlResourcePageInfo =
          enginePluginAdminService.queryDataSourceInfoPage(enginePluginBMLVo);
      log.info("{} finished to list all ec resource", username);
      List<EngineConnBmlResource> queryList = engineConnBmlResourcePageInfo.getList();
      return Message.ok()
          .data("queryList", queryList)
          .data("totalPage", engineConnBmlResourcePageInfo.getTotal());
    } else {
      return Message.error("Only administrators can operate");
    }
  }

  @ApiOperation(
      value = "uploadEnginePluginBML",
      notes = "add one engineplugin bml",
      response = Message.class)
  @RequestMapping(path = "/uploadEnginePluginBML", method = RequestMethod.POST)
  public Message uploadEnginePluginBML(
      @RequestParam("file") MultipartFile file, HttpServletRequest req) {
    if (file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
      String username = ModuleUserUtils.getOperationUser(req, "uploadEnginePluginBML");
      if (Configuration.isAdmin(username)) {
        log.info("{} start to upload enginePlugin", username);
        try {
          enginePluginAdminService.uploadToECHome(file);
        } catch (Exception e) {
          return Message.error(e.getMessage());
        }
        engineConnResourceService.refreshAll(true, false);
        log.info("{} finished to upload enginePlugin", username);
        return Message.ok().data("msg", "upload file success");
      } else {
        return Message.error("Only administrators can operate");
      }
    } else {
      return Message.error("Only suppose zip format file");
    }
  }

  @ApiOperation(
      value = "deleteEnginePluginBML",
      notes = "delete one engineplugin bml",
      response = Message.class)
  @RequestMapping(path = "/deleteEnginePluginBML", method = RequestMethod.GET)
  public Message deleteEnginePluginBML(
      HttpServletRequest req,
      @RequestParam(value = "ecType") String ecType,
      @RequestParam(value = "version", required = false) String version) {
    String username = ModuleUserUtils.getOperationUser(req, "deleteEnginePluginBML");
    if (Configuration.isAdmin(username)) {
      log.info("{} start to delete engineplugin {} {}", username, ecType, version);
      try {
        enginePluginAdminService.deleteEnginePluginBML(ecType, version, username);
      } catch (Exception e) {
        return Message.error(e.getMessage());
      }
      log.info("{} finished to delete engineplugin {} {}", username, ecType, version);
      return Message.ok().data("msg", "delete successfully");
    } else {
      return Message.error("Only administrators can operate");
    }
  }

  @ApiOperation(
      value = "refreshAll",
      notes = "refresh all engineconn resource",
      response = Message.class)
  @RequestMapping(path = "/refreshAll", method = RequestMethod.GET)
  public Message refreshAll(
      HttpServletRequest req,
      @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force) {
    String username = ModuleUserUtils.getOperationUser(req, "refreshAll");
    if (Configuration.isAdmin(username)) {
      log.info("{} start to refresh all ec resource", username);
      engineConnResourceService.refreshAll(true, force);
      log.info("{} finished to refresh all ec resource", username);
      return Message.ok().data("msg", "Refresh successfully");
    } else {
      return Message.error("Only administrators can operate");
    }
  }

  @ApiOperation(
      value = "refreshOne",
      notes = "refresh one engineconn resource",
      response = Message.class)
  @RequestMapping(path = "/refresh", method = RequestMethod.GET)
  public Message refreshOne(
      HttpServletRequest req,
      @RequestParam(value = "ecType") String ecType,
      @RequestParam(value = "version", required = false) String version,
      @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force) {
    String username = ModuleUserUtils.getOperationUser(req, "refreshOne");
    if (Configuration.isAdmin(username)) {
      log.info("{} start to refresh {} ec resource", username, ecType);
      RefreshEngineConnResourceRequest refreshEngineConnResourceRequest =
          new RefreshEngineConnResourceRequest();
      refreshEngineConnResourceRequest.setEngineConnType(ecType);
      refreshEngineConnResourceRequest.setVersion(version);

      engineConnResourceService.refresh(refreshEngineConnResourceRequest, force);
      log.info("{} finished to refresh {} ec resource", username, ecType);
      return Message.ok().data("msg", "Refresh successfully");
    } else {
      return Message.error("Only administrators can operate");
    }
  }
}
