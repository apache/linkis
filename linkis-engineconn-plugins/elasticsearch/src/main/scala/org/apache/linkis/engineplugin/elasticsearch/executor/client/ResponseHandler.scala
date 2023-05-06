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

package org.apache.linkis.engineplugin.elasticsearch.executor.client

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.elasticsearch.executor.client.impl.ResponseHandlerImpl
import org.apache.linkis.storage.domain._
import org.apache.linkis.storage.domain.DataType.{
  ArrayType,
  BinaryType,
  BooleanType,
  DateType,
  DecimalType,
  NullType,
  StringType,
  StructType
}

import java.util.Locale

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import org.elasticsearch.client.Response

trait ResponseHandler extends Logging {

  def handle(response: Response): ElasticSearchResponse

}

object ResponseHandler {

  val RESPONSE_HANDLER = new ResponseHandlerImpl()

  def handle(response: Response): ElasticSearchResponse =
    RESPONSE_HANDLER.handle(response)

  val jsonMapper = new ObjectMapper()
  val yamlMapper = new YAMLMapper()
  val cborMapper = new ObjectMapper(new CBORFactory())
  val smileMapper = new ObjectMapper(new SmileFactory())
  val csvMapper = new CsvMapper()

  def getNodeDataType(node: JsonNode): DataType = node.getNodeType match {
    case JsonNodeType.ARRAY => ArrayType
    case JsonNodeType.BINARY => BinaryType
    case JsonNodeType.BOOLEAN => BooleanType
    case JsonNodeType.NULL => NullType
    case JsonNodeType.NUMBER => DecimalType
    case JsonNodeType.OBJECT => StructType
    case JsonNodeType.POJO => StructType
    case JsonNodeType.STRING => StringType
    case JsonNodeType.MISSING => StringType
    case _ => StringType
  }

  def getNodeTypeByEsType(estype: String): DataType = estype.toLowerCase(Locale.getDefault) match {
    case "long" | "integer" | "short" | "byte" | "double" | "float" | "half_float" |
        "scaled_float" =>
      DecimalType
    case "text" | "keyword" => StringType
    case "date" => DateType
    case "binary" => BinaryType
    case _ => StringType
  }

  def getNodeValue(node: JsonNode): Any = node.getNodeType match {
    case JsonNodeType.NUMBER => node.asDouble()
    case JsonNodeType.NULL => null
    case _ => node.toString().replaceAll("\n|\t", " ")
  }

}
