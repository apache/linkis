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
 
package org.apache.linkis.orchestrator.plans.logical

import org.apache.linkis.orchestrator.plans.ast.ASTOrchestration

/**
  *
  */
trait Origin {

  def getASTOrchestration: ASTOrchestration[_]

  def getPosition: Int

}

object Origin {
  private val emptyOrigin = apply(null, 0)
  def apply(astOrchestration: ASTOrchestration[_], position: Int): Origin = new Origin {
    override def getASTOrchestration: ASTOrchestration[_] = astOrchestration
    override def getPosition: Int = position

    override def equals(obj: Any): Boolean = if (super.equals(obj)) true else obj match {
      case o: Origin => o.getASTOrchestration == getASTOrchestration && position == o.getPosition
      case _ => false
    }
  }
  def empty(): Origin = emptyOrigin
  def isEmpty(origin: Origin): Boolean = origin == emptyOrigin
}