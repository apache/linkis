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

package org.apache.linkis.metadata.query.service;

import org.apache.linkis.common.conf.CommonVars;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.hadoop.fs.FileSystem.FS_DEFAULT_NAME_KEY;

public class HiveConnection implements Closeable {

  private Hive hiveClient;

  private IMetaStoreClient metaStoreClient;

  private static final CommonVars<String> KERBEROS_DEFAULT_PRINCIPLE =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.kerberos.principle", "hadoop/_HOST@EXAMPLE.COM");

  private static final CommonVars<String> DEFAULT_SERVICE_USER =
      CommonVars.apply("wds.linkis.server.mdm.service.user", "hadoop");

  public HiveConnection(
      String uris, String principle, String keytabFilePath, Map<String, String> hadoopConf)
      throws Exception {
    final HiveConf conf = new HiveConf();
    conf.setVar(HiveConf.ConfVars.METASTOREURIS, uris);
    conf.setVar(HiveConf.ConfVars.METASTORE_USE_THRIFT_SASL, "true");
    conf.setVar(
        HiveConf.ConfVars.METASTORE_KERBEROS_PRINCIPAL, KERBEROS_DEFAULT_PRINCIPLE.getValue());
    // Disable the cache in FileSystem
    conf.setBoolean(
        String.format(
            "fs.%s.impl.disable.cache", URI.create(conf.get(FS_DEFAULT_NAME_KEY, "")).getScheme()),
        true);
    conf.set("hadoop.security.authentication", "kerberos");
    hadoopConf.forEach(conf::set);
    principle = principle.substring(0, principle.indexOf("@"));
    UserGroupInformation ugi =
        UserGroupInformationWrapper.loginUserFromKeytab(conf, principle, keytabFilePath);
    hiveClient = getHive(ugi, conf);
  }

  public HiveConnection(String uris, Map<String, String> hadoopConf) throws Exception {
    final HiveConf conf = new HiveConf();
    conf.setVar(HiveConf.ConfVars.METASTOREURIS, uris);
    hadoopConf.forEach(conf::set);
    // Disable the cache in FileSystem
    conf.setBoolean(
        String.format(
            "fs.%s.impl.disable.cache", URI.create(conf.get(FS_DEFAULT_NAME_KEY, "")).getScheme()),
        true);
    // TODO choose an authentication strategy for hive, and then use createProxyUser
    UserGroupInformation ugi =
        UserGroupInformation.createRemoteUser(DEFAULT_SERVICE_USER.getValue());
    hiveClient = getHive(ugi, conf);
  }
  /**
   * Get Hive client(Hive object)
   *
   * @return hive
   */
  public Hive getClient() {
    return hiveClient;
  }

  private Hive getHive(UserGroupInformation ugi, HiveConf conf)
      throws IOException, InterruptedException {
    return ugi.doAs(
        (PrivilegedExceptionAction<Hive>)
            () -> {
              Hive hive = Hive.get(conf);
              metaStoreClient = hive.getMSC();
              // To remove thread Local vars
              Hive.set(null);
              return hive;
            });
  }

  @Override
  public void close() throws IOException {
    // Close meta store client
    metaStoreClient.close();
  }

  /** Wrapper class of UserGroupInformation */
  private static class UserGroupInformationWrapper {
    private static ReentrantLock globalLock = new ReentrantLock();

    public static UserGroupInformation loginUserFromKeytab(
        final Configuration conf, String user, String path) throws Exception {
      globalLock.lock();
      try {
        UserGroupInformation.setConfiguration(conf);
        return UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path);
      } finally {
        globalLock.unlock();
      }
    }

    public static UserGroupInformation createProxyUser(final Configuration conf, String user)
        throws Exception {
      globalLock.lock();
      try {
        UserGroupInformation.setLoginUser(null);
        UserGroupInformation.setConfiguration(conf);
        return UserGroupInformation.createProxyUser(user, UserGroupInformation.getLoginUser());
      } finally {
        globalLock.unlock();
      }
    }

    public static UserGroupInformation getLoginUser() throws Exception {
      globalLock.lock();
      try {
        UserGroupInformation.setLoginUser(null);
        return UserGroupInformation.getLoginUser();
      } finally {
        globalLock.unlock();
      }
    }
  }
}
