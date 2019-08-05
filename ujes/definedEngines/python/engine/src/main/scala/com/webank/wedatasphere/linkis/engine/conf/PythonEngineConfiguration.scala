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

package com.webank.wedatasphere.linkis.engine.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars


/**
  * created by allenlliu on 2019/3/21
  * Description:
  */
object PythonEngineConfiguration {
  val PYTHON_CONSOLE_OUTPUT_LINE_LIMIT = CommonVars("wds.linkis.python.line.limit", 10)
  val PY4J_HOME = CommonVars("wds.linkis.python.py4j.path", this.getClass.getResource("/").getPath)
}
