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
 
package org.apache.linkis.orchestrator.computation.catalyst.converter.ruler

import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import org.apache.linkis.orchestrator.plans.ast.ASTContext

class SQLLimitConverterCheckRuler  extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = {
    in match {
      case computationJobReq: ComputationJobReq =>
        computationJobReq.getCodeLanguageLabel.getCodeType.toLowerCase match {
          case "hql" | "sql" | "jdbc"|"hive" | "psql" =>
            val logicalUnit = computationJobReq.getCodeLogicalUnit.parseCodes({ c => SQLExplain.dealSQLLimit(c, new java.lang.StringBuilder()) })
            computationJobReq.setCodeLogicalUnit(logicalUnit)
          case _ =>
        }
      case _ =>
    }
  }

  override def getName: String = "SQLLimitConverterCheckRuler"
}
