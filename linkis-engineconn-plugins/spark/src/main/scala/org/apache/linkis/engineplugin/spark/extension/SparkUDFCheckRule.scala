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
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.rules.Rule

import org.slf4j.{Logger, LoggerFactory}

case class SparkUDFCheckRule(sparkSession: SparkSession) extends Rule[LogicalPlan] {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  /**
   * 检查表达式中是否包含特定的函数名
   * @param expression
   * @param functionName
   * @return
   */
  private def containsSpecificFunction(
      logicalPlan: LogicalPlan,
      functionName: Array[String]
  ): Boolean = {
    logicalPlan.collect {
      case e
          if sparkSession.catalog
            .functionExists(e.toString()) && e.toString.contains(functionName) =>
        true
    }.nonEmpty
  }

  override def apply(plan: LogicalPlan): LogicalPlan = {
    logger.info(plan.toString())
    // 从系统属性中获取代码类型
    val codeType: String = System.getProperty(ComputationExecutorConf.CODE_TYPE, "sql")
    logger.info("SparkUDFCheckRule codeType: {}", codeType)
    // 从系统属性中获取udf函数名
    val udfNames: String = System.getProperty(ComputationExecutorConf.ONLY_SQL_USE_UDF_KEY, "")
    logger.info("SparkUDFCheckRule udfNames: {}", udfNames)
    if ("sql".equals(codeType) || StringUtils.isBlank(udfNames)) {
      // 如果是 SQL 类型，直接返回原始计划
      plan
    } else {
      val udfName: Array[String] = udfNames.split(",")
      // 如果不是 SQL 类型，则检查逻辑计划
      try {
        // 遍历逻辑计划中的所有节点
        plan.foreachUp { node =>
          node.collect {
            case e: LogicalPlan if containsSpecificFunction(e, udfName) =>
              logger.info("contains specific functionName: {}", e.toString())
              // 如果找到包含特定udf函数的表达式，则抛出异常中断
              throw new RuntimeException(
                s"Found expression containing specific functionName in non-SQL code type, terminating optimization."
              )
            case _ =>
              logger.info(
                "not expression or not contains specific functionName: {}",
                node.toString()
              )
          }
        }
      } catch {
        case e: RuntimeException =>
          logger.info("check udf function error: {}", e.getMessage)
      }
      plan
    }
  }

}
