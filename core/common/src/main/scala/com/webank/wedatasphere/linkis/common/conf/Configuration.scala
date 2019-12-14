/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.common.conf

/**
  * Created by enjoyyin on 2018/4/18.
  */
object Configuration {

  val BDP_ENCODING = CommonVars("wds.linkis.encoding", "utf-8")

  val DEFAULT_DATE_PATTERN = CommonVars("wds.linkis.date.pattern", "yyyy-MM-dd'T'HH:mm:ssZ")

  val FIELD_SPLIT = CommonVars("wds.linkis.field.split", "hadoop")

  val IS_TEST_MODE = CommonVars("wds.linkis.test.mode", false)

  val hadoopConfDir = CommonVars("hadoop.config.dir", CommonVars("HADOOP_CONF_DIR", "").getValue).getValue

}
