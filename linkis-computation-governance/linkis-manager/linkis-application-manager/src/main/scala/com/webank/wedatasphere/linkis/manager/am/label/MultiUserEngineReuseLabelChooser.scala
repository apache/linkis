package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.am.exception.{AMErrorCode, AMErrorException}
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
class MultiUserEngineReuseLabelChooser extends EngineReuseLabelChooser with Logging {

  private val multiUserEngine = AMConfiguration.MULTI_USER_ENGINE_TYPES.getValue.split(",")

  private val userMap = getMultiUserEngineUserMap()

  def getMultiUserEngineUserMap(): util.Map[String, String] = {
    val userJson = AMConfiguration.MULTI_USER_ENGINE_USER.getValue
    if (StringUtils.isNotBlank(userJson)) {
      val userMap = BDPJettyServerHelper.gson.fromJson(userJson, classOf[java.util.Map[String, String]])
      userMap
    } else {
      throw new AMErrorException(AMErrorCode.AM_CONF_ERROR.getCode, s"Multi-user engine parameter configuration error,please check key ${AMConfiguration.MULTI_USER_ENGINE_USER.key}")
    }

  }

  /**
   * 过滤掉支持多用户引擎的UserCreator Label
   *
   * @param labelList
   * @return
   */
  override def chooseLabels(labelList: util.List[Label[_]]): util.List[Label[_]] = {
    val labels = labelList.asScala
    val engineTypeLabelOption = labels.find(_.isInstanceOf[EngineTypeLabel])
    if (engineTypeLabelOption.isDefined) {
      val engineTypeLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
      val maybeString = multiUserEngine.find(_.equalsIgnoreCase(engineTypeLabel.getEngineType))
      val userCreatorLabelOption = labels.find(_.isInstanceOf[UserCreatorLabel])
      if (maybeString.isDefined && userCreatorLabelOption.isDefined) {
        val userAdmin = userMap.get(engineTypeLabel.getEngineType)
        val userCreatorLabel = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
        info(s"For multi user engine to reset userCreatorLabel user ${userCreatorLabel.getUser} to Admin $userAdmin ")
        userCreatorLabel.setUser(userCreatorLabel.getUser)
        return labels.asJava
      }
    }
    labelList
  }

}
