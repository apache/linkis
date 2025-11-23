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

package org.apache.linkis.computation.client.operator.impl

import org.apache.linkis.computation.client.once.result.EngineConnOperateResult
import org.apache.linkis.computation.client.operator.OnceJobOperator
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.ujes.client.exception.UJESJobException

class EngineConnApplicationInfoOperator extends OnceJobOperator[ApplicationInfo] {

  override def getName: String = EngineConnApplicationInfoOperator.OPERATOR_NAME

  override protected def resultToObject(result: EngineConnOperateResult): ApplicationInfo = {
    ApplicationInfo(
      result
        .getAsOption(ECConstants.YARN_APPID_NAME_KEY)
        .getOrElse(
          throw new UJESJobException(
            20300,
            s"Cannot get applicationId from EngineConn $getServiceInstance."
          )
        ),
      result
        .getAsOption(ECConstants.YARN_APP_URL_KEY)
        .getOrElse(
          throw new UJESJobException(
            20300,
            s"Cannot get applicationUrl from EngineConn $getServiceInstance."
          )
        ),
      result.getAs(ECConstants.QUEUE)
    )
  }

}

object EngineConnApplicationInfoOperator {
  val OPERATOR_NAME = "engineConnYarnApplication"
}

case class ApplicationInfo(applicationId: String, applicationUrl: String, queue: String)
