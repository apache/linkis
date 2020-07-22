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

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.enginemanager.{Engine, EngineHook}
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration.ENGINE_UDF_APP_NAME
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.udf.api.rpc.{RequestUdfTree, ResponseUdfTree}
import com.webank.wedatasphere.linkis.udf.entity.{UDFInfo, UDFTree}
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.jackson.map.ObjectMapper

import scala.collection.JavaConversions._
import scala.collection.mutable

class JarLoaderEngineHook extends EngineHook with Logging{

  override def beforeCreateSession(requestEngine: RequestEngine): RequestEngine = {
    info("start loading UDFs")
    val udfInfos = extractUdfInfos(requestEngine).filter{info => info.getUdfType == 0 && info.getExpire == false && StringUtils.isNotBlank(info.getPath) && isJarExists(info) && info.getLoad == true }
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

  override def afterCreatedSession(engine: Engine, requestEngine: RequestEngine): Unit = {
  }

  protected def isJarExists(udfInfo: UDFInfo) : Boolean = {
    true
//    if(FileUtils.getFile(udfInfo.getPath).exists()){
//      true
//    } else {
//      info(s"The jar file [${udfInfo.getPath}] of UDF [${udfInfo.getUdfName}] doesn't exist, ignore it.")
//      false
//    }
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
      .ask(RequestUdfTree(userName, treeType, treeId, "udf"))
      .asInstanceOf[ResponseUdfTree]
      .udfTree
    //info("got udf tree:" + new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(udfTree))
    udfTree
  }
}
