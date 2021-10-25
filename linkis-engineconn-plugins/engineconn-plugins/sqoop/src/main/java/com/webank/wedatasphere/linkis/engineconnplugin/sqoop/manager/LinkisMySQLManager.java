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

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.hbase.HBaseUtil;
import com.cloudera.sqoop.manager.ExportJobContext;
import com.cloudera.sqoop.mapreduce.*;
import com.cloudera.sqoop.util.ExportException;
import com.cloudera.sqoop.util.ImportException;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sqoop.accumulo.AccumuloUtil;
import org.apache.sqoop.manager.ImportJobContext;
import org.apache.sqoop.mapreduce.AccumuloImportJob;
import org.apache.sqoop.mapreduce.HBaseBulkImportJob;
import org.apache.sqoop.mapreduce.mysql.MySQLUpsertOutputFormat;
import org.apache.sqoop.util.LoggingUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LinkisMySQLManager extends LinkisInformationSchemaManager{
    public static final Log LOG = LogFactory.getLog(LinkisMySQLManager.class.getName());

    // driver class to ensure is loaded when making db connection.
    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

    // set to true after we warn the user that we can use direct fastpath.
    private static boolean warningPrinted = false;

    private static final String EXPORT_OPERATION = "export";

    public LinkisMySQLManager(final SqoopOptions opts) {
        super(DRIVER_CLASS, opts);
        Sqoop.sqlManager = this;
    }

    @Override
    protected void initOptionDefaults() {
        if (options.getFetchSize() == null) {
            LOG.info("Preparing to use a MySQL streaming resultset.");
            String operation = options.getToolName();
            if (StringUtils.isNotBlank(operation) && operation.equalsIgnoreCase(EXPORT_OPERATION)) {
                options.setFetchSize(0);
            } else {
                options.setFetchSize(Integer.MIN_VALUE);
            }
        } else if (
                !options.getFetchSize().equals(Integer.MIN_VALUE)
                        && !options.getFetchSize().equals(0)) {
            LOG.info("Argument '--fetch-size " + options.getFetchSize()
                    + "' will probably get ignored by MySQL JDBC driver.");

        }
    }

    @Override
    protected String getPrimaryKeyQuery(String tableName) {
        return "SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = (" + getSchemaQuery() + ") "
                + "AND TABLE_NAME = '"+tableName+"' "
                + "AND COLUMN_KEY = 'PRI'";
    }

    @Override
    protected String getColNamesQuery(String tableName) {
        // Use mysql-specific hints and LIMIT to return fast
        return "SELECT t.* FROM " + escapeTableName(tableName) + " AS t LIMIT 1";
    }

    @Override
    public void importTable(com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {

        // Check that we're not doing a MapReduce from localhost. If we are, point
        // out that we could use mysqldump.
        if (!LinkisMySQLManager.warningPrinted) {
            String connectString = context.getOptions().getConnectString();

            if (null != connectString) {
                // DirectMySQLManager will probably be faster.
                LOG.warn("It looks like you are importing from mysql.");
                LOG.warn("This transfer can be faster! Use the --direct");
                LOG.warn("option to exercise a MySQL-specific fast path.");

                LinkisMySQLManager.markWarningPrinted(); // don't display this twice.
            }
        }

        checkDateTimeBehavior(context);

        // Then run the normal importTable() method.
        selfImportTable(context);
    }


    private void selfImportTable(com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {
        String tableName = context.getTableName();
        String jarFile = context.getJarFile();
        SqoopOptions opts = context.getOptions();

        context.setConnManager(this);

        ImportJobBase importer;
        if (opts.getHBaseTable() != null) {
            // Import to HBase.
            if (!HBaseUtil.isHBaseJarPresent()) {
                throw new ImportException("HBase jars are not present in "
                        + "classpath, cannot import to HBase!");
            }
            if (!opts.isBulkLoadEnabled()){
                importer = new HBaseImportJob(opts, context);
            } else {
                importer = new HBaseBulkImportJob(opts, context);
            }
        } else if (opts.getAccumuloTable() != null) {
            // Import to Accumulo.
            if (!AccumuloUtil.isAccumuloJarPresent()) {
                throw new ImportException("Accumulo jars are not present in "
                        + "classpath, cannot import to Accumulo!");
            }
            importer = new AccumuloImportJob(opts, context);
        } else {
            // Import to HDFS.
            importer = new DataDrivenImportJob(opts, context.getInputFormat(),
                    context);
        }

        Sqoop.jobBase = importer;
        checkTableImportOptions(context);

        String splitCol = getSplitColumn(opts, tableName);
        importer.runImport(tableName, jarFile, splitCol, opts.getConf());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsertTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);
        LOG.warn("MySQL Connector upsert functionality is using INSERT ON");
        LOG.warn("DUPLICATE KEY UPDATE clause that relies on table's unique key.");
        LOG.warn("Insert/update distinction is therefore independent on column");
        LOG.warn("names specified in --update-key parameter. Please see MySQL");
        LOG.warn("documentation for additional limitations.");

        LinkisJdbcUpsertExportJob exportJob =
                new LinkisJdbcUpsertExportJob(context, MySQLUpsertOutputFormat.class);
        exportJob.runExport();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void configureDbOutputColumns(SqoopOptions options) {
        // In case that we're running upsert, we do not want to change column order
        // as we're actually going to use INSERT INTO ... ON DUPLICATE KEY UPDATE
        // clause.
        if (options.getUpdateMode() == SqoopOptions.UpdateMode.AllowInsert) {
            return;
        }

        super.configureDbOutputColumns(options);
    }

    /**
     * Set a flag to prevent printing the --direct warning twice.
     */
    protected static void markWarningPrinted() {
        LinkisMySQLManager.warningPrinted = true;
    }

    /**
     * MySQL allows TIMESTAMP fields to have the value '0000-00-00 00:00:00',
     * which causes errors in import. If the user has not set the
     * zeroDateTimeBehavior property already, we set it for them to coerce
     * the type to null.
     */
    private void checkDateTimeBehavior(ImportJobContext context) {
        final String ZERO_BEHAVIOR_STR = "zeroDateTimeBehavior";
        final String CONVERT_TO_NULL = "=convertToNull";

        String connectStr = context.getOptions().getConnectString();
        if (connectStr.indexOf("jdbc:") != 0) {
            // This connect string doesn't have the prefix we expect.
            // We can't parse the rest of it here.
            return;
        }

        // This starts with 'jdbc:mysql://' ... let's remove the 'jdbc:'
        // prefix so that java.net.URI can parse the rest of the line.
        String uriComponent = connectStr.substring(5);
        try {
            URI uri = new URI(uriComponent);
            String query = uri.getQuery(); // get the part after a '?'

            // If they haven't set the zeroBehavior option, set it to
            // squash-null for them.
            if (null == query) {
                connectStr = connectStr + "?" + ZERO_BEHAVIOR_STR + CONVERT_TO_NULL;
                LOG.info("Setting zero DATETIME behavior to convertToNull (mysql)");
            } else if (query.length() == 0) {
                connectStr = connectStr + ZERO_BEHAVIOR_STR + CONVERT_TO_NULL;
                LOG.info("Setting zero DATETIME behavior to convertToNull (mysql)");
            } else if (query.indexOf(ZERO_BEHAVIOR_STR) == -1) {
                if (!connectStr.endsWith("&")) {
                    connectStr = connectStr + "&";
                }
                connectStr = connectStr + ZERO_BEHAVIOR_STR + CONVERT_TO_NULL;
                LOG.info("Setting zero DATETIME behavior to convertToNull (mysql)");
            }

            LOG.debug("Rewriting connect string to " + connectStr);
            context.getOptions().setConnectString(connectStr);
        } catch (URISyntaxException use) {
            // Just ignore this. If we can't parse the URI, don't attempt
            // to add any extra flags to it.
            LOG.debug("mysql: Couldn't parse connect str in checkDateTimeBehavior: "
                    + use);
        }
    }

    @Override
    public void execAndPrint(String s) {
        // Override default execAndPrint() with a special version that forces
        // use of fully-buffered ResultSets (MySQLManager uses streaming ResultSets
        // in the default execute() method; but the execAndPrint() method needs to
        // issue overlapped queries for metadata.)

        ResultSet results = null;
        try {
            results = super.execute(s, 0);
        } catch (SQLException sqlE) {
            LoggingUtils.logAll(LOG, "Error executing statement: ", sqlE);
            release();
            return;
        }

        PrintWriter pw = new PrintWriter(System.out, true);
        try {
            formatAndPrintResultSet(results, pw);
        } finally {
            pw.close();
        }
    }

    /**
     * When using a column name in a generated SQL query, how (if at all)
     * should we escape that column name? e.g., a column named "table"
     * may need to be quoted with backtiks: "`table`".
     *
     * @param colName the column name as provided by the user, etc.
     * @return how the column name should be rendered in the sql text.
     */
    public String escapeColName(String colName) {
        if (null == colName) {
            return null;
        }
        return "`" + colName + "`";
    }

    /**
     * When using a table name in a generated SQL query, how (if at all)
     * should we escape that column name? e.g., a table named "table"
     * may need to be quoted with backtiks: "`table`".
     *
     * @param tableName the table name as provided by the user, etc.
     * @return how the table name should be rendered in the sql text.
     */
    public String escapeTableName(String tableName) {
        if (null == tableName) {
            return null;
        }
        return "`" + tableName + "`";
    }

    @Override
    public boolean supportsStagingForExport() {
        return true;
    }

    @Override
    public String[] getColumnNamesForProcedure(String procedureName) {
        List<String> ret = new ArrayList<String>();
        try {
            DatabaseMetaData metaData = this.getConnection().getMetaData();
            ResultSet results = metaData.getProcedureColumns(null, null,
                    procedureName, null);
            if (null == results) {
                LOG.debug("Get Procedure Columns returns null");
                return null;
            }

            try {
                while (results.next()) {
                    if (results.getInt("COLUMN_TYPE")
                            != DatabaseMetaData.procedureColumnReturn) {
                        String name = results.getString("COLUMN_NAME");
                        ret.add(name);
                    }
                }
                String[] result = ret.toArray(new String[ret.size()]);
                LOG.debug("getColumnsNamesForProcedure returns "
                        + StringUtils.join(ret, ","));
                return result;
            } finally {
                results.close();
                getConnection().commit();
            }
        } catch (SQLException e) {
            LoggingUtils.logAll(LOG, "Error reading procedure metadata: ", e);
            throw new RuntimeException("Can't fetch column names for procedure.", e);
        }
    }

    @Override
    public Map<String, Integer> getColumnTypesForProcedure(String procedureName) {
        Map<String, Integer> ret = new TreeMap<String, Integer>();
        try {
            DatabaseMetaData metaData = this.getConnection().getMetaData();
            ResultSet results = metaData.getProcedureColumns(null, null,
                    procedureName, null);
            if (null == results) {
                LOG.debug("getColumnTypesForProcedure returns null");
                return null;
            }

            try {
                while (results.next()) {
                    if (results.getInt("COLUMN_TYPE")
                            != DatabaseMetaData.procedureColumnReturn) {
                        // we don't care if we get several rows for the
                        // same ORDINAL_POSITION (e.g. like H2 gives us)
                        // as we'll just overwrite the entry in the map:
                        ret.put(
                                results.getString("COLUMN_NAME"),
                                results.getInt("DATA_TYPE"));
                    }
                }

                LOG.debug("Columns returned = " + StringUtils.join(ret.keySet(), ","));
                LOG.debug("Types returned = " + StringUtils.join(ret.values(), ","));

                return ret.isEmpty() ? null : ret;
            } finally {
                if (results != null) {
                    results.close();
                }
                getConnection().commit();
            }
        } catch (SQLException sqlException) {
            LoggingUtils.logAll(LOG, "Error reading primary key metadata: "
                    + sqlException.toString(), sqlException);
            return null;
        }
    }

    @Override
    public Map<String, String>
    getColumnTypeNamesForProcedure(String procedureName) {
        Map<String, String> ret = new TreeMap<String, String>();
        try {
            DatabaseMetaData metaData = this.getConnection().getMetaData();
            ResultSet results = metaData.getProcedureColumns(null, null,
                    procedureName, null);
            if (null == results) {
                LOG.debug("getColumnTypesForProcedure returns null");
                return null;
            }

            try {
                while (results.next()) {
                    if (results.getInt("COLUMN_TYPE")
                            != DatabaseMetaData.procedureColumnReturn) {
                        // we don't care if we get several rows for the
                        // same ORDINAL_POSITION (e.g. like H2 gives us)
                        // as we'll just overwrite the entry in the map:
                        ret.put(
                                results.getString("COLUMN_NAME"),
                                results.getString("TYPE_NAME"));
                    }
                }

                LOG.debug("Columns returned = " + StringUtils.join(ret.keySet(), ","));
                LOG.debug(
                        "Type names returned = " + StringUtils.join(ret.values(), ","));

                return ret.isEmpty() ? null : ret;
            } finally {
                if (results != null) {
                    results.close();
                }
                getConnection().commit();
            }
        } catch (SQLException sqlException) {
            LoggingUtils.logAll(LOG, "Error reading primary key metadata: "
                    + sqlException.toString(), sqlException);
            return null;
        }
    }

    @Override
    protected String getListDatabasesQuery() {
        return "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA";
    }

    @Override
    protected String getSchemaQuery() {
        return "SELECT SCHEMA()";
    }

    private Map<String, String> colTypeNames;
    private static final int YEAR_TYPE_OVERWRITE = Types.SMALLINT;

    private int overrideSqlType(String tableName, String columnName,
                                int sqlType) {

        if (colTypeNames == null) {
            colTypeNames = getColumnTypeNames(tableName, options.getCall(),
                    options.getSqlQuery());
        }

        if ("YEAR".equalsIgnoreCase(colTypeNames.get(columnName))) {
            sqlType = YEAR_TYPE_OVERWRITE;
        }
        return sqlType;
    }

    @Override
    public String toJavaType(String tableName, String columnName, int sqlType) {
        sqlType = overrideSqlType(tableName, columnName, sqlType);
        return super.toJavaType(tableName, columnName, sqlType);
    }

    @Override
    public String toHiveType(String tableName, String columnName, int sqlType) {
        sqlType = overrideSqlType(tableName, columnName, sqlType);
        return super.toHiveType(tableName, columnName, sqlType);
    }

    @Override
    public Schema.Type toAvroType(String tableName, String columnName, int sqlType) {
        sqlType = overrideSqlType(tableName, columnName, sqlType);
        return super.toAvroType(tableName, columnName, sqlType);
    }
}

