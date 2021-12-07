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


import org.apache.commons.pool.impl.GenericObjectPool;

import javax.naming.AuthenticationException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author davidhua
 * 2020/4/8
 */
public class LdapConnector {

    public static final int MAX_ACTIVE = 50;
    public static final int MAX_IDLE = 5;

    public static final String URL_PROP_NAME = "ldap.url";

    public static final String BASE_DN_PROP_NAME = "ldap.baseDN";

    private static final ConcurrentHashMap<String, LdapConnector> connectorMap = new ConcurrentHashMap<>();

    private GenericObjectPool<LdapConnection> pool;

    public static LdapConnector getInstance(String url, String baseDn){
        return connectorMap.computeIfAbsent(url + baseDn, key -> new LdapConnector(url, baseDn));
    }

    public static LdapConnector getInstance(Properties properties){
        String url = String.valueOf(properties.getOrDefault(URL_PROP_NAME, ""));
        String baseDn = String.valueOf(properties.getOrDefault(BASE_DN_PROP_NAME, ""));
        return connectorMap.computeIfAbsent(url + baseDn, key -> new LdapConnector(url, baseDn));
    }
    private LdapConnector(String url, String baseDn){
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxActive = MAX_ACTIVE;
        config.maxIdle = MAX_IDLE;
        this.pool = new GenericObjectPool<>(
                new LdapConnectionFactory(url, baseDn), config);
    }
    public boolean authenticate(String userName, String password){
        LdapConnection ldapConnection = null;
        try {
            ldapConnection = pool.borrowObject();
            ldapConnection.reconnect(userName, password);
            return true;
        } catch (Exception e) {
            if(!(e instanceof AuthenticationException) && null != ldapConnection){
                try {
                    pool.invalidateObject(ldapConnection);
                } catch (Exception ex) {
                    //Ignore
                }
            }
        } finally {
            if(null != ldapConnection){
                try {
                    pool.returnObject(ldapConnection);
                } catch (Exception e) {
                    //Ignore
                }
            }
        }
        return false;
    }

}
