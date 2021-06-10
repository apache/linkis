package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.service.common.label.LabelChecker
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._


@Component
class AMLabelChecker extends LabelChecker {

  override def checkEngineLabel(labelList: util.List[Label[_]]): Boolean = {
    checkCorrespondingLabel(labelList, classOf[EngineTypeLabel], classOf[UserCreatorLabel])
  }

  override def checkEMLabel(labelList: util.List[Label[_]]): Boolean = {
    checkCorrespondingLabel(labelList, classOf[EMInstanceLabel])
  }

  override def checkCorrespondingLabel(labelList: util.List[Label[_]], clazz: Class[_]*): Boolean = {
    // TODO: 是否需要做子类的判断
    labelList.filter(null != _).map(_.getClass).containsAll(clazz)
  }
}

object AD{
  def main(args: Array[String]): Unit = {
    val label = new UserCreatorLabel
    val checker = new AMLabelChecker
    println(checker.checkCorrespondingLabel(util.Arrays.asList(label),classOf[UserCreatorLabel]))
  }
}
