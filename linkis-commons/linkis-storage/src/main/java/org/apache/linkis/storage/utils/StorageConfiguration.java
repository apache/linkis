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

package org.apache.linkis.storage.utils;

import org.apache.linkis.common.conf.ByteType;
import org.apache.linkis.common.conf.CommonVars;

import java.util.List;

import com.google.common.collect.Lists;

public class StorageConfiguration {

  public static CommonVars<String> PROXY_USER =
      new CommonVars<>("wds.linkis.storage.proxy.user", "${UM}", null, null);

  public static CommonVars<String> STORAGE_ROOT_USER =
      new CommonVars<>("wds.linkis.storage.root.user", "hadoop", null, null);

  public static CommonVars<String> HDFS_ROOT_USER =
      new CommonVars<>("wds.linkis.storage.hdfs.root.user", "hadoop", null, null);

  public static CommonVars<String> LOCAL_ROOT_USER =
      new CommonVars<>("wds.linkis.storage.local.root.user", "root", null, null);

  public static CommonVars<String> STORAGE_USER_GROUP =
      new CommonVars<>("wds.linkis.storage.fileSystem.group", "bdap", null, null);

  public static CommonVars<String> STORAGE_RS_FILE_TYPE =
      new CommonVars<>("wds.linkis.storage.rs.file.type", "utf-8", null, null);

  public static CommonVars<String> STORAGE_RS_FILE_SUFFIX =
      new CommonVars<>("wds.linkis.storage.rs.file.suffix", ".dolphin", null, null);

  public static CommonVars<String> LINKIS_STORAGE_FS_LABEL =
      new CommonVars<>("linkis.storage.default.fs.label", "linkis-storage", null, null);

  public static List<String> ResultTypes =
      Lists.newArrayList("%TEXT", "%TABLE", "%HTML", "%IMG", "%ANGULAR", "%SVG");

  public static CommonVars<String> STORAGE_RESULT_SET_PACKAGE =
      new CommonVars<>(
          "wds.linkis.storage.result.set.package",
          "org.apache.linkis.storage.resultset",
          null,
          null);

  public static CommonVars<String> STORAGE_RESULT_SET_CLASSES =
      new CommonVars<>(
          "wds.linkis.storage.result.set.classes",
          "txt.TextResultSet,table.TableResultSet,io.IOResultSet,html.HtmlResultSet,picture.PictureResultSet",
          null,
          null);

  public static CommonVars<String> STORAGE_BUILD_FS_CLASSES =
      new CommonVars<>(
          "wds.linkis.storage.build.fs.classes",
          "org.apache.linkis.storage.factory.impl.BuildHDFSFileSystem,org.apache.linkis.storage.factory.impl.BuildLocalFileSystem,"
              + "org.apache.linkis.storage.factory.impl.BuildOSSSystem,org.apache.linkis.storage.factory.impl.BuildS3FileSystem",
          null,
          null);

  public static CommonVars<Boolean> IS_SHARE_NODE =
      new CommonVars<>("wds.linkis.storage.is.share.node", true, null, null);

  public static CommonVars<Boolean> ENABLE_IO_PROXY =
      new CommonVars<>("wds.linkis.storage.enable.io.proxy", false, null, null);

  public static CommonVars<String> IO_USER =
      new CommonVars<>("wds.linkis.storage.io.user", "root", null, null);
  public static CommonVars<Integer> IO_FS_EXPIRE_TIME =
      new CommonVars<>("wds.linkis.storage.io.fs.num", 1000 * 60 * 10, null, null);

  public static CommonVars<ByteType> IO_PROXY_READ_FETCH_SIZE =
      new CommonVars<>("wds.linkis.storage.io.read.fetch.size", new ByteType("100k"), null, null);

  public static CommonVars<ByteType> IO_PROXY_WRITE_CACHE_SIZE =
      new CommonVars<>("wds.linkis.storage.io.write.cache.size", new ByteType("64k"), null, null);

  public static CommonVars<String> IO_DEFAULT_CREATOR =
      new CommonVars<>("wds.linkis.storage.io.default.creator", "IDE", null, null);
  public static CommonVars<String> IO_FS_RE_INIT =
      new CommonVars<>("wds.linkis.storage.io.fs.re.init", "re-init", null, null);

  public static CommonVars<Integer> IO_INIT_RETRY_LIMIT =
      new CommonVars<>("wds.linkis.storage.io.init.retry.limit", 10, null, null);

  public static CommonVars<String> STORAGE_HDFS_GROUP =
      new CommonVars<>("wds.linkis.storage.fileSystem.hdfs.group", "hadoop", null, null);

  public static CommonVars<Integer> DOUBLE_FRACTION_LEN =
      new CommonVars<>("wds.linkis.double.fraction.length", 30, null, null);

  public static CommonVars<Boolean> HDFS_PATH_PREFIX_CHECK_ON =
      new CommonVars<>("wds.linkis.storage.hdfs.prefix_check.enable", true, null, null);

  public static CommonVars<Boolean> HDFS_PATH_PREFIX_REMOVE =
      new CommonVars<>("wds.linkis.storage.hdfs.prefxi.remove", true, null, null);

  public static CommonVars<Boolean> FS_CACHE_DISABLE =
      new CommonVars<>("wds.linkis.fs.hdfs.impl.disable.cache", false, null, null);

  public static CommonVars<Boolean> FS_CHECKSUM_DISBALE =
      new CommonVars<>("linkis.fs.hdfs.impl.disable.checksum", false, null, null);

  /**
   * more arguments please refer to:
   * https://hadoop.apache.org/docs/stable/hadoop-aliyun/tools/hadoop-aliyun/index.html Aliyun OSS
   * endpoint to connect to. eg: https://oss-cn-hangzhou.aliyuncs.com
   */
  public static CommonVars<String> OSS_ENDPOINT =
      new CommonVars<String>("wds.linkis.fs.oss.endpoint", "", null, null);

  /** Aliyun bucket name eg: benchmark2 */
  public static CommonVars<String> OSS_ACCESS_BUCKET_NAME =
      new CommonVars<String>("wds.linkis.fs.oss.bucket.name", "", null, null);

  /** Aliyun access key ID */
  public static CommonVars<String> OSS_ACCESS_KEY_ID =
      new CommonVars<String>("wds.linkis.fs.oss.accessKeyId", "", null, null);

  /** Aliyun access key secret */
  public static CommonVars<String> OSS_ACCESS_KEY_SECRET =
      new CommonVars<String>("wds.linkis.fs.oss.accessKeySecret", "", null, null);

  public static CommonVars<Boolean> OSS_PATH_PREFIX_CHECK_ON =
      new CommonVars<Boolean>("wds.linkis.storage.oss.prefix_check.enable", false, null, null);

  public static CommonVars<Boolean> OSS_PATH_PREFIX_REMOVE =
      new CommonVars<Boolean>("wds.linkis.storage.oss.prefix.remove", true, null, null);

  public static CommonVars<String> S3_ACCESS_KEY =
      new CommonVars("linkis.storage.s3.access.key", "", null, null);

  public static CommonVars<String> S3_SECRET_KEY =
      new CommonVars("linkis.storage.s3.secret.key", "", null, null);

  public static CommonVars<String> S3_ENDPOINT =
      new CommonVars("linkis.storage.s3.endpoint", "", null, null);

  public static CommonVars<String> S3_REGION =
      new CommonVars("linkis.storage.s3.region", "", null, null);

  public static CommonVars<String> S3_BUCKET =
      new CommonVars("linkis.storage.s3.bucket", "", null, null);
}
