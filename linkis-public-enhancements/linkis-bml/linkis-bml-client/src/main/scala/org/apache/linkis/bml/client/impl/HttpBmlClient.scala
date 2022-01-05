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
 
package org.apache.linkis.bml.client.impl

import java.io.{File, IOException, InputStream}
import java.util

import org.apache.linkis.bml.client.AbstractBmlClient
import org.apache.linkis.bml.common._
import org.apache.linkis.bml.conf.BmlConfiguration._
import org.apache.linkis.bml.http.HttpConf
import org.apache.linkis.bml.protocol._
import org.apache.linkis.bml.request._
import org.apache.linkis.bml.response.{BmlCreateBmlProjectResult, _}
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.config.ClientConfigBuilder
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.DWSClientConfig
import org.apache.linkis.storage.FSFactory
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

class HttpBmlClient(clientConfig: DWSClientConfig,
                   serverUrl: String,
                   properties: util.Map[String, Any]) extends AbstractBmlClient  with Logging {

  def this(serverUrl: String) = this(null, null, null)

  def this() = this(null, null, null)

  def this(clientConfig: DWSClientConfig) = this(clientConfig, null, null)

  def this(serverUrl: String, properties: util.Map[String, Any]) = {
    this(null, serverUrl, properties)
  }

  private val clientName = if(properties == null) "BML-Client"
      else properties.getOrDefault(CLIENT_NAME_SHORT_NAME, "BML-Client").asInstanceOf[String]
    private val dwsClient = new DWSHttpClient(if(clientConfig != null) clientConfig else createClientConfig(), clientName)
    val FIRST_VERSION = "v000001"

  private def createClientConfig(): DWSClientConfig = {
      val _serverUrl = if(StringUtils.isEmpty(serverUrl)) HttpConf.gatewayInstance else serverUrl
      if(StringUtils.isEmpty(_serverUrl)) throw BmlClientFailException("serverUrl cannot be null.")
      val config = if (properties == null) {
        new util.HashMap[String, Object]()
      } else {
        properties
      }

      val maxConnection:Int = config.getOrDefault(CONNECTION_MAX_SIZE_SHORT_NAME, CONNECTION_MAX_SIZE.getValue).asInstanceOf[Int]
      val connectTimeout:Int = config.getOrDefault(CONNECTION_TIMEOUT_SHORT_NAME, CONNECTION_TIMEOUT.getValue).asInstanceOf[Int]
      val readTimeout:Int = config.getOrDefault(CONNECTION_READ_TIMEOUT_SHORT_NAME, CONNECTION_READ_TIMEOUT.getValue).asInstanceOf[Int]
      val tokenKey = config.getOrDefault(AUTH_TOKEN_KEY_SHORT_NAME, AUTH_TOKEN_KEY.getValue).asInstanceOf[String]
      val tokenValue = config.getOrDefault(AUTH_TOKEN_VALUE_SHORT_NAME, AUTH_TOKEN_VALUE.getValue).asInstanceOf[String]

      val authenticationStrategy:AuthenticationStrategy = new TokenAuthenticationStrategy()
      val clientConfig = ClientConfigBuilder.newBuilder()
        .addServerUrl(_serverUrl)
        .connectionTimeout(connectTimeout)
        .discoveryEnabled(false)
        .loadbalancerEnabled(false)
        .maxConnectionSize(maxConnection)
        .retryEnabled(false)
        .readTimeout(readTimeout)
        .setAuthenticationStrategy(authenticationStrategy)
        .setAuthTokenKey(tokenKey)
        .setAuthTokenValue(tokenValue)
        .build()

      val dwsClientConfig = new DWSClientConfig(clientConfig)
      dwsClientConfig.setDWSVersion(Configuration.LINKIS_WEB_VERSION.getValue)
      dwsClientConfig
   }

  override def downloadResource(user:String, resourceID: String): BmlDownloadResponse = {
    downloadResource(user, resourceID, "")
  }

  override def downloadResource(user: String, resourceId: String, version: String): BmlDownloadResponse = {
    val bmlDownloadAction = BmlDownloadAction()
    import scala.collection.JavaConversions._
    bmlDownloadAction.getParameters += "resourceId" -> resourceId
    // TODO: 不能放非空的参数
    if(version != null)bmlDownloadAction.getParameters += "version" -> version
    bmlDownloadAction.setUser(user)
    val result = dwsClient.execute(bmlDownloadAction)
    BmlDownloadResponse(isSuccess = true, bmlDownloadAction.getInputStream, resourceId, version, null)
  }

  /**
    * 下载资源到指定的path中
    * @param user 用户名
    * @param resourceId 资源ID
    * @param version 版本信息
    * @param path 指定的目录,前面要加schema share:// local:// 等
    * @param overwrite 是否是追加
    * @return 返回的inputStream已经被全部读完，所以返回一个null,另外的fullFileName是整个文件的名字
    */
  override def downloadResource(user: String, resourceId: String, version: String, path: String, overwrite:Boolean = false): BmlDownloadResponse = {
    val fsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, user)
    fileSystem.init(new util.HashMap[String, String]())
    val fullFileName = path
    val downloadAction = BmlDownloadAction()
    import scala.collection.JavaConversions._
    downloadAction.getParameters += "resourceId" -> resourceId
    if(StringUtils.isNotEmpty(version)) downloadAction.getParameters += "version" -> version
    downloadAction.setUser(user)
    val downloadResult = dwsClient.execute(downloadAction)
    val fullFilePath = new FsPath(fullFileName)
    if (downloadResult != null){
      val inputStream = downloadAction.getInputStream
      val outputStream = fileSystem.write(fullFilePath, overwrite)
      try{
        IOUtils.copy(inputStream, outputStream)
      }catch{
        case e:IOException => logger.error("failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)", e)
          val exception = BmlClientFailException("failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)")
          exception.initCause(e)
          throw e
        case t:Throwable => logger.error("failed to copy stream (流复制失败)",t)
          throw t
      }finally{
        IOUtils.closeQuietly(inputStream)
        IOUtils.closeQuietly(outputStream)
      }
      BmlDownloadResponse(isSuccess = true, null, resourceId, version, fullFileName)
    }else{
      BmlDownloadResponse(isSuccess = false, null, null, null, null)
    }
  }


  override def downloadShareResource(user: String, resourceId: String, version: String, path: String,
                                     overwrite:Boolean = false): BmlDownloadResponse = {
    val fsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, user)
    fileSystem.init(new util.HashMap[String, String]())
    val fullFileName = path
    val downloadAction = BmlDownloadShareAction()
    import scala.collection.JavaConversions._
    downloadAction.getParameters += "resourceId" -> resourceId
    if(StringUtils.isNotEmpty(version)) downloadAction.getParameters += "version" -> version
    downloadAction.setUser(user)
    val downloadResult = dwsClient.execute(downloadAction)
    val fullFilePath = new FsPath(fullFileName)
    if (downloadResult != null){
      val inputStream = downloadAction.getInputStream
      val outputStream = fileSystem.write(fullFilePath, overwrite)
      try{
        IOUtils.copy(inputStream, outputStream)
      }catch{
        case e:IOException => logger.error("failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)", e)
          val exception = BmlClientFailException("failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)")
          exception.initCause(e)
          throw e
        case t:Throwable => logger.error("failed to copy stream (流复制失败)",t)
          throw t
      }finally{
        IOUtils.closeQuietly(inputStream)
        IOUtils.closeQuietly(outputStream)
      }
      BmlDownloadResponse(isSuccess = true, null, resourceId, version, fullFileName)
    }else{
      BmlDownloadResponse(isSuccess = false, null, null, null, null)
    }
  }



  /**
    * 更新资源信息
    *
    * @param resourceID 资源id
    * @param filePath   目标文件路径
    * @return resourceId 新的版本信息
    */
  override def updateResource(user:String, resourceID: String, filePath: String): BmlUpdateResponse = {
    val inputStream:InputStream = getInputStream(filePath)
    updateResource(user, resourceID, filePath, inputStream)
  }

  override def updateResource(user:String, resourceID: String, filePath: String, inputStream: InputStream): BmlUpdateResponse = {
    val _inputStreams = new util.HashMap[String, InputStream]()
    _inputStreams.put("file", inputStream)
    val bmlUpdateAction = BmlUpdateAction(null, _inputStreams)
    bmlUpdateAction.setUser(user)
    bmlUpdateAction.inputStreamNames.put("file", pathToName(filePath))
    bmlUpdateAction.getParameters.put("resourceId",resourceID)
    val result = dwsClient.execute(bmlUpdateAction)
    result match{
      case updateResult:BmlUpdateResult => val isSuccess= if (updateResult.getStatus == 0) true else false
        if (isSuccess){
          val resourceId = updateResult.getResourceId
          val version = updateResult.getVersion
          BmlUpdateResponse(isSuccess, resourceId, version)
        }else{
          logger.error(s"user $user update resource failed, status code is ${updateResult.getStatusCode}")
          BmlUpdateResponse(isSuccess, null, null)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlResourceDownloadResult")
        throw POSTResultNotMatchException()
      case _ => throw POSTResultNotMatchException()
    }
  }

  /**
    * relateResource方法将targetFilePath路径的文件关联到resourceID下面
    * targetFilePath需要包括schema，如果不包含schema，默认是hdfs
    *
    * @param resourceID     resourceID
    * @param targetFilePath 指定文件目录
    * @return BmlRelateResult  包含resourceId和新的version
    */
  override def relateResource(resourceID: String, targetFilePath: String): BmlRelateResponse = {
    null
  }




  /**
    * 获取resourceid 对应资源的所有版本
    * @param user       用户名
    * @param resourceId 资源Id
    * @return resourceId对应下的所有版本信息
    */
  override def getVersions(user: String, resourceId: String): BmlResourceVersionsResponse = {
    val getVersionsAction = BmlGetVersionsAction(user, resourceId)
    val result = dwsClient.execute(getVersionsAction)
    result match{
      case _result:BmlResourceVersionResult => val isSuccess= if (_result.getStatus == 0) true else false
        if (isSuccess){
          val resourceId = _result.getResourceId
          val resourceVersions = _result.getResourceVersions
          BmlResourceVersionsResponse(isSuccess,resourceId, resourceVersions)
        }else{
          logger.error(s"user $user get versions failed, status code is ${_result.getStatusCode}")
          BmlResourceVersionsResponse(isSuccess, null, null)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlResourceDownloadResult")
        throw POSTResultNotMatchException()
      case _ => throw POSTResultNotMatchException()
    }
  }




  /**
    * 上传文件，用户指定文件路径，客户端自动获取输入流
    * @param user     用户名
    * @param filePath 文件路径
    * @return 包含resourceId和version
    */
  override def uploadResource(user: String, filePath: String): BmlUploadResponse = {
    val inputStream:InputStream = getInputStream(filePath)
    uploadResource(user, filePath, inputStream)
  }


  private def pathToName(filePath:String):String = new File(filePath).getName


  /**
    * 上传资源
    *
    * @param user        用户名
    * @param filePath    上传的资源的路径
    * @param inputStream 上传资源的输入流
    * @return
    */
  override def uploadResource(user: String, filePath: String, inputStream: InputStream): BmlUploadResponse = {
    val _inputStreams = new util.HashMap[String, InputStream]()
    _inputStreams.put("file", inputStream)
    val uploadAction = BmlUploadAction(null, _inputStreams)
    uploadAction.inputStreamNames.put("file", pathToName(filePath))
    uploadAction.setUser(user)
    val result = dwsClient.execute(uploadAction)
    result match {
      case bmlUploadResult:BmlUploadResult => val isSuccess = if(bmlUploadResult.getStatus == 0) true else false
        if (isSuccess){
          val resourceId = bmlUploadResult.getResourceId
          val version =  bmlUploadResult.getVersion
          BmlUploadResponse(isSuccess, resourceId,version)
        }else{
          logger.error(s"user $user upload resource failed, status code is ${bmlUploadResult.getStatusCode}")
          BmlUploadResponse(isSuccess, null, null)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlResourceDownloadResult")
        throw POSTResultNotMatchException()
      case _ => throw POSTResultNotMatchException()
    }
  }


  /**
   *
   */
  override def deleteResource(user: String, resourceId: String, version: String): BmlDeleteResponse = {
    null
  }

  override def deleteResource(user: String, resourceId: String): BmlDeleteResponse = {
    val deleteAction = BmlDeleteAction(resourceId)
    deleteAction.getParameters.put("resourceId", resourceId)
    val result = dwsClient.execute(deleteAction)
    result match {
      case bmlDeleteResult: BmlDeleteResult => val isSuccess= if (bmlDeleteResult.getStatus == 0) true else false
        if (isSuccess){
          BmlDeleteResponse(isSuccess)
        }else{
          logger.error(s"user $user update resource failed, status code is ${bmlDeleteResult.getStatusCode}")
          BmlDeleteResponse(isSuccess)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlResourceDownloadResult")
        throw POSTResultNotMatchException()
      case _ =>  throw POSTResultNotMatchException()
    }
  }

  //todo 现在是为了通过编译
  private def getInputStream(str: String):InputStream = {
    null
  }

  override def createBmlProject(creator: String,
                                projectName: String,
                                accessUsers: util.List[String], editUsers: util.List[String]): BmlCreateProjectResponse = {
    val createBmlProjectAction = CreateBmlProjectAction()
    createBmlProjectAction.setUser(creator)
    createBmlProjectAction.getRequestPayloads.put("projectName", projectName)
    createBmlProjectAction.getRequestPayloads.put("editUsers", editUsers)
    createBmlProjectAction.getRequestPayloads.put("accessUsers", accessUsers)
    val result = dwsClient.execute(createBmlProjectAction)
    result match {
      case bmlCreateBmlProjectResult: BmlCreateBmlProjectResult => val isSuccess= if (bmlCreateBmlProjectResult.getStatus == 0) true else false
        if (isSuccess){
          BmlCreateProjectResponse(isSuccess)
        }else{
          logger.error(s"user $user create bml project, status code is ${bmlCreateBmlProjectResult.getStatusCode}")
          BmlCreateProjectResponse(isSuccess)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlCreateBmlProjectResult")
        throw POSTResultNotMatchException()
      case _ =>  throw POSTResultNotMatchException()
    }
  }

  override def uploadShareResource(user: String, projectName: String, filePath: String,
                                   inputStream: InputStream): BmlUploadResponse = {
    val _inputStreams = new util.HashMap[String, InputStream]()
    _inputStreams.put("file", inputStream)
    val uploadAction = BmlUploadShareResourceAction(null, _inputStreams)
    uploadAction.inputStreamNames.put("file", pathToName(filePath))
    uploadAction.setUser(user)
    uploadAction.getParameters.put("projectName", projectName)
    uploadAction.getRequestPayloads.put("projectName", projectName)
    val result = dwsClient.execute(uploadAction)
    result match {
      case bmlUploadResult:BmlUploadShareResourceResult => val isSuccess = if(bmlUploadResult.getStatus == 0) true else false
        if (isSuccess){
          val resourceId = bmlUploadResult.getResourceId
          val version =  bmlUploadResult.getVersion
          BmlUploadResponse(isSuccess, resourceId,version)
        }else{
          logger.error(s"user $user upload resource failed, status code is ${bmlUploadResult.getStatusCode}")
          BmlUploadResponse(isSuccess, null, null)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlResourceDownloadResult")
        throw POSTResultNotMatchException()
      case _ => throw POSTResultNotMatchException()
    }
  }

  override def uploadShareResource(user: String, projectName: String, filePath: String): BmlUploadResponse = ???

  override def downloadShareResource(user: String, resourceId: String, version: String): BmlDownloadResponse = {
    val bmlDownloadShareAction = BmlDownloadShareAction()
    import scala.collection.JavaConversions._
    bmlDownloadShareAction.getParameters += "resourceId" -> resourceId
    // TODO: 不能放非空的参数
    if(version != null)bmlDownloadShareAction.getParameters += "version" -> version
    bmlDownloadShareAction.setUser(user)
    val result = dwsClient.execute(bmlDownloadShareAction)
    BmlDownloadResponse(isSuccess = true, bmlDownloadShareAction.getInputStream, resourceId, version, null)
  }

  override def downloadShareResource(user: String, resourceId: String): BmlDownloadResponse = ???

  override def updateShareResource(user: String, resourceId: String, filePath: String,
                                   inputStream: InputStream): BmlUpdateResponse = {
    val _inputStreams = new util.HashMap[String, InputStream]()
    _inputStreams.put("file", inputStream)
    val bmlUpdateShareResourceAction = BmlUpdateShareResourceAction(null, _inputStreams)
    bmlUpdateShareResourceAction.setUser(user)
    bmlUpdateShareResourceAction.inputStreamNames.put("file", pathToName(filePath))
    bmlUpdateShareResourceAction.getParameters.put("resourceId",resourceId)
    val result = dwsClient.execute(bmlUpdateShareResourceAction)
    result match{
      case bmlUpdateShareResourceResult:BmlUpdateShareResourceResult => val isSuccess = if (bmlUpdateShareResourceResult.getStatus == 0) true else false
        if (isSuccess){
          val resourceId = bmlUpdateShareResourceResult.getResourceId
          val version = bmlUpdateShareResourceResult.getVersion
          BmlUpdateResponse(isSuccess, resourceId, version)
        }else{
          logger.error(s"user $user update resource failed, status code is ${bmlUpdateShareResourceResult.getStatusCode}")
          BmlUpdateResponse(isSuccess, null, null)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlResourceDownloadResult")
        throw POSTResultNotMatchException()
      case _ => throw POSTResultNotMatchException()
    }
  }

  override def updateShareResource(user: String, resourceId: String, filePath: String): BmlUpdateResponse = ???

  override def getProjectInfoByName(projectName: String): BmlProjectInfoResponse = ???

  override def getResourceInfo(resourceId: String): BmlResourceInfoResponse = ???

  override def getProjectPriv(projectName: String): BmlProjectPrivResponse = ???

  override def attachResourceAndProject(projectName: String, resourceId: String): BmlAttachResourceAndProjectResponse = {
    val bmlAttachAction = BmlAttachAction()
    bmlAttachAction.setUser(this.getUser)
    bmlAttachAction.getRequestPayloads.put("projectName", projectName)
    bmlAttachAction.getRequestPayloads.put("resourceId", resourceId)
    val result = dwsClient.execute(bmlAttachAction)
    result match {
      case bmlAttachResult: BmlAttachResult => val isSuccess= if (bmlAttachResult.getStatus == 0) true else false
        if (isSuccess){
          BmlAttachResourceAndProjectResponse(isSuccess)
        }else{
          logger.error(s"user $user create bml project, status code is ${bmlAttachResult.getStatusCode}")
          BmlAttachResourceAndProjectResponse(isSuccess)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlCreateBmlProjectResult")
        throw POSTResultNotMatchException()
      case _ =>  throw POSTResultNotMatchException()
    }
  }

  override def updateProjectPriv(username: String,
                                 projectName: String,
                                 editUsers: util.List[String],
                                 accessUsers: util.List[String]): BmlUpdateProjectPrivResponse = {
    val updateBmlProjectAction = UpdateBmlProjectAction()
    updateBmlProjectAction.setUser(username)
    updateBmlProjectAction.getRequestPayloads.put("projectName", projectName)
    updateBmlProjectAction.getRequestPayloads.put("editUsers", editUsers)
    updateBmlProjectAction.getRequestPayloads.put("accessUsers", accessUsers)
    val result = dwsClient.execute(updateBmlProjectAction)
    result match {
      case bmlUpdateProjectResult: BmlUpdateProjectResult => val isSuccess = if (bmlUpdateProjectResult.getStatus == 0) true else false
        if (isSuccess){
          BmlUpdateProjectPrivResponse(isSuccess)
        }else{
          logger.error(s"user $user update bml project, status code is ${bmlUpdateProjectResult.getStatusCode}")
          BmlUpdateProjectPrivResponse(isSuccess)
        }
      case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlUpdateProjectResult")
        throw POSTResultNotMatchException()
      case _ =>  throw POSTResultNotMatchException()
    }
  }

  override def close(): Unit = dwsClient.close()
}
