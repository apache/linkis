/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.highavailable.ha.instancealias.impl

import org.apache.linkis.cs.highavailable.ha.instancealias.InstanceAliasConverter

import org.apache.commons.lang3.StringUtils

import java.util.Base64
import java.util.regex.Pattern

// @Component
class DefaultInstanceAliasConverter extends InstanceAliasConverter {

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
