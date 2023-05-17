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

package org.apache.linkis.engineplugin.elasticsearch.executor.client.impl

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineplugin.elasticsearch.errorcode.EasticsearchErrorCodeSummary._
import org.apache.linkis.engineplugin.elasticsearch.exception.EsConvertResponseException
import org.apache.linkis.engineplugin.elasticsearch.executor.client.{
  ElasticSearchJsonResponse,
  ElasticSearchResponse,
  ElasticSearchTableResponse,
  ResponseHandler
}
import org.apache.linkis.engineplugin.elasticsearch.executor.client.ResponseHandler._
import org.apache.linkis.storage.domain._
import org.apache.linkis.storage.domain.DataType.{DoubleType, StringType}
import org.apache.linkis.storage.resultset.table.TableRecord

import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils

import java.nio.charset.Charset

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import org.elasticsearch.client.Response

class ResponseHandlerImpl extends ResponseHandler {

  // scalastyle:off
  override def handle(response: Response): ElasticSearchResponse = {
    val contentType = ContentType.get(response.getEntity).getMimeType.toLowerCase
    val charSet = ContentType.get(response.getEntity).getCharset match {
      case c: Charset => c
      case _ => Charset.forName("UTF-8")
    }
    val contentBytes = EntityUtils.toByteArray(response.getEntity)

    if (contentBytes == null || contentBytes.isEmpty) {
      throw new EsConvertResponseException(RESPONSE_FAIL_IS_EMPTY.getErrorDesc)
    }

    val jsonNode = Utils.tryCatch {
      contentType match {
        case "application/yaml" =>
          yamlMapper.readTree(contentBytes)
        case "application/cbor" =>
          cborMapper.readTree(contentBytes)
        case "application/smile" =>
          smileMapper.readTree(contentBytes)
        case _ =>
          jsonMapper.readTree(contentBytes)
      }
    } {
      case t: Throwable => {
        warn("deserialize response content error, {}", t)
        null
      }
    }

    if (jsonNode == null) {
      ElasticSearchJsonResponse(new String(contentBytes, charSet))
    }

    var isTable = false
    val columns = ArrayBuffer[Column]()
    val records = new ArrayBuffer[TableRecord]

    // es json runType response
    jsonNode.at("/hits/hits") match {
      case hits: ArrayNode => {
        isTable = true
        columns += new Column("_index", StringType, "")
        columns += new Column("_type", StringType, "")
        columns += new Column("_id", StringType, "")
        columns += new Column("_score", DoubleType, "")
        hits.asScala.foreach {
          case obj: ObjectNode => {
            val lineValues = new Array[Any](columns.length).toBuffer
            obj
              .fields()
              .asScala
              .foreach(entry => {
                val key = entry.getKey
                val value = entry.getValue
                if ("_source".equals(key.trim)) {
                  value
                    .fields()
                    .asScala
                    .foreach(sourceEntry => {
                      val sourcekey = sourceEntry.getKey
                      val sourcevalue = sourceEntry.getValue
                      val index = columns.indexWhere(_.columnName.equals(sourcekey))
                      if (index < 0) {
                        columns += new Column(sourcekey, getNodeDataType(sourcevalue), "")
                        lineValues += getNodeValue(sourcevalue)
                      } else {
                        lineValues(index) = getNodeValue(sourcevalue)
                      }
                    })
                } else {
                  val index = columns.indexWhere(_.columnName.equals(key))
                  if (index < 0) {
                    columns += new Column(key, getNodeDataType(value), "")
                    lineValues += getNodeValue(value)
                  } else {
                    lineValues(index) = getNodeValue(value)
                  }
                }
              })
            records += new TableRecord(lineValues.toArray.asInstanceOf[Array[AnyRef]])
          }
          case _ =>
        }
      }
      case _ =>
    }

    // es sql runType response
    jsonNode.at("/rows") match {
      case rows: ArrayNode => {
        isTable = true
        jsonNode
          .get("columns")
          .asInstanceOf[ArrayNode]
          .asScala
          .foreach(node => {
            val name = node.get("name").asText()
            val estype = node.get("type").asText().trim
            columns += new Column(name, getNodeTypeByEsType(estype), "")
          })
        rows.asScala.foreach {
          case row: ArrayNode => {
            val lineValues = new ArrayBuffer[Any]()
            row.asScala.foreach(node => lineValues += getNodeValue(node))
            records += new TableRecord(lineValues.toArray.asInstanceOf[Array[AnyRef]])
          }
          case _ =>
        }
      }
      case _ =>
    }

    // write result
    if (isTable) {
      ElasticSearchTableResponse(columns.toArray, records.toArray)
    } else {
      ElasticSearchJsonResponse(new String(contentBytes, charSet))
    }
  }
  // scalastyle:on

}
