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

package com.webank.wedatasphere.linkis.storage.source

import java.util

import com.webank.wedatasphere.linkis.storage.LineRecord
import com.webank.wedatasphere.linkis.storage.script.ScriptRecord
import org.apache.commons.math3.util.Pair

/**
  * Created by johnnwang on 2020/1/15.
  */
class TextFileSource extends AbstractFileSource {

  shuffler = {
    case s: ScriptRecord if "".equals(s.getLine) => new LineRecord("\n")
    case record => record
  }

  override def collect(): Pair[Object, util.ArrayList[Array[String]]] = {
    val collect: Pair[Object, util.ArrayList[Array[String]]] = super.collect()
    if (!params.getOrDefault("ifMerge", "true").toBoolean) return collect
    import scala.collection.JavaConversions._
    val snd: util.ArrayList[Array[String]] = collect.getSecond
    val str = new StringBuilder
    for (i <- snd) {
      i match {
        case Array("\n") => str.append("\n")
        case Array(y) => str.append(y).append("\n")
      }
    }
    snd.clear()
    snd.add(Array(str.toString()))
    collect
  }

}
