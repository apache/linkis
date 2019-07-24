/*
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

package com.webank.wedatasphere.linkis.common.utils

import java.io.{Closeable, File, InputStream}
import java.util
import java.util.{Map => JMap}

import com.ning.http.multipart.{FilePart, PartSource, StringPart}
import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import dispatch._
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpException
import org.json4s._
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by enjoyyin on 2018/3/10.
  */
trait HttpClient extends Closeable {

  protected implicit val executors: ExecutionContext
  protected implicit val formats: Formats = DefaultFormats
  protected val httpTimeout = Duration.Inf

  protected var fileService: Option[FileService] = None
  protected var fs: Option[Fs] = None

  private implicit def svc(url: String): Req =
    dispatch.url(url)

  private def response(req: Req): JMap[String, Object] = {
    val response = Http(req).map { r =>
      val responseBody = r.getResponseBody
      if(r.getContentType == "application/json" || r.getContentType == null) {
        if(StringUtils.isEmpty(responseBody)) new util.HashMap[String, Object]
        else mapAsJavaMap(read[Map[String, Object]](responseBody))
      } else {
        if(responseBody.startsWith("{") && responseBody.endsWith("}")) mapAsJavaMap(read[Map[String, Object]](responseBody))
        else if(responseBody.length > 200) throw new HttpException(responseBody.substring(0, 200))
        else throw new HttpException(responseBody)
      }
    }
    Await.result(response, httpTimeout)
  }

  @Deprecated
  protected def uploadForFileService(url: String, files: Map[String, String], queryParams: Map[String, String],
                       headerParams: Map[String, String], user: String, paths: String*): JMap[String, Object] = {
    if(fileService.isEmpty) throw new HttpException("Method is not available! Reason: fileService!(方法不可用！原因：fileService！)")
    uploadFor(url, files, queryParams, headerParams, paths: _*)(user)
  }

  protected def uploadForFs(url: String, files: Map[String, String], queryParams: Map[String, String],
                                     headerParams: Map[String, String], paths: String*): JMap[String, Object] = {
    if(fs.isEmpty) throw new HttpException("Method is not available! Reason: fs is empty!(方法不可用！原因：fs为空！)")
    uploadFor(url, files, queryParams, headerParams, paths:_*)(null)
  }

  protected def upload(url: String, files: Map[String, String], queryParams: Map[String, String],
                          headerParams: Map[String, String], paths: String*): JMap[String, Object] =
    uploadFor(url, files, queryParams, headerParams, paths:_*)(null)

  private def uploadFor(url: String, files: Map[String, String], queryParams: Map[String, String],
                           headerParams: Map[String, String], paths: String*)(user: String): JMap[String, Object] = {
    var req = url.POST.setContentType("multipart/form-data", Configuration.BDP_ENCODING.getValue)
    if(paths != null) paths.filter(_ != null).foreach(p => req = req / p)
    if(files != null) files.foreach { case (k, v) =>
      if(StringUtils.isEmpty(v)) throw new HttpException(s"The file path of $k cannot be empty!($k 的文件路径不能为空！)")
      val filePart = fileService.map(fs => if(fs.exists(v, user) && fs.isFile(v, user)) new FilePart(k, new FileServicePartSource(fs, v, user))
        else throw new HttpException(s"File $v does not exist!(File $v 不存在！)")).getOrElse {
          fs.map(f => if(f.exists(new FsPath(v))) new FilePart(k, new FsPartSource(f, v)) else throw new HttpException(s"File $v does not exist!（File $v 不存在！）")).getOrElse {
            val f = new File(v)
            if(!f.exists() || !f.isFile) throw new HttpException(s"File $v does not exist!（File $v 不存在！）")
            else new FilePart(k, f)
          }
      }
      req = req.addBodyPart(filePart)
    }
    queryParams.foreach { case (k, v) => req = req.addBodyPart(new StringPart(k, v)) }
    if(headerParams != null && headerParams.nonEmpty) req = req <:< headerParams
    response(req)
  }

  protected def download(url: String, queryParams: Map[String, String], headerParams: Map[String, String],
                         write: InputStream => Unit,
                         paths: String*): Unit = {
    var req = url.GET
    if(headerParams != null && headerParams.nonEmpty) req = req <:< headerParams
    if(queryParams != null) queryParams.foreach{ case (k, v) => req = req.addQueryParameter(k, v)}
    if(paths != null) paths.filter(_ != null).foreach(p => req = req / p)
    val response = Http(req OK as.Response(_.getResponseBodyAsStream)).map(write)
    Await.result(response, httpTimeout)
  }

  protected def http(url: String, queryParams: Map[String, String], formParams: Map[String, String],
                     headerParams: Map[String, String], httpType: String, paths: String*): JMap[String, Object] = {
    var req = svc(url)
    if(headerParams != null && headerParams.nonEmpty) req = req <:< headerParams
    if(queryParams != null) queryParams.foreach{ case (k, v) => req = req.addQueryParameter(k, v)}
    if(paths != null) paths.filter(_ != null).foreach(p => req = req / p)
    if(formParams != null) {
      req = req.setContentType("application/x-www-form-urlencoded", Configuration.BDP_ENCODING.getValue) << formParams
    } else req = req.setContentType("application/json", Configuration.BDP_ENCODING.getValue)
    response(httpType match {
      case "GET" => req.GET
      case "POST" => req.POST
      case "PUT" => req.PUT
      case "DELETE" => req.DELETE
    })
  }

  protected def get(url: String, queryParams: Map[String, String], params: Map[String, String],
                    headerParams: Map[String, String], paths: String*): JMap[String, Object] =
    http(url, queryParams, params, headerParams, "GET", paths:_*)

  protected def post(url: String, queryParams: Map[String, String], params: Map[String, String],
                     headerParams: Map[String, String], paths: String*): JMap[String, Object] =
    http(url, queryParams, params, headerParams, "POST", paths:_*)

  override def close(): Unit = {
    dispatch.Http.shutdown()
  }
}

class FsPartSource(fs: Fs, path: String) extends PartSource {
  val fsPath = fs.get(path)
  override def getLength: Long = fsPath.getLength

  override def createInputStream(): InputStream = fs.read(fsPath)

  override def getFileName: String = fsPath.toFile.getName
}
@Deprecated
class FileServicePartSource(fileService: FileService, path: String, user: String)  extends PartSource {
  override def getLength: Long = fileService.getLength(path, user)

  override def createInputStream(): InputStream = fileService.open(path, user)

  override def getFileName: String = new File(path).getName
}