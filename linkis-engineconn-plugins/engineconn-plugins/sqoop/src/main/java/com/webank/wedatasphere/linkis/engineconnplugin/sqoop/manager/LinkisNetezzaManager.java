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
import com.cloudera.sqoop.mapreduce.*;
import com.cloudera.sqoop.util.ExportException;
import com.cloudera.sqoop.util.ImportException;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.sqoop.accumulo.AccumuloUtil;
import org.apache.sqoop.cli.RelatedOptions;
import org.apache.sqoop.config.ConfigurationHelper;
import org.apache.sqoop.manager.GenericJdbcManager;
import org.apache.sqoop.manager.ImportJobContext;
import org.apache.sqoop.mapreduce.AccumuloImportJob;
import org.apache.sqoop.mapreduce.HBaseBulkImportJob;
import org.apache.sqoop.mapreduce.JobBase;
import org.apache.sqoop.mapreduce.netezza.NetezzaDataDrivenDBInputFormat;

import java.io.IOException;
import java.sql.SQLException;

public class LinkisNetezzaManager extends LinkisGenericJdbcManager {

    public static final Log LOG = LogFactory.getLog(LinkisNetezzaManager.class
            .getName());

    // driver class to ensure is loaded when making db connection.
    private static final String DRIVER_CLASS = "org.netezza.Driver";

    // set to true after we warn the user that we can use direct fastpath.
    protected static boolean directModeWarningPrinted = false;

    // set to true after we warn the user that they should consider using
    // batching.
    protected static boolean batchModeWarningPrinted = false;

    public static final String NETEZZA_DATASLICE_ALIGNED_ACCESS_OPT =
            "netezza.dataslice.aligned.access";

    public static final String NETEZZA_DATASLICE_ALIGNED_ACCESS_LONG_ARG =
            "partitioned-access";

    public LinkisNetezzaManager(final SqoopOptions opts) {
        super(DRIVER_CLASS, opts);
    }


    @Override
    public String escapeColName(String colName) {
        return escapeIdentifier(colName);
    }

    @Override
    public String escapeTableName(String tableName) {
        return escapeIdentifier(tableName);
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
            this.getConnection().rollback(); // Rollback any changes
        }

        super.close();
    }

    @Override
    public void importTable(com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {
        context.setConnManager(this);
        // The user probably should have requested --direct to invoke external
        // table option.
        // Display a warning informing them of this fact.
        if (!LinkisNetezzaManager.directModeWarningPrinted) {
            LOG.warn("It looks like you are importing from Netezza.");
            LOG.warn("This transfer can be faster! Use the --direct");
            LOG.warn("option to exercise a Netezza-specific fast path.");

            LinkisNetezzaManager.directModeWarningPrinted = true; // don't display this
            // twice.
        }
        try {
            handleNetezzaImportExtraArgs(context);
        } catch (ParseException pe) {
            throw (ImportException) new ImportException(pe.getMessage(), pe);
        }
        // Then run the normal importTable() method.
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

    @Override
    public void exportTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        // The user probably should have requested --direct to invoke external
        // table option.
        // Display a warning informing them of this fact.
        context.setConnManager(this);
        if (!LinkisNetezzaManager.directModeWarningPrinted) {
            LOG.warn("It looks like you are exporting to Netezza.");
            LOG.warn("This transfer can be faster! Use the --direct");
            LOG.warn("option to exercise a Netezza-specific fast path.");

            LinkisNetezzaManager.directModeWarningPrinted = true; // don't display this
            // twice.
        }

        // Netezza does not have multi row inserts
        if (!options.isBatchMode()) {
            if (!LinkisNetezzaManager.batchModeWarningPrinted) {
                LOG.warn("It looks like you are exporting to Netezza in non-batch ");
                LOG.warn("mode.  Still this transfer can be made faster! Use the ");
                LOG.warn("--batch option to exercise a Netezza-specific fast path.");
                LOG.warn("Forcing records per statement to 1 in non batch mode");

                LinkisNetezzaManager.batchModeWarningPrinted = true; // don't display this
                // twice.
            }
            context.getOptions().getConf()
                    .setInt(AsyncSqlOutputFormat.RECORDS_PER_STATEMENT_KEY, 1);
        }
        // options.setBatchMode(true);
        // TODO Force batchmode?
        context.setConnManager(this);
        LinkisExportJobBase exportJob = new LinkisExportJobBase(context);
        exportJob.runExport();
    }

    @Override
    public void updateTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        if (options.getNumMappers() > 1) {
            String msg = "Netezza update with multiple mappers can lead to "
                    + "inconsistencies - Please set num-mappers option to 1 in the SQOOP "
                    + "command line for update jobs with Netezza and SQOOP";
            throw new ExportException(msg);
        }

        if (!options.isBatchMode()) {
            if (!LinkisNetezzaManager.batchModeWarningPrinted) {
                LOG.warn("It looks like you are exporting to Netezza in non-batch ");
                LOG.warn("mode.  Still this transfer can be made faster! Use the ");
                LOG.warn("--batch option to exercise a Netezza-specific fast path.");
                LOG.warn("Forcing records per statement to 1 in non batch mode");
                LinkisNetezzaManager.batchModeWarningPrinted = true; // don't display this
                // twice.
            }
            context.getOptions().getConf()
                    .setInt(AsyncSqlOutputFormat.RECORDS_PER_STATEMENT_KEY, 1);
        }
        super.updateTable(context);
    }

    @Override
    public boolean supportsStagingForExport() {
        return true;
    }

    @Override
    protected String getCurTimestampQuery() {
        return "SELECT CURRENT_TIMESTAMP";
    }

    protected RelatedOptions getNetezzaExtraOpts() {
        RelatedOptions netezzaOpts = new RelatedOptions("Netezza options");
        netezzaOpts.addOption(OptionBuilder
                .withArgName(NETEZZA_DATASLICE_ALIGNED_ACCESS_OPT).hasArg()
                .withDescription("Data slice aligned import")
                .withLongOpt(NETEZZA_DATASLICE_ALIGNED_ACCESS_LONG_ARG).create());
        return netezzaOpts;
    }

    private void handleNetezzaImportExtraArgs(ImportJobContext context)
            throws ParseException {

        SqoopOptions opts = context.getOptions();
        Configuration conf = opts.getConf();

        String[] extraArgs = opts.getExtraArgs();


        conf.setBoolean(NETEZZA_DATASLICE_ALIGNED_ACCESS_OPT, false);

        if (extraArgs != null && extraArgs.length > 0
                && ConfigurationHelper.getConfNumMaps(conf) > 1) {
            RelatedOptions netezzaOpts = getNetezzaExtraOpts();
            CommandLine cmdLine = new GnuParser().parse(netezzaOpts, extraArgs, true);
            if (cmdLine.hasOption(NETEZZA_DATASLICE_ALIGNED_ACCESS_LONG_ARG)) {
                conf.setBoolean(NETEZZA_DATASLICE_ALIGNED_ACCESS_OPT, true);
                context.setInputFormat(NetezzaDataDrivenDBInputFormat.class);
            }
        }

    }

}
