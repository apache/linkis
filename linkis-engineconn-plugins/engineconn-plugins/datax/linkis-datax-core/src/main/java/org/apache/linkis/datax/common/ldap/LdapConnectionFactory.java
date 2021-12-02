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

package org.apache.linkis.datax.common.ldap;


import org.apache.commons.pool.PoolableObjectFactory;

import javax.naming.Context;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

/**
 * @author davidhua
 * 2020/4/9
 */
public class LdapConnectionFactory implements PoolableObjectFactory<LdapConnection> {

    private String ldapUrl;

    private String baseDn;

    public LdapConnectionFactory(String url, String baseDn){
        this.ldapUrl = url;
        this.baseDn = baseDn;
    }
    @Override
    public LdapConnection makeObject() throws Exception {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        String url = ldapUrl;
        if(!url.endsWith("/")){
            url += "/";
        }
        env.put(Context.PROVIDER_URL, url + baseDn);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        return new LdapConnection(env);
    }

    @Override
    public void destroyObject(LdapConnection ldapConnection) throws Exception {
        LdapContext ldapContext = ldapConnection.getContext();
        if(null != ldapContext){
            try {
                ldapContext.close();
            }catch(Exception e){
                //Ignore
            }
        }
    }

    @Override
    public boolean validateObject(LdapConnection ldapConnection) {
        return false;
    }

    @Override
    public void activateObject(LdapConnection ldapConnection) throws Exception {

    }

    @Override
    public void passivateObject(LdapConnection ldapConnection) throws Exception {

    }
}
