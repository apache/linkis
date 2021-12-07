package com.alibaba.datax.plugin.reader.hdfsreader;

import com.alibaba.datax.common.element.*;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.plugin.TaskPluginCollector;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.LdapUtil;
import com.alibaba.datax.plugin.reader.hdfsreader.hfile.HFileParser;
import com.alibaba.datax.plugin.reader.hdfsreader.hfile.HFileParserFactory;
import com.alibaba.datax.plugin.unstructuredstorage.reader.ColumnEntry;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderUtil;
import com.alibaba.datax.plugin.utils.HdfsUserGroupInfoLock;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.wedatasphere.linkis.datax.common.CryptoUtils;
import com.webank.wedatasphere.linkis.datax.common.ldap.LdapConnector;
import com.webank.wedatasphere.linkis.datax.core.job.meta.MetaSchema;
import com.webank.wedatasphere.linkis.datax.util.KerberosUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.ql.io.*;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.orc.OrcInputFormat;
import org.apache.hadoop.hive.ql.io.orc.OrcSerde;
import org.apache.hadoop.hive.ql.io.orc.Reader;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.hive.serde2.columnar.BytesRefWritable;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.alibaba.datax.plugin.reader.hdfsreader.Key.*;
import static org.apache.hadoop.fs.FileSystem.FS_DEFAULT_NAME_KEY;

/**
 * Created by mingya.wmy on 2015/8/12.
 */
public class HdfsReaderUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HdfsReader.Job.class);

    private org.apache.hadoop.conf.Configuration hadoopConf;
    private Configuration readerConfig;
    private String specifiedFileType = null;
    private FileSystem fileSystem = null;
    private UserGroupInformation ugi = null;

    private static final int DIRECTORY_SIZE_GUESS = 16 * 1024;
    private static final String HDFS_DEFAULT_FS_KEY = "fs.defaultFS";
    private static final String FS_DISABLE_CACHE = "fs.%s.impl.disable.cache";
    private static final String FS_AUTOMATIC_CLOSE_KEY = "fs.automatic.close";
    private static final String FALLBACK_TO_SIMPLE_AUTH_KEY = "ipc.client.fallback-to-simple-auth-allowed";
    private static final String HADOOP_SECURITY_AUTHENTICATION_KEY = "hadoop.security.authentication";
    private static final String EXEC_USER = "exec.user";
    /**
     * hive parameters
     */
    private static final String DEFAULT_HIVE_USER = "hadoop";

    /**
     * kerberos parameters
     */
    private Boolean haveKerberos;
    private String kerberosKeytabFilePath;
    private String kerberosPrincipal;

    UserGroupInformation getUgi(){
        return ugi;
    }

    HdfsReaderUtil(Configuration readerConfig) {
        //to store task configuration
        this.readerConfig = readerConfig;
        hadoopConf = new org.apache.hadoop.conf.Configuration();
        //http://blog.csdn.net/yangjl38/article/details/7583374
        Configuration hadoopSiteParams = readerConfig.getConfiguration(HADOOP_CONFIG);
        JSONObject hadoopSiteParamsAsJsonObject = JSON.parseObject(readerConfig.getString(HADOOP_CONFIG));
        if (null != hadoopSiteParams) {
            Set<String> paramKeys = hadoopSiteParams.getKeys();
            for (String each : paramKeys) {
                hadoopConf.set(each, hadoopSiteParamsAsJsonObject.getString(each));
            }
        }
        hadoopConf.set(HDFS_DEFAULT_FS_KEY, readerConfig.getString(DEFAULT_FS));
        //disable automatic close
        hadoopConf.setBoolean(FS_AUTOMATIC_CLOSE_KEY, false);
        //if has Kerberos authentication
        this.haveKerberos = readerConfig.getBool(HAVE_KERBEROS, false);
        HdfsUserGroupInfoLock.lock();
        try {
            if (haveKerberos) {
                this.kerberosKeytabFilePath = readerConfig.getString(KERBEROS_KEYTAB_FILE_PATH);
                this.kerberosPrincipal = readerConfig.getString(KERBEROS_PRINCIPAL);
                this.hadoopConf.set(HADOOP_SECURITY_AUTHENTICATION_KEY, "kerberos");
                //disable the cache
                this.hadoopConf.setBoolean(
                        String.format(FS_DISABLE_CACHE, URI.create(this.hadoopConf.get(FS_DEFAULT_NAME_KEY, "")).getScheme()), true);
                hadoopConf.setBoolean(FALLBACK_TO_SIMPLE_AUTH_KEY, true);
                ugi = this.kerberosAuthentication(this.kerberosPrincipal, this.kerberosKeytabFilePath);
            } else {
                ugi = this.getUgiInAuth(readerConfig);
            }
            try {
                fileSystem = null == ugi? null : ugi.doAs((PrivilegedExceptionAction<FileSystem>) () -> {
                    FileSystem fs = null;
                    try {
                        fs = FileSystem.get(hadoopConf);
                        fs.exists(new Path("/"));
                    } catch (IOException e) {
                        String message = String.format("获取FileSystem时发生网络IO异常,请检查您的网络是否正常!HDFS地址：[%s]",
                                "message:defaultFS =" + readerConfig.getString(DEFAULT_FS));
                        LOG.error(message);
                        throw DataXException.asDataXException(HdfsReaderErrorCode.CONNECT_HDFS_IO_ERROR, e);
                    } catch (Exception e) {
                        String message = String.format("获取FileSystem失败,请检查HDFS地址是否正确: [%s]",
                                "message:defaultFS =" + hadoopConf);
                        LOG.error(message);
                        throw DataXException.asDataXException(HdfsReaderErrorCode.CONNECT_HDFS_IO_ERROR, e);
                    }
                    return fs;
                });
            } catch (Exception e) {
                throw DataXException.asDataXException(HdfsReaderErrorCode.CONNECT_HDFS_IO_ERROR, e);
            }
        }finally{
            HdfsUserGroupInfoLock.unlock();
        }
        if (null == fileSystem || null == hadoopConf) {
            String message = String.format("获取FileSystem失败,请检查HDFS地址是否正确: [%s]",
                    "message:defaultFS =" + readerConfig.getString(DEFAULT_FS));
            LOG.error(message);
            throw DataXException.asDataXException(HdfsReaderErrorCode.CONNECT_HDFS_IO_ERROR, message);
        }

        LOG.info(String.format("hadoopConfig details:%s", JSON.toJSONString(this.hadoopConf)));
    }

    /**
     * fetch meta information
     * @param database database name
     * @param table  table name
     * @param hiveMetaStoreUris hiveMetaStore uris
     * @return meta schema
     */
    MetaSchema getHiveMetadata(String database, String table, String hiveMetaStoreUris) {
        MetaSchema metaSchema = new MetaSchema();
        try {
            IMetaStoreClient client = null;
            try {
                client = getHiveConnByUris(hiveMetaStoreUris).getMSC();
                Table tableInfo = client.getTable(database, table);
                StorageDescriptor storageDescriptor = tableInfo.getSd();
                metaSchema.addSchemaInfo(HIVE_META_SERDE_INFO, storageDescriptor.getSerdeInfo());
                metaSchema.addSchemaInfo(HIVE_META_NUM_BUCKETS, storageDescriptor.getNumBuckets());
                metaSchema.addSchemaInfo(HIVE_META_BUCKET_COLS, storageDescriptor.getBucketCols());
                metaSchema.addSchemaInfo(HIVE_META_SORT_COLS, storageDescriptor.getSortCols());
                metaSchema.addSchemaInfo(HIVE_META_INPUT_FORMAT, storageDescriptor.getInputFormat());
                metaSchema.addSchemaInfo(HIVE_META_OUTPUT_FORMAT, storageDescriptor.getOutputFormat());
                metaSchema.addSchemaInfo(HIVE_META_PARAMETERS, storageDescriptor.getParameters());
                metaSchema.addSchemaInfo(HIVE_META_COMPRESSED, storageDescriptor.isCompressed());
                // get the field schema list from storage descriptor
                List<MetaSchema.FieldSchema> fieldSchemas = new ArrayList<>();
                List<org.apache.hadoop.hive.metastore.api.FieldSchema> fields =
                    storageDescriptor.getCols();
                fields.forEach(schemaDescriptor ->{
                    MetaSchema.FieldSchema fieldSchema = new MetaSchema.FieldSchema(
                            schemaDescriptor.getName(),
                            schemaDescriptor.getType(),
                            schemaDescriptor.getComment()
                    );
                    fieldSchemas.add(fieldSchema);
                });
                metaSchema.setFieldSchemas(fieldSchemas);
            } finally {
                if (client != null) {
                    client.close();
                }
            }

        } catch (Exception e) {
            LOG.error("Failure to obtain metadata", e);
            throw DataXException.asDataXException(HdfsReaderErrorCode.OBTAIN_METADATA_ERROR, e.getMessage());
        }
        return metaSchema;
    }

    /**
     * update reader configuration by hive meta information dynamically
     * @param database database name
     * @param table table name
     * @param hiveMetaStoreUris hiveMetaStore uris
     * @param originConfig the configuration should be updated
     * @return if affect the original configuration
     */
    boolean updateConfigByHiveMeta(String database, String table,
                                   String hiveMetaStoreUris, Configuration originConfig){
        try{
            IMetaStoreClient client = null;
            boolean affect = false;
            try{
                client = getHiveConnByUris(hiveMetaStoreUris).getMSC();
                Table tableInfo = client.getTable(database, table);
                StorageDescriptor descriptor = tableInfo.getSd();
                String partitionValues = originConfig.getString(PARTITIONS_VALUES);
                if(StringUtils.isNotBlank(partitionValues)){
                    String[] partitions = partitionValues.split(",");
                    Partition partition= null;
                    try{
                        partition = client.getPartition(database, table, Arrays.asList(partitions));
                    }catch(Exception e){
                        //ignore
                    }
                    if(null != partition){
                        //if the partition exists, use its storage descriptor
                        descriptor = partition.getSd();
                    }
                }
                String fileType = detectFileType(descriptor);
                if(StringUtils.isNotBlank(fileType) && !fileType
                        .equalsIgnoreCase(originConfig.getString(FILETYPE, ""))){
                    affect = true;
                    originConfig.set(FILETYPE, fileType);
                }
                String fieldDelimiter = descriptor.getSerdeInfo().getParameters().getOrDefault(Constant.META_FIELD_DELIMITER, "");
                if(StringUtils.isNotEmpty(fieldDelimiter)
                        && !fieldDelimiter.equalsIgnoreCase(originConfig
                        .getString( com.alibaba.datax.plugin.unstructuredstorage.reader.Key.FIELD_DELIMITER, ""))){
                    affect = true;
                    originConfig.set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.FIELD_DELIMITER, fieldDelimiter);
                }
                return affect;
            }finally{
                if(null != client){
                    client.close();
                }
            }
        }catch(Exception e){
            LOG.error("Fail to update configuration", e);
            throw DataXException.asDataXException(HdfsReaderErrorCode.UPDATE_CONFIG_ERROR, e.getMessage());
        }
    }
    void closeFileSystem(){
        try {
            fileSystem.close();
        } catch (IOException e) {
            String message = "关闭FileSystem时发生IO异常,请检查您的网络是否正常！";
            LOG.error(message);
            throw DataXException.asDataXException(HdfsReaderErrorCode.CONNECT_HDFS_IO_ERROR, message);
        }
    }



    boolean exists(String path){
        try{
            return fileSystem.exists(new Path(path));
        }catch(IOException e){
            String message = "exception occurs while reading the file info in HDFS ,please check your network";
            LOG.error(message);
            throw DataXException.asDataXException(HdfsReaderErrorCode.CONNECT_HDFS_IO_ERROR, message);
        }
    }

    /**
     * get hadoop configuration
     * @return
     */
    org.apache.hadoop.conf.Configuration getConf(){
        return hadoopConf;
    }
    /**
     * 获取指定路径列表下符合条件的所有文件的绝对路径
     *
     * @param srcPaths          路径列表
     * @param specifiedFileType 指定文件类型
     */
    HashSet<String> getAllFiles(List<String> srcPaths, String specifiedFileType) {

        this.specifiedFileType = specifiedFileType;

        if (!srcPaths.isEmpty()) {
            for (String eachPath : srcPaths) {
                LOG.info(String.format("get HDFS all files in path = [%s]", eachPath));
                getHDFSAllFiles(eachPath);
            }
        }
        return sourceHDFSAllFilesList;
    }

    private HashSet<String> sourceHDFSAllFilesList = new HashSet<>();

    private HashSet<String> getHDFSAllFiles(String hdfsPath) {

        try {
            //check if the hdfsPath contains regular sign
            if (hdfsPath.contains("*") || hdfsPath.contains("?")) {
                Path path = new Path(hdfsPath);
                FileStatus[] stats = fileSystem.globStatus(path, path1 -> !path1.getName().startsWith("."));
                for (FileStatus f : stats) {
                    if (f.isFile()) {
                        if (f.getLen() == 0) {
                            String message = String.format("文件[%s]长度为0，将会跳过不作处理！", hdfsPath);
                            LOG.warn(message);
                        } else {
                            addSourceFileByType(f.getPath().toString());
                        }
                    } else if (f.isDirectory()) {
                        getHDFSAllFilesNORegex(f.getPath().toString(), fileSystem);
                    }
                }
            } else {
                getHDFSAllFilesNORegex(hdfsPath, fileSystem);
            }

            return sourceHDFSAllFilesList;

        } catch (IOException e) {
            String message = String.format("无法读取路径[%s]下的所有文件,请确认您的配置项fs.defaultFS, path的值是否正确，" +
                    "是否有读写权限，网络是否已断开！", hdfsPath);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsReaderErrorCode.PATH_CONFIG_ERROR, e);
        }
    }

    private HashSet<String> getHDFSAllFilesNORegex(String path, FileSystem hdfs) throws IOException {

        // get the root directory
        Path listFiles = new Path(path);
        // If the network disconnected, this method will retry 45 times
        // each time the retry interval for 20 seconds
        // get all the subdirectories of root directory
        FileStatus stats[] = hdfs.listStatus(listFiles, path1 -> !path1.getName().startsWith("."));
        for (FileStatus f : stats) {
            // recursive directory
            if (f.isDirectory()) {
                LOG.info(String.format("[%s] 是目录, 递归获取该目录下的文件", f.getPath().toString()));
                getHDFSAllFilesNORegex(f.getPath().toString(), hdfs);
            } else if (f.isFile()) {

                addSourceFileByType(f.getPath().toString());
            } else {
                String message = String.format("该路径[%s]文件类型既不是目录也不是文件，插件自动忽略。",
                        f.getPath().toString());
                LOG.info(message);
            }
        }
        return sourceHDFSAllFilesList;
    }

    /**
     * add file int sourceHDFSAllFilesList by filePath
     * @param filePath
     */
    private void addSourceFileByType(String filePath) {
        if(isEmpty(filePath)){
            return;
        }
        boolean isMatchedFileType = checkHdfsFileType(filePath, this.specifiedFileType);

        if (isMatchedFileType) {
            LOG.info(String.format("[%s]是[%s]类型的文件, 将该文件加入source files列表", filePath, this.specifiedFileType));
            sourceHDFSAllFilesList.add(filePath);
        } else {
            String message = String.format("文件[%s]的类型与用户配置的fileType类型不一致，" +
                            "请确认您配置的目录下面所有文件的类型均为[%s]"
                    , filePath, this.specifiedFileType);
            LOG.error(message);
            throw DataXException.asDataXException(
                    HdfsReaderErrorCode.FILE_TYPE_UNSUPPORT, message);
        }
    }

    InputStream getInputStream(String filepath) {
        InputStream inputStream;
        Path path = new Path(filepath);
        try {

            //If the network disconnected, this method will retry 45 times
            //each time the retry interval for 20 seconds
            inputStream = fileSystem.open(path);
            FSDataInputStream in;
            return inputStream;
        } catch (IOException e) {
            String message = String.format("读取文件 : [%s] 时出错,请确认文件：[%s]存在且配置的用户有权限读取", filepath, filepath);
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, message, e);
        }
    }

    void sequenceFileStartRead(String sourceSequenceFilePath, Configuration readerSliceConfig,
                               RecordSender recordSender, TaskPluginCollector taskPluginCollector) {
        LOG.info(String.format("Start Read sequence file [%s].", sourceSequenceFilePath));

        Path seqFilePath = new Path(sourceSequenceFilePath);
        SequenceFile.Reader reader = null;
        try {
            reader = new SequenceFile.Reader(this.hadoopConf,
                    SequenceFile.Reader.file(seqFilePath));
            Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), this.hadoopConf);
            Text value = new Text();
            while (reader.next(key, value)) {
                if (StringUtils.isNotBlank(value.toString())) {
                    UnstructuredStorageReaderUtil.transportOneRecord(recordSender,
                            readerSliceConfig, taskPluginCollector, value.toString());
                }
            }
        } catch (Exception e) {
            String message = String.format("SequenceFile.Reader读取文件[%s]时出错", sourceSequenceFilePath);
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_SEQUENCEFILE_ERROR, message, e);
        } finally {
            IOUtils.closeStream(reader);
            LOG.info("Finally, Close stream SequenceFile.Reader.");
        }

    }

    void rcFileStartRead(String sourceRcFilePath, Configuration readerSliceConfig,
                         RecordSender recordSender, TaskPluginCollector taskPluginCollector) {
        LOG.info(String.format("Start Read RC File [%s].", sourceRcFilePath));
        List<ColumnEntry> column = UnstructuredStorageReaderUtil
                .getListColumnEntry(readerSliceConfig, com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN);
        // warn: no default value '\N'
        String nullFormat = readerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.NULL_FORMAT);

        Path rcFilePath = new Path(sourceRcFilePath);
        RCFileRecordReader recordReader = null;
        try {
            long fileLen = fileSystem.getFileStatus(rcFilePath).getLen();
            FileSplit split = new FileSplit(rcFilePath, 0, fileLen, (String[]) null);
            recordReader = new RCFileRecordReader(hadoopConf, split);
            LongWritable key = new LongWritable();
            BytesRefArrayWritable value = new BytesRefArrayWritable();
            Text txt = new Text();
            while (recordReader.next(key, value)) {
                String[] sourceLine = new String[value.size()];
                txt.clear();
                for (int i = 0; i < value.size(); i++) {
                    BytesRefWritable v = value.get(i);
                    txt.set(v.getData(), v.getStart(), v.getLength());
                    sourceLine[i] = txt.toString();
                }
                UnstructuredStorageReaderUtil.transportOneRecord(recordSender,
                        column, sourceLine, nullFormat, taskPluginCollector);
            }

        } catch (IOException e) {
            String message = String.format("读取文件[%s]时出错", sourceRcFilePath);
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_RCFILE_ERROR, message, e);
        } finally {
            try {
                if (recordReader != null) {
                    recordReader.close();
                    LOG.info("Finally, Close RCFileRecordReader.");
                }
            } catch (IOException e) {
                LOG.warn(String.format("finally: 关闭RCFileRecordReader失败, %s", e.getMessage()));
            }
        }

    }

    void orcFileStartRead(String sourceOrcFilePath, Configuration readerSliceConfig,
                          RecordSender recordSender, TaskPluginCollector taskPluginCollector) {
        LOG.info(String.format("Start Read ORC File [%s].", sourceOrcFilePath));
        List<ColumnEntry> column = UnstructuredStorageReaderUtil
                .getListColumnEntry(readerSliceConfig, com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN);
        String nullFormat = readerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.NULL_FORMAT);
        StringBuilder allColumns = new StringBuilder();
        StringBuilder allColumnTypes = new StringBuilder();
        boolean isReadAllColumns = false;
        int columnIndexMax = -1;
        if (null == column || column.size() == 0) {
            int allColumnsCount = getAllColumnsCount(sourceOrcFilePath);
            columnIndexMax = allColumnsCount - 1;
            isReadAllColumns = true;
        } else {
            columnIndexMax = getMaxIndex(column);
        }
        for (int i = 0; i <= columnIndexMax; i++) {
            allColumns.append("col");
            allColumnTypes.append("string");
            if (i != columnIndexMax) {
                allColumns.append(",");
                allColumnTypes.append(":");
            }
        }
        if (columnIndexMax >= 0) {
            JobConf conf = new JobConf(hadoopConf);
            Path orcFilePath = new Path(sourceOrcFilePath);
            Properties p = new Properties();
            p.setProperty("columns", allColumns.toString());
            p.setProperty("columns.types", allColumnTypes.toString());
            try {
                OrcSerde serde = new OrcSerde();
                serde.initialize(conf, p);
                StructObjectInspector inspector = (StructObjectInspector) serde.getObjectInspector();
                InputFormat<?, ?> in = new OrcInputFormat();
                FileInputFormat.setInputPaths(conf, orcFilePath.toString());

                //If the network disconnected, will retry 45 times, each time the retry interval for 20 seconds
                //Each file as a split
                InputSplit[] splits = in.getSplits(conf, -1);
                for(InputSplit split : splits){
                    RecordReader reader = in.getRecordReader(split, conf, Reporter.NULL);
                    Object key = reader.createKey();
                    Object value = reader.createValue();
                    // get all field refs
                    List<? extends StructField> fields = inspector.getAllStructFieldRefs();

                    List<Object> recordFields;
                    while (reader.next(key, value)) {
                        recordFields = new ArrayList<>();
                        for (int i = 0; i <= columnIndexMax; i++) {
                            Object field = inspector.getStructFieldData(value, fields.get(i));
                            recordFields.add(field);
                        }
                        transportOneRecord(column, recordFields, recordSender,
                                taskPluginCollector, isReadAllColumns, nullFormat);
                    }
                    reader.close();
                }
            } catch (IOException | SerDeException e) {
                String message = String.format("从orcfile文件路径[%s]中读取数据发生异常，请联系系统管理员。"
                        , sourceOrcFilePath);
                throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, message);
            }
        } else {
            String message = String.format("请确认您所读取的列配置正确！columnIndexMax 小于0,column:%s", JSON.toJSONString(column));
            throw DataXException.asDataXException(HdfsReaderErrorCode.BAD_CONFIG_VALUE, message);
        }
    }

    void hFileStartRead(String sourceFilePath, Configuration readerSliceConfig,
                        RecordSender recordSender, TaskPluginCollector taskPluginCollector){
        LOG.info(String.format("Start Read HFile file [%s].", sourceFilePath));
        String encoding = readerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING, "UTF-8");
        HFileParser parser = HFileParserFactory.getHBASEImpl(fileSystem, encoding);
        List<ColumnEntry> column = UnstructuredStorageReaderUtil
                .getListColumnEntry(readerSliceConfig, com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN);
        String nullFormat = readerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.NULL_FORMAT);
        parser.parse(sourceFilePath, readerSliceConfig.getConfiguration(HFILE_PARSE_CONFIG), sourceLine -> UnstructuredStorageReaderUtil.transportOneRecord(recordSender, column,
                sourceLine, nullFormat, taskPluginCollector));
        LOG.info("Finally, Close stream HFile.Reader");
    }

    public FileStatus getFileStatus(String path){
        try{
            return fileSystem.getFileStatus(new Path(path));
        }catch(IOException e){
            String message = String.format("Failed to get file status from : %s", path);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, e);
        }
    }
    private Record transportOneRecord(List<ColumnEntry> columnConfigs, List<Object> recordFields
            , RecordSender recordSender, TaskPluginCollector taskPluginCollector, boolean isReadAllColumns, String nullFormat) {
        Record record = recordSender.createRecord();
        Column columnGenerated;
        try {
            if (isReadAllColumns) {
                // read all the columns, then create the column whose type is STRING
                for (Object recordField : recordFields) {
                    String columnValue = null;
                    if (recordField != null) {
                        columnValue = recordField.toString();
                    }
                    columnGenerated = new StringColumn(columnValue);
                    record.addColumn(columnGenerated);
                }
            } else {
                for (ColumnEntry columnConfig : columnConfigs) {
                    String columnType = columnConfig.getType();
                    Integer columnIndex = columnConfig.getIndex();
                    String columnConst = columnConfig.getValue();

                    String columnValue = null;

                    if (null != columnIndex) {
                        if (null != recordFields.get(columnIndex)) {
                            columnValue = recordFields.get(columnIndex).toString();
                        }
                    } else {
                        columnValue = columnConst;
                    }
                    Type type = Type.valueOf(columnType.toUpperCase());
                    // it's all ok if nullFormat is null
                    if (StringUtils.equals(columnValue, nullFormat) || StringUtils.isBlank(columnValue)) {
                        columnValue = null;
                    }
                    switch (type) {
                        case STRING:
                            columnGenerated = new StringColumn(columnValue);
                            break;
                        case LONG:
                            try {
                                columnGenerated = new LongColumn(columnValue);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "LONG"));
                            }
                            break;
                        case DOUBLE:
                            try {
                                columnGenerated = new DoubleColumn(columnValue);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "DOUBLE"));
                            }
                            break;
                        case BOOLEAN:
                            try {
                                columnGenerated = new BoolColumn(columnValue);
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "BOOLEAN"));
                            }

                            break;
                        case DATE:
                            try {
                                if (columnValue == null) {
                                    columnGenerated = new DateColumn((Date) null);
                                } else {
                                    String formatString = columnConfig.getFormat();
                                    if (StringUtils.isNotBlank(formatString)) {
                                        SimpleDateFormat format = new SimpleDateFormat(
                                                formatString);
                                        columnGenerated = new DateColumn(
                                                format.parse(columnValue));
                                    } else {
                                        columnGenerated = new DateColumn(
                                                new StringColumn(columnValue)
                                                        .asDate());
                                    }
                                }
                            } catch (Exception e) {
                                throw new IllegalArgumentException(String.format(
                                        "类型转换错误, 无法将[%s] 转换为[%s]", columnValue,
                                        "DATE"));
                            }
                            break;
                        default:
                            String errorMessage = String.format(
                                    "您配置的列类型暂不支持 : [%s]", columnType);
                            LOG.error(errorMessage);
                            throw DataXException
                                    .asDataXException(
                                            UnstructuredStorageReaderErrorCode.NOT_SUPPORT_TYPE,
                                            errorMessage);
                    }

                    record.addColumn(columnGenerated);
                }
            }
            recordSender.sendToWriter(record);
        } catch (IllegalArgumentException iae) {
            taskPluginCollector
                    .collectDirtyRecord(record, iae.getMessage());
        } catch (IndexOutOfBoundsException ioe) {
            taskPluginCollector
                    .collectDirtyRecord(record, ioe.getMessage());
        } catch (Exception e) {
            if (e instanceof DataXException) {
                throw (DataXException) e;
            }
            //regard then failure of transform as dirty record
            taskPluginCollector.collectDirtyRecord(record, e.getMessage());
        }

        return record;
    }

    private int getAllColumnsCount(String filePath) {
        Path path = new Path(filePath);
        try {
            Reader reader = OrcFile.createReader(path, OrcFile.readerOptions(hadoopConf));
            return reader.getTypes().get(0).getSubtypesCount();
        } catch (IOException e) {
            String message = "读取orcfile column列数失败，请联系系统管理员";
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, message);
        }
    }

    private int getMaxIndex(List<ColumnEntry> columnConfigs) {
        int maxIndex = -1;
        for (ColumnEntry columnConfig : columnConfigs) {
            Integer columnIndex = columnConfig.getIndex();
            if (columnIndex != null && columnIndex < 0) {
                String message = String.format("您column中配置的index不能小于0，请修改为正确的index,column配置:%s",
                        JSON.toJSONString(columnConfigs));
                LOG.error(message);
                throw DataXException.asDataXException(HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION, message);
            } else if (columnIndex != null && columnIndex > maxIndex) {
                maxIndex = columnIndex;
            }
        }
        return maxIndex;
    }

    private enum Type {
        STRING, LONG, BOOLEAN, DOUBLE, DATE,
    }

    public boolean checkHdfsFileType(String filepath, String specifiedFileType) {
        if(StringUtils.isBlank(specifiedFileType)){
            return true;
        }
        Path file = new Path(filepath);

        try {
            FSDataInputStream in = fileSystem.open(file);

            if (StringUtils.equalsIgnoreCase(specifiedFileType, Constant.CSV)
                    || StringUtils.equalsIgnoreCase(specifiedFileType, Constant.TEXT)) {

                boolean isORC = isORCFile(file, fileSystem, in);
                if (isORC) {
                    return false;
                }
                boolean isRC = isRCFile(filepath, in);
                if (isRC) {
                    return false;
                }
                boolean isSEQ = isSequenceFile(filepath, in);
                if (isSEQ) {
                    return false;
                }
                //default file type is TEXT or CSV
                return !isORC && !isRC && !isSEQ;

            } else if (StringUtils.equalsIgnoreCase(specifiedFileType, Constant.ORC)) {

                return isORCFile(file, fileSystem, in);
            } else if (StringUtils.equalsIgnoreCase(specifiedFileType, Constant.RC)) {

                return isRCFile(filepath, in);
            } else if (StringUtils.equalsIgnoreCase(specifiedFileType, Constant.SEQ)) {

                return isSequenceFile(filepath, in);
            } else if (StringUtils.equalsIgnoreCase(specifiedFileType, Constant.HFILE)){
                //Accept all files
                return true;
            }

        } catch (Exception e) {
            String message = String.format("检查文件[%s]类型失败，目前支持ORC,SEQUENCE,RCFile,TEXT,CSV五种格式的文件," +
                    "请检查您文件类型和文件是否正确。", filepath);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, message, e);
        }
        return false;
    }

    /**
     * if the file is ORC File
     * @param file
     * @param fs
     * @param in
     * @return
     */
    private boolean isORCFile(Path file, FileSystem fs, FSDataInputStream in) {
        try {
            // figure out the size of the file using the option or filesystem
            long size = fs.getFileStatus(file).getLen();

            //read last bytes into buffer to get PostScript
            int readSize = (int) Math.min(size, DIRECTORY_SIZE_GUESS);
            in.seek(size - readSize);
            ByteBuffer buffer = ByteBuffer.allocate(readSize);
            in.readFully(buffer.array(), buffer.arrayOffset() + buffer.position(),
                    buffer.remaining());

            //read the PostScript
            //get length of PostScript
            int psLen = buffer.get(readSize - 1) & 0xff;
            int len = OrcFile.MAGIC.length();
            if (psLen < len + 1) {
                return false;
            }
            int offset = buffer.arrayOffset() + buffer.position() + buffer.limit() - 1
                    - len;
            byte[] array = buffer.array();
            // now look for the magic string at the end of the postscript.
            if (Text.decode(array, offset, len).equals(OrcFile.MAGIC)) {
                return true;
            } else {
                // If it isn't there, this may be the 0.11.0 version of ORC.
                // Read the first 3 bytes of the file to check for the header
                in.seek(0);
                byte[] header = new byte[len];
                in.readFully(header, 0, len);
                // if it isn't there, this isn't an ORC file
                if (Text.decode(header, 0, len).equals(OrcFile.MAGIC)) {
                    return true;
                }
            }
        } catch (IOException e) {
            LOG.info(String.format("检查文件类型: [%s] 不是ORC File.", file.toString()));
        }
        return false;
    }

    /**
     * if the file is RC file
      * @param filepath
     * @param in
     * @return
     */
    private boolean isRCFile(String filepath, FSDataInputStream in) {

        // The first version of RCFile used the sequence file header.
        final byte[] ORIGINAL_MAGIC = new byte[]{(byte) 'S', (byte) 'E', (byte) 'Q'};
        // The 'magic' bytes at the beginning of the RCFile
        final byte[] RC_MAGIC = new byte[]{(byte) 'R', (byte) 'C', (byte) 'F'};
        // the version that was included with the original magic, which is mapped
        // into ORIGINAL_VERSION
        final byte ORIGINAL_MAGIC_VERSION_WITH_METADATA = 6;
        // All of the versions should be place in this list.
        // version with SEQ
        final int ORIGINAL_VERSION = 0;
        // version with RCF
        final int NEW_MAGIC_VERSION = 1;
        final int CURRENT_VERSION = NEW_MAGIC_VERSION;
        byte version;

        byte[] magic = new byte[RC_MAGIC.length];
        try {
            in.seek(0);
            in.readFully(magic);

            if (Arrays.equals(magic, ORIGINAL_MAGIC)) {
                byte vers = in.readByte();
                if (vers != ORIGINAL_MAGIC_VERSION_WITH_METADATA) {
                    return false;
                }
                version = ORIGINAL_VERSION;
            } else {
                if (!Arrays.equals(magic, RC_MAGIC)) {
                    return false;
                }

                // Set 'version'
                version = in.readByte();
                if (version > CURRENT_VERSION) {
                    return false;
                }
            }

            if (version == ORIGINAL_VERSION) {
                try {
                    Class<?> keyCls = hadoopConf.getClassByName(Text.readString(in));
                    Class<?> valCls = hadoopConf.getClassByName(Text.readString(in));
                    if (!keyCls.equals(RCFile.KeyBuffer.class)
                            || !valCls.equals(RCFile.ValueBuffer.class)) {
                        return false;
                    }
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
            // is compressed?
            boolean decompress = in.readBoolean();
            if (version == ORIGINAL_VERSION) {
                // is block-compressed? it should be always false.
                boolean blkCompressed = in.readBoolean();
                if (blkCompressed) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            LOG.info(String.format("检查文件类型: [%s] 不是RC File.", filepath));
        }
        return false;
    }

    /**
     * if the file is Sequence file
     * @param filepath
     * @param in
     * @return
     */
    private boolean isSequenceFile(String filepath, FSDataInputStream in) {
        byte[] SEQ_MAGIC = new byte[]{(byte) 'S', (byte) 'E', (byte) 'Q'};
        byte[] magic = new byte[SEQ_MAGIC.length];
        try {
            in.seek(0);
            in.readFully(magic);
            if (Arrays.equals(magic, SEQ_MAGIC)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            LOG.info(String.format("检查文件类型: [%s] 不是Sequence File.", filepath));
        }
        return false;
    }


    /**
     * check if the file is empty
     * @param filePath
     * @return
     * @throws IOException
     */
    private boolean isEmpty(String filePath){
        FileStatus status;
        try {
            status = fileSystem.getFileStatus(new Path(filePath));
            return status.getLen() <= 0;
        } catch (IOException e) {
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, e.getMessage(), e);
        }
    }

    private Hive getHiveConnByUris(String hiveMetaStoreUris) throws IOException, InterruptedException {
        HiveConf hiveConf = new HiveConf();
        hiveConf.setVar(HiveConf.ConfVars.METASTOREURIS, hiveMetaStoreUris);
        UserGroupInformation hiveUgi;
        if(haveKerberos){
            Properties kerberosProps = KerberosUtil.getProperties();
            kerberosProps = null == kerberosProps? new Properties() : kerberosProps;
            if(StringUtils.isNotBlank(kerberosProps.getProperty("kerberos.krb5.path", ""))){
                System.setProperty("java.security.krb5.conf", kerberosProps.getProperty("kerberos.krb5.path"));
            }
            hiveConf.setVar(HiveConf.ConfVars.METASTORE_USE_THRIFT_SASL, "true");
            hiveConf.set("hadoop.security.authentication", "kerberos");
            hiveConf.setVar(HiveConf.ConfVars.METASTORE_KERBEROS_PRINCIPAL,
                    this.readerConfig.getString(HIVE_KEBEROS_PRINCIPAL, DEFAULT_HIVE_USER + "/_HOST@EXAMPLE.COM"));
            hiveUgi = this.kerberosAuthentication(hiveConf, this.kerberosPrincipal, this.kerberosKeytabFilePath);
        }else{
            hiveUgi = this.getUgiInAuth(this.readerConfig);
        }
        return hiveUgi.doAs((PrivilegedExceptionAction<Hive>) () -> {
            Hive hive1 = Hive.get(hiveConf);
            hive1.getMSC();
            return hive1;
        });
    }

    private UserGroupInformation getUgiInAuth(Configuration taskConfig){
        String userName = taskConfig.getString(LDAP_USERNAME, "");
        String password = taskConfig.getString(LDAP_USERPASSWORD, "");
        if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)){
            try {
                password = (String) CryptoUtils.string2Object(password);
            } catch (Exception e) {
                LOG.error("Fail to decrypt password", e);
                throw DataXException.asDataXException(HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION, e);
            }
            Properties properties = null;
            try {
                properties = LdapUtil.getLdapProperties();
            }catch(Exception e){
                //Ignore
            }
            if(null != properties){
                LdapConnector ldapConnector = LdapConnector.getInstance(properties);
                if(!ldapConnector.authenticate(userName, password)){
                    throw DataXException.asDataXException(HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION, "LDAP authenticate fail");
                }
            }else{
                throw DataXException.asDataXException(HdfsReaderErrorCode.CONFIG_INVALID_EXCEPTION, "Engine need LDAP configuration");
            }
        }
        UserGroupInformation ugi;
        try {
            UserGroupInformation.setConfiguration(hadoopConf);
            String procUser = System.getProperty("user.name", "");
            String execUser = System.getProperty(EXEC_USER, "");
            String remoteUser = StringUtils.isNotBlank(userName) ? userName : execUser;
            if(StringUtils.isNotBlank(remoteUser) && !remoteUser.equals(procUser)){
                //Disable the cache
                this.hadoopConf.setBoolean(
                        String.format(FS_DISABLE_CACHE, URI.create(this.hadoopConf.get(FS_DEFAULT_NAME_KEY, "")).getScheme()), true);
                ugi = UserGroupInformation.createRemoteUser(remoteUser);
            }else{
                ugi = UserGroupInformation.getCurrentUser();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw DataXException.asDataXException(HdfsReaderErrorCode.HDFS_PROXY_ERROR, e);
        }
        return ugi;
    }
    private UserGroupInformation kerberosAuthentication(String kerberosPrincipal, String kerberosKeytabFilePath){
        return kerberosAuthentication(this.hadoopConf, kerberosPrincipal, kerberosKeytabFilePath);
    }

    private UserGroupInformation kerberosAuthentication(org.apache.hadoop.conf.Configuration config,
                                                        String kerberosPrincipal, String kerberosKeytabFilePath) {
        UserGroupInformation ugi = null;
        if(StringUtils.isNotBlank(kerberosPrincipal) && StringUtils.isNotBlank(kerberosKeytabFilePath)) {
            UserGroupInformation.setConfiguration(config);
            try {
                ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(kerberosPrincipal
                        .substring(0, kerberosPrincipal.indexOf("@")), kerberosKeytabFilePath);
            } catch (Exception e) {
                String message = String.format("kerberos认证失败,请确定kerberosKeytabFilePath[%s]和kerberosPrincipal[%s]填写正确",
                        kerberosKeytabFilePath, kerberosPrincipal);
                LOG.error(message);
                throw DataXException.asDataXException(HdfsReaderErrorCode.KERBEROS_LOGIN_ERROR, e);
            }
        }
        return ugi;
    }

    private enum HiveFileType{
        /**
         * TYPE: TEXT
         */
        TEXT,
        /**
         * TYPE: ORC
         */
        ORC,
        /**
         * TYPE: AVRO
         */
        AVRO,
        /**
         * TYPE: PARQUET
         */
        PARQUET,
        /**
         * TYPE: RC
         */
        RC,
        /**
         * TYPE: SEQ
         */
        SEQ;
        static final Map<String, HiveFileType> INPUT_FORMAT = new HashMap<>();
        static{
            INPUT_FORMAT.put(new TextFileStorageFormatDescriptor().getInputFormat(), TEXT);
            INPUT_FORMAT.put(new ORCFileStorageFormatDescriptor().getInputFormat(), ORC);
            INPUT_FORMAT.put(new AvroStorageFormatDescriptor().getInputFormat(), AVRO);
            INPUT_FORMAT.put(new ParquetFileStorageFormatDescriptor().getInputFormat(), PARQUET);
            INPUT_FORMAT.put(new RCFileStorageFormatDescriptor().getInputFormat(), RC);
            INPUT_FORMAT.put(new SequenceFileStorageFormatDescriptor().getInputFormat(), SEQ);
        }
        static HiveFileType input(String inputStreamFormat){
            return INPUT_FORMAT.get(inputStreamFormat);
        }
    }

    /**
     * detect the file type
     * @param tableDescriptor tableDescriptor
     * @return
     */
    private String detectFileType(StorageDescriptor tableDescriptor){
        //search file type by output format of table/partition
        HiveFileType hiveFileType = HiveFileType.input(tableDescriptor.getInputFormat());
        return hiveFileType != null ? hiveFileType.toString(): "";
    }
}
