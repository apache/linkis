/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.common.utils

import java.util.Hashtable

import org.apache.linkis.common.conf.CommonVars
import javax.naming.Context
import javax.naming.ldap.InitialLdapContext
import org.apache.commons.lang.StringUtils



object LDAPUtils extends Logging {

  val url =  CommonVars("wds.linkis.ldap.proxy.url", "").getValue
  val baseDN = CommonVars("wds.linkis.ldap.proxy.baseDN", "").getValue
  val userNameFormat = CommonVars("wds.linkis.ldap.proxy.userNameFormat", "").getValue
  def login(userID: String, password: String): Unit = {
    val env = new Hashtable[String, String]()
    val bindDN = if (StringUtils.isBlank(userNameFormat)) userID else {
      userNameFormat.split("%s", -1).mkString(userID)
    }
    val bindPassword = password
    env.put(Context.SECURITY_AUTHENTICATION, "simple")
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    env.put(Context.PROVIDER_URL, url + baseDN)
    env.put(Context.SECURITY_PRINCIPAL, bindDN)
    env.put(Context.SECURITY_CREDENTIALS, bindPassword)
    //    Utils.tryCatch {
    new InitialLdapContext(env, null)
    info(s"user $userID login success.")
    //      true
    //    } { e =>
    //        error(s"user $userID login failed.", e)
    //        false
    //    }
  }
}

