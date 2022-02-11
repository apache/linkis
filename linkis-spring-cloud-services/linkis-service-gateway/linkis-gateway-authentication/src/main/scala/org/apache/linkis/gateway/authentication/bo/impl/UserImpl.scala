package org.apache.linkis.gateway.authentication.bo.impl

import org.apache.linkis.gateway.authentication.bo.User

/**
  * Created by shangda on 2021/9/8.
  */
class UserImpl(userName: String) extends User {

  override def getUserName(): String = userName
}
