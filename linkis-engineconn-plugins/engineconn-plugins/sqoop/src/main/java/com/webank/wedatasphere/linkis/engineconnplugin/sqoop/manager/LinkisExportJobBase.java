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
import com.cloudera.sqoop.config.ConfigurationHelper;
import com.cloudera.sqoop.lib.SqoopRecord;
import com.cloudera.sqoop.manager.ConnManager;
import com.cloudera.sqoop.manager.ExportJobContext;
import com.cloudera.sqoop.orm.TableClassName;
import com.cloudera.sqoop.util.ExportException;
import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.sqoop.mapreduce.*;
import org.apache.sqoop.mapreduce.hcat.SqoopHCatUtilities;
import org.apache.sqoop.util.LoggingUtils;
import org.apache.sqoop.util.PerfCounters;
import org.apache.sqoop.validation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import org.apache.sqoop.util.FileSystemUtil;

public class LinkisExportJobBase extends JobBase{
    /**
     * The (inferred) type of a file or group of files.
     */
    public enum FileType {
        SEQUENCE_FILE, AVRO_DATA_FILE, HCATALOG_MANAGED_FILE, PARQUET_FILE, UNKNOWN
    }

    public static final Log LOG = LogFactory.getLog(
            LinkisExportJobBase.class.getName());

    /** What SqoopRecord class to use to read a record for export. */
    public static final String SQOOP_EXPORT_TABLE_CLASS_KEY =
            "sqoop.mapreduce.export.table.class";

    /**
     * What column of the table to use for the WHERE clause of
     * an updating export.
     */
    public static final String SQOOP_EXPORT_UPDATE_COL_KEY =
            "sqoop.mapreduce.export.update.col";

    /** Number of map tasks to use for an export. */
    public static final String EXPORT_MAP_TASKS_KEY =
            "sqoop.mapreduce.export.map.tasks";

    /**
     *  Maximal number of attempts for map task during export
     *
     *  Sqoop will default to "1" if this property is not set regardless of what is configured directly
     *  in your hadoop configuration.
     */
    public static final String SQOOP_EXPORT_MAP_TASK_MAX_ATTEMTPS =
            "sqoop.export.mapred.map.max.attempts";

    /** Start and endtime captured for export job. */
    private long startTime;
    public static final String OPERATION = "export";

    protected ExportJobContext context;


    public LinkisExportJobBase(final ExportJobContext ctxt) {
        this(ctxt, null, null, null);
    }

    public LinkisExportJobBase(final ExportJobContext ctxt,
                         final Class<? extends Mapper> mapperClass,
                         final Class<? extends InputFormat> inputFormatClass,
                         final Class<? extends OutputFormat> outputFormatClass) {
        super(ctxt.getOptions(), mapperClass, inputFormatClass, outputFormatClass);
        this.context = ctxt;
        this.startTime = new Date().getTime();
    }

    /**
     * @return true if p is a SequenceFile, or a directory containing
     * SequenceFiles.
     */
    public static boolean isSequenceFiles(Configuration conf, Path p)
            throws IOException {
        return getFileType(conf, p) == LinkisExportJobBase.FileType.SEQUENCE_FILE;
    }

    /**
     * @return the type of the file represented by p (or the files in p, if a
     * directory)
     */
    public static LinkisExportJobBase.FileType getFileType(Configuration conf, Path p)
            throws IOException {
        FileSystem fs = p.getFileSystem(conf);

        try {
            FileStatus[] fileStatuses = fs.globStatus(p);

            if (null == fileStatuses) {
                // Couldn't get the item.
                LOG.warn("Input path " + p + " does not exist");
                return LinkisExportJobBase.FileType.UNKNOWN;
            }

            if (fileStatuses.length == 0) {
                LOG.warn("Input path " + p + " does not match any file");
                return LinkisExportJobBase.FileType.UNKNOWN;
            }

            FileStatus stat = fileStatuses[0];

            if (stat.isDir()) {
                Path dir = stat.getPath();
                FileStatus [] subitems = fs.listStatus(dir);
                if (subitems == null || subitems.length == 0) {
                    LOG.warn("Input path " + dir + " contains no files");
                    return LinkisExportJobBase.FileType.UNKNOWN; // empty dir.
                }

                // Pick a child entry to examine instead.
                boolean foundChild = false;
                for (int i = 0; i < subitems.length; i++) {
                    stat = subitems[i];
                    if (!stat.isDir() && !stat.getPath().getName().startsWith("_")) {
                        foundChild = true;
                        break; // This item is a visible file. Check it.
                    }
                }

                if (!foundChild) {
                    stat = null; // Couldn't find a reasonable candidate.
                }
            }

            if (null == stat) {
                LOG.warn("null FileStatus object in isSequenceFiles(); "
                        + "assuming false.");
                return LinkisExportJobBase.FileType.UNKNOWN;
            }

            Path target = stat.getPath();
            return fromMagicNumber(target, conf);
        } catch (FileNotFoundException fnfe) {
            LOG.warn("Input path " + p + " does not exist");
            return LinkisExportJobBase.FileType.UNKNOWN; // doesn't exist!
        }
    }

    /**
     * @param file a file to test.
     * @return true if 'file' refers to a SequenceFile.
     */
    private static LinkisExportJobBase.FileType fromMagicNumber(Path file, Configuration conf) {
        // Test target's header to see if it contains magic numbers indicating its
        // file type
        byte [] header = new byte[3];
        FSDataInputStream is = null;
        try {
            FileSystem fs = file.getFileSystem(conf);
            is = fs.open(file);
            is.readFully(header);
        } catch (IOException ioe) {
            // Error reading header or EOF; assume unknown
            LOG.warn("IOException checking input file header: " + ioe);
            return LinkisExportJobBase.FileType.UNKNOWN;
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore; closing.
                LOG.warn("IOException closing input stream: " + ioe + "; ignoring.");
            }
        }

        if (header[0] == 'S' && header[1] == 'E' && header[2] == 'Q') {
            return LinkisExportJobBase.FileType.SEQUENCE_FILE;
        }
        if (header[0] == 'O' && header[1] == 'b' && header[2] == 'j') {
            return LinkisExportJobBase.FileType.AVRO_DATA_FILE;
        }
        return LinkisExportJobBase.FileType.UNKNOWN;
    }

    /**
     * @return the Path to the files we are going to export to the db.
     */
    protected Path getInputPath() throws IOException {
        if (isHCatJob) {
            return null;
        }
        Path inputPath = new Path(context.getOptions().getExportDir());
        Configuration conf = options.getConf();
        inputPath = FileSystemUtil.makeQualified(inputPath, conf);
        return inputPath;
    }

    @Override
    protected void configureInputFormat(Job job, String tableName,
                                        String tableClassName, String splitByCol)
            throws ClassNotFoundException, IOException {

        super.configureInputFormat(job, tableName, tableClassName, splitByCol);
        if (!isHCatJob) {
            FileInputFormat.addInputPath(job, getInputPath());
        }
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClass()
            throws ClassNotFoundException {
        Class<? extends InputFormat> configuredIF = super.getInputFormatClass();
        if (null == configuredIF) {
            return ExportInputFormat.class;
        } else {
            return configuredIF;
        }
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass()
            throws ClassNotFoundException {
        Class<? extends OutputFormat> configuredOF = super.getOutputFormatClass();
        if (null == configuredOF) {
            if (!options.isBatchMode()) {
                return ExportOutputFormat.class;
            } else {
                return ExportBatchOutputFormat.class;
            }
        } else {
            return configuredOF;
        }
    }

    @Override
    protected void configureMapper(Job job, String tableName,
                                   String tableClassName) throws ClassNotFoundException, IOException {

        job.setMapperClass(getMapperClass());

        // Concurrent writes of the same records would be problematic.
        ConfigurationHelper.setJobMapSpeculativeExecution(job, false);

        job.setMapOutputKeyClass(SqoopRecord.class);
        job.setMapOutputValueClass(NullWritable.class);
    }

    @Override
    protected int configureNumMapTasks(Job job) throws IOException {
        int numMaps = super.configureNumMapTasks(job);
        job.getConfiguration().setInt(EXPORT_MAP_TASKS_KEY, numMaps);
        return numMaps;
    }

    @Override
    protected boolean runJob(Job job) throws ClassNotFoundException, IOException,
            InterruptedException {

        PerfCounters perfCounters = new PerfCounters();
        perfCounters.startClock();

        boolean success = doSubmitJob(job);
        perfCounters.stopClock();

        Counters jobCounters = job.getCounters();
        // If the job has been retired, these may be unavailable.
        if (null == jobCounters) {
            displayRetiredJobNotice(LOG);
        } else {
            perfCounters.addBytes(jobCounters.getGroup("FileSystemCounters")
                    .findCounter("HDFS_BYTES_READ").getValue());
            LOG.info("Transferred " + perfCounters.toString());
            long numRecords =  ConfigurationHelper.getNumMapInputRecords(job);
            LOG.info("Exported " + numRecords + " records.");
        }

        return success;
    }

    /**
     * Submit the Map Reduce Job.
     */
    protected boolean doSubmitJob(Job job)
            throws IOException, InterruptedException, ClassNotFoundException {
        return job.waitForCompletion(true);
    }

    /**
     * Run an export job to dump a table from HDFS to a database. If a staging
     * table is specified and the connection manager supports staging of data,
     * the export will first populate the staging table and then migrate the
     * data to the target table.
     * @throws IOException if the export job encounters an IO error
     * @throws ExportException if the job fails unexpectedly or is misconfigured.
     */
    public void runExport() throws ExportException, IOException {

        ConnManager cmgr = context.getConnManager();
        SqoopOptions options = context.getOptions();
        Configuration conf = options.getConf();

        String outputTableName = context.getTableName();
        String stagingTableName = context.getOptions().getStagingTableName();

        String tableName = outputTableName;
        boolean stagingEnabled = false;

        // Check if there are runtime error checks to do
        if (isHCatJob && options.isDirect()
                && !context.getConnManager().isDirectModeHCatSupported()) {
            throw new IOException("Direct import is not compatible with "
                    + "HCatalog operations using the connection manager "
                    + context.getConnManager().getClass().getName()
                    + ". Please remove the parameter --direct");
        }
        if (options.getAccumuloTable() != null && options.isDirect()
                && !cmgr.isDirectModeAccumuloSupported()) {
            throw new IOException("Direct mode is incompatible with "
                    + "Accumulo. Please remove the parameter --direct");
        }
        if (options.getHBaseTable() != null && options.isDirect()
                && !cmgr.isDirectModeHBaseSupported()) {
            throw new IOException("Direct mode is incompatible with "
                    + "HBase. Please remove the parameter --direct");
        }
        if (stagingTableName != null) { // user has specified the staging table
            if (cmgr.supportsStagingForExport()) {
                LOG.info("Data will be staged in the table: " + stagingTableName);
                tableName = stagingTableName;
                stagingEnabled = true;
            } else {
                throw new ExportException("The active connection manager ("
                        + cmgr.getClass().getCanonicalName()
                        + ") does not support staging of data for export. "
                        + "Please retry without specifying the --staging-table option.");
            }
        }


        String tableClassName = null;
        if (!cmgr.isORMFacilitySelfManaged()) {
            tableClassName =
                    new TableClassName(options).getClassForTable(outputTableName);
        }
        // For ORM self managed, we leave the tableClassName to null so that
        // we don't check for non-existing classes.
        String ormJarFile = context.getJarFile();

        LOG.info("Beginning export of " + outputTableName);
        loadJars(conf, ormJarFile, tableClassName);

        if (stagingEnabled) {
            // Prepare the staging table
            if (options.doClearStagingTable()) {
                try {
                    // Delete all records from staging table
                    cmgr.deleteAllRecords(stagingTableName);
                } catch (SQLException ex) {
                    throw new ExportException(
                            "Failed to empty staging table before export run", ex);
                }
            } else {
                // User has not explicitly specified the clear staging table option.
                // Assert that the staging table is empty.
                try {
                    long rowCount = cmgr.getTableRowCount(stagingTableName);
                    if (rowCount != 0L) {
                        throw new ExportException("The specified staging table ("
                                + stagingTableName + ") is not empty. To force deletion of "
                                + "its data, please retry with --clear-staging-table option.");
                    }
                } catch (SQLException ex) {
                    throw new ExportException(
                            "Failed to count data rows in staging table: "
                                    + stagingTableName, ex);
                }
            }
        }

        Job job = createJob(conf);
        try {
            // Set the external jar to use for the job.
            job.getConfiguration().set("mapred.jar", ormJarFile);
            if (options.getMapreduceJobName() != null) {
                job.setJobName(options.getMapreduceJobName());
            }

            propagateOptionsToJob(job);
            if (isHCatJob) {
                LOG.info("Configuring HCatalog for export job");
                SqoopHCatUtilities hCatUtils = SqoopHCatUtilities.instance();
                hCatUtils.configureHCat(options, job, cmgr, tableName,
                        job.getConfiguration());
            }
            configureInputFormat(job, tableName, tableClassName, null);
            configureOutputFormat(job, tableName, tableClassName);
            configureMapper(job, tableName, tableClassName);
            configureNumTasks(job);
            cacheJars(job, context.getConnManager());

            jobSetup(job);
            setJob(job);
            Sqoop.jobBase = job;
            boolean success = runJob(job);
            if (!success) {
                LOG.error("Export job failed!");
                throw new ExportException("Export job failed!");
            }

            if (options.isValidationEnabled()) {
                validateExport(tableName, conf, job);
            }

            if (isHCatJob) {
                // Publish export job data for hcat export operation
                LOG.info("Publishing HCatalog export job data to Listeners");
                PublishJobData.publishJobData(conf, options, OPERATION, tableName, startTime);
            }

        } catch (InterruptedException ie) {
            throw new IOException(ie);
        } catch (ClassNotFoundException cnfe) {
            throw new IOException(cnfe);
        } finally {
            unloadJars();
            jobTeardown(job);
        }

        // Unstage the data if needed
        if (stagingEnabled) {
            // Migrate data from staging table to the output table
            try {
                LOG.info("Starting to migrate data from staging table to destination.");
                cmgr.migrateData(stagingTableName, outputTableName);
            } catch (SQLException ex) {
                LoggingUtils.logAll(LOG, "Failed to move data from staging table ("
                        + stagingTableName + ") to target table ("
                        + outputTableName + ")", ex);
                throw new ExportException(
                        "Failed to move data from staging table", ex);
            }
        }
    }

    protected void validateExport(String tableName, Configuration conf, Job job)
            throws ExportException {
        LOG.debug("Validating exported data.");
        try {
            ValidationContext validationContext = new ValidationContext(
                    getRowCountFromHadoop(job),
                    getRowCountFromDB(context.getConnManager(), tableName));

            doValidate(options, conf, validationContext);
        } catch (ValidationException e) {
            throw new ExportException("Error validating row counts", e);
        } catch (SQLException e) {
            throw new ExportException("Error retrieving DB target row count", e);
        } catch (IOException e) {
            throw new ExportException("Error retrieving source row count", e);
        } catch (InterruptedException e) {
            throw new ExportException("Error retrieving source row count", e);
        }
    }

    /**
     * @return true if the input directory contains SequenceFiles.
     * @deprecated use {@link #getInputFileType()} instead
     */
    @Deprecated
    protected boolean inputIsSequenceFiles() {
        try {
            return isSequenceFiles(
                    context.getOptions().getConf(), getInputPath());
        } catch (IOException ioe) {
            LOG.warn("Could not check file format for export; assuming text");
            return false;
        }
    }

    protected LinkisExportJobBase.FileType getInputFileType() {
        if (isHCatJob) {
            return LinkisExportJobBase.FileType.HCATALOG_MANAGED_FILE;
        }
        try {
            return this.getFileType(context.getOptions().getConf(), getInputPath());
        } catch (IOException ioe) {
            return LinkisExportJobBase.FileType.UNKNOWN;
        }
    }

    /**
     * Open-ended "setup" routine that is called after the job is configured
     * but just before it is submitted to MapReduce. Subclasses may override
     * if necessary.
     */
    protected void jobSetup(Job job) throws IOException, ExportException {
    }

    /**
     * Open-ended "teardown" routine that is called after the job is executed.
     * Subclasses may override if necessary.
     */
    protected void jobTeardown(Job job) throws IOException, ExportException {
    }

    @Override
    protected void propagateOptionsToJob(Job job) {
        super.propagateOptionsToJob(job);
        Configuration conf = job.getConfiguration();

        // This is export job where re-trying failed mapper mostly don't make sense. By
        // default we will force MR to run only one attempt per mapper. User or connector
        // developer can override this behavior by setting SQOOP_EXPORT_MAP_TASK_MAX_ATTEMTPS:
        //
        // * Positive number - we will allow specified number of attempts
        // * Negative number - we will default to Hadoop's default number of attempts
        //
        // This is important for most connectors as they are directly committing data to
        // final table and hence re-running one mapper will lead to a misleading errors
        // of inserting duplicate rows.
        int sqoopMaxAttempts = conf.getInt(SQOOP_EXPORT_MAP_TASK_MAX_ATTEMTPS, 1);
        if (sqoopMaxAttempts > 0) {
            conf.setInt(HADOOP_MAP_TASK_MAX_ATTEMTPS, sqoopMaxAttempts);
        }
    }

    protected void doValidate(SqoopOptions options, Configuration conf, ValidationContext validationContext) throws ValidationException {
        Validator validator = (Validator) ReflectionUtils.newInstance(options.getValidatorClass(), conf);
        ValidationThreshold threshold = (ValidationThreshold)ReflectionUtils.newInstance(options.getValidationThresholdClass(), conf);
        ValidationFailureHandler failureHandler = (ValidationFailureHandler)ReflectionUtils.newInstance(options.getValidationFailureHandlerClass(), conf);
        StringBuilder sb = new StringBuilder();
        sb.append("Validating the integrity of the import using the following configuration\n");
        sb.append("\tValidator : ").append(validator.getClass().getName()).append('\n');
        sb.append("\tThreshold Specifier : ").append(threshold.getClass().getName()).append('\n');
        sb.append("\tFailure Handler : ").append(failureHandler.getClass().getName()).append('\n');
        LOG.info(sb.toString());
        validator.validate(validationContext, threshold, failureHandler);
    }

    protected long getRowCountFromHadoop(Job job) throws IOException, InterruptedException {
        return org.apache.sqoop.config.ConfigurationHelper.getNumMapOutputRecords(job);
    }
    protected long getRowCountFromDB(org.apache.sqoop.manager.ConnManager connManager, String tableName) throws SQLException {
        return connManager.getTableRowCount(tableName);
    }

}
