/**
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client.impl

import java.nio.charset.Charset

import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.exception.EsConvertResponseException
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client.ResponseHandler._
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client.ResponseHandler
import com.webank.wedatasphere.linkis.storage.LineRecord
import com.webank.wedatasphere.linkis.storage.domain._
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.io.IOUtils
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Response

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer


/**
 *
 * @author wang_zh
 * @date 2020/5/12
 */
class ResponseHandlerImpl extends ResponseHandler {

  override def handle(response: Response, storePath: String, alias: String): String = {
    handle(response, storePath, alias, StorageUtils.getJvmUser)
  }

  override def handle(response: Response, storePath: String, alias: String, proxyUser: String): String = {
    val contentType = ContentType.get(response.getEntity).getMimeType.toLowerCase
    val charSet = ContentType.get(response.getEntity).getCharset match {
      case c: Charset => c
      case _ => Charset.forName("UTF-8")
    }
    val contentBytes = EntityUtils.toByteArray(response.getEntity)

    if (contentBytes == null || contentBytes.isEmpty) {
      throw EsConvertResponseException("EsEngineExecutor convert response fail, response content is empty.")
    }

    val jsonNode = Utils.tryCatch{ contentType match {
        case "application/yaml" =>
          yamlMapper.readTree(contentBytes)
        case "application/cbor" =>
          cborMapper.readTree(contentBytes)
        case "application/smile" =>
          smileMapper.readTree(contentBytes)
//        case "text/csv" =>
//          csvMapper.readTree(contentBytes)
//        case "text/tab-separated-values" =>
//          csvMapper.readTree(contentBytes)
//          val schema = csvMapper.schemaFor(classOf[Array[Byte]]).withColumnSeparator('\t')
//          val reader = csvMapper.readerFor(classOf[Array[Byte]]).`with`(schema)
//          reader.readValue(contentBytes)
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
      return writeText(new String(contentBytes, charSet), storePath, alias, proxyUser)
    }

    var isTable = false
    val columns = ArrayBuffer[Column]()
    val records = new ArrayBuffer[TableRecord]

    // es json runType response
    jsonNode.at("/hits/hits") match {
      case hits: ArrayNode => {
        isTable = true
        columns += Column("_index", StringType, "")
        columns += Column("_type", StringType, "")
        columns += Column("_id", StringType, "")
        columns += Column("_score", DoubleType, "")
        hits.foreach {
          case obj: ObjectNode => {
            val lineValues = new Array[Any](columns.length).toBuffer
            obj.fields().foreach(entry => {
              val key = entry.getKey
              val value = entry.getValue
              if ("_source".equals(key.trim)) {
                value.fields().foreach(sourceEntry => {
                  val sourcekey = sourceEntry.getKey
                  val sourcevalue = sourceEntry.getValue
                  val index = columns.indexWhere(_.columnName.equals(sourcekey))
                  if (index < 0) {
                    columns += Column(sourcekey, getNodeDataType(sourcevalue), "")
                    lineValues += getNodeValue(sourcevalue)
                  } else {
                    lineValues(index) = getNodeValue(sourcevalue)
                  }
                })
              } else {
                val index = columns.indexWhere(_.columnName.equals(key))
                if (index < 0) {
                  columns += Column(key, getNodeDataType(value), "")
                  lineValues += getNodeValue(value)
                } else {
                  lineValues(index) = getNodeValue(value)
                }
              }
            })
            records += new TableRecord(lineValues.toArray)
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
        jsonNode.get("columns").asInstanceOf[ArrayNode]
          .foreach(node => {
            val name = node.get("name").asText()
            val estype = node.get("type").asText().trim
            columns += Column(name, getNodeTypeByEsType(estype), "")
          })
        rows.foreach {
          case row: ArrayNode => {
            val lineValues = new ArrayBuffer[Any]()
            row.foreach(node => lineValues += getNodeValue(node))
            records += new TableRecord(lineValues.toArray)
          }
          case _ =>
        }
      }
      case _ =>
    }

    // write result
    if (isTable) {
      writeTable(new TableMetaData(columns.toArray), records, storePath, alias, proxyUser)
    } else {
      writeText(new String(contentBytes, charSet), storePath, alias, proxyUser)
    }
  }

  def writeText(content: String, storePath: String, alias: String, proxyUser: String): String = {
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TEXT_TYPE)
    val resultSetPath = resultSet.getResultSetPath(new FsPath(storePath), alias)
    val writer = ResultSetWriter.getResultSetWriter(resultSet, ElasticSearchConfiguration.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, proxyUser)
    writer.addMetaData(null)
    content.split("\\n").foreach(item => writer.addRecord(new LineRecord(item)))
    IOUtils.closeQuietly(writer)
    writer.toString()
  }

  def writeTable(metaData: TableMetaData, records: ArrayBuffer[TableRecord], storePath: String, alias: String, proxyUser: String): String = {
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TABLE_TYPE)
    val resultSetPath = resultSet.getResultSetPath(new FsPath(storePath), alias)
    val writer = ResultSetWriter.getResultSetWriter(resultSet, ElasticSearchConfiguration.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, proxyUser)
    writer.addMetaData(metaData)
    records.foreach(writer.addRecord)
    IOUtils.closeQuietly(writer)
    writer.toString()
  }

}
