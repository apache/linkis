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

package org.apache.linkis.hadoop.common.utils;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.hadoop.common.conf.HadoopConf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHENTICATION;
import static org.apache.hadoop.security.UserGroupInformation.AuthenticationMethod.KERBEROS;

public class KerberosUtils {
  private static final Logger LOG = LoggerFactory.getLogger(KerberosUtils.class);

  private static boolean kerberosRefreshStarted = false;

  private static final Object kerberosRefreshLock = new Object();

  private KerberosUtils() {}

  private static Configuration createKerberosSecurityConfiguration() {
    Configuration conf = HDFSUtils.getConfiguration(HadoopConf.HADOOP_ROOT_USER().getValue());
    conf.set(HADOOP_SECURITY_AUTHENTICATION, KERBEROS.toString());
    return conf;
  }

  public static void createKerberosSecureConfiguration(String keytab, String principal) {
    Configuration conf = createKerberosSecurityConfiguration();
    UserGroupInformation.setConfiguration(conf);
    try {
      if (!UserGroupInformation.isSecurityEnabled()
          || UserGroupInformation.getCurrentUser().getAuthenticationMethod() != KERBEROS
          || !UserGroupInformation.isLoginKeytabBased()) {
        UserGroupInformation.loginUserFromKeytab(principal, keytab);
        LOG.info("Login successfully with keytab: {} and principal: {}", keytab, principal);
      } else {
        LOG.info("The user has already logged in using keytab and principal, no action required");
      }
    } catch (IOException e) {
      LOG.error("Failed to get either keytab location or principal name in the jdbc executor", e);
    }
  }

  public static boolean runRefreshKerberosLogin() {
    Configuration conf = createKerberosSecurityConfiguration();
    UserGroupInformation.setConfiguration(conf);
    try {
      if (UserGroupInformation.isLoginKeytabBased()) {
        LOG.debug("Trying re-login from keytab");
        UserGroupInformation.getLoginUser().reloginFromKeytab();
        return true;
      } else if (UserGroupInformation.isLoginTicketBased()) {
        LOG.debug("Trying re-login from ticket cache");
        UserGroupInformation.getLoginUser().reloginFromTicketCache();
        return true;
      }
    } catch (Exception e) {
      LOG.error("Unable to run kinit for linkis jdbc executor", e);
    }
    LOG.debug(
        "Neither Keytab nor ticket based login. runRefreshKerberosLoginWork() returning false");
    return false;
  }

  public static Long getKerberosRefreshInterval() {
    long refreshInterval;
    String refreshIntervalString = "43200";
    // defined in linkis-env.sh, if not initialized then the default value is 43200 s (0.5d).
    if (System.getenv("LINKIS_KERBEROS_REFRESH_INTERVAL") != null) {
      refreshIntervalString = System.getenv("LINKIS_KERBEROS_REFRESH_INTERVAL");
    }
    try {
      refreshInterval = Long.parseLong(refreshIntervalString);
    } catch (NumberFormatException e) {
      LOG.error(
          "Cannot get time in S for the given string, "
              + refreshIntervalString
              + " defaulting to 43200 ",
          e);
      refreshInterval = 43200;
    }
    return refreshInterval;
  }

  public static Integer kinitFailTimesThreshold() {
    Integer kinitFailThreshold = 5;
    // defined in linkis-env.sh, if not initialized then the default value is 5.
    if (System.getenv("LINKIS_KERBEROS_KINIT_FAIL_THRESHOLD") != null) {
      try {
        kinitFailThreshold = new Integer(System.getenv("LINKIS_KERBEROS_KINIT_FAIL_THRESHOLD"));
      } catch (Exception e) {
        LOG.error(
            "Cannot get integer value from the given string, "
                + System.getenv("LINKIS_KERBEROS_KINIT_FAIL_THRESHOLD")
                + " defaulting to "
                + kinitFailThreshold,
            e);
      }
    }
    return kinitFailThreshold;
  }

  public static void checkStatus() {
    try {
      LOG.info("isSecurityEnabled:" + UserGroupInformation.isSecurityEnabled());
      LOG.info(
          "userAuthenticationMethod:"
              + UserGroupInformation.getLoginUser().getAuthenticationMethod());
      UserGroupInformation loginUsr = UserGroupInformation.getLoginUser();
      UserGroupInformation curUsr = UserGroupInformation.getCurrentUser();
      LOG.info("LoginUser: " + loginUsr);
      LOG.info("CurrentUser: " + curUsr);
      if (curUsr == null) {
        LOG.info("CurrentUser is null");
      } else {
        LOG.info("CurrentUser is not null");
      }
      if (loginUsr.getClass() != curUsr.getClass()) {
        LOG.info("getClass() is different");
      } else {
        LOG.info("getClass() is same");
      }
      if (loginUsr.equals(curUsr)) {
        LOG.info("subject is equal");
      } else {
        LOG.info("subject is not equal");
      }
    } catch (Exception e) {
      LOG.error("UGI error: ", e.getMessage());
    }
  }

  public static void startKerberosRefreshThread() {

    if (kerberosRefreshStarted || !HadoopConf.KERBEROS_ENABLE()) {
      LOG.warn(
          "kerberos refresh thread had start or not kerberos {}", HadoopConf.HDFS_ENABLE_CACHE());
      return;
    }
    synchronized (kerberosRefreshLock) {
      if (kerberosRefreshStarted) {
        LOG.warn("kerberos refresh thread had start");
        return;
      }
      kerberosRefreshStarted = true;
      LOG.info("kerberos Refresh tread started");
      Utils.defaultScheduler()
          .scheduleAtFixedRate(
              () -> {
                try {
                  checkStatus();
                  if (UserGroupInformation.isLoginKeytabBased()) {
                    LOG.info("Trying re-login from keytab");
                    UserGroupInformation.getLoginUser().checkTGTAndReloginFromKeytab();
                  } else if (UserGroupInformation.isLoginTicketBased()) {
                    LOG.info("Trying re-login from ticket cache");
                    UserGroupInformation.getLoginUser().reloginFromTicketCache();
                  }
                } catch (Exception e) {
                  LOG.error("Unable to re-login", e);
                }
              },
              getKerberosRefreshInterval(),
              getKerberosRefreshInterval(),
              TimeUnit.SECONDS);
    }
  }
}
