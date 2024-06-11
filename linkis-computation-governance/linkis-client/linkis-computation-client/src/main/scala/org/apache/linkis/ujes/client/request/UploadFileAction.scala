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

package org.apache.linkis.ujes.client.request

import org.apache.linkis.httpclient.request.{BinaryBody, GetAction, UploadAction}
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

import org.apache.http.entity.ContentType

import java.io.{File, FileInputStream}
import java.util

import scala.collection.JavaConverters._

class UploadFileAction extends GetAction with UploadAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("filesystem", "upload")

  override val files: util.Map[String, String] = new util.HashMap[String, String]()

  override val binaryBodies: util.List[BinaryBody] = new util.ArrayList[BinaryBody](0)

}

object UploadFileAction {
  def builder(): Builder = new Builder

  class Builder private[UploadFileAction] {
    private var user: String = _
    private var path: String = _
    private var uploadFiles: util.List[File] = new util.ArrayList[File](0)

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setPath(path: String): Builder = {
      this.path = path
      this
    }

    def addFile(file: File): Builder = {
      this.uploadFiles.add(file)
      this
    }

    def build(): UploadFileAction = {
      val uploadFileAction = new UploadFileAction
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      if (path == null) throw new UJESClientBuilderException("path is needed!")

      uploadFileAction.setUser(user)
      uploadFileAction.setParameter("path", path)
      uploadFiles.asScala.foreach { file =>
        println(String.format("=============== upload file ========== %s ", file.getAbsolutePath))
        uploadFileAction.binaryBodies.add(
          BinaryBody
            .apply("file", new FileInputStream(file), file.getName, ContentType.MULTIPART_FORM_DATA)
        )
      }

      uploadFileAction
    }

  }

}
