package com.webank.wedatasphere.linkis.gateway.ujes.route

import com.webank.wedatasphere.linkis.gateway.http.GatewayContext

/**
 *
 * @author wang_zh
 * @date 2020/5/21
 */
trait EntranceGatewayRouterRuler {

  def rule(serviceId: String, gatewayContext: GatewayContext)

}
