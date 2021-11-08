package org.apache.sqoop.manager.com.webank.wedatasphere;

import java.io.IOException;

import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.mapreduce.MySQLDumpImportJob;
import com.cloudera.sqoop.util.ImportException;
import com.cloudera.sqoop.util.ExportException;
import org.apache.sqoop.mapreduce.com.webank.wedatasphere.MySQLExportJob;

/**
 * Manages direct connections to MySQL databases
 * so we can use mysqldump to get really fast dumps.
 */
public class DirectMySQLManager
        extends MySQLManager {

    public static final Log LOG = LogFactory.getLog(
            DirectMySQLManager.class.getName());

    public DirectMySQLManager(final SqoopOptions options) {
        super(options);
    }

    /**
     * Import the table into HDFS by using mysqldump to pull out the data from
     * the database and upload the files directly to HDFS.
     */
    @Override
    public void importTable(com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {

        context.setConnManager(this);
        if (context.getOptions().getColumns() != null) {
            LOG.warn("Direct-mode import from MySQL does not support column");
            LOG.warn("selection. Falling back to JDBC-based import.");
            // Don't warn them "This could go faster..."
            MySQLManager.markWarningPrinted();
            // Use JDBC-based importTable() method.
            super.importTable(context);
            return;
        }

        String tableName = context.getTableName();
        String jarFile = context.getJarFile();
        SqoopOptions options = context.getOptions();

        MySQLDumpImportJob importer = null;
        try {
            importer = new MySQLDumpImportJob(options, context);
        } catch (ClassNotFoundException cnfe) {
            throw new IOException("Could not load required classes", cnfe);
        }

        Sqoop.jobBase = importer;
        String splitCol = getSplitColumn(options, tableName);
        if (null == splitCol && options.getNumMappers() > 1) {
            // Can't infer a primary key.
            throw new ImportException("No primary key could be found for table "
                    + tableName + ". Please specify one with --split-by or perform "
                    + "a sequential import with '-m 1'.");
        }

        LOG.info("Beginning mysqldump fast path import");

        if (options.getFileLayout() != SqoopOptions.FileLayout.TextFile) {
            // TODO(aaron): Support SequenceFile-based load-in.
            LOG.warn("File import layout " + options.getFileLayout()
                    + " is not supported by");
            LOG.warn("MySQL direct import; import will proceed as text files.");
        }

        importer.runImport(tableName, jarFile, splitCol, options.getConf());
    }

    /**
     * Export the table from HDFS by using mysqlimport to insert the data
     * back into the database.
     */
    @Override
    public void exportTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        context.setConnManager(this);
        MySQLExportJob exportJob = new MySQLExportJob(context);
        exportJob.runExport();
    }

    public void upsertTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        throw new ExportException("MySQL direct connector does not support upsert"
                + " mode. Please use JDBC based connector (remove --direct parameter)");
    }

    @Override
    public boolean supportsStagingForExport() {
        return false;
    }
}
