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

package org.apache.linkis.computation.client.once

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.computation.client.once.action.{
  AskEngineConnAction,
  CreateEngineConnAction,
  EngineConnOperateAction,
  GetEngineConnAction,
  KillEngineConnAction,
  LinkisManagerAction
}
import org.apache.linkis.computation.client.once.result.{
  AskEngineConnResult,
  CreateEngineConnResult,
  EngineConnOperateResult,
  GetEngineConnResult,
  KillEngineConnResult,
  LinkisManagerResult
}
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.request.Action
import org.apache.linkis.ujes.client.{UJESClient, UJESClientImpl}

import java.io.Closeable

trait LinkisManagerClient extends Closeable {

  def askEngineConn(askEngineConnAction: AskEngineConnAction): AskEngineConnResult

  def createEngineConn(createEngineConnAction: CreateEngineConnAction): CreateEngineConnResult

  def getEngineConn(getEngineConnAction: GetEngineConnAction): GetEngineConnResult

  def killEngineConn(killEngineConnAction: KillEngineConnAction): KillEngineConnResult

  def executeEngineConnOperation(
      engineConnOperateAction: EngineConnOperateAction
  ): EngineConnOperateResult

}

object LinkisManagerClient {

  def apply(ujesClient: UJESClient): LinkisManagerClient = new LinkisManagerClientImpl(ujesClient)

}

class LinkisManagerClientImpl(ujesClient: UJESClient) extends LinkisManagerClient {

  private val dwsHttpClient = {
    val dwsHttpClientField = classOf[UJESClientImpl].getDeclaredField("dwsHttpClient")
    dwsHttpClientField.setAccessible(true)
    dwsHttpClientField.get(ujesClient).asInstanceOf[DWSHttpClient]
  }

  protected def execute[T <: LinkisManagerResult](linkisManagerAction: LinkisManagerAction): T =
    linkisManagerAction match {
      case action: Action => dwsHttpClient.execute(action).asInstanceOf[T]
    }

  override def createEngineConn(
      createEngineConnAction: CreateEngineConnAction
  ): CreateEngineConnResult = execute(createEngineConnAction)

  override def getEngineConn(getEngineConnAction: GetEngineConnAction): GetEngineConnResult =
    execute(getEngineConnAction)

  override def killEngineConn(killEngineConnAction: KillEngineConnAction): KillEngineConnResult =
    execute(killEngineConnAction)

  override def executeEngineConnOperation(
      engineConnOperateAction: EngineConnOperateAction
  ): EngineConnOperateResult = {
    Utils.tryCatch {
      val rs = execute[EngineConnOperateResult](engineConnOperateAction)
      rs
    } { case e: Exception =>
      val rs = new EngineConnOperateResult
      rs.setIsError(true)
      rs.setErrorMsg(e.getMessage)
      rs
    }
  }

  override def close(): Unit = ujesClient.close()

  override def askEngineConn(askEngineConnAction: AskEngineConnAction): AskEngineConnResult =
    execute(askEngineConnAction)

}
