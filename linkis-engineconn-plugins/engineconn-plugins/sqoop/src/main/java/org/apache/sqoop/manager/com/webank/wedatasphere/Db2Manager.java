package org.apache.sqoop.manager.com.webank.wedatasphere;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cloudera.sqoop.hbase.HBaseUtil;
import com.cloudera.sqoop.mapreduce.*;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.sqoop.accumulo.AccumuloUtil;
import org.apache.sqoop.cli.RelatedOptions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sqoop.mapreduce.AccumuloImportJob;
import org.apache.sqoop.mapreduce.HBaseBulkImportJob;
import org.apache.sqoop.mapreduce.com.webank.wedatasphere.ExportJobBase;
import org.apache.sqoop.mapreduce.db.Db2DataDrivenDBInputFormat;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.util.ExportException;
import com.cloudera.sqoop.util.ImportException;
import org.apache.sqoop.util.LoggingUtils;

public class Db2Manager extends GenericJdbcManager {
    public static final Log LOG = LogFactory.getLog(
        Db2Manager.class.getName());

    // driver class to ensure is loaded when making db connection.
    private static final String DRIVER_CLASS =
            "com.ibm.db2.jcc.DB2Driver";

    private static final String XML_TO_JAVA_DATA_TYPE = "String";

    private Map<String, String> columnTypeNames;

    public static final String SCHEMA = "schema";

    /**
     *  Query to list all tables visible to the current user. Note that this list
     *  does not identify the table owners which is required in order to ensure
     *  that the table can be operated on for import/export purposes.
     */

    public static final String QUERY_LIST_SCHEMA_TABLES = "SELECT DISTINCT NAME FROM SYSIBM.SYSTABLES WHERE CREATOR =? AND TYPE='T' ";

    /**
     * Query to get the current user's schema for the DB session.   Used in case of
     * wallet logins.
     */
    public static final String QUERY_GET_USERSCHEMA =
            "select current schema from sysibm.sysdummy1";

    /**
     *  DB2 schema that we should use.
     */
    private String schema = null;

    public Db2Manager(final SqoopOptions opts) {
        super(DRIVER_CLASS, opts);

        // Try to parse extra arguments
        try {
            this.schema = parseExtraScheArgs(opts.getExtraArgs(),getExtraOptions());
        } catch (ParseException e) {
            throw new RuntimeException("Can't parse extra arguments", e);
        }
    }

    /**
     * Perform an import of a table from the database into HDFS.
     */
    @Override
    public void importTable(
            com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {
        context.setConnManager(this);
        // Specify the DB2-specific DBInputFormat for import.
        context.setInputFormat(Db2DataDrivenDBInputFormat.class);
        String tableName = context.getTableName();
        String jarFile = context.getJarFile();
        SqoopOptions opts = context.getOptions();
        context.setConnManager(this);
        JobBase importer;
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
    @Override
    public void exportTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);
        ExportJobBase exportJob = new ExportJobBase(context, null, null,
                ExportBatchOutputFormat.class);
        exportJob.runExport();
    }

    /**
     * DB2 does not support the CURRENT_TIMESTAMP() function. Instead
     * it uses the sysibm schema for timestamp lookup.
     */
    @Override
    public String getCurTimestampQuery() {
        return "SELECT CURRENT TIMESTAMP FROM SYSIBM.SYSDUMMY1 WITH UR";
    }

    @Override
    public String[] listDatabases() {
        Connection conn = null;
        ResultSet rset = null;
        List<String> databases = new ArrayList<String>();
        try {
            conn = getConnection();
            rset = conn.getMetaData().getSchemas();
            while (rset.next()) {
                // The ResultSet contains two columns - TABLE_SCHEM(1),
                // TABLE_CATALOG(2). We are only interested in TABLE_SCHEM which
                // represents schema name.
                databases.add(rset.getString(1));
            }
        } catch (SQLException sqle) {
            LoggingUtils.logAll(LOG, "Failed to list databases", sqle);
            throw new RuntimeException(sqle);
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException re) {
                    LoggingUtils.logAll(LOG, "Failed to close resultset", re);
                }
            }
        }

        return databases.toArray(new String[databases.size()]);
    }

    public static String getUserSchema(Connection conn) {
        Statement stmt = null;
        ResultSet rset = null;
        String currSchema = null;
        try {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            rset = stmt.executeQuery(QUERY_GET_USERSCHEMA);

            if (rset.next()) {
                currSchema = rset.getString(1);
            }
        } catch (SQLException e) {
            LoggingUtils.logAll(LOG, "Failed to get user schema", e);
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
        if (currSchema == null) {
            throw new RuntimeException("Unable to get current user schema");
        }
        return currSchema;
    }

    @Override
    public String[] listTables() {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rset = null;
        List<String> tables = new ArrayList<String>();
        String currUserSchema = null;

        try {
            conn = getConnection();
            currUserSchema = getUserSchema(conn);

            pStmt = conn.prepareStatement(QUERY_LIST_SCHEMA_TABLES,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);

            // if user don't provide schema in CLI
            if (schema == null) {
                pStmt.setString(1, currUserSchema);
            } else {  //user provide a schema
                pStmt.setString(1, schema);
            }

            rset = pStmt.executeQuery();

            if (schema != null && rset == null) {
                LOG.debug("schema=" + schema
                        + ",maybe not exists in current database");
            }
            while (rset.next()) {
                if(schema == null){
                    tables.add(rset.getString(1));
                }else{
                    tables.add(schema + "." + rset.getString(1));
                }
            }
        } catch (SQLException e) {
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

    /**
     * Return hive type for SQL type.
     *
     * @param tableName
     *            table name
     * @param columnName
     *            column name
     * @param sqlType
     *            sql data type
     * @return hive type
     */
    @Override
    public String toHiveType(String tableName, String columnName, int sqlType) {
        String hiveType = super.toHiveType(tableName, columnName, sqlType);
        if (hiveType == null) {
            hiveType = toDbSpecificHiveType(tableName, columnName);
        }
        return hiveType;
    }

    /**
     * Resolve a database-specific type to the Hive type that should contain it.
     *
     * @param tableName
     *            table name
     * @param colName
     *            column name
     * @return the name of a Hive type to hold the sql datatype, or null if
     *         none.
     */
    private String toDbSpecificHiveType(String tableName, String colName) {
        if (columnTypeNames == null) {
            columnTypeNames = getColumnTypeNames(tableName, options.getCall(),
                    options.getSqlQuery());
        }
        LOG.debug("database-specific Column Types and names returned = ("
                + StringUtils.join(columnTypeNames.keySet(), ",") + ")=>("
                + StringUtils.join(columnTypeNames.values(), ",") + ")");

        String colTypeName = columnTypeNames.get(colName);

        if (colTypeName != null) {
            if (colTypeName.toUpperCase().startsWith("XML")) {
                return XML_TO_JAVA_DATA_TYPE;
            }
        }
        return null;
    }

    /**
     * Return java type for SQL type.
     *
     * @param tableName
     *            table name
     * @param columnName
     *            column name
     * @param sqlType
     *            sql type
     * @return java type
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
     * Resolve a database-specific type to the Java type that should contain it.
     *
     * @param tableName
     *            table name
     * @param colName
     *            column name
     * @return the name of a Java type to hold the sql datatype, or null if
     *         none.
     */
    private String toDbSpecificJavaType(String tableName, String colName) {
        if (columnTypeNames == null) {
            columnTypeNames = getColumnTypeNames(tableName, options.getCall(),
                    options.getSqlQuery());
        }
        String colTypeName = columnTypeNames.get(colName);
        if (colTypeName != null) {
            if (colTypeName.equalsIgnoreCase("XML")) {
                return XML_TO_JAVA_DATA_TYPE;
            }
        }
        return null;
    }

    /**
     * Create related options for PostgreSQL extra parameters.
     * @return
     */
    @SuppressWarnings("static-access")
    protected RelatedOptions getExtraOptions() {
        // Connection args (common)
        RelatedOptions extraOptions = new RelatedOptions("DB2 extra options:");
        extraOptions.addOption(OptionBuilder.withArgName("string").hasArg()
                .withDescription("Optional schema name").withLongOpt(SCHEMA)
                .create("schema"));
        return extraOptions;
    }
}
