package com.webank.wedatasphere.linkis.computation.client.operator.impl

import com.webank.wedatasphere.linkis.computation.client.operator.OnceJobOperator

/**
  * Created by enjoyyin on 2021/6/7.
  */
class ApplicationInfoOperator extends OnceJobOperator[ApplicationInfo] {

  override def getName: String = ApplicationInfoOperator.OPERATOR_NAME

  override def apply(): ApplicationInfo = {
    //TODO
    ApplicationInfo("", "", "")
  }

}

object ApplicationInfoOperator {
  val OPERATOR_NAME = "application"
}

case class ApplicationInfo(applicationId: String, applicationUrl: String, queue: String)
