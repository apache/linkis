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

package org.apache.linkis.metadata.hive.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class DatabaseQueryParam {

    /** the query user's username */
    private String userName;

    /** the query db name */
    private String dbName;

    /** the query table name */
    private String tableName;

    /** the query table's partition name */
    private String partitionName;

    /** the query storage description id */
    private String sdId;

    /** the user's role */
    private List<String> roles;

    public static DatabaseQueryParam of(String userName) {
        return new DatabaseQueryParam(userName);
    }

    public DatabaseQueryParam() {
        this.roles = Collections.emptyList();
    }

    public DatabaseQueryParam(String username) {
        this.userName = username;
        this.roles = Collections.emptyList();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public DatabaseQueryParam withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public DatabaseQueryParam withDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DatabaseQueryParam withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public DatabaseQueryParam withRoles(List<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            this.roles.addAll(roles);
        }
        return this;
    }

    public DatabaseQueryParam withRole(String role) {
        if (StringUtils.isNotBlank(role)) {
            this.roles.add(role);
        }
        return this;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public DatabaseQueryParam withPartitionName(String partitionName) {
        this.partitionName = partitionName;
        return this;
    }

    public String getSdId() {
        return sdId;
    }

    public void setSdId(String sdId) {
        this.sdId = sdId;
    }

    public DatabaseQueryParam withSdId(String sdId) {
        this.sdId = sdId;
        return this;
    }
}
