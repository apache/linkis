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

package org.apache.linkis.datasourcemanager.core.service.impl;

import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlDeleteResponse;
import org.apache.linkis.bml.protocol.BmlUpdateResponse;
import org.apache.linkis.bml.protocol.BmlUploadResponse;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.core.service.BmlAppService;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.datasourcemanager.common.errorcode.LinkisDatasourceManagerErrorCodeSummary.EXPRESS_IS_NULL;

/** Wrap the communication between Bml service // TODO RPCException defined */
@Service
@RefreshScope
public class BmlAppServiceImpl implements BmlAppService {
  private static final Logger LOG = LoggerFactory.getLogger(BmlAppService.class);
  /** Bml client */
  private BmlClient client;

  @PostConstruct
  public void buildClient() {
    client = BmlClientFactory.createBmlClient();
  }

  @Override
  public String clientUploadResource(String userName, String fileName, InputStream inputStream)
      throws ErrorException {
    LOG.info(
        "Upload resource to bml server: [ proxy_to_user: "
            + userName
            + ", file name:"
            + fileName
            + " ]");
    try {
      BmlUploadResponse response = client.uploadResource(userName, fileName, inputStream);
      if (!response.isSuccess()) {
        throw new ErrorException(EXPRESS_IS_NULL.getErrorCode(), "");
      }
      return response.resourceId();
    } catch (Exception e) {
      LOG.error(
          "Failed to upload resource to bml server[上传资源文件失败], [ proxy_to_user: "
              + userName
              + ", file name:"
              + fileName
              + " ]",
          e);
      throw e;
    }
  }

  @Override
  public void clientRemoveResource(String userName, String resourceId) throws ErrorException {
    LOG.info(
        "Remove resource to bml server: [ proxy_to_user: "
            + userName
            + ", resource id:"
            + resourceId
            + " ]");
    try {
      BmlDeleteResponse response = client.deleteResource(userName, resourceId);
      if (!response.isSuccess()) {
        throw new ErrorException(EXPRESS_IS_NULL.getErrorCode(), "");
      }
    } catch (Exception e) {
      LOG.error(
          "Fail to remove resource to bml server[删除资源文件失败], [ proxy_to_user: "
              + userName
              + ", resource id:"
              + resourceId
              + " ]");
      throw e;
    }
  }

  @Override
  public String clientUpdateResource(String userName, String resourceId, InputStream inputStream)
      throws ErrorException {
    LOG.info(
        "Update resource to bml server: [ proxy_to_user: "
            + userName
            + ", resource id:"
            + resourceId
            + " ]");
    try {
      // File name is invalid;
      BmlUpdateResponse response =
          client.updateResource(userName, resourceId, "filename", inputStream);
      if (!response.isSuccess()) {
        throw new ErrorException(EXPRESS_IS_NULL.getErrorCode(), "");
      }
      return response.version();
    } catch (Exception e) {
      LOG.error(
          "Fail to update resource to bml server[更新资源文件失败], [ proxy_to_user: "
              + userName
              + ", resource id:"
              + resourceId
              + " ]");
      throw e;
    }
  }
}
