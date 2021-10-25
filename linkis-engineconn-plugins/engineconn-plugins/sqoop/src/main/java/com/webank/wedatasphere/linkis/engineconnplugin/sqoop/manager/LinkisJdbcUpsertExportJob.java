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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import com.cloudera.sqoop.manager.ConnManager;
import com.cloudera.sqoop.manager.ExportJobContext;
import com.cloudera.sqoop.mapreduce.db.DBConfiguration;
import com.cloudera.sqoop.mapreduce.db.DBOutputFormat;


public class LinkisJdbcUpsertExportJob  extends LinkisJdbcUpdateExportJob{
    public static final Log LOG = LogFactory.getLog(
            LinkisJdbcUpsertExportJob.class.getName());

    public LinkisJdbcUpsertExportJob(final ExportJobContext context,
                               final Class<? extends OutputFormat> outputFormatClass)
            throws IOException {
        super(context, null, null, outputFormatClass);
    }

    @Override
    protected void configureOutputFormat(Job job, String tableName,
                                         String tableClassName) throws IOException {

        ConnManager mgr = context.getConnManager();
        try {
            String username = options.getUsername();
            if (null == username || username.length() == 0) {
                DBConfiguration.configureDB(job.getConfiguration(),
                        mgr.getDriverClass(),
                        options.getConnectString(),
                        options.getConnectionParams());
            } else {
                DBConfiguration.configureDB(job.getConfiguration(),
                        mgr.getDriverClass(),
                        options.getConnectString(),
                        username, options.getPassword(),
                        options.getConnectionParams());
            }

            String [] colNames = options.getColumns();
            if (null == colNames) {
                colNames = mgr.getColumnNames(tableName);
            }
            if (null == colNames) {
                throw new IOException(
                        "Export column names could not be determined for " + tableName);
            }
            DBOutputFormat.setOutput(job, mgr.escapeTableName(tableName), mgr.escapeColNames(colNames));

            String updateKeyColumns = options.getUpdateKeyCol();
            if (null == updateKeyColumns) {
                throw new IOException("Update key column not set in export job");
            }
            // Update key columns lookup and removal
            Set<String> updateKeys = new LinkedHashSet<String>();
            StringTokenizer stok = new StringTokenizer(updateKeyColumns, ",");
            while (stok.hasMoreTokens()) {
                String nextUpdateKey = stok.nextToken().trim();
                if (nextUpdateKey.length() > 0) {
                    updateKeys.add(nextUpdateKey);
                } else {
                    throw new RuntimeException("Invalid update key column value specified"
                            + ": '" + updateKeyColumns + "'");
                }
            }

            if (updateKeys.size() == 0) {
                throw new IOException("Unpdate key columns not valid in export job");
            }

            job.setOutputFormatClass(getOutputFormatClass());
            job.getConfiguration().set(SQOOP_EXPORT_TABLE_CLASS_KEY, tableClassName);
            job.getConfiguration().set(SQOOP_EXPORT_UPDATE_COL_KEY, updateKeyColumns);
        } catch (ClassNotFoundException cnfe) {
            throw new IOException("Could not load OutputFormat", cnfe);
        }
    }
}
