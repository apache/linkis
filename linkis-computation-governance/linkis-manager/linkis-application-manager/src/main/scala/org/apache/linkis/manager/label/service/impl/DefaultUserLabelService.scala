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

package org.apache.linkis.manager.label.service.impl

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.label.LabelManagerUtils
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.errorcode.LabelCommonErrorCodeSummary._
import org.apache.linkis.manager.label.exception.LabelErrorException
import org.apache.linkis.manager.label.service.UserLabelService
import org.apache.linkis.manager.persistence.LabelManagerPersistence

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util

import scala.collection.JavaConverters._

@Service
class DefaultUserLabelService extends UserLabelService with Logging {

  @Autowired
  var labelManagerPersistence: LabelManagerPersistence = _

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  override def addLabelToUser(user: String, labels: util.List[Label[_]]): Unit = {
    // 逻辑基本同 addLabelsToNode
    labels.asScala.foreach(addLabelToUser(user, _))
  }

  override def addLabelToUser(user: String, label: Label[_]): Unit = {
    // instance 存在
    // 1.插入linkis_manager_label 表，这里表应该有唯一约束，key和valueStr　调用Persistence的addLabel即可，忽略duplicateKey异常
    val persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label)
    Utils.tryAndWarn(labelManagerPersistence.addLabel(persistenceLabel))
    // 2.查询出当前label的id
    val dbLabel = labelManagerPersistence
      .getLabelsByKey(label.getLabelKey)
      .asScala
      .find(_.getStringValue.equals(persistenceLabel.getStringValue))
      .get
    // 3.根据usr 找出当前关联当前user的所有labels,看下有没和当前key重复的
    // TODO: persistence 这里最好提供个一次查询的方法
    val userRelationLabels = labelManagerPersistence.getLabelsByUser(user)
    val duplicatedKeyLabel =
      userRelationLabels.asScala.find(_.getLabelKey.equals(dbLabel.getLabelKey))
    // 4.找出重复key,删除这个relation
    duplicatedKeyLabel.foreach(l => {
      labelManagerPersistence.removeLabelFromUser(user, util.Arrays.asList(l.getId))
      userRelationLabels.remove(duplicatedKeyLabel.get)
    })
    // 5.插入新的relation 需要抛出duplicateKey异常，回滚
    labelManagerPersistence.addLabelToUser(user, util.Arrays.asList(dbLabel.getId))
    // 6.重新查询，确认更新，如果没对上，抛出异常，回滚
    userRelationLabels.add(dbLabel)
    val newUserRelationLabels = labelManagerPersistence.getLabelsByUser(user)

    if (
        newUserRelationLabels.size != userRelationLabels.size
        || !newUserRelationLabels.asScala
          .map(_.getId)
          .asJava
          .containsAll(userRelationLabels.asScala.map(_.getId).asJava)
    ) {
      throw new LabelErrorException(
        UPDATE_LABEL_FAILED.getErrorCode,
        UPDATE_LABEL_FAILED.getErrorDesc
      )
    }
  }

  override def removeLabelFromUser(user: String, labels: util.List[Label[_]]): Unit = {
    // 这里前提是表中保证了同个key，只会有最新的value保存在数据库中
    val dbLabels =
      labelManagerPersistence.getLabelsByUser(user).asScala.map(l => (l.getLabelKey, l)).toMap
    labelManagerPersistence.removeLabelFromUser(
      user,
      labels.asScala.map(l => dbLabels(l.getLabelKey).getId).asJava
    )
  }

  override def getUserByLabel(label: Label[_]): util.List[String] = {
    // TODO: persistence 需要提供 key，valueString 找到相关label的方法
    // 1.找出当前label 对应的数据库的label
    val labels = labelManagerPersistence
      .getLabelsByKey(label.getLabelKey)
      .asScala
      .filter(_.getStringValue.equals(label.getStringValue))
    // 2.获取用户并且去重
    labelManagerPersistence.getUserByLabels(labels.map(_.getId).asJava).asScala.distinct
  }.asJava

  override def getUserByLabels(labels: util.List[Label[_]]): util.List[String] = {
    // 去重
    labels.asScala.flatMap(label => getUserByLabel(label).asScala).distinct
  }.asJava

  override def getUserLabels(user: String): util.List[Label[_]] = {
    labelManagerPersistence.getLabelsByUser(user).asScala.map { label =>
      labelFactory.createLabel(label.getLabelKey, label.getValue)
    }
  }.toList.asJava

}
