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

package scala.org.apache.linkis.manager.engineplugin.pipeline.conf

import org.apache.linkis.common.conf.TimeType
import org.apache.linkis.manager.engineplugin.pipeline.conf.PipelineEngineConfiguration
import org.junit.jupiter.api.{Assertions, Test}

class TestPipelineEngineConfiguration {

  @Test
  def testConfig: Unit = {
    Assertions.assertEquals(true, PipelineEngineConfiguration.PIPELINE_OUTPUT_ISOVERWRITE_SWITCH.getValue)
    Assertions.assertEquals("UTF-8", PipelineEngineConfiguration.PIPELINE_OUTPUT_CHARSET_STR.getValue)
    Assertions.assertEquals(",", PipelineEngineConfiguration.PIPELINE_FIELD_SPLIT_STR.getValue)
    Assertions.assertEquals(false, PipelineEngineConfiguration.PIPELINE_FIELD_QUOTE_RETOUCH_ENABLE.getValue)
    Assertions.assertEquals(false, PipelineEngineConfiguration.EXPORT_EXCEL_AUTO_FORMAT.getValue)
  }

}
