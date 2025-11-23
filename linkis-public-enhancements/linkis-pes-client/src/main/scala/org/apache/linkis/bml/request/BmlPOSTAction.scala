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

package org.apache.linkis.bml.request

import org.apache.linkis.bml.http.HttpConf
import org.apache.linkis.httpclient.request._

import org.apache.http.HttpResponse

import java.{lang, util}
import java.io.{File, InputStream}
import java.lang.reflect.Type

import com.google.gson._

trait BmlAction extends UserAction {

  private var user: String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  implicit val gson = new GsonBuilder()
    .setPrettyPrinting()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .serializeNulls
    .registerTypeAdapter(
      classOf[java.lang.Double],
      new JsonSerializer[java.lang.Double] {

        override def serialize(
            t: lang.Double,
            `type`: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement =
          if (t == t.longValue()) new JsonPrimitive(t.longValue()) else new JsonPrimitive(t)

      }
    )
    .create

}

abstract class BmlPOSTAction extends POSTAction with BmlAction {
  override def getRequestPayload: String = gson.toJson(getRequestPayloads)
}

abstract class BmlGETAction extends GetAction with BmlAction

/**
 * BmlUpload
 * @param filePaths
 * @param _inputStreams
 */
case class BmlUploadAction(filePaths: Array[String], _inputStreams: util.Map[String, InputStream])
    extends BmlPOSTAction
    with UploadAction {

  private val streamNames = new util.HashMap[String, String]

  override val files: util.Map[String, String] = {
    if (null == filePaths || filePaths.length == 0) new util.HashMap[String, String]()
    else {
      val map = new java.util.HashMap[String, String]
      filePaths foreach { filePath =>
        val arr = filePath.split(File.separator)
        val fileName = arr(arr.length - 1)
        map.put("file", filePath)
      }
      map
    }
  }

  override def inputStreams: util.Map[String, InputStream] = _inputStreams

  override def inputStreamNames: util.Map[String, String] = streamNames

  private var _user: String = _

  override def setUser(user: String): Unit = this._user = user

  override def getUser: String = this._user

  override def getRequestPayload: String = ""

  override def getURL: String = HttpConf.uploadURL
}

case class BmlUploadShareResourceAction(
    filePaths: Array[String],
    _inputStreams: util.Map[String, InputStream]
) extends BmlPOSTAction
    with UploadAction {
  override def getURL: String = HttpConf.uploadShareResourceUrl
  private val streamNames = new util.HashMap[String, String]

  override val files: util.Map[String, String] = {
    if (null == filePaths || filePaths.length == 0) new util.HashMap[String, String]()
    else {
      val map = new java.util.HashMap[String, String]
      filePaths foreach { filePath =>
        val arr = filePath.split(File.separator)
        val fileName = arr(arr.length - 1)
        map.put("file", filePath)
      }
      map
    }
  }

  override def inputStreams: util.Map[String, InputStream] = _inputStreams

  override def inputStreamNames: util.Map[String, String] = streamNames

  private var _user: String = _

  override def setUser(user: String): Unit = this._user = user

  override def getUser: String = this._user

  override def getRequestPayload: String = ""
}

case class BmlUpdateAction(filePaths: Array[String], _inputStreams: util.Map[String, InputStream])
    extends BmlPOSTAction
    with UploadAction {
  override def getURL: String = HttpConf.updateVersionURL

  override def getRequestPayload: String = ""

  private var _user: String = _

  private val streamNames = new util.HashMap[String, String]

  override val files: util.Map[String, String] = {
    if (null == filePaths || filePaths.length == 0) new util.HashMap[String, String]()
    else {
      val map = new java.util.HashMap[String, String]
      filePaths foreach { filePath =>
        val arr = filePath.split(File.separator)
        val fileName = arr(arr.length - 1)
        map.put("file", filePath)
      }
      map
    }
  }

  override def setUser(user: String): Unit = this._user = user

  override def getUser: String = this._user
  override def inputStreams: util.Map[String, InputStream] = _inputStreams

  override def inputStreamNames: util.Map[String, String] = streamNames
}

case class BmlUpdateShareResourceAction(
    filePaths: Array[String],
    _inputStreams: util.Map[String, InputStream]
) extends BmlPOSTAction
    with UploadAction {
  override def getURL: String = HttpConf.updateShareResourceUrl

  override def getRequestPayload: String = ""

  private var _user: String = _

  private val streamNames = new util.HashMap[String, String]

  override val files: util.Map[String, String] = {
    if (null == filePaths || filePaths.length == 0) new util.HashMap[String, String]()
    else {
      val map = new java.util.HashMap[String, String]
      filePaths foreach { filePath =>
        val arr = filePath.split(File.separator)
        val fileName = arr(arr.length - 1)
        map.put("file", filePath)
      }
      map
    }
  }

  override def setUser(user: String): Unit = this._user = user

  override def getUser: String = this._user
  override def inputStreams: util.Map[String, InputStream] = _inputStreams

  override def inputStreamNames: util.Map[String, String] = streamNames
}

case class BmlDownloadAction() extends BmlGETAction with DownloadAction with UserAction {

  private var inputStream: InputStream = _
  private var user: String = _
  private var respoonse: HttpResponse = _

  def getInputStream: InputStream = this.inputStream

  def setInputStream(inputStream: InputStream): Unit = this.inputStream = inputStream

  override def getURL: String = HttpConf.downloadURL

  override def write(inputStream: InputStream): Unit = this.inputStream = inputStream

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def getResponse: HttpResponse = respoonse

  override def setResponse(response: HttpResponse): Unit = this.respoonse = response
}

case class BmlDownloadShareAction() extends BmlGETAction with DownloadAction with UserAction {

  private var inputStream: InputStream = _
  private var user: String = _
  private var response: HttpResponse = _

  def getInputStream: InputStream = this.inputStream

  def setInputStream(inputStream: InputStream): Unit = this.inputStream = inputStream

  override def getURL: String = HttpConf.downloadShareURL

  override def write(inputStream: InputStream): Unit = this.inputStream = inputStream

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  override def getResponse: HttpResponse = response

  override def setResponse(response: HttpResponse): Unit = this.response = response
}

case class BmlRelateAction(user: String, resourceId: String, inputStream: InputStream)
    extends BmlPOSTAction {
  override def getRequestPayload: String = ""

  override def getURL: String = HttpConf.updateVersionURL
}

case class BmlGetVersionsAction(user: String, resourceId: String) extends BmlPOSTAction {
  override def getRequestPayload: String = ""

  override def getURL: String = HttpConf.getVersionsUrl
}

case class BmlUpdateBasicAction(properties: java.util.Map[String, String]) extends BmlPOSTAction {
  override def getRequestPayload: String = ""

  override def getURL: String = HttpConf.updateBasicUrl
}

case class BmlGetBasicAction(resourceId: String) extends BmlGETAction with UserAction {

  private var user: String = _

  override def getURL: String = HttpConf.getBasicUrl

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

case class BmlDeleteAction(resourceId: String) extends BmlPOSTAction {
  override def getRequestPayload: String = ""

  override def getURL: String = HttpConf.deleteURL
}

case class CreateBmlProjectAction() extends BmlPOSTAction {
  override def getURL: String = HttpConf.createProjectUrl
}

case class UpdateBmlProjectAction() extends BmlPOSTAction {
  override def getURL: String = HttpConf.updateProjectUrl
}

case class BmlAttachAction() extends BmlPOSTAction {
  override def getURL: String = HttpConf.attachUrl
}

case class BmlChangeOwnerAction() extends BmlPOSTAction {
  override def getURL: String = HttpConf.changeOwnerUrl
}

case class BmlCopyResourceAction() extends BmlPOSTAction {
  override def getURL: String = HttpConf.copyResourceUrl
}

case class BmlRollbackVersionAction() extends BmlPOSTAction {
  override def getURL: String = HttpConf.rollbackVersionUrl
}
