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

package org.apache.linkis.scheduler.util;

import org.apache.linkis.scheduler.conf.SchedulerConfiguration;

import org.apache.commons.lang3.StringUtils;

public class SchedulerUtils {
  private static final String EVENT_ID_SPLIT = "_";
  private static final String ALL_CREATORS = "ALL_CREATORS";
  private static final String SPACIAL_USER_SPLIT = "_v_";

  /**
   * support priority queue with config username or creator
   *
   * @param groupName
   * @return
   */
  public static boolean isSupportPriority(String groupName) {
    String users = SchedulerConfiguration.SUPPORT_PRIORITY_TASK_USERS();
    if (StringUtils.isEmpty(users)) {
      return false;
    }
    String userName = getUserFromGroupName(groupName);
    if (StringUtils.isEmpty(userName)) {
      return false;
    }
    String creators = SchedulerConfiguration.SUPPORT_PRIORITY_TASK_CREATORS();
    creators = creators.toLowerCase();
    users = users.toLowerCase();
    if (ALL_CREATORS.equalsIgnoreCase(creators)) {
      return users.contains(userName.toLowerCase());
    } else {
      String creatorName = getCreatorFromGroupName(groupName);
      return users.contains(userName.toLowerCase()) && creators.contains(creatorName.toLowerCase());
    }
  }

  public static String getUserFromGroupName(String groupName) {
    if (groupName.contains(SPACIAL_USER_SPLIT)) {
      int vIndex = groupName.lastIndexOf(SPACIAL_USER_SPLIT);
      int lastIndex = groupName.lastIndexOf(EVENT_ID_SPLIT);
      String user = groupName.substring(vIndex + 1, lastIndex);
      return user;
    }
    String[] groupNames = groupName.split(EVENT_ID_SPLIT);
    String user = groupNames[groupNames.length - 2];
    return user;
  }

  public static String getEngineTypeFromGroupName(String groupName) {
    String[] groupNames = groupName.split(EVENT_ID_SPLIT);
    String ecType = groupNames[groupNames.length - 1];
    return ecType;
  }

  public static String getCreatorFromGroupName(String groupName) {
    if (groupName.contains(SPACIAL_USER_SPLIT)) {
      int vIndex = groupName.lastIndexOf(SPACIAL_USER_SPLIT);
      String creatorName = groupName.substring(0, vIndex);
      return creatorName;
    }
    int lastIndex = groupName.lastIndexOf(EVENT_ID_SPLIT);
    int secondLastIndex = groupName.lastIndexOf(EVENT_ID_SPLIT, lastIndex - 1);
    String creatorName = groupName.substring(0, secondLastIndex);
    return creatorName;
  }
}
