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
import org.apache.linkis.configuration.dao.{ConfigMapper, LabelMapper}
import org.apache.linkis.configuration.entity.{CategoryLabel, CategoryLabelVo, ConfigValue}
import org.apache.linkis.configuration.exception.ConfigurationException
import org.apache.linkis.configuration.util.LabelEntityParser
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder
import org.apache.linkis.manager.label.entity.CombinedLabel
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util
import java.util.{List, Locale}

import scala.collection.JavaConverters._

@Service
class CategoryService extends Logging {

  @Autowired
  private var configMapper: ConfigMapper = _

  @Autowired
  private var configurationService: ConfigurationService = _

  @Autowired
  private var labelMapper: LabelMapper = _

  private val combinedLabelBuilder = new CombinedLabelBuilder

  def setCategoryVo(vo: CategoryLabelVo, categoryLabel: CategoryLabel): Unit = {
    vo.setCategoryId(categoryLabel.getCategoryId)
    vo.setLabelId(categoryLabel.getId)
    vo.setCreateTime(categoryLabel.getCreateTime)
    vo.setUpdateTime(categoryLabel.getUpdateTime)
    vo.setLevel(categoryLabel.getLevel)
    if (StringUtils.isNotEmpty(categoryLabel.getDescription)) {
      vo.setDescription(categoryLabel.getDescription)
    }
    if (StringUtils.isNotEmpty(categoryLabel.getTag)) {
      vo.setTag(categoryLabel.getTag)
    }

  }

  def buildCategoryTree(categoryLabelList: util.List[CategoryLabel]): util.List[CategoryLabelVo] = {
    val firstCategoryList = new util.ArrayList[CategoryLabelVo]()
    val secondaryCategoryList = new util.ArrayList[CategoryLabelVo]()
    categoryLabelList.asScala.foreach(categoryLabel => {
      val vo = new CategoryLabelVo
      setCategoryVo(vo, categoryLabel)
      val combinedLabel = combinedLabelBuilder
        .buildFromStringValue(categoryLabel.getLabelKey, categoryLabel.getStringValue)
        .asInstanceOf[CombinedLabel]
      val labelList = combinedLabel.getValue
      labelList.asScala.foreach(label =>
        label match {
          case u: UserCreatorLabel =>
            if (categoryLabel.getLevel == 1) {
              vo.setCategoryName(u.getCreator)
              firstCategoryList.add(vo)
            } else if (categoryLabel.getLevel == 2) {
              vo.setFatherCategoryName(u.getCreator)
            }
          case e: EngineTypeLabel =>
            if (categoryLabel.getLevel == 2) {
              vo.setCategoryName(e.getStringValue)
              secondaryCategoryList.add(vo)
            }
          case _ =>
        }
      )
    })
    secondaryCategoryList.asScala.foreach(secondaryVo => {
      // TODO use getOrElse
      firstCategoryList.asScala
        .find(_.getCategoryName.equals(secondaryVo.getFatherCategoryName))
        .get
        .getChildCategory
        .add(secondaryVo)
    })
    firstCategoryList
  }

  def getAllCategory(language: String): util.List[CategoryLabelVo] = {
    val categoryLabelList = configMapper.getCategory()
    val categoryLabelTreeList = buildCategoryTree(categoryLabelList)
    if (!"en".equals(language)) {
      categoryLabelTreeList.asScala
        .filter(_.getCategoryId == 1)
        .foreach(_.setCategoryName(Configuration.GLOBAL_CONF_CHN_NAME))
    }
    categoryLabelTreeList
  }

  def getCategoryById(categoryId: Integer): Option[CategoryLabelVo] = {
    val categoryLabelTreeList = getAllCategory(null)
    categoryLabelTreeList.asScala.find(_.getCategoryId == categoryId)
  }

  def generateCategoryLabel(
      labelId: Integer,
      description: String,
      level: Integer
  ): CategoryLabel = {
    val categoryLabel = new CategoryLabel
    categoryLabel.setId(labelId)
    categoryLabel.setDescription(description)
    categoryLabel.setLevel(level)
    categoryLabel
  }

  @Transactional
  def createFirstCategory(categoryName: String, description: String): Unit = {
    val categoryList =
      getAllCategory(null).asScala.map(category => category.getCategoryName.toLowerCase())
    if (categoryList.contains(categoryName.toLowerCase(Locale.ROOT))) {
      throw new ConfigurationException(
        s"category name : ${categoryName} is exist, cannot be created(目录名：${categoryName}已存在，无法创建)"
      )
    }
    val combinedLabel = configurationService.generateCombinedLabel(null, null, null, categoryName)
    val parsedLabel = LabelEntityParser.parseToConfigLabel(combinedLabel)
    // New Query Avoid Repeated Insertion
    val configValue =
      labelMapper.getLabelByKeyValue(parsedLabel.getLabelKey, parsedLabel.getStringValue)
    if (configValue != null) {
      parsedLabel.setId(configValue.getId)
    } else {
      labelMapper.insertLabel(parsedLabel)
    }
    if (parsedLabel.getId != null) {
      val categoryLabel = generateCategoryLabel(parsedLabel.getId, description, 1)
      configMapper.insertCategory(categoryLabel)
      logger.info(
        s"success to create category: ${categoryName} --category id: ${categoryLabel.getCategoryId} " +
          s"--category level: 1"
      )
    }
  }

  @Transactional
  def associateConfigKey(labelId: Integer, stringValue: String): Unit = {
    if (!StringUtils.isEmpty(stringValue) && labelId != null) {
      val keyList = configMapper.listKeyByStringValue(stringValue)
      val keyIdList = keyList.asScala.map(_.getId)
      if (!keyIdList.isEmpty) {
        val configValueList = new util.ArrayList[ConfigValue]()
        keyIdList.foreach(keyId => {
          val configValue = new ConfigValue
          configValue.setConfigKeyId(keyId)
          configValue.setConfigValue("")
          configValue.setConfigLabelId(labelId)
          configValueList.add(configValue)
        })
        Utils.tryQuietly(configMapper.insertValueList(configValueList))
      }
    }
  }

  @Transactional
  def createSecondCategory(
      categoryId: Integer,
      engineType: String,
      version: String,
      description: String
  ): Unit = {
    val categoryTree = getCategoryById(categoryId)
    val categoryList = categoryTree.getOrElse(
      throw new ConfigurationException(
        s"category id : ${categoryId} is not exist, cannot be created(目录id：${categoryId}不存在，无法创建)"
      )
    )
    val childList = categoryList.getChildCategory.asScala
    if (
        childList != null && !childList
          .filter(
            _.getCategoryName
              .toLowerCase(Locale.ROOT)
              .equals(engineType.toLowerCase(Locale.ROOT) + "-" + version)
          )
          .isEmpty
    ) {
      throw new ConfigurationException(
        s"${engineType}-${version} is exist, cannot be created(${engineType}-${version}已经存在，无法创建)"
      )
    }
    val creator = categoryList.getCategoryName match {
      case Configuration.GLOBAL_CONF_CHN_NAME | Configuration.GLOBAL_CONF_CHN_OLDNAME =>
        throw new ConfigurationException(
          "Global setting do not allow the configuration of engines to be added(全局设置不允许添加引擎配置!)"
        )
      case _ => categoryList.getCategoryName
    }
    val combinedLabel =
      configurationService.generateCombinedLabel(engineType, version, null, creator)
    val parsedLabel = LabelEntityParser.parseToConfigLabel(combinedLabel)
    Utils.tryCatch(labelMapper.insertLabel(parsedLabel)) {
      case exception: DuplicateKeyException =>
        parsedLabel.setId(
          labelMapper
            .getLabelByKeyValue(parsedLabel.getLabelKey, parsedLabel.getStringValue)
            .getId
        )
      case exception: Exception => throw exception
    }
    if (parsedLabel.getId != null) {
      val categoryLabel = generateCategoryLabel(parsedLabel.getId, description, 2)
      configMapper.insertCategory(categoryLabel)
      logger.info(
        s"success to create category: ${combinedLabel.getStringValue} --category id: ${categoryLabel.getCategoryId} " +
          s"--category level: 2"
      )
      // 1.Here, the engine and the corresponding engine default configuration are associated and initialized,
      // and the relevant configuration of the corresponding version of the engine needs to be entered in the database in advance
      // 2.Now all the default configurations obtained are the default configuration of the engine level, and there is no default configuration of the application level for the time being.
      // If you need to consider, you need to change the creator of the label generated here to the corresponding application,
      // and you need to modify the getFullTree to obtain the label of the defaultConfig, and also replace its creator with the creator of the application.
      val linkedEngineTypeLabel =
        configurationService.generateCombinedLabel(engineType, version, null, null)
      val linkedEngineTypeLabelInDb = labelMapper.getLabelByKeyValue(
        linkedEngineTypeLabel.getLabelKey,
        linkedEngineTypeLabel.getStringValue
      )
      if (linkedEngineTypeLabelInDb != null) {
        associateConfigKey(linkedEngineTypeLabelInDb.getId, linkedEngineTypeLabel.getStringValue)
      } else {
        val engineLabel = LabelEntityParser.parseToConfigLabel(linkedEngineTypeLabel)
        labelMapper.insertLabel(engineLabel)
      }
    }
  }

  @Transactional
  def deleteCategory(categoryId: Integer): Unit = {
    if (categoryId > 0) {
      val categoryLabel = configMapper.getCategoryById(categoryId)
      if (categoryLabel == null) {
        throw new ConfigurationException(
          s"cannot find category, categoryId:${categoryId}" +
            s"(没有找到要删除的目录，目录Id:${categoryId})"
        )
      }
      categoryLabel.getLevel.toInt match {
        case 1 => deleteAllNode(categoryId)
        case _ => deleteCurrentNode(categoryId)
      }
    }
  }

  def deleteAllNode(categoryId: Integer): Unit = {
    val categoryLabelVo = getCategoryById(categoryId).getOrElse(null)
    if (categoryLabelVo != null) {
      val idList = new util.ArrayList[Integer]()
      idList.add(categoryLabelVo.getCategoryId)
      val childCategoryList = categoryLabelVo.getChildCategory.asScala
      childCategoryList.foreach(child => idList.add(child.getCategoryId))
      configMapper.deleteCategory(idList)
      idList.clear()
      idList.add(categoryLabelVo.getLabelId)
      childCategoryList.foreach(child => idList.add(child.getLabelId))
      labelMapper.deleteLabel(idList)
      logger.info(
        s"success to delete category:${categoryLabelVo.getCategoryName}, " +
          s"with child category:${childCategoryList.map(_.getCategoryName).toArray}"
      )
    }
  }

  def updateCategory(categoryId: Integer, description: String): Unit = {
    val categoryLabel = new CategoryLabel
    categoryLabel.setCategoryId(categoryId)
    categoryLabel.setDescription(description)
    configMapper.updateCategory(categoryLabel)
  }

  def deleteCurrentNode(categoryId: Integer): Unit = {
    val categoryLabel = configMapper.getCategoryById(categoryId)
    if (categoryLabel != null) {
      val idList = new util.ArrayList[Integer]()
      idList.add(categoryId)
      configMapper.deleteCategory(idList)
      idList.clear()
      idList.add(categoryLabel.getId)
      labelMapper.deleteLabel(idList)
      logger.info(s"success to delete category:${categoryLabel.getStringValue}")
    }
  }

}
