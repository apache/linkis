package org.apache.sqoop.mapreduce.com.webank.wedatasphere;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.sqoop.mapreduce.ExportCallOutputFormat;
import org.apache.sqoop.mapreduce.db.DBConfiguration;
import org.apache.sqoop.mapreduce.db.DBOutputFormat;

import com.cloudera.sqoop.manager.ConnManager;
import com.cloudera.sqoop.manager.ExportJobContext;
import com.google.common.base.Strings;


public class JdbcCallExportJob extends JdbcExportJob {
    public static final String SQOOP_EXPORT_CALL_KEY = "sqoop.export.call";

    public static final Log LOG = LogFactory.getLog(
            JdbcCallExportJob.class.getName());

    public JdbcCallExportJob(final ExportJobContext context) {
        super(context, null, null, ExportCallOutputFormat.class);
    }

    public JdbcCallExportJob(final ExportJobContext ctxt,
                             final Class<? extends Mapper> mapperClass,
                             final Class<? extends InputFormat> inputFormatClass,
                             final Class<? extends OutputFormat> outputFormatClass) {
        super(ctxt, mapperClass, inputFormatClass, outputFormatClass);
    }

    /**
     * makes sure the job knows what stored procedure to call.
     */
    @Override
    protected void propagateOptionsToJob(Job job) {
        super.propagateOptionsToJob(job);
        job.getConfiguration().set(SQOOP_EXPORT_CALL_KEY, options.getCall());
    }

    @Override
    protected void configureOutputFormat(Job job, String tableName,
                                         String tableClassName) throws IOException {
        String procedureName = job.getConfiguration().get(SQOOP_EXPORT_CALL_KEY);

        ConnManager mgr = context.getConnManager();
        try {
            if (Strings.isNullOrEmpty(options.getUsername())) {
                DBConfiguration.configureDB(job.getConfiguration(),
                        mgr.getDriverClass(),
                        options.getConnectString(),
                        options.getConnectionParams());
            } else {
                DBConfiguration.configureDB(job.getConfiguration(),
                        mgr.getDriverClass(),
                        options.getConnectString(),
                        options.getUsername(),
                        options.getPassword(),
                        options.getConnectionParams());
            }

            String [] colNames = options.getColumns();
            if (null == colNames) {
                colNames = mgr.getColumnNamesForProcedure(procedureName);
            }
            DBOutputFormat.setOutput(
                    job,
                    mgr.escapeTableName(procedureName),
                    mgr.escapeColNames(colNames));

            job.setOutputFormatClass(getOutputFormatClass());
            job.getConfiguration().set(SQOOP_EXPORT_TABLE_CLASS_KEY, tableClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new IOException("Could not load OutputFormat", cnfe);
        }
    }
}
