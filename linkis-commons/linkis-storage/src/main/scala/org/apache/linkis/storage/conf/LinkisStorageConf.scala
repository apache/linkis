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

package org.apache.linkis.storage.conf

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.ByteTimeUtils

import org.apache.commons.lang3.StringUtils

object LinkisStorageConf {
  private val CONF_LOCK = new Object()

  val HDFS_FILE_SYSTEM_REST_ERRS: String =
    CommonVars
      .apply(
        "wds.linkis.hdfs.rest.errs",
        ".*Filesystem closed.*|.*Failed to find any Kerberos tgt.*"
      )
      .getValue

  val ROW_BYTE_MAX_LEN_STR = CommonVars("wds.linkis.resultset.row.max.str", "2m").getValue

  val ROW_BYTE_MAX_LEN = ByteTimeUtils.byteStringAsBytes(ROW_BYTE_MAX_LEN_STR)

  val FILE_TYPE = CommonVars(
    "wds.linkis.storage.file.type",
    "dolphin,sql,scala,py,hql,python,out,log,text,sh,jdbc,ngql,psql,fql,tsql"
  ).getValue

  private var fileTypeArr: Array[String] = null

  private def fileTypeArrParser(fileType: String): Array[String] = {
    if (StringUtils.isBlank(fileType)) Array()
    else fileType.split(",")
  }

  def getFileTypeArr: Array[String] = {
    if (fileTypeArr == null) {
      CONF_LOCK.synchronized {
        if (fileTypeArr == null) {
          fileTypeArr = fileTypeArrParser(FILE_TYPE)
        }
      }
    }
    fileTypeArr
  }

}
