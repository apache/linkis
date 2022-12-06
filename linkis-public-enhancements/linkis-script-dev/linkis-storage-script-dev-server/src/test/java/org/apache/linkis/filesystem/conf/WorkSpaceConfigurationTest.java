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

package org.apache.linkis.filesystem.conf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WorkSpaceConfigurationTest {

  @Test
  @DisplayName("staticCommonConst")
  public void staticCommonConst() {

    String localUserRootPath = WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue();
    String hdfsUserRootPathPrefix = WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue();
    String hdfsUserRootPathSuffix = WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_SUFFIX.getValue();
    Boolean resultSetDownloadIsLimit =
        WorkSpaceConfiguration.RESULT_SET_DOWNLOAD_IS_LIMIT.getValue();
    Integer resultSetDownloadMaxSizeCsv =
        WorkSpaceConfiguration.RESULT_SET_DOWNLOAD_MAX_SIZE_CSV.getValue();
    Integer resultSetDownloadMaxSizeExecl =
        WorkSpaceConfiguration.RESULT_SET_DOWNLOAD_MAX_SIZE_EXCEL.getValue();
    Long fileSystemGetTimeOut = WorkSpaceConfiguration.FILESYSTEM_GET_TIMEOUT.getValue();
    Integer fileSystemFsThreadNum = WorkSpaceConfiguration.FILESYSTEM_FS_THREAD_NUM.getValue();
    Integer fileSystemFsThreadCache = WorkSpaceConfiguration.FILESYSTEM_FS_THREAD_CACHE.getValue();
    Boolean filesystemPathCheckTrigger =
        WorkSpaceConfiguration.FILESYSTEM_PATH_CHECK_TRIGGER.getValue();
    Boolean filesystemPathCheckOwner =
        WorkSpaceConfiguration.FILESYSTEM_PATH_CHECK_OWNER.getValue();
    Boolean filesystemPathAutoCreate =
        WorkSpaceConfiguration.FILESYSTEM_PATH_AUTO_CREATE.getValue();
    Long localFilesystemUserRefreshInterval =
        WorkSpaceConfiguration.LOCAL_FILESYSTEM_USER_REFRESH_INTERVAL.getValue();
    Boolean enableUserGroup = WorkSpaceConfiguration.ENABLE_USER_GROUP.getValue();

    Assertions.assertNotNull(localUserRootPath);
    Assertions.assertNotNull(hdfsUserRootPathPrefix);
    Assertions.assertNotNull(hdfsUserRootPathSuffix);
    Assertions.assertTrue(resultSetDownloadIsLimit.booleanValue());
    Assertions.assertTrue(resultSetDownloadMaxSizeCsv.intValue() == 5000);
    Assertions.assertTrue(resultSetDownloadMaxSizeExecl == 5000);
    Assertions.assertTrue(fileSystemGetTimeOut == 10000L);
    Assertions.assertTrue(fileSystemFsThreadNum == 10);
    Assertions.assertTrue(fileSystemFsThreadCache == 1000);
    Assertions.assertFalse(filesystemPathCheckTrigger);
    Assertions.assertFalse(filesystemPathCheckOwner.booleanValue());
    Assertions.assertFalse(filesystemPathAutoCreate.booleanValue());
    Assertions.assertTrue(localFilesystemUserRefreshInterval == 1800000L);
    Assertions.assertTrue(enableUserGroup.booleanValue());
  }
}
