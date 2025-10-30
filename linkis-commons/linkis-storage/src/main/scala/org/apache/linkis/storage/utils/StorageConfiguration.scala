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

import org.apache.linkis.common.conf.{ByteType, CommonVars}

object StorageConfiguration {

  val PROXY_USER = CommonVars("wds.linkis.storage.proxy.user", "${UM}")

  val STORAGE_ROOT_USER = CommonVars("wds.linkis.storage.root.user", "hadoop")

  val HDFS_ROOT_USER = CommonVars("wds.linkis.storage.hdfs.root.user", "hadoop")

  val LOCAL_ROOT_USER = CommonVars("wds.linkis.storage.local.root.user", "root")

  val STORAGE_USER_GROUP = CommonVars("wds.linkis.storage.fileSystem.group", "bdap")

  val STORAGE_RS_FILE_TYPE = CommonVars("wds.linkis.storage.rs.file.type", "utf-8")

  val STORAGE_RS_FILE_SUFFIX = CommonVars("wds.linkis.storage.rs.file.suffix", ".dolphin")

  val LINKIS_STORAGE_FS_LABEL = CommonVars("linkis.storage.default.fs.label", "linkis-storage")

  val ResultTypes = List("%TEXT", "%TABLE", "%HTML", "%IMG", "%ANGULAR", "%SVG")

  val STORAGE_RESULT_SET_PACKAGE =
    CommonVars("wds.linkis.storage.result.set.package", "org.apache.linkis.storage.resultset")

  val STORAGE_RESULT_SET_CLASSES = CommonVars(
    "wds.linkis.storage.result.set.classes",
    "txt.TextResultSet,table.TableResultSet,io.IOResultSet,html.HtmlResultSet,picture.PictureResultSet"
  )

  val STORAGE_BUILD_FS_CLASSES = CommonVars(
    "wds.linkis.storage.build.fs.classes",
    "org.apache.linkis.storage.factory.impl.BuildHDFSFileSystem,org.apache.linkis.storage.factory.impl.BuildLocalFileSystem," +
      "org.apache.linkis.storage.factory.impl.BuildOSSSystem,org.apache.linkis.storage.factory.impl.BuildS3FileSystem," +
      "org.apache.linkis.storage.factory.impl.BuildAzureBlobFileSystem"
  )

  val IS_SHARE_NODE = CommonVars("wds.linkis.storage.is.share.node", true)

  val ENABLE_IO_PROXY = CommonVars("wds.linkis.storage.enable.io.proxy", false)

  val IO_USER = CommonVars("wds.linkis.storage.io.user", "root")
  val IO_FS_EXPIRE_TIME = CommonVars("wds.linkis.storage.io.fs.num", 1000 * 60 * 10)

  val IO_PROXY_READ_FETCH_SIZE =
    CommonVars("wds.linkis.storage.io.read.fetch.size", new ByteType("100k"))

  val IO_PROXY_WRITE_CACHE_SIZE =
    CommonVars("wds.linkis.storage.io.write.cache.size", new ByteType("64k"))

  val IO_DEFAULT_CREATOR = CommonVars("wds.linkis.storage.io.default.creator", "IDE")
  val IO_FS_RE_INIT = CommonVars("wds.linkis.storage.io.fs.re.init", "re-init")

  val IO_INIT_RETRY_LIMIT = CommonVars("wds.linkis.storage.io.init.retry.limit", 10)

  val STORAGE_HDFS_GROUP = CommonVars("wds.linkis.storage.fileSystem.hdfs.group", "hadoop")

  val DOUBLE_FRACTION_LEN = CommonVars[Int]("wds.linkis.double.fraction.length", 30)

  val HDFS_PATH_PREFIX_CHECK_ON =
    CommonVars[Boolean]("wds.linkis.storage.hdfs.prefix_check.enable", true)

  val HDFS_PATH_PREFIX_REMOVE = CommonVars[Boolean]("wds.linkis.storage.hdfs.prefxi.remove", true)

  val FS_CHECKSUM_DISBALE =
    CommonVars[Boolean]("linkis.fs.hdfs.impl.disable.checksum", false).getValue

  /**
   * more arguments please refer to:
   * https://hadoop.apache.org/docs/stable/hadoop-aliyun/tools/hadoop-aliyun/index.html Aliyun OSS
   * endpoint to connect to. eg: https://oss-cn-hangzhou.aliyuncs.com
   */
  val OSS_ENDPOINT = new CommonVars[String]("wds.linkis.fs.oss.endpoint", "", null, null)

  /** Aliyun bucket name eg: benchmark2 */
  val OSS_ACCESS_BUCKET_NAME =
    CommonVars[String]("wds.linkis.fs.oss.bucket.name", "", null, null)

  /** Aliyun access key ID */
  val OSS_ACCESS_KEY_ID = new CommonVars[String]("wds.linkis.fs.oss.accessKeyId", "", null, null)

  /** Aliyun access key secret */
  val OSS_ACCESS_KEY_SECRET =
    CommonVars[String]("wds.linkis.fs.oss.accessKeySecret", "", null, null)

  val OSS_PATH_PREFIX_CHECK_ON =
    CommonVars[Boolean]("wds.linkis.storage.oss.prefix_check.enable", false)

  val OSS_PATH_PREFIX_REMOVE =
    CommonVars[Boolean]("wds.linkis.storage.oss.prefix.remove", true)

  val S3_ACCESS_KEY = CommonVars[String]("linkis.storage.s3.access.key", "", null, null)

  val S3_SECRET_KEY = CommonVars[String]("linkis.storage.s3.secret.key", "", null, null)

  val S3_ENDPOINT = CommonVars[String]("linkis.storage.s3.endpoint", "", null, null)

  val S3_REGION = CommonVars[String]("linkis.storage.s3.region", "", null, null)

  val S3_BUCKET = CommonVars[String]("linkis.storage.s3.bucket", "", null, null)

  val AZURE_ACCT_NAME = CommonVars[String]("linkis.storage.azure.acctName", "", null, null)

  val AZURE_ACCT_CONNECT_STR = CommonVars[String]("linkis.storage.azure.connectstr", "", null, null)
}
