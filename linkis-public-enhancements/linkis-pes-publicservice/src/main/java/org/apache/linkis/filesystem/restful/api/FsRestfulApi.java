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

package org.apache.linkis.filesystem.restful.api;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.FsWriter;
import org.apache.linkis.common.utils.AESUtils;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.common.utils.ResultSetUtils;
import org.apache.linkis.filesystem.entity.DirFileTree;
import org.apache.linkis.filesystem.entity.LogLevel;
import org.apache.linkis.filesystem.exception.WorkSpaceException;
import org.apache.linkis.filesystem.exception.WorkspaceExceptionManager;
import org.apache.linkis.filesystem.service.FsService;
import org.apache.linkis.filesystem.util.FilesystemUtils;
import org.apache.linkis.filesystem.util.WorkspaceUtil;
import org.apache.linkis.filesystem.utils.UserGroupUtils;
import org.apache.linkis.filesystem.validator.PathValidator$;
import org.apache.linkis.governance.common.utils.LoggerUtils;
import org.apache.linkis.hadoop.common.conf.HadoopConf;
import org.apache.linkis.hadoop.common.utils.HDFSUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.apache.linkis.storage.conf.LinkisStorageConf;
import org.apache.linkis.storage.csv.CSVFsWriter;
import org.apache.linkis.storage.domain.FsPathListWithError;
import org.apache.linkis.storage.excel.*;
import org.apache.linkis.storage.exception.ColLengthExceedException;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.script.*;
import org.apache.linkis.storage.source.FileSource;
import org.apache.linkis.storage.source.FileSource$;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.http.Consts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.filesystem.conf.WorkSpaceConfiguration.*;
import static org.apache.linkis.filesystem.constant.WorkSpaceConstants.*;
import static org.apache.linkis.manager.label.utils.LabelUtils.logger;

@Api(tags = "file system")
@RestController
@RequestMapping(path = "/filesystem")
public class FsRestfulApi {

  @Autowired private FsService fsService;

  private final Logger LOGGER = LoggerFactory.getLogger(getClass());

  /**
   * check 权限
   *
   * @param requestPath
   * @param userName
   * @return
   */
  private boolean checkIsUsersDirectory(String requestPath, String userName, Boolean withAdmin) {
    // 配置文件默认关闭检查，withadmin默认true，特殊情况传false 开启权限检查（
    // The configuration file defaults to disable checking, with admin defaulting to true, and in
    // special cases, false is passed to enable permission checking）
    boolean ownerCheck = FILESYSTEM_PATH_CHECK_OWNER.getValue();
    if (!ownerCheck && withAdmin) {
      LOGGER.debug("not check filesystem owner.");
      return true;
    }
    requestPath = requestPath.toLowerCase().trim() + "/";
    String hdfsUserRootPathPrefix =
        WorkspaceUtil.suffixTuning(HDFS_USER_ROOT_PATH_PREFIX.getValue());
    String hdfsUserRootPathSuffix = HDFS_USER_ROOT_PATH_SUFFIX.getValue();
    String localUserRootPath = WorkspaceUtil.suffixTuning(LOCAL_USER_ROOT_PATH.getValue());

    String workspacePath = hdfsUserRootPathPrefix + userName + hdfsUserRootPathSuffix;
    String enginconnPath = localUserRootPath + userName;
    // 管理员修改其他用户文件目录时，会导致用户无法使用文件，故此优化管理员不能修改(When administrators modify the file directory of other
    // users,
    // it will cause users to be unable to use the file, so the optimization administrator cannot
    // modify it)
    if (withAdmin && Configuration.isJobHistoryAdmin(userName)) {
      workspacePath = hdfsUserRootPathPrefix;
      enginconnPath = localUserRootPath;
    }
    LOGGER.debug("requestPath:" + requestPath);
    LOGGER.debug("workspacePath:" + workspacePath);
    LOGGER.debug("enginconnPath:" + enginconnPath);
    LOGGER.debug("adminUser:" + String.join(",", Configuration.getJobHistoryAdmin()));
    return (requestPath.contains(workspacePath)) || (requestPath.contains(enginconnPath));
  }

  private boolean checkIsUsersDirectory(String requestPath, String userName) {
    return checkIsUsersDirectory(requestPath, userName, true);
  }

  @ApiOperation(value = "getUserRootPath", notes = "get user root path", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "pathType", required = false, dataType = "String", value = "path type")
  })
  @RequestMapping(path = "/getUserRootPath", method = RequestMethod.GET)
  public Message getUserRootPath(
      HttpServletRequest req, @RequestParam(value = "pathType", required = false) String pathType)
      throws IOException, WorkSpaceException {
    String userName = ModuleUserUtils.getOperationUser(req, "getUserRootPath");
    String hdfsUserRootPathPrefix =
        WorkspaceUtil.suffixTuning(HDFS_USER_ROOT_PATH_PREFIX.getValue());
    String hdfsUserRootPathSuffix = HDFS_USER_ROOT_PATH_SUFFIX.getValue();
    String localUserRootPath = WorkspaceUtil.suffixTuning(LOCAL_USER_ROOT_PATH.getValue());
    String path;
    String returnType;
    if (StorageUtils.HDFS().equalsIgnoreCase(pathType)) {
      path = hdfsUserRootPathPrefix + userName + hdfsUserRootPathSuffix;
      returnType = StorageUtils.HDFS().toUpperCase();
    } else if (StorageUtils.S3().equalsIgnoreCase(pathType)) {
      path = localUserRootPath + userName;
      returnType = StorageUtils.S3().toUpperCase();
    } else {
      path = localUserRootPath + userName;
      returnType = LOCAL_RETURN_TYPE;
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    if (!fileSystem.exists(fsPath)) {
      if (!FsPath.WINDOWS && !UserGroupUtils.isUserExist(userName)) {
        LOGGER.warn("User {} not exist in linkis node.", userName);
        throw WorkspaceExceptionManager.createException(80031, userName);
      }
      if (FILESYSTEM_PATH_AUTO_CREATE.getValue()) {
        try {
          fileSystem.mkdirs(fsPath);
          return Message.ok().data(String.format("user%sRootPath", returnType), path);
        } catch (IOException e) {
          LOGGER.error(e.getMessage(), e);
          throw WorkspaceExceptionManager.createException(80030, path);
        }
      }
      throw WorkspaceExceptionManager.createException(80003, path);
    }
    return Message.ok().data(String.format("user%sRootPath", returnType), path);
  }

  @ApiOperation(value = "createNewDir", notes = "create new dir", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"json"})
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = true, dataType = "String", value = "Path")
  })
  @RequestMapping(path = "/createNewDir", method = RequestMethod.POST)
  public Message createNewDir(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException, WorkSpaceException {

    String path = json.get("path").textValue();
    String userName = ModuleUserUtils.getOperationUser(req, "createNewDir " + path);
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    WorkspaceUtil.fileAndDirNameSpecialCharCheck(path);
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    if (fileSystem.exists(fsPath)) {
      throw WorkspaceExceptionManager.createException(80005, path);
    }
    fileSystem.mkdirs(fsPath);
    return Message.ok();
  }

  @ApiOperation(value = "createNewFile", notes = "create new file", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = true, dataType = "String", value = "Path")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/createNewFile", method = RequestMethod.POST)
  public Message createNewFile(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException, WorkSpaceException {
    String path = json.get("path").textValue();
    String userName = ModuleUserUtils.getOperationUser(req, "createNewFile " + path);
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    WorkspaceUtil.fileAndDirNameSpecialCharCheck(path);
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    if (fileSystem.exists(fsPath)) {
      throw WorkspaceExceptionManager.createException(80006, path);
    }
    fileSystem.createNewFile(fsPath);
    return Message.ok();
  }

  @ApiOperation(value = "rename", notes = "rename", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "oldDest", required = true, dataType = "String", value = "old dest"),
    @ApiImplicitParam(name = "newDest", required = true, dataType = "String", value = "new dest")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/rename", method = RequestMethod.POST)
  public Message rename(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException, WorkSpaceException {
    String oldDest = json.get("oldDest").textValue();
    String newDest = json.get("newDest").textValue();
    String userName = ModuleUserUtils.getOperationUser(req, "rename " + newDest);
    if (FILESYSTEM_PATH_CHECK_TRIGGER.getValue()) {
      LOGGER.info(
          String.format(
              "path check trigger is open,now check the path,oldDest:%s,newDest:%s",
              oldDest, newDest));
      PathValidator$.MODULE$.validate(oldDest, userName);
      PathValidator$.MODULE$.validate(newDest, userName);
    }
    if (!checkIsUsersDirectory(newDest, userName, false)) {
      throw WorkspaceExceptionManager.createException(80010, userName, newDest);
    }
    if (StringUtils.isEmpty(oldDest)) {
      throw WorkspaceExceptionManager.createException(80004, oldDest);
    }
    if (StringUtils.isEmpty(newDest)) {
      // No change in file name(文件名字无变化)
      return Message.ok();
    }
    WorkspaceUtil.fileAndDirNameSpecialCharCheck(newDest);
    FsPath fsPathOld = new FsPath(oldDest);
    FsPath fsPathNew = new FsPath(newDest);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPathOld);
    if (fileSystem.exists(fsPathNew)) {
      throw WorkspaceExceptionManager.createException(80007, newDest);
    }
    fileSystem.renameTo(fsPathOld, fsPathNew);
    return Message.ok();
  }

  @ApiOperation(value = "move", notes = "move", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "filePath", required = true, dataType = "String", value = "file path"),
    @ApiImplicitParam(name = "newDest", required = true, dataType = "String", value = "new dest")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/move", method = RequestMethod.POST)
  public Message move(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException, WorkSpaceException {
    String filePath = json.get("filePath").textValue();
    String newDir = json.get("newDir").textValue();
    String userName = ModuleUserUtils.getOperationUser(req, "move " + filePath);
    if (StringUtils.isEmpty(filePath)) {
      return Message.ok();
    }
    if (StringUtils.isEmpty(newDir)) {
      throw WorkspaceExceptionManager.createException(80004, newDir);
    }
    if (FILESYSTEM_PATH_CHECK_TRIGGER.getValue()) {
      LOGGER.info(
          String.format(
              "path check trigger is open,now check the path,oldDest:%s,newDest:%s",
              filePath, newDir));
      PathValidator$.MODULE$.validate(filePath, userName);
      PathValidator$.MODULE$.validate(newDir, userName);
    }
    if (!checkIsUsersDirectory(filePath, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, filePath);
    }
    FsPath flieOldPath = new FsPath(filePath);
    String name =
        flieOldPath.getPath().substring(flieOldPath.getPath().lastIndexOf(FsPath.SEPARATOR) + 1);
    FsPath flieNewPath = new FsPath(newDir + FsPath.SEPARATOR + name);
    FileSystem fileSystem = fsService.getFileSystem(userName, flieOldPath);
    WorkspaceUtil.fileAndDirNameSpecialCharCheck(flieOldPath.getPath());
    WorkspaceUtil.fileAndDirNameSpecialCharCheck(flieNewPath.getPath());
    if (!fileSystem.exists(flieOldPath)) {
      throw WorkspaceExceptionManager.createException(80013, filePath);
    }
    fileSystem.renameTo(flieOldPath, flieNewPath);
    return Message.ok();
  }

  @ApiOperation(value = "upload", notes = "upload", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = true, dataType = "String", value = "path"),
    @ApiImplicitParam(name = "file", required = true, dataType = "List<MultipartFile> ")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/upload", method = RequestMethod.POST)
  public Message upload(
      HttpServletRequest req,
      @RequestParam("path") String path,
      @RequestParam("file") List<MultipartFile> files)
      throws IOException, WorkSpaceException {

    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String userName = ModuleUserUtils.getOperationUser(req, "upload " + path);
    LoggerUtils.setJobIdMDC("uploadThread_" + userName);
    LOGGER.info("userName {} start to upload File {}", userName, path);
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    for (MultipartFile p : files) {
      String fileName = p.getOriginalFilename();
      WorkspaceUtil.charCheckFileName(fileName);
      FsPath fsPathNew = new FsPath(fsPath.getPath() + "/" + fileName);
      fileSystem.createNewFile(fsPathNew);
      try (InputStream is = p.getInputStream();
          OutputStream outputStream = fileSystem.write(fsPathNew, true)) {
        IOUtils.copy(is, outputStream);
      }
    }
    LOGGER.info("userName {} Finished to upload File {}", userName, path);
    LoggerUtils.removeJobIdMDC();

    return Message.ok();
  }

  @ApiOperation(value = "deleteDirOrFile", notes = "delete dir or file", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = true, dataType = "String", value = "path")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/deleteDirOrFile", method = RequestMethod.POST)
  public Message deleteDirOrFile(HttpServletRequest req, @RequestBody JsonNode json)
      throws IOException, WorkSpaceException {

    String path = json.get("path").textValue();
    String userName = ModuleUserUtils.getOperationUser(req, "deleteDirOrFile " + path);
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    if (!fileSystem.exists(fsPath)) {
      throw WorkspaceExceptionManager.createException(80008);
    }
    if (!fileSystem.canWrite(fsPath.getParent()) || !fileSystem.canExecute(fsPath.getParent())) {
      throw WorkspaceExceptionManager.createException(80009);
    }
    deleteAllFiles(fileSystem, fsPath);
    return Message.ok();
  }

  @ApiOperation(value = "getDirFileTrees", notes = "get dir file trees", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "path")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/getDirFileTrees", method = RequestMethod.GET)
  public Message getDirFileTrees(
      HttpServletRequest req, @RequestParam(value = "path", required = false) String path)
      throws IOException, WorkSpaceException {

    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String userName = ModuleUserUtils.getOperationUser(req, "getDirFileTrees " + path);
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
    if (!fileSystem.exists(fsPath)) {
      return Message.ok().data("dirFileTrees", null);
    }
    DirFileTree dirFileTree = new DirFileTree();
    dirFileTree.setPath(fsPath.getSchemaPath());
    // if(!isInUserWorkspace(path,userName)) throw new WorkSpaceException("The user does not
    // have permission to view the contents of the directory");
    if (!fileSystem.canExecute(fsPath) || !fileSystem.canRead(fsPath)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    dirFileTree.setName(new File(path).getName());
    dirFileTree.setChildren(new ArrayList<>());
    Set<String> fileNameSet = new HashSet<>();
    fileNameSet.add(dirFileTree.getPath().trim());
    FsPathListWithError fsPathListWithError = fileSystem.listPathWithError(fsPath);
    if (fsPathListWithError != null) {
      for (FsPath children : fsPathListWithError.getFsPaths()) {
        DirFileTree dirFileTreeChildren = new DirFileTree();
        dirFileTreeChildren.setName(new File(children.getPath()).getName());

        dirFileTreeChildren.setPath(fsPath.getFsType() + "://" + children.getPath());
        dirFileTreeChildren.setProperties(new HashMap<>());
        dirFileTreeChildren.setParentPath(fsPath.getSchemaPath());
        if (fileNameSet.contains(dirFileTreeChildren.getPath().trim())) {
          LOGGER.info("File {} is duplicate", dirFileTreeChildren.getPath());
          continue;
        } else {
          fileNameSet.add(dirFileTreeChildren.getPath().trim());
        }
        if (!children.isdir()) {
          dirFileTreeChildren.setIsLeaf(true);
          dirFileTreeChildren.getProperties().put("size", String.valueOf(children.getLength()));
          dirFileTreeChildren
              .getProperties()
              .put("modifytime", String.valueOf(children.getModification_time()));
        }
        dirFileTree.getChildren().add(dirFileTreeChildren);
      }
    }
    return Message.ok().data("dirFileTrees", dirFileTree);
  }

  @ApiOperation(value = "download", notes = "download", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = true, dataType = "String", value = "path"),
    @ApiImplicitParam(name = "charset", required = true, dataType = "String", value = "charset")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/download", method = RequestMethod.POST)
  public void download(
      HttpServletRequest req, HttpServletResponse response, @RequestBody Map<String, String> json)
      throws IOException, WorkSpaceException {
    InputStream inputStream = null;
    ServletOutputStream outputStream = null;
    PrintWriter writer = null;
    try {
      String charset = json.get("charset");
      String path = json.get("path");
      String userName = ModuleUserUtils.getOperationUser(req, "download " + path);
      LoggerUtils.setJobIdMDC("downloadThread_" + userName);
      LOGGER.info("userName {} start to download File {}", userName, path);
      if (StringUtils.isEmpty(path)) {
        throw WorkspaceExceptionManager.createException(80004, path);
      }
      if (StringUtils.isEmpty(charset)) {
        charset = Consts.UTF_8.toString();
      }
      if (!checkIsUsersDirectory(path, userName)) {
        throw WorkspaceExceptionManager.createException(80010, userName, path);
      }
      FsPath fsPath = new FsPath(path);
      // TODO: 2018/11/29 Judging the directory, the directory cannot be
      // downloaded(判断目录,目录不能下载)
      FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
      if (!fileSystem.exists(fsPath)) {
        throw WorkspaceExceptionManager.createException(80011, path);
      }
      inputStream = fileSystem.read(fsPath);
      byte[] buffer = new byte[1024];
      int bytesRead = 0;
      response.setCharacterEncoding(charset);
      Path source = Paths.get(fsPath.getPath());
      response.addHeader("Content-Type", Files.probeContentType(source));
      response.addHeader("Content-Disposition", "attachment;filename=" + new File(path).getName());
      outputStream = response.getOutputStream();
      while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      LOGGER.info("userName {} Finished to download File {}", userName, path);
    } catch (Exception e) {
      LOGGER.error("download failed", e);
      response.reset();
      response.setCharacterEncoding(Consts.UTF_8.toString());
      response.setContentType("text/plain; charset=utf-8");
      writer = response.getWriter();
      writer.append("error(错误):" + e.getMessage());
      writer.flush();
    } finally {
      LoggerUtils.removeJobIdMDC();

      if (outputStream != null) {
        outputStream.flush();
      }
      IOUtils.closeQuietly(outputStream);
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(writer);
    }
  }

  @ApiOperation(value = "isExist", notes = "is exist", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "path", dataType = "String", value = "path")})
  @RequestMapping(path = "/isExist", method = RequestMethod.GET)
  public Message isExist(
      HttpServletRequest req, @RequestParam(value = "path", required = false) String path)
      throws IOException, WorkSpaceException {
    String userName = ModuleUserUtils.getOperationUser(req, "isExist " + path);
    FsPath fsPath = new FsPath(path);
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    return Message.ok().data("isExist", fileSystem.exists(fsPath));
  }

  @ApiOperation(value = "FileInfo", notes = "File_Info", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "Path"),
    @ApiImplicitParam(
        name = "pageSize",
        required = true,
        dataType = "Integer",
        defaultValue = "5000")
  })
  @RequestMapping(path = "/fileInfo", method = RequestMethod.GET)
  public Message fileInfo(
      HttpServletRequest req,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "pageSize", defaultValue = "5000") Integer pageSize)
      throws IOException, WorkSpaceException {
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String userName = ModuleUserUtils.getOperationUser(req, "fileInfo " + path);
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
    // Throws an exception if the file does not have read access(如果文件没读权限，抛出异常)
    if (!fileSystem.canRead(fsPath)) {
      throw WorkspaceExceptionManager.createException(80012);
    }
    FileSource fileSource = null;
    try {
      Message message = Message.ok();
      fileSource = FileSource$.MODULE$.create(fsPath, fileSystem);
      Pair<Object, Object>[] fileInfo = fileSource.getFileInfo(pageSize);
      IOUtils.closeQuietly(fileSource);
      if (null != fileInfo && fileInfo.length > 0) {
        message.data("path", path);
        message.data("colNumber", fileInfo[0].getFirst());
        message.data("rowNumber", fileInfo[0].getSecond());
      } else {
        message.data("path", path);
        message.data("colNumber", 0);
        message.data("rowNumber", 0);
      }
      return message;
    } finally {
      IOUtils.closeQuietly(fileSource);
    }
  }

  /**
   * Opens a file method. This method opens a file based on user requests and returns metadata and
   * content of the file.
   *
   * @param req The HttpServletRequest object used to retrieve request information.
   * @param path The file path, an optional parameter indicating the path of the file to be opened.
   * @param page The requested page number, with a default value of 1.
   * @param pageSize The number of rows per page, defaulting to 5000.
   * @param nullValue A string representing a null value, defaulting to an empty string. If
   *     specified, replaces null values with this string in query results.
   * @param enableLimit A string indicating whether query limitations should be enabled, with a
   *     default value of an empty string. If enabled, sets query limitations.
   * @return A Message object containing file metadata, content, and related messages.
   * @throws IOException Thrown if there's an I/O error.
   * @throws WorkSpaceException Thrown if the file fails to open.
   */
  @ApiOperation(value = "openFile", notes = "open file", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "Path"),
    @ApiImplicitParam(name = "page", required = true, dataType = "Integer", defaultValue = "1"),
    @ApiImplicitParam(name = "pageSize", dataType = "Integer", defaultValue = "5000"),
    @ApiImplicitParam(name = "nullValue", required = false, dataType = "String", defaultValue = ""),
    @ApiImplicitParam(
        name = "enableLimit",
        required = false,
        dataType = "String",
        defaultValue = ""),
    @ApiImplicitParam(name = "columnIndices", required = false, dataType = "array"),
    @ApiImplicitParam(
        name = "columnPageSize",
        required = false,
        dataType = "Integer",
        defaultValue = "500"),
    @ApiImplicitParam(
        name = "pageSize",
        required = true,
        dataType = "Integer",
        defaultValue = "5000"),
    @ApiImplicitParam(name = "maskedFieldNames", required = false, dataType = "String")
  })
  @RequestMapping(path = "/openFile", method = RequestMethod.GET)
  public Message openFile(
      HttpServletRequest req,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "pageSize", defaultValue = "5000") Integer pageSize,
      @RequestParam(value = "nullValue", defaultValue = "") String nullValue,
      @RequestParam(value = "enableLimit", defaultValue = "") String enableLimit,
      @RequestParam(value = "columnPage", required = false, defaultValue = "1") Integer columnPage,
      @RequestParam(value = "columnPageSize", required = false, defaultValue = "500")
          Integer columnPageSize,
      @RequestParam(value = "maskedFieldNames", required = false) String maskedFieldNames)
      throws IOException, WorkSpaceException {

    Message message = Message.ok();
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }

    if (columnPage < 0 || columnPageSize < 0 || columnPageSize > 500) {
      throw WorkspaceExceptionManager.createException(80036, path);
    }

    String userName = ModuleUserUtils.getOperationUser(req, "openFile " + path);
    LoggerUtils.setJobIdMDC("openFileThread_" + userName);
    LOGGER.info("userName {} start to open File {}", userName, path);
    Long startTime = System.currentTimeMillis();
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
    // Throws an exception if the file does not have read access(如果文件没读权限，抛出异常)
    if (!fileSystem.canRead(fsPath)) {
      throw WorkspaceExceptionManager.createException(80012);
    }

    int[] columnIndices = null;
    FileSource fileSource = null;
    try {
      fileSource = FileSource$.MODULE$.create(fsPath, fileSystem);
      if (nullValue != null && BLANK.equalsIgnoreCase(nullValue)) {
        nullValue = "";
      }
      if (FileSource$.MODULE$.isResultSet(fsPath.getPath())) {
        if (!StringUtils.isEmpty(nullValue)) {
          fileSource.addParams("nullValue", nullValue);
        }
        if (pageSize > FILESYSTEM_RESULTSET_ROW_LIMIT.getValue()) {
          throw WorkspaceExceptionManager.createException(
              80034, FILESYSTEM_RESULTSET_ROW_LIMIT.getValue());
        }

        if (StringUtils.isNotBlank(enableLimit) && "true".equals(enableLimit)) {
          LOGGER.info("set enable limit for thread: {}", Thread.currentThread().getName());
          LinkisStorageConf.enableLimitThreadLocal().set(enableLimit);
          // 组装列索引
          columnIndices = genColumnIndices(columnPage, columnPageSize);
          LinkisStorageConf.columnIndicesThreadLocal().set(columnIndices);
        }

        fileSource = fileSource.page(page, pageSize);
      } else if (fileSystem.getLength(fsPath)
          > ByteTimeUtils.byteStringAsBytes(FILESYSTEM_FILE_CHECK_SIZE.getValue())) {
        // Increase file size limit, making it easy to OOM without limitation
        throw WorkspaceExceptionManager.createException(80032);
      }
      try {
        Pair<Object, ArrayList<String[]>> result = fileSource.collect()[0];
        LOGGER.info(
            "Finished to open File {}, taken {} ms", path, System.currentTimeMillis() - startTime);
        IOUtils.closeQuietly(fileSource);
        Object metaMap = result.getFirst();
        Map[] newMap = null;
        try {
          if (metaMap instanceof Map[]) {
            Map[] realMap = (Map[]) metaMap;
            int realSize = realMap.length;

            // 判断列索引在实际列数范围内
            if ((columnPage - 1) * columnPageSize > realSize) {
              throw WorkspaceExceptionManager.createException(80036, path);
            }
            message.data("totalColumn", realSize);
            if (realSize > FILESYSTEM_RESULT_SET_COLUMN_LIMIT.getValue()) {
              message.data("column_limit_display", true);
              message.data(
                  "zh_msg",
                  "全量结果集超过"
                      + FILESYSTEM_RESULT_SET_COLUMN_LIMIT.getValue()
                      + "列，页面提供500列数据预览，如需查看完整结果集，请使用结果集导出功能");
              message.data(
                  "en_msg",
                  "Because your result set is large, to view the full result set, use the Result set Export feature.");
            }
            if (columnIndices == null || columnIndices.length >= realSize) {
              newMap = realMap;
            } else {
              int realLength =
                  (columnPage * columnPageSize) > realSize
                      ? realSize - (columnPage - 1) * columnPageSize
                      : columnPageSize;
              newMap = new Map[realLength];
              for (int i = 0; i < realLength; i++) {
                newMap[i] = realMap[columnIndices[i]];
              }
            }
          }
        } catch (Exception e) {
          LOGGER.info("Failed to set flag", e);
        }
        // 增加字段屏蔽
        Object resultmap = newMap == null ? metaMap : newMap;
        if (FileSource$.MODULE$.isResultSet(fsPath.getPath())
            && StringUtils.isNotBlank(maskedFieldNames)) {
          // 如果结果集并且屏蔽字段不为空，则执行屏蔽逻辑，反之则保持原逻辑
          Set<String> maskedFields =
              new HashSet<>(Arrays.asList(maskedFieldNames.toLowerCase().split(",")));
          Map[] metadata = filterMaskedFieldsFromMetadata(resultmap, maskedFields);
          List<String[]> fileContent =
              removeFieldsFromContent(resultmap, result.getSecond(), maskedFields);
          message.data("metadata", metadata).data("fileContent", fileContent);
        } else {
          message.data("metadata", resultmap).data("fileContent", result.getSecond());
        }
        message.data("type", fileSource.getFileSplits()[0].type());
        message.data("totalLine", fileSource.getTotalLine());
        return message.data("page", page).data("totalPage", 0);
      } catch (ColLengthExceedException e) {
        LOGGER.info("Failed to open file {}", path, e);
        message.data("type", fileSource.getFileSplits()[0].type());
        message.data("display_prohibited", true);
        message.data(
            "zh_msg",
            MessageFormat.format(
                "结果集存在字段值字符数超过{0}，如需查看全部数据请导出文件或使用字符串截取函数（substring、substr）截取相关字符即可前端展示数据内容",
                LinkisStorageConf.LINKIS_RESULT_COL_LENGTH()));
        message.data(
            "en_msg",
            MessageFormat.format(
                "There is a field value exceed {0} characters or col size exceed {1} in the result set. If you want to view it, please use the result set export function.",
                LinkisStorageConf.LINKIS_RESULT_COL_LENGTH(),
                LinkisStorageConf.LINKIS_RESULT_COLUMN_SIZE()));
        return message;
      }
    } finally {
      // 移除标识
      if (StringUtils.isNotBlank(enableLimit)) {
        LinkisStorageConf.enableLimitThreadLocal().remove();
      }
      LoggerUtils.removeJobIdMDC();
      IOUtils.closeQuietly(fileSource);
    }
  }
  /**
   * 删除指定字段的内容
   *
   * @param metadata 元数据数组，包含字段信息
   * @param contentList 需要处理的二维字符串数组
   * @param fieldsToRemove 需要删除的字段集合
   * @return 处理后的字符串数组，若输入无效返回空集合而非null
   */
  @SuppressWarnings("unchecked")
  private List<String[]> removeFieldsFromContent(
      Object metadata, List<String[]> contentList, Set<String> fieldsToRemove) {
    // 1. 参数校验
    if (metadata == null
        || fieldsToRemove == null
        || fieldsToRemove.isEmpty()
        || contentList == null
        || !(metadata instanceof Map[])) {
      return contentList;
    }

    // 2. 安全类型转换
    Map<String, Object>[] fieldMetadata = (Map<String, Object>[]) metadata;

    // 3. 收集需要删除的列索引（去重并排序）
    List<Integer> columnsToRemove =
        IntStream.range(0, fieldMetadata.length)
            .filter(
                i -> {
                  Map<String, Object> meta = fieldMetadata[i];
                  Object columnName = meta.get("columnName");
                  return columnName != null
                      && fieldsToRemove.contains(columnName.toString().toLowerCase());
                })
            .distinct()
            .boxed()
            .sorted((a, b) -> Integer.compare(b, a))
            .collect(Collectors.toList());

    // 如果没有需要删除的列，直接返回副本
    if (columnsToRemove.isEmpty()) {
      return new ArrayList<>(contentList);
    }
    // 4. 对每行数据进行处理（删除指定列）
    return contentList.stream()
        .map(
            row -> {
              if (row == null || row.length == 0) {
                return row;
              }
              // 创建可变列表以便删除元素
              List<String> rowList = new ArrayList<>(Arrays.asList(row));
              // 从后向前删除列，避免索引变化问题
              for (int columnIndex : columnsToRemove) {
                if (columnIndex < rowList.size()) {
                  rowList.remove(columnIndex);
                }
              }
              return rowList.toArray(new String[0]);
            })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private Map[] filterMaskedFieldsFromMetadata(Object metadata, Set<String> maskedFields) {
    // 1. 参数校验
    if (metadata == null || maskedFields == null || !(metadata instanceof Map[])) {
      return new Map[0];
    }

    // 2. 类型转换（已通过校验，可安全强转）
    Map<String, Object>[] originalMaps = (Map<String, Object>[]) metadata;

    // 3. 过滤逻辑（提取谓词增强可读性）
    Predicate<Map<String, Object>> isNotMaskedField =
        map -> !maskedFields.contains(map.get("columnName").toString().toLowerCase());

    // 4. 流处理 + 结果转换
    return Arrays.stream(originalMaps)
        .filter(isNotMaskedField)
        .toArray(Map[]::new); // 等价于 toArray(new Map[0])
  }

  /**
   * 组装获取列索引
   *
   * @param columnPage default 1
   * @param columnPageSize
   * @return
   */
  private int[] genColumnIndices(Integer columnPage, Integer columnPageSize) {
    int[] indexArray = new int[columnPageSize];
    for (int i = 0; i < columnPageSize; i++) {
      indexArray[i] = (columnPage - 1) * columnPageSize + i;
    }
    return indexArray;
  }

  /**
   * @param req
   * @param json
   * @return
   * @throws IOException
   */
  @ApiOperation(value = "saveScript", notes = "save script", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = true, dataType = "String", value = "path"),
    @ApiImplicitParam(name = "scriptContent", dataType = "String"),
    @ApiImplicitParam(name = "params", required = false, dataType = "Object", value = "params"),
    @ApiImplicitParam(name = "charset", required = false, dataType = "String", value = "charset")
  })
  @ApiOperationSupport(ignoreParameters = {"json"})
  @RequestMapping(path = "/saveScript", method = RequestMethod.POST)
  public Message saveScript(HttpServletRequest req, @RequestBody Map<String, Object> json)
      throws IOException, WorkSpaceException {
    String path = (String) json.get("path");
    String userName = ModuleUserUtils.getOperationUser(req, "saveScript " + path);
    LoggerUtils.setJobIdMDC("saveScriptThread_" + userName);
    LOGGER.info("userName {} start to saveScript File {}", userName, path);
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String charset = (String) json.get("charset");
    if (StringUtils.isEmpty(charset)) {
      charset = Consts.UTF_8.toString();
    }
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    String scriptContent = (String) json.get("scriptContent");
    Object params = json.get("params");
    Map<String, Object> map = (Map<String, Object>) params;
    Variable[] v = VariableParser.getVariables(map);
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    if (!fileSystem.exists(fsPath)) {
      throw WorkspaceExceptionManager.createException(80013, path);
    }
    if (!fileSystem.canWrite(fsPath)) {
      throw WorkspaceExceptionManager.createException(80014);
    }
    try (ScriptFsWriter scriptFsWriter =
        ScriptFsWriter.getScriptFsWriter(fsPath, charset, fileSystem.write(fsPath, true)); ) {
      scriptFsWriter.addMetaData(new ScriptMetaData(v));
      String[] split = scriptContent.split("\\n");
      for (int i = 0; i < split.length; i++) {
        if ("".equals(split[i]) || i != split.length - 1) {
          split[i] += "\n";
        }
        scriptFsWriter.addRecord(new ScriptRecord(split[i]));
      }
      LOGGER.info("userName {} Finished to saveScript File {}", userName, path);
      LoggerUtils.removeJobIdMDC();

      return Message.ok();
    }
  }

  @ApiOperation(value = "resultsetToExcel", notes = "resultset to excel", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "path"),
    @ApiImplicitParam(
        name = "charset",
        required = true,
        dataType = "String",
        defaultValue = "utf-8"),
    @ApiImplicitParam(
        name = "outputFileType",
        required = true,
        dataType = "String",
        defaultValue = "csv"),
    @ApiImplicitParam(
        name = "csvSeperator",
        required = true,
        dataType = "String",
        defaultValue = ","),
    @ApiImplicitParam(name = "quoteRetouchEnable", dataType = "boolean"),
    @ApiImplicitParam(
        name = "outputFileName",
        required = true,
        dataType = "String",
        defaultValue = "downloadResultset"),
    @ApiImplicitParam(
        name = "sheetName",
        required = true,
        dataType = "String",
        defaultValue = "result"),
    @ApiImplicitParam(
        name = "nullValue",
        required = true,
        dataType = "String",
        defaultValue = "NULL"),
    @ApiImplicitParam(name = "limit", required = true, dataType = "Integer", defaultValue = "0"),
    @ApiImplicitParam(name = "autoFormat", dataType = "Boolean")
  })
  @RequestMapping(path = "resultsetToExcel", method = RequestMethod.GET)
  public void resultsetToExcel(
      HttpServletRequest req,
      HttpServletResponse response,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "charset", defaultValue = "utf-8") String charset,
      @RequestParam(value = "outputFileType", defaultValue = "csv") String outputFileType,
      @RequestParam(value = "csvSeperator", defaultValue = ",") String csvSeperator,
      @RequestParam(value = "csvSeparator", defaultValue = ",") String csvSeparator,
      @RequestParam(value = "quoteRetouchEnable", required = false) boolean quoteRetouchEnable,
      @RequestParam(value = "outputFileName", defaultValue = "downloadResultset")
          String outputFileName,
      @RequestParam(value = "sheetName", defaultValue = "result") String sheetName,
      @RequestParam(value = "nullValue", defaultValue = "NULL") String nullValue,
      @RequestParam(value = "limit", defaultValue = "0") Integer limit,
      @RequestParam(value = "autoFormat", defaultValue = "false") Boolean autoFormat,
      @RequestParam(value = "keepNewline", defaultValue = "false") Boolean keepNewline)
      throws WorkSpaceException, IOException {
    ServletOutputStream outputStream = null;
    FsWriter fsWriter = null;
    PrintWriter writer = null;
    FileSource fileSource = null;
    if (csvSeparator.equals(",") && !csvSeperator.equals(",")) {
      csvSeparator = csvSeperator;
    }
    LOGGER.info(
        "resultsetToExcel with outputFileType:{}, csvSeparator:{}, quoteRetouchEnable:{}, charset:{}",
        outputFileType,
        csvSeparator,
        quoteRetouchEnable,
        charset);
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "resultsetToExcel " + path);
      LoggerUtils.setJobIdMDC("resultsetToExcelThread_" + userName);
      LOGGER.info("userName {} start to resultsetToExcel File {}", userName, path);
      FsPath fsPath = new FsPath(path);
      FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
      boolean isLimitDownloadSize = RESULT_SET_DOWNLOAD_IS_LIMIT.getValue();
      Integer csvDownloadSize = RESULT_SET_DOWNLOAD_MAX_SIZE_CSV.getValue();
      Integer excelDownloadSize = RESULT_SET_DOWNLOAD_MAX_SIZE_EXCEL.getValue();
      if (limit > 0) {
        csvDownloadSize = limit;
        excelDownloadSize = limit;
      }

      if (StringUtils.isEmpty(path)) {
        throw WorkspaceExceptionManager.createException(80004, path);
      }
      if (!checkIsUsersDirectory(path, userName)) {
        throw WorkspaceExceptionManager.createException(80010, userName, path);
      }
      response.addHeader(
          "Content-Disposition",
          "attachment;filename="
              + new String(outputFileName.getBytes("UTF-8"), "ISO8859-1")
              + "."
              + outputFileType);
      response.setCharacterEncoding(charset);
      outputStream = response.getOutputStream();
      // 前台传""会自动转为null
      if (nullValue != null && BLANK.equalsIgnoreCase(nullValue)) nullValue = "";
      fileSource = FileSource$.MODULE$.create(fsPath, fileSystem).addParams("nullValue", nullValue);
      switch (outputFileType) {
        case "csv":
          if (FileSource$.MODULE$.isTableResultSet(fileSource)) {
            fsWriter =
                CSVFsWriter.getCSVFSWriter(
                    charset, csvSeparator, quoteRetouchEnable, outputStream, keepNewline);
          } else {
            fsWriter =
                ScriptFsWriter.getScriptFsWriter(new FsPath(outputFileType), charset, outputStream);
          }
          response.addHeader("Content-Type", "text/plain");
          if (isLimitDownloadSize) {
            fileSource = fileSource.page(1, csvDownloadSize);
          }
          break;
        case "xlsx":
          if (!FileSource$.MODULE$.isTableResultSet(fileSource)) {
            throw WorkspaceExceptionManager.createException(80024);
          }
          fsWriter =
              ExcelFsWriter.getExcelFsWriter(
                  charset, sheetName, DEFAULT_DATE_TYPE, outputStream, autoFormat);
          response.addHeader("Content-Type", XLSX_RESPONSE_CONTENT_TYPE);
          if (isLimitDownloadSize) {
            fileSource = fileSource.page(1, excelDownloadSize);
          }
          break;
        default:
          throw WorkspaceExceptionManager.createException(80015);
      }
      fileSource.write(fsWriter);
      fsWriter.flush();
      LOGGER.info("userName {} Finished to resultsetToExcel File {}", userName, path);
    } catch (Exception e) {
      LOGGER.error("output failed", e);
      response.reset();
      response.setCharacterEncoding(Consts.UTF_8.toString());
      response.setContentType("text/plain; charset=utf-8");
      writer = response.getWriter();
      writer.append("error(错误):" + e.getMessage());
      writer.flush();
    } finally {
      LoggerUtils.removeJobIdMDC();
      if (outputStream != null) {
        outputStream.flush();
      }
      IOUtils.closeQuietly(fsWriter);
      IOUtils.closeQuietly(fileSource);
      IOUtils.closeQuietly(writer);
    }
  }

  @ApiOperation(
      value = "resultsetsToExcel",
      notes = "resultsets to excel",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "path"),
    @ApiImplicitParam(
        name = "outputFileName",
        required = true,
        dataType = "String",
        defaultValue = "downloadResultset"),
    @ApiImplicitParam(
        name = "nullValue",
        required = true,
        dataType = "String",
        defaultValue = "NULL"),
    @ApiImplicitParam(name = "limit", required = true, dataType = "Integer", defaultValue = "0"),
    @ApiImplicitParam(name = "autoFormat", dataType = "Boolean")
  })
  @RequestMapping(path = "resultsetsToExcel", method = RequestMethod.GET)
  public void resultsetsToExcel(
      HttpServletRequest req,
      HttpServletResponse response,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "outputFileName", defaultValue = "downloadResultset")
          String outputFileName,
      @RequestParam(value = "nullValue", defaultValue = "NULL") String nullValue,
      @RequestParam(value = "limit", defaultValue = "0") Integer limit,
      @RequestParam(value = "autoFormat", defaultValue = "false") Boolean autoFormat)
      throws WorkSpaceException, IOException {
    ServletOutputStream outputStream = null;
    FsWriter fsWriter = null;
    PrintWriter writer = null;
    FileSource fileSource = null;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "resultsetsToExcel " + path);
      LoggerUtils.setJobIdMDC("resultsetsToExcelThread_" + userName);
      LOGGER.info("userName {} start to resultsetsToExcel File {}", userName, path);
      FsPath fsPath = new FsPath(path);
      FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
      if (StringUtils.isEmpty(path)) {
        throw WorkspaceExceptionManager.createException(80004, path);
      }
      if (!checkIsUsersDirectory(path, userName)) {
        throw WorkspaceExceptionManager.createException(80010, userName, path);
      }
      // list目录下的文件
      FsPathListWithError fsPathListWithError = fileSystem.listPathWithError(fsPath);
      if (fsPathListWithError == null) {
        throw WorkspaceExceptionManager.createException(80029);
      }

      List<FsPath> fsPathList = fsPathListWithError.getFsPaths();
      // sort asc by _num.dolphin of num
      ResultSetUtils.sortByNameNum(fsPathList);
      FsPath[] fsPaths = fsPathList.toArray(new FsPath[] {});

      boolean isLimitDownloadSize = RESULT_SET_DOWNLOAD_IS_LIMIT.getValue();
      Integer excelDownloadSize = RESULT_SET_DOWNLOAD_MAX_SIZE_EXCEL.getValue();
      if (limit > 0) {
        excelDownloadSize = limit;
      }
      response.addHeader(
          "Content-Disposition",
          "attachment;filename="
              + new String(outputFileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1")
              + ".xlsx");
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      outputStream = response.getOutputStream();
      // 前台传""会自动转为null
      if (nullValue != null && BLANK.equalsIgnoreCase(nullValue)) {
        nullValue = "";
      }
      fileSource =
          FileSource$.MODULE$.create(fsPaths, fileSystem).addParams("nullValue", nullValue);
      if (!FileSource$.MODULE$.isTableResultSet(fileSource)) {
        throw WorkspaceExceptionManager.createException(80024);
      }
      fsWriter = new StorageMultiExcelWriter(outputStream, autoFormat);
      response.addHeader("Content-Type", XLSX_RESPONSE_CONTENT_TYPE);
      if (isLimitDownloadSize) {
        fileSource = fileSource.page(1, excelDownloadSize);
      }
      fileSource.write(fsWriter);
      fsWriter.flush();
      LOGGER.info("userName {} Finished to resultsetsToExcel File {}", userName, path);
    } catch (Exception e) {
      LOGGER.error("output failed", e);
      response.reset();
      response.setCharacterEncoding(Consts.UTF_8.toString());
      response.setContentType("text/plain; charset=utf-8");
      writer = response.getWriter();
      writer.append("error(错误):" + e.getMessage());
      writer.flush();
    } finally {
      LoggerUtils.removeJobIdMDC();
      if (outputStream != null) {
        outputStream.flush();
      }
      IOUtils.closeQuietly(fsWriter);
      IOUtils.closeQuietly(fileSource);
      IOUtils.closeQuietly(writer);
    }
  }

  @ApiOperation(value = "formate", notes = "formate", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "Path"),
    @ApiImplicitParam(
        name = "encoding",
        required = true,
        dataType = "String",
        defaultValue = "utf-8"),
    @ApiImplicitParam(
        name = "fieldDelimiter",
        required = true,
        dataType = "String",
        defaultValue = ","),
    @ApiImplicitParam(
        name = "hasHeader",
        required = true,
        defaultValue = "false",
        dataType = "Boolean"),
    @ApiImplicitParam(name = "quote", required = true, dataType = "String", defaultValue = "\""),
    @ApiImplicitParam(
        name = "escapeQuotes",
        required = true,
        dataType = "Boolean",
        defaultValue = "false")
  })
  @RequestMapping(path = "formate", method = RequestMethod.GET)
  public Message formate(
      HttpServletRequest req,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "encoding", defaultValue = "utf-8") String encoding,
      @RequestParam(value = "fieldDelimiter", defaultValue = ",") String fieldDelimiter,
      @RequestParam(value = "hasHeader", defaultValue = "false") Boolean hasHeader,
      @RequestParam(value = "quote", defaultValue = "\"") String quote,
      @RequestParam(value = "escapeQuotes", defaultValue = "false") Boolean escapeQuotes)
      throws Exception {
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String userName = ModuleUserUtils.getOperationUser(req, "formate " + path);
    LoggerUtils.setJobIdMDC("formateThread_" + userName);
    LOGGER.info("userName {} start to formate File {}", userName, path);
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    String suffix = path.substring(path.lastIndexOf("."));
    FsPath fsPath = new FsPath(path);
    Map<String, Object> res = new HashMap<String, Object>();
    FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
    try (InputStream in = fileSystem.read(fsPath)) {
      if (".xlsx".equalsIgnoreCase(suffix) || ".xls".equalsIgnoreCase(suffix)) {
        List<List<String>> info;
        info = ExcelStorageReader.getExcelTitle(in, null, hasHeader, suffix);
        res.put("columnName", info.get(1));
        res.put("columnType", info.get(2));
        res.put("sheetName", info.get(0));
      } else {
        String[][] column = null;
        // fix csv file with utf-8 with bom chart[&#xFEFF]
        BOMInputStream bomIn = new BOMInputStream(in, false); // don't include the BOM
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(bomIn, encoding))) {
          String header = reader.readLine();
          if (StringUtils.isEmpty(header)) {
            throw WorkspaceExceptionManager.createException(80016);
          }
          String[] line = header.split(fieldDelimiter, -1);
          int colNum = line.length;
          column = new String[2][colNum];
          if (hasHeader) {
            for (int i = 0; i < colNum; i++) {
              column[0][i] = line[i];
              if (escapeQuotes) {
                try {
                  column[0][i] = column[0][i].substring(1, column[0][i].length() - 1);
                } catch (StringIndexOutOfBoundsException e) {
                  throw WorkspaceExceptionManager.createException(80017);
                }
              }
              column[1][i] = "string";
            }
          } else {
            for (int i = 0; i < colNum; i++) {
              column[0][i] = "col_" + (i + 1);
              column[1][i] = "string";
            }
          }
        }
        res.put("columnName", column[0]);
        res.put("columnType", column[1]);
        LOGGER.info("userName {} Finished to formate File {}", userName, path);
        LoggerUtils.removeJobIdMDC();
      }
      return Message.ok().data("formate", res);
    }
  }

  @ApiOperation(value = "getSheetInfo", notes = "getSheetInfo", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "Path"),
    @ApiImplicitParam(
        name = "encoding",
        required = true,
        dataType = "String",
        defaultValue = "utf-8"),
    @ApiImplicitParam(
        name = "fieldDelimiter",
        required = true,
        dataType = "String",
        defaultValue = ","),
    @ApiImplicitParam(
        name = "hasHeader",
        required = true,
        defaultValue = "false",
        dataType = "Boolean"),
    @ApiImplicitParam(name = "quote", required = true, dataType = "String", defaultValue = "\""),
    @ApiImplicitParam(
        name = "escapeQuotes",
        required = true,
        dataType = "Boolean",
        defaultValue = "false")
  })
  @RequestMapping(path = "getSheetInfo", method = RequestMethod.GET)
  public Message getSheetInfo(
      HttpServletRequest req,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "encoding", defaultValue = "utf-8") String encoding,
      @RequestParam(value = "fieldDelimiter", defaultValue = ",") String fieldDelimiter,
      @RequestParam(value = "hasHeader", defaultValue = "false") Boolean hasHeader,
      @RequestParam(value = "quote", defaultValue = "\"") String quote,
      @RequestParam(value = "escapeQuotes", defaultValue = "false") Boolean escapeQuotes)
      throws Exception {
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String userName = ModuleUserUtils.getOperationUser(req, "getSheetInfo " + path);
    LoggerUtils.setJobIdMDC("getSheetInfoThread_" + userName);
    LOGGER.info("userName {} start to getSheetInfo File {}", userName, path);
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    String suffix = path.substring(path.lastIndexOf("."));
    FsPath fsPath = new FsPath(path);
    Map<String, List<Map<String, String>>> sheetInfo;
    FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
    try (InputStream in = fileSystem.read(fsPath)) {
      if (".xlsx".equalsIgnoreCase(suffix)) {
        sheetInfo = XlsxUtils.getAllSheetInfo(in, null, hasHeader);
      } else if (".xls".equalsIgnoreCase(suffix)) {
        sheetInfo = XlsUtils.getSheetsInfo(in, hasHeader);
      } else if (".csv".equalsIgnoreCase(suffix)) {
        List<Map<String, String>> csvMapList = new ArrayList<>();
        String[][] column = null;
        // fix csv file with utf-8 with bom chart[&#xFEFF]
        BOMInputStream bomIn = new BOMInputStream(in, false); // don't include the BOM
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(bomIn, encoding))) {
          String header = reader.readLine();
          if (StringUtils.isEmpty(header)) {
            throw WorkspaceExceptionManager.createException(80016);
          }
          String[] line = header.split(fieldDelimiter, -1);
          int colNum = line.length;
          column = new String[2][colNum];
          if (hasHeader) {
            for (int i = 0; i < colNum; i++) {
              HashMap<String, String> csvMap = new HashMap<>();
              column[0][i] = line[i];
              if (escapeQuotes) {
                try {
                  csvMap.put(column[0][i].substring(1, column[0][i].length() - 1), "string");
                } catch (StringIndexOutOfBoundsException e) {
                  throw WorkspaceExceptionManager.createException(80017);
                }
              } else {
                csvMap.put(column[0][i], "string");
              }
              csvMapList.add(csvMap);
            }
          } else {
            for (int i = 0; i < colNum; i++) {
              HashMap<String, String> csvMap = new HashMap<>();
              csvMap.put("col_" + (i + 1), "string");
              csvMapList.add(csvMap);
            }
          }
        }
        sheetInfo = new HashMap<>(1);
        sheetInfo.put("sheet_csv", csvMapList);
        LOGGER.info("userName {} Finished to getSheetInfo File {}", userName, path);
        LoggerUtils.removeJobIdMDC();
      } else {
        LoggerUtils.removeJobIdMDC();
        throw WorkspaceExceptionManager.createException(80004, path);
      }
      return Message.ok().data("sheetInfo", sheetInfo);
    }
  }

  @ApiOperation(value = "openLog", notes = "open log", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "path"),
    @ApiImplicitParam(name = "proxyUser", dataType = "String")
  })
  @RequestMapping(path = "/openLog", method = RequestMethod.GET)
  public Message openLog(
      HttpServletRequest req,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "proxyUser", required = false) String proxyUser)
      throws IOException, WorkSpaceException {
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String userName = ModuleUserUtils.getOperationUser(req, "openLog " + path);
    LoggerUtils.setJobIdMDC("openLogThread_" + userName);
    LOGGER.info("userName {} start to openLog File {}", userName, path);
    if (proxyUser != null && Configuration.isJobHistoryAdmin(userName)) {
      userName = proxyUser;
    }
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystemForRead(userName, fsPath);
    if (!fileSystem.canRead(fsPath)) {
      throw WorkspaceExceptionManager.createException(80018);
    }
    if (fileSystem.getLength(fsPath)
        > ByteTimeUtils.byteStringAsBytes(FILESYSTEM_FILE_CHECK_SIZE.getValue())) {
      throw WorkspaceExceptionManager.createException(80033, path);
    }
    try (FileSource fileSource =
        FileSource$.MODULE$.create(fsPath, fileSystem).addParams("ifMerge", "false")) {
      Pair<Object, ArrayList<String[]>> collect = fileSource.collect()[0];
      StringBuilder[] log =
          Arrays.stream(new StringBuilder[4])
              .map(f -> new StringBuilder())
              .toArray(StringBuilder[]::new);
      ArrayList<String[]> snd = collect.getSecond();
      LogLevel start = new LogLevel(LogLevel.Type.ALL);
      snd.stream()
          .map(f -> f[0])
          .forEach(
              s -> WorkspaceUtil.logMatch(s, start).forEach(i -> log[i].append(s).append("\n")));
      LOGGER.info("userName {} Finished to openLog File {}", userName, path);
      LoggerUtils.removeJobIdMDC();
      return Message.ok()
          .data("log", Arrays.stream(log).map(StringBuilder::toString).toArray(String[]::new));
    }
  }

  private static void deleteAllFiles(FileSystem fileSystem, FsPath fsPath) throws IOException {
    fileSystem.delete(fsPath);
    List<FsPath> list = null;
    if (fileSystem.exists(fsPath)) {
      list = fileSystem.list(fsPath);
    }
    if (list == null) {
      return;
    }
    for (FsPath path : list) {
      deleteAllFiles(fileSystem, path);
    }
    fileSystem.delete(fsPath);
  }

  @ApiOperation(value = "chmod", notes = "file permission chmod", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "filepath", required = true, dataType = "String", value = "filepath"),
    @ApiImplicitParam(
        name = "isRecursion",
        required = false,
        dataType = "String",
        value = "isRecursion"),
    @ApiImplicitParam(
        name = "filePermission",
        required = true,
        dataType = "String",
        value = "filePermission"),
  })
  @RequestMapping(path = "/chmod", method = RequestMethod.GET)
  public Message chmod(
      HttpServletRequest req,
      @RequestParam(value = "filepath", required = true) String filePath,
      @RequestParam(value = "isRecursion", required = false, defaultValue = "true")
          Boolean isRecursion,
      @RequestParam(value = "filePermission", required = true) String filePermission)
      throws WorkSpaceException, IOException {
    String userName = ModuleUserUtils.getOperationUser(req, "chmod " + filePath);
    if (StringUtils.isEmpty(filePath)) {
      return Message.error(MessageFormat.format(PARAMETER_NOT_BLANK, filePath));
    }
    if (StringUtils.isEmpty(filePermission)) {
      return Message.error(MessageFormat.format(PARAMETER_NOT_BLANK, filePermission));
    }
    if (!filePath.startsWith("file://") && !filePath.startsWith("hdfs://")) {
      filePath = "file://" + filePath;
    }
    if (!checkIsUsersDirectory(filePath, userName, false)) {
      return Message.error(MessageFormat.format(FILEPATH_ILLEGALITY, filePath));
    } else {
      // Prohibit users from modifying their own unreadable content
      if (FilesystemUtils.checkFilePermissions(filePermission)) {
        FileSystem fileSystem = fsService.getFileSystem(userName, new FsPath(filePath));
        Stack<FsPath> dirsToChmod = new Stack<>();
        dirsToChmod.push(new FsPath(filePath));
        if (isRecursion) {
          FilesystemUtils.traverseFolder(new FsPath(filePath), fileSystem, dirsToChmod);
        }
        while (!dirsToChmod.empty()) {
          fileSystem.setPermission(dirsToChmod.pop(), filePermission);
        }
        return Message.ok();
      } else {
        return Message.error(MessageFormat.format(FILE_PERMISSION_ERROR, filePermission));
      }
    }
  }

  @ApiOperation(value = "encrypt-path", notes = "encrypt file path", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "filePath", required = true, dataType = "String", value = "Path")
  })
  @RequestMapping(path = "/encrypt-path", method = RequestMethod.GET)
  public Message encryptPath(
      HttpServletRequest req, @RequestParam(value = "filePath", required = false) String filePath)
      throws WorkSpaceException, IOException {
    String username = ModuleUserUtils.getOperationUser(req, "encrypt-path " + filePath);
    if (StringUtils.isEmpty(filePath)) {
      return Message.error(MessageFormat.format(PARAMETER_NOT_BLANK, "filePath"));
    }
    if (!WorkspaceUtil.filePathRegexPattern.matcher(filePath).find()) {
      return Message.error(MessageFormat.format(FILEPATH_ILLEGAL_SYMBOLS, filePath));
    }
    FileSystem fs = fsService.getFileSystem(username, new FsPath(filePath));
    String fileMD5Str = fs.getChecksumWithMD5(new FsPath(filePath));
    return Message.ok().data("data", fileMD5Str);
  }

  @ApiOperation(value = "check-hdfs-files", notes = "encrypt file path", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "filePath", required = true, dataType = "String", value = "Path")
  })
  @RequestMapping(path = "/check-hdfs-files", method = RequestMethod.GET)
  public Message checkHdfsFiles(
      HttpServletRequest req, @RequestParam(value = "filePath", required = false) String filePath)
      throws WorkSpaceException, IOException {
    String username = ModuleUserUtils.getOperationUser(req, "check-hdfs-files " + filePath);
    if (StringUtils.isEmpty(filePath)) {
      return Message.error(MessageFormat.format(PARAMETER_NOT_BLANK, "filePath"));
    }
    if (!WorkspaceUtil.filePathRegexPattern.matcher(filePath).find()) {
      return Message.error(MessageFormat.format(FILEPATH_ILLEGAL_SYMBOLS, filePath));
    }

    if (!WorkspaceUtil.hiveFilePathRegexPattern.matcher(filePath).find()) {
      return Message.error(MessageFormat.format(HIVE_FILEPATH_ILLEGAL_SYMBOLS, filePath));
    }

    FsPath fsPath = new FsPath(filePath);
    FileSystem fs = fsService.getFileSystem(username, fsPath);
    if (!fs.exists(fsPath)) {
      return Message.error(MessageFormat.format(FILEPATH_ILLEGALITY, filePath));
    }
    List<Map<String, Object>> resultMap = new ArrayList<>();
    List<FsPath> list = fs.getAllFilePaths(fsPath);
    if (CollectionUtils.isNotEmpty(list)) {
      List<CompletableFuture<Void>> futures =
          list.stream()
              .map(
                  path ->
                      CompletableFuture.runAsync(
                          () -> {
                            try {
                              Map<String, Object> dataMap = new HashMap<>();
                              dataMap.put("checkSum", fs.getChecksum(path));
                              dataMap.put("blockSize", fs.getBlockSize(path));
                              dataMap.put("path", path.getPath());
                              synchronized (resultMap) {
                                resultMap.add(dataMap);
                              }
                            } catch (IOException e) {
                              LOGGER.error(e.getMessage(), e);
                              throw new RuntimeException(e);
                            }
                          },
                          FilesystemUtils.executorService))
              .collect(Collectors.toList());

      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
    return Message.ok().data("fileDataList", resultMap);
  }

  @ApiOperation(
      value = "copy-keytab-files",
      notes = "copy keytab file from hdfs",
      response = Message.class)
  @RequestMapping(path = "/copy-keytab-files", method = RequestMethod.GET)
  public Message copyAndEncryptKeytab(HttpServletRequest req, String user) {
    final String owner = LINKIS_KEYTAB_FILE_OWNER.getValue();
    final String group = LINKIS_KEYTAB_FILE_OWNER.getValue();
    final String permission = LINKIS_KEYTAB_FILE_PEIMISSION.getValue();
    // 1. 获取用户信息并校验功能开关
    String username = ModuleUserUtils.getOperationUser(req, "copy-and-encrypt-keytab");
    if (!Configuration.LINKIS_KEYTAB_SWITCH()) {
      logger.info("Keytab copy feature is disabled for user: {}", username);
      return Message.ok("Keytab copy feature is disabled");
    }
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    try {
      // 2. 初始化路径和文件系统
      String sourcePath = HadoopConf.KEYTAB_FILE().getValue();
      String targetPath = HadoopConf.LINKIS_KEYTAB_FILE().getValue();
      FsPath hdfsKeytabPath = new FsPath(sourcePath);
      FsPath linkisKeytabPath = new FsPath(targetPath);
      FileSystem fs = fsService.getFileSystem(username, hdfsKeytabPath);
      // 3. 验证源路径
      if (!fs.exists(hdfsKeytabPath)) {
        String errorMsg = String.format("Source path does not exist: %s", sourcePath);
        logger.error(errorMsg);
        return Message.error(errorMsg);
      }
      // 4. 确保目标目录存在
      if (!fs.exists(linkisKeytabPath)) {
        logger.info("Creating target directory: {}", targetPath);
        fs.mkdirs(linkisKeytabPath);
        // 设置目标目录权限
        fs.setOwner(linkisKeytabPath, owner);
        fs.setPermission(linkisKeytabPath, permission);
        fs.setGroup(linkisKeytabPath, group);
      }
      // 5. 处理每个keytab文件
      List<FsPath> sourceFiles = fs.list(hdfsKeytabPath);
      if (StringUtils.isNotBlank(user)) {
        sourceFiles =
            sourceFiles.stream()
                .filter(fsPath -> fsPath.getPath().endsWith(user + HDFSUtils.KEYTAB_SUFFIX()))
                .collect(Collectors.toList());
      }
      if (sourceFiles.isEmpty()) {
        logger.warn("No keytab files found in source directory: {}", sourcePath);
        return Message.ok("No keytab files to copy");
      }
      int successCount = 0;
      for (FsPath sourceFile : sourceFiles) {
        try {
          String fileName = sourceFile.getPath().replace(sourcePath, targetPath);
          FsPath targetFile = new FsPath(fileName);
          // 读取源文件内容
          byte[] keyTabFileByte = IOUtils.toByteArray(fs.read(sourceFile));
          // 加密内容
          String encryptedContent = AESUtils.encrypt(keyTabFileByte, AESUtils.PASSWORD);
          // 写入目标文件
          try (OutputStream out = new BufferedOutputStream(fs.write(targetFile, true))) {
            out.write(encryptedContent.getBytes(Configuration.BDP_ENCODING().getValue()));
          }
          // 设置文件权限
          fs.setOwner(targetFile, owner);
          fs.setPermission(targetFile, permission);
          fs.setGroup(targetFile, group);
          successCount++;
          logger.debug("Successfully processed keytab file: {}", fileName);
        } catch (Exception e) {
          logger.error("Failed to process keytab file: {}", sourceFile.getPath(), e);
          // 继续处理下一个文件
        }
      }
      // 6. 返回处理结果
      if (successCount == sourceFiles.size()) {
        logger.info("Successfully processed all {} keytab files", successCount);
        return Message.ok(String.format("Successfully processed %d keytab files", successCount));
      } else {
        String msg =
            String.format(
                "Processed %d of %d keytab files, some failed", successCount, sourceFiles.size());
        logger.warn(msg);
        return Message.warn(msg);
      }
    } catch (Exception e) {
      String errorMsg = "Failed to copy and encrypt keytab files";
      logger.error(errorMsg, e);
      return Message.error(errorMsg + ": " + e.getMessage());
    }
  }
}
