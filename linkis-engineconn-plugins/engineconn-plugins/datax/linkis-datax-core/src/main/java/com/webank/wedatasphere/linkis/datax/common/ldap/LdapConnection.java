/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datax.common.ldap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

/**
 * @author davidhua
 * 2020/4/9
 */
public class LdapConnection {
    private Hashtable<String, String> env = new Hashtable<>();
    private LdapContext context;
    public LdapConnection(Hashtable<String, String> env){
        if(null != env){
            this.env = env;
        }
    }

    /**
     * Reconnect
     * @param principle
     * @param credentials
     * @throws NamingException
     */
    public void reconnect(String principle, String credentials) throws NamingException {
        if(null == context){
            env.put(Context.SECURITY_PRINCIPAL, principle);
            env.put(Context.SECURITY_CREDENTIALS, credentials);
            this.context = new InitialLdapContext(env, null);
        }else{
            this.context.addToEnvironment(Context.SECURITY_PRINCIPAL, principle);
            this.context.addToEnvironment(Context.SECURITY_CREDENTIALS, credentials);
            this.context.reconnect(null);
        }
    }

    public LdapContext getContext() {
        return context;
    }

    public void setContext(LdapContext context) {
        this.context = context;
    }
}
