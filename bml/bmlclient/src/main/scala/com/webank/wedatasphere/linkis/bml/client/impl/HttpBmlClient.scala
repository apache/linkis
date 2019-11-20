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
package com.webank.wedatasphere.linkis.bml.client.impl

import java.io.{File, IOException, InputStream}
import java.util

import com.webank.wedatasphere.linkis.bml.client.AbstractBmlClient
import com.webank.wedatasphere.linkis.bml.common._
import com.webank.wedatasphere.linkis.bml.conf.BmlConfiguration
import com.webank.wedatasphere.linkis.bml.http.HttpConf
import com.webank.wedatasphere.linkis.bml.protocol._
import com.webank.wedatasphere.linkis.bml.request._
import com.webank.wedatasphere.linkis.bml.response._
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.config.{ClientConfig, ClientConfigBuilder}
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.{StaticAuthenticationStrategy, TokenAuthenticationStrategy}
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig
import com.webank.wedatasphere.linkis.storage.FSFactory
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

/**
  * created by cooperyang on 2019/5/23
  * Description:
  */
class HttpBmlClient extends AbstractBmlClient{

  private val logger:Logger = LoggerFactory.getLogger(classOf[HttpBmlClient])

  val serverUrl:String = HttpConf.gatewayInstance
  val maxConnection:Int = 10
  val readTimeout:Int = 10000
  val authenticationStrategy:AuthenticationStrategy = new TokenAuthenticationStrategy()
  val clientConfig:ClientConfig = ClientConfigBuilder.newBuilder().addUJESServerUrl(serverUrl)
    .connectionTimeout(30000).discoveryEnabled(false)
    .loadbalancerEnabled(false).maxConnectionSize(maxConnection)
    .retryEnabled(false).readTimeout(readTimeout)
    .setAuthenticationStrategy(authenticationStrategy).setAuthTokenKey(BmlConfiguration.AUTH_TOKEN_KEY.getValue)
    .setAuthTokenValue(BmlConfiguration.AUTH_TOKEN_VALUE.getValue).build()
  val dwsClientConfig:DWSClientConfig = new DWSClientConfig(clientConfig)
  dwsClientConfig.setDWSVersion(BmlConfiguration.DWS_VERSION.getValue)
  val dwsClientName:String = "BML-Client"
  val dwsClient:DWSHttpClient = new DWSHttpClient(dwsClientConfig, dwsClientName)

  val FIRST_VERSION:String = "v000001"



  override def downloadResource(user:String, resourceID: String): BmlDownloadResponse = {
    downloadResource(user, resourceID, "")
  }

  override def downloadResource(user: String, resourceId: String, version: String): BmlDownloadResponse = {
    val bmlDownloadAction = BmlDownloadAction()
    import scala.collection.JavaConversions._
    bmlDownloadAction.getParameters +="resourceId"->resourceId
    // TODO: 不能放非空的参数
    if(version != null)bmlDownloadAction.getParameters +="version"->version
    bmlDownloadAction.setUser(user)
    val result = dwsClient.execute(bmlDownloadAction)
    new BmlDownloadResponse(true,bmlDownloadAction.getInputStream,resourceId,version,null)
    /*    result match {
          case downloadResult:BmlResourceDownloadResult => val isSuccess = if (downloadResult.getStatusCode == 0) true else false
            if (isSuccess){
              downloadResult.setInputStream(bmlDownloadAction.getInputStream)
              BmlDownloadResponse(isSuccess, downloadResult.inputStream, downloadResult.getResourceId, downloadResult.getVersion, "")
            }else{
              logger.error(s"user ${user} download resource $resourceId  version $version failed, status code is ${ downloadResult.getStatusCode}")
              BmlDownloadResponse(isSuccess, null, null, null, null)
            }
          case r:BmlResult => logger.error(s"result type ${r.getResultType} not match BmlResourceDownloadResult")
            throw POSTResultNotMatchException()
          case _ => throw POSTResultNotMatchException()
        }*/
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
    //1检查目录是否存在,包括path的schema
    //2检查文件是否存在，如果文件存在，并且overwrite是false，则报错
    //3获取downloaded_file_name 拼成一个完整的filePath
    //4获取inputStream，然后写入到filePath中
    val fsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, user)
    fileSystem.init(new util.HashMap[String, String]())
//    if (fileSystem.exists(fsPath)){
//      logger.error(s"path $path not exists")
//      throw IllegalPathException()
//    }
//    val getBasicAction = BmlGetBasicAction(resourceId)
//    val getBasicResult = dwsClient.execute(getBasicAction) match{
//      case result:BmlGetBasicResult => result
//      case _ => throw GetResultNotMatchException()
//    }

//    val fileName:StringBuilder = new StringBuilder
//    fileName.append(path).append(if (path.endsWith("/")) "" else "/")

//    if (getBasicResult != null && getBasicResult.getStatusCode == 0){
//      val downloadedFileName = getBasicResult.downloadedFileName
//      if (StringUtils.isNotEmpty(downloadedFileName)){
//        fileName.append(downloadedFileName)
//      }else{
//        throw BmlResponseErrorException("返回的downloadedFileName参数为空")
//      }
//    }else{
//      logger.error(s"获取 $resourceId 资源失败, BmlServer的返回码是 ${getBasicResult.getStatusCode}")
//      throw BmlResponseErrorException("通过http方式获取")
//    }

    val fullFileName = path
    val downloadAction = BmlDownloadAction() // TODO: 这里暂时还没改
    import scala.collection.JavaConversions._
    downloadAction.getParameters += "resourceId" -> resourceId
    // TODO: 不能放非空的参数
    if(version != null) downloadAction.getParameters += "version" -> version
    downloadAction.setUser(user)
    val downloadResult = dwsClient.execute(downloadAction)
    val fullFilePath = new FsPath(fullFileName)
    if (downloadResult != null){
      val inputStream = downloadAction.getInputStream
      val outputStream = fileSystem.write(fullFilePath, overwrite)
      try{
        IOUtils.copy(inputStream, outputStream)
      }catch{
        case e:IOException => logger.error("inputStream和outputStream流copy失败", e)
          val exception = BmlClientFailException("inputStream和outputStream流copy失败")
          exception.initCause(e)
          throw e
        case t:Throwable => logger.error("流复制失败",t)
          throw t
      }finally{
        IOUtils.closeQuietly(inputStream)
        IOUtils.closeQuietly(outputStream)
      }
      BmlDownloadResponse(true, null, resourceId, version, fullFileName)
    }else{
      BmlDownloadResponse(false, null, null, null, null)
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


  //todo 现在是为了通过编译
  private def getInputStream(str: String):InputStream = {
    null
  }

}
