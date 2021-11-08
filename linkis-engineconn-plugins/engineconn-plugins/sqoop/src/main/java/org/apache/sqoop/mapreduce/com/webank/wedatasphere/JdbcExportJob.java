package org.apache.sqoop.mapreduce.com.webank.wedatasphere;

import com.cloudera.sqoop.manager.ConnManager;
import com.cloudera.sqoop.manager.ExportJobContext;
import com.cloudera.sqoop.mapreduce.db.DBConfiguration;
import com.cloudera.sqoop.mapreduce.db.DBOutputFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.DefaultStringifier;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.sqoop.mapreduce.*;
import org.apache.sqoop.mapreduce.hcat.SqoopHCatUtilities;

import java.io.IOException;
import java.util.Map;
import org.apache.sqoop.util.FileSystemUtil;

public class JdbcExportJob extends ExportJobBase {
    private FileType fileType;

    public static final Log LOG = LogFactory.getLog(
            JdbcExportJob.class.getName());

    public JdbcExportJob(final ExportJobContext context) {
        super(context);
    }

    public JdbcExportJob(final ExportJobContext ctxt,
                         final Class<? extends Mapper> mapperClass,
                         final Class<? extends InputFormat> inputFormatClass,
                         final Class<? extends OutputFormat> outputFormatClass) {
        super(ctxt, mapperClass, inputFormatClass, outputFormatClass);
    }

    @Override
    protected void configureInputFormat(Job job, String tableName,
                                        String tableClassName, String splitByCol)
            throws ClassNotFoundException, IOException {

        fileType = getInputFileType();

        super.configureInputFormat(job, tableName, tableClassName, splitByCol);

        if (isHCatJob) {
            SqoopHCatUtilities.configureExportInputFormat(options, job,
                    context.getConnManager(), tableName, job.getConfiguration());
            return;
        } else if (fileType == FileType.AVRO_DATA_FILE) {
            LOG.debug("Configuring for Avro export");
            configureGenericRecordExportInputFormat(job, tableName);
        } else if (fileType == FileType.PARQUET_FILE) {
            LOG.debug("Configuring for Parquet export");
            configureGenericRecordExportInputFormat(job, tableName);
            String uri = "dataset:" + FileSystemUtil.makeQualified(getInputPath(), job.getConfiguration());

        }
    }

    private void configureGenericRecordExportInputFormat(Job job, String tableName)
            throws IOException {
        ConnManager connManager = context.getConnManager();
        Map<String, Integer> columnTypeInts;
        if (options.getCall() == null) {
            columnTypeInts = connManager.getColumnTypes(
                    tableName,
                    options.getSqlQuery());
        } else {
            columnTypeInts = connManager.getColumnTypesForProcedure(
                    options.getCall());
        }
        String[] specifiedColumns = options.getColumns();
        MapWritable columnTypes = new MapWritable();
        for (Map.Entry<String, Integer> e : columnTypeInts.entrySet()) {
            String column = e.getKey();
            column = (specifiedColumns == null) ? column : options.getColumnNameCaseInsensitive(column);
            if (column != null) {
                Text columnName = new Text(column);
                Text columnType = new Text(connManager.toJavaType(tableName, column, e.getValue()));
                columnTypes.put(columnName, columnType);
            }
        }
        DefaultStringifier.store(job.getConfiguration(), columnTypes,
                AvroExportMapper.AVRO_COLUMN_TYPES_MAP);
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClass()
            throws ClassNotFoundException {
        if (isHCatJob) {
            return SqoopHCatUtilities.getInputFormatClass();
        }
        switch (fileType) {
            case AVRO_DATA_FILE:
                return AvroInputFormat.class;
            case PARQUET_FILE:
                return null;
            default:
                return super.getInputFormatClass();
        }
    }

    @Override
    protected Class<? extends Mapper> getMapperClass() {
        if (isHCatJob) {
            return SqoopHCatUtilities.getExportMapperClass();
        }
        switch (fileType) {
            case SEQUENCE_FILE:
                return SequenceFileExportMapper.class;
            case AVRO_DATA_FILE:
                return AvroExportMapper.class;
            case PARQUET_FILE:
                return ParquetExportMapper.class;
            case UNKNOWN:
            default:
                return TextExportMapper.class;
        }
    }

    @Override
    protected void configureOutputFormat(Job job, String tableName,
                                         String tableClassName) throws IOException {

        ConnManager mgr = context.getConnManager();
        try {
            String username = options.getUsername();
            if (null == username || username.length() == 0) {
                DBConfiguration.configureDB(job.getConfiguration(),
                        mgr.getDriverClass(),
                        options.getConnectString(),
                        options.getConnectionParams());
            } else {
                DBConfiguration.configureDB(job.getConfiguration(),
                        mgr.getDriverClass(),
                        options.getConnectString(),
                        username, options.getPassword(),
                        options.getConnectionParams());
            }

            String [] colNames = options.getColumns();
            if (null == colNames) {
                colNames = mgr.getColumnNames(tableName);
            }

            if (mgr.escapeTableNameOnExport()) {
                DBOutputFormat.setOutput(job, mgr.escapeTableName(tableName), mgr.escapeColNames(colNames));
            } else {
                DBOutputFormat.setOutput(job, tableName, mgr.escapeColNames(colNames));
            }

            job.setOutputFormatClass(getOutputFormatClass());
            job.getConfiguration().set(SQOOP_EXPORT_TABLE_CLASS_KEY, tableClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new IOException("Could not load OutputFormat", cnfe);
        }
    }
}
