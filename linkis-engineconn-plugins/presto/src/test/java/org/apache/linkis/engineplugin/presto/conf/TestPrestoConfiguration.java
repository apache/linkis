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

package org.apache.linkis.engineplugin.presto.conf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPrestoConfiguration {

  @Test
  public void testConfig() {
    Assertions.assertEquals(100, PrestoConfiguration.ENGINE_CONCURRENT_LIMIT.getValue());
    Assertions.assertEquals(60, PrestoConfiguration.PRESTO_HTTP_CONNECT_TIME_OUT.getValue());
    Assertions.assertEquals(60, PrestoConfiguration.PRESTO_HTTP_READ_TIME_OUT.getValue());
    Assertions.assertEquals(5000, PrestoConfiguration.ENGINE_DEFAULT_LIMIT.getValue());
    Assertions.assertEquals("http://127.0.0.1:8080", PrestoConfiguration.PRESTO_URL.getValue());
    Assertions.assertEquals("", PrestoConfiguration.PRESTO_RESOURCE_CONFIG_PATH.getValue());
    Assertions.assertEquals("default", PrestoConfiguration.PRESTO_USER_NAME.getValue());
    Assertions.assertEquals("", PrestoConfiguration.PRESTO_PASSWORD.getValue());
    Assertions.assertEquals("system", PrestoConfiguration.PRESTO_CATALOG.getValue());
    Assertions.assertEquals("", PrestoConfiguration.PRESTO_SCHEMA.getValue());
    Assertions.assertEquals("global", PrestoConfiguration.PRESTO_SOURCE.getValue());
    Assertions.assertEquals("8GB", PrestoConfiguration.PRESTO_REQUEST_MEMORY.getValue());
  }
}
