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

package org.apache.linkis.governance.common.utils;

import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.nio.file.Paths;

public class ECPathUtils {

  public static String getECWOrkDirPathSuffix(String user, String ticketId, String engineType) {
    String engineTypeRes = "";
    if (StringUtils.isNotBlank(engineType)) {
      engineTypeRes = engineType;
    }
    File file =
        Paths.get(
                user, DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd"), engineTypeRes)
            .toFile();
    return file.getPath() + File.separator + ticketId;
  }

  public static String getECLogDirSuffix(
      EngineTypeLabel engineTypeLabel, UserCreatorLabel userCreatorLabel, String ticketId) {
    if (null == engineTypeLabel || null == userCreatorLabel) {
      return "";
    }
    String ecwOrkDirPathSuffix =
        ECPathUtils.getECWOrkDirPathSuffix(
            userCreatorLabel.getUser(), ticketId, engineTypeLabel.getEngineType());
    return ecwOrkDirPathSuffix + File.separator + "logs";
  }
}
