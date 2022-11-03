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

package org.apache.linkis.engineplugin.elasticsearch.conf

import org.apache.linkis.common.conf.TimeType

import org.junit.jupiter.api.{Assertions, Test}

class TestElasticSearchConfiguration {

  @Test
  def testConfig: Unit = {

    Assertions.assertEquals("127.0.0.1:9200", ElasticSearchConfiguration.ES_CLUSTER.getValue)
    Assertions.assertEquals(
      "default_datasource",
      ElasticSearchConfiguration.ES_DATASOURCE_NAME.getValue
    )
    Assertions.assertEquals(false, ElasticSearchConfiguration.ES_AUTH_CACHE.getValue)
    Assertions.assertEquals(false, ElasticSearchConfiguration.ES_SNIFFER_ENABLE.getValue)
    Assertions.assertEquals("GET", ElasticSearchConfiguration.ES_HTTP_METHOD.getValue)
    Assertions.assertEquals("/_search", ElasticSearchConfiguration.ES_HTTP_ENDPOINT.getValue)
    Assertions.assertEquals("/_sql", ElasticSearchConfiguration.ES_HTTP_SQL_ENDPOINT.getValue)
    Assertions.assertEquals(100, ElasticSearchConfiguration.ENTRANCE_MAX_JOB_INSTANCE.getValue)
    Assertions.assertEquals(20, ElasticSearchConfiguration.ENTRANCE_PROTECTED_JOB_INSTANCE.getValue)
    Assertions.assertEquals(5000, ElasticSearchConfiguration.ENGINE_DEFAULT_LIMIT.getValue)
    Assertions.assertEquals(100, ElasticSearchConfiguration.ENGINE_CONCURRENT_LIMIT.getValue)
    Assertions.assertEquals("7.6.2", ElasticSearchConfiguration.DEFAULT_VERSION.getValue)
  }

}
