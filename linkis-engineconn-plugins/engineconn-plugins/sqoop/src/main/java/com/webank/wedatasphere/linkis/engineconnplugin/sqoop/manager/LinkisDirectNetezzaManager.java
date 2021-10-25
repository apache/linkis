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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.sqoop.mapreduce.netezza.NetezzaExternalTableImportJob;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.cli.RelatedOptions;
import com.cloudera.sqoop.util.ExportException;
import com.cloudera.sqoop.util.ImportException;

public class LinkisDirectNetezzaManager extends LinkisNetezzaManager{
    public static final Log LOG = LogFactory.getLog(LinkisDirectNetezzaManager.class
            .getName());

    public static final String NETEZZA_LOG_DIR_OPT = "netezza.log.dir";
    public static final String NETEZZA_LOG_DIR_LONG_ARG = "log-dir";

    public static final String NETEZZA_ERROR_THRESHOLD_OPT =
            "netezza.error.threshold";
    public static final String NETEZZA_ERROR_THRESHOLD_LONG_ARG =
            "max-errors";

    public static final String NETEZZA_CTRL_CHARS_OPT =
            "netezza.ctrl.chars";
    public static final String NETEZZA_CTRL_CHARS_LONG_ARG =
            "ctrl-chars";


    public static final String NETEZZA_CRIN_STRING_OPT =
            "netezza.crin.string";
    public static final String NETEZZA_CRIN_STRING_LONG_ARG =
            "crin-string";


    public static final String NETEZZA_IGNORE_ZERO_OPT =
            "netezza.ignore.zero";
    public static final String NETEZZA_IGNORE_ZERO_LONG_ARG =
            "ignore-zero";

    public static final String NETEZZA_TRUNC_STRING_OPT =
            "netezza.trunc.string";
    public static final String NETEZZA_TRUNC_STRING_LONG_ARG =
            "trunc-string";

    public static final String NETEZZA_TABLE_ENCODING_OPT =
            "netezza.table.encoding";
    public static final String NETEZZA_TABLE_ENCODING_LONG_ARG =
            "encoding";


    private static final String QUERY_CHECK_DICTIONARY_FOR_TABLE =
            "SELECT 1 FROM _V_TABLE WHERE OWNER= ? "
                    + " AND TABLENAME = ?";
    public static final String NETEZZA_NULL_VALUE =
            "netezza.exttable.null.value";

    public LinkisDirectNetezzaManager(SqoopOptions opts) {
        super(opts);
        try {
            handleNetezzaExtraArgs(options);
        } catch (ParseException pe) {
            throw  new RuntimeException(pe.getMessage(), pe);
        }
    }

    private void checkNullValueStrings(String nullStrValue,
                                       String nullNonStrValue) throws IOException {

        if (!StringUtils.equals(nullStrValue, nullNonStrValue)) {
            throw new IOException(
                    "Detected different values of --input-string and --input-non-string "
                            + "parameters. Netezza direct manager does not support that. Please "
                            + "either use the same values or omit the --direct parameter.");
        }


        // Null String values cannot be more 4 chars in length in the case
        // Netezza external tables.

        if (nullStrValue != null)  {
            nullStrValue = StringEscapeUtils.unescapeJava(nullStrValue);
            if (nullStrValue.length() > 4) {
                throw new IOException(
                        "Null string (and null non string) values for Netezza direct mode"
                                + " manager must be less than 4 characters in length");
            }
            options.getConf().set(NETEZZA_NULL_VALUE, nullStrValue);
        }
    }

    /**
     * Check Table if it is valid for export. Parse the table like what we do in
     * Oracle manager
     *
     * @throws IOException
     * @throws ExportException
     */
    private void checkTable() throws IOException, ExportException {
        String tableOwner = this.options.getUsername();
        String tableName = this.options.getTableName();
        String shortTableName = tableName;
        int qualifierIndex = tableName.indexOf('.');
        if (qualifierIndex != -1) {
            tableOwner = tableName.substring(0, qualifierIndex);
            shortTableName = tableName.substring(qualifierIndex + 1);
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            try {
                conn = getConnection();
                ps = conn.prepareStatement(QUERY_CHECK_DICTIONARY_FOR_TABLE,
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, tableOwner);
                ps.setString(2, shortTableName);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    String message = tableName
                            + " is not a valid Netezza table.  "
                            + "Please make sure that you have connected to the Netezza DB "
                            + "and the table name is right.   The current values are\n\t"
                            + "  connection string : " + options.getConnectString()
                            + "\n\t  table owner : " + tableOwner + "\n\t  table name : "
                            + shortTableName;
                    LOG.error(message);
                    throw new IOException(message);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                close();
            }
        } catch (SQLException sqle) {
            throw new IOException("SQL exception checking table "
                    + sqle.getMessage(), sqle);
        }
    }

    /**
     * Export data stored in HDFS into a table in a database.
     */
    public void exportTable(com.cloudera.sqoop.manager.ExportJobContext context)
            throws IOException, ExportException {
        options = context.getOptions();
        context.setConnManager(this);

        checkTable(); // Throws exception as necessary
        LinkisExportJobBase exporter = null;

        char qc = (char) options.getInputEnclosedBy();
        char ec = (char) options.getInputEscapedBy();
        checkNullValueStrings(options.getInNullStringValue(),
                options.getInNullNonStringValue());

        if (qc > 0 && !(qc == '"' || qc == '\'')) {
            throw new ExportException("Input enclosed-by character must be '\"' "
                    + "or ''' for netezza direct mode exports");
        }
        if (ec > 0 && ec != '\\') {
            throw new ExportException("Input escaped-by character must be '\\' "
                    + "for netezza direct mode exports");
        }
        exporter = new LinkisExportJobBase(context,(Class)null, (Class)null, NullOutputFormat.class);
        exporter.runExport();
    }

    /**
     * Import the table into HDFS by using Netezza external tables to pull out the
     * data from the database and upload the files directly to HDFS.
     */
    @Override
    public void importTable(com.cloudera.sqoop.manager.ImportJobContext context)
            throws IOException, ImportException {

        context.setConnManager(this);

        String tableName = context.getTableName();
        String jarFile = context.getJarFile();
        SqoopOptions options = context.getOptions();

        if (null == tableName) {
            LOG.
                    error("Netezza external table import does not support query imports.");
            LOG.
                    error("Do not use --direct and --query together for Netezza.");
            throw
                    new IOException("Null tableName for Netezza external table import.");
        }

        checkNullValueStrings(options.getNullStringValue(),
                options.getNullNonStringValue());

        char qc = options.getOutputEnclosedBy();
        char ec = options.getOutputEscapedBy();

        if (qc > 0 && !(qc == '"' || qc == '\'')) {
            throw new ImportException("Output enclosed-by character must be '\"' "
                    + "or ''' for netezza direct mode imports");
        }
        if (ec > 0 && ec != '\\') {
            throw new ImportException("Output escaped-by character must be '\\' "
                    + "for netezza direct mode exports");
        }

        NetezzaExternalTableImportJob importer = null;

        importer = new NetezzaExternalTableImportJob(options, context);

        // Direct Netezza Manager will use the datasliceid so no split columns
        // will be used.

        LOG.info("Beginning netezza fast path import");

        if (options.getFileLayout() != SqoopOptions.FileLayout.TextFile) {
            LOG.warn("File import layout " + options.getFileLayout()
                    + " is not supported by");
            LOG.warn("Netezza direct import; import will proceed as text files.");
        }

        Sqoop.jobBase = importer;
        importer.runImport(tableName, jarFile, null, options.getConf());
    }

    protected  RelatedOptions getNetezzaExtraOpts() {
        // Just add the options from NetezzaManager and ignore the setting
        // for direct mode access
        RelatedOptions netezzaOpts =
                new RelatedOptions("Netezza Connector Direct mode options");

        netezzaOpts.addOption(OptionBuilder
                .withArgName(NETEZZA_ERROR_THRESHOLD_OPT).hasArg()
                .withDescription("Error threshold for the job")
                .withLongOpt(NETEZZA_ERROR_THRESHOLD_LONG_ARG).create());
        netezzaOpts.addOption(OptionBuilder.withArgName(NETEZZA_LOG_DIR_OPT)
                .hasArg().withDescription("Netezza log directory")
                .withLongOpt(NETEZZA_LOG_DIR_LONG_ARG).create());
        netezzaOpts.addOption(OptionBuilder.withArgName(NETEZZA_CTRL_CHARS_OPT)
                .withDescription("Allow control chars in data")
                .withLongOpt(NETEZZA_CTRL_CHARS_LONG_ARG).create());
        netezzaOpts.addOption(OptionBuilder.withArgName(NETEZZA_TRUNC_STRING_OPT)
                .withDescription("Truncate string to declared storage size")
                .withLongOpt(NETEZZA_TRUNC_STRING_LONG_ARG).create());
        netezzaOpts.addOption(OptionBuilder.withArgName(NETEZZA_CRIN_STRING_OPT)
                .withDescription("Truncate string to declared storage size")
                .withLongOpt(NETEZZA_CRIN_STRING_LONG_ARG).create());
        netezzaOpts.addOption(OptionBuilder.withArgName(NETEZZA_IGNORE_ZERO_OPT)
                .withDescription("Truncate string to declared storage size")
                .withLongOpt(NETEZZA_IGNORE_ZERO_LONG_ARG).create());
        netezzaOpts.addOption(OptionBuilder.withArgName(NETEZZA_TABLE_ENCODING_OPT)
                .hasArg().withDescription("Table encoding")
                .withLongOpt(NETEZZA_TABLE_ENCODING_LONG_ARG).create());
        return netezzaOpts;
    }

    private void handleNetezzaExtraArgs(SqoopOptions opts)
            throws ParseException {

        Configuration conf = opts.getConf();

        String[] extraArgs = opts.getExtraArgs();

        RelatedOptions netezzaOpts = getNetezzaExtraOpts();
        CommandLine cmdLine = new GnuParser().parse(netezzaOpts, extraArgs, true);
        if (cmdLine.hasOption(NETEZZA_ERROR_THRESHOLD_LONG_ARG)) {
            int threshold = Integer.parseInt(cmdLine
                    .getOptionValue(NETEZZA_ERROR_THRESHOLD_LONG_ARG));
            conf.setInt(NETEZZA_ERROR_THRESHOLD_OPT, threshold);
        }
        if (cmdLine.hasOption(NETEZZA_LOG_DIR_LONG_ARG)) {
            String dir = cmdLine.getOptionValue(NETEZZA_LOG_DIR_LONG_ARG);
            conf.set(NETEZZA_LOG_DIR_OPT, dir);
        }
        if (cmdLine.hasOption(NETEZZA_TABLE_ENCODING_LONG_ARG)) {
            String encoding = cmdLine
                    .getOptionValue(NETEZZA_TABLE_ENCODING_LONG_ARG);
            conf.set(NETEZZA_TABLE_ENCODING_OPT, encoding);
        }

        conf.setBoolean(NETEZZA_CTRL_CHARS_OPT,
                cmdLine.hasOption(NETEZZA_CTRL_CHARS_LONG_ARG));

        conf.setBoolean(NETEZZA_TRUNC_STRING_OPT,
                cmdLine.hasOption(NETEZZA_TRUNC_STRING_LONG_ARG));

        conf.setBoolean(NETEZZA_CRIN_STRING_OPT,
                cmdLine.hasOption(NETEZZA_CRIN_STRING_LONG_ARG));

        conf.setBoolean(NETEZZA_IGNORE_ZERO_OPT,
                cmdLine.hasOption(NETEZZA_IGNORE_ZERO_LONG_ARG));

        // Always true for Netezza direct mode access
        conf.setBoolean(NETEZZA_DATASLICE_ALIGNED_ACCESS_OPT, true);
    }

    @Override
    public boolean supportsStagingForExport() {
        return false;
    }

    @Override
    public boolean isORMFacilitySelfManaged() {
        if (options.getHCatTableName() != null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isDirectModeHCatSupported() {
        return true;
    }


    public static String getLocalLogDir(TaskAttemptID attemptId) {
        int tid = attemptId.getTaskID().getId();
        int aid = attemptId.getId();
        String jid = attemptId.getJobID().toString();
        StringBuilder sb = new StringBuilder(jid).append('-');
        sb.append(tid).append('-').append(aid);
        String localLogDir = sb.toString();
        return localLogDir;
    }
}
