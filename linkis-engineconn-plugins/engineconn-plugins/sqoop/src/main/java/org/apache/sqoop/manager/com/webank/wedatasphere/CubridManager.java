package org.apache.sqoop.manager.com.webank.wedatasphere;

import java.io.IOException;
import java.sql.Types;
import java.util.Map;

import com.cloudera.sqoop.hbase.HBaseUtil;
import com.cloudera.sqoop.mapreduce.*;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.avro.Schema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sqoop.accumulo.AccumuloUtil;
import org.apache.sqoop.mapreduce.AccumuloImportJob;
import org.apache.sqoop.mapreduce.HBaseBulkImportJob;
import org.apache.sqoop.mapreduce.com.webank.wedatasphere.ExportJobBase;
import org.apache.sqoop.mapreduce.cubrid.CubridUpsertOutputFormat;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.util.ExportException;
import com.cloudera.sqoop.util.ImportException;

public class CubridManager extends
        com.cloudera.sqoop.manager.CatalogQueryManager{
    public static final Log LOG = LogFactory
            .getLog(CubridManager.class.getName());

    // driver class to ensure is loaded when making db connection.
    private static final String DRIVER_CLASS =
            "cubrid.jdbc.driver.CUBRIDDriver";

    public CubridManager(final SqoopOptions opts) {
        super(DRIVER_CLASS, opts);
    }

    @Override
    public void importTable(
            com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {

        // Then run the normal importTable() method.
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
    public void exportTable(
            com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);
        ExportJobBase exportJob = new ExportJobBase(context, null, null,
                ExportBatchOutputFormat.class);

        exportJob.runExport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsertTable(
            com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);

        ExportJobBase exportJob = new ExportJobBase(context,null,null,
                CubridUpsertOutputFormat.class);
        exportJob.runExport();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void configureDbOutputColumns(SqoopOptions options) {
        // In case that we're running upsert, we do not want to
        // change column order as we're actually going to use
        // INSERT INTO ... ON DUPLICATE KEY UPDATE
        // clause.
        if (options.getUpdateMode()
                == SqoopOptions.UpdateMode.AllowInsert) {
            return;
        }

        super.configureDbOutputColumns(options);
    }

    @Override
    public String getColNamesQuery(String tableName) {
        // Use LIMIT to return fast
        return "SELECT t.* FROM " + escapeTableName(tableName)
                + " AS t LIMIT 1";
    }

    @Override
    public String getInputBoundsQuery(String splitByCol,
                                      String sanitizedQuery) {
        return "SELECT MIN(" + splitByCol + "), MAX("
                + splitByCol + ") FROM ("
                + sanitizedQuery + ") t1";
    }

    private Map<String, String> colTypeNames;
    private static final int YEAR_TYPE_OVERWRITE = Types.SMALLINT;

    private int overrideSqlType(String tableName, String columnName,
                                int sqlType) {

        if (colTypeNames == null) {
            colTypeNames = getColumnTypeNames(tableName,
                    options.getCall(),
                    options.getSqlQuery());
        }

        if ("YEAR".equalsIgnoreCase(colTypeNames.get(columnName))) {
            sqlType = YEAR_TYPE_OVERWRITE;
        }
        return sqlType;
    }

    @Override
    public String toJavaType(String tableName, String columnName,
                             int sqlType) {
        sqlType = overrideSqlType(tableName, columnName, sqlType);
        String javaType = super.toJavaType(tableName,
                columnName, sqlType);
        return javaType;
    }

    @Override
    public String toHiveType(String tableName, String columnName,
                             int sqlType) {
        sqlType = overrideSqlType(tableName, columnName, sqlType);
        return super.toHiveType(tableName, columnName, sqlType);
    }

    @Override
    public Schema.Type toAvroType(String tableName, String columnName,
                                  int sqlType) {
        sqlType = overrideSqlType(tableName, columnName, sqlType);
        return super.toAvroType(tableName, columnName, sqlType);
    }

    @Override
    protected String getListDatabasesQuery() {
        return null;
    }

    @Override
    protected String getListTablesQuery() {
        return "SELECT CLASS_NAME FROM DB_CLASS WHERE"
                + " IS_SYSTEM_CLASS = 'NO'";
    }

    @Override
    protected String getListColumnsQuery(String tableName) {
        tableName = tableName.toLowerCase();
        return "SELECT ATTR_NAME FROM DB_ATTRIBUTE WHERE"
                + " CLASS_NAME = '"
                + tableName + "'  ORDER BY def_order";
    }

    @Override
    protected String getPrimaryKeyQuery(String tableName) {
        tableName = tableName.toLowerCase();
        return "SELECT KEY_ATTR_NAME FROM DB_INDEX_KEY WHERE"
                + " CLASS_NAME = '"
                + tableName + "' ";
    }
}
