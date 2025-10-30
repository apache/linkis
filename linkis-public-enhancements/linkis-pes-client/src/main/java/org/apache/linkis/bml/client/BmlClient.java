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

package org.apache.linkis.bml.client;

import org.apache.linkis.bml.protocol.*;

import java.io.Closeable;
import java.io.InputStream;
import java.util.List;

public interface BmlClient extends Closeable {
  /**
   * 传入resourceID bmlclient会resource的输入流,如果不传入version,默认返回最新的版本 Pass in the resourceID bmlclient
   * will resource the input stream. If you do not pass in the version, the latest version will be
   * returned by default.
   *
   * @param resourceID resourceID
   * @return InputStream
   */
  BmlDownloadResponse downloadResource(String user, String resourceID);

  BmlDownloadResponse downloadResource(String user, String resourceId, String version);

  BmlDownloadResponse downloadResource(
      String user, String resourceId, String version, String path, boolean overwrite);

  /**
   * relateResource方法将targetFilePath路径的文件关联到resourceID下面
   * targetFilePath需要包括schema，如果不包含schema，默认是hdfs
   *
   * @param resourceID resourceID
   * @param targetFilePath 指定文件目录
   * @return BmlRelateResult 包含resourceId和新的version
   */
  BmlRelateResponse relateResource(String resourceID, String targetFilePath);

  /**
   * 更新资源信息
   *
   * @param resourceID 资源id
   * @param filePath 目标文件路径
   * @return resourceId 新的版本信息
   */
  BmlUpdateResponse updateResource(String user, String resourceID, String filePath);

  BmlUpdateResponse updateResource(
      String user, String resourceID, String filePath, InputStream inputStream);

  /**
   * 上传资源,用户指定输入流
   *
   * @param user 用户名
   * @param filePath 上传的资源的路径
   * @param inputStream 上传资源的输入流
   * @return 包含resourceId和version
   */
  BmlUploadResponse uploadResource(String user, String filePath, InputStream inputStream);

  /**
   * 上传文件，用户指定文件路径，客户端自动获取输入流
   *
   * @param user 用户名
   * @param filePath 文件路径
   * @return 包含resourceId和version
   */
  BmlUploadResponse uploadResource(String user, String filePath);

  /**
   * 获取resource的所有版本
   *
   * @param user 用户名
   * @param resourceId 资源Id
   * @return resourceId对应下的所有版本信息
   */
  BmlResourceVersionsResponse getVersions(String user, String resourceId);

  /** */
  BmlDeleteResponse deleteResource(String user, String resourceId, String version);

  BmlDeleteResponse deleteResource(String user, String resourceId);

  BmlCreateProjectResponse createBmlProject(
      String creator, String projectName, List<String> accessUsers, List<String> editUsers);

  BmlUpdateProjectPrivResponse updateProjectPriv(
      String username, String projectName, List<String> editUsers, List<String> accessUsers);

  BmlUploadResponse uploadShareResource(
      String user, String projectName, String filePath, InputStream inputStream);

  BmlUploadResponse uploadShareResource(String user, String projectName, String filePath);

  BmlDownloadResponse downloadShareResource(String user, String resourceId, String version);

  BmlDownloadResponse downloadShareResource(String user, String resourceId);

  BmlUpdateResponse updateShareResource(
      String user, String resourceId, String filePath, InputStream inputStream);

  BmlUpdateResponse updateShareResource(String user, String resourceId, String filePath);

  BmlProjectInfoResponse getProjectInfoByName(String projectName);

  BmlResourceInfoResponse getResourceInfo(String resourceId);

  BmlProjectPrivResponse getProjectPriv(String projectName);

  BmlAttachResourceAndProjectResponse attachResourceAndProject(
      String projectName, String resourceId);

  BmlDownloadResponse downloadShareResource(
      String user, String resourceId, String version, String path, boolean overwrite);

  BmlChangeOwnerResponse changeOwnerByResourceId(
      String resourceId, String oldOwner, String newOwner);

  BmlCopyResourceResponse copyResourceToAnotherUser(
      String resourceId, String anotherUser, String originOwner);

  BmlRollbackVersionResponse rollbackVersion(String resourceId, String version, String user);

  BmlClientConnectInfoResponse getBmlClientConnectInfo();
}
