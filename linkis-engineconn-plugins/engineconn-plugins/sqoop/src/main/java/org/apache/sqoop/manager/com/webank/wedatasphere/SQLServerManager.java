package org.apache.sqoop.manager.com.webank.wedatasphere;

import com.cloudera.sqoop.hbase.HBaseUtil;
import com.cloudera.sqoop.mapreduce.HBaseImportJob;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.sqoop.accumulo.AccumuloUtil;
import org.apache.sqoop.mapreduce.*;
import org.apache.sqoop.mapreduce.com.webank.wedatasphere.JdbcExportJob;
import org.apache.sqoop.mapreduce.db.SQLServerDBInputFormat;
import org.apache.sqoop.mapreduce.db.SQLServerConnectionFailureHandler;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.util.ExportException;
import com.cloudera.sqoop.util.ImportException;

import org.apache.sqoop.cli.RelatedOptions;
import org.apache.sqoop.mapreduce.sqlserver.SqlServerExportBatchOutputFormat;
import org.apache.sqoop.mapreduce.sqlserver.SqlServerInputFormat;
import org.apache.sqoop.mapreduce.sqlserver.SqlServerUpsertOutputFormat;

import java.io.IOException;

public class SQLServerManager extends InformationSchemaManager {
    public static final String SCHEMA = "schema";
    public static final String TABLE_HINTS = "table-hints";
    public static final String TABLE_HINTS_PROP
            = "org.apache.sqoop.manager.sqlserver.table.hints";

    public static final Log LOG = LogFactory.getLog(
            SQLServerManager.class.getName());

    // Option set in extra-arguments to disable resiliency and use default mode
    public static final String NON_RESILIENT_OPTION = "non-resilient";

    // Option to allow inserts on identity columns
    public static final String IDENTITY_INSERT = "identity-insert";
    public static final String IDENTITY_INSERT_PROP =
            "org.apache.sqoop.manager.sqlserver.table.identity";

    // driver class to ensure is loaded when making db connection.
    private static final String DRIVER_CLASS =
            "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    // Define SQL Server specific types that are not covered by parent classes
    private static final int DATETIMEOFFSET = -155;

    /**
     * Schema name that we will use.
     */
    private String schema;

    /**
     * Optional table hints to use.
     */
    private String tableHints;

    /**
     * Whether to allow identity inserts.
     */
    private boolean identityInserts;

    public SQLServerManager(final SqoopOptions opts) {
        this(DRIVER_CLASS, opts);
    }

    public SQLServerManager(final String driver, final SqoopOptions opts) {
        super(driver, opts);

        // Try to parse extra arguments
        try {
            parseExtraArgs(opts.getExtraArgs());
        } catch (ParseException e) {
            throw new RuntimeException("Can't parse extra arguments", e);
        }
    }

    /**
     * Resolve MS SQL Server specific database type to the Java type that should
     * contain it.
     * @param sqlType     sql type
     * @return the name of a Java type to hold the sql datatype, or null if none.
     */
    @Override
    public String toJavaType(int sqlType) {
        String javaType;

        if (sqlType == DATETIMEOFFSET) {
            /*
             * We cannot use the TimeStamp class to represent MS SQL Server
             * datetimeoffset data type since it does not preserve time zone
             * offset values, so use String instead which would work for
             * import/export.
             */
            javaType = "String";
        }else {
            //If none of the above data types match, it returns parent method's
            //status, which can be null.
            javaType = super.toJavaType(sqlType);
        }
        return javaType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importTable(
            com.cloudera.sqoop.manager.ImportJobContext context)
            throws ImportException, IOException {
        // We're the correct connection manager
        context.setConnManager(this);

        // Propagate table hints to job
        Configuration configuration = context.getOptions().getConf();
        if (tableHints != null) {
            configuration.set(TABLE_HINTS_PROP, tableHints);
        }
        if (!isNonResilientOperation()) {
            // Enable connection recovery only if split column is provided
            SqoopOptions opts = context.getOptions();
            String splitCol = getSplitColumn(opts, context.getTableName());
            if (splitCol != null) {
                // Configure SQLServer table import jobs for connection recovery
                configureConnectionRecoveryForImport(context);
            } else {
                // Set our own input format
                context.setInputFormat(SqlServerInputFormat.class);
            }
        } else {
            context.setInputFormat(SqlServerInputFormat.class);
        }
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
            importer = new com.cloudera.sqoop.mapreduce.DataDrivenImportJob(opts, context.getInputFormat(), context);
        }

        Sqoop.jobBase = importer;
        this.checkTableImportOptions(context);
        String splitCol = this.getSplitColumn(opts, tableName);
        ((com.cloudera.sqoop.mapreduce.ImportJobBase)importer).runImport(tableName, jarFile, splitCol, opts.getConf());
    }

    /**
     * Export data stored in HDFS into a table in a database.
     */
    @Override
    public void exportTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);

        // Propagate table hints to job
        Configuration configuration = context.getOptions().getConf();
        if (tableHints != null) {
            configuration.set(TABLE_HINTS_PROP, tableHints);
        }

        // Propagate whether to allow identity inserts to job
        configuration.setBoolean(IDENTITY_INSERT_PROP, identityInserts);

        org.apache.sqoop.mapreduce.com.webank.wedatasphere.JdbcExportJob exportJob;
        if (isNonResilientOperation()) {
            exportJob = new org.apache.sqoop.mapreduce.com.webank.wedatasphere.JdbcExportJob(context, null, null,
                    SqlServerExportBatchOutputFormat.class);
        } else {
            exportJob = new JdbcExportJob(context, null, null,
                    SQLServerResilientExportOutputFormat.class);
            configureConnectionRecoveryForExport(context);
        }
        exportJob.runExport();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void updateTable(
            com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        if (isNonResilientOperation()) {
            super.updateTable(context);
        } else {
            context.setConnManager(this);
            org.apache.sqoop.mapreduce.com.webank.wedatasphere.ExportJobBase exportJob = new org.apache.sqoop.mapreduce.com.webank.wedatasphere.ExportJobBase(context, null,
                    null, SQLServerResilientUpdateOutputFormat.class);
            configureConnectionRecoveryForUpdate(context);
            exportJob.runExport();
        }
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void upsertTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);

        // Propagate table hints to job
        Configuration configuration = context.getOptions().getConf();
        if (tableHints != null) {
            configuration.set(TABLE_HINTS_PROP, tableHints);
        }

        org.apache.sqoop.mapreduce.com.webank.wedatasphere.ExportJobBase exportJob =
                new org.apache.sqoop.mapreduce.com.webank.wedatasphere.ExportJobBase(context,null,null, SqlServerUpsertOutputFormat.class);
        exportJob.runExport();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void configureDbOutputColumns(SqoopOptions options) {
        if (options.getUpdateMode() == SqoopOptions.UpdateMode.UpdateOnly) {
            super.configureDbOutputColumns(options);
        } else {
            // We're in upsert mode. We need to explicitly set
            // the database output column ordering in the codeGenerator.
            options.setDbOutputColumns(getColumnNames(options.getTableName()));
        }
    }

    /**
     * SQLServer does not support the CURRENT_TIMESTAMP() function. Instead
     * it has the notion of keyword CURRENT_TIMESTAMP that resolves to the
     * current time stamp for the database system.
     */
    @Override
    public String getCurTimestampQuery() {
        return "SELECT CURRENT_TIMESTAMP";
    }

    @Override
    protected String getListDatabasesQuery() {
        return "SELECT NAME FROM SYS.DATABASES";
    }

    @Override
    protected String getSchemaQuery() {
        if (schema == null) {
            return "SELECT SCHEMA_NAME()";
        }

        return "'" + schema + "'";
    }
    @Override
    protected String getListColumnsQuery(String tableName) {
        return
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_SCHEMA = (" + getSchemaQuery() + ") "
                        + "  AND TABLE_NAME = N'" + tableName + "' "
                        + "  ORDER BY ORDINAL_POSITION";
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
                        + "  AND tc.TABLE_NAME = N'" + tableName + "' "
                        + "  AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY'";
    }


    @Override
    public String escapeColName(String colName) {
        return escapeObjectName(colName);
    }

    @Override
    public String escapeTableName(String tableName) {
        // Return table name including schema if requested
        if (schema != null && !schema.isEmpty()) {
            return escapeObjectName(schema) + "." + escapeObjectName(tableName);
        }

        return escapeObjectName(tableName);
    }

    @Override
    public boolean escapeTableNameOnExport() {
        return true;
    }

    /**
     * Escape database object name (database, table, column, schema).
     *
     * @param objectName Object name in database
     * @return Escaped variant of the name
     */
    public String escapeObjectName(String objectName) {
        if (null == objectName) {
            return null;
        }
        return "[" + objectName + "]";
    }

    /**
     * Parse extra arguments.
     *
     * @param args Extra arguments array
     * @throws ParseException
     */
    void parseExtraArgs(String[] args) throws ParseException {
        // No-op when no extra arguments are present
        if (args == null || args.length == 0) {
            return;
        }

        // We do not need extended abilities of SqoopParser, so we're using
        // Gnu parser instead.
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine = parser.parse(getExtraOptions(), args, true);

        // Apply extra options
        if (cmdLine.hasOption(SCHEMA)) {
            String schemaName = cmdLine.getOptionValue(SCHEMA);
            LOG.info("We will use schema " + schemaName);

            this.schema = schemaName;
        }

        // Apply table hints
        if (cmdLine.hasOption(TABLE_HINTS)) {
            String hints = cmdLine.getOptionValue(TABLE_HINTS);
            LOG.info("Sqoop will use following table hints for data transfer: "
                    + hints);

            this.tableHints = hints;
        }

        identityInserts = cmdLine.hasOption(IDENTITY_INSERT);
    }

    /**
     * Create related options for SQL Server extra parameters.
     *
     * @return
     */
    @SuppressWarnings("static-access")
    private RelatedOptions getExtraOptions() {
        // Connection args (common)
        RelatedOptions extraOptions =
                new RelatedOptions("SQL Server extra options:");

        extraOptions.addOption(OptionBuilder.withArgName("string").hasArg()
                .withDescription("Optional schema name")
                .withLongOpt(SCHEMA).create());

        extraOptions.addOption(OptionBuilder.withArgName("string").hasArg()
                .withDescription("Optional table hints to use")
                .withLongOpt(TABLE_HINTS).create());

        extraOptions.addOption(OptionBuilder
                .withDescription("Allow identity inserts")
                .withLongOpt(IDENTITY_INSERT).create());

        return extraOptions;
    }

    /**
     * Launch a MapReduce job via DataDrivenImportJob to read the table with
     * SQLServerDBInputFormat which handles connection failures while
     * using free-form query importer.
     */
    public void importQuery(com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {
        if (!isNonResilientOperation()) {
            // Enable connection recovery only if split column is provided
            SqoopOptions opts = context.getOptions();
            String splitCol = getSplitColumn(opts, context.getTableName());
            if (splitCol != null) {
                // Configure SQLServer query import jobs for connection recovery
                configureConnectionRecoveryForImport(context);
            }
        }
        super.importQuery(context);
    }

    /**
     * Configure SQLServer Sqoop Jobs to recover failed connections by using
     * SQLServerConnectionFailureHandler by default.
     */
    protected void configureConnectionRecoveryForImport(
            com.cloudera.sqoop.manager.ImportJobContext context) {

        Configuration conf = context.getOptions().getConf();

        // Configure input format class
        context.setInputFormat(SQLServerDBInputFormat.class);

        // Set connection failure handler and recovery settings
        // Default settings can be overridden if provided as Configuration
        // properties by the user
        if (conf.get(SQLServerDBInputFormat.IMPORT_FAILURE_HANDLER_CLASS)
                == null) {
            conf.set(SQLServerDBInputFormat.IMPORT_FAILURE_HANDLER_CLASS,
                    SQLServerConnectionFailureHandler.class.getName());
        }
    }

    /**
     * Configure SQLServer Sqoop export Jobs to recover failed connections by
     * using SQLServerConnectionFailureHandler by default.
     */
    protected void configureConnectionRecoveryForExport(
            com.cloudera.sqoop.manager.ExportJobContext context) {

        Configuration conf = context.getOptions().getConf();

        // Set connection failure handler and recovery settings
        // Default settings can be overridden if provided as Configuration
        // properties by the user
        String clsFailureHandler = conf.get(
                SQLServerResilientExportOutputFormat.EXPORT_FAILURE_HANDLER_CLASS);
        if (clsFailureHandler == null) {
            conf.set(
                    SQLServerResilientExportOutputFormat.EXPORT_FAILURE_HANDLER_CLASS,
                    SQLServerConnectionFailureHandler.class.getName());
        }
    }

    /**
     * Configure SQLServer Sqoop Update Jobs to recover connection failures by
     * using SQLServerConnectionFailureHandler by default.
     */
    protected void configureConnectionRecoveryForUpdate(
            com.cloudera.sqoop.manager.ExportJobContext context) {

        Configuration conf = context.getOptions().getConf();

        // Set connection failure handler and recovery settings
        // Default settings can be overridden if provided as Configuration
        // properties by the user
        String clsFailureHandler = conf.get(
                SQLServerResilientExportOutputFormat.EXPORT_FAILURE_HANDLER_CLASS);
        if (clsFailureHandler == null) {
            conf.set(
                    SQLServerResilientExportOutputFormat.EXPORT_FAILURE_HANDLER_CLASS,
                    SQLServerConnectionFailureHandler.class.getName());
        }
    }

    /**
     * Check if the user has requested the operation to be non resilient.
     */
    protected boolean isNonResilientOperation() {
        String [] extraArgs = options.getExtraArgs();
        if (extraArgs != null) {
            // Traverse the extra options
            for (int iArg = 0; iArg < extraArgs.length; ++iArg) {
                String currentArg = extraArgs[iArg];
                if (currentArg.startsWith("--")
                        && currentArg.substring(2).equalsIgnoreCase(NON_RESILIENT_OPTION)) {
                    // User has explicitly requested the operation to be non-resilient
                    return true;
                }
            }
        }
        return false;
    }
}
