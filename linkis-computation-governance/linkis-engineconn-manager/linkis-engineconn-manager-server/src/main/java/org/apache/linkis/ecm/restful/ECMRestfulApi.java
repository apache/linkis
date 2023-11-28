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

package org.apache.linkis.ecm.restful;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;

import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary.*;

@Api(tags = "ECM")
@RequestMapping(path = "/engineconnManager")
@RestController
public class ECMRestfulApi {

  private final Logger logger = LoggerFactory.getLogger(ECMRestfulApi.class);

  /**
   * * Reason for using the get method: Added gateway forwarding rules, which only support get
   * requests
   *
   * @param req
   * @param response
   * @param emInstance
   * @param instance
   * @param logDirSuffix
   * @param logType
   * @throws IOException
   */
  @ApiOperation(
      value = "downloadEngineLog",
      notes = "download engine log",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "emInstance",
        required = true,
        dataType = "String",
        example = "xxx0002:9102"),
    @ApiImplicitParam(
        name = "instance",
        required = true,
        dataType = "String",
        example = "xxx0002:35873"),
    @ApiImplicitParam(name = "logDirSuffix", required = true, dataType = "String"),
    @ApiImplicitParam(name = "logType", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/downloadEngineLog", method = RequestMethod.GET)
  public Message downloadEngineLog(
      HttpServletRequest req,
      HttpServletResponse response,
      @RequestParam(value = "emInstance") String emInstance,
      @RequestParam(value = "instance") String instance,
      @RequestParam(value = "logDirSuffix") String logDirSuffix,
      @RequestParam(value = "logType") String logType)
      throws IOException {
    String userName = ModuleUserUtils.getOperationUser(req, "downloadEngineLog");
    if (StringUtils.isBlank(instance)) {
      return Message.error(MessageFormat.format(PARAMETER_NOT_NULL.getErrorDesc(), "instance"));
    }
    if (StringUtils.isBlank(logDirSuffix)) {
      return Message.error(MessageFormat.format(PARAMETER_NOT_NULL.getErrorDesc(), "logDirSuffix"));
    }
    if (StringUtils.isBlank(logType)) {
      return Message.error(MessageFormat.format(PARAMETER_NOT_NULL.getErrorDesc(), "logType"));
    } else if (!logType.equals("stdout")
        && !logType.equals("stderr")
        && !logType.equals("gc")
        && !logType.equals("yarnApp")) {
      return Message.error(LOGTYPE_ERROR.getErrorDesc());
    }
    // 获取文件的权限归属者
    FileOwnerAttributeView ownerView =
        Files.getFileAttributeView(
            Paths.get(logDirSuffix + "/" + logType), FileOwnerAttributeView.class);
    UserPrincipal owner = ownerView.getOwner();
    if (!owner.getName().equals(userName)
        && Configuration.isNotAdmin(userName)
        && Configuration.isNotJobHistoryAdmin(userName)) {
      return Message.error(
          MessageFormat.format(NOT_PERMISSION.getErrorDesc(), userName, emInstance));
    }
    File inputFile = new File(logDirSuffix, logType);
    if (!inputFile.exists()) {
      return Message.error(MessageFormat.format(LOG_IS_NOT_EXISTS.getErrorDesc(), logDirSuffix));
    } else {
      long fileSizeInBytes = inputFile.length();
      long fileSizeInMegabytes = fileSizeInBytes / (1024 * 1024);
      if (fileSizeInMegabytes > 100) {
        return Message.error(MessageFormat.format(FILE_IS_OVERSIZE.getErrorDesc(), logDirSuffix));
      }
      ServletOutputStream outputStream = null;
      FileInputStream inputStream = null;
      BufferedInputStream fis = null;
      PrintWriter writer = null;
      try {
        inputStream = new FileInputStream(inputFile);
        fis = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        response.setCharacterEncoding(Consts.UTF_8.toString());
        java.nio.file.Path source = Paths.get(inputFile.getPath());
        response.addHeader("Content-Type", Files.probeContentType(source));
        // filename eg:xxx002_11529_stdout.txt
        response.addHeader(
            "Content-Disposition",
            "attachment;filename=" + instance.replace(":", "_") + "_" + logType + ".txt");
        response.addHeader("Content-Length", fileSizeInBytes + "");
        outputStream = response.getOutputStream();
        while ((bytesRead = fis.read(buffer, 0, 1024)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      } catch (IOException e) {
        logger.warn("Download EngineLog Failed Msg :", e);
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
        IOUtils.closeQuietly(inputStream);
      }
      return Message.ok();
    }
  }
}
