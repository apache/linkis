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

package org.apache.linkis.httpclient.request

import org.apache.http.entity.ContentType

import java.util

import scala.tools.nsc.interpreter.InputStream

trait UploadAction extends UserAction {

  /**
   * The file to be uploaded, the key is the parameter name, and the value is the file path.
   * 需要上传的文件，key为参数名，value为文件路径
   */
  @deprecated val files: util.Map[String, String]

  /**
   * The inputStream that needs to be uploaded, the key is the parameter name, and the value is the
   * input stream. 需要上传的输入流，key为参数名，value为输入流
   */
  def inputStreams: util.Map[String, InputStream] = new util.HashMap[String, InputStream]()

  /**
   * The inputStream that needs to be uploaded, the key is the parameter name, and the value is the
   * fileName of inputStream. 需要上传的输入流，key为参数名，value为输入流的文件名
   */
  @deprecated def inputStreamNames: util.Map[String, String] = new util.HashMap[String, String]()
  def binaryBodies: util.List[BinaryBody] = new util.ArrayList[BinaryBody](0)
  def user: Option[String] = Option(getUser)

}

case class BinaryBody(
    parameterName: String,
    inputStream: InputStream,
    fileName: String,
    contentType: ContentType
)

object BinaryBody {

  def apply(
      parameterName: String,
      inputStream: InputStream,
      fileName: String,
      contentType: String
  ): BinaryBody =
    new BinaryBody(parameterName, inputStream, fileName, ContentType.create(contentType))

  def apply(parameterName: String, inputStream: InputStream): BinaryBody =
    new BinaryBody(parameterName, inputStream, null, ContentType.DEFAULT_BINARY)

  def apply(parameterName: String, inputStream: InputStream, fileName: String): BinaryBody =
    new BinaryBody(parameterName, inputStream, fileName, ContentType.DEFAULT_BINARY)

}
