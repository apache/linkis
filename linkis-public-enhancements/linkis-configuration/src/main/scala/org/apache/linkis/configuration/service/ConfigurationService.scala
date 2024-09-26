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

package org.apache.linkis.configuration.service

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.configuration.conf.Configuration
import org.apache.linkis.configuration.dao.{ConfigKeyLimitForUserMapper, ConfigMapper, LabelMapper}
import org.apache.linkis.configuration.entity._
import org.apache.linkis.configuration.exception.ConfigurationException
import org.apache.linkis.configuration.util.{LabelEntityParser, LabelParameterParser}
import org.apache.linkis.configuration.validate.ValidatorManager
import org.apache.linkis.governance.common.protocol.conf.{
  RemoveCacheConfRequest,
  ResponseQueryConfig
}
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.{CombinedLabel, CombinedLabelImpl, Label}
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.{EngineTypeLabelCreator, LabelUtils}
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

import java.text.MessageFormat
import java.util

import scala.collection.JavaConverters._

import com.google.common.collect.Lists

@Service
class ConfigurationService extends Logging {

  @Autowired private var configMapper: ConfigMapper = _

  @Autowired private var labelMapper: LabelMapper = _

  @Autowired private var validatorManager: ValidatorManager = _

  @Autowired private var configKeyLimitForUserMapper: ConfigKeyLimitForUserMapper = _

  private val combinedLabelBuilder: CombinedLabelBuilder = new CombinedLabelBuilder

  @Transactional
  def addKeyForEngine(engineType: String, version: String, key: ConfigKey): Unit = {
    val labelList =
      LabelEntityParser.generateUserCreatorEngineTypeLabelList("*", "*", engineType, version)
    val combinedLabel = combinedLabelBuilder.build("", labelList).asInstanceOf[CombinedLabel]
    var label =
      labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey, combinedLabel.getStringValue)
    var configs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if (label != null && label.getId > 0) {
      configs = configMapper.getConfigKeyValueByLabelId(label.getId)
    } else {
      val parsedLabel = LabelEntityParser.parseToConfigLabel(combinedLabel)
      labelMapper.insertLabel(parsedLabel)
      logger.info(s"succeed to create lable:${parsedLabel.getStringValue}")
      label = parsedLabel
    }
    val existsKey = configs.asScala.map(_.getKey).contains(key.getKey)
    if (!existsKey) {
      configMapper.insertKey(key)
      logger.info(s"succeed to create key: ${key.getKey}")
    } else {
      configs.asScala.foreach(conf => if (conf.getKey.equals(key.getKey)) key.setId(conf.getId))
    }
    val existsConfigValue = configMapper.getConfigKeyValueByLabelId(label.getId)
    if (existsConfigValue == null) {
      val configValue = new ConfigValue()
      configValue.setConfigKeyId(key.getId)
      configValue.setConfigValue("")
      configValue.setConfigLabelId(label.getId)
      configMapper.insertValue(configValue)
      logger.info(s"Succeed to  create relation: key:${key.getKey},label:${label.getStringValue}")
    }
  }

  def checkAndCreateUserLabel(
      settings: util.List[ConfigKeyValue],
      username: String,
      creator: String
  ): Integer = {
    var labelId: Integer = null
    if (!settings.isEmpty) {
      val setting = settings.get(0)
      val configLabel = labelMapper.getLabelById(setting.getConfigLabelId)
      val combinedLabel = combinedLabelBuilder
        .buildFromStringValue(configLabel.getLabelKey, configLabel.getStringValue)
        .asInstanceOf[CombinedLabel]
      combinedLabel.getValue.asScala.foreach {
        case userCreator: UserCreatorLabel =>
          if (userCreator.getUser.equals(LabelUtils.COMMON_VALUE)) {
            userCreator.setUser(username)
            userCreator.setCreator(creator)
            val parsedLabel = LabelEntityParser.parseToConfigLabel(combinedLabel)
            val userLabel =
              labelMapper.getLabelByKeyValue(parsedLabel.getLabelKey, parsedLabel.getStringValue)
            if (userLabel == null) {
              labelMapper.insertLabel(parsedLabel)
              labelId = parsedLabel.getId
            } else {
              labelId = userLabel.getId
            }
          } else {
            labelId = configLabel.getId
          }
        case _ =>
      }
    }
    if (labelId == null) {
      throw new ConfigurationException(
        "create user label false, cannot save user configuration!(创建用户label信息失败，无法保存用户配置)"
      )
    }
    labelId
  }

  def updateUserValue(
      createList: util.List[ConfigValue],
      updateList: util.List[ConfigValue]
  ): Unit = {
    if (!CollectionUtils.isEmpty(createList)) {
      configMapper.insertValueList(createList)
    }
    if (!CollectionUtils.isEmpty(updateList)) {
      configMapper.updateUserValueList(updateList)
    }
  }

  def clearAMCacheConf(username: String, creator: String, engine: String, version: String): Unit = {
    val sender = Sender.getSender(Configuration.MANAGER_SPRING_NAME.getValue)
    if (StringUtils.isNotBlank(username)) {
      val userCreatorLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[UserCreatorLabel])
      userCreatorLabel.setUser(username)
      userCreatorLabel.setCreator(creator)
      val engineTypeLabel: EngineTypeLabel =
        if (StringUtils.isNotBlank(engine) && StringUtils.isNotBlank(version)) {
          val label = EngineTypeLabelCreator.createEngineTypeLabel(engine)
          label.setVersion(version)
          label
        } else {
          null
        }
      val request = RemoveCacheConfRequest(userCreatorLabel, engineTypeLabel)
      logger.info(s"Broadcast cleanup message to manager $request")
      sender.ask(request)
    }
  }

  def updateUserValue(
      setting: ConfigKeyValue,
      userLabelId: Integer,
      createList: util.List[ConfigValue],
      updateList: util.List[ConfigValue]
  ): Any = {

    val configLabel = labelMapper.getLabelById(setting.getConfigLabelId)
    val combinedLabel = combinedLabelBuilder
      .buildFromStringValue(configLabel.getLabelKey, configLabel.getStringValue)
      .asInstanceOf[CombinedLabel]
    val templateConfigKeyVo =
      configKeyLimitForUserMapper.selectByLabelAndKeyId(combinedLabel.getStringValue, setting.getId)
    if (templateConfigKeyVo != null && StringUtils.isNotBlank(templateConfigKeyVo.getMaxValue)) {
      Utils.tryCatch {
        val maxValue = Integer.valueOf(templateConfigKeyVo.getMaxValue.replaceAll("[^0-9]", ""))
        val configValue = Integer.valueOf(setting.getConfigValue.replaceAll("[^0-9]", ""))
        if (configValue > maxValue) {
          throw new ConfigurationException(
            s"Parameter key:${setting.getKey},config value:${setting.getConfigValue} verification failed，exceeds the specified max value:${templateConfigKeyVo.getMaxValue}:(参数校验失败，超过指定的最大值):" +
              s"${setting.getValidateType}--${setting.getValidateRange}"
          )
        }
      } { case exception: Exception =>
        if (exception.isInstanceOf[ConfigurationException]) {
          throw exception
        } else {
          logger.warn(
            s"Failed to check special limit setting for key:${setting.getKey},config value:${setting.getConfigValue}"
          )
        }
      }
    }
    paramCheck(setting)
    if (setting.getIsUserDefined) {
      val configValue = new ConfigValue
      if (StringUtils.isEmpty(setting.getConfigValue)) {
        configValue.setConfigValue("")
      } else {
        configValue.setConfigValue(setting.getConfigValue)
      }
      configValue.setId(setting.getValueId)
      updateList.add(configValue)
    } else {
      if (!StringUtils.isEmpty(setting.getConfigValue)) {
        val configValue = new ConfigValue
        configValue.setConfigKeyId(setting.getId)
        configValue.setConfigLabelId(userLabelId)
        configValue.setConfigValue(setting.getConfigValue)
        createList.add(configValue)
      }
    }
  }

  def paramCheck(setting: ConfigKeyValue): Unit = {
    if (!StringUtils.isEmpty(setting.getConfigValue)) {
      var key: ConfigKey = null
      if (setting.getId != null) {
        key = configMapper.selectKeyByKeyID(setting.getId)
      } else {
        val keys = configMapper.selectKeyByKeyName(setting.getKey)
        if (null != keys && !keys.isEmpty) {
          key = keys.get(0)
        }
      }
      if (key == null) {
        throw new ConfigurationException(
          "config key is null, please check again!(配置信息为空，请重新检查key值)"
        )
      }
      logger.info(
        s"parameter ${key.getKey} value ${setting.getConfigValue} is not empty, enter checksum...(参数${key.getKey} 值${setting.getConfigValue}不为空，进入校验...)"
      )
      if (
          !validatorManager
            .getOrCreateValidator(key.getValidateType)
            .validate(setting.getConfigValue, key.getValidateRange)
      ) {
        throw new ConfigurationException(
          s"Parameter verification failed(参数校验失败):${key.getKey}--${key.getValidateType}--${key.getValidateRange}--${setting.getConfigValue}"
        )
      }
    }
  }

  def paramCheckByKeyValue(key: String, value: String): Unit = {
    val setting = new ConfigKeyValue
    setting.setKey(key)
    setting.setConfigValue(value)
    paramCheck(setting)
  }

  def listAllEngineType(): Array[String] = {
    val engineTypeString = Configuration.ENGINE_TYPE.getValue
    val engineTypeList = engineTypeString.split(",")
    engineTypeList
  }

  def generateCombinedLabel(
      engineType: String = "*",
      version: String,
      userName: String = "*",
      creator: String = "*"
  ): CombinedLabel = {
    val labelList = LabelEntityParser.generateUserCreatorEngineTypeLabelList(
      userName,
      creator,
      engineType,
      version
    )
    val combinedLabel = combinedLabelBuilder.build("", labelList)
    combinedLabel.asInstanceOf[CombinedLabelImpl]
  }

  /**
   * Priority: configs > defaultConfigs
   * @param configs
   * @param defaultConfigs
   * @return
   */
  def buildTreeResult(
      configs: util.List[ConfigKeyValue],
      defaultConfigs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
  ): util.ArrayList[ConfigTree] = {
    var resultConfigs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if (!defaultConfigs.isEmpty) {
      defaultConfigs.asScala.foreach(defaultConfig => {
        defaultConfig.setIsUserDefined(false)
        configs.asScala.foreach(config => {
          if (config.getKey != null && config.getKey.equals(defaultConfig.getKey)) {
            // configValue also needs to be replaced when the value is empty
            defaultConfig.setConfigValue(config.getConfigValue)
            defaultConfig.setConfigLabelId(config.getConfigLabelId)
            defaultConfig.setValueId(config.getValueId)
            defaultConfig.setIsUserDefined(true)
          }
        })
      })
      resultConfigs = defaultConfigs
    }
    val treeNames = resultConfigs.asScala.map(config => config.getTreeName).distinct
    val resultConfigsTree = new util.ArrayList[ConfigTree](treeNames.length)
    resultConfigsTree.addAll(
      treeNames
        .map(treeName => {
          val configTree = new ConfigTree()
          configTree.setName(treeName)
          configTree.getSettings.addAll(
            resultConfigs.asScala
              .filter(config => config.getTreeName.equals(treeName))
              .toList
              .asJava
          )
          configTree
        })
        .toList
        .asJava
    )
    resultConfigsTree
  }

  def replaceCreatorToEngine(
      defaultCreatorConfigs: util.List[ConfigKeyValue],
      defaultEngineConfigs: util.List[ConfigKeyValue]
  ): Unit = {
    defaultCreatorConfigs.asScala.foreach(creatorConfig => {
      if (creatorConfig.getKey != null) {
        val engineconfig =
          defaultEngineConfigs.asScala.find(_.getKey.equals(creatorConfig.getKey))
        if (engineconfig.isDefined) {
          engineconfig.get.setDefaultValue(creatorConfig.getDefaultValue)
          engineconfig.get.setConfigValue(creatorConfig.getConfigValue)
        } else {
          defaultEngineConfigs.add(creatorConfig)
        }
      }
    })
  }

  def getConfigByLabelId(
      labelId: Integer,
      language: String = "zh-CN"
  ): util.List[ConfigKeyValue] = {
    var configs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if ("en".equals(language)) {
      configs = configMapper.getConfigEnKeyValueByLabelId(labelId)
    } else {
      configs = configMapper.getConfigKeyValueByLabelId(labelId)
    }
    configs
  }

  def getConfigsByLabelList(
      labelList: java.util.List[Label[_]],
      useDefaultConfig: Boolean = true,
      language: String
  ): (util.List[ConfigKeyValue], util.List[ConfigKeyValue]) = {
    LabelParameterParser.labelCheck(labelList)
    val combinedLabel = combinedLabelBuilder.build("", labelList).asInstanceOf[CombinedLabelImpl]
    val label =
      labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey, combinedLabel.getStringValue)
    var configs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if (label != null && label.getId > 0) {
      configs = getConfigByLabelId(label.getId, language)
    }
    var defaultEngineConfigs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    var defaultCreatorConfigs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    var defaultUserConfigs: util.List[ConfigKeyValue] = new util.ArrayList[ConfigKeyValue]()
    if (useDefaultConfig) {
      // query *-ide  default conf
      val defaultCreatorLabel = getConfByLabelList(
        LabelParameterParser.changeUserToDefault(labelList, false)
      )
      if (defaultCreatorLabel != null) {
        defaultCreatorConfigs = getConfigByLabelId(defaultCreatorLabel.getId, language)
      }
      // query user-*  default conf
      val defaultUserLabel = getConfByLabelList(
        LabelParameterParser.changeUserToDefault(labelList, true, false)
      )
      if (defaultUserLabel != null) {
        defaultUserConfigs = getConfigByLabelId(defaultUserLabel.getId, language)
      }
      // query *-*  default conf
      val defaultEngineLabel = getConfByLabelList(
        LabelParameterParser.changeUserToDefault(labelList)
      )
      if (defaultEngineLabel != null) {
        defaultEngineConfigs = getConfigByLabelId(defaultEngineLabel.getId, language)
      }
      if (CollectionUtils.isEmpty(defaultEngineConfigs)) {
        logger.warn(
          s"The default configuration is empty. Please check the default configuration information in the database table(默认配置为空,请检查数据库表中关于标签 *-* 的默认配置信息是否完整)"
        )
      }
      val userCreatorLabel = labelList.asScala
        .find(_.isInstanceOf[UserCreatorLabel])
        .get
        .asInstanceOf[UserCreatorLabel]
      if (Configuration.USE_CREATOR_DEFAULE_VALUE && userCreatorLabel.getCreator != "*") {
        replaceCreatorToEngine(defaultCreatorConfigs, defaultEngineConfigs)
      }
      if (Configuration.USE_USER_DEFAULE_VALUE && userCreatorLabel.getUser != "*") {
        replaceCreatorToEngine(defaultUserConfigs, defaultEngineConfigs)
      }
    }

    // add special config limit info
    if (defaultEngineConfigs.size() > 0) {
      val keyIdList = defaultEngineConfigs.asScala.toStream
        .map(e => {
          e.getId
        })
        .toList
        .asJava
      val limitList =
        configKeyLimitForUserMapper.selectByLabelAndKeyIds(combinedLabel.getStringValue, keyIdList)
      defaultEngineConfigs.asScala.foreach(entity => {
        val keyId = entity.getId
        val res = limitList.asScala.filter(v => v.getKeyId == keyId).toList.asJava
        if (res.size() > 0) {
          val specialMap = new util.HashMap[String, String]()
          val maxValue = res.get(0).getMaxValue
          if (StringUtils.isNotBlank(maxValue)) {
            specialMap.put("maxValue", maxValue)
            entity.setSpecialLimit(specialMap)
          }
        }
      })
    } else {
      logger.warn(
        s"The configuration is empty. Please check the configuration information in the database table(配置为空,请检查数据库表中关于标签${combinedLabel.getStringValue}的配置信息是否完整)"
      )
    }

    (configs, defaultEngineConfigs)
  }

  private def getConfByLabelList(labelList: java.util.List[Label[_]]): ConfigLabel = {
    val combinedLabel = combinedLabelBuilder.build("", labelList).asInstanceOf[CombinedLabelImpl]
    labelMapper.getLabelByKeyValue(combinedLabel.getLabelKey, combinedLabel.getStringValue)
  }

  /**
   * Priority: User Configuration-->Creator's Default Engine Configuration-->Default Engine
   * Configuration For database initialization: you need to modify the linkis.dml file--associated
   * label and default configuration, modify the engine such as label.label_value = @SPARK_ALL to
   * @SPARK_IDE
   *   and so on for each application
   * @param labelList
   * @param useDefaultConfig
   * @return
   */
  def getFullTreeByLabelList(
      labelList: java.util.List[Label[_]],
      useDefaultConfig: Boolean = true,
      language: String
  ): util.ArrayList[ConfigTree] = {
    val (configs, defaultEngineConfigs) =
      getConfigsByLabelList(labelList, useDefaultConfig, language)
    buildTreeResult(configs, defaultEngineConfigs)
  }

  def getConfigurationTemplateByLabelList(
      labelList: java.util.List[Label[_]],
      useDefaultConfig: Boolean = true
  ): util.ArrayList[ConfigTree] = {
    var (configs, defaultEngineConfigs) = getConfigsByLabelList(labelList, useDefaultConfig, null)
    configs = Lists.newArrayList()
    buildTreeResult(configs, defaultEngineConfigs)
  }

  /**
   * Priority: labelList > defaultConfig
   * @param labelList
   * @param useDefaultConfig
   * @return
   */
  def getConfigMapByLabels(
      labelList: java.util.List[Label[_]],
      useDefaultConfig: Boolean = true
  ): util.Map[String, String] = {
    val (configs, defaultEngineConfigs) = getConfigsByLabelList(labelList, useDefaultConfig, null)
    val configMap = new util.HashMap[String, String]()
    if (null != defaultEngineConfigs) {
      defaultEngineConfigs.asScala.foreach { keyValue =>
        if (
            StringUtils
              .isNotBlank(keyValue.getKey) && StringUtils.isNotEmpty(keyValue.getConfigValue)
        ) {
          configMap.put(keyValue.getKey, keyValue.getConfigValue)
        }
      }
    }
    if (null != configs) {
      configs.asScala.foreach { keyValue =>
        if (StringUtils.isNoneBlank(keyValue.getKey)) {
          if (StringUtils.isNoneEmpty(keyValue.getConfigValue)) {
            configMap.put(keyValue.getKey, keyValue.getConfigValue)
          } else {
            configMap.put(keyValue.getKey, keyValue.getDefaultValue)
          }
        }
      }
    }
    configMap
  }

  @Transactional
  def persisteUservalue(
      configs: util.List[ConfigKeyValue],
      defaultConfigs: util.List[ConfigKeyValue],
      combinedLabel: CombinedLabel,
      existsLabel: ConfigLabel
  ): Unit = {
    logger.info(
      s"Start checking the integrity of user configuration data(开始检查用户配置数据的完整性): label标签为：${combinedLabel.getStringValue}"
    )
    val userConfigList = configs.asScala
    val userConfigKeyIdList = userConfigList.map(config => config.getId)
    val defaultConfigsList = defaultConfigs.asScala
    val parsedLabel = LabelEntityParser.parseToConfigLabel(combinedLabel)
    if (existsLabel == null) {
      logger.info(
        "start to create label for user(开始为用户创建label)：" + "labelKey:" + parsedLabel.getLabelKey + " , " + "labelValue:" + parsedLabel.getStringValue
      )
      labelMapper.insertLabel(parsedLabel)
      logger.info("Creation completed(创建完成！)：" + parsedLabel)
    }
    defaultConfigsList.foreach(defaultConfig => {
      if (!userConfigKeyIdList.contains(defaultConfig.getId)) {
        logger.info(
          s"Initialize database configuration information for users(为用户初始化数据库配置信息)：" + s"configKey: ${defaultConfig.getKey}"
        )
        val configValue = new ConfigValue
        configValue.setConfigKeyId(defaultConfig.getId)
        if (existsLabel == null) {
          configValue.setConfigLabelId(parsedLabel.getId)
        } else {
          configValue.setConfigLabelId(existsLabel.getId)
        }
        configValue.setConfigValue("")
        configMapper.insertValue(configValue)
        logger.info(
          s"Initialization of user database configuration information completed(初始化用户数据库配置信息完成)：configKey: ${defaultConfig.getKey}"
        )
      }
    })
    logger.info(
      s"User configuration data integrity check completed!(用户配置数据完整性检查完毕！): label标签为：${combinedLabel.getStringValue}"
    )
  }

  def queryConfigByLabel(
      labelList: java.util.List[Label[_]],
      isMerge: Boolean = true,
      filter: String = null
  ): ResponseQueryConfig = {
    LabelParameterParser.labelCheck(labelList)
    val allGolbalUserConfig = getConfigMapByLabels(labelList)
    val defaultLabel = LabelParameterParser.changeUserToDefault(labelList)
    val allGolbalDefaultConfig = getConfigMapByLabels(defaultLabel)
    val config = new ResponseQueryConfig
    config.setKeyAndValue(getMap(allGolbalDefaultConfig, allGolbalUserConfig, filter))
    config
  }

  def queryDefaultEngineConfig(engineTypeLabel: EngineTypeLabel): ResponseQueryConfig = {
    val labelList = LabelEntityParser.generateUserCreatorEngineTypeLabelList(
      "*",
      "*",
      engineTypeLabel.getEngineType,
      engineTypeLabel.getVersion
    )
    queryConfigByLabel(labelList)
  }

  def queryGlobalConfig(userName: String): ResponseQueryConfig = {
    val labelList =
      LabelEntityParser.generateUserCreatorEngineTypeLabelList(userName, "*", "*", "*")
    queryConfigByLabel(labelList)
  }

  def queryConfig(
      userCreatorLabel: UserCreatorLabel,
      engineTypeLabel: EngineTypeLabel,
      filter: String
  ): ResponseQueryConfig = {
    val labelList = new util.ArrayList[Label[_]]
    labelList.add(userCreatorLabel)
    labelList.add(engineTypeLabel)
    queryConfigByLabel(labelList, true, filter)
  }

  def queryConfigWithGlobal(
      userCreatorLabel: UserCreatorLabel,
      engineTypeLabel: EngineTypeLabel,
      filter: String
  ): ResponseQueryConfig = {
    val globalConfig = queryGlobalConfig(userCreatorLabel.getUser)
    val engineConfig = queryConfig(userCreatorLabel, engineTypeLabel, filter)
    globalConfig.getKeyAndValue.asScala.foreach(keyAndValue => {
      if (!engineConfig.getKeyAndValue.containsKey(keyAndValue._1)) {
        engineConfig.getKeyAndValue.put(keyAndValue._1, keyAndValue._2)
      }
    })
    engineConfig
  }

  private def getMap(
      all: util.Map[String, String],
      user: util.Map[String, String],
      filter: String = null
  ): util.Map[String, String] = {
    val map = new util.HashMap[String, String]()
    if (filter != null) {
      if (null != all) {
        all.asScala.foreach { keyValue =>
          if (keyValue._1.contains(filter)) {
            map.put(keyValue._1, keyValue._2)
          }
        }
      }
      if (null != user) {
        user.asScala.foreach { keyValue =>
          if (keyValue._1.contains(filter)) {
            map.put(keyValue._1, keyValue._2)
          }
        }
      }
    } else {
      map.putAll(all)
      map.putAll(user)
    }
    // user conf reset default value
    map
  }

}
