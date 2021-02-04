package com.webank.wedatasphere.linkis.rpc.instancealias.impl

import java.util.Base64
import java.util.regex.Pattern

import com.webank.wedatasphere.linkis.rpc.instancealias.InstanceAliasConverter
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * @Author alexyang
 * @Date 2020/2/18
 */
@Component
class DefaultInstanceAliasConverter extends InstanceAliasConverter  {

  val pattern = Pattern.compile("[a-zA-Z\\d=\\+/]+")

  // todo use base64 for the moment
  override def instanceToAlias(instance: String): String = {
    new String(Base64.getEncoder.encode(instance.getBytes()))
  }

  override def aliasToInstance(alias: String): String = {
    new String(Base64.getDecoder.decode(alias))
  }

  override def checkAliasFormatValid(alias: String): Boolean = {
    if (StringUtils.isBlank(alias)) {
      return false
    }
    val matcher = pattern.matcher(alias)
    matcher.find()
  }
}
