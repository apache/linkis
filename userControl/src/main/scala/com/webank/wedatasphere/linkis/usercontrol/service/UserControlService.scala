package com.webank.wedatasphere.linkis.usercontrol.service

import com.webank.wedatasphere.linkis.protocol.usercontrol.{RequestLogin, RequestRegister, ResponseLogin, ResponseRegister}

/**
  * Created by alexyang
  */
trait UserControlService {

  // Login here when usercontrol-switch is on in gateway(网关模块用户控制开关开启后，将从这里登录）
  def login(requestLogin: RequestLogin) : ResponseLogin

  // register here when usercontrol-switch is on in gateway
  def register(requestRegister: RequestRegister) : ResponseRegister
}
