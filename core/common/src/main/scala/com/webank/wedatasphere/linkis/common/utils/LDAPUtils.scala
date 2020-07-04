/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.common.utils

import java.util.Hashtable

import javax.naming.Context
import javax.naming.ldap.InitialLdapContext
import com.webank.wedatasphere.linkis.common.conf.CommonVars
import javax.naming.directory.SearchControls


/**
 * Created by enjoyyin on 8/30/17.
 */
object LDAPUtils extends Logging {

  val url = CommonVars("wds.linkis.ldap.proxy.url", "").getValue
  val baseDN = CommonVars("wds.linkis.ldap.proxy.baseDN", "").getValue
  val adminDN = CommonVars("wds.linkis.ldap.proxy.adminDN", "").getValue
  val adminPassw = CommonVars("wds.linkis.ldap.proxy.adminPassw", "").getValue

  def login(userID: String, password: String): Unit = {
    val env = new Hashtable[String, String]()
    val bindDN = getUserDnByName(userID)
    env.put(Context.SECURITY_AUTHENTICATION, "simple")
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    env.put(Context.PROVIDER_URL, url + baseDN)
    env.put(Context.SECURITY_PRINCIPAL, bindDN)
    env.put(Context.SECURITY_CREDENTIALS, password)
    //    Utils.tryCatch {
    new InitialLdapContext(env, null)
    info(s"user $userID login success.")
    //      true
    //    } { e =>
    //        error(s"user $userID login failed.", e)
    //        false
    //    }
  }

  def getUserDnByName(user: String): String = {
    val context = getContext()
    val sc = new SearchControls()
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE)
    val name = context.search("", s"uid=${user}", sc)
    val result = name.next()
    result.getNameInNamespace
  }

  /**
   * 根据传输的uid（useName）获取ldap中对应的属性值，例如传入key=mobile，则获取ldap中mobile信息
   *
   * @param user
   * @param key
   * @return
   */
  def getValueByName(user: String, key: String): String = {
    val context = getContext()
    val sc = new SearchControls()
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE)
    val name = context.search("", s"uid=${user}", sc)
    val result = name.next()
    val attrs = result.getAttributes.getAll
    while (attrs.hasMore) {
      val attr = attrs.next
      info(attr.getID + "====" + attr.get)
      if (attr.getID.equals(key)) {
        return attr.get.toString
      }
    }
    null
  }


  /**
   * 初始化过去ldap context
   *
   * @return
   */
  def getContext(): InitialLdapContext = {
    val env = new Hashtable[String, String]()
    env.put(Context.SECURITY_AUTHENTICATION, "simple")
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    env.put(Context.PROVIDER_URL, url + baseDN)
    env.put(Context.SECURITY_PRINCIPAL, adminDN)
    env.put(Context.SECURITY_CREDENTIALS, adminPassw)
    new InitialLdapContext(env, null)
  }

}

