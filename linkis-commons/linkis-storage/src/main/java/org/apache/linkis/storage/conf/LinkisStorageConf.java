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

package org.apache.linkis.storage.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.utils.ByteTimeUtils;

import org.apache.commons.lang3.StringUtils;

public class LinkisStorageConf {
  private static final Object CONF_LOCK = new Object();

  public static final String DOLPHIN = "dolphin";

  public static final String PARQUET = "parquet";

  public static final String PARQUET_FILE_SUFFIX = ".parquet";

  public static final String ORC = "orc";

  public static final String ORC_FILE_SUFFIX = ".orc";

  public static final String HDFS_FILE_SYSTEM_REST_ERRS =
      CommonVars.apply(
              "wds.linkis.hdfs.rest.errs",
              ".*Filesystem closed.*|.*Failed to find any Kerberos tgt.*")
          .getValue();

  public static final String ROW_BYTE_MAX_LEN_STR =
      CommonVars.apply("wds.linkis.resultset.row.max.str", "2m").getValue();

  public static final String ENGINE_RESULT_TYPE =
      CommonVars.apply("linkis.engine.resultSet.type", DOLPHIN, "Result type").getValue();

  public static final long ROW_BYTE_MAX_LEN = ByteTimeUtils.byteStringAsBytes(ROW_BYTE_MAX_LEN_STR);

  public static final String FILE_TYPE =
      CommonVars.apply(
              "wds.linkis.storage.file.type",
              "dolphin,sql,scala,py,hql,python,out,log,text,txt,sh,jdbc,ngql,psql,fql,tsql"
                  + ","
                  + PARQUET
                  + ","
                  + ORC)
          .getValue();

  private static volatile String[] fileTypeArr = null;

  private static String[] fileTypeArrParser(String fileType) {
    if (StringUtils.isBlank(fileType)) {
      return new String[0];
    } else {
      return fileType.split(",");
    }
  }

  public static String[] getFileTypeArr() {
    if (fileTypeArr == null) {
      synchronized (CONF_LOCK) {
        if (fileTypeArr == null) {
          fileTypeArr = fileTypeArrParser(FILE_TYPE);
        }
      }
    }
    return fileTypeArr;
  }
}
