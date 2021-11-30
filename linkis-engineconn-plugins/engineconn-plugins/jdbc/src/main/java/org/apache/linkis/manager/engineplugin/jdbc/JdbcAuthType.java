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

package org.apache.linkis.manager.engineplugin.jdbc;

public enum JdbcAuthType {
    /**
     * the auth type of jdbc
     */
    SIMPLE("SIMPLE"),
    USERNAME("USERNAME"),
    KERBEROS("KERBEROS");

    private final String authType;

    JdbcAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthType() {
        return authType;
    }

    public static JdbcAuthType of(String authType) {
        for (JdbcAuthType s : values()) {
            if (authType.equals(s.getAuthType())) {
                return s;
            }
        }
        throw new UnsupportedOperationException("the login authentication type of " + authType + " is not supported");
    }
}
