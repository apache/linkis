package org.apache.sqoop.manager.com.webank.wedatasphere;

import java.io.IOException;
import java.sql.SQLException;

import com.cloudera.sqoop.hbase.HBaseUtil;
import com.cloudera.sqoop.mapreduce.DataDrivenImportJob;
import com.cloudera.sqoop.mapreduce.HBaseImportJob;
import com.cloudera.sqoop.mapreduce.ImportJobBase;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.util.ImportException;
import org.apache.sqoop.accumulo.AccumuloUtil;
import org.apache.sqoop.cli.RelatedOptions;
import org.apache.sqoop.mapreduce.AccumuloImportJob;
import org.apache.sqoop.mapreduce.HBaseBulkImportJob;

public class PostgresqlManager extends CatalogQueryManager {
    public static final String SCHEMA = "schema";

    public static final Log LOG = LogFactory.getLog(
            PostgresqlManager.class.getName());

    // driver class to ensure is loaded when making db connection.
    private static final String DRIVER_CLASS = "org.postgresql.Driver";

    // set to true after we warn the user that we can use direct fastpath.
    private static boolean warningPrinted = false;

    /*
     * PostgreSQL schema that we should use.
     */
    private String schema;

    public PostgresqlManager(final SqoopOptions opts) {
        super(DRIVER_CLASS, opts);

        // Try to parse extra arguments
        try {
            parseExtraArgs(opts.getExtraArgs());
        } catch (ParseException e) {
            throw new RuntimeException("Can't parse extra arguments", e);
        }
    }

    @Override
    public String escapeColName(String colName) {
        return escapeIdentifier(colName);
    }

    @Override
    public String escapeTableName(String tableName) {
        // Return full table name including schema if needed
        if (schema != null && !schema.isEmpty()) {
            return escapeIdentifier(schema) + "." + escapeIdentifier(tableName);
        }

        return escapeIdentifier(tableName);
    }

    @Override
    public boolean escapeTableNameOnExport() {
        return true;
    }

    protected String escapeIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    @Override
    public void close() throws SQLException {
        if (this.hasOpenConnection()) {
            this.getConnection().rollback(); // rollback any pending changes.
        }

        super.close();
    }

    @Override
    protected String getColNamesQuery(String tableName) {
        // Use LIMIT to return fast
        return "SELECT t.* FROM " + escapeTableName(tableName) + " AS t LIMIT 1";
    }

    @Override
    public void importTable(
            com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {
        String tableName = context.getTableName();
        String jarFile = context.getJarFile();
        SqoopOptions opts = context.getOptions();
        context.setConnManager(this);
        ImportJobBase importer;
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
        importer.runImport(tableName, jarFile, splitCol, opts.getConf());
    }

    @Override
    public boolean supportsStagingForExport() {
        return true;
    }

    @Override
    protected String getListDatabasesQuery() {
        return
                "SELECT DATNAME FROM PG_CATALOG.PG_DATABASE";
    }

    @Override
    protected String getListTablesQuery() {
        return
                "SELECT TABLENAME FROM PG_CATALOG.PG_TABLES "
                        + "WHERE SCHEMANAME = " + getSchemaSqlFragment();
    }

    @Override
    protected String getListColumnsQuery(String tableName) {
        return
                "SELECT col.ATTNAME FROM PG_CATALOG.PG_NAMESPACE sch,"
                        + "  PG_CATALOG.PG_CLASS tab, PG_CATALOG.PG_ATTRIBUTE col "
                        + "WHERE sch.OID = tab.RELNAMESPACE "
                        + "  AND tab.OID = col.ATTRELID "
                        + "  AND sch.NSPNAME = " + getSchemaSqlFragment()
                        + "  AND tab.RELNAME = '" + escapeLiteral(tableName) + "' "
                        + "  AND col.ATTNUM >= 1"
                        + "  AND col.ATTISDROPPED = 'f' "
                        + "ORDER BY col.ATTNUM ASC";
    }

    @Override
    protected String getPrimaryKeyQuery(String tableName) {
        return
                "SELECT col.ATTNAME FROM PG_CATALOG.PG_NAMESPACE sch, "
                        + "  PG_CATALOG.PG_CLASS tab, PG_CATALOG.PG_ATTRIBUTE col, "
                        + "  PG_CATALOG.PG_INDEX ind "
                        + "WHERE sch.OID = tab.RELNAMESPACE "
                        + "  AND tab.OID = col.ATTRELID "
                        + "  AND tab.OID = ind.INDRELID "
                        + "  AND sch.NSPNAME = " + getSchemaSqlFragment()
                        + "  AND tab.RELNAME = '" + escapeLiteral(tableName) + "' "
                        + "  AND col.ATTNUM = ANY(ind.INDKEY) "
                        + "  AND ind.INDISPRIMARY";
    }

    private String getSchemaSqlFragment() {
        if (schema != null && !schema.isEmpty()) {
            return "'" + escapeLiteral(schema) + "'";
        }

        return "(SELECT CURRENT_SCHEMA())";
    }

    private String escapeLiteral(String literal) {
        return literal.replace("'", "''");
    }

    @Override
    protected String getCurTimestampQuery() {
        return "SELECT CURRENT_TIMESTAMP";
    }

    /**
     * Parse extra arguments.
     *
     * @param args Extra arguments array
     * @throws ParseException
     */
    private void parseExtraArgs(String[] args) throws ParseException {
        // No-op when no extra arguments are present
        if (args == null || args.length == 0) {
            return;
        }

        // We do not need extended abilities of SqoopParser, so we're using
        // Gnu parser instead.
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine = parser.parse(getExtraOptions(), args, true);

        // Apply parsed arguments
        applyExtraArguments(cmdLine);
    }

    protected void applyExtraArguments(CommandLine cmdLine) {
        // Apply extra options
        if (cmdLine.hasOption(SCHEMA)) {
            String schemaName = cmdLine.getOptionValue(SCHEMA);
            LOG.info("We will use schema " + schemaName);

            this.schema = schemaName;
        }
    }

    /**
     * Create related options for PostgreSQL extra parameters.
     *
     * @return
     */
    @SuppressWarnings("static-access")
    protected RelatedOptions getExtraOptions() {
        // Connection args (common)
        RelatedOptions extraOptions =
                new RelatedOptions("PostgreSQL extra options:");

        extraOptions.addOption(OptionBuilder.withArgName("string").hasArg()
                .withDescription("Optional schema name")
                .withLongOpt(SCHEMA).create());

        return extraOptions;
    }
}
