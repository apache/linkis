/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.engineplugin.pipeline.conf

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.manager.engineplugin.pipeline.constant.PipeLineConstant.{PIPELINE_FIELD_SPLIT, PIPELINE_OUTPUT_CHARSET, PIPELINE_OUTPUT_ISOVERWRITE}

object PipelineEngineConfiguration {

  val PIPELINE_OUTPUT_ISOVERWRITE_SWITCH = CommonVars(PIPELINE_OUTPUT_ISOVERWRITE, true)

  val PIPELINE_OUTPUT_CHARSET_STR = CommonVars(PIPELINE_OUTPUT_CHARSET, "UTF-8")

  val PIPELINE_FIELD_SPLIT_STR = CommonVars(PIPELINE_FIELD_SPLIT, " ")

}
