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
 
package org.apache.linkis.storage.source

import java.util

import org.apache.linkis.storage.LineRecord
import org.apache.linkis.storage.script.ScriptRecord
import org.apache.commons.math3.util.Pair

import scala.collection.JavaConversions._


class TextFileSource(fileSplits: Array[FileSplit]) extends AbstractFileSource(fileSplits) {

  shuffle({
    case s: ScriptRecord if "".equals(s.getLine) => new LineRecord("\n")
    case record => record
  })

  override def collect(): Array[Pair[Object, util.ArrayList[Array[String]]]] = {
    val collects: Array[Pair[Object, util.ArrayList[Array[String]]]] = super.collect()
    if (!getParams.getOrDefault("ifMerge", "true").toBoolean) return collects
    val snds: Array[util.ArrayList[Array[String]]] = collects.map(_.getSecond)
    snds.foreach { snd =>
      val str = new StringBuilder
      snd.foreach {
        case Array("\n") => str.append("\n")
        case Array(y) => str.append(y).append("\n")
      }
      snd.clear()
      snd.add(Array(str.toString()))
    }
    collects
  }

}
