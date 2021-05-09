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
package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client

import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client.impl.ResponseHandlerImpl
import com.webank.wedatasphere.linkis.storage.domain._
import org.elasticsearch.client.Response

/**
 *
 * @author wang_zh
 * @date 2020/5/12
 */
trait ResponseHandler extends Logging {

  def handle(response: Response, storePath: String, alias: String): String

  def handle(response: Response, storePath: String, alias: String, proxyUser: String): String

}


object ResponseHandler {

  val RESPONSE_HANDLER = new ResponseHandlerImpl()

  def handle(response: Response, storePath: String, alias: String): String =
    RESPONSE_HANDLER.handle(response, storePath, alias)

  def handle(response: Response, storePath: String, alias: String, proxyUser: String): String =
    RESPONSE_HANDLER.handle(response, storePath, alias, proxyUser)

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

  def getNodeTypeByEsType(estype: String): DataType = estype.toLowerCase match {
    case "long" | "integer" | "short" | "byte" | "double" | "float" | "half_float" | "scaled_float" => DecimalType
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