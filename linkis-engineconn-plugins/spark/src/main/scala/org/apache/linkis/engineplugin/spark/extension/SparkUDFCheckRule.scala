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

package org.apache.linkis.engineplugin.spark.extension

import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary
import org.apache.linkis.engineplugin.spark.exception.RuleCheckFailedException
import org.apache.linkis.engineplugin.spark.utils.PlanParseUtil

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.rules.Rule

import org.slf4j.{Logger, LoggerFactory}

case class SparkUDFCheckRule(sparkSession: SparkSession) extends Rule[LogicalPlan] {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def apply(plan: LogicalPlan): LogicalPlan = {
    // 从系统属性中获取代码类型
    val codeType: String = System.getProperty(ComputationExecutorConf.CODE_TYPE, "sql")
    // 从系统属性中获取udf函数名
    val udfNames: String = System.getProperty(ComputationExecutorConf.ONLY_SQL_USE_UDF_KEY, "")

    if (
        ComputationExecutorConf.SUPPORT_SPECIAL_UDF_LANGUAGES.getValue.contains(
          codeType
        ) || StringUtils.isBlank(udfNames)
    ) {
      // 如果是 SQL 类型，或者未注册特殊udf函数，直接返回原始计划
      plan
    } else {
      // 如果不是 SQL 类型，则检查逻辑计划
      try {
        val udfName: Array[String] = udfNames.split(",")
        if (PlanParseUtil.checkUdf(plan, udfName)) {
          logger.info("contains specific functionName: {}", udfNames)
          throw new RuleCheckFailedException(
            SparkErrorCodeSummary.NOT_SUPPORT_FUNCTION.getErrorCode,
            SparkErrorCodeSummary.NOT_SUPPORT_FUNCTION.getErrorDesc
          )
        }
      } catch {
        case e: RuleCheckFailedException =>
          throw e
        case e: Exception =>
          logger.info("check udf function error: {}", e.getMessage)
      }
      plan
    }
  }

}
