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

package org.apache.linkis.datasource.client.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DatasourceClientConfigTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String metaDataService = DatasourceClientConfig.METADATA_SERVICE_MODULE().getValue();
    String dataSourceService = DatasourceClientConfig.DATA_SOURCE_SERVICE_MODULE().getValue();
    String authTokenKey = DatasourceClientConfig.AUTH_TOKEN_KEY().getValue();
    String authTokenValue = DatasourceClientConfig.AUTH_TOKEN_VALUE().getValue();
    String dataSourceClientName =
        DatasourceClientConfig.DATA_SOURCE_SERVICE_CLIENT_NAME().getValue();
    Object connectionMaxSize = DatasourceClientConfig.CONNECTION_MAX_SIZE().getValue();
    Object connectionTimeOut = DatasourceClientConfig.CONNECTION_TIMEOUT().getValue();
    Object connectionReadTimeOut = DatasourceClientConfig.CONNECTION_READ_TIMEOUT().getValue();

    Assertions.assertNotNull(metaDataService);
    Assertions.assertNotNull(dataSourceService);
    Assertions.assertNotNull(authTokenKey);
    Assertions.assertNotNull(authTokenValue);
    Assertions.assertNotNull(dataSourceClientName);
    Assertions.assertNotNull(connectionMaxSize);
    Assertions.assertNotNull(connectionTimeOut);
    Assertions.assertNotNull(connectionReadTimeOut);
  }
}
