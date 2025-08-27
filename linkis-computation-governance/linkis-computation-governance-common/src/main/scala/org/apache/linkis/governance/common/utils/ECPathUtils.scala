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

package org.apache.linkis.governance.common.utils

import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils

import java.io.File
import java.nio.file.Paths

object ECPathUtils {

  def getECWOrkDirPathSuffix(
      user: String,
      ticketId: String,
      engineType: String,
      timeStamp: Long = System.currentTimeMillis()
  ): String = {
    val suffix = if (StringUtils.isBlank(engineType)) {
      Paths
        .get(user, DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd"))
        .toFile
        .getPath
    } else {
      Paths
        .get(user, DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd"), engineType)
        .toFile
        .getPath
    }
    suffix + File.separator + ticketId
  }

  def getECLogDirSuffix(
      engineTypeLabel: EngineTypeLabel,
      userCreatorLabel: UserCreatorLabel,
      ticketId: String
  ): String = {
    if (null == engineTypeLabel || null == userCreatorLabel) {
      return ""
    }
    val suffix = ECPathUtils.getECWOrkDirPathSuffix(
      userCreatorLabel.getUser,
      ticketId,
      engineTypeLabel.getEngineType
    )
    suffix + File.separator + "logs"
  }

}
