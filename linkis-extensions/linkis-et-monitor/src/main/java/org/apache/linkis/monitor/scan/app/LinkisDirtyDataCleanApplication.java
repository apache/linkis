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

package org.apache.linkis.monitor.scan.app;

import org.apache.linkis.monitor.scan.app.dirtydata.entrance.EntranceDirtyDataHandler;
import org.apache.linkis.monitor.scan.utils.log.LogUtils;
import org.apache.linkis.server.utils.LinkisMainHelper;

import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import org.slf4j.Logger;

public class LinkisDirtyDataCleanApplication {
  private static final Logger logger = LogUtils.stdOutLogger();

  /** @param args: args[0]: host args[1] port */
  public static void main(String[] args) throws ReflectiveOperationException {
    if (args.length == 0
        || StringUtils.equalsIgnoreCase(args[0], "help")
        || StringUtils.equalsIgnoreCase(args[0], "--help")) {
      LogUtils.stdOutLogger()
          .info(
              "[help-message]this app cleans entrance dirty-data. args[0]: command-type (entrance/help/...) args[1]: entrance-hostname(not null), args[2]: entrance-port(can be null)");
      return;
    }
    String serviceName = System.getProperty(LinkisMainHelper.SERVER_NAME_KEY());
    LinkisMainHelper.formatPropertyFiles(serviceName);

    if (StringUtils.equalsIgnoreCase(args[0], "entrance")) {
      AbstractApplicationContext context =
          new AnnotationConfigApplicationContext(LinkisJobHistoryScanSpringConfiguration.class);

      String host = "";
      String port = "";
      if (args.length > 1) {
        host = args[1];
      }
      if (args.length > 2) {
        port = args[2];
      }
      if (args.length > 3) {
        printIllegalInput("wrong number of arguments");
        return;
      }
      try {
        removeDirtyEurekaInstance(host, port);
      } catch (Exception e) {
        LogUtils.stdOutLogger().error("Failed to remove dirty eureka-instance", e);
      }
      try {
        removeDbDirtyData(host, port);
      } catch (Exception e) {
        LogUtils.stdOutLogger().error("Failed to remove dirty db-data", e);
      }

      context.close();
    } else {
      LogUtils.stdOutLogger().error("Upsupported command type: " + args[0]);
    }
  }

  private static void printIllegalInput(String msg) {
    LogUtils.stdOutLogger().error("illegal input: " + msg);
    LogUtils.stdOutLogger()
        .info(
            "[help-message] this app cleans entrance dirty-data. args[0]: entrance-hostname, args[1]: entrance-port");
    return;
  }

  private static void removeDirtyEurekaInstance(String host, String port) {
    if (StringUtils.isBlank(host)) {
      printIllegalInput("host cannot be blank");
      return;
    }
    if (StringUtils.isBlank(port)) {
      EntranceDirtyDataHandler.handleEurekaDirtyData(host);
    } else {
      EntranceDirtyDataHandler.handleEurekaDirtyData(host, port);
    }
  }

  private static void removeDbDirtyData(String host, String port) {
    if (StringUtils.isBlank(host)) {
      printIllegalInput("host cannot be blank");
      return;
    }
    if (StringUtils.isBlank(port)) {
      EntranceDirtyDataHandler.handleDbDirtData(host);
    } else {
      EntranceDirtyDataHandler.handleDbDirtData(host, port);
    }
  }
}
