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

package org.apache.linkis.variable.service

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.protocol.variable.ResponseQueryVariable
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.variable.dao.VarMapper
import org.apache.linkis.variable.entity.{VarKey, VarKeyUser, VarKeyValueVO}
import org.apache.linkis.variable.exception.VariableException

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util

@Service
class VariableServiceImpl extends VariableService with Logging {

  @Autowired
  private var varMapper: VarMapper = _

  override def queryGolbalVariable(userName: String): ResponseQueryVariable = {
    val globals = listGlobalVariable(userName)
    val response = new ResponseQueryVariable
    val map = new util.HashMap[String, String]()
    import scala.collection.JavaConverters._
    globals.asScala.foreach(f => map.put(f.getKey, f.getValue))
    response.setKeyAndValue(map)
    response
  }

  override def queryAppVariable(
      userName: String,
      creator: String,
      appName: String
  ): ResponseQueryVariable = {
    val globals = listGlobalVariable(userName)
    val response = new ResponseQueryVariable
    val map = new util.HashMap[String, String]()
    import scala.collection.JavaConverters._
    globals.asScala.foreach(f => map.put(f.getKey, f.getValue))
    response.setKeyAndValue(map)
    response
  }

  override def listGlobalVariable(userName: String): util.List[VarKeyValueVO] = {
    varMapper.listGlobalVariable(userName)
  }

  private def removeGlobalVariable(keyID: Long): Unit = {
    val value = varMapper.getValueByKeyID(keyID)
    varMapper.removeKey(keyID)
    varMapper.removeValue(value.getId)
  }

  private def insertGlobalVariable(saveVariable: VarKeyValueVO, userName: String): Unit = {
    val newKey = new VarKey
    newKey.setApplicationID(-1L)
    newKey.setKey(saveVariable.getKey)
    varMapper.insertKey(newKey)
    val newValue = new VarKeyUser
    newValue.setApplicationID(-1L)
    newValue.setKeyID(newKey.getId)
    newValue.setUserName(userName)
    newValue.setValue(saveVariable.getValue)
    varMapper.insertValue(newValue)
  }

  private def updateGlobalVariable(saveVariable: VarKeyValueVO, valueID: Long): Unit = {
    varMapper.updateValue(valueID, saveVariable.getValue)
  }

  @Transactional
  override def saveGlobalVaraibles(
      globalVariables: util.List[_],
      userVariables: util.List[VarKeyValueVO],
      userName: String
  ): Unit = {
    import scala.collection.JavaConverters._
    import scala.util.control.Breaks._
    val saves = globalVariables.asScala.map(f =>
      BDPJettyServerHelper.gson
        .fromJson(BDPJettyServerHelper.gson.toJson(f), classOf[VarKeyValueVO])
    )
    saves.foreach { f =>
      if (StringUtils.isBlank(f.getKey) || StringUtils.isBlank(f.getValue)) {
        throw new VariableException("Key or value cannot be empty")
      }
      var flag = true
      breakable {
        for (ele <- userVariables.asScala) {
          if (f.getKey.equals(ele.getKey)) {
            flag = false
            updateGlobalVariable(f, ele.getValueID)
            break()
          }
        }
      }
      if (flag) insertGlobalVariable(f, userName)
    }
    userVariables.asScala.foreach { f =>
      var flag = true
      breakable {
        for (ele <- saves) {
          if (ele.getKey.equals(f.getKey)) {
            flag = false
            break()
          }
        }
        if (flag) removeGlobalVariable(f.getKeyID)
      }
    }

  }

}
