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

package org.apache.linkis.storage.utils

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class StorageConfigurationTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val storagerootuser = StorageConfiguration.STORAGE_ROOT_USER.getValue
    val hdfsrootuser = StorageConfiguration.HDFS_ROOT_USER.getValue
    val localrootuser = StorageConfiguration.LOCAL_ROOT_USER.getValue
    val storageusergroup = StorageConfiguration.STORAGE_USER_GROUP.getValue
    val storagersfiletype = StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue
    val storagersfilesuffix = StorageConfiguration.STORAGE_RS_FILE_SUFFIX.getValue
    val types = StorageConfiguration.ResultTypes
    val storageresultsetpackage = StorageConfiguration.STORAGE_RESULT_SET_PACKAGE.getValue
    val storageresultsetclasses = StorageConfiguration.STORAGE_RESULT_SET_CLASSES.getValue
    val storagebuildfsclasses = StorageConfiguration.STORAGE_BUILD_FS_CLASSES.getValue
    val issharenode = StorageConfiguration.IS_SHARE_NODE.getValue
    val enableioproxy = StorageConfiguration.ENABLE_IO_PROXY.getValue
    val ioUser = StorageConfiguration.IO_USER.getValue
    val iofsexpiretime = StorageConfiguration.IO_FS_EXPIRE_TIME.getValue
    val iodefaultcreator = StorageConfiguration.IO_DEFAULT_CREATOR.getValue
    val iofsreinit = StorageConfiguration.IO_FS_RE_INIT.getValue
    val ioinitretrylimit = StorageConfiguration.IO_INIT_RETRY_LIMIT.getValue
    val storagehdfsgroup = StorageConfiguration.STORAGE_HDFS_GROUP.getValue
    val doublefractionlen = StorageConfiguration.DOUBLE_FRACTION_LEN.getValue
    val hdfspathprefixcheckon = StorageConfiguration.HDFS_PATH_PREFIX_CHECK_ON.getValue
    val hdfspathprefixremove = StorageConfiguration.HDFS_PATH_PREFIX_REMOVE.getValue
    val fscachedisable = StorageConfiguration.FS_CACHE_DISABLE.getValue
    val fschecksumdisbale = StorageConfiguration.FS_CHECKSUM_DISBALE.getValue

    Assertions.assertEquals("hadoop", storagerootuser)
    Assertions.assertEquals("hadoop", hdfsrootuser)
    Assertions.assertEquals("root", localrootuser)
    Assertions.assertEquals("bdap", storageusergroup)
    Assertions.assertEquals("utf-8", storagersfiletype)
    Assertions.assertEquals(".dolphin", storagersfilesuffix)
    Assertions.assertTrue(types.size > 0)
    Assertions.assertEquals("org.apache.linkis.storage.resultset", storageresultsetpackage)
    Assertions.assertEquals(
      "txt.TextResultSet,table.TableResultSet,io.IOResultSet,html.HtmlResultSet,picture.PictureResultSet",
      storageresultsetclasses
    )
    Assertions.assertEquals(
      "org.apache.linkis.storage.factory.impl.BuildHDFSFileSystem,org.apache.linkis.storage.factory.impl.BuildLocalFileSystem," +
        "org.apache.linkis.storage.factory.impl.BuildOSSSystem,org.apache.linkis.storage.factory.impl.BuildS3FileSystem",
      storagebuildfsclasses
    )
    Assertions.assertTrue(issharenode)
    Assertions.assertFalse(enableioproxy)
    Assertions.assertEquals("root", ioUser)
    Assertions.assertTrue(600000 == iofsexpiretime)
    Assertions.assertEquals("IDE", iodefaultcreator)
    Assertions.assertEquals("re-init", iofsreinit)
    Assertions.assertTrue(10 == ioinitretrylimit)
    Assertions.assertEquals("hadoop", storagehdfsgroup)
    Assertions.assertTrue(30 == doublefractionlen)
    Assertions.assertTrue(hdfspathprefixcheckon)
    Assertions.assertTrue(hdfspathprefixremove)
    Assertions.assertFalse(fscachedisable)
    Assertions.assertFalse(fschecksumdisbale)

  }

}
