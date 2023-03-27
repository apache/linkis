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

package org.apache.linkis.udf.service.impl;

import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.*;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.publicservice.common.lock.entity.CommonLock;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.udf.dao.UDFDao;
import org.apache.linkis.udf.dao.UDFTreeDao;
import org.apache.linkis.udf.dao.UDFVersionDao;
import org.apache.linkis.udf.entity.UDFInfo;
import org.apache.linkis.udf.entity.UDFManager;
import org.apache.linkis.udf.entity.UDFTree;
import org.apache.linkis.udf.entity.UDFVersion;
import org.apache.linkis.udf.excepiton.UDFException;
import org.apache.linkis.udf.service.UDFService;
import org.apache.linkis.udf.utils.ConstantVar;
import org.apache.linkis.udf.vo.*;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.udf.utils.ConstantVar.*;

@Service
public class UDFServiceImpl implements UDFService {

  private static final Logger logger = LoggerFactory.getLogger(UDFServiceImpl.class);

  private static final String _LOCK = "_UDF";

  private static final long LOCK_TIMEOUT = 5000L;

  private static final String USER_REGEX = "^[0-9a-zA-Z_]{1,}$";

  Pattern pattern = Pattern.compile(USER_REGEX);

  Map<String, Collection<Integer>> categoryToCodes = new HashedMap();

  {
    categoryToCodes.put(
        ConstantVar.FUNCTION,
        Lists.newArrayList(ConstantVar.FUNCTION_PY, ConstantVar.FUNCTION_SCALA));
    categoryToCodes.put(
        ConstantVar.UDF, Lists.newArrayList(UDF_JAR, ConstantVar.UDF_PY, ConstantVar.UDF_SCALA));
    categoryToCodes.put(
        ConstantVar.ALL,
        Lists.newArrayList(
            ConstantVar.FUNCTION_PY,
            ConstantVar.FUNCTION_SCALA,
            UDF_JAR,
            ConstantVar.UDF_PY,
            ConstantVar.UDF_SCALA));
  }

  @Autowired private UDFDao udfDao;

  @Autowired private UDFTreeDao udfTreeDao;

  @Autowired private UDFVersionDao udfVersionDao;

  @Autowired private CommonLockService commonLockService;

  private final BmlClient bmlClient = BmlClientFactory.createBmlClient();

  @Override
  @Transactional(rollbackFor = Exception.class)
  public long addUDF(UDFAddVo udfVo, String userName) throws Exception {
    logger.info(userName + " add udfVo: " + udfVo.getUdfName());
    // 锁同一用户
    CommonLock commonLock = new CommonLock();
    commonLock.setLockObject(userName + _LOCK);
    commonLock.setCreateTime(new Date());
    commonLock.setUpdateTime(new Date());
    try {
      commonLockService.lock(commonLock, LOCK_TIMEOUT);
      if (validateDuplicateUDFName(udfVo.getUdfName(), userName)) {
        throw new UDFException(
            "The name of udf is the same name. Please rename it and rebuild it.(udf的名字重名，请改名后重建)");
      }
      if (StringUtils.isEmpty(udfVo.getDirectory())) {
        throw new UDFException("分类名不能为空！");
      }
      // 支持hdfs path
      String path = udfVo.getPath();
      if (StringUtils.isBlank(path) || path.contains("../")) {
        throw new UDFException(
            "The path: "
                + path
                + " of udf is error. Please rename it and rebuild it.(udf的路径错误，请修改后重建)");
      }
      FsPath fsPath = new FsPath(path);
      //        FileSystem fileSystem = (FileSystem) FSFactory.getFs(fsPath.getFsType());
      FileSystem fileSystem = (FileSystem) FSFactory.getFsByProxyUser(fsPath, userName);
      if (udfVo.getUdfType() == UDF_JAR && StringUtils.isNotBlank(udfVo.getPath())) {
        validateJarFileName(
            udfVo.getPath().substring(udfVo.getPath().lastIndexOf("/") + 1), userName);
      }
      // todo inputStream拷贝
      InputStream is = null, is2 = null;
      String md5 = "";
      BmlUploadResponse response;
      try {
        fileSystem.init(null);
        is = fileSystem.read(fsPath);
        md5 = DigestUtils.md5DigestAsHex(is);
        logger.info("fsPath.path:" + fsPath.getPath());
        is2 = fileSystem.read(fsPath);
        response = uploadToBml(userName, fsPath.getPath(), is2);
        logger.info("bml resourceId:" + response.resourceId());
      } catch (IOException e) {
        throw new UDFException("文件读取异常：" + e.getMessage());
      } finally {
        IOUtils.closeQuietly(is2);
        IOUtils.closeQuietly(is);
        if (fileSystem != null) {
          fileSystem.close();
        }
      }
      if (!response.isSuccess()) {
        throw new UDFException("上传到bml失败，文件路径为：" + udfVo.getPath());
      }
      // 判断分类不存在则创建
      String category =
          udfVo.getUdfType() == FUNCTION_PY || udfVo.getUdfType() == FUNCTION_SCALA
              ? FUNCTION
              : UDF;
      UDFTree parentTree = getOrCreateTree(userName, category, udfVo.getDirectory());

      UDFInfo udfInfo = new UDFInfo();
      DateConverter converter = new DateConverter(new Date());
      BeanUtilsBean.getInstance().getConvertUtils().register(converter, Date.class);
      BeanUtils.copyProperties(udfInfo, udfVo);
      udfInfo.setId(null);
      udfInfo.setTreeId(parentTree.getId());
      udfInfo.setCreateTime(new Date());
      udfInfo.setUpdateTime(new Date());
      udfDao.addUDF(udfInfo);
      UDFVersion udfVersion = new UDFVersion();
      BeanUtils.copyProperties(udfVersion, udfVo);
      udfVersion.setUdfId(udfInfo.getId());
      udfVersion.setId(null);
      udfVersion.setPublished(false);
      udfVersion.setCreateTime(new Date());
      udfVersion.setBmlResourceId(response.resourceId());
      udfVersion.setBmlResourceVersion(response.version());
      udfVersion.setMd5(md5);
      udfVersionDao.addUdfVersion(udfVersion);
      if (udfVo.getLoad() != null && udfVo.getLoad()) {
        addLoadInfo(udfInfo.getId(), userName);
      }
      logger.info("end for " + userName + " add udfVo: " + udfVo.getUdfName());
      return udfInfo.getId();
    } finally {
      commonLockService.unlock(commonLock);
    }
  }

  private boolean validateDuplicateUDFName(String udfName, String userName) throws UDFException {
    long count = udfDao.getSameNameCountByUser(udfName, userName);
    long sharedCount = udfDao.getShareSameNameCountByUser(udfName, userName);
    if (count > 0 || sharedCount > 0) {
      return true;
    }
    return false;
  }

  private BmlUploadResponse uploadToBml(String userName, String filePath, InputStream inputStream) {
    BmlUploadResponse response =
        bmlClient.uploadResource(
            userName == null ? Utils.getJvmUser() : userName, filePath, inputStream);
    return response;
  }

  private BmlUpdateResponse uploadToBml(
      String userName, String filePath, InputStream inputStream, String resourceId) {
    BmlUpdateResponse response =
        bmlClient.updateResource(
            userName == null ? Utils.getJvmUser() : userName, resourceId, filePath, inputStream);
    return response;
  }

  private BmlDownloadResponse downloadBml(String userName, String resourceId, String version) {
    return bmlClient.downloadResource(
        userName == null ? Utils.getJvmUser() : userName, resourceId, version);
  }

  private void validateJarFileName(String jarPath, String userName) throws UDFException {
    int cnt = udfVersionDao.getSameJarCount(userName, jarPath);
    if (cnt > 0) {
      throw new UDFException("用户的udf已存在同名jar！");
    }
  }

  //    @Deprecated
  //    private void validateJarFile(UDFInfo udfInfo, String userName) throws UDFException {
  //        File targetFile = new File(UdfConfiguration.UDF_TMP_PATH().getValue() +
  // udfInfo.getPath());
  //        FsPath fsPath = new FsPath("file://" + udfInfo.getPath());
  //        Fs remoteFs = FSFactory.getFsByProxyUser(fsPath,userName);
  //        try{
  //            remoteFs.init(null);
  //            if(remoteFs.exists(fsPath)){
  //                InputStream remoteStream = remoteFs.read(fsPath);
  //                if(!targetFile.exists()){
  //                    targetFile.getParentFile().mkdirs();
  //                    targetFile.createNewFile();
  //                }
  //                Files.copy(remoteStream, targetFile.toPath(),
  // StandardCopyOption.REPLACE_EXISTING);
  //                IOUtils.closeQuietly(remoteStream);
  //            }
  //        }catch (IOException e){
  //            logger.error(e);
  //            throw new UDFException("Verify that there is a problem with the UDF jar
  // package(校验UDF jar包存在问题)：" + e.getMessage());
  //        }
  //
  //        this.getClass().getResource("").getFile();
  //        File hiveDependency = new File(UdfConfiguration.UDF_HIVE_EXEC_PATH().getValue());
  //        String udfClassName = StringUtils.substringBetween(udfInfo.getRegisterFormat(), "\"",
  // "\"");
  //        try{
  //            URL[] url = {new URL("file://" + targetFile.getAbsolutePath()), new URL("file://"
  // + hiveDependency.getAbsolutePath())};
  //            URLClassLoader loader = URLClassLoader.newInstance(url);
  //            Class clazz = loader.loadClass(udfClassName);
  //            Constructor constructor = clazz.getConstructor(new Class[0]);
  //            if(!Modifier.isPublic(constructor.getModifiers())) throw new
  // NoSuchMethodException();
  //        }catch (ClassNotFoundException cne){
  //            throw new UDFException("There is a problem verifying the UDF jar package: the
  // class is not found(校验UDF jar包存在问题：找不到类) " + cne.getMessage());
  //        } catch (NoSuchMethodException e) {
  //            throw new UDFException("There is a problem verifying the UDF jar package:
  // class(校验UDF jar包存在问题：类) " + udfClassName + " Missing public no-argument
  // constructor(缺少public的无参数构造方法)");
  //        }catch (Exception e){
  //            throw new UDFException("Verify that there is a problem with the UDF jar
  // package(校验UDF jar包存在问题)：" + e.getMessage());
  //        } finally {
  //            targetFile.delete();
  //        }
  //    }

  @Override
  @Transactional
  public void updateUDF(UDFUpdateVo udfUpdateVo, String userName) throws Exception {
    logger.info(userName + "start to update udfInfo, udfName:" + udfUpdateVo.getUdfName());
    if (udfUpdateVo.getId() == null) {
      throw new UDFException("id Can not be empty(不能为空)");
    }
    UDFInfo oldUdfInfo = udfDao.getUDFById(udfUpdateVo.getId());
    if (oldUdfInfo == null) {
      throw new UDFException("No old UDF found by this ID.");
    }
    if (!userName.equals(oldUdfInfo.getCreateUser())) {
      throw new UDFException(
          "Current user must be consistent with the modified user(当前用户必须和修改用户一致)");
    }
    if (!oldUdfInfo.getUdfType().equals(udfUpdateVo.getUdfType())) {
      throw new UDFException("UDF type modification is not allowed.");
    }
    if (!udfUpdateVo.getUdfName().equals(oldUdfInfo.getUdfName())) {
      throw new UDFException("The name of udf is not allowed modified.(udf的名字禁止修改！)");
    }
    // 不考虑jar共享
    if (udfUpdateVo.getUdfType() == UDF_JAR && StringUtils.isNotBlank(udfUpdateVo.getPath())) {
      int cnt =
          udfVersionDao.getOtherSameJarCount(
              userName,
              udfUpdateVo.getPath().substring(udfUpdateVo.getPath().lastIndexOf("/") + 1),
              udfUpdateVo.getId());
      if (cnt > 0) {
        throw new UDFException("用户的udf已存在同名jar");
      }
    }
    oldUdfInfo.setUpdateTime(new Date());
    //        udfInfo.setPath(StringUtils.replace(udfInfo.getPath(), "file://", ""));
    CommonLock persistenceLock = new CommonLock();
    persistenceLock.setLockObject(userName + _LOCK);
    persistenceLock.setCreateTime(new Date());
    persistenceLock.setUpdateTime(new Date());
    try {
      commonLockService.lock(persistenceLock, LOCK_TIMEOUT);
      UDFVersion latestVersion = udfVersionDao.selectLatestByUdfId(udfUpdateVo.getId());
      if (null == latestVersion) {
        throw new UDFException("can't found latestVersion for the udf");
      }
      // 支持hdfs path
      FsPath fsPath = new FsPath(udfUpdateVo.getPath());
      FileSystem fileSystem = (FileSystem) FSFactory.getFsByProxyUser(fsPath, userName);
      InputStream is = null, is2 = null;
      BmlUpdateResponse response;
      String newMd5 = "";
      try {
        fileSystem.init(null);
        is = fileSystem.read(fsPath);
        newMd5 = DigestUtils.md5DigestAsHex(is);
        if (newMd5.equals(latestVersion.getMd5())) {
          latestVersion.setPath(udfUpdateVo.getPath());
          latestVersion.setRegisterFormat(udfUpdateVo.getRegisterFormat());
          latestVersion.setUseFormat(udfUpdateVo.getUseFormat());
          latestVersion.setDescription(udfUpdateVo.getDescription());
          udfVersionDao.updateUDFVersion(latestVersion);
          udfDao.updateUDF(oldUdfInfo);
          return;
        }
        is2 = fileSystem.read(fsPath);
        response = uploadToBml(userName, fsPath.getPath(), is2, latestVersion.getBmlResourceId());
      } catch (IOException e) {
        throw new UDFException("文件读取异常：" + e.getMessage());
      } catch (Exception e) {
        throw new UDFException("上传到bml异常！msg:" + e.getMessage());
      } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(is2);
        if (fileSystem != null) {
          fileSystem.close();
        }
      }
      if (!response.isSuccess()) {
        throw new UDFException("上传到bml失败，文件路径为：" + udfUpdateVo.getPath());
      }
      UDFVersion newVersion = new UDFVersion();
      newVersion.setUdfId(udfUpdateVo.getId());
      newVersion.setPath(udfUpdateVo.getPath());
      newVersion.setUseFormat(udfUpdateVo.getUseFormat());
      newVersion.setRegisterFormat(udfUpdateVo.getRegisterFormat());
      newVersion.setDescription(udfUpdateVo.getDescription());
      newVersion.setCreateTime(new Date());
      newVersion.setBmlResourceId(response.resourceId());
      newVersion.setBmlResourceVersion(response.version());
      newVersion.setMd5(newMd5);
      udfVersionDao.addUdfVersion(newVersion);
      udfDao.updateUDF(oldUdfInfo);
      logger.info("end for udf update, udfName:" + udfUpdateVo.getUdfName());
    } finally {
      commonLockService.unlock(persistenceLock);
    }
  }

  @Override
  @Deprecated
  public UDFInfo createSharedUdfInfo(UDFInfo udfInfo, Long shareParentId, FsPath sharedPath)
      throws Exception {
    UDFInfo sharedUDFInfo = new UDFInfo();
    DateConverter converter = new DateConverter(new Date());
    BeanUtilsBean.getInstance().getConvertUtils().register(converter, Date.class);
    BeanUtils.copyProperties(sharedUDFInfo, udfInfo);
    sharedUDFInfo.setId(null);
    sharedUDFInfo.setCreateTime(new Date());
    sharedUDFInfo.setUpdateTime(new Date());
    sharedUDFInfo.setTreeId(shareParentId);
    //        sharedUDFInfo.setPath(sharedPath.getPath());
    sharedUDFInfo.setShared(true);
    sharedUDFInfo.setExpire(false);
    return sharedUDFInfo;
  }

  private UDFTree getOrCreateTree(String userName, String category, String treeName)
      throws UDFException {
    // 个人函数目录
    List<UDFTree> selfTree =
        udfTreeDao.getTreesByParentId(
            new HashMap<String, Object>() {
              {
                put("parent", -1);
                put("userName", userName);
                put("category", category);
              }
            });
    if (selfTree == null || selfTree.size() == 0) {
      throw new UDFException("该用户没有个人函数目录!");
    }
    List<UDFTree> selfTreeChildren =
        udfTreeDao.getTreesByParentId(
            new HashMap<String, Object>() {
              {
                put("parent", selfTree.get(0).getId());
                put("userName", userName);
                put("category", category);
              }
            });
    for (UDFTree tree : selfTreeChildren) {
      if (tree.getName().equals(treeName)) {
        return tree;
      }
    }
    UDFTree treeToAdd =
        new UDFTree(
            null,
            selfTree.get(0).getId(),
            treeName,
            userName,
            "",
            new Date(),
            new Date(),
            category);
    udfTreeDao.addTree(treeToAdd);
    return treeToAdd;
  }

  @Override
  @Transactional
  public void handoverUdf(Long udfId, String handoverUser) throws UDFException {
    logger.info("begin to handover udf, udfId: " + udfId);
    UDFInfo udfInfo = udfDao.getUDFById(udfId);
    UDFVersion latestVersion = udfVersionDao.selectLatestByUdfId(udfId);
    long count = udfDao.getSameNameCountByUser(udfInfo.getUdfName(), handoverUser);
    long sharedCount =
        udfDao.getShareSameNameCountExcludeUser(
            udfInfo.getUdfName(), handoverUser, udfInfo.getCreateUser());
    if (count > 0 || sharedCount > 0) {
      throw new UDFException("The handoverUser has same name udf.(被移交用户包含重名udf)");
    }
    // 只考虑校验最新版本的jar是否同名，否则需要校验移交udf的所有版本的jar
    if (udfInfo.getUdfType() == UDF_JAR && StringUtils.isNotBlank(latestVersion.getPath())) {
      validateJarFileName(
          latestVersion.getPath().substring(latestVersion.getPath().lastIndexOf("/") + 1),
          handoverUser);
    }
    String oldUser = udfInfo.getCreateUser();
    udfInfo.setCreateUser(handoverUser);
    String category =
        udfInfo.getUdfType() == FUNCTION_PY || udfInfo.getUdfType() == FUNCTION_SCALA
            ? FUNCTION
            : UDF;
    UDFTree originTree = udfTreeDao.getTreeById(udfInfo.getTreeId());

    UDFTree tree = getOrCreateTree(handoverUser, category, originTree.getName());
    udfInfo.setTreeId(tree.getId());
    udfDao.updateUDF(udfInfo);
    // 若用户共享了该udf，需要删除记录
    udfDao.deleteSharedUser(handoverUser, udfId);
    // 移交用户不保留加载
    udfDao.deleteLoadInfo(udfId, oldUser);

    //        String newResourceId = "";
    //        long versionCnt = 0;
    //        for (UDFVersion udfVersion : udfVersionList) {
    //            // todo BMLService添加copy方法，根据resourceID直接复制
    //            BmlDownloadResponse downloadResponse = downloadBml(oldUser,
    // udfVersion.getBmlResourceId(), udfVersion.getBmlResourceVersion());
    //            if (!downloadResponse.isSuccess()) {
    //                throw new UDFException("bml下载异常！");
    //            }
    //            if (versionCnt++ == 0) {
    //                try {
    //                    BmlUploadResponse uploadResponse = uploadToBml(handoverUser,
    // udfVersion.getPath(), downloadResponse.inputStream());
    //                    newResourceId = uploadResponse.resourceId();
    //                } finally {
    //                    IOUtils.closeQuietly(downloadResponse.inputStream());
    //                }
    //            } else {
    //                try {
    //                    uploadToBml(handoverUser, udfVersion.getPath(),
    // downloadResponse.inputStream(), newResourceId);
    //                } finally {
    //                    IOUtils.closeQuietly(downloadResponse.inputStream());
    //                }
    //            }
    //        }
    BmlCopyResourceResponse response =
        bmlClient.copyResourceToAnotherUser(
            latestVersion.getBmlResourceId(), handoverUser, oldUser);
    if (!response.isSuccess()) {
      throw new UDFException("failed to copy resource to anotherUser:" + handoverUser);
    }
    udfVersionDao.updateResourceIdByUdfId(udfId, response.resourceId(), oldUser, handoverUser);

    logger.info("end to handover udf, udfId: " + udfId);
  }

  @Override
  public void publishUdf(Long udfId, String version) throws UDFException {
    logger.info("begin to publish udf, udfId: " + udfId);
    UDFInfo udfInfo = udfDao.getUDFById(udfId);
    if (!Boolean.TRUE.equals(udfInfo.getShared())) {
      throw new UDFException("非共享udf不支持发布操作！");
    }
    udfVersionDao.updatePublishStatus(udfId, version, true);
    logger.info("end to publish udf, udfId: " + udfId);
  }

  @Override
  public void publishLatestUdf(Long udfId) throws UDFException {
    UDFVersion udfVersion = udfVersionDao.selectLatestByUdfId(udfId);
    udfVersionDao.updatePublishStatus(udfId, udfVersion.getBmlResourceVersion(), true);
  }

  @Override
  public void rollbackUDF(Long udfId, String version, String userName) throws UDFException {
    logger.info("begin to rollback udf, udfId: " + udfId);
    UDFVersion udfVersion = udfVersionDao.selectByUdfIdAndVersion(udfId, version);
    //        BmlDownloadResponse downloadResponse = downloadBml(userName,
    // udfVersion.getBmlResourceId(), udfVersion.getBmlResourceVersion());
    //        if (!downloadResponse.isSuccess()) {
    //            throw new UDFException("bml下载异常！");
    //        }
    //        BmlUpdateResponse updateResponse = null;
    //        try {
    //            updateResponse = uploadToBml(userName, udfVersion.getPath(),
    // downloadResponse.inputStream(), udfVersion.getBmlResourceId());
    //            if (!updateResponse.isSuccess()) {
    //                throw new UDFException("bml上传异常！");
    //            }
    //        } catch (Exception e) {
    //            logger.error(e.getMessage(), e);
    //            throw new UDFException("bml上传异常！" + e.getMessage());
    //        } finally {
    //            IOUtils.closeQuietly(downloadResponse.inputStream());
    //        }
    BmlRollbackVersionResponse response =
        bmlClient.rollbackVersion(
            udfVersion.getBmlResourceId(), udfVersion.getBmlResourceVersion(), userName);
    if (!response.isSuccess()) {
      throw new UDFException("bml rollback version 异常！");
    }
    UDFVersion newVersion =
        new UDFVersion(
            null,
            udfVersion.getUdfId(),
            udfVersion.getPath(),
            response.resourceId(),
            response.version(),
            udfVersion.getPublished(),
            udfVersion.getRegisterFormat(),
            udfVersion.getUseFormat(),
            udfVersion.getDescription(),
            new Date(),
            udfVersion.getMd5());
    udfVersionDao.addUdfVersion(newVersion);
    logger.info("end to rollback udf, udfId: " + udfId);
  }

  @Override
  public List<UDFVersionVo> getUdfVersionList(long udfId) {
    return udfVersionDao.getAllVersionByUdfId(udfId);
  }

  @Override
  public PageInfo<UDFAddVo> getManagerPages(
      String udfName, Collection<Integer> udfType, String createUser, int curPage, int pageSize)
      throws Exception {
    logger.info("begin to get managerPages.");
    List<UDFAddVo> retList = new ArrayList<>();
    PageHelper.startPage(curPage, pageSize);
    retList = udfDao.getUdfInfoByPages(udfName, udfType, createUser);
    PageInfo<UDFAddVo> pageInfo = new PageInfo<>(retList);
    boolean ismanager = isUDFManager(createUser);
    List<Long> loadedUdf = udfDao.getLoadedUDFIds(createUser);
    if (pageInfo.getList() != null) {
      pageInfo
          .getList()
          .forEach(
              l -> {
                l.setLoad(loadedUdf.contains(l.getId()));
                boolean canExpire = false;
                if (Boolean.TRUE.equals(l.getShared())) {
                  long loadCount = udfDao.getUserLoadCountByUdfId(l.getId(), createUser);
                  if (loadCount > 0) {
                    canExpire = true;
                  }
                }
                boolean finalCanExpire = canExpire;
                l.setOperationStatus(
                    new HashMap<String, Boolean>() {
                      {
                        put("canUpdate", true);
                        put("canDelete", !finalCanExpire);
                        put("canExpire", finalCanExpire);
                        put("canShare", ismanager);
                        put("canPublish", ismanager && Boolean.TRUE.equals(l.getShared()));
                        put("canHandover", true);
                      }
                    });
              });
    }
    logger.info("end to get managerPages.");
    return pageInfo;
  }

  @Override
  public String downLoadUDF(long udfId, String version, String user) throws Exception {
    logger.info("user " + user + " begin to downLoad udf, udfId: " + udfId);
    UDFInfo udfInfo = udfDao.getUDFById(udfId);
    if (udfInfo.getUdfType() == UDF_JAR) {
      throw new UDFException("jar类型的udf不支持下载查看内容");
    }
    UDFVersion udfVersion = udfVersionDao.selectByUdfIdAndVersion(udfId, version);
    BmlDownloadResponse downloadResponse =
        downloadBml(user, udfVersion.getBmlResourceId(), udfVersion.getBmlResourceVersion());
    String content =
        IOUtils.toString(downloadResponse.inputStream(), Configuration.BDP_ENCODING().getValue());
    IOUtils.closeQuietly(downloadResponse.inputStream());
    logger.info("user " + user + " end to downLoad udf, udfId: " + udfId);
    return content;
  }

  @Override
  public DownloadVo downloadToLocal(long udfId, String version, String user) throws Exception {
    logger.info("user " + user + " begin to downLoad udf, udfId: " + udfId);
    UDFVersion udfVersion = udfVersionDao.selectByUdfIdAndVersion(udfId, version);
    BmlDownloadResponse downloadResponse =
        downloadBml(user, udfVersion.getBmlResourceId(), udfVersion.getBmlResourceVersion());
    //        IOUtils.closeQuietly(downloadResponse.inputStream());
    logger.info("user " + user + " end to downLoad udf, udfId: " + udfId);
    return new DownloadVo(
        udfVersion.getPath().substring(udfVersion.getPath().lastIndexOf("/") + 1),
        downloadResponse.inputStream());
  }

  @Override
  public List<String> allUdfUsers() {
    return udfDao.selectAllUser();
  }

  @Override
  public List<String> getUserDirectory(String user, String category) {
    return udfTreeDao.getUserDirectory(user, category);
  }

  @Override
  public List<UDFInfoVo> getAllUDFSByUserName(String userName) throws UDFException {
    logger.info("Start to get all udf {}", userName);
    List<String> users = new ArrayList<String>();
    users.add(SYS_USER);
    users.add(BDP_USER);
    users.add(userName);
    List<UDFInfoVo> udfsByUsers = udfDao.getUDFSByUsers(users);
    List<UDFInfoVo> sharedUDFByUser = udfDao.getSharedUDFByUser(userName);
    List<UDFInfoVo> udfInfoVos = new ArrayList<>();
    if (null != udfsByUsers) {
      udfInfoVos.addAll(udfsByUsers);
    }
    if (null != sharedUDFByUser) {
      udfInfoVos.addAll(sharedUDFByUser);
    }
    logger.info("Finished to get all udf {}, size {}", userName, udfInfoVos.size());
    return udfInfoVos;
  }

  //    @Override
  //    public FsPath copySharedUdfFile(String userName, UDFInfo udfInfo) throws IOException {
  //        String fileName = FilenameUtils.getBaseName(udfInfo.getPath()) +
  // System.currentTimeMillis() + "." + FilenameUtils.getExtension(udfInfo.getPath());
  //        FsPath udfPath = FsPath.getFsPath(udfInfo.getPath());
  //        FsPath sharedPath = FsPath.getFsPath(UdfConfiguration.UDF_SHARE_PATH().getValue(),
  // fileName);
  //        FileSystem fileSystem = (FileSystem) FSFactory.getFsByProxyUser(sharedPath, userName);
  //        try{
  //            fileSystem.init(null);
  //            File file = new File(sharedPath.getPath());
  //            if (!file.exists()) {
  //                file.createNewFile();
  //                file.setReadable(true, false);
  //                file.setWritable(true, false);
  //                //fileSystem.createNewFile(sharedPath);
  //            }
  //            InputStream udfStream = fileSystem.read(udfPath);
  //            FileUtils.copyInputStreamToFile(udfStream, file);
  //            //fileSystem.setPermission(sharedPath, "-rwxrwxrwx");
  //        } catch (Exception e){
  //            fileSystem.close();
  //            throw e;
  //        }
  //        return sharedPath;
  //    }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public Boolean deleteUDF(Long udfId, String userName) throws UDFException {
    logger.info(userName + " begin to delete udf, udfId: " + udfId);
    UDFInfo udfInfo = udfDao.getUDFById(udfId);
    // 被用户加载的共享udf不能被删
    if (Boolean.TRUE.equals(udfInfo.getShared())) {
      long loadCount = udfDao.getUserLoadCountByUdfId(udfId, userName);
      if (loadCount > 0) {
        throw new UDFException("该共享udf被用户加载，不能删除");
      } else {
        udfDao.deleteAllSharedUser(udfId);
      }
    }
    // 若不是共享udf，load_info最多一条记录
    udfDao.deleteLoadInfo(udfId, userName);
    udfDao.deleteUDF(udfId, userName);
    udfVersionDao.deleteVersionByUdfId(udfId);
    logger.info(userName + " end to delete udf, udfId: " + udfId);
    return true;
  }

  @Override
  public UDFInfo getUDFById(Long id, String userName) {
    return udfDao.getUDFById(id);
  }

  @Override
  public Boolean deleteLoadInfo(Long id, String userName) {
    udfDao.deleteLoadInfo(id, userName);
    return true;
  }

  /**
   * * Load UDF For the UDF of the jar package type, you need to add a judgment: * 1. Is there a
   * judgment of the same name? * 2. Is there a different path jar to judge? 加载UDF 对于jar包类型的UDF
   * 需要加判断： 1.是否有同名判断 2.是否有不同路径jar判断
   *
   * @param id
   * @param userName
   * @return
   * @throws UDFException
   */
  @Override
  public Boolean addLoadInfo(Long id, String userName) throws UDFException {
    try {
      UDFInfo udfInfo = getUDFById(id, userName);
      if (udfInfo.getUdfType() == 0) {
        /*long sysCount = udfDao.getSameSysCount(udfInfo.getUdfName());*/
        long loadCount = udfDao.getSameLoadCount(userName, udfInfo.getUdfName());
        if (loadCount > 0) {
          throw new UDFException(
              "There is a Jar package function with the same name(存在同名的Jar包函数)： "
                  + udfInfo.getUdfName());
        }
        // 校验jar包名字
        //                String path = udfInfo.getPath();
        //                String jarPath = path.substring(path.lastIndexOf("/"));
        //                if (!StringUtils.isEmpty(jarPath)) {
        //                    List<UDFInfo> sameJarUDF = udfDao.getSameJarUDF(userName,
        // jarPath);
        //                    for (UDFInfo udf : sameJarUDF) {
        //                        if (path.equalsIgnoreCase(udf.getPath())) {
        //                            continue;
        //                        }
        //                        if
        // (jarPath.equalsIgnoreCase(udf.getPath().substring(udf.getPath().lastIndexOf("/")))) {
        //                            throw new UDFException("There is a Jar package with a
        // different path of the same name. The UDF name is(存在同名不同路径的Jar包,UDF名字为)：" +
        // udf.getUdfName() + ",The jar path is(jar路径为)" + udf.getPath());
        //                        }
        //                    }
        //                }

      }
      udfDao.addLoadInfo(id, userName);
    } catch (Throwable e) {
      if (e instanceof DuplicateKeyException) {
        return true;
      } else {
        throw new UDFException(e.getMessage());
      }
    }
    return true;
  }

  @Override
  public List<UDFInfo> getUDFSByUserName(String userName) {

    return udfDao.getUDFSByUserName(userName);
  }

  @Override
  public List<UDFInfoVo> getUDFSByTreeIdAndUser(Long treeId, String userName, String category) {
    return udfDao.getUDFSByTreeIdAndUser(treeId, userName, categoryToCodes.get(category));
  }

  @Override
  public List<UDFInfoVo> getUDFInfoByTreeId(Long treeId, String userName, String category) {
    return udfDao.getUDFInfoByTreeId(treeId, userName, categoryToCodes.get(category));
  }

  @Override
  public List<UDFInfoVo> getUDFInfoByIds(String username, Long[] ids, String category) {
    if (ids == null || ids.length == 0) {
      return new ArrayList<>(0);
    }
    return udfDao.getUDFInfoByIds(username, ids, categoryToCodes.get(category));
  }

  /**
   * Generate sql needs content: divided into jar, python, scala Save Path and registration syntax
   * separately 生成sql需要内容： 分为jar，python，scala 分别保存Path和注册语法
   *
   * @param userName
   * @return
   */
  @Override
  @Deprecated
  public Map<String, List<String>> generateInitSql(String userName) {
    logger.info(userName + " generateInitSql");
    List<UDFInfo> loadedUDFs = udfDao.getLoadedUDFs(userName);
    Set<String> fileSet = new HashSet<>();
    List<String> udfJars = new ArrayList<>();
    List<String> registerJars = new ArrayList<>();
    List<String> udfPys = new ArrayList<>();
    List<String> registerPys = new ArrayList<>();
    List<String> udfScalas = new ArrayList<>();
    List<String> registerScalas = new ArrayList<>();

    List<String> functionsPython = new ArrayList<>();
    List<String> functionsScala = new ArrayList<>();

    //        for (UDFInfo udfInfo : loadedUDFs) {
    //            if (udfInfo.getUdfType() == UDF_JAR) {
    //                if (!fileSet.contains(udfInfo.getPath())) {
    //                    fileSet.add(udfInfo.getPath());
    //                    udfJars.add(udfInfo.getPath());
    //                }
    //                registerJars.add(udfInfo.getRegisterFormat());
    //            } else if (udfInfo.getUdfType() == ConstantVar.UDF_PY) {
    //                if (!fileSet.contains(udfInfo.getPath())) {
    //                    fileSet.add(udfInfo.getPath());
    //                    udfPys.add(udfInfo.getPath());
    //                }
    //                registerPys.add(udfInfo.getRegisterFormat());
    //            } else if (udfInfo.getUdfType() == ConstantVar.UDF_SCALA) {
    //                if (!fileSet.contains(udfInfo.getPath())) {
    //                    fileSet.add(udfInfo.getPath());
    //                    udfScalas.add(udfInfo.getPath());
    //                }
    //                registerScalas.add(udfInfo.getRegisterFormat());
    //            } else if (udfInfo.getUdfType() == ConstantVar.FUNCTION_PY) {
    //                if (!fileSet.contains(udfInfo.getPath())) {
    //                    fileSet.add(udfInfo.getPath());
    //                    functionsPython.add(udfInfo.getPath());
    //                }
    //            } else if (udfInfo.getUdfType() == ConstantVar.FUNCTION_SCALA) {
    //                if (!fileSet.contains(udfInfo.getPath())) {
    //                    fileSet.add(udfInfo.getPath());
    //                    functionsScala.add(udfInfo.getPath());
    //                }
    //            }
    //        }
    Map<String, List<String>> res = new HashedMap();
    res.put("udfJars", udfJars);
    res.put("registerJars", registerJars);
    res.put("udfPys", udfPys);
    res.put("registerPys", registerPys);
    res.put("udfScalas", udfScalas);
    res.put("registerScalas", registerScalas);
    res.put("functionsPython", functionsPython);
    res.put("functionsScala", functionsScala);
    return res;
  }

  @Override
  @Deprecated
  public Iterator<String> getAllLoadJars(String userName) throws UDFException {
    List<UDFInfo> loadedUDFs = udfDao.getLoadedUDFs(userName);
    Set<String> fileSet = new HashSet<>();
    for (UDFInfo udfInfo : loadedUDFs) {
      //            if (udfInfo.getUdfType() == 0) {
      //                if (!fileSet.contains(udfInfo.getPath())) {
      //                    fileSet.add(udfInfo.getPath());
      //                }
      //            }
    }
    return fileSet.iterator();
  }

  @Override
  @Deprecated
  public List<UDFInfo> getSharedUDFByUserName(String userName) {
    return null;
  }

  @Override
  @Deprecated
  public List<UDFInfo> getSharedUDFByTreeId(Integer treeId, String userName) {
    return null;
  }

  @Override
  @Deprecated
  public List<UDFInfo> getSharedUDFInfos(Long id, String userName, String category) {
    return udfDao.selectSharedUDFInfosByTreeIdAndUserName(
        id, userName, categoryToCodes.get(category));
  }

  /**
   * 得到用户的共享函数列表
   *
   * @param userName
   * @param category
   * @return
   */
  @Override
  public List<UDFInfoVo> getSharedUDFs(String userName, String category) {
    List<UDFInfoVo> udfIds = getLatesetPublishedUDF(userName, category);
    return udfIds.stream()
        .filter(l -> !Boolean.TRUE.equals(l.getExpire()))
        .collect(Collectors.toList());
  }

  private List<UDFInfoVo> getLatesetPublishedUDF(String userName, String category) {
    List<UDFInfoVo> udfIds = udfDao.getLatesetPublishedUDF(userName, categoryToCodes.get(category));
    return udfIds;
  }

  @Override
  public List<UDFInfoVo> getExpiredUDFs(String userName, String category) {
    List<UDFInfoVo> udfIds = getLatesetPublishedUDF(userName, category);
    return udfIds.stream()
        .filter(l -> Boolean.TRUE.equals(l.getExpire()))
        .collect(Collectors.toList());
  }

  @Override
  public Boolean isUDFManager(String userName) {
    UDFManager udfManager = udfDao.selectUDFManager(userName);
    if (udfManager == null) {
      return false;
    }
    return true;
  }

  @Override
  public void checkSharedUsers(Set<String> sharedUsers, String userName, String udfName)
      throws UDFException {
    if (sharedUsers.contains(userName)) {
      throw new UDFException("Do not support sharing to yourself!(不支持分享给自己!)");
    }
    // 校验共享用户是否包含同名udf
    for (String shareduser : sharedUsers) {
      if (StringUtils.isEmpty(shareduser)) {
        throw new UDFException("共享用户不能包含空用户名！");
      }
      if (!pattern.matcher(shareduser).matches()) {
        throw new UDFException("用户名只能包含字母数字下划线！");
      }
      long count = udfDao.getSameNameCountByUser(udfName, shareduser);
      long sharedCount = udfDao.getShareSameNameCountExcludeUser(udfName, shareduser, userName);
      if (count > 0 || sharedCount > 0) {
        throw new UDFException("用户：" + shareduser + "包含同名udf！");
      }
    }
    //        List<String> notExistUsers = Lists.newArrayList();
    //        for(String sharedUser: sharedUsers){
    //            Long userId = udfDao.selectIdByUserName(sharedUser);
    //            if(userId == null){
    //                notExistUsers.add(sharedUser);
    //            }
    //        }
    //        if(!notExistUsers.isEmpty()){
    //            throw new UDFException("Sharing user does not exist：" + String.join(",",
    // notExistUsers));
    //        }
  }

  @Override
  @Deprecated
  public UDFInfo addSharedUDFInfo(UDFInfo sharedUDFInfo) throws UDFException {
    long count =
        udfDao.getShareSameNameCountByUser(
            sharedUDFInfo.getUdfName(), sharedUDFInfo.getCreateUser());
    if (count > 0) {
      throw new UDFException(
          "Shared udf name(分享的udf的名字)("
              + sharedUDFInfo.getUdfName()
              + ")Already exists, please edit the name and re-share(已存在，请修改名字后重新进行分享)");
    }
    udfDao.addUDF(sharedUDFInfo);
    return sharedUDFInfo;
  }

  @Override
  public void setUDFSharedInfo(boolean iShared, Long id) {
    udfDao.updateUDFIsShared(iShared, id);
  }

  @Override
  public Long getAllShareUDFInfoIdByUDFId(String userName, String udfName) {

    return udfDao.selectAllShareUDFInfoIdByUDFId(userName, udfName);
  }

  @Override
  public void setUdfExpire(Long udfId, String userName) throws UDFException {
    logger.info(userName + " begin to expire udf, udfId: " + udfId);
    UDFInfo udfInfo = getUDFById(udfId, userName);
    if (Boolean.TRUE.equals(udfInfo.getShared())) {
      long loadCount = udfDao.getUserLoadCountByUdfId(udfId, userName);
      if (loadCount > 0) {
        udfDao.updateSharedUDFExpire(udfId);
        logger.info(userName + " end to expire udf, udfId: " + udfId);
        return;
      }
    }
    throw new UDFException("只有被共享用户加载的udf可以设置过期");
  }

  @Override
  public List<String> getAllSharedUsersByUdfId(String userName, long udfId) {
    List<String> shareUsers = udfDao.selectAllShareUsersByUDFId(userName, udfId);
    shareUsers.removeIf(userName::equals);
    return shareUsers;
  }

  @Override
  public void addSharedUser(Set<String> sharedUsers, Long udfId) {
    for (String sharedUser : sharedUsers) {
      udfDao.insertSharedUser(sharedUser, udfId);
    }
  }

  @Override
  public void removeSharedUser(Collection<String> oldsharedUsers, Long udfId) {
    // 同时需要取消共享用户加载这个udf
    for (String oldsharedUser : oldsharedUsers) {
      udfDao.deleteSharedUser(oldsharedUser, udfId);
      udfDao.deleteLoadInfo(udfId, oldsharedUser);
    }
  }

  @Override
  public List<UDFAddVo> getUdfByNameList(List<String> udfNameList, String creator) {
    List<UDFAddVo> retList = new ArrayList<>();
    retList = udfDao.getUdfInfoByNameList(udfNameList, creator);
    boolean ismanager = isUDFManager(creator);
    List<Long> loadedUdf = udfDao.getLoadedUDFIds(creator);
    retList.forEach(
        udfInfo -> {
          udfInfo.setLoad(loadedUdf.contains(udfInfo.getId()));
          boolean canExpire = false;
          if (Boolean.TRUE.equals(udfInfo.getShared())) {
            long loadCount =
                udfDao.getUserLoadCountByUdfId(udfInfo.getId(), udfInfo.getCreateUser());
            if (loadCount > 0) {
              canExpire = true;
            }
          }
          boolean finalCanExpire = canExpire;
          udfInfo.setOperationStatus(
              new HashMap<String, Boolean>() {
                {
                  put("canUpdate", true);
                  put("canDelete", !finalCanExpire);
                  put("canExpire", finalCanExpire);
                  put("canShare", ismanager);
                  put("canPublish", ismanager && Boolean.TRUE.equals(udfInfo.getShared()));
                  put("canHandover", true);
                }
              });
        });
    return retList;
  }

  @Override
  public UDFVersionVo getUdfVersionInfo(String udfName, String createUser) {
    return udfVersionDao.getUdfVersionInfoByName(udfName, createUser);
  }
}
