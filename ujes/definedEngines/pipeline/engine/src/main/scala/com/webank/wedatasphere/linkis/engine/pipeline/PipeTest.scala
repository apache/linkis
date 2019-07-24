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

package com.webank.wedatasphere.linkis.engine.pipeline

import com.webank.wedatasphere.linkis.engine.pipeline.domain.PipeEntity
import com.webank.wedatasphere.linkis.engine.pipeline.util.PipeLineUtil

/**
  * Created by johnnwang on 2018/11/14.
  */
object PipeTest {
  def main(args: Array[String]): Unit = {
    val out01 = "from file:///appcom/bdap/johnnwang/ to hdfs:///tmp/bdp-ide/johnnwang/test1/new_py_180910175033.python"
    val regex = ("(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s?").r
    out01 match {
      case regex(source,desct) => print(source.contains("."))
    }

  }

}
