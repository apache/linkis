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

package org.apache.linkis.udf.utils

import org.apache.linkis.common.conf.CommonVars

import java.util.regex.Pattern

object UdfConfiguration {

  val UDF_HIVE_EXEC_PATH = CommonVars(
    "wds.linkis.udf.hive.exec.path",
    "/appcom/Install/DataWorkCloudInstall/linkis-linkis-Udf-0.0.3-SNAPSHOT/lib/hive-exec-1.2.1.jar"
  )

  val UDF_TMP_PATH = CommonVars("wds.linkis.udf.tmp.path", "/tmp/udf/")
  val UDF_SHARE_PATH = CommonVars("wds.linkis.udf.share.path", "/mnt/bdap/udf/")
  val UDF_SHARE_PROXY_USER = CommonVars("wds.linkis.udf.share.proxy.user", "hadoop")

  val NAME_REGEX: String = "^[a-zA-Z\\-\\d_\\.=/,]+$"

  val nameRegexPattern: Pattern = Pattern.compile(NAME_REGEX)

}
