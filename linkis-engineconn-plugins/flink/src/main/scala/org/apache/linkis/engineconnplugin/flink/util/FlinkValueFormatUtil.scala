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

package org.apache.linkis.engineconnplugin.flink.util

import org.apache.linkis.common.conf.CommonVars

import java.text.NumberFormat

object FlinkValueFormatUtil {

  val FLINK_NF_FRACTION_LENGTH = CommonVars("wds.linkis.engine.flink.fraction.length", 30)

  private val nf = NumberFormat.getInstance()
  nf.setGroupingUsed(false)
  nf.setMaximumFractionDigits(FLINK_NF_FRACTION_LENGTH.getValue)

  def formatValue(value: Any): Any = value match {
    case value: String => value.replaceAll("\n|\t", " ")
    case value: Double => nf.format(value)
    case value: Any => value.toString
    case _ => null
  }

  def mergeAndDeduplicate(defaultJavaOpts: String, envJavaOpts: String): String = {
    val patternX = """-XX:([^\s]+)=([^\s]+)""".r
    val keyValueMapX = patternX
      .findAllMatchIn(envJavaOpts)
      .map { matchResult =>
        val key = matchResult.group(1)
        val value = matchResult.group(2)
        (key, value)
      }
      .toMap

    val patternD = """-D([^\s]+)=([^\s]+)""".r
    val keyValueMapD = patternD
      .findAllMatchIn(envJavaOpts)
      .map { matchResult =>
        val key = matchResult.group(1)
        val value = matchResult.group(2)
        (key, value)
      }
      .toMap
    val xloggcPattern = """-Xloggc:[^\s]+""".r
    val xloggcValueStr1 = xloggcPattern.findFirstMatchIn(defaultJavaOpts).getOrElse("").toString
    val xloggcValueStr2 = xloggcPattern.findFirstMatchIn(envJavaOpts).getOrElse("").toString
    var escapedXloggcValue = ""
    var replaceStr1 = ""
    var replaceStr2 = ""
    if (xloggcValueStr1.nonEmpty && xloggcValueStr2.nonEmpty) {
      escapedXloggcValue = xloggcValueStr2.replace("\\<", "<").replace("\\>", ">")
      replaceStr1 = defaultJavaOpts.replace(xloggcValueStr1, escapedXloggcValue)
      replaceStr2 = envJavaOpts.replace(xloggcValueStr2, "")
    }
    if (xloggcValueStr1.nonEmpty && xloggcValueStr2.isEmpty) {
      escapedXloggcValue = xloggcValueStr1.replace("\\<", "<").replace("\\>", ">")
      replaceStr1 = defaultJavaOpts.replace(xloggcValueStr1, escapedXloggcValue)
      replaceStr2 = envJavaOpts
    }
    if (xloggcValueStr1.isEmpty && xloggcValueStr2.isEmpty) {
      replaceStr1 = defaultJavaOpts
      replaceStr2 = envJavaOpts
    }
    val MergedStringX = keyValueMapX.foldLeft(replaceStr1) { (result, entry) =>
      val (key, value) = entry
      val oldValue = s"$key=[^\\s]+"
      val newValue = key + "=" + value
      result.replaceAll(oldValue, newValue)
    }

    val MergedStringD = keyValueMapD.foldLeft(MergedStringX) { (result, entry) =>
      val (key, value) = entry
      val oldValue = s"$key=[^\\s]+"
      val newValue = key + "=" + value
      result.replaceAll(oldValue, newValue)
    }
    val javaOpts = (MergedStringD.split("\\s+") ++ replaceStr2.split("\\s+")).distinct.mkString(" ")
    javaOpts
  }

}
