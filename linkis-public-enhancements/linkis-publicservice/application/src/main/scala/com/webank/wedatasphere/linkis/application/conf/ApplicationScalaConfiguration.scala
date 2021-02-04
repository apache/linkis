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

package com.webank.wedatasphere.linkis.application.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * Created by johnnwang on 2019/2/14.
  */
object ApplicationScalaConfiguration {
  val INIT_EXAMPLE_PATH= CommonVars("wds.linkis.application.example.path", "hdfs:///tmp/")
  val INIT_EXAMPLE_SQL_NAME = CommonVars("wds.linkis.application.example.sql.name", "example01.sql")
  val INIT_EXAMPLE_SCALA_NAME = CommonVars("wds.linkis.application.example.scala.name", "example02.scala")
  val INIT_EXAMPLE_SPY_NAME = CommonVars("wds.linkis.application.example.spy.name", "example03.py")
  val INIT_EXAMPLE_HQL_NAME = CommonVars("wds.linkis.application.example.hql.name", "example04.hql")
  val INIT_EXAMPLE_PYTHON_NAME = CommonVars("wds.linkis.application.example.python.name", "example05.python")
}
