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

package com.webank.wedatasphere.linkis.configuration.service

import java.lang.Long
import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.configuration.dao.ConfigMapper
import com.webank.wedatasphere.linkis.configuration.entity.{ConfigKey, _}
import com.webank.wedatasphere.linkis.configuration.exception.ConfigurationException
import com.webank.wedatasphere.linkis.configuration.util.Constants
import com.webank.wedatasphere.linkis.configuration.validate.ValidatorManager
import com.webank.wedatasphere.linkis.protocol.config.ResponseQueryConfig
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
  * Created by allenlliu on 2019/4/8.
  */
@Service
class ConfigurationService extends Logging {

  @Autowired private var configMapper: ConfigMapper = _

  @Autowired private var validatorManager: ValidatorManager = _

  @Transactional
  def addKey(creator:String,appName:String,treeName:String,key: ConfigKey): Unit ={
    val appID:Long = configMapper.selectAppIDByAppName(appName)
    val creatorID: Long = configMapper.selectAppIDByAppName(creator)
    val tree:ConfigTree = configMapper.selectTreeByAppIDAndName(appID,treeName)
    key.setId(null)
    key.setApplicationID(creatorID)
    configMapper.insertKey(key)
    configMapper.insertKeyTree(key.getId,tree.getId)
  }

  def insertCreator(creator:String): Unit ={
    val creatorID: Long = configMapper.selectAppIDByAppName(creator)
    if(creatorID == null) configMapper.insertCreator(creator) else warn(s"creator${creator} exists")
  }

  def listKeyByCreatorAndAppName(creator:String, appName:String):java.util.List[ConfigKey] ={
    val creatorID: Long = configMapper.selectAppIDByAppName(creator)
    val appID:Long = configMapper.selectAppIDByAppName(appName)
    configMapper.listKeyByCreatorAndAppName(creatorID,appID)
  }

  @Transactional
  def copyKeyFromIDE(key:ConfigKey,creator:String, appName:String) ={
    val creatorID: Long = configMapper.selectAppIDByAppName(creator)
    key.setApplicationID(creatorID)
    val treeID = configMapper.selectTreeIDByKeyID(key.getId)
    key.setId(null)
    configMapper.insertKey(key)
    configMapper.insertKeyTree(key.getId,treeID)
  }


  def updateUserValue(setting: ConfigKeyValueVO) = {
    if (!StringUtils.isEmpty(setting.getValue)) {
      if(!StringUtils.isEmpty(setting.getUnit) && !setting.getValue.contains(setting.getUnit) && !setting.getValue.contains(setting.getUnit.toLowerCase)){
        setting.setValue(setting.getValue + setting.getUnit)
      }
      val key = configMapper.selectKeyByKeyID(setting.getKeyID)
      info(s"Save parameter ${key.getKey} value ${setting.getValue} is not empty, enter checksum...(保存参数${key.getKey}值${setting.getValue}不为空，进入校验...)")
      if (!validatorManager.getOrCreateValidator(key.getValidateType).validate(setting.getValue, key.getValidateRange)) {
        throw new ConfigurationException(s"Parameter verification failed(参数校验失败):${key.getKey}--${key.getValidateType}--${key.getValidateRange}--${setting.getValue}")
      }
    }
    configMapper.updateUserValue(setting.getValue, setting.getValueID)
  }


  def getFullTree(appName: String, userName: String, creator: String): util.ArrayList[ConfigTree] = {
    val id = 0L
    val configTrees = configMapper.selectTreesByAppNameAndParentID(appName, id)
    val configTreesReturn = new util.ArrayList[ConfigTree]()
    import scala.collection.JavaConversions._
    configTrees.foreach(f =>
      configTreesReturn.add(recursiveFullTree(f.getId, appName, userName, creator))
    )
    configTreesReturn
  }

  def getVO(key: ConfigKey, value: ConfigKeyUser): ConfigKeyValueVO = {
    val vo = new ConfigKeyValue
    vo.setAdvanced(key.getAdvanced)
    vo.setApplicationID(value.getApplicationID)
    vo.setCreatorID(key.getApplicationID)
    vo.setDefaultValue(key.getDefaultValue)
    vo.setDescription(key.getDescription)
    vo.setHidden(key.getHidden)
    vo.setKey(key.getKey)
    vo.setKeyID(key.getId)
    vo.setLevel(key.getLevel)
    vo.setName(key.getName)
    vo.setUserName(value.getUserName)
    vo.setValidateRange(key.getValidateRange)
    vo.setValidateType(key.getValidateType)
    vo.setValue(value.getValue)
    vo.setValueID(value.getId)
    vo.setUnit(key.getUnit)
    vo
  }

  private def recursiveFullTree(id: Long, appName: String, userName: String, creator: String): ConfigTree = {
    import scala.collection.JavaConversions._
    val configTree = configMapper.selectTreeByTreeID(id)
    val keys = getKeysByTreeID(configTree.getId(), configMapper.selectAppIDByAppName(creator))
    if (!keys.isEmpty && keys.get(0) != null) {
      keys.foreach {
        f =>
          var value = getValueByKeyId(f.getId, userName, appName)
          if (value == null) {
            value = persisteUserValue(f.getId, userName, appName)
          }
          //configTree.getSettings.add(getVO(f, value))
          Utils.tryCatch(configTree.getSettings.add(getVO(f, value))){
            t => {
              info(t.getMessage)
              info("restful get data...(开始将非法的数据置空...)")
              val setting = new ConfigKeyValueVO
              setting.setValue(null)
              setting.setValueID(value.getId)
              updateUserValue(setting)
              info("Data is blanked out...(数据置空完成...)")
              value.setValue(null)
              configTree.getSettings.add(getVO(f, value))
            }

          }
      }
    }
    val parentTree = configMapper.selectTreesByAppNameAndParentID(appName, id)
    parentTree.foreach { f => configTree.getChildrens.add(recursiveFullTree(f.getId, appName, userName, creator)) }
    return configTree
  }

  private def persisteUserValue(keyID: Long, userName: String, appName: String): ConfigKeyUser = {
    var value = new ConfigKeyUser()
    value.setKeyID(keyID)
    value.setUserName(userName)
    value.setApplicationID(configMapper.selectAppIDByAppName(appName))
    try {
      configMapper.insertValue(value)
    } catch {
      case e: Exception => value = getValueByKeyId(value.getKeyID, userName, appName)
    }
    value
  }


  private def getKeysByTreeID(treeID: Long, creatorID: Long): util.List[ConfigKey] = {
    configMapper.selectKeysByTreeID(treeID, creatorID)
  }

  private def getValueByKeyId(keyID: Long, userName: String, appName: String): ConfigKeyUser = {
    val appID = configMapper.selectAppIDByAppName(appName)
    configMapper.selectValueByKeyId(keyID, userName, appID)
  }

  def queryAppConfigWithGlobal(userName: String, creator: String, appName: String, isMerge: Boolean): ResponseQueryConfig = {
    val config = new ResponseQueryConfig
    val globalMap = queryGolbalConfig(userName).getKeyAndValue
    val appMap = queryAppConfig(userName, null, appName).getKeyAndValue //Here is when isMerge=false(这里是为了isMerge=false的时候)
    val creatorMap = queryAppConfig(userName, creator, appName).getKeyAndValue
    if (!isMerge) {
      val notMerge = new util.HashMap[String, String]()
      val globalJson: String = BDPJettyServerHelper.gson.toJson(globalMap)
      val appJson: String = BDPJettyServerHelper.gson.toJson(appMap)
      val creatorJson: String = BDPJettyServerHelper.gson.toJson(creatorMap)
      notMerge.put("creator", creatorJson)
      notMerge.put("appName", appJson)
      notMerge.put("global", globalJson)
      config.setKeyAndValue(notMerge)
      config
    } else {
      globalMap.putAll(creatorMap)
      config.setKeyAndValue(globalMap)
      config
    }
  }

  def queryAppConfig(userName: String, creator: String, appName: String): ResponseQueryConfig = {
    val appID: Long = configMapper.selectAppIDByAppName(appName)
    var creatorID: Long = null
    if (StringUtils.isBlank(creator) || creator == appName) creatorID = appID
    else if (StringUtils.isNotBlank(appName)) creatorID = configMapper.selectAppIDByAppName(creator)
    getResponseConfig(userName, appID, creatorID)
  }

  def queryGolbalConfig(userName: String): ResponseQueryConfig = {
    val globalID = configMapper.selectAppIDByAppName(Constants.GOLBAL_CONFIG_NAME)
    getResponseConfig(userName, globalID, globalID)
  }

  private def getResponseConfig(userName: String, appID: Long, creatorID: Long): ResponseQueryConfig = {
    val all = configMapper.selectAllAppKVs(creatorID,appID)//Currently, the tree table is connected to the left, so no one in the tree will be calculated.(目前左连接了tree表，所以不在树上的都不会被计算出来)
    val user = configMapper.selectAppconfigByAppIDAndCreatorID(userName, creatorID, appID)
    val config = new ResponseQueryConfig
    config.setKeyAndValue(getMap(all, user))
    config
  }


  private def getMap(all: util.List[ConfigKeyValueVO], user: util.List[ConfigKeyValueVO]): util.Map[String, String] = {
    val map = new util.HashMap[String, String]()
    import scala.collection.JavaConversions._
    all.foreach(f => map.put(f.getKey, f.getValue))
    user.foreach(f => map.put(f.getKey, f.getValue))
    map
  }


  //Verify when data is sent to internal services(数据给内部服务的时候进行校验)
  implicit def ConfigKeyValuesToVOs(configKeyValus: java.util.List[ConfigKeyValue]): java.util.List[ConfigKeyValueVO] = {
    import scala.collection.JavaConversions._
    val vos = new util.ArrayList[ConfigKeyValueVO]()
    configKeyValus.foreach { f =>
      if (StringUtils.isEmpty(f.getValue)) {
        //All queries must enter, user's query, if the value is null, it will enter(all的查询必定进入，user的查询，如果值为null也会进入)
        info(s"The rpc-query value is null, and the ${f.getUserName} parameter ${f.getKey} is appended with the default value ${f.getDefaultValue}(rpc-查询值为空，给${f.getUserName}参数${f.getKey}附上默认值${f.getDefaultValue})")
        f.setValue(f.getDefaultValue)//Here you need to ensure that the default value is non-empty.(这里需要保证默认值是非空的)
      }
      Utils.tryCatch(vos.add(f)){
         t => {
           info(t.getMessage)
           info("Rpc starts to empty illegal data...(rpc开始将非法的数据置空...)")
           val setting = new ConfigKeyValueVO
           setting.setValue(null)
           setting.setValueID(f.getValueID)
           updateUserValue(setting)
           info("Data is blanked out...(数据置空完成...)")
           f.setValue(f.getDefaultValue)
           vos.add(f)
         }
      }
    }
    vos
  }

  //Check when data is returned to the front end(数据返回给前端的时候进行校验)
  implicit def ConfigKeyValueToVO(configKeyValue: ConfigKeyValue): ConfigKeyValueVO = {
    val vo = new ConfigKeyValueVO
    var validateValue = configKeyValue.getValue
    if (StringUtils.isEmpty(validateValue)) {
      //According to the reason, rpc will not enter this side unless the default value of the key is null.（）按道理rpc的不会进入这边，除非key的defaultvalue是null,进入这里也不影响）
      info(s"Restful-${configKeyValue.getUserName} Query the ${configKeyValue.getKey} value to null, and let the default value ${configKeyValue.getDefaultValue} be checked（restful-${configKeyValue.getUserName}查询${configKeyValue.getKey}值为空，让默认值${configKeyValue.getDefaultValue}进行校验）")
      validateValue = configKeyValue.getDefaultValue
    }
    if (!validatorManager.getOrCreateValidator(configKeyValue.getValidateType).validate(validateValue, configKeyValue.getValidateRange)) {
      throw new ConfigurationException(s"Parameter verification failed(参数校验失败):${configKeyValue.getKey}--${configKeyValue.getValidateType}--${configKeyValue.getValidateRange}--${validateValue}--${configKeyValue.getUserName}")
    }
    BeanUtils.copyProperties(configKeyValue, vo)
    vo
  }

}
