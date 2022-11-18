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

package org.apache.linkis.manager.engineplugin.pipeline.constant

object PipeLineConstant {
  val DEFAULTC_HARSET = "utf-8"
  val DEFAULT_SHEETNAME = "result"
  val DEFAULT_DATEFORMATE = "yyyy-MM-dd HH:mm:ss"
  val PIPELINE_OUTPUT_ISOVERWRITE = "pipeline.output.isoverwrite"
  val PIPELINE_OUTPUT_SHUFFLE_NULL_TYPE = "pipeline.output.shuffle.null.type"
  val PIPELINE_OUTPUT_CHARSET = "pipeline.output.charset"
  val PIPELINE_FIELD_SPLIT = "pipeline.field.split"

  val PIPELINE_FIELD_QUOTE_RETOUCH_CONF_ENABLE =
    "wds.linkis.engine.pipeline.field.quote.retoch.enable"

  val BLANK = "BLANK"
}
