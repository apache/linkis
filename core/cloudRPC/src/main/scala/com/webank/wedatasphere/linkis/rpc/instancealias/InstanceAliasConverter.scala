package com.webank.wedatasphere.linkis.rpc.instancealias

/**
 * @Author alexyang
 * @Date 2020/2/18
 */
trait InstanceAliasConverter {

  def instanceToAlias(instance: String): String

  def aliasToInstance(alias: String): String

  def checkAliasFormatValid(alias: String): Boolean
}
