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
 
package org.apache.linkis.orchestrator.plans.unit




import java.util

import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel

import scala.collection.JavaConverters._


/**
  * Code logical unit, use to judge the script type, such as: sql, py, etc.
  * 代码存储单位，并通过CodeLanguageLabel判断是为sql or py等
  */
class CodeLogicalUnit(val codes: java.util.List[String], codeLogicalLabel: CodeLanguageLabel, separator: String) extends LogicalUnit[CodeLogicalUnit] {

  def this(codes: java.util.List[String], codeLogicalLabel: CodeLanguageLabel) = this(codes, codeLogicalLabel, "\n")

  override def toStringCode: String = codes.asScala.mkString(separator)

  override def add(logicalUnit: CodeLogicalUnit): CodeLogicalUnit = {
    codes.addAll(logicalUnit.codes)
    this
  }

  def getLabel: CodeLanguageLabel = codeLogicalLabel

  def getSeparator = separator

  def parseCodes(op: String => String): CodeLogicalUnit = {
    new CodeLogicalUnit(codes.asScala.map(op).asJava, codeLogicalLabel, separator)
  }

  def getCodes = codes

}

object CodeLogicalUnit {

  def apply(codes: String, codeLogicalLabel: CodeLanguageLabel): CodeLogicalUnit = new CodeLogicalUnit(util.Arrays.asList(codes), codeLogicalLabel)

  def apply(codes: String, codeType: String): CodeLogicalUnit = apply(codes, CodeLanguageLabelCreator(codeType))

}



object CodeLanguageLabelCreator {
  def apply(codeType: String): CodeLanguageLabel = {
    val codeLogicalUnit = new CodeLanguageLabel()
    codeLogicalUnit.setCodeType(codeType)
    codeLogicalUnit
  }
}