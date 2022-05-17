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
 
package org.apache.linkis.computation.client

import java.util

import org.apache.linkis.ujes.client.UJESClient
import org.apache.linkis.ujes.client.exception.UJESJobException
import org.apache.linkis.ujes.client.request.ResultSetAction
import org.apache.linkis.ujes.client.response.ResultSetResult


abstract class ResultSetIterator[M, R](resultSetIterable: ResultSetIterable, metadata: Object, records: Object)
  extends Iterator[R] with java.util.Iterator[R] {

  protected var cachedRecords: util.List[R] = recordsTo(records)
  protected var canFetch = true
  private var index: Int = 0

  def getMetadata: M = metadata match {
    case m: M => m
    case null => null.asInstanceOf[M]
  }

  protected def recordsTo(records: Object): util.List[R] = records match {
    case list: util.List[R] => list
    case l => throw new UJESJobException(s"Not support resultSet type $l.")
  }

  override def hasNext: Boolean = if(!canFetch && index >= cachedRecords.size) false else {
    if(index >= cachedRecords.size) {
      cachedRecords = recordsTo(resultSetIterable.fetch())
      if(cachedRecords.size < resultSetIterable.pageSize) canFetch = false
      index = 0
    }
    index < cachedRecords.size
  }

  override def next(): R = {
    val record = cachedRecords.get(index)
    index += 1
    record
  }

}
class ResultSetIterable(ujesClient: UJESClient,
                                       user: String,
                                       fsPath: String,
                                       jobMetrics: LinkisJobMetrics,
                        val pageSize: Int = 100)
  extends MetricIterator[LinkisJobMetrics] {

  private var metadata: Object = _
  private var `type`: String = _
  private var page: Int = 0

  def getFsPath: String = fsPath

  override def getMetrics: LinkisJobMetrics = jobMetrics

  private[client] def fetch(): Object = {
    val startTime = System.currentTimeMillis
    page += 1
    val action = ResultSetAction.builder().setPath(fsPath).setUser(user).setPage(page).setPageSize(pageSize).build()
    ujesClient.resultSet(action) match {
      case resultSet: ResultSetResult =>
        jobMetrics.addClientFetchResultSetTime(System.currentTimeMillis - startTime)
        metadata = resultSet.metadata
        `type` = resultSet.getType
        resultSet.getFileContent
    }
  }

  def iterator: ResultSetIterator[_, _] = {
    val records = fetch()
    `type` match {
      case "1" | "5" =>
        new TextResultSetIterator(this, metadata, records).asInstanceOf[ResultSetIterator[Object, Object]]
      case "2" =>
        new TableResultSetIterator(this, metadata, records).asInstanceOf[ResultSetIterator[Object, Object]]
      case _ => throw new UJESJobException(s"Not support resultSet type ${`type`}.")
    }
  }

}

class TableResultSetIterator(resultSetIterable: ResultSetIterable, metadata: Object, records: Object)
  extends ResultSetIterator[util.List[util.Map[String, String]], util.List[Any]](resultSetIterable, metadata, records)

import scala.collection.convert.WrapAsScala._
import scala.collection.convert.WrapAsJava._
class TextResultSetIterator(resultSetIterable: ResultSetIterable, metadata: Object, records: Object)
  extends ResultSetIterator[String, String](resultSetIterable, metadata, records) {

  override protected def recordsTo(records: Object): util.List[String] = records match {
    case list: util.List[util.List[String]] => list.map(_.head)
    case null => new util.ArrayList[String]
    case r => throw new UJESJobException(s"Not support resultSet $r.")
  }

}

trait MetricIterator[M <: ClientMetrics] {

  def getMetrics: M

}