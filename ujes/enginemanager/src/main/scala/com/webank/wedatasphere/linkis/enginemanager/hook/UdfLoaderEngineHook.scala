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

package com.webank.wedatasphere.linkis.enginemanager.hook

import java.io.{File, StringWriter}
import java.nio.file.{Files, StandardCopyOption}
import java.util.UUID

import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.enginemanager.cache.UdfMapCache
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration.ENGINE_UDF_APP_NAME
import com.webank.wedatasphere.linkis.enginemanager.{Engine, EngineHook}
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.utils.{FileSystemUtils, StorageUtils}
import com.webank.wedatasphere.linkis.udf.api.rpc.{RequestUdfTree, ResponseUdfTree}
import com.webank.wedatasphere.linkis.udf.entity.{UDFInfo, UDFTree}
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang.StringUtils
import org.codehaus.jackson.map.ObjectMapper

import scala.collection.JavaConversions._
import scala.collection.mutable

@Deprecated
//Moved to ujes-engine, for jar loading functionality, see JarLoaderEngineHook
abstract class UdfLoaderEngineHook extends EngineHook with Logging{

  val jsonMapper = new ObjectMapper()
  val ow = jsonMapper.writer().withDefaultPrettyPrinter()

  //val sender = Sender.getSender(EngineManagerConfiguration.ENGINE_UDF_APP_NAME.getValue)
  val udfPathProp = "udf.paths"
  val udfType: BigInt
  val category: String
  var codeBuffer: String = _

  protected def generateCode(udfInfo: UDFInfo): String

  override def beforeCreateSession(requestEngine: RequestEngine): RequestEngine = {
    info("start loading UDFs")
    val udfInfos = extractUdfInfos(requestEngine).filter{info => info.getUdfType == udfType && info.getExpire == false && info.getLoad == true}
//    info("Start fetching remote udf files.")
//    udfInfos.map(_.getPath).foreach(fetchRemoteFile)
//    info("Finish fetching remote udf files.")
    codeBuffer = ""
    udfInfos.map(generateCode).map(putEngineCode(_, requestEngine))
    //write to buffer file
    putUdfPath(writeFile(codeBuffer, requestEngine.user), requestEngine)
    info("end loading UDFs")
    requestEngine
  }

  protected def putEngineCode(code: String, requestEngine: RequestEngine): Unit = {
    info("got engine code:" + code)
    codeBuffer = codeBuffer + "\n" + code
  }

  protected def putUdfPath(path: String, requestEngine: RequestEngine) ={
    val properties = requestEngine.properties
    if(!properties.containsKey(udfPathProp) || StringUtils.isEmpty(properties.get(udfPathProp))){
      properties.put(udfPathProp, path)
    }else{
      properties.put(udfPathProp, path + "," + properties.get(udfPathProp))
    }
  }

  protected def extractUdfInfos(requestEngine: RequestEngine): mutable.ArrayBuffer[UDFInfo] = {
    val udfInfoBuilder = new mutable.ArrayBuffer[UDFInfo]
    val userName = requestEngine.user
    val udfTree = queryUdfRpc(userName)
    extractUdfInfos(udfInfoBuilder, udfTree, userName)
    udfInfoBuilder
  }

  protected def extractUdfInfos(udfInfoBuilder: mutable.ArrayBuffer[UDFInfo], udfTree: UDFTree, userName: String) : Unit = {
    if(CollectionUtils.isNotEmpty(udfTree.getUdfInfos)){
      for(udfInfo <- udfTree.getUdfInfos){
        udfInfoBuilder.append(udfInfo)
      }
    }
    if(CollectionUtils.isNotEmpty(udfTree.getChildrens)){
      for(child <- udfTree.getChildrens){
        var childInfo = child
        if(TreeType.specialTypes.contains(child.getUserName)){
          childInfo = queryUdfRpc(userName, child.getId, child.getUserName)
        } else {
          childInfo = queryUdfRpc(userName, child.getId, TreeType.SELF)
        }
        extractUdfInfos(udfInfoBuilder, childInfo, userName)
      }
    }
  }

  private def queryUdfRpc(userName: String, treeId: Long = -1, treeType: String = "self"): UDFTree = {
    val udfTree = Sender.getSender(ENGINE_UDF_APP_NAME.getValue)
      .ask(RequestUdfTree(userName, treeType, treeId, category))
      .asInstanceOf[ResponseUdfTree]
      .udfTree
    //val udfTree = UdfMapCache.udfMapCache.getCacheMap(RequestUdfTree(userName, treeType, treeId, category)).get("udfTree")
    //info("got udf tree:" + ow.writeValueAsString(udfTree))
    udfTree
  }

  protected def readFile(path: String): String = {
    info("read file: " + path)
    val file = new File(path)
    if(file.exists()){
      FileUtils.readFileToString(file)
    } else {
      info("udf file: [" + path + "] doesn't exist, ignore it.")
      ""
    }
  }

  protected def writeFile(code: String, user: String) : String = {
    val dirPath = EngineManagerConfiguration.ENGINE_UDF_BUFFER.getValue + user + "/"
    val dir = new File(dirPath)
    if(dir.exists()){
      //dir.listFiles().foreach(FileUtils.deleteQuietly)
    } else {
      dir.mkdirs()
    }
    val udfBufferFile = new File(dirPath + UUID.randomUUID().toString)
    udfBufferFile.createNewFile()
    FileUtils.write(udfBufferFile, code)
    udfBufferFile.getAbsolutePath
  }

  protected def fetchRemoteFile(path: String, user: String): Boolean = {
    if(path.contains(EngineManagerConfiguration.ENGINE_UDF_BUILT_IN_PATH.getValue)){
      val localLib = new File(path)
      if(localLib.exists()){
        return false
      }
    }
    val targetFile = new File(path)
    val fsPath = new FsPath("file://" + path)
    val remoteFs = FSFactory.getFsByProxyUser(fsPath, user)
    if(remoteFs.exists(fsPath)){
      val remoteStream = remoteFs.read(fsPath)
      Files.copy(remoteStream, targetFile.toPath, StandardCopyOption.REPLACE_EXISTING)
      IOUtils.closeQuietly(remoteStream)
      return true
    }
    false
  }

  override def afterCreatedSession(engine: Engine, requestEngine: RequestEngine): Unit = {
  }

}
@Deprecated
class JarUdfEngineHook extends UdfLoaderEngineHook{
  override val udfType: BigInt = UdfType.UDF_JAR
  override val category: String = UdfCategory.UDF

  override def beforeCreateSession(requestEngine: RequestEngine): RequestEngine = {
    info("start loading UDFs")
    val udfInfos = extractUdfInfos(requestEngine).filter{info => info.getUdfType == udfType && info.getExpire == false && StringUtils.isNotBlank(info.getPath) && isJarExists(info) && info.getLoad == true }
    codeBuffer = ""
    udfInfos.map(generateCode).map(putEngineCode(_, requestEngine))
    putUdfPath(writeFile(codeBuffer, requestEngine.user), requestEngine)
    // add to class path
    val jars = new mutable.HashSet[String]()
    udfInfos.foreach{udfInfo => jars.add("file://" + udfInfo.getPath)}
    val jarPaths = jars.mkString(",")
    if(StringUtils.isBlank(requestEngine.properties.get("jars"))){
      requestEngine.properties.put("jars", jarPaths)
    } else {
      requestEngine.properties.put("jars", requestEngine.properties.get("jars") + "," + jarPaths)
    }
    info("added jars: " + jarPaths)
    //jars.foreach(fetchRemoteFile)
    //info("copied jars.")
    info("end loading UDFs")
    requestEngine
  }

  protected def isJarExists(udfInfo: UDFInfo) : Boolean = {
    if(FileUtils.getFile(udfInfo.getPath).exists()){
      true
    } else {
      info(s"The jar file [${udfInfo.getPath}] of UDF [${udfInfo.getUdfName}] doesn't exist, ignore it.")
      false
    }
  }

  override protected def generateCode(udfInfo: UDFInfo): String = {
    "%sql\n" + udfInfo.getRegisterFormat
  }
}
@Deprecated
class PyUdfEngineHook extends UdfLoaderEngineHook{
  override val udfType: BigInt = UdfType.UDF_PY
  override val category: String = UdfCategory.UDF
  override protected def generateCode(udfInfo: UDFInfo): String = {
    "%python\n" + readFile(udfInfo.getPath) + "\n" + (if(StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }
}
@Deprecated
class ScalaUdfEngineHook extends UdfLoaderEngineHook{
  override val udfType: BigInt = UdfType.UDF_SCALA
  override val category: String = UdfCategory.UDF
  override protected def generateCode(udfInfo: UDFInfo): String = {
    "%scala\n" + readFile(udfInfo.getPath) + "\n" + (if(StringUtils.isNotBlank(udfInfo.getRegisterFormat)) udfInfo.getRegisterFormat else "")
  }
}
@Deprecated
class PyFunctionEngineHook extends UdfLoaderEngineHook{
  override val udfType: BigInt = UdfType.FUNCTION_PY
  override val category: String = UdfCategory.FUNCTION
  override protected def generateCode(udfInfo: UDFInfo): String = {
    "%python\n" + readFile(udfInfo.getPath)
  }
}
@Deprecated
class ScalaFunctionEngineHook extends UdfLoaderEngineHook{
  override val udfType: BigInt = UdfType.FUNCTION_SCALA
  override val category: String = UdfCategory.FUNCTION
  override protected def generateCode(udfInfo: UDFInfo): String = {
    "%scala\n" + readFile(udfInfo.getPath)
  }
}
object UdfType {
  val UDF_JAR = 0
  val UDF_PY = 1
  val UDF_SCALA = 2
  val FUNCTION_PY = 3
  val FUNCTION_SCALA = 4
}
object UdfCategory{
  val UDF = "udf"
  val FUNCTION = "function"
}
object TreeType {
  val SYS = "sys"
  val BDP = "bdp"
  val SELF = "self"
  val SHARE = "share"
  def specialTypes = Array(SYS, BDP, SHARE)
}





