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

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.datasourcemanager.core.formdata.FormStreamContent;
import org.apache.linkis.datasourcemanager.core.service.BmlAppService;
import org.apache.linkis.datasourcemanager.core.service.MetadataOperateService;
import org.apache.linkis.metadata.query.common.protocol.MetadataConnect;
import org.apache.linkis.metadata.query.common.protocol.MetadataResponse;
import org.apache.linkis.rpc.Sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.datasourcemanager.common.errorcode.LinkisDatasourceManagerErrorCodeSummary.CONNECTION_FAILED;
import static org.apache.linkis.datasourcemanager.common.errorcode.LinkisDatasourceManagerErrorCodeSummary.OPERATE_FILE_IN_REQUEST;
import static org.apache.linkis.datasourcemanager.common.errorcode.LinkisDatasourceManagerErrorCodeSummary.REMOTE_SERVICE_ERROR;

@Service
public class MetadataOperateServiceImpl implements MetadataOperateService {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataOperateService.class);
  @Autowired private BmlAppService bmlAppService;

  @Override
  public void doRemoteConnect(
      String mdRemoteServiceName,
      String dataSourceType,
      String operator,
      Map<String, Object> connectParams)
      throws WarnException {
    List<String> uploadedResources = new ArrayList<>();
    try {
      connectParams
          .entrySet()
          .removeIf(
              entry -> {
                Object paramValue = entry.getValue();
                // Upload stream resource in connection parameters
                if (paramValue instanceof FormStreamContent) {
                  FormStreamContent streamContent = (FormStreamContent) paramValue;
                  String fileName = streamContent.getFileName();
                  InputStream inputStream = streamContent.getStream();
                  if (null != inputStream) {
                    try {
                      String resourceId =
                          bmlAppService.clientUploadResource(operator, fileName, inputStream);
                      if (null == resourceId) {
                        return true;
                      }
                      uploadedResources.add(resourceId);
                      entry.setValue(resourceId);
                    } catch (ErrorException e) {
                      // TODO redefined a exception extends warnException
                      throw new WarnException(
                          OPERATE_FILE_IN_REQUEST.getErrorCode(),
                          OPERATE_FILE_IN_REQUEST.getErrorDesc());
                    }
                  }
                }
                return false;
              });
      LOG.info(
          "Send request to metadata service:["
              + mdRemoteServiceName
              + "] for building a connection");
      // Get a sender
      Sender sender = Sender.getSender(mdRemoteServiceName);
      try {
        Object object =
            sender.ask(new MetadataConnect(dataSourceType, operator, connectParams, ""));
        if (object instanceof MetadataResponse) {
          MetadataResponse response = (MetadataResponse) object;
          if (!response.status()) {
            throw new WarnException(
                CONNECTION_FAILED.getErrorCode(),
                CONNECTION_FAILED.getErrorDesc() + ", Msg[" + response.data() + "]");
          }
        } else {
          throw new WarnException(
              REMOTE_SERVICE_ERROR.getErrorCode(), REMOTE_SERVICE_ERROR.getErrorDesc());
        }
      } catch (Exception t) {
        if (!(t instanceof WarnException)) {
          throw new WarnException(
              REMOTE_SERVICE_ERROR.getErrorCode(),
              REMOTE_SERVICE_ERROR.getErrorDesc() + ", message:[" + t.getMessage() + "]");
        }
        throw t;
      }
    } finally {
      if (!uploadedResources.isEmpty()) {
        uploadedResources.forEach(
            resourceId -> {
              try {
                // Proxy to delete resource
                bmlAppService.clientRemoveResource(operator, resourceId);
              } catch (Exception e) {
                // ignore
                // TODO add strategy to fix the failure of deleting
              }
            });
      }
    }
  }
}
