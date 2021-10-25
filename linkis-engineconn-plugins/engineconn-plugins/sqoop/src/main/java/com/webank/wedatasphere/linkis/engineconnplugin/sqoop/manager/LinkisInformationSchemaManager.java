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

package com.webank.wedatasphere.linkis.engineconnplugin.sqoop.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudera.sqoop.SqoopOptions;

public abstract class LinkisInformationSchemaManager extends LinkisCatalogQueryManager{



    public static final Log LOG = LogFactory.getLog(
            LinkisInformationSchemaManager.class.getName());

    public LinkisInformationSchemaManager(final String driverClass,
                                    final SqoopOptions opts) {
        super(driverClass, opts);
    }

    protected abstract String getSchemaQuery();

    @Override
    protected String getListTablesQuery() {
        return
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                        + "WHERE TABLE_SCHEMA = (" + getSchemaQuery() + ")";
    }

    @Override
    protected String getListColumnsQuery(String tableName) {
        return
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_SCHEMA = (" + getSchemaQuery() + ") "
                        + "  AND TABLE_NAME = '" + tableName + "' ";
    }

    @Override
    protected String getPrimaryKeyQuery(String tableName) {
        return
                "SELECT kcu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc, "
                        + "  INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu "
                        + "WHERE tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA "
                        + "  AND tc.TABLE_NAME = kcu.TABLE_NAME "
                        + "  AND tc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA "
                        + "  AND tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME "
                        + "  AND tc.TABLE_SCHEMA = (" + getSchemaQuery() + ") "
                        + "  AND tc.TABLE_NAME = '" + tableName + "' "
                        + "  AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY'";
    }
}
