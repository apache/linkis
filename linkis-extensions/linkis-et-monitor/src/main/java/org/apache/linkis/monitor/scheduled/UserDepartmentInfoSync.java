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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class UserDepartmentInfoSync {

  private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);
  private static final int pagesize = 5000;

  private static final UserDepartmentInfoMapper userDepartmentInfoMapper =
      MapperFactory.getUserDepartmentInfoMapper();

  @Scheduled(cron = "${linkis.monitor.org.user.sync.cron:0 0 0 1/7 * ?}")
  public static void DepartmentInfoSync() {
    // 获取linkis_org_user_sync信息
    // 收集异常用户
    List<UserDepartmentInfo> alterList = new ArrayList<>();
    int pageNum = 1; // 初始pageNum
    while (true) {
      List<UserDepartmentInfo> departSyncList = null;
      PageHelper.startPage(pageNum, pagesize);
      try {
        departSyncList = userDepartmentInfoMapper.selectAllUsers();
      } finally {
        PageHelper.clearPage();
      }
      PageInfo<UserDepartmentInfo> pageInfo = new PageInfo<>(departSyncList);
      // 处理 departSyncList 中的数据
      processDepartSyncList(pageInfo.getList(), alterList);
      if (!pageInfo.isHasNextPage()) {
        break; // 没有更多记录，退出循环
      }
      pageNum++;
    }
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

  private static void processDepartSyncList(
      List<UserDepartmentInfo> departSyncList, List<UserDepartmentInfo> alterList) {
    if (CollectionUtils.isEmpty(departSyncList)) {
      logger.info("No user department info to sync");
      // 并且发送告警通知
      return;
    } else {
      logger.info("Start to sync user department info");
      // 收集异常用户
      List<UserDepartmentInfo> errorUserList =
          departSyncList.stream()
              .filter(
                  userDepartmentInfo ->
                      StringUtils.isNotBlank(userDepartmentInfo.getUserName())
                          && (StringUtils.isBlank(userDepartmentInfo.getOrgId())
                              || StringUtils.isBlank(userDepartmentInfo.getOrgName())))
              .collect(Collectors.toList());
      // 收集需要同步用户
      List<UserDepartmentInfo> syncList =
          departSyncList.stream()
              .filter(
                  userDepartmentInfo ->
                      StringUtils.isNotBlank(userDepartmentInfo.getUserName())
                          && StringUtils.isNotBlank(userDepartmentInfo.getOrgId())
                          && StringUtils.isNotBlank(userDepartmentInfo.getOrgName()))
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(errorUserList)) {
        alterList.addAll(errorUserList);
      }
      if (!CollectionUtils.isEmpty(syncList)) {
        // 同步用户
        List<UserDepartmentInfo> insertList = new ArrayList<>();
        syncList.forEach(
            departSyncInfo -> {
              UserDepartmentInfo userDepartmentInfo =
                  userDepartmentInfoMapper.selectUser(departSyncInfo.getUserName());
              if (null == userDepartmentInfo) {
                insertList.add(departSyncInfo);
              } else {
                if ((!departSyncInfo.getOrgId().equals(userDepartmentInfo.getOrgId()))
                    || (!departSyncInfo.getOrgName().equals(userDepartmentInfo.getOrgName()))) {
                  userDepartmentInfoMapper.updateUser(departSyncInfo);
                }
              }
            });
        if (!CollectionUtils.isEmpty(insertList)) {
          userDepartmentInfoMapper.batchInsertUsers(insertList);
        }
      }
    }
  }
}
