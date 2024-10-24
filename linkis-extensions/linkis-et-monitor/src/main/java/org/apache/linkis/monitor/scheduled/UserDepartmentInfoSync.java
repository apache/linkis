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

package org.apache.linkis.monitor.scheduled;

import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.department.dao.UserDepartmentInfoMapper;
import org.apache.linkis.monitor.department.entity.UserDepartmentInfo;
import org.apache.linkis.monitor.factory.MapperFactory;
import org.apache.linkis.monitor.utils.alert.AlertDesc;
import org.apache.linkis.monitor.utils.alert.ims.MonitorAlertUtils;
import org.apache.linkis.monitor.utils.alert.ims.PooledImsAlertUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class UserDepartmentInfoSync {

  private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);

  @Scheduled(cron = "${linkis.monitor.org.user.sync.cron:0 0 0 0/7 * ?}")
  public static void DepartmentInfoSync() {

    UserDepartmentInfoMapper userDepartmentInfoMapper = MapperFactory.getUserDepartmentInfoMapper();
    // 获取linkis_org_user_sync信息
    List<UserDepartmentInfo> userDepartmentInfos = userDepartmentInfoMapper.selectAllUsers();

    if (CollectionUtils.isEmpty(userDepartmentInfos)) {
      logger.info("No user department info to sync");
      // 并且发送告警通知
      return;
    } else {
      logger.info("Start to sync user department info");

      List<UserDepartmentInfo> alterList =
          userDepartmentInfos.stream()
              .filter(
                  userDepartmentInfo ->
                      StringUtils.isNotBlank(userDepartmentInfo.getUserName())
                          && (StringUtils.isBlank(userDepartmentInfo.getOrgId())
                              || StringUtils.isBlank(userDepartmentInfo.getOrgName())))
              .collect(Collectors.toList());
      List<UserDepartmentInfo> syncList =
          userDepartmentInfos.stream()
              .filter(
                  userDepartmentInfo ->
                      StringUtils.isNotBlank(userDepartmentInfo.getUserName())
                          && StringUtils.isNotBlank(userDepartmentInfo.getOrgId())
                          && StringUtils.isNotBlank(userDepartmentInfo.getOrgName()))
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(alterList)) {
        // 统计异常名称，然后发送告警
        String usernames =
            alterList.stream()
                .filter(s -> StringUtils.isNotBlank(s.getUserName()))
                .map(UserDepartmentInfo::getUserName)
                .limit(5)
                .collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(usernames)) {
          HashMap<String, String> replaceParm = new HashMap<>();
          replaceParm.put("$user", usernames);
          replaceParm.put("$count", String.valueOf(alterList.size()));
          Map<String, AlertDesc> ecmResourceAlerts =
              MonitorAlertUtils.getAlerts(Constants.DEPARTMENT_USER_IM(), replaceParm);
          PooledImsAlertUtils.addAlert(ecmResourceAlerts.get("12019"));
        }
      }
      if (!CollectionUtils.isEmpty(syncList)) {
        // 删除org_user数据，再同步
        userDepartmentInfoMapper.deleteUser();
        userDepartmentInfoMapper.batchInsertUsers(syncList);
      }
    }
  }
}
