package com.webank.wedatasphere.linkis.gateway.security.sso

import java.net.URI

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.utils.ClassUtils
import com.webank.wedatasphere.linkis.gateway.config.GatewayConfiguration
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext

trait SSOInterceptor {

  /**
    * 如果打开SSO单点登录功能，当前端跳转SSO登录页面登录成功后，前端再次转发请求给gateway。
    * 用户需实现该接口，通过Request返回user
    * @param gatewayContext
    * @return
    */
  def getUser(gatewayContext: GatewayContext): String

  /**
    * 通过前端的requestUrl，用户传回一个可跳转的SSO登录页面URL。
    * 要求：需带上原请求URL，以便登录成功后能跳转回来
    * @param requestUrl
    * @return
    */
  def redirectTo(requestUrl: URI): String

  /**
    * gateway退出时，会调用此接口，以保证gateway清除cookie后，SSO单点登录也会把登录信息清除掉
    * @param gatewayContext
    */
  def logout(gatewayContext: GatewayContext): Unit

}
object SSOInterceptor {
  import scala.collection.JavaConversions._
  private var interceptor: SSOInterceptor = _
  def getSSOInterceptor: SSOInterceptor = if(interceptor != null) interceptor else {
    val ssoInterceptors = DataWorkCloudApplication.getApplicationContext.getBeansOfType(classOf[SSOInterceptor])
    if(ssoInterceptors != null && !ssoInterceptors.isEmpty) {
      interceptor = ssoInterceptors.head._2
    } else {
      interceptor = ClassUtils.getClassInstance(GatewayConfiguration.SSO_INTERCEPTOR_CLASS.getValue)
    }
    interceptor
  }
}