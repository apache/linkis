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

package org.apache.linkis.ujes.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UJESSQLDriver extends UJESSQLDriverMain implements Driver {
  static {
    try {
      DriverManager.registerDriver(new UJESSQLDriver());
    } catch (SQLException e) {
      Logger logger = LoggerFactory.getLogger(UJESSQLDriver.class);
      logger.info("Load driver failed", e);
    }
  }

  static String URL_PREFIX = "jdbc:linkis://";
  static String URL_REGEX = "jdbc:linkis://([^:]+)(:\\d+)?(/[^\\?]+)?(\\?\\S*)?";

  static String HOST = "HOST";
  static String PORT = "PORT";
  static String DB_NAME = "DBNAME";
  static String PARAMS = "PARAMS";
  static String ENGINE_TYPE = "EngineType";
  static String ENGINE_VERSION = "EngineVersion";

  static String USER = "user";
  static String TOKEN_KEY = "key";
  static String TOKEN_VALUE = "value";
  static String PASSWORD = "password";
  static boolean TABLEAU_SERVER = false;
  static String FIXED_SESSION = "fixedSession";

  static String USE_SSL = "useSSL";
  static String VERSION = "version";
  static int DEFAULT_VERSION = 1;
  static String MAX_CONNECTION_SIZE = "maxConnectionSize";
  static String READ_TIMEOUT = "readTimeout";
  static String ENABLE_DISCOVERY = "enableDiscovery";
  static String ENABLE_LOADBALANCER = "enableLoadBalancer";
  static String CREATOR = "creator";
  static String TABLEAU = "tableau";
  static String VARIABLE_HEADER = "var:";
  static String PARAM_SPLIT = "&";
  static String KV_SPLIT = "=";
}
