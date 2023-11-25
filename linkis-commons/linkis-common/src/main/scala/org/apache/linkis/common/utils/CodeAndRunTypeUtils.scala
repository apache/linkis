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

import scala.collection.mutable

object CodeAndRunTypeUtils {
  private val CONF_LOCK = new Object()

  /**
   * sql Language type sql/hql/jdbc code Type
   */
  val CODE_TYPE_AND_RUN_TYPE_RELATION = CommonVars(
    "linkis.codeType.language.relation",
    "sql=>sql|hql|jdbc|hive|psql|fql|tsql,python=>python|py|pyspark,java=>java,scala=>scala,shell=>sh|shell,json=>json|data_calc"
  )

  val LANGUAGE_TYPE_SQL = "sql"

  val LANGUAGE_TYPE_PYTHON = "python"

  val LANGUAGE_TYPE_JAVA = "java"

  val LANGUAGE_TYPE_SCALA = "scala"

  val LANGUAGE_TYPE_SHELL = "shell"

  val LANGUAGE_TYPE_JSON = "json"

  private var codeTypeAndLanguageTypeRelationMap: Map[String, List[String]] = null

  /**
   * Used to parse the wds.linkis.codeType.runType.relation parameter
   *   1. Get scripting language type by "=>" 2. Get the code type by "|"
   * @param configV
   * @return
   */
  private def codeTypeAndLanguageTypeRelationMapParser(
      configV: String
  ): Map[String, List[String]] = {
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
        .filter(runTypeCodeTypes =>
          runTypeCodeTypes != null && StringUtils.isNotBlank(
            runTypeCodeTypes._1
          ) && null != runTypeCodeTypes._2
        )
        .toMap
    }
  }

  /**
   * Obtain the mapping relationship between code Type and script type key: Language Type value:
   * list[code type]
   * @return
   */
  def getCodeTypeAndLanguageTypeRelationMap: Map[String, List[String]] = {
    if (codeTypeAndLanguageTypeRelationMap == null) {
      CONF_LOCK.synchronized {
        if (codeTypeAndLanguageTypeRelationMap == null) {
          codeTypeAndLanguageTypeRelationMap =
            codeTypeAndLanguageTypeRelationMapParser(CODE_TYPE_AND_RUN_TYPE_RELATION.getValue)
        }
      }
    }
    codeTypeAndLanguageTypeRelationMap
  }

  /**
   * Obtain the Map of script type and CodeType key: script type value: Language Type
   * @return
   */
  def getLanguageTypeAndCodeTypeRelationMap: Map[String, String] = {
    val codeTypeAndRunTypeRelationMap = getCodeTypeAndLanguageTypeRelationMap
    if (codeTypeAndRunTypeRelationMap.isEmpty) Map()
    else {
//      codeTypeAndRunTypeRelationMap.flatMap(x => x._2.map(y => (y, x._1)))
      val map = mutable.Map[String, String]()
      codeTypeAndRunTypeRelationMap.foreach(kv => {
        kv._2.foreach(v => map.put(v, kv._1))
      })
      map.toMap
    }
  }

  def getLanguageTypeByCodeType(codeType: String, defaultLanguageType: String = ""): String = {
    if (StringUtils.isBlank(codeType)) {
      return ""
    }
    getLanguageTypeAndCodeTypeRelationMap.getOrElse(codeType, defaultLanguageType)
  }

  /**
   * Determine whether the corresponding file suffix is the corresponding Language type
   * @param suffix
   * @param languageType
   * @return
   */
  def getSuffixBelongToLanguageTypeOrNot(suffix: String, languageType: String): Boolean = {
    val codeTypeAndRunTypeRelationMap = getCodeTypeAndLanguageTypeRelationMap
    if (codeTypeAndRunTypeRelationMap.isEmpty) return false
    val suffixListOfRunType = codeTypeAndRunTypeRelationMap.getOrElse(languageType, List())
    if (suffixListOfRunType.isEmpty) return false
    if (suffixListOfRunType.contains(suffix)) return true
    false
  }

}
