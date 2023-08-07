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

package org.apache.linkis.datasourcemanager.common.auth;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Auth context */
public class AuthContext {

  public static CommonVars<String> AUTH_ADMINISTRATOR =
      CommonVars.apply("wds.linkis.server.dsm.auth.admin", "hadoop");

  public static final String AUTH_SEPARATOR = ",";

  private static List<String> administrators = new ArrayList<>();

  static {
    String adminStr = AUTH_ADMINISTRATOR.getValue();
    if (StringUtils.isNotBlank(adminStr)) {
      for (String admin : adminStr.split(AUTH_SEPARATOR)) {
        if (StringUtils.isNotBlank(admin)) {
          administrators.add(admin);
        }
      }
    }
  }

  public static boolean hasPermission(DataSource dataSource, String username) {
    if (Objects.nonNull(dataSource)) {
      String creator = dataSource.getCreateUser();
      return (administrators.contains(username)
          || (StringUtils.isNotBlank(creator) && username.equals(creator)));
    }
    return false;
  }

  /**
   * If is admin
   *
   * @param username username
   * @return boolean
   */
  public static boolean isAdministrator(String username) {
    return administrators.contains(username);
  }
}
