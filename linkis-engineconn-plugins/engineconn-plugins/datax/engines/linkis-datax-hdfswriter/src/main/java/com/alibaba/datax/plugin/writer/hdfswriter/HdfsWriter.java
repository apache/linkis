package com.alibaba.datax.plugin.writer.hdfswriter;

import com.alibaba.datax.common.constant.CommonConstant;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.CompressSuffixName;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.writer.Constant;
import com.alibaba.datax.plugin.unstructuredstorage.writer.UnstructuredStorageWriterUtil;
import com.google.common.collect.Sets;
import com.webank.wedatasphere.linkis.datax.common.constant.TransportType;
import com.webank.wedatasphere.linkis.datax.core.job.meta.MetaSchema;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ChannelInput;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.StreamMeta;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivilegedAction;
import java.util.*;


public class HdfsWriter extends Writer {
    public static class Job extends Writer.Job {
        private static final Logger LOG = LoggerFactory.getLogger(Job.class);

        private Configuration writerSliceConfig = null;

        private String defaultFS;
        private String path;
        private String fileType;
        private String fileName;
        private List<Configuration> columns;
        private String writeMode;
        private String fieldDelimiter;
        private String compress;
        private String encoding;

        private String tempPath;
        /**
         * 临时文件全路径
         */
        private HashSet<String> tmpFiles = new HashSet<>();
        /**
         * 最终文件全路径
         */
        private HashSet<String> endFiles = new HashSet<>();

        private HdfsWriterUtil hdfsWriterUtil = null;

        @Override
        public boolean isSupportStream() {
            return true;
        }

        @Override
        public void init() {
            this.writerSliceConfig = this.getPluginJobConf();
            this.validateParameter();

            //创建textfile存储
            hdfsWriterUtil = new HdfsWriterUtil();

            hdfsWriterUtil.getFileSystem(defaultFS, this.writerSliceConfig);

        }

        @Override
        public void syncMetaData(MetaSchema metaSchema) {
            if(StringUtils.isNotBlank(writerSliceConfig.getString(Key.HIVE_METASTORE_URIS, ""))){
                hdfsWriterUtil.updateHiveMetaData(writerSliceConfig.getString(Key.HIVE_DATABASE),
                        writerSliceConfig.getString(Key.HIVE_TABLE),
                        writerSliceConfig.getString(Key.HIVE_METASTORE_URIS),
                        metaSchema);
            }
        }

        private void validateParameter() {
            this.defaultFS = this.writerSliceConfig.getNecessaryValue(Key.DEFAULT_FS, HdfsWriterErrorCode.REQUIRED_VALUE);
            //fileType check
            if(getTransportType() == TransportType.RECORD) {
                this.fileType = this.writerSliceConfig.getNecessaryValue(Key.FILE_TYPE, HdfsWriterErrorCode.REQUIRED_VALUE);
                if (!fileType.equalsIgnoreCase("ORC") && !fileType.equalsIgnoreCase("TEXT")) {
                    String message = "HdfsWriter插件目前只支持ORC和TEXT两种格式的文件,请将filetype选项的值配置为ORC或者TEXT";
                    throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
                }
                //columns check
                this.columns = this.writerSliceConfig.getListConfiguration(Key.COLUMN);
                if (null != columns && columns.size() > 0) {
                    for (Configuration eachColumnConf : columns) {
                        eachColumnConf.getNecessaryValue(Key.NAME, HdfsWriterErrorCode.COLUMN_REQUIRED_VALUE);
                        eachColumnConf.getNecessaryValue(Key.TYPE, HdfsWriterErrorCode.COLUMN_REQUIRED_VALUE);
                    }
                }
                //fieldDelimiter check
                this.fieldDelimiter = this.writerSliceConfig.getString(Key.FIELD_DELIMITER, ",");
                if (null == fieldDelimiter) {
                    throw DataXException.asDataXException(HdfsWriterErrorCode.REQUIRED_VALUE,
                            String.format("您提供配置文件有误，[%s]是必填参数.", Key.FIELD_DELIMITER));
                }
            }
            //path
            this.path = this.writerSliceConfig.getNecessaryValue(Key.PATH, HdfsWriterErrorCode.REQUIRED_VALUE);
            if (!path.startsWith("/")) {
                String message = String.format("请检查参数path:[%s],需要配置为绝对路径", path);
                LOG.error(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
            } else if (path.contains("*") || path.contains("?")) {
                String message = String.format("请检查参数path:[%s],不能包含*,?等特殊字符", path);
                LOG.error(message);
                throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE, message);
            }
            //fileName
            this.fileName = this.writerSliceConfig.getString(Key.FILE_NAME, "");
            //writeMode check
            this.writeMode = this.writerSliceConfig.getNecessaryValue(Key.WRITE_MODE, HdfsWriterErrorCode.REQUIRED_VALUE);
            writeMode = writeMode.toLowerCase().trim();
            Set<String> supportedWriteModes = Sets.newHashSet("append", "nonconflict", "truncate");
            if (!supportedWriteModes.contains(writeMode)) {
                throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                        String.format("仅支持append, nonConflict,truncate模式, 不支持您配置的 writeMode 模式 : [%s]",
                                writeMode));
            }
            this.writerSliceConfig.set(Key.WRITE_MODE, writeMode);
            //compress check
            this.compress = this.writerSliceConfig.getString(Key.COMPRESS, null);
            if(StringUtils.isNotBlank(fileType)) {
                if (fileType.equalsIgnoreCase("TEXT")) {
                    Set<String> textSupportedCompress = Sets.newHashSet("GZIP", "BZIP2");
                    //用户可能配置的是compress:"",空字符串,需要将compress设置为null
                    if (StringUtils.isBlank(compress)) {
                        this.writerSliceConfig.set(Key.COMPRESS, null);
                    } else {
                        compress = compress.toUpperCase().trim();
                        if (!textSupportedCompress.contains(compress)) {
                            throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                                    String.format("目前TEXT FILE仅支持GZIP、BZIP2 两种压缩, 不支持您配置的 compress 模式 : [%s]",
                                            compress));
                        }
                    }
                } else if (fileType.equalsIgnoreCase("ORC")) {
                    Set<String> orcSupportedCompress = Sets.newHashSet("NONE", "SNAPPY");
                    if (null == compress) {
                        this.writerSliceConfig.set(Key.COMPRESS, "NONE");
                    } else {
                        compress = compress.toUpperCase().trim();
                        if (!orcSupportedCompress.contains(compress)) {
                            throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                                    String.format("目前ORC FILE仅支持SNAPPY压缩, 不支持您配置的 compress 模式 : [%s]",
                                            compress));
                        }
                    }

                }
            }
            //Kerberos check
            Boolean haveKerberos = this.writerSliceConfig.getBool(Key.HAVE_KERBEROS, false);
            if (haveKerberos) {
                this.writerSliceConfig.getNecessaryValue(Key.KERBEROS_KEYTAB_FILE_PATH, HdfsWriterErrorCode.REQUIRED_VALUE);
                this.writerSliceConfig.getNecessaryValue(Key.KERBEROS_PRINCIPAL, HdfsWriterErrorCode.REQUIRED_VALUE);
            }
            // encoding check
            this.encoding = this.writerSliceConfig.getString(Key.ENCODING, Constant.DEFAULT_ENCODING);
            try {
                encoding = encoding.trim();
                this.writerSliceConfig.set(Key.ENCODING, encoding);
                Charsets.toCharset(encoding);
            } catch (Exception e) {
                throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                        String.format("不支持您配置的编码格式:[%s]", encoding), e);
            }
        }

        @Override
        public void prepare() {
            if(StringUtils.isNotBlank(writerSliceConfig.getString(Key.HIVE_METASTORE_URIS, ""))){
                LOG.info("update the configuration dynamically by hive meta...");
                boolean affected = hdfsWriterUtil.updateConfigByHiveMeta(
                        writerSliceConfig.getString(Key.HIVE_DATABASE),
                        writerSliceConfig.getString(Key.HIVE_TABLE),
                        writerSliceConfig.getString(Key.HIVE_METASTORE_URIS),
                        this.writerSliceConfig
                );
                if(affected){
                    //validate the configuration again
                    this.validateParameter();
                }
            }
            //若路径已经存在，检查path是否是目录
            if (hdfsWriterUtil.isPathexists(path)) {
                if (!hdfsWriterUtil.isPathDir(path)) {
                    throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                            String.format("您配置的path: [%s] 不是一个合法的目录, 请您注意文件重名, 不合法目录名等情况.",
                                    path));
                }
                //根据writeMode对目录下文件进行处理
                String[] pathList = hdfsWriterUtil.hdfsDirList(path);
                Path[] existFilePaths  = new Path[pathList.length];
                for(int i = 0; i < pathList.length; i ++){
                    existFilePaths[i] = new Path(pathList[i]);
                }
                boolean isExistFile = false;
                if (existFilePaths.length > 0) {
                    isExistFile = true;
                }

                if ("truncate".equals(writeMode) && isExistFile) {
                    LOG.info(String.format("由于您配置了writeMode truncate, 开始清理 [%s] 下面的内容",
                            path));
                    hdfsWriterUtil.deleteFiles(existFilePaths);
                } else if ("append".equalsIgnoreCase(writeMode)) {
                    LOG.info(String.format("由于您配置了writeMode append, 写入前不做清理工作",
                            path, fileName));
                } else if ("nonconflict".equalsIgnoreCase(writeMode) && isExistFile) {
                    LOG.info(String.format("由于您配置了writeMode nonConflict, 开始检查 [%s] 下面的内容", path));
                    List<String> allFiles = new ArrayList<String>();
                    for (Path eachFile : existFilePaths) {
                        allFiles.add(eachFile.toString());
                    }
                    LOG.error(String.format("冲突文件列表为: [%s]", StringUtils.join(allFiles, ",")));
                    throw DataXException.asDataXException(HdfsWriterErrorCode.ILLEGAL_VALUE,
                            String.format("由于您配置了writeMode nonConflict,但您配置的path: [%s] 目录不为空, 下面存在其他文件或文件夹.", path));
                }
            } else {
                hdfsWriterUtil.mkdirs(new Path(path));
            }
        }

        @Override
        public void post() {
            //reset configuration
            UserGroupInformation.setConfiguration(hdfsWriterUtil.hadoopConf);
            if(!tmpFiles.isEmpty() && !endFiles.isEmpty()) {
                hdfsWriterUtil.renameFile(tmpFiles, endFiles);
            }else if (StringUtils.isNotBlank(this.tempPath)) {
                try{
                    LOG.info(String.format("move files or directories under temporary path: %s to path: %s", tempPath, path));
                    hdfsWriterUtil.moveToDirectory(Arrays.asList(hdfsWriterUtil.hdfsDirList(this.tempPath)), this.path);
                }finally {
                    LOG.info(String.format("delete temporary path : %s", tempPath));
                    hdfsWriterUtil.deleteDir(new Path(this.tempPath));
                    this.tempPath = null;
                }
            }
        }

        @Override
        public void destroy() {
            if(StringUtils.isNotBlank(this.tempPath)){
                hdfsWriterUtil.deleteDir(new Path(this.tempPath));
            }
            hdfsWriterUtil.closeFileSystem();
        }


        @Override
        public List<Configuration> split(int mandatoryNumber) {
            LOG.info("begin do split...");
            List<Configuration> writerSplitConfigs = new ArrayList<Configuration>();
            String filePrefix = fileName;

            Set<String> allFiles = new HashSet<String>();
            //获取该路径下的所有已有文件列表
            if (hdfsWriterUtil.isPathexists(path)) {
                allFiles.addAll(Arrays.asList(hdfsWriterUtil.hdfsDirList(path)));
            }

            String fileSuffix;
            //临时存放路径
            String storePath = UnstructuredStorageWriterUtil
                    .buildTmpFilePath(this.path, String.format(CommonConstant.TEMP_PREFIX, System.currentTimeMillis()),
                            IOUtils.DIR_SEPARATOR_UNIX,
                            path -> hdfsWriterUtil.isPathexists(path));
            this.tempPath = storePath;
            //最终存放路径
            String endStorePath = buildFilePath();
            this.path = endStorePath;
            for (int i = 0; i < mandatoryNumber; i++) {
                // handle same file name
                Configuration splitedTaskConfig = this.writerSliceConfig.clone();
                String fullFileName;
                if(getTransportType() == TransportType.RECORD) {
                    String endFullFileName = null;
                    do{
                        fileSuffix = UUID.randomUUID().toString().replace('-', '_');
                        fullFileName = String.format("%s%s%s__%s", defaultFS, storePath, filePrefix, fileSuffix);
                        endFullFileName = String.format("%s%s%s__%s", defaultFS, endStorePath, filePrefix, fileSuffix);
                    }while(allFiles.contains(endFullFileName));
                    allFiles.add(endFullFileName);
                    String suffix = CompressSuffixName.chooseSuffix(this.compress);
                    if(StringUtils.isNotBlank(suffix)){
                        fullFileName += suffix;
                        endFullFileName += suffix;
                    }
                    this.tmpFiles.add(fullFileName);
                    this.endFiles.add(endFullFileName);
                    LOG.info(String.format("splited write file name:[%s]",
                            fullFileName));
                }else{
                    fullFileName = String.format("%s%s%s", defaultFS , storePath ,fileName);
                }
                splitedTaskConfig
                        .set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FILE_NAME,
                                fullFileName);
                splitedTaskConfig.set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.TEMP_PATH,
                        this.tempPath);
                writerSplitConfigs.add(splitedTaskConfig);
            }
            LOG.info("end do split.");
            return writerSplitConfigs;
        }

        private String buildFilePath() {
            if (!this.path.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))) {
                this.path = this.path + IOUtils.DIR_SEPARATOR_UNIX;
            }
            return this.path;
        }

    }

    public static class Task extends Writer.Task {
        private static final Logger LOG = LoggerFactory.getLogger(Task.class);

        private Configuration writerSliceConfig;

        private String defaultFS;
        private String fileType;
        private String fileName;

        private HdfsWriterUtil hdfsWriterUtil = null;

        @Override
        public void init() {
            this.writerSliceConfig = this.getPluginJobConf();

            this.defaultFS = this.writerSliceConfig.getString(Key.DEFAULT_FS);
            this.fileType = this.writerSliceConfig.getString(Key.FILE_TYPE);
            this.fileName = this.writerSliceConfig.getString(Key.FILE_NAME);
            hdfsWriterUtil = new HdfsWriterUtil();
            hdfsWriterUtil.getFileSystem(defaultFS, writerSliceConfig);
        }

        @Override
        public void prepare() {

        }

        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            LOG.info("begin do write...");
            hdfsWriterUtil.getUgi().doAs((PrivilegedAction<Object>) () -> {
                LOG.info(String.format("write to file : [%s]", fileName));
                if (fileType.equalsIgnoreCase("TEXT")) {
                    //write TXT FILE, you should remove you file's extension before
                    int extIndex = fileName.lastIndexOf(".");
                    if(extIndex > 0 && fileName.substring(extIndex + 1).lastIndexOf(IOUtils.DIR_SEPARATOR_UNIX) < 0){
                        fileName = fileName.substring(0, extIndex);
                    }
                    hdfsWriterUtil.textFileStartWrite(lineReceiver, writerSliceConfig,
                            fileName,
                            getTaskPluginCollector());
                } else if (fileType.equalsIgnoreCase("ORC")) {
                    //写ORC FILE
                    hdfsWriterUtil.orcFileStartWrite(lineReceiver, writerSliceConfig, fileName,
                           getTaskPluginCollector());
                }
                return null;
            });
            LOG.info("end do write");
        }


        @Override
        public void startWrite(ChannelInput channelInput) {
            LOG.info("begin do write from stream channel...");
            String finalPathPrefix = fileName;
            hdfsWriterUtil.getUgi().doAs((PrivilegedAction<Object>) () -> {
                try{
                    InputStream inputStream = null;
                    while((inputStream = channelInput.nextStream()) != null){
                        StreamMeta streamMeta = channelInput.streamMetaData(this.writerSliceConfig.getString
                                (Key.ENCODING, Constant.DEFAULT_ENCODING));
                        LOG.info("begin do read input stream, name : " + streamMeta.getName() + ", relativePath:" + streamMeta.getRelativePath());
                        String relativePath = streamMeta.getRelativePath();
                        String pathPrefix = finalPathPrefix;
                        if(!pathPrefix.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR))){
                            //means that having the fileNamePrefix
                            String fileNamePrefix = pathPrefix.substring(pathPrefix.lastIndexOf(IOUtils.DIR_SEPARATOR) + 1);
                            pathPrefix =  pathPrefix.substring(0, pathPrefix.lastIndexOf(IOUtils.DIR_SEPARATOR) + 1);
                            //modify the relativePath
                            relativePath = relativePath.substring(0, relativePath.lastIndexOf(IOUtils.DIR_SEPARATOR) + 1)
                                    + fileNamePrefix + "_" + streamMeta.getName();
                        }
                        String destPath = pathPrefix + relativePath;
                        FSDataOutputStream outputStream = null;
                        try{
                            outputStream = hdfsWriterUtil.fileSystem.create(new Path(destPath), true);
                            UnstructuredStorageWriterUtil.writeToStream(inputStream, outputStream,
                                    this.writerSliceConfig);
                            outputStream.flush();
                        }finally{
                            if(null != outputStream){
                                IOUtils.closeQuietly(outputStream);
                            }
                        }
                    }
                }catch(IOException e){
                    throw DataXException.asDataXException(FrameworkErrorCode.CHANNEL_STREAM_ERROR, e);
                }
               return null;
            });
            LOG.info("end do write from stream channel");
        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }
    }
}
