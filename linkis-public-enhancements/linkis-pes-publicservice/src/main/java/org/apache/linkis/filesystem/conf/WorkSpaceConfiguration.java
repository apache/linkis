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

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.CommonVars$;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkSpaceConfiguration {
  public static final CommonVars<String> LOCAL_USER_ROOT_PATH =
      CommonVars$.MODULE$.apply("wds.linkis.filesystem.root.path", "file:///tmp/linkis/");
  public static final CommonVars<String> HDFS_USER_ROOT_PATH_PREFIX =
      CommonVars$.MODULE$.apply("wds.linkis.filesystem.hdfs.root.path", "hdfs:///tmp/");
  public static final CommonVars<String> HDFS_USER_ROOT_PATH_SUFFIX =
      CommonVars$.MODULE$.apply(
          "wds.linkis.workspace.filesystem.hdfsuserrootpath.suffix", "/linkis/");
  public static final CommonVars<Boolean> RESULT_SET_DOWNLOAD_IS_LIMIT =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.resultset.download.is.limit", true);
  public static final CommonVars<Integer> RESULT_SET_DOWNLOAD_MAX_SIZE_CSV =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.resultset.download.maxsize.csv", 5000);
  public static final CommonVars<Integer> RESULT_SET_DOWNLOAD_MAX_SIZE_EXCEL =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.resultset.download.maxsize.excel", 5000);
  public static final CommonVars<Long> FILESYSTEM_GET_TIMEOUT =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.filesystem.get.timeout", 10000L);
  public static final CommonVars<Integer> FILESYSTEM_FS_THREAD_NUM =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.filesystem.thread.num", 10);
  public static final CommonVars<Integer> FILESYSTEM_FS_THREAD_CACHE =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.filesystem.thread.cache", 1000);
  public static final CommonVars<Boolean> FILESYSTEM_PATH_CHECK_TRIGGER =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.filesystem.path.check", false);

  public static final CommonVars<Boolean> FILESYSTEM_PATH_CHECK_OWNER =
      CommonVars$.MODULE$.apply("wds.linkis.workspace.filesystem.owner.check", false);

  public static final CommonVars<Boolean> FILESYSTEM_PATH_AUTO_CREATE =
      CommonVars$.MODULE$.apply("linkis.workspace.filesystem.auto.create", false);

  public static final CommonVars<Long> LOCAL_FILESYSTEM_USER_REFRESH_INTERVAL =
      CommonVars$.MODULE$.apply(
          "linkis.filesystem.local.usermap.refresh.interval.mills", 30 * 60 * 1000L);

  public static final CommonVars<String> FILESYSTEM_FILE_CHECK_SIZE =
      CommonVars$.MODULE$.apply("linkis.filesystem.file.size.limit", "30M");

  public static final CommonVars<Boolean> ENABLE_USER_GROUP =
      CommonVars$.MODULE$.apply("linkis.os.user.group.enabled", true);

  public static final CommonVars<Integer> FILESYSTEM_RESULTSET_ROW_LIMIT =
      CommonVars$.MODULE$.apply("linkis.filesystem.resultset.row.limit", 100000);

  public static final CommonVars<Integer> FILESYSTEM_RESULT_SET_COLUMN_LIMIT =
      CommonVars$.MODULE$.apply("linkis.filesystem.result.set.column.limit", 10000);

  public static final CommonVars<Boolean> FILESYSTEM_JVM_USER_SWITCH =
      CommonVars$.MODULE$.apply("linkis.filesystem.jvm.user.switch", true);

  public static final CommonVars<String> LINKIS_KEYTAB_FILE_OWNER =
      CommonVars$.MODULE$.apply("linkis.keytab.file.owner", "hadoop");

  public static final CommonVars<String> LINKIS_KEYTAB_FILE_PEIMISSION =
      CommonVars$.MODULE$.apply("linkis.keytab.file.permission", "640");

  public static final ExecutorService executorService =
      new ThreadPoolExecutor(
          FILESYSTEM_FS_THREAD_NUM.getValue(),
          FILESYSTEM_FS_THREAD_NUM.getValue(),
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>(FILESYSTEM_FS_THREAD_CACHE.getValue()));
}
