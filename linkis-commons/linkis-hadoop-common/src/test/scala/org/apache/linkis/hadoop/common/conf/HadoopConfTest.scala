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

package org.apache.linkis.hadoop.common.conf

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class HadoopConfTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    Assertions.assertEquals("hadoop", HadoopConf.HADOOP_ROOT_USER.getValue)
    Assertions.assertFalse(HadoopConf.KERBEROS_ENABLE.getValue)
    Assertions.assertEquals("/appcom/keytab/", HadoopConf.KEYTAB_FILE.getValue)
    Assertions.assertEquals("127.0.0.1", HadoopConf.KEYTAB_HOST.getValue)
    Assertions.assertFalse(HadoopConf.KEYTAB_HOST_ENABLED.getValue)
    Assertions.assertFalse(HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue)
    Assertions.assertEquals("hadoop", HadoopConf.KEYTAB_PROXYUSER_SUPERUSER.getValue)
    Assertions.assertEquals(
      "/appcom/config/external-conf/hadoop",
      HadoopConf.HADOOP_EXTERNAL_CONF_DIR_PREFIX.getValue
    )
    Assertions.assertFalse(HadoopConf.HDFS_ENABLE_CACHE)
    Assertions.assertTrue(180000 == HadoopConf.HDFS_ENABLE_CACHE_IDLE_TIME)

  }

}
