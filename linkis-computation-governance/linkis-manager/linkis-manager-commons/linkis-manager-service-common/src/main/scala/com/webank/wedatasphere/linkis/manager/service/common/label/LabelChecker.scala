package com.webank.wedatasphere.linkis.manager.service.common.label

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait LabelChecker {

  def checkEngineLabel(labelList: util.List[Label[_]]): Boolean

  def checkEMLabel(labelList: util.List[Label[_]]): Boolean

  def checkCorrespondingLabel(labelList: util.List[Label[_]], clazz: Class[_]*): Boolean
}
