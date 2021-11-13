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
 
package org.apache.linkis.orchestrator.domain.req

import org.apache.linkis.orchestrator.domain.AbstractJobReq.AbstractJobReqBuilder
import org.apache.linkis.orchestrator.domain.{AbstractJobReq, JobReq}

/**
  *
  */
trait DataViewReq extends JobReq {

  def getDataView: String

}

class ShowDataViewReq private() extends AbstractJobReq with DataViewReq {
  private var dataView: String = _

  override def getDataView: String = dataView

  override def getId: String = ???

}

object ShowDataViewReq {
  def newBuilder(): ShowDataViewReqBuilder = new ShowDataViewReqBuilder
  class ShowDataViewReqBuilder extends AbstractJobReqBuilder {
    def setDataView(dataView: String): ShowDataViewReqBuilder = jobReq match {
      case jobReq: ShowDataViewReq =>
        jobReq.dataView = dataView
        this
    }

    override def clone(jobReq: JobReq): ShowDataViewReqBuilder = {
      jobReq match {
        case jobReq: ShowDataViewReq => setDataView(jobReq.getDataView)
        case _ =>
      }
      null //TODO update
    }

    override protected def createJobReq(): AbstractJobReq = new ShowDataViewReq
  }

}