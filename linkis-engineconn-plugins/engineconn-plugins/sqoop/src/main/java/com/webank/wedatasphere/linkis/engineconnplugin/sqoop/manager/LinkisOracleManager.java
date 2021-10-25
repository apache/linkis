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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

import com.cloudera.sqoop.hbase.HBaseUtil;
import com.cloudera.sqoop.mapreduce.*;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sqoop.accumulo.AccumuloUtil;
import org.apache.sqoop.manager.oracle.OracleUtils;
import org.apache.sqoop.mapreduce.AccumuloImportJob;
import org.apache.sqoop.mapreduce.HBaseBulkImportJob;
import org.apache.sqoop.util.LoggingUtils;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.SqoopOptions.UpdateMode;
import com.cloudera.sqoop.mapreduce.db.OracleDataDrivenDBInputFormat;
import com.cloudera.sqoop.util.ExportException;
import com.cloudera.sqoop.util.ImportException;

public class LinkisOracleManager extends LinkisGenericJdbcManager{
    public static final Log LOG = LogFactory.getLog(
            LinkisOracleManager.class.getName());

    /**
     * ORA-00942: Table or view does not exist. Indicates that the user does
     * not have permissions.
     */
    public static final int ERROR_TABLE_OR_VIEW_DOES_NOT_EXIST = 942;

    /**
     * This is a catalog view query to list the databases. For Oracle we map the
     * concept of a database to a schema, and a schema is identified by a user.
     * In order for the catalog view DBA_USERS be visible to the user who executes
     * this query, they must have the DBA privilege.
     */
    public static final String QUERY_LIST_DATABASES =
            "SELECT USERNAME FROM DBA_USERS";

    /**
     * Query to list all tables visible to the current user. Note that this list
     * does not identify the table owners which is required in order to
     * ensure that the table can be operated on for import/export purposes.
     */
    public static final String QUERY_LIST_TABLES =
            "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ?";

    /**
     * Query to list all columns of the given table. Even if the user has the
     * privileges to access table objects from another schema, this query will
     * limit it to explore tables only from within the active schema.
     */
    public static final String QUERY_COLUMNS_FOR_TABLE =
            "SELECT COLUMN_NAME FROM ALL_TAB_COLUMNS WHERE "
                    + "OWNER = ? AND TABLE_NAME = ? ORDER BY COLUMN_ID";

    /**
     * Query to find the primary key column name for a given table. This query
     * is restricted to the current schema.
     */
    public static final String QUERY_PRIMARY_KEY_FOR_TABLE =
            "SELECT ALL_CONS_COLUMNS.COLUMN_NAME FROM ALL_CONS_COLUMNS, "
                    + "ALL_CONSTRAINTS WHERE ALL_CONS_COLUMNS.CONSTRAINT_NAME = "
                    + "ALL_CONSTRAINTS.CONSTRAINT_NAME AND "
                    + "ALL_CONSTRAINTS.CONSTRAINT_TYPE = 'P' AND "
                    + "ALL_CONS_COLUMNS.TABLE_NAME = ? AND "
                    + "ALL_CONS_COLUMNS.OWNER = ?";

    /**
     * Query to get the current user for the DB session.   Used in case of
     * wallet logins.
     */
    public static final String QUERY_GET_SESSIONUSER =
            "SELECT USER FROM DUAL";

    // driver class to ensure is loaded when making db connection.
    private static final String DRIVER_CLASS = "oracle.jdbc.OracleDriver";

    // Configuration key to use to set the session timezone.
    public static final String ORACLE_TIMEZONE_KEY = "oracle.sessionTimeZone";

    // Oracle XE does a poor job of releasing server-side resources for
    // closed connections. So we actually want to cache connections as
    // much as possible. This is especially important for JUnit tests which
    // may need to make 60 or more connections (serially), since each test
    // uses a different OracleManager instance.
    private static class ConnCache {

        public static final Log LOG = LogFactory.getLog(LinkisOracleManager.ConnCache.class.getName());

        private static class CacheKey {
            private final String connectString;
            private final String username;

            public CacheKey(String connect, String user) {
                this.connectString = connect;
                this.username = user; // note: may be null.
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof LinkisOracleManager.ConnCache.CacheKey) {
                    LinkisOracleManager.ConnCache.CacheKey k = (LinkisOracleManager.ConnCache.CacheKey) o;
                    if (null == username) {
                        return k.username == null && k.connectString.equals(connectString);
                    } else {
                        return k.username.equals(username)
                                && k.connectString.equals(connectString);
                    }
                } else {
                    return false;
                }
            }

            @Override
            public int hashCode() {
                if (null == username) {
                    return connectString.hashCode();
                } else {
                    return username.hashCode() ^ connectString.hashCode();
                }
            }

            @Override
            public String toString() {
                return connectString + "/" + username;
            }
        }

        private Map<LinkisOracleManager.ConnCache.CacheKey, Connection> connectionMap;

        public ConnCache() {
            LOG.debug("Instantiated new connection cache.");
            connectionMap = new HashMap<LinkisOracleManager.ConnCache.CacheKey, Connection>();
        }

        /**
         * @return a Connection instance that can be used to connect to the
         * given database, if a previously-opened connection is available in
         * the cache. Returns null if none is available in the map.
         */
        public synchronized Connection getConnection(String connectStr,
                                                     String username) throws SQLException {
            LinkisOracleManager.ConnCache.CacheKey key = new LinkisOracleManager.ConnCache.CacheKey(connectStr, username);
            Connection cached = connectionMap.get(key);
            if (null != cached) {
                connectionMap.remove(key);
                if (cached.isReadOnly()) {
                    // Read-only mode? Don't want it.
                    cached.close();
                }

                if (cached.isClosed()) {
                    // This connection isn't usable.
                    return null;
                }

                cached.rollback(); // Reset any transaction state.
                cached.clearWarnings();

                LOG.debug("Got cached connection for " + key);
            }

            return cached;
        }

        /**
         * Returns a connection to the cache pool for future use. If a connection
         * is already cached for the connectstring/username pair, then this
         * connection is closed and discarded.
         */
        public synchronized void recycle(String connectStr, String username,
                                         Connection conn) throws SQLException {

            LinkisOracleManager.ConnCache.CacheKey key = new LinkisOracleManager.ConnCache.CacheKey(connectStr, username);
            Connection existing = connectionMap.get(key);
            if (null != existing) {
                // Cache is already full for this entry.
                LOG.debug("Discarding additional connection for " + key);
                conn.close();
                return;
            }

            // Put it in the map for later use.
            LOG.debug("Caching released connection for " + key);
            connectionMap.put(key, conn);
        }

        @Override
        protected synchronized void finalize() throws Throwable {
            for (Connection c : connectionMap.values()) {
                c.close();
            }

            super.finalize();
        }
    }

    private static final LinkisOracleManager.ConnCache CACHE;
    static {
        CACHE = new LinkisOracleManager.ConnCache();
    }

    public LinkisOracleManager(final SqoopOptions opts) {
        super(DRIVER_CLASS, opts);
    }

    public void close() throws SQLException {
        release(); // Release any open statements associated with the connection.
        if (hasOpenConnection()) {
            // Release our open connection back to the cache.
            CACHE.recycle(options.getConnectString(), options.getUsername(),
                    getConnection());
            discardConnection(false);
        }
    }

    protected String getColNamesQuery(String tableName) {
        // SqlManager uses "tableName AS t" which doesn't work in Oracle.
        String query =  "SELECT t.* FROM " + escapeTableName(tableName)
                + " t WHERE 1=0";

        LOG.debug("Using column names query: " + query);
        return query;
    }

    /**
     * Create a connection to the database; usually used only from within
     * getConnection(), which enforces a singleton guarantee around the
     * Connection object.
     *
     * Oracle-specific driver uses READ_COMMITTED which is the weakest
     * semantics Oracle supports.
     */
    protected Connection makeConnection() throws SQLException {

        Connection connection;
        String driverClass = getDriverClass();

        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Could not load db driver class: "
                    + driverClass);
        }

        String username = options.getUsername();
        String password = options.getPassword();
        String connectStr = options.getConnectString();

        try {
            connection = CACHE.getConnection(connectStr, username);
        } catch (SQLException e) {
            connection = null;
            LOG.debug("Cached connecion has expired.");
        }

        if (null == connection) {
            // Couldn't pull one from the cache. Get a new one.
            LOG.debug("Creating a new connection for "
                    + connectStr + ", using username: " + username);
            Properties connectionParams = options.getConnectionParams();
            if (connectionParams != null && connectionParams.size() > 0) {
                LOG.debug("User specified connection params. "
                        + "Using properties specific API for making connection.");

                Properties props = new Properties();
                if (username != null) {
                    props.put("user", username);
                }

                if (password != null) {
                    props.put("password", password);
                }

                props.putAll(connectionParams);
                connection = DriverManager.getConnection(connectStr, props);
            } else {
                LOG.debug("No connection paramenters specified. "
                        + "Using regular API for making connection.");
                if (username == null) {
                    connection = DriverManager.getConnection(connectStr);
                } else {
                    connection = DriverManager.getConnection(
                            connectStr, username, password);
                }
            }
        }

        // We only use this for metadata queries. Loosest semantics are okay.
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        // Setting session time zone
        setSessionTimeZone(connection);

        // Rest of the Sqoop code expects that the connection will have be running
        // without autoCommit, so we need to explicitly set it to false. This is
        // usually done directly by SqlManager in the makeConnection method, but
        // since we are overriding it, we have to do it ourselves.
        connection.setAutoCommit(false);

        return connection;
    }

    public static String getSessionUser(Connection conn) {
        Statement stmt = null;
        ResultSet rset = null;
        String user = null;
        try {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            rset = stmt.executeQuery(QUERY_GET_SESSIONUSER);

            if (rset.next()) {
                user = rset.getString(1);
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Failed to rollback transaction", ex);
            }
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close resultset", ex);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close statement", ex);
                }
            }
        }
        if (user == null) {
            throw new RuntimeException("Unable to get current session user");
        }
        return user;
    }

    /**
     * Set session time zone.
     * @param conn      Connection object
     * @throws          SQLException instance
     */
    private void setSessionTimeZone(Connection conn) throws SQLException {
        // Need to use reflection to call the method setSessionTimeZone on the
        // OracleConnection class because oracle specific java libraries are not
        // accessible in this context.
        Method methodSession;
        Method methodDefaultTimezone;
        try {
            methodSession = conn.getClass().getMethod(
                    "setSessionTimeZone", new Class [] {String.class});
            methodDefaultTimezone = conn.getClass().getMethod("setDefaultTimeZone", TimeZone.class);
        } catch (Exception ex) {
            LOG.error("Could not find method setSessionTimeZone in "
                    + conn.getClass().getName(), ex);
            // rethrow SQLException
            throw new SQLException(ex);
        }

        // Need to set the time zone in order for Java to correctly access the
        // column "TIMESTAMP WITH LOCAL TIME ZONE".  The user may have set this in
        // the configuration as 'oracle.sessionTimeZone'.
        String clientTimeZoneStr = options.getConf().get(ORACLE_TIMEZONE_KEY,
                "GMT");
        TimeZone timeZone = TimeZone.getTimeZone(clientTimeZoneStr);
        TimeZone.setDefault(timeZone);
        try {
            methodSession.setAccessible(true);
            methodSession.invoke(conn, clientTimeZoneStr);
            methodDefaultTimezone.setAccessible(true);
            methodDefaultTimezone.invoke(conn, timeZone);
            LOG.info("Time zone has been set to " + clientTimeZoneStr);
        } catch (Exception ex) {
            LOG.warn("Time zone " + clientTimeZoneStr
                    + " could not be set on Oracle database.");
            LOG.info("Setting default time zone: GMT");
            try {
                // Per the documentation at:
                // http://download-west.oracle.com/docs/cd/B19306_01
                //     /server.102/b14225/applocaledata.htm#i637736
                // The "GMT" timezone is guaranteed to exist in the available timezone
                // regions, whereas others (e.g., "UTC") are not.
                methodSession.invoke(conn, "GMT");
                methodDefaultTimezone.invoke(conn, "GMT");
                TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            } catch (Exception ex2) {
                LOG.error("Could not set time zone for oracle connection", ex2);
                // rethrow SQLException
                throw new SQLException(ex);
            }
        }
    }

    @Override
    public void importTable(
            com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {
        context.setConnManager(this);
        // Specify the Oracle-specific DBInputFormat for import.
        context.setInputFormat(OracleDataDrivenDBInputFormat.class);
        String tableName = context.getTableName();
        String jarFile = context.getJarFile();
        SqoopOptions opts = context.getOptions();
        context.setConnManager(this);
        org.apache.sqoop.mapreduce.JobBase importer;
        if (opts.getHBaseTable() != null) {
            if (!HBaseUtil.isHBaseJarPresent()) {
                throw new ImportException("HBase jars are not present in classpath, cannot import to HBase!");
            }

            if (!opts.isBulkLoadEnabled()) {
                importer = new HBaseImportJob(opts, context);
            } else {
                importer = new HBaseBulkImportJob(opts, context);
            }
        } else if (opts.getAccumuloTable() != null) {
            if (!AccumuloUtil.isAccumuloJarPresent()) {
                throw new ImportException("Accumulo jars are not present in classpath, cannot import to Accumulo!");
            }

            importer = new AccumuloImportJob(opts, context);
        } else {
            importer = new DataDrivenImportJob(opts, context.getInputFormat(), context);
        }
        Sqoop.jobBase = importer;

        this.checkTableImportOptions(context);
        String splitCol = this.getSplitColumn(opts, tableName);
        ((ImportJobBase)importer).runImport(tableName, jarFile, splitCol, opts.getConf());
    }

    /**
     * Export data stored in HDFS into a table in a database.
     */
    public void exportTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);
        LinkisExportJobBase exportJob = new LinkisExportJobBase(context,
                null, null, ExportBatchOutputFormat.class);
        exportJob.runExport();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void upsertTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);
        JdbcUpsertExportJob exportJob =
                new JdbcUpsertExportJob(context, OracleUpsertOutputFormat.class);
        exportJob.runExport();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void configureDbOutputColumns(SqoopOptions options) {
        if (options.getUpdateMode() == UpdateMode.UpdateOnly) {
            super.configureDbOutputColumns(options);
        } else {
            // We're in upsert mode. We need to explicitly set
            // the database output column ordering in the codeGenerator.
            Set<String> updateKeys = new LinkedHashSet<String>();
            Set<String> updateKeysUppercase = new HashSet<String>();
            String updateKeyValue = options.getUpdateKeyCol();
            StringTokenizer stok = new StringTokenizer(updateKeyValue, ",");
            while (stok.hasMoreTokens()) {
                String nextUpdateColumn = stok.nextToken().trim();
                if (nextUpdateColumn.length() > 0) {
                    updateKeys.add(nextUpdateColumn);
                    updateKeysUppercase.add(nextUpdateColumn.toUpperCase());
                }  else {
                    throw new RuntimeException("Invalid update key column value specified"
                            + ": '" + updateKeyValue + "'");
                }
            }

            String [] allColNames = getColumnNames(options.getTableName());
            List<String> dbOutCols = new ArrayList<String>();
            dbOutCols.addAll(updateKeys);
            for (String col : allColNames) {
                if (!updateKeysUppercase.contains(col.toUpperCase())) {
                    dbOutCols.add(col); // add update columns to the output order list.
                }
            }
            for (String col : allColNames) {
                dbOutCols.add(col); // add insert columns to the output order list.
            }
            options.setDbOutputColumns(dbOutCols.toArray(
                    new String[dbOutCols.size()]));
        }
    }

    @Override
    public ResultSet readTable(String tableName, String[] columns)
            throws SQLException {
        if (columns == null) {
            columns = getColumnNames(tableName);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        boolean first = true;
        for (String col : columns) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(escapeColName(col));
            first = false;
        }
        sb.append(" FROM ");
        sb.append(escapeTableName(tableName));

        String sqlCmd = sb.toString();
        LOG.debug("Reading table with command: " + sqlCmd);
        return execute(sqlCmd);
    }

    private Map<String, String> columnTypeNames;

    /**
     * Resolve a database-specific type to the Java type that should contain it.
     * @param tableName   table name
     * @param colName  column name
     * @return the name of a Java type to hold the sql datatype, or null if none.
     */
    private String toDbSpecificJavaType(String tableName, String colName) {
        if (columnTypeNames == null) {
            columnTypeNames = getColumnTypeNames(tableName, options.getCall(),
                    options.getSqlQuery());
        }

        String colTypeName = columnTypeNames.get(colName);
        if (colTypeName != null) {
            if (colTypeName.equalsIgnoreCase("BINARY_FLOAT")) {
                return "Float";
            }
            if (colTypeName.equalsIgnoreCase("FLOAT")) {
                return "Float";
            }
            if (colTypeName.equalsIgnoreCase("BINARY_DOUBLE")) {
                return "Double";
            }
            if (colTypeName.equalsIgnoreCase("DOUBLE")) {
                return "Double";
            }
            if (colTypeName.toUpperCase().startsWith("TIMESTAMP")) {
                return "java.sql.Timestamp";
            }
        }
        return null;
    }

    /**
     * Resolve a database-specific type to the Hive type that should contain it.
     * @param tableName   table name
     * @param colName  column name
     * @return the name of a Hive type to hold the sql datatype, or null if none.
     */
    private String toDbSpecificHiveType(String tableName, String colName) {
        if (columnTypeNames == null) {
            columnTypeNames = getColumnTypeNames(tableName, options.getCall(),
                    options.getSqlQuery());
        }
        LOG.debug("Column Types and names returned = ("
                + StringUtils.join(columnTypeNames.keySet(), ",")
                + ")=>("
                + StringUtils.join(columnTypeNames.values(), ",")
                + ")");

        String colTypeName = columnTypeNames.get(colName);
        if (colTypeName != null) {
            if (colTypeName.equalsIgnoreCase("BINARY_FLOAT")) {
                return "FLOAT";
            }
            if (colTypeName.equalsIgnoreCase("BINARY_DOUBLE")) {
                return "DOUBLE";
            }
            if (colTypeName.toUpperCase().startsWith("TIMESTAMP")) {
                return "STRING";
            }
        }
        return null;
    }

    /**
     * Return java type for SQL type.
     * @param tableName   table name
     * @param columnName  column name
     * @param sqlType     sql type
     * @return            java type
     */
    @Override
    public String toJavaType(String tableName, String columnName, int sqlType) {
        String javaType = super.toJavaType(tableName, columnName, sqlType);
        if (javaType == null) {
            javaType = toDbSpecificJavaType(tableName, columnName);
        }
        return javaType;
    }

    /**
     * Return hive type for SQL type.
     * @param tableName   table name
     * @param columnName  column name
     * @param sqlType     sql data type
     * @return            hive type
     */
    @Override
    public String toHiveType(String tableName, String columnName, int sqlType) {
        String hiveType = super.toHiveType(tableName, columnName, sqlType);
        if (hiveType == null) {
            hiveType = toDbSpecificHiveType(tableName, columnName);
        }
        return hiveType;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    protected String getCurTimestampQuery() {
        return "SELECT SYSDATE FROM dual";
    }

    @Override
    public String timestampToQueryString(Timestamp ts) {
        return "TO_TIMESTAMP('" + ts + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
    }

    @Override
    public String datetimeToQueryString(String datetime, int columnType) {
        if (columnType == Types.TIMESTAMP) {
            return "TO_TIMESTAMP('" + datetime + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
        } else if (columnType == Types.DATE) {
            // converting timestamp of the form 2012-11-11 11:11:11.00 to
            // date of the form 2011:11:11 11:11:11
            datetime = datetime.split("\\.")[0];
            return "TO_DATE('" + datetime + "', 'YYYY-MM-DD HH24:MI:SS')";
        } else {
            String msg = "Column type is neither timestamp nor date!";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public boolean supportsStagingForExport() {
        return true;
    }

    /**
     * The concept of database in Oracle is mapped to schemas. Each schema
     * is identified by the corresponding username.
     */
    @Override
    public String[] listDatabases() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        List<String> databases = new ArrayList<String>();

        try {
            conn = getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            rset = stmt.executeQuery(QUERY_LIST_DATABASES);

            while (rset.next()) {
                databases.add(rset.getString(1));
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Failed to rollback transaction", ex);
            }

            if (e.getErrorCode() == ERROR_TABLE_OR_VIEW_DOES_NOT_EXIST) {
                LOG.error("The catalog view DBA_USERS was not found. "
                        + "This may happen if the user does not have DBA privileges. "
                        + "Please check privileges and try again.");
                LOG.debug("Full trace for ORA-00942 exception", e);
            } else {
                LoggingUtils.logAll(LOG, "Failed to list databases", e);
            }
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close resultset", ex);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close statement", ex);
                }
            }

            try {
                close();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Unable to discard connection", ex);
            }
        }

        return databases.toArray(new String[databases.size()]);
    }

    @Override
    public String[] listTables() {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rset = null;
        List<String> tables = new ArrayList<String>();
        String tableOwner = null;


        try {
            conn = getConnection();
            tableOwner = getSessionUser(conn);
            pStmt = conn.prepareStatement(QUERY_LIST_TABLES,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1, tableOwner);

            rset = pStmt.executeQuery();

            while (rset.next()) {
                tables.add(rset.getString(1));
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Failed to rollback transaction", ex);
            }
            LoggingUtils.logAll(LOG, "Failed to list tables", e);
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close resultset", ex);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close statement", ex);
                }
            }

            try {
                close();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Unable to discard connection", ex);
            }
        }

        return tables.toArray(new String[tables.size()]);
    }

    @Override
    public String[] getColumnNamesForProcedure(String procedureName) {
        List<String> ret = new ArrayList<String>();
        try {
            DatabaseMetaData metaData = this.getConnection().getMetaData();
            ResultSet results = metaData.getProcedureColumns(null, null,
                    procedureName, null);
            if (null == results) {
                return null;
            }

            try {
                while (results.next()) {
                    if (results.getInt("COLUMN_TYPE")
                            != DatabaseMetaData.procedureColumnReturn) {
                        int index = results.getInt("ORDINAL_POSITION");
                        if (index < 0) {
                            continue; // actually the return type
                        }
                        for (int i = ret.size(); i < index; ++i) {
                            ret.add(null);
                        }
                        String name = results.getString("COLUMN_NAME");
                        if (index == ret.size()) {
                            ret.add(name);
                        } else {
                            ret.set(index, name);
                        }
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
    public Map<String, Integer>
    getColumnTypesForProcedure(String procedureName) {
        Map<String, Integer> ret = new TreeMap<String, Integer>();
        try {
            DatabaseMetaData metaData = this.getConnection().getMetaData();
            ResultSet results = metaData.getProcedureColumns(null, null,
                    procedureName, null);
            if (null == results) {
                return null;
            }

            try {
                while (results.next()) {
                    if (results.getInt("COLUMN_TYPE")
                            != DatabaseMetaData.procedureColumnReturn) {
                        int index = results.getInt("ORDINAL_POSITION");
                        if (index < 0) {
                            continue; // actually the return type
                        }
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
                results.close();
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
                return null;
            }

            try {
                while (results.next()) {
                    if (results.getInt("COLUMN_TYPE")
                            != DatabaseMetaData.procedureColumnReturn) {
                        int index = results.getInt("ORDINAL_POSITION");
                        if (index < 0) {
                            continue; // actually the return type
                        }
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
                results.close();
                getConnection().commit();
            }
        } catch (SQLException sqlException) {
            LoggingUtils.logAll(LOG, "Error reading primary key metadata: "
                    + sqlException.toString(), sqlException);
            return null;
        }
    }

    @Override
    public String escapeColName(String colName) {
        return OracleUtils.escapeIdentifier(colName, options.isOracleEscapingDisabled());
    }

    @Override
    public String escapeTableName(String tableName) {
        return OracleUtils.escapeIdentifier(tableName, options.isOracleEscapingDisabled());
    }

    @Override
    public boolean escapeTableNameOnExport() {
        return true;
    }

    @Override
    public String[] getColumnNames(String tableName) {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rset = null;
        List<String> columns = new ArrayList<String>();

        String tableOwner = null;
        String shortTableName = tableName;
        int qualifierIndex = tableName.indexOf('.');
        if (qualifierIndex != -1) {
            tableOwner = tableName.substring(0, qualifierIndex);
            shortTableName = tableName.substring(qualifierIndex + 1);
        }

        try {
            conn = getConnection();

            if (tableOwner == null) {
                tableOwner = getSessionUser(conn);
            }

            pStmt = conn.prepareStatement(QUERY_COLUMNS_FOR_TABLE,
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            pStmt.setString(1, tableOwner);

            pStmt.setString(2, shortTableName);
            rset = pStmt.executeQuery();

            while (rset.next()) {
                columns.add(rset.getString(1));
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Failed to rollback transaction", ex);
            }
            LoggingUtils.logAll(LOG, "Failed to list columns", e);
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close resultset", ex);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close statement", ex);
                }
            }

            try {
                close();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Unable to discard connection", ex);
            }
        }

        return filterSpecifiedColumnNames(columns.toArray(new String[columns.size()]));
    }

    @Override
    public String getPrimaryKey(String tableName) {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rset = null;
        List<String> columns = new ArrayList<String>();

        String tableOwner = null;
        String shortTableName = tableName;
        int qualifierIndex = tableName.indexOf('.');
        if (qualifierIndex != -1) {
            tableOwner = tableName.substring(0, qualifierIndex);
            shortTableName = tableName.substring(qualifierIndex + 1);
        }

        try {
            conn = getConnection();

            if (tableOwner == null) {
                tableOwner = getSessionUser(conn);
            }

            pStmt = conn.prepareStatement(QUERY_PRIMARY_KEY_FOR_TABLE,
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pStmt.setString(1, shortTableName);
            pStmt.setString(2, tableOwner);
            rset = pStmt.executeQuery();

            while (rset.next()) {
                columns.add(rset.getString(1));
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Failed to rollback transaction", ex);
            }
            LoggingUtils.logAll(LOG, "Failed to list columns", e);
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close resultset", ex);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (SQLException ex) {
                    LoggingUtils.logAll(LOG, "Failed to close statement", ex);
                }
            }

            try {
                close();
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Unable to discard connection", ex);
            }
        }

        if (columns.size() == 0) {
            // Table has no primary key
            return null;
        }

        if (columns.size() > 1) {
            // The primary key is multi-column primary key. Warn the user.
            // TODO select the appropriate column instead of the first column based
            // on the datatype - giving preference to numerics over other types.
            LOG.warn("The table " + tableName + " "
                    + "contains a multi-column primary key. Sqoop will default to "
                    + "the column " + columns.get(0) + " only for this job.");
        }

        return columns.get(0);
    }

    @Override
    public String getInputBoundsQuery(String splitByCol, String sanitizedQuery) {
        /*
         * The default input bounds query generated by DataDrivenImportJob
         * is of the form:
         *  SELECT MIN(splitByCol), MAX(splitByCol) FROM (sanitizedQuery) AS t1
         *
         * This works for most databases but not Oracle since Oracle does not
         * allow the use of "AS" to project the subquery as a table. Instead the
         * correct format for use with Oracle is as follows:
         *  SELECT MIN(splitByCol), MAX(splitByCol) FROM (sanitizedQuery) t1
         */
        return "SELECT MIN(" + splitByCol + "), MAX(" + splitByCol + ") FROM ("
                + sanitizedQuery + ") t1";
    }
}
