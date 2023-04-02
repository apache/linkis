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
import org.apache.linkis.filesystem.conf.WorkSpaceConfiguration;
import org.apache.linkis.filesystem.entity.DirFileTree;
import org.apache.linkis.filesystem.entity.LogLevel;
import org.apache.linkis.filesystem.exception.WorkSpaceException;
import org.apache.linkis.filesystem.exception.WorkspaceExceptionManager;
import org.apache.linkis.filesystem.service.FsService;
import org.apache.linkis.filesystem.util.WorkspaceUtil;
import org.apache.linkis.filesystem.utils.UserGroupUtils;
import org.apache.linkis.filesystem.validator.PathValidator$;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.apache.linkis.storage.csv.CSVFsWriter;
import org.apache.linkis.storage.domain.FsPathListWithError;
import org.apache.linkis.storage.excel.ExcelFsWriter;
import org.apache.linkis.storage.excel.ExcelStorageReader;
import org.apache.linkis.storage.excel.StorageMultiExcelWriter;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.script.*;
import org.apache.linkis.storage.source.FileSource;
import org.apache.linkis.storage.source.FileSource$;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.http.Consts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
  private boolean checkIsUsersDirectory(String requestPath, String userName) {
    boolean ownerCheck = WorkSpaceConfiguration.FILESYSTEM_PATH_CHECK_OWNER.getValue();
    if (!ownerCheck) {
      LOGGER.debug("not check filesystem owner.");
      return true;
    }
    if (requestPath.contains(WorkspaceUtil.suffixTuning(HDFS_USER_ROOT_PATH_PREFIX.getValue()))
        || Configuration.isAdmin(userName)) {
      return true;
    }
    requestPath = requestPath.toLowerCase().trim() + "/";
    String hdfsUserRootPathPrefix =
        WorkspaceUtil.suffixTuning(HDFS_USER_ROOT_PATH_PREFIX.getValue());
    String hdfsUserRootPathSuffix = HDFS_USER_ROOT_PATH_SUFFIX.getValue();
    String localUserRootPath = WorkspaceUtil.suffixTuning(LOCAL_USER_ROOT_PATH.getValue());

    String workspacePath = hdfsUserRootPathPrefix + userName + hdfsUserRootPathSuffix;
    String enginconnPath = localUserRootPath + userName;
    if (Configuration.isJobHistoryAdmin(userName)) {
      workspacePath = hdfsUserRootPathPrefix;
      enginconnPath = localUserRootPath;
    }
    LOGGER.debug("requestPath:" + requestPath);
    LOGGER.debug("workspacePath:" + workspacePath);
    LOGGER.debug("enginconnPath:" + enginconnPath);
    LOGGER.debug("adminUser:" + String.join(",", Configuration.getJobHistoryAdmin()));
    return (requestPath.contains(workspacePath)) || (requestPath.contains(enginconnPath));
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
    if (!checkIsUsersDirectory(newDest, userName)) {
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
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
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
    FsPathListWithError fsPathListWithError = fileSystem.listPathWithError(fsPath);
    if (fsPathListWithError != null) {
      for (FsPath children : fsPathListWithError.getFsPaths()) {
        DirFileTree dirFileTreeChildren = new DirFileTree();
        dirFileTreeChildren.setName(new File(children.getPath()).getName());
        dirFileTreeChildren.setPath(fsPath.getFsType() + "://" + children.getPath());
        dirFileTreeChildren.setProperties(new HashMap<>());
        dirFileTreeChildren.setParentPath(fsPath.getSchemaPath());
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
      FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
      if (!fileSystem.exists(fsPath)) {
        throw WorkspaceExceptionManager.createException(80011, path);
      }
      inputStream = fileSystem.read(fsPath);
      byte[] buffer = new byte[1024];
      int bytesRead = 0;
      response.setCharacterEncoding(charset);
      java.nio.file.Path source = Paths.get(fsPath.getPath());
      response.addHeader("Content-Type", Files.probeContentType(source));
      response.addHeader("Content-Disposition", "attachment;filename=" + new File(path).getName());
      outputStream = response.getOutputStream();
      while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    } catch (Exception e) {
      LOGGER.error("download failed", e);
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
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
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

  @ApiOperation(value = "openFile", notes = "open file", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "Path"),
    @ApiImplicitParam(name = "page", required = true, dataType = "Integer", defaultValue = "1"),
    @ApiImplicitParam(
        name = "pageSize",
        required = true,
        dataType = "Integer",
        defaultValue = "5000"),
    @ApiImplicitParam(
        name = "charset",
        required = true,
        dataType = "String",
        defaultValue = "utf-8")
  })
  @RequestMapping(path = "/openFile", method = RequestMethod.GET)
  public Message openFile(
      HttpServletRequest req,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "pageSize", defaultValue = "5000") Integer pageSize,
      @RequestParam(value = "charset", defaultValue = "utf-8") String charset)
      throws IOException, WorkSpaceException {

    Message message = Message.ok();
    if (StringUtils.isEmpty(path)) {
      throw WorkspaceExceptionManager.createException(80004, path);
    }
    String userName = ModuleUserUtils.getOperationUser(req, "openFile " + path);
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    // Throws an exception if the file does not have read access(如果文件没读权限，抛出异常)
    if (!fileSystem.canRead(fsPath)) {
      throw WorkspaceExceptionManager.createException(80012);
    }
    FileSource fileSource = null;
    try {
      fileSource = FileSource$.MODULE$.create(fsPath, fileSystem);
      if (FileSource$.MODULE$.isResultSet(fsPath.getPath())) {
        fileSource = fileSource.page(page, pageSize);
      }
      Pair<Object, ArrayList<String[]>> result = fileSource.collect()[0];
      IOUtils.closeQuietly(fileSource);
      message.data("metadata", result.getFirst()).data("fileContent", result.getSecond());
      message.data("type", fileSource.getFileSplits()[0].type());
      message.data("totalLine", fileSource.getTotalLine());
      return message.data("page", page).data("totalPage", 0);
    } finally {
      IOUtils.closeQuietly(fileSource);
    }
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
      @RequestParam(value = "autoFormat", defaultValue = "false") Boolean autoFormat)
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
      FsPath fsPath = new FsPath(path);
      FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
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
                CSVFsWriter.getCSVFSWriter(charset, csvSeparator, quoteRetouchEnable, outputStream);
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
          WorkspaceExceptionManager.createException(80015);
      }
      fileSource.write(fsWriter);
      fsWriter.flush();
    } catch (Exception e) {
      LOGGER.error("output failed", e);
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
      FsPath fsPath = new FsPath(path);
      FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
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
      FsPath[] fsPaths = fsPathListWithError.getFsPaths().toArray(new FsPath[] {});
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
      if (nullValue != null && BLANK.equalsIgnoreCase(nullValue)) nullValue = "";
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
    } catch (Exception e) {
      LOGGER.error("output failed", e);
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
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    String suffix = path.substring(path.lastIndexOf("."));
    FsPath fsPath = new FsPath(path);
    Map<String, Object> res = new HashMap<String, Object>();
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    try (InputStream in = fileSystem.read(fsPath)) {
      if (".xlsx".equalsIgnoreCase(suffix) || ".xls".equalsIgnoreCase(suffix)) {
        List<List<String>> info;
        info = ExcelStorageReader.getExcelTitle(in, null, hasHeader, suffix);
        res.put("columnName", info.get(1));
        res.put("columnType", info.get(2));
        res.put("sheetName", info.get(0));
      } else {
        String[][] column = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
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
        res.put("columnName", column[0]);
        res.put("columnType", column[1]);
      }
      return Message.ok().data("formate", res);
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
    if (proxyUser != null && Configuration.isJobHistoryAdmin(userName)) {
      userName = proxyUser;
    }
    if (!checkIsUsersDirectory(path, userName)) {
      throw WorkspaceExceptionManager.createException(80010, userName, path);
    }
    FsPath fsPath = new FsPath(path);
    FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
    if (!fileSystem.canRead(fsPath)) {
      throw WorkspaceExceptionManager.createException(80018);
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
}
