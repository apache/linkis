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

package org.apache.linkis.metadata.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DWSConfigTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String hiveConfDir = DWSConfig.HIVE_CONF_DIR.getValue();
    String metaUrl = DWSConfig.HIVE_META_URL.getValue();
    String metaUser = DWSConfig.HIVE_META_USER.getValue();
    String hiveMetaPassword = DWSConfig.HIVE_META_PASSWORD.getValue();

    Boolean encodeEnabled = DWSConfig.HIVE_PASS_ENCODE_ENABLED.getValue();
    Boolean hivePermissionWithLOGINUserEnabled =
        DWSConfig.HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED.getValue();
    String dbFilterKeywords = DWSConfig.DB_FILTER_KEYWORDS.getValue();
    String hiveDbAdminUser = DWSConfig.HIVE_DB_ADMIN_USER.getValue();
    String hdfsFileSystemRestErrs = DWSConfig.HDFS_FILE_SYSTEM_REST_ERRS;

    Assertions.assertNotNull(hiveConfDir);
    Assertions.assertNotNull(metaUrl);
    Assertions.assertNotNull(metaUser);
    Assertions.assertNotNull(hiveMetaPassword);

    Assertions.assertFalse(encodeEnabled.booleanValue());
    Assertions.assertTrue(hivePermissionWithLOGINUserEnabled.booleanValue());

    Assertions.assertNotNull(dbFilterKeywords);
    Assertions.assertNotNull(hiveDbAdminUser);
    Assertions.assertNotNull(hdfsFileSystemRestErrs);
  }
}
