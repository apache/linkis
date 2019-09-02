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

package com.webank.wedatasphere.linkis.jobhistory.util

import java.util
import scala.collection.JavaConversions._
import com.webank.wedatasphere.linkis.jobhistory.entity.{QueryTask, QueryTaskVO}

/**
  * Created by johnnwang on 2019/2/25.
  */
object QueryUtil {
  def getQueryVOList(list:java.util.List[QueryTask]):java.util.List[QueryTaskVO] ={
    val ovs = new util.ArrayList[QueryTaskVO]
    list.foreach(f =>{
      import com.webank.wedatasphere.linkis.jobhistory.conversions.TaskConversions.queryTask2QueryTaskVO
      ovs.add(f)
    })
    ovs
  }
}
