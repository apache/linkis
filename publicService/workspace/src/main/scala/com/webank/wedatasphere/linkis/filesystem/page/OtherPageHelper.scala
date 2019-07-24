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

package com.webank.wedatasphere.linkis.filesystem.page

import java.util
import scala.collection.JavaConversions._

/**
  * Created by johnnwang on 2019/4/17.
  */
class OtherPageHelper(bodyP: util.ArrayList[util.ArrayList[String]], paramsP: util.HashMap[String, String]) extends AbstractPageHelper(bodyP, paramsP) {

  def startPage(): String = {
    val stringBuilder = new StringBuilder
    val subList: util.List[String] = body(0).subList(fromIndex, toIndex)
    subList.foreach {
      f => f match {
        case "" => stringBuilder.append("\n")
        case _ => stringBuilder.append(f + "\n")
      }
    }
    stringBuilder.toString()
  }

  override def init(): Unit = {
    commonInit(body(0).size())
  }

}



