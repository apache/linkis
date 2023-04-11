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

package org.apache.linkis.datasourcemanager.core.receivers;

import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoQueryRequest;
import org.apache.linkis.datasourcemanager.common.protocol.DsInfoResponse;
import org.apache.linkis.datasourcemanager.core.restful.RestfulApiHelper;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DsmReceiver {
  private static final Logger LOG = LoggerFactory.getLogger(DsmReceiver.class);

  @Autowired private DataSourceInfoService dataSourceInfoService;

  @Autowired private DataSourceRelateService dataSourceRelateService;

  @Receiver
  public DsInfoResponse dealDsInfoQueryRequest(DsInfoQueryRequest dsInfoQueryRequest) {
    if (dsInfoQueryRequest.isValid()) {
      try {
        DataSource dataSource = null;
        if (!Objects.isNull(dsInfoQueryRequest.getName())
            && !Objects.isNull(dsInfoQueryRequest.getEnvId())) {
          LOG.info(
              "Try to get dataSource by dataSourceName: {}, envId: {}",
              dsInfoQueryRequest.getName(),
              dsInfoQueryRequest.getEnvId());
          dataSource =
              dataSourceInfoService.getDataSourceInfoForConnect(
                  dsInfoQueryRequest.getName(), dsInfoQueryRequest.getEnvId());
        } else if (!Objects.isNull(dsInfoQueryRequest.getName())) {
          LOG.info("Try to get dataSource by dataSourceName: {}", dsInfoQueryRequest.getName());
          dataSource =
              dataSourceInfoService.getDataSourceInfoForConnect(dsInfoQueryRequest.getName());
        } else if (Long.valueOf(dsInfoQueryRequest.getId()) > 0) {
          LOG.info("Try to get dataSource by dataSourceId: {}", dsInfoQueryRequest.getId());
          dataSource =
              dataSourceInfoService.getDataSourceInfoForConnect(
                  Long.valueOf(dsInfoQueryRequest.getId()));
        }
        if (null != dataSource) {
          Long publishedVersionId = dataSource.getPublishedVersionId();
          if (Objects.isNull(publishedVersionId)) {
            LOG.warn("Datasource name:{} is not published.", dataSource.getDataSourceName());
            return new DsInfoResponse(
                false,
                MessageFormat.format(
                    "Datasource name:{0} is not published.", dataSource.getDataSourceName()));
          }

          RestfulApiHelper.decryptPasswordKey(
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId()),
              dataSource.getConnectParams());
          return new DsInfoResponse(
              true,
              dataSource.getDataSourceType().getName(),
              dataSource.getConnectParams(),
              dataSource.getCreateUser(),
              "");
        } else {
          LOG.warn("Can not get any dataSource");
          return new DsInfoResponse(true, "Can not get any dataSource");
        }
      } catch (Exception e) {
        LOG.error(
            "Fail to get data source information, id: {} system: {}",
            dsInfoQueryRequest.getId(),
            dsInfoQueryRequest.getSystem(),
            e);
        return new DsInfoResponse(
            false,
            String.format(
                "Fail to get data source information, id: %s system: %s",
                dsInfoQueryRequest.getId(), dsInfoQueryRequest.getSystem()));
      } catch (Throwable t) {
        LOG.error(
            "Fail to get data source information, id: {} system: {}",
            dsInfoQueryRequest.getId(),
            dsInfoQueryRequest.getSystem(),
            t);
        return new DsInfoResponse(
            false,
            String.format(
                "Fail to get data source information, id: %s system: %s",
                dsInfoQueryRequest.getId(), dsInfoQueryRequest.getSystem()));
      }
    } else {
      return new DsInfoResponse(true);
    }
  }
}
