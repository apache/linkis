package com.alibaba.datax.plugin.writer.hdfswriter;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.plugin.TaskPluginCollector;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.LdapUtil;
import com.alibaba.datax.plugin.utils.HdfsUserGroupInfoLock;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.datax.common.CryptoUtils;
import com.webank.wedatasphere.linkis.datax.common.ldap.LdapConnector;
import com.webank.wedatasphere.linkis.datax.core.job.meta.MetaSchema;
import com.webank.wedatasphere.linkis.datax.util.KerberosUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.Warehouse;
import org.apache.hadoop.hive.metastore.api.*;
import org.apache.hadoop.hive.ql.io.*;
import org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat;
import org.apache.hadoop.hive.ql.io.orc.OrcSerde;
import org.apache.hadoop.hive.ql.io.orc.OrcStruct;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static com.alibaba.datax.plugin.writer.hdfswriter.Key.HIVE_KEBEROS_PRINCIPAL;
import static org.apache.hadoop.fs.FileSystem.FS_DEFAULT_NAME_KEY;

public class HdfsWriterUtil {
    private static final String DEFAULT_HIVE_USER = "hadoop";
    private static final Logger LOG = LoggerFactory.getLogger(HdfsWriter.Job.class);
    FileSystem fileSystem = null;
    private JobConf conf = null;
    org.apache.hadoop.conf.Configuration hadoopConf = null;

    private static final String FS_AUTOMATIC_CLOSE_KEY = "fs.automatic.close";
    private static final String FS_DISABLE_CACHE = "fs.%s.impl.disable.cache";
    private static final String HADOOP_SECURITY_AUTHENTICATION_KEY = "hadoop.security.authentication";
    private static final String FALLBACK_TO_SIMPLE_AUTH_KEY = "ipc.client.fallback-to-simple-auth-allowed";
    private static final String HDFS_DEFAULT_FS_KEY = "fs.defaultFS";
    private static final String DEFAULT_COLUMN_CONFIG = "{\"name\":\"column%s\",\"type\":\"string\"}";
    private static final String EXEC_USER = "exec.user";
    // Kerberos
    private Boolean haveKerberos = false;
    private String kerberosKeytabFilePath;
    private String kerberosPrincipal;
    private UserGroupInformation ugi = null;
    private Configuration writerConfig;

    UserGroupInformation getUgi(){
        return ugi;
    }

    void getFileSystem(String defaultFS, Configuration taskConfig) {
        hadoopConf = new org.apache.hadoop.conf.Configuration();
        this.writerConfig = taskConfig;
        Configuration hadoopSiteParams = taskConfig.getConfiguration(Key.HADOOP_CONFIG);
        JSONObject hadoopSiteParamsAsJsonObject = JSON.parseObject(taskConfig.getString(Key.HADOOP_CONFIG));
        if (null != hadoopSiteParams) {
            Set<String> paramKeys = hadoopSiteParams.getKeys();
            for (String each : paramKeys) {
                hadoopConf.set(each, hadoopSiteParamsAsJsonObject.getString(each));
            }
        }
        hadoopConf.set(HDFS_DEFAULT_FS_KEY, defaultFS);
        //disable automatic close
        hadoopConf.setBoolean(FS_AUTOMATIC_CLOSE_KEY, false);
        //if use kerberos authentication
        this.haveKerberos = taskConfig.getBool(Key.HAVE_KERBEROS, false);
        HdfsUserGroupInfoLock.lock();
        try {
            if (haveKerberos) {
                this.kerberosKeytabFilePath = taskConfig.getString(Key.KERBEROS_KEYTAB_FILE_PATH);
                this.kerberosPrincipal = taskConfig.getString(Key.KERBEROS_PRINCIPAL);
                hadoopConf.set(HADOOP_SECURITY_AUTHENTICATION_KEY, "kerberos");
                //disable the cache
                this.hadoopConf.setBoolean(
                        String.format(FS_DISABLE_CACHE, URI.create(this.hadoopConf.get(FS_DEFAULT_NAME_KEY, "")).getScheme()), true);
                hadoopConf.setBoolean(FALLBACK_TO_SIMPLE_AUTH_KEY, true);
                ugi = getUgiByKerberos(this.hadoopConf, this.kerberosPrincipal, this.kerberosKeytabFilePath);
            } else {
                ugi = getUgiInAuth(taskConfig);
            }
            try {
                fileSystem = ugi.doAs((PrivilegedExceptionAction<FileSystem>) () -> {
                    conf = new JobConf(hadoopConf);
                    FileSystem fs;
                    try {
                        fs = FileSystem.get(conf);
                        fs.exists(new Path("/"));
                    } catch (IOException e) {
                        String message = String.format("获取FileSystem时发生网络IO异常,请检查您的网络是否正常!HDFS地址：[%s]",
                                "message:defaultFS =" + defaultFS);
                        LOG.error(message);
                        throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
                    } catch (Exception e) {
                        String message = String.format("获取FileSystem失败,请检查HDFS地址是否正确: [%s]",
                                "message:defaultFS =" + defaultFS);
                        LOG.error(message);
                        throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
                    }
                    return fs;
                });
            } catch (Exception e) {
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
            }
        }finally{
            HdfsUserGroupInfoLock.unlock();
        }
        if (null == fileSystem || null == conf) {
            String message = String.format("获取FileSystem失败,请检查HDFS地址是否正确: [%s]",
                    "message:defaultFS =" + defaultFS);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, message);
        }
    }

    /**
     * update hive meta information
     * @param metaSchema meta schema info
     */
    void updateHiveMetaData(String database, String table, String hiveMetaStoreUris,MetaSchema metaSchema){
        Hive hive = null;
        try {
            try {
                hive = getHiveConnByUris(hiveMetaStoreUris);
                IMetaStoreClient metaStoreClient = hive.getMSC();
                Table tableInfo = metaStoreClient.getTable(database, table);
                StorageDescriptor storageDescriptor = tableInfo.getSd();
                List<MetaSchema.FieldSchema> fieldSchemas = metaSchema.getFieldSchemas();
                List<FieldSchema> cols = new ArrayList<>();
                fieldSchemas.forEach(fieldSchema -> cols.add(new FieldSchema(fieldSchema.getName(), fieldSchema.getType(),
                        fieldSchema.getComment())));
                storageDescriptor.setCols(cols);
                SerDeInfo serDeInfo = metaSchema.getSchemaInfo(Key.HIVE_META_SERDE_INFO, SerDeInfo.class);
                if (null != serDeInfo) {
                    storageDescriptor.setSerdeInfo(serDeInfo);
                }
                Integer numBuckets = metaSchema.getSchemaInfo(Key.HIVE_META_NUM_BUCKETS, Integer.class);
                if (null != numBuckets) {
                    storageDescriptor.setNumBuckets(numBuckets);
                }
                List<String> bucketCols = metaSchema.getSchemaInfoList(Key.HIVE_META_BUCKET_COLS, String.class);
                if (null != bucketCols) {
                    storageDescriptor.setBucketCols(bucketCols);
                }
                List<Order> sortCols = metaSchema.getSchemaInfoList(Key.HIVE_META_SORT_COLS, Order.class);
                if (null != sortCols) {
                    storageDescriptor.setSortCols(sortCols);
                }
                String inputFormat = metaSchema.getSchemaInfo(Key.HIVE_META_INPUT_FORMAT, String.class);
                if (null != inputFormat) {
                    storageDescriptor.setInputFormat(inputFormat);
                }
                String outputFormat = metaSchema.getSchemaInfo(Key.HIVE_META_OUTPUT_FORMAT, String.class);
                if (null != outputFormat) {
                    storageDescriptor.setOutputFormat(outputFormat);
                }
                Map<String, String> parameters = metaSchema.getSchemaInfoMap(Key.HIVE_META_PARAMETERS, String.class, String.class);
                if (null != parameters && !parameters.isEmpty()) {
                    storageDescriptor.setParameters(parameters);
                }
                Boolean compressed = metaSchema.getSchemaInfo(Key.HIVE_META_COMPRESSED, Boolean.class);
                if (null != compressed) {
                    storageDescriptor.setCompressed(compressed);
                }
                //alter_table finally
                metaStoreClient.alter_table(database, table, tableInfo, true);
            } finally {
                if (null != hive) {
                    hive.getMSC().close();
                }
            }
        }catch(Exception e){
            LOG.error("Update hive metadata error table:[{}], database:[{}]", table, database, e);
            throw DataXException.asDataXException(HdfsWriterErrorCode.UPDATE_HIVE_META, e.getMessage());
        }
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
                Hive hive = getHiveConnByUris(hiveMetaStoreUris);
                client = hive.getMSC();
                Table tableInfo = client.getTable(database, table);
                StorageDescriptor descriptor = tableInfo.getSd();
                String partitionValues = originConfig.getString(Key.PARTITIONS_VALUES);
                if(StringUtils.isNotBlank(partitionValues)){
                    String[] partitions = partitionValues.split(",");
                    Partition partition= null;
                    try{
                        partition = client.getPartition(database, table, Arrays.asList(partitions));
                    }catch(Exception e){
                        LOG.error(e.getMessage());
                        //ignore
                    }
                    if(null != partition){
                        //if the partition exists, use its storage descriptor
                        descriptor = partition.getSd();
                    }
                }
                String fileType = detectFileType(descriptor);
                if(StringUtils.isNotBlank(fileType) && !fileType
                        .equalsIgnoreCase(originConfig.getString(Key.FILE_TYPE, ""))){
                    affect = true;
                    originConfig.set(Key.FILE_TYPE, fileType);
                    if(fileType.equalsIgnoreCase(HiveFileType.TEXT.toString())){
                        originConfig.set(Key.COMPRESS, "GZIP");
                    }else if(fileType.equalsIgnoreCase(HiveFileType.ORC.toString())){
                        originConfig.set(Key.COMPRESS, "SNAPPY");
                    }else{
                        originConfig.set(Key.COMPRESS, null);
                    }
                }
                String fieldDelimiter = descriptor.getSerdeInfo().getParameters().getOrDefault(Constant.META_FIELD_DELIMITER, "");
                if(StringUtils.isNotEmpty(fieldDelimiter)
                        && !fieldDelimiter.equalsIgnoreCase(originConfig
                        .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FIELD_DELIMITER, ""))){
                    affect = true;
                    originConfig.set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FIELD_DELIMITER, fieldDelimiter);
                }
                if(null == originConfig.getListConfiguration(Key.COLUMN)){
                    affect = true;
                    List<MetaSchema.FieldSchema> fieldSchemas = new ArrayList<>();
                    descriptor.getCols().forEach(col -> fieldSchemas.add(new MetaSchema.FieldSchema(col.getName(), col.getType(), null)));
                    originConfig.set(Key.COLUMN, fieldSchemas);
                }
                //to add partition
                if(StringUtils.isNotBlank(originConfig.getString(Key.PARTITIONS_VALUES, ""))){
                    addHiveTablePartitions(hive, database, table, originConfig.getString(Key.PARTITIONS_VALUES, ""));
                }
                return affect;
            }finally{
                if(null != client){
                    client.close();
                }
            }
        }catch(Exception e){
            if(e instanceof DataXException){
                throw (DataXException)e;
            }
            LOG.error("Fail to update configuration", e);
            throw DataXException.asDataXException(HdfsWriterErrorCode.UPDATE_CONFIG_ERROR, e.getMessage());
        }
    }
    /**
     * 获取指定目录先的文件列表
     *
     * @param dir
     * @return 拿到的是文件全路径，
     */
    String[] hdfsDirList(String dir) {
        Path path = new Path(dir);
        String[] files = null;
        try {
            FileStatus[] status = fileSystem.listStatus(path);
            files = new String[status.length];
            for (int i = 0; i < status.length; i++) {
                files[i] = status[i].getPath().toString();
            }
        } catch (IOException e) {
            String message = String.format("获取目录[%s]文件列表时发生网络IO异常,请检查您的网络是否正常！", dir);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return files;
    }

    /**
     * 获取以fileName__ 开头的文件列表
     *
     * @param dir
     * @param fileName
     * @return
     */
    public Path[] hdfsDirList(String dir, String fileName) {
        Path path = new Path(dir);
        Path[] files = null;
        String filterFileName = fileName + "__*";
        try {
            PathFilter pathFilter = new GlobFilter(filterFileName);
            FileStatus[] status = fileSystem.listStatus(path, pathFilter);
            files = new Path[status.length];
            for (int i = 0; i < status.length; i++) {
                files[i] = status[i].getPath();
            }
        } catch (IOException e) {
            String message = String.format("获取目录[%s]下文件名以[%s]开头的文件列表时发生网络IO异常,请检查您的网络是否正常！",
                    dir, fileName);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return files;
    }

    boolean isPathexists(String filePath) {
        Path path = new Path(filePath);
        boolean exist = false;
        try {
            exist = fileSystem.exists(path);
        } catch (IOException e) {
            String message = String.format("判断文件路径[%s]是否存在时发生网络IO异常,请检查您的网络是否正常！",
                    "message:filePath =" + filePath);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return exist;
    }

    boolean isPathDir(String filePath) {
        Path path = new Path(filePath);
        boolean isDir = false;
        try {
            isDir = fileSystem.isDirectory(path);
        } catch (IOException e) {
            String message = String.format("判断路径[%s]是否是目录时发生网络IO异常,请检查您的网络是否正常！", filePath);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        return isDir;
    }

    void mkdirs(Path path){
        try {
            fileSystem.mkdirs(path);
        }catch(IOException e){
            String message = String.format("Occurred IO error while creating directory %s", path.toString());
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
    }
    void deleteFiles(Path[] paths) {
        for (int i = 0; i < paths.length; i++) {
            LOG.info(String.format("delete file [%s].", paths[i].toString()));
            try {
                fileSystem.delete(paths[i], true);
            } catch (IOException e) {
                String message = String.format("删除文件[%s]时发生IO异常,请检查您的网络是否正常！",
                        paths[i].toString());
                LOG.error(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
            }
        }
    }

    void deleteDir(Path path) {
        LOG.info(String.format("start delete tmp dir [%s] .", path.toString()));
        try {
            if (isPathexists(path.toString())) {
                fileSystem.delete(path, true);
            }
        } catch (Exception e) {
            String message = String.format("删除临时目录[%s]时发生IO异常,请检查您的网络是否正常！", path.toString());
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
        LOG.info(String.format("finish delete tmp dir [%s] .", path.toString()));
    }

    void renameFile(HashSet<String> tmpFiles, HashSet<String> endFiles) {
        Path tmpFilesParent = null;
        if (tmpFiles.size() != endFiles.size()) {
            String message = String.format("临时目录下文件名个数与目标文件名个数不一致!");
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_RENAME_FILE_ERROR, message);
        } else {
            try {
                for (Iterator it1 = tmpFiles.iterator(), it2 = endFiles.iterator(); it1.hasNext() && it2.hasNext(); ) {
                    String srcFile = it1.next().toString();
                    String dstFile = it2.next().toString();
                    Path srcFilePah = new Path(srcFile);
                    Path dstFilePah = new Path(dstFile);
                    if (tmpFilesParent == null) {
                        tmpFilesParent = srcFilePah.getParent();
                    }
                    LOG.info(String.format("start rename file [%s] to file [%s].", srcFile, dstFile));
                    boolean renameTag = false;
                    long fileLen = fileSystem.getFileStatus(srcFilePah).getLen();
                    if (fileLen > 0) {
                        renameTag = fileSystem.rename(srcFilePah, dstFilePah);
                        if (!renameTag) {
                            String message = String.format("重命名文件[%s]失败,请检查您的网络是否正常！", srcFile);
                            LOG.error(message);
                            throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_RENAME_FILE_ERROR, message);
                        }
                        LOG.info(String.format("finish rename file [%s] to file [%s].", srcFile, dstFile));
                    } else {
                        LOG.info(String.format("文件［%s］内容为空,请检查写入是否正常！", srcFile));
                    }
                }
            } catch (Exception e) {
                String message = String.format("重命名文件时发生异常,请检查您的网络是否正常！");
                LOG.error(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
            } finally {
                deleteDir(tmpFilesParent);
            }
        }
    }

    void moveToDirectory(List<String> srcPaths, String destPath){
        try{
            Path dest = new Path(destPath);
            if(!fileSystem.exists(dest)){
                fileSystem.mkdirs(dest);
            }else if(!fileSystem.isDirectory(dest)){
                String message = String.format("move to directory error, %s is not a directory", destPath);
                LOG.error(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_RENAME_FILE_ERROR, message);
            }
            for(String srcPath : srcPaths){
                if(srcPath.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))){
                    srcPath = srcPath.substring(0, srcPath.length() - 1);
                }
                Path path = new Path(srcPath);
                String destName = path.getName();
                StringBuilder destPathBuilder = new StringBuilder(destPath);
                if(!destPathBuilder.toString().endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))){
                    destPathBuilder.append(IOUtils.DIR_SEPARATOR_UNIX);
                }
                destPathBuilder.append(destName);
                if(fileSystem.isDirectory(path)) {
                    moveToDirectory(Arrays.asList(hdfsDirList(path.toString())), destPathBuilder.toString());
                }else{
                    Path destFilePath = new Path(destPathBuilder.toString());
                    if(fileSystem.exists(destFilePath)){
                        fileSystem.delete(destFilePath, false);
                    }
                    fileSystem.rename(path, destFilePath);
                }
            }
        }catch(Exception e){
            String message = String.format("occurred error while move srcPaths : %s to destPath: %s ,please check your network",
                    JSON.toJSONString(srcPaths), destPath);
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
    }
    //关闭FileSystem
    void closeFileSystem() {
        try {
            fileSystem.close();
        } catch (IOException e) {
            String message = "关闭FileSystem时发生IO异常,请检查您的网络是否正常！";
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONNECT_HDFS_IO_ERROR, e);
        }
    }


    //textfile格式文件
    public FSDataOutputStream getOutputStream(String path) {
        Path storePath = new Path(path);
        FSDataOutputStream fSDataOutputStream = null;
        try {
            fSDataOutputStream = fileSystem.create(storePath);
        } catch (IOException e) {
            String message = String.format("Create an FSDataOutputStream at the indicated Path[%s] failed: [%s]",
                    "message:path =" + path, e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(HdfsWriterErrorCode.WRITE_FILE_IO_ERROR, e);
        }
        return fSDataOutputStream;
    }

    /**
     * 写textfile类型文件
     *
     * @param lineReceiver
     * @param config
     * @param fileName
     * @param taskPluginCollector
     */
    void textFileStartWrite(RecordReceiver lineReceiver, Configuration config, String fileName,
                            TaskPluginCollector taskPluginCollector) {
        String fieldDelimiter = config.getString(Key.FIELD_DELIMITER);
        List<Configuration> columns = config.getListConfiguration(Key.COLUMN);
        String compress = config.getString(Key.COMPRESS, null);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String attempt = "attempt_" + dateFormat.format(new Date()) + "_0001_m_000000_0";
        Path outputPath = new Path(fileName);
        //todo 需要进一步确定TASK_ATTEMPT_ID
        conf.set(JobContext.TASK_ATTEMPT_ID, attempt);
        FileOutputFormat outFormat = new TextOutputFormat();
        FileOutputFormat.setOutputPath(conf, outputPath);
        FileOutputFormat.setWorkOutputPath(conf, outputPath);
        if (null != compress) {
            Class<? extends CompressionCodec> codecClass = getCompressCodec(compress);
            if (null != codecClass) {
                FileOutputFormat.setOutputCompressorClass(conf, codecClass);
            }
        }
        try {
            RecordWriter writer = outFormat.getRecordWriter(fileSystem, conf, outputPath.toString(), Reporter.NULL);
            Record record = null;
            while ((record = lineReceiver.getFromReader()) != null) {
                MutablePair<Text, Boolean> transportResult = transportOneRecord(record, fieldDelimiter, columns, taskPluginCollector);
                if (!transportResult.getRight()) {
                    writer.write(NullWritable.get(), transportResult.getLeft());
                }
            }
            writer.close(Reporter.NULL);
        } catch (Exception e) {
            String message = String.format("写文件文件[%s]时发生IO异常,请检查您的网络是否正常！", fileName);
            LOG.error(message);
            Path path = new Path(fileName);
            deleteDir(path.getParent());
            throw DataXException.asDataXException(HdfsWriterErrorCode.WRITE_FILE_IO_ERROR, e);
        }

    }

    private static MutablePair<Text, Boolean> transportOneRecord(
            Record record, String fieldDelimiter, List<Configuration> columnsConfiguration, TaskPluginCollector taskPluginCollector) {
        MutablePair<List<Object>, Boolean> transportResultList = transportOneRecord(record, columnsConfiguration, taskPluginCollector);
        //保存<转换后的数据,是否是脏数据>
        MutablePair<Text, Boolean> transportResult = new MutablePair<Text, Boolean>();
        transportResult.setRight(false);
        if (null != transportResultList) {
            Text recordResult = new Text(StringUtils.join(transportResultList.getLeft(), fieldDelimiter));
            transportResult.setRight(transportResultList.getRight());
            transportResult.setLeft(recordResult);
        }
        return transportResult;
    }

    private Class<? extends CompressionCodec> getCompressCodec(String compress) {
        Class<? extends CompressionCodec> codecClass = null;
        if (null == compress) {
            codecClass = null;
        } else if ("GZIP".equalsIgnoreCase(compress)) {
            codecClass = org.apache.hadoop.io.compress.GzipCodec.class;
        } else if ("BZIP2".equalsIgnoreCase(compress)) {
            codecClass = org.apache.hadoop.io.compress.BZip2Codec.class;
        } else if ("SNAPPY".equalsIgnoreCase(compress)) {
            //todo 等需求明确后支持 需要用户安装SnappyCodec
            codecClass = org.apache.hadoop.io.compress.SnappyCodec.class;
            // org.apache.hadoop.hive.ql.io.orc.ZlibCodec.class  not static
            //codecClass = org.apache.hadoop.hive.ql.io.orc.ZlibCodec.class;
        } else {
            throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                    String.format("目前不支持您配置的 compress 模式 : [%s]", compress));
        }
        return codecClass;
    }

    /**
     * 写orcfile类型文件
     *
     * @param lineReceiver
     * @param config
     * @param fileName
     * @param taskPluginCollector
     */
    void orcFileStartWrite(RecordReceiver lineReceiver, Configuration config, String fileName,
                           TaskPluginCollector taskPluginCollector) {
        List<Configuration> columns = config.getListConfiguration(Key.COLUMN);
        String compress = config.getString(Key.COMPRESS, null);
        OrcSerde orcSerde = new OrcSerde();

        FileOutputFormat outFormat = new OrcOutputFormat();
        if (!"NONE".equalsIgnoreCase(compress) && null != compress) {
            Class<? extends CompressionCodec> codecClass = getCompressCodec(compress);
            if (null != codecClass) {
                FileOutputFormat.setOutputCompressorClass(conf, codecClass);
            }
        }
        try {
            RecordWriter writer = outFormat.getRecordWriter(fileSystem, conf, fileName, Reporter.NULL);
            Record record = null;
            StructObjectInspector inspector =  null;
            while ((record = lineReceiver.getFromReader()) != null) {
                MutablePair<List<Object>, Boolean> transportResult = transportOneRecord(record, columns, taskPluginCollector);
                int length = record.getColumnNumber();
                if(null == inspector){
                    if(null == columns){
                        columns = new ArrayList<>();
                        for(int i = 0; i < length; i ++){
                            columns.add(Configuration.from(String.format(DEFAULT_COLUMN_CONFIG, i)));
                        }
                    }
                    List<String> columnNames = getColumnNames(columns);
                    List<ObjectInspector> columnTypeInspectors = getColumnTypeInspectors(columns);
                    inspector = ObjectInspectorFactory
                            .getStandardStructObjectInspector(columnNames, columnTypeInspectors);
                }
                if (!transportResult.getRight() && null != writer) {
                    transformOrcStruct(transportResult, columns);
                    writer.write(NullWritable.get(), orcSerde.serialize(transportResult.getLeft(), inspector));
                }
            }
            writer.close(Reporter.NULL);
        } catch (Exception e) {
            String message = String.format("写文件文件[%s]时发生IO异常,请检查您的网络是否正常！", fileName);
            LOG.error(message);
            Path path = new Path(fileName);
            deleteDir(path.getParent());
            throw DataXException.asDataXException(HdfsWriterErrorCode.WRITE_FILE_IO_ERROR, e);
        }

    }

    private List<String> getColumnNames(List<Configuration> columns) {
        List<String> columnNames = Lists.newArrayList();
        if(null != columns) {
            for (Configuration eachColumnConf : columns) {
                columnNames.add(eachColumnConf.getString(Key.NAME));
            }
        }
        return columnNames;
    }

    /**
     * 根据writer配置的字段类型，构建inspector
     *
     * @param columns
     * @return
     */
    private List<ObjectInspector> getColumnTypeInspectors(List<Configuration> columns) {
        List<ObjectInspector> columnTypeInspectors = Lists.newArrayList();
        if(null != columns) {
            for (Configuration eachColumnConf : columns) {
                SupportHiveDataType columnType = SupportHiveDataType.valueOf(eachColumnConf.getString(Key.TYPE).toUpperCase());
                ObjectInspector objectInspector = null;
                switch (columnType) {
                    case TINYINT:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Byte.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case SMALLINT:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Short.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case INT:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Integer.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case BIGINT:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Long.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case FLOAT:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Float.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case DOUBLE:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Double.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case TIMESTAMP:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(java.sql.Timestamp.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case DATE:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(java.sql.Date.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case STRING:
                    case VARCHAR:
                    case CHAR:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(String.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case BOOLEAN:
                        objectInspector = ObjectInspectorFactory.getReflectionObjectInspector(Boolean.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
                        break;
                    case MAP:
                        objectInspector = OrcStruct.createObjectInspector(
                                TypeInfoFactory.getMapTypeInfo(TypeInfoFactory.stringTypeInfo, TypeInfoFactory.stringTypeInfo));
                        break;
                    case ARRAY:
                        objectInspector = OrcStruct.createObjectInspector(TypeInfoFactory.getListTypeInfo(TypeInfoFactory.stringTypeInfo));
                        break;
                    default:
                        throw DataXException
                                .asDataXException(
                                        HdfsWriterErrorCode.ILLEGAL_VALUE,
                                        String.format(
                                                "您的配置文件中的列配置信息有误. 因为DataX 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%s]. 请修改表中该字段的类型或者不同步该字段.",
                                                eachColumnConf.getString(Key.NAME),
                                                eachColumnConf.getString(Key.TYPE)));
                }

                columnTypeInspectors.add(objectInspector);
            }
        }
        return columnTypeInspectors;
    }

    public OrcSerde getOrcSerde(Configuration config) {
        String fieldDelimiter = config.getString(Key.FIELD_DELIMITER);
        String compress = config.getString(Key.COMPRESS);
        String encoding = config.getString(Key.ENCODING);

        OrcSerde orcSerde = new OrcSerde();
        Properties properties = new Properties();
        properties.setProperty("orc.bloom.filter.columns", fieldDelimiter);
        properties.setProperty("orc.compress", compress);
        properties.setProperty("orc.encoding.strategy", encoding);

        orcSerde.initialize(conf, properties);
        return orcSerde;
    }

    private static MutablePair<List<Object>, Boolean> transportOneRecord(
            Record record, List<Configuration> columnsConfiguration,
            TaskPluginCollector taskPluginCollector) {

        MutablePair<List<Object>, Boolean> transportResult = new MutablePair<List<Object>, Boolean>();
        transportResult.setRight(false);
        List<Object> recordList = Lists.newArrayList();
        int recordLength = record.getColumnNumber();
        if (0 != recordLength) {
            Column column;
            for (int i = 0; i < recordLength; i++) {
                column = record.getColumn(i);
                if (null != column.getRawData()) {
                    if(null == columnsConfiguration){
                        recordList.add(column.asString());
                        continue;
                    }
                    String rowData = column.getRawData().toString();
                    SupportHiveDataType columnType = SupportHiveDataType.valueOf(
                            columnsConfiguration.get(i).getString(Key.TYPE).toUpperCase());
                    //根据writer端类型配置做类型转换
                    try {
                        switch (columnType) {
                            case TINYINT:
                                recordList.add(Byte.valueOf(rowData));
                                break;
                            case SMALLINT:
                                recordList.add(Short.valueOf(rowData));
                                break;
                            case INT:
                                recordList.add(Integer.valueOf(rowData));
                                break;
                            case BIGINT:
                                recordList.add(column.asLong());
                                break;
                            case FLOAT:
                                recordList.add(Float.valueOf(rowData));
                                break;
                            case DOUBLE:
                                recordList.add(column.asDouble());
                                break;
                            case STRING:
                            case VARCHAR:
                            case ARRAY:
                            case MAP:
                            case CHAR:
                                recordList.add(column.asString());
                                break;
                            case BOOLEAN:
                                recordList.add(column.asBoolean());
                                break;
                            case DATE:
                                recordList.add(new java.sql.Date(column.asDate().getTime()));
                                break;
                            case TIMESTAMP:
                                recordList.add(new java.sql.Timestamp(column.asDate().getTime()));
                                break;
                            default:
                                throw DataXException
                                        .asDataXException(
                                                HdfsWriterErrorCode.ILLEGAL_VALUE,
                                                String.format(
                                                        "您的配置文件中的列配置信息有误. 因为DataX 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%s]. 请修改表中该字段的类型或者不同步该字段.",
                                                        columnsConfiguration.get(i).getString(Key.NAME),
                                                        columnsConfiguration.get(i).getString(Key.TYPE)));
                        }
                    } catch (Exception e) {
                        // warn: 此处认为脏数据
                        String message = String.format(
                                "字段类型转换错误：你目标字段为[%s]类型，实际字段值为[%s].",
                                columnsConfiguration.get(i).getString(Key.TYPE), column.getRawData().toString());
                        taskPluginCollector.collectDirtyRecord(record, message);
                        transportResult.setRight(true);
                        break;
                    }
                } else {
                    // warn: it's all ok if nullFormat is null
                    recordList.add(null);
                }
            }
        }
        transportResult.setLeft(recordList);
        return transportResult;
    }

    private UserGroupInformation getUgiByKerberos(
            org.apache.hadoop.conf.Configuration conf,
            String kerberosPrincipal, String kerberosKeytabFilePath){
        return kerberosAuthentication(conf, kerberosPrincipal, kerberosKeytabFilePath);
    }

    private UserGroupInformation getUgiInAuth(Configuration taskConfig){
        String userName = taskConfig.getString(Key.LDAP_USERNAME, "");
        String password = taskConfig.getString(Key.LDAP_USERPASSWORD, "");
        if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
            try {
                password = (String) CryptoUtils.string2Object(password);
            } catch (Exception e) {
                LOG.error("Fail to decrypt password", e);
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONFIG_INVALID_EXCEPTION, e);
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
                    throw DataXException.asDataXException(HdfsWriterErrorCode.CONFIG_INVALID_EXCEPTION, "LDAP authenticate fail");
                }
            }else{
                throw DataXException.asDataXException(HdfsWriterErrorCode.CONFIG_INVALID_EXCEPTION, "Engine need LDAP configuration");
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
            throw DataXException.asDataXException(HdfsWriterErrorCode.HDFS_PROXY_ERROR, e);
        }
        return ugi;
    }

    private void transformOrcStruct( MutablePair<List<Object>, Boolean> transportResult,
                                     List<Configuration> columns){
        //deal the columns whose type is MAP or ARRAY
        List<Object> result = transportResult.getLeft();
        for(int i = 0; i < columns.size(); i++) {
            Configuration column = columns.get(i);
            SupportHiveDataType columnType = SupportHiveDataType.valueOf(column.getString(Key.TYPE).toUpperCase());
            String split = ",";
            if (columnType == SupportHiveDataType.ARRAY) {
                String array = result.get(i).toString().trim();
                if (array.startsWith("[") && array.endsWith("]")) {
                    array = array.substring(1, array.length() - 1);
                    String[] items  = array.split(split);
                    for (int t = 0; t < items.length; t++){
                        items[t] = StringUtils.join(new String[]{"\"",items[t] ,"\""}, "");
                    }
                    List<String> list = JSON.parseArray("[" + StringUtils.join(items, split) + "]", String.class);
                    List<Text> listText = new ArrayList<>();
                    list.forEach(value -> listText.add(new Text(value)));
                    result.set(i, listText);
                }
            } else if (columnType == SupportHiveDataType.MAP) {
                String map = result.get(i).toString().trim();
                if (map.startsWith("{") && map.endsWith("}")) {
                    map = map.substring(1, map.length() - 1);
                    String[] entries = map.split(split);
                    for(int t = 0; t < entries.length; t++){
                        String[] attrs = entries[t].split("=");
                        if(attrs.length >= 2){
                            entries[t] = StringUtils.join
                                    (new String[]{"\"", attrs[0],"\":\"", attrs[1], "\""}, "");
                        }
                    }
                    Map map1 = JSON.parseObject("{" + StringUtils.join(entries, split) + "}", Map.class);
                    Map<Text, Text> mapText = new HashMap<>();
                    if(null != map1) {
                        map1.forEach((k, v) -> mapText.put(new Text((String) k), new Text((String) v)));
                    }
                    result.set(i, mapText);
                }
            }
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
            hiveConf.setBoolVar(HiveConf.ConfVars.METASTORE_USE_THRIFT_SASL, true);
            hiveConf.set("hadoop.security.authentication", "kerberos");
            hiveConf.setVar(HiveConf.ConfVars.METASTORE_KERBEROS_PRINCIPAL,
                    writerConfig.getString(HIVE_KEBEROS_PRINCIPAL, DEFAULT_HIVE_USER+ "/_HOST@EXAMPLE.COM"));
            hiveUgi = getUgiByKerberos(hiveConf, this.kerberosPrincipal, this.kerberosKeytabFilePath);
        }else{
            hiveUgi = getUgiInAuth(this.writerConfig);
        }
        return hiveUgi.doAs((PrivilegedExceptionAction<Hive>) () -> {
            Hive hive1 = Hive.get(hiveConf);
            hive1.getMSC();
            return hive1;
        });
    }
    /**
     * add hive partition
     *
     * @param hive hive
     * @param partitionValues
     */
    private void addHiveTablePartitions(Hive hive, String database, String table, String partitionValues) {
        try {
            String[] parts = partitionValues.split(",");
            List<String> partVal = Arrays.asList(parts);
            org.apache.hadoop.hive.ql.metadata.Table t = hive.getTable(database, table);
            Partition partition = new Partition();
            partition.setDbName(database);
            partition.setTableName(t.getTableName());
            StorageDescriptor partitionSd = new StorageDescriptor(t.getSd());
            List<FieldSchema> partitionKeys = t.getPartitionKeys();
            partitionKeys = partitionKeys.subList(0, partVal.size());
            String location = t.getPath().toUri().getPath() + Path.SEPARATOR + Warehouse.makePartName(partitionKeys, partVal);
            Path p = new Path(location);
            if (!fileSystem.exists(p)) {
                //build directory by runtime user
                fileSystem.mkdirs(p);
            }
            partitionSd.setLocation(location);
            partition.setSd(partitionSd);
            partition.setValues(partVal);
            List<Partition> partitions = new ArrayList<>();
            partitions.add(partition);
            hive.getMSC().add_partitions(partitions, true, true);
        } catch (Exception e) {
            LOG.error("Add table partition error.", e);
            throw DataXException.asDataXException(HdfsWriterErrorCode.ADD_PARTITION_ERROR, e.getMessage());
        }
    }

    private UserGroupInformation kerberosAuthentication(org.apache.hadoop.conf.Configuration conf,
                                                        String kerberosPrincipal, String kerberosKeytabFilePath) {
        UserGroupInformation ugi = null;
        if (StringUtils.isNotBlank(kerberosPrincipal) && StringUtils.isNotBlank(kerberosKeytabFilePath)) {
            UserGroupInformation.setConfiguration(conf);
            try {
                ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(kerberosPrincipal
                        .substring(0, kerberosPrincipal.indexOf("@")), kerberosKeytabFilePath);
            } catch (Exception e) {
                String message = String.format("kerberos认证失败,请确定kerberosKeytabFilePath[%s]和kerberosPrincipal[%s]填写正确",
                        kerberosKeytabFilePath, kerberosPrincipal);
                LOG.error(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.KERBEROS_LOGIN_ERROR, e);
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
        static final Map<String, HiveFileType> OUTPUT_FORMAT = new HashMap<>();
        static{
            OUTPUT_FORMAT.put(new TextFileStorageFormatDescriptor().getOutputFormat(), TEXT);
            OUTPUT_FORMAT.put(new ORCFileStorageFormatDescriptor().getOutputFormat(), ORC);
            OUTPUT_FORMAT.put(new AvroStorageFormatDescriptor().getOutputFormat(), AVRO);
            OUTPUT_FORMAT.put(new ParquetFileStorageFormatDescriptor().getOutputFormat(), PARQUET);
            OUTPUT_FORMAT.put(new RCFileStorageFormatDescriptor().getOutputFormat(), RC);
            OUTPUT_FORMAT.put(new SequenceFileStorageFormatDescriptor().getOutputFormat(), SEQ);
        }

        static HiveFileType output(String outputStreamFormat){
            return OUTPUT_FORMAT.get(outputStreamFormat);
        }
    }

    /**
     * detect the file type
     * @param tableDescriptor tableDescriptor
     * @return
     */
    private String detectFileType(StorageDescriptor tableDescriptor){
        //search file type by output format of table/partition
        return HiveFileType.output(tableDescriptor.getOutputFormat()) != null ? HiveFileType.output(tableDescriptor.getOutputFormat()).toString() : "";
    }
}
