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

package org.apache.linkis.common.utils

import org.apache.linkis.common.conf.CommonVars

import org.apache.commons.lang3.StringUtils

object CodeAndRunTypeUtils {
  private val CONF_LOCK = new Object()

  val CODE_TYPE_AND_RUN_TYPE_RELATION = CommonVars(
    "wds.linkis.codeType.runType.relation",
    "sql=>sql|hql|jdbc|hive|psql|fql|tsql,python=>python|py|pyspark,java=>java,scala=>scala,shell=>sh|shell"
  )

  val RUN_TYPE_SQL = "sql"
  val RUN_TYPE_PYTHON = "python"
  val RUN_TYPE_JAVA = "java"
  val RUN_TYPE_SCALA = "scala"
  val RUN_TYPE_SHELL = "shell"

  private var codeTypeAndRunTypeRelationMap: Map[String, List[String]] = null

  private def codeTypeAndRunTypeRelationMapParser(configV: String): Map[String, List[String]] = {
    val confDelimiter = ","
    if (configV == null || "".equals(configV)) {
      Map()
    } else {
      configV
        .split(confDelimiter)
        .filter(x => x != null && !"".equals(x))
        .map(x => {
          val confArr = x.split("=>")
          if (confArr.length == 2) {
            (confArr(0), for (x <- confArr(1).split("\\|").toList) yield x.trim)
          } else null
        })
        .filter(x => x != null)
        .toMap
    }
  }

  def getCodeTypeAndRunTypeRelationMap: Map[String, List[String]] = {
    if (codeTypeAndRunTypeRelationMap == null) {
      CONF_LOCK.synchronized {
        if (codeTypeAndRunTypeRelationMap == null) {
          codeTypeAndRunTypeRelationMap =
            codeTypeAndRunTypeRelationMapParser(CODE_TYPE_AND_RUN_TYPE_RELATION.getValue)
        }
      }
    }
    codeTypeAndRunTypeRelationMap
  }

  def getRunTypeAndCodeTypeRelationMap: Map[String, String] = {
    val codeTypeAndRunTypeRelationMap = getCodeTypeAndRunTypeRelationMap
    if (codeTypeAndRunTypeRelationMap.isEmpty) Map()
    else codeTypeAndRunTypeRelationMap.flatMap(x => x._2.map(y => (y, x._1)))
  }

  def getRunTypeByCodeType(codeType: String, defaultRunType: String = ""): String = {
    if (StringUtils.isBlank(codeType)) {
      return ""
    }
    getRunTypeAndCodeTypeRelationMap.getOrElse(codeType, defaultRunType)
  }

  def getSuffixBelongToRunTypeOrNot(suffix: String, runType: String): Boolean = {
    val codeTypeAndRunTypeRelationMap = getCodeTypeAndRunTypeRelationMap
    if (codeTypeAndRunTypeRelationMap.isEmpty) return false
    val suffixListOfRunType = codeTypeAndRunTypeRelationMap.getOrElse(runType, List())
    if (suffixListOfRunType.isEmpty) return false
    if (suffixListOfRunType.contains(suffix)) return true
    false
  }

}
