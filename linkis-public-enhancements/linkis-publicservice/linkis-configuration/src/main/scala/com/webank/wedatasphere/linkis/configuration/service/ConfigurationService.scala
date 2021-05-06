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

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.configuration.dao.{ConfigMapper, LabelMapper}
import com.webank.wedatasphere.linkis.configuration.entity.{ConfigKey, _}
import com.webank.wedatasphere.linkis.configuration.exception.ConfigurationException
import com.webank.wedatasphere.linkis.configuration.util.{LabelEntityParser, LabelParameterParser}
import com.webank.wedatasphere.linkis.configuration.validate.ValidatorManager
import com.webank.wedatasphere.linkis.governance.common.protocol.conf.ResponseQueryConfig
import com.webank.wedatasphere.linkis.manager.label.builder.CombinedLabelBuilder
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.label.entity.{CombinedLabel, CombinedLabelImpl, Label}
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

import scala.collection.JavaConverters._

@Service
class ConfigurationService extends Logging {

  @Autowired private var configMapper: ConfigMapper = _

  @Autowired private var labelMapper: LabelMapper = _

  @Autowired private var validatorManager: ValidatorManager = _

  private  val combinedLabelBuilder: CombinedLabelBuilder = new CombinedLabelBuilder

  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  @Transactional
  def addKeyForEngine(engineType: String, version: String, key: ConfigKey): Unit ={
    val labelList = LabelEntityParser.generateUserCreatorEngineTypeLabelList("*","*",engineType,version)
    val combinedLabel = combinedLabelBuilder.build("", labelList).asInstanceOf[CombinedLabel]
    var label = labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey, combinedLabel.getStringValue)
    var configs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if(label != null && label.getId > 0){
      configs = configMapper.getConfigKeyValueByLabelId(label.getId)
    }else{
      val parsedLabel = LabelEntityParser.parseToConfigLabel(combinedLabel)
      labelMapper.insertLabel(parsedLabel)
      info(s"创建label成功：${parsedLabel.getStringValue}")
      label = parsedLabel
    }
    val existsKey = configs.asScala.map(_.getKey).contains(key.getKey)
    if(!existsKey){
      configMapper.insertKey(key)
      info(s"创建Key成功：${key.getKey}")
    }else{
      configs.asScala.foreach(conf => if(conf.getKey.equals(key.getKey)) key.setId(conf.getId))
    }
    val existsConfigValue = configMapper.getConfigKeyValueByLabelId(label.getId)
    if(existsConfigValue == null){
      val configValue = new ConfigValue()
      configValue.setConfigKeyId(key.getId)
      configValue.setConfigValue("")
      configValue.setConfigLabelId(label.getId)
      configMapper.insertValue(configValue)
      info(s"key、label关联成功：key:${key.getKey},label:${label.getStringValue}")
    }
  }

  def insertCreator(creator:String): Unit ={
    val creatorID: Long = configMapper.selectAppIDByAppName(creator)
    if(creatorID == null) configMapper.insertCreator(creator) else warn(s"creator${creator} exists")
  }

  def listKeyByEngineType(engineType: String):java.util.List[ConfigKey] ={
    configMapper.listKeyByEngineType(engineType)
  }

  @Transactional
  def copyKeyFromIDE(key:ConfigKey,creator:String, appName:String) ={
    /*val creatorID: Long = configMapper.selectAppIDByAppName(creator)
    key.setApplicationID(creatorID)
    val treeID = configMapper.selectTreeIDByKeyID(key.getId)
    key.setId(null)
    configMapper.insertKey(key)
    configMapper.insertKeyTree(key.getId,treeID)*/
  }


  def updateUserValue(setting: ConfigKeyValue) = {
    if (!StringUtils.isEmpty(setting.getConfigValue)) {
      val key = configMapper.selectKeyByKeyID(setting.getId)
      info(s"Save parameter ${key.getKey} value ${setting.getConfigValue} is not empty, enter checksum...(保存参数${key.getKey}值${setting.getConfigValue}不为空，进入校验...)")
      if (!validatorManager.getOrCreateValidator(key.getValidateType).validate(setting.getConfigValue, key.getValidateRange)) {
        throw new ConfigurationException(s"Parameter verification failed(参数校验失败):${key.getKey}--${key.getValidateType}--${key.getValidateRange}--${setting.getConfigValue}")
      }
    }
    configMapper.updateUserValue(setting.getConfigValue, setting.getValueId)
  }


  def generateCombinedLabel(engineType: String = "*", version: String, userName: String = "*", creator: String = "*"): CombinedLabel = {
    val labelList = LabelEntityParser.generateUserCreatorEngineTypeLabelList(userName, creator, engineType, version)
    val combinedLabel = combinedLabelBuilder.build("",labelList)
    combinedLabel.asInstanceOf[CombinedLabelImpl]
  }

  def buildTreeResult(configs: util.List[ConfigKeyValue],defaultConfigs:util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()): util.ArrayList[ConfigTree] = {
    var resultConfigs: util.List[ConfigKeyValue] = null
    if(!defaultConfigs.isEmpty){
      defaultConfigs.asScala.foreach(defaultConfig => {
        configs.asScala.foreach(config => {
          if(config.getKey != null && config.getKey.equals(defaultConfig.getKey)){
            if(config.getConfigValue != null){
              defaultConfig.setConfigValue(config.getConfigValue)
              defaultConfig.setConfigLabelId(config.getConfigLabelId)
              defaultConfig.setValueId(config.getValueId)
            }
          }
        })
      })
      resultConfigs = defaultConfigs
    }else {
      resultConfigs = configs
    }
    val treeNames = resultConfigs.asScala.map(config => config.getTreeName).distinct
    val resultConfigsTree = new util.ArrayList[ConfigTree](treeNames.length)
    resultConfigsTree.addAll(treeNames.map(treeName => {
      val configTree = new ConfigTree()
      configTree.setName(treeName)
      configTree.getSettings.addAll(resultConfigs.asScala.filter(config => config.getTreeName.equals(treeName)).toList.asJava)
      configTree
    }).toList.asJava)
    resultConfigsTree
  }

  def getFullTree(engineType: String = "*", version: String, userName: String = "*", creator: String = "*", useDefaultConfig: Boolean = true): util.ArrayList[ConfigTree] = {
    val combinedLabel = generateCombinedLabel(engineType, version,userName,creator)
    info(s"start to get config by label：${combinedLabel.getStringValue}（开始通过标签获取配置信息：${combinedLabel.getStringValue}）")
    val label = labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey,combinedLabel.getStringValue)
    var configs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if(label != null && label.getId > 0){
      configs = configMapper.getConfigKeyValueByLabelId(label.getId)
    }
    var defaultConfigs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if(useDefaultConfig) {
      var defaultCombinedLabel: CombinedLabel = null
      defaultCombinedLabel = generateCombinedLabel(engineType, version)
      val defaultLabel = labelMapper.getLabelByKeyValue(defaultCombinedLabel.getLabelKey, defaultCombinedLabel.getStringValue)
      if(defaultLabel != null){
        defaultConfigs = configMapper.getConfigKeyValueByLabelId(defaultLabel.getId)
      }
      if(CollectionUtils.isEmpty(defaultConfigs)){
        throw new ConfigurationException(s"The default configuration is empty. Please check the default configuration information in the database table(默认配置为空,请检查数据库表中关于标签${defaultCombinedLabel.getStringValue}的默认配置信息是否完整)")
      }
    }
    persisteUservalue(configs,defaultConfigs,combinedLabel,label)
    info("finished to get config by label, start to build config tree(获取配置信息结束,开始构造配置树)")
    buildTreeResult(configs,defaultConfigs)
  }

  def labelCheck(labelList: java.util.List[Label[_]]):Boolean = {
    if(!CollectionUtils.isEmpty(labelList)){
      labelList.asScala.foreach(label => label match {
        case a:UserCreatorLabel => Unit
        case a:EngineTypeLabel => Unit
        case _ => throw new ConfigurationException(s"this type of label is not supported:${label.getClass}(目前暂不支持该类型的label${label.getClass})")
      })
      true
    }else{
      throw new ConfigurationException("The label parameter is empty(label参数为空，无法根据label查询相关配置)")
    }
  }
  def getFullTreeByLabelList(labelList: java.util.List[Label[_]], useDefaultConfig: Boolean = true): util.ArrayList[ConfigTree] = {
    labelCheck(labelList)
    val combinedLabel = combinedLabelBuilder.build("",labelList).asInstanceOf[CombinedLabelImpl]
    info("start to get config by label（开始通过标签获取配置信息）")
    val label = labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey,combinedLabel.getStringValue)
    var configs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if(label != null && label.getId > 0){
      configs = configMapper.getConfigKeyValueByLabelId(label.getId)
    }
    var defaultConfigs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if(useDefaultConfig) {
      //todo 优先级：用户配置-->creator的默认引擎配置-->默认引擎配置,现在缺少creator级别的覆盖
      val defaultLabelList = LabelParameterParser.changeUserToDefault(labelList)
      val defaultCombinedLabel = combinedLabelBuilder.build("",defaultLabelList).asInstanceOf[CombinedLabelImpl]
      val defaultLabel = labelMapper.getLabelByKeyValue(defaultCombinedLabel.getLabelKey, defaultCombinedLabel.getStringValue)
      if(defaultLabel != null){
        defaultConfigs = configMapper.getConfigKeyValueByLabelId(defaultLabel.getId)
      }
      if(CollectionUtils.isEmpty(defaultConfigs)){
        warn(s"The default configuration is empty. Please check the default configuration information in the database table(默认配置为空,请检查数据库表中关于标签${defaultCombinedLabel.getStringValue}的默认配置信息是否完整)")
      }
    }
    persisteUservalue(configs,defaultConfigs,combinedLabel,label)
    info("finished to get config by label, start to build config tree(获取配置信息成功,开始返回配置树)")
    buildTreeResult(configs,defaultConfigs)
  }


  @Transactional
   def persisteUservalue(configs: util.List[ConfigKeyValue], defaultConfigs:util.List[ConfigKeyValue], combinedLabel: CombinedLabel, existsLabel: ConfigLabel): Unit = {
    info(s"Start checking the integrity of user configuration data(开始检查用户配置数据的完整性): label标签为：${combinedLabel.getStringValue}")
    val userConfigList = configs.asScala
    val userConfigKeyIdList = userConfigList.map(config => config.getId)
    val defaultConfigsList = defaultConfigs.asScala
    val parsedLabel = LabelEntityParser.parseToConfigLabel(combinedLabel)
    if(existsLabel == null){
      info("start to create label for user(开始为用户创建label)：" + "labelKey:" + parsedLabel.getLabelKey + " , " + "labelValue:" + parsedLabel.getStringValue)
      labelMapper.insertLabel(parsedLabel)
      info("Creation completed(创建完成！)：" + parsedLabel)
    }
    defaultConfigsList.foreach(defaultConfig => {
      if(!userConfigKeyIdList.contains(defaultConfig.getId)){
        info(s"Initialize database configuration information for users(为用户初始化数据库配置信息)："+s"configKey: ${defaultConfig.getKey}")
        val configValue = new ConfigValue
        configValue.setConfigKeyId(defaultConfig.getId)
        if(existsLabel == null){
          configValue.setConfigLabelId(parsedLabel.getId)
        }else{
          configValue.setConfigLabelId(existsLabel.getId)
        }
        configValue.setConfigValue("")
        configMapper.insertValue(configValue)
        info(s"Initialization of user database configuration information completed(初始化用户数据库配置信息完成)：configKey: ${defaultConfig.getKey}")
      }
    })
    info(s"User configuration data integrity check completed!(用户配置数据完整性检查完毕！): label标签为：${combinedLabel.getStringValue}")
  }

    def queryConfigByLabel(labelList: java.util.List[Label[_]], isMerge:Boolean = true, filter:String = null): ResponseQueryConfig = {
      labelCheck(labelList)
      val allGolbalUserConfig = getFullTreeByLabelList(labelList)
      val defaultLabel = LabelParameterParser.changeUserToDefault(labelList)
      val allGolbalDefaultConfig = getFullTreeByLabelList(defaultLabel)
      val config = new ResponseQueryConfig
      config.setKeyAndValue(getMap(allGolbalDefaultConfig,allGolbalUserConfig,filter))
      config
  }


  def queryDefaultEngineConfig(engineTypeLabel: EngineTypeLabel): ResponseQueryConfig = {
    val labelList = LabelEntityParser.generateUserCreatorEngineTypeLabelList("*","*",engineTypeLabel.getEngineType,engineTypeLabel.getVersion)
    queryConfigByLabel(labelList)
  }

  def queryGlobalConfig(userName: String): ResponseQueryConfig = {
    val labelList = LabelEntityParser.generateUserCreatorEngineTypeLabelList(userName,"*","*","*")
    queryConfigByLabel(labelList)
  }

  def queryConfig(userCreatorLabel: UserCreatorLabel, engineTypeLabel: EngineTypeLabel, filter: String): ResponseQueryConfig ={
    val labelList = new util.ArrayList[Label[_]]
    labelList.add(userCreatorLabel)
    labelList.add(engineTypeLabel)
    queryConfigByLabel(labelList, true,filter)
  }


  private def getMap(all: util.List[ConfigTree], user: util.List[ConfigTree], filter: String = null): util.Map[String, String] = {
    val map = new util.HashMap[String, String]()
    val allConfig = all.asScala.map(configTree => configTree.getSettings)
    val userConfig = user.asScala.map(configTree => configTree.getSettings)
    if(filter != null){
      allConfig.foreach(f => f.asScala.foreach(configKeyValue => if(configKeyValue.getKey.contains(filter)) map.put(configKeyValue.getKey,configKeyValue.getDefaultValue)))
      userConfig.foreach(f => f.asScala.foreach(configKeyValue => if(configKeyValue.getKey.contains(filter) && StringUtils.isNotEmpty(configKeyValue.getConfigValue)) map.put(configKeyValue.getKey,configKeyValue.getConfigValue)))
    }else{
      allConfig.foreach(f => f.asScala.foreach(configKeyValue => map.put(configKeyValue.getKey,configKeyValue.getDefaultValue)))
      userConfig.foreach(f => f.asScala.foreach(configKeyValue => if(StringUtils.isNotEmpty(configKeyValue.getConfigValue))map.put(configKeyValue.getKey,configKeyValue.getConfigValue)))
    }
    /*此处的用户配置会覆盖默认的配置*/
    map
  }

  def setCategoryVo(vo: CategoryLabelVo, categoryLabel: CategoryLabel) = {
    vo.setCategoryId(categoryLabel.getCategoryId)
    vo.setCreateTime(categoryLabel.getCreateTime)
    vo.setUpdateTime(categoryLabel.getUpdateTime)
    vo.setLevel(categoryLabel.getLevel)
    if(StringUtils.isNotEmpty(categoryLabel.getDescription)){
      vo.setDescription(categoryLabel.getDescription)
    }
    if(StringUtils.isNotEmpty(categoryLabel.getTag)){
      vo.setTag(categoryLabel.getTag)
    }

  }

  def getCategory(): util.List[CategoryLabelVo] = {
    val categoryLabelList = configMapper.getCategory()
    val firstCategoryList = new util.ArrayList[CategoryLabelVo]()
    val secondaryCategoryList = new util.ArrayList[CategoryLabelVo]()
    categoryLabelList.asScala.foreach(categoryLabel => {
      val vo = new CategoryLabelVo
      setCategoryVo(vo, categoryLabel)
      val labelList = LabelEntityParser.LabelDecompile(categoryLabel.getLabelKey, categoryLabel.getStringValue)
      labelList.asScala.foreach(label => label match{
        case u: UserCreatorLabel => if(categoryLabel.getLevel == 1) {
          vo.setCategoryName(u.getCreator)
          firstCategoryList.add(vo)
        }else if(categoryLabel.getLevel == 2){
          vo.setFatherCategoryName(u.getCreator)
        }
        case e: EngineTypeLabel => if(categoryLabel.getLevel == 2) {
          vo.setCategoryName(e.getStringValue)
          secondaryCategoryList.add(vo)
        }
        case _ =>
      })
    })
    secondaryCategoryList.asScala.foreach(secondaryVo => {
      //TODO use getOrElse
      firstCategoryList.asScala.find(_.getCategoryName.equals(secondaryVo.getFatherCategoryName)).get.getChildCategory.add(secondaryVo)
    })
    firstCategoryList
  }


}
