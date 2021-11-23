package com.alibaba.datax.plugin.reader.hdfsreader;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.spi.Reader;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.PathMeta;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.wedatasphere.linkis.datax.common.constant.TransportType;
import com.webank.wedatasphere.linkis.datax.core.job.meta.MetaSchema;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ChannelOutput;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.StreamMeta;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.security.UserGroupInformation;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INCR_BEGIN_TIME;
import static com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INCR_END_TIME;

public class HdfsReader extends Reader {

    /**
     * Job 中的方法仅执行一次，task 中方法会由框架启动多个 task 线程并行执行。
     * <p/>
     * 整个 Reader 执行流程是：
     * <pre>
     * Job类init-->prepare-->split
     *
     * Task类init-->prepare-->startRead-->post-->destroy
     * Task类init-->prepare-->startRead-->post-->destroy
     *
     * Job类post-->destroy
     * </pre>
     */
    public static class Job extends Reader.Job {
        private static final Logger LOG = LoggerFactory
                .getLogger(Job.class);

        private Configuration readerOriginConfig = null;
        private String encoding = null;
        private HashSet<PathMeta> sourceFiles = new HashSet<>();
        private String specifiedFileType = null;
        private HdfsReaderUtil hdfsReaderUtil = null;
        private List<String> path = null;
        private long incrBeginTime = 0;
        private long incrEndTime = 0;
        private String defaultFS;
        org.apache.hadoop.conf.Configuration hadoopConf = null;
        private UserGroupInformation ugi = null;
        @Override
        public boolean isSupportStream(){
            return true;
        }



        @Override
        public void init() {

            LOG.info("init() begin...");
            this.readerOriginConfig = super.getPluginJobConf();
            this.validate();
            hdfsReaderUtil = new HdfsReaderUtil(this.readerOriginConfig);
            LOG.info("init() ok and end...");
            for(String eachPath : path) {
                if (!hdfsReaderUtil.exists(eachPath)) {
                    String message = String.format("cannot find the path: [%s], please check your configuration", eachPath);
                    LOG.error(message);
                    throw DataXException.asDataXException(HdfsReaderErrorCode.PATH_NOT_FOUND, message);
                }
            }
        }

        @Override
        public MetaSchema syncMetaData() {
            if(StringUtils.isNotBlank(readerOriginConfig.getString(Key.HIVE_METASTORE_URIS, ""))){
                return hdfsReaderUtil.getHiveMetadata(
                        readerOriginConfig.getString(Key.HIVE_DATABASE),
                        readerOriginConfig.getString(Key.HIVE_TABLE),
                        readerOriginConfig.getString(Key.HIVE_METASTORE_URIS)
                );
            }
            return null;
        }

        private void validate() {
            this.defaultFS = this.readerOriginConfig.getNecessaryValue(Key.DEFAULT_FS,
                    HdfsReaderErrorCode.DEFAULT_FS_NOT_FIND_ERROR);
            // path check
            String pathInString = this.readerOriginConfig.getNecessaryValue(Key.PATH, HdfsReaderErrorCode.REQUIRED_VALUE);
            if (!pathInString.startsWith("[") && !pathInString.endsWith("]")) {
                path = new ArrayList<String>();
                path.add(pathInString);
            } else {
                path = this.readerOriginConfig.getList(Key.PATH, String.class);
                if (null == path || path.size() == 0) {
                    throw DataXException.asDataXException(HdfsReaderErrorCode.REQUIRED_VALUE, "您需要指定待读取的源目录或文件");
                }
                for (String eachPath : path) {
                    if (!eachPath.startsWith("/")) {
                        String message = String.format("请检查参数path:[%s],需要配置为绝对路径", eachPath);
                        LOG.error(message);
                        throw DataXException.asDataXException(HdfsReaderErrorCode.ILLEGAL_VALUE, message);
                    }
                }
            }
            if(getTransportType() == TransportType.RECORD) {
                specifiedFileType = this.readerOriginConfig.getNecessaryValue(Key.FILETYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
                if (!specifiedFileType.equalsIgnoreCase(Constant.ORC) &&
                        !specifiedFileType.equalsIgnoreCase(Constant.TEXT) &&
                        !specifiedFileType.equalsIgnoreCase(Constant.CSV) &&
                        !specifiedFileType.equalsIgnoreCase(Constant.SEQ) &&
                        !specifiedFileType.equalsIgnoreCase(Constant.RC)  &&
                        !specifiedFileType.equalsIgnoreCase(Constant.HFILE)) {
                    String message = "HdfsReader插件目前支持ORC, TEXT, CSV, SEQUENCE, RC, HFile 格式的文件," +
                            "请将fileType选项的值配置为ORC, TEXT, CSV, SEQUENCE, HFile 或者 RC";
                    throw DataXException.asDataXException(HdfsReaderErrorCode.FILE_TYPE_ERROR, message);
                }
                if (this.specifiedFileType.equalsIgnoreCase(Constant.CSV)) {
                    //compress校验
                    UnstructuredStorageReaderUtil.validateCompress(this.readerOriginConfig);
                    UnstructuredStorageReaderUtil.validateCsvReaderConfig(this.readerOriginConfig);
                }
            }

            encoding = this.readerOriginConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING, "UTF-8");

            try {
                Charsets.toCharset(encoding);
            } catch (UnsupportedCharsetException uce) {
                throw DataXException.asDataXException(
                        HdfsReaderErrorCode.ILLEGAL_VALUE,
                        String.format("不支持的编码格式 : [%s]", encoding), uce);
            } catch (Exception e) {
                throw DataXException.asDataXException(
                        HdfsReaderErrorCode.ILLEGAL_VALUE,
                        String.format("运行配置异常 : %s", e.getMessage()), e);
            }
            //check Kerberos
            Boolean haveKerberos = this.readerOriginConfig.getBool(Key.HAVE_KERBEROS, false);
            if (haveKerberos) {
                this.readerOriginConfig.getNecessaryValue(Key.KERBEROS_KEYTAB_FILE_PATH, HdfsReaderErrorCode.REQUIRED_VALUE);
                this.readerOriginConfig.getNecessaryValue(Key.KERBEROS_PRINCIPAL, HdfsReaderErrorCode.REQUIRED_VALUE);
            }
            this.incrBeginTime = this.readerOriginConfig.getLong(INCR_BEGIN_TIME, 0);
            this.incrEndTime = this.readerOriginConfig.getLong(INCR_END_TIME, DateTime.now().getMillis());
            // validate the Columns
            validateColumns();

        }

        private void validateColumns() {
            List<Configuration> column = this.readerOriginConfig
                    .getListConfiguration(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN);
            if(null == column){
                column = new ArrayList<>();
            }
            boolean emptyColumn = column.isEmpty() || (1 == column.size() && ("\"*\"".equals(column.get(0).toString()) || "'*'"
                    .equals(column.get(0).toString())));
            if (emptyColumn) {
                this.readerOriginConfig
                        .set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN, new ArrayList<String>());
            } else {
                // column: 1. index type 2.value type 3.when type is Data, may have format
                List<Configuration> columns = this.readerOriginConfig
                        .getListConfiguration(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN);
                if (null != columns && columns.size() != 0) {
                    for (Configuration eachColumnConf : columns) {
                        eachColumnConf.getNecessaryValue(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.TYPE, HdfsReaderErrorCode.REQUIRED_VALUE);
                        Integer columnIndex = eachColumnConf.getInt(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INDEX);
                        String columnValue = eachColumnConf.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.VALUE);

                        if (null == columnIndex && null == columnValue) {
                            throw DataXException.asDataXException(
                                    HdfsReaderErrorCode.NO_INDEX_VALUE,
                                    "由于您配置了type, 则至少需要配置 index 或 value");
                        }

                        if (null != columnIndex && null != columnValue) {
                            throw DataXException.asDataXException(
                                    HdfsReaderErrorCode.MIXED_INDEX_VALUE,
                                    "您混合配置了index, value, 每一列同时仅能选择其中一种");
                        }

                    }
                }
            }
        }

        @Override
        public void prepare() {
            if(StringUtils.isNotBlank(readerOriginConfig.getString(Key.HIVE_METASTORE_URIS, ""))) {
                LOG.info("update the configuration dynamically by hive meta...");
                boolean affected = hdfsReaderUtil.updateConfigByHiveMeta( readerOriginConfig.getString(Key.HIVE_DATABASE),
                        readerOriginConfig.getString(Key.HIVE_TABLE),
                        readerOriginConfig.getString(Key.HIVE_METASTORE_URIS), this.readerOriginConfig);
                if(affected){
                    //validate the configuration again
                    this.validate();
                }
            }
            LOG.info("start to getAllFiles...");
            HashSet<String> sourceFiles0 = hdfsReaderUtil.getAllFiles(path, specifiedFileType);
            //to find the parent directory of path
            Set<String> parents = new HashSet<>();
            for(String path0 : path){
                boolean find = false;
                for(int i = 0; i < path0.length(); i++){
                    if('*' == path0.charAt(i) || '?' == path0.charAt(i)){
                        int lastDirSeparator = path0.substring(0, i)
                                .lastIndexOf(IOUtils.DIR_SEPARATOR);
                        parents.add(path0.
                                substring(0, lastDirSeparator + 1));
                        find = true;
                        break;
                    }
                }
                if(!find){
                    parents.add(path0);
                }
            }
            for(String sourceFile : sourceFiles0){
                if(getTransportType() == TransportType.STREAM ){
                    FileStatus status = hdfsReaderUtil.getFileStatus(sourceFile);
                    if(status.getModificationTime() <= incrBeginTime
                            || status.getModificationTime() > incrEndTime){
                        continue;
                    }
                }
                boolean find = false;
                for(String parent : parents){
                    //0: absolute path, 1: relative path
                    if(sourceFile.indexOf(parent) > 0){
                        String relativePath = sourceFile.substring(sourceFile.indexOf(parent) + parent.length());
                        if(StringUtils.isNotBlank(relativePath)){
                            this.sourceFiles.add(new PathMeta(sourceFile,
                                    relativePath));
                        }else{
                            this.sourceFiles.add(new PathMeta(sourceFile,
                                    parent.substring(parent.lastIndexOf(IOUtils.DIR_SEPARATOR))));
                        }
                        find = true;
                    }

                    if(find){
                        break;
                    }
                }
                if(!find){
                    throw new DataXException(FrameworkErrorCode.ARGUMENT_ERROR, "路径参数配置错误");
                }
            }
            String fileSeq = StringUtils.join(sourceFiles0, ",");
            if(fileSeq.length() > 30){
                fileSeq = fileSeq.substring(0, 30);
            }
            LOG.info(String.format("您即将读取的文件数为: [%s], 列表为: [%s]",
                    sourceFiles0.size(), fileSeq));
        }

        @Override
        public List<Configuration> split(int adviceNumber) {

            LOG.info("split() begin...");
            List<Configuration> readerSplitConfigs = new ArrayList<Configuration>();
            // warn:每个slice拖且仅拖一个文件,
            // int splitNumber = adviceNumber;
            int splitNumber = this.sourceFiles.size();
            if (0 == splitNumber) {
                return new ArrayList<>();
            }

            List<List<PathMeta>> splitedSourceFiles = this.splitSourceFiles(new ArrayList<>(this.sourceFiles), splitNumber);
            for (List<PathMeta> files : splitedSourceFiles) {
                Configuration splitedConfig = this.readerOriginConfig.clone();
                splitedConfig.set(Constant.SOURCE_FILES, files);
                readerSplitConfigs.add(splitedConfig);
            }

            return readerSplitConfigs;
        }


        private <T> List<List<T>> splitSourceFiles(final List<T> sourceList, int adviceNumber) {
            List<List<T>> splitedList = new ArrayList<List<T>>();
            int averageLength = sourceList.size() / adviceNumber;
            averageLength = averageLength == 0 ? 1 : averageLength;

            for (int begin = 0, end = 0; begin < sourceList.size(); begin = end) {
                end = begin + averageLength;
                if (end > sourceList.size()) {
                    end = sourceList.size();
                }
                splitedList.add(sourceList.subList(begin, end));
            }
            return splitedList;
        }


        @Override
        public void post() {

        }

        @Override
        public void destroy() {
            hdfsReaderUtil.closeFileSystem();
        }

    }

    public static class Task extends Reader.Task {

        private static Logger LOG = LoggerFactory.getLogger(Reader.Task.class);
        private Configuration taskConfig;
        private List<Object> sourceFiles;
        private String specifiedFileType;
        private String encoding;
        private HdfsReaderUtil hdfsReaderUtil = null;
        private int bufferSize;

        @Override
        public void init() {

            this.taskConfig = super.getPluginJobConf();
            this.sourceFiles = this.taskConfig.getList(Constant.SOURCE_FILES, Object.class);
            this.specifiedFileType = this.taskConfig.getString(Key.FILETYPE, "");
            this.encoding = this.taskConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING, "UTF-8");
            this.hdfsReaderUtil = new HdfsReaderUtil(this.taskConfig);
            this.bufferSize = this.taskConfig.getInt(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.BUFFER_SIZE,
                    com.alibaba.datax.plugin.unstructuredstorage.reader.Constant.DEFAULT_BUFFER_SIZE);
        }

        @Override
        public void prepare() {

        }


        @Override
        public void startRead(RecordSender recordSender) {
            LOG.info("Read start");
            hdfsReaderUtil.getUgi().doAs((PrivilegedAction<Object>) () -> {
                for (Object sourceFile : sourceFiles) {
                    PathMeta pathMeta = JSONObject.parseObject(JSON.toJSONString(sourceFile), PathMeta.class);
                    String fileName = pathMeta.getAbsolute();
                    LOG.info(String.format("Reading file : [%s]", fileName));

                    if (specifiedFileType.equalsIgnoreCase(Constant.TEXT)
                            || specifiedFileType.equalsIgnoreCase(Constant.CSV)) {
                        InputStream inputStream = hdfsReaderUtil.getInputStream(fileName);
                        if(null == taskConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COMPRESS, null)){
                            CompressionCodecFactory factory = new CompressionCodecFactory(hdfsReaderUtil.getConf());
                            try {
                                CompressionCodec codec = factory.getCodec(new Path(fileName));
                                if(null != codec){
                                    inputStream = codec.createInputStream(inputStream);
                                }
                            } catch (IOException e) {
                                throw DataXException.asDataXException(HdfsReaderErrorCode.READ_FILE_ERROR, "Hdfs使用压缩工厂类CodecFactory生成文件流失败,message:"
                                        + e.getMessage());
                            }
                        }
                        UnstructuredStorageReaderUtil.readFromStream(inputStream, fileName, taskConfig,
                                recordSender, getTaskPluginCollector());
                    } else if (specifiedFileType.equalsIgnoreCase(Constant.ORC)) {
                        hdfsReaderUtil.orcFileStartRead(fileName, taskConfig, recordSender, getTaskPluginCollector());
                    } else if (specifiedFileType.equalsIgnoreCase(Constant.SEQ)) {
                        hdfsReaderUtil.sequenceFileStartRead(fileName, taskConfig, recordSender, getTaskPluginCollector());
                    } else if (specifiedFileType.equalsIgnoreCase(Constant.RC)) {
                        hdfsReaderUtil.rcFileStartRead(fileName, taskConfig, recordSender, getTaskPluginCollector());
                    } else if (specifiedFileType.equalsIgnoreCase(Constant.HFILE)) {
                        hdfsReaderUtil.hFileStartRead(fileName, taskConfig, recordSender, getTaskPluginCollector());
                    }else{
                        String message = "HdfsReader插件目前支持ORC, TEXT, CSV, SEQUENCE, HFile, RC格式的文件," +
                                "请将fileType选项的值配置为ORC, TEXT, CSV, SEQUENCE, HFile或者 RC";
                        throw DataXException.asDataXException(HdfsReaderErrorCode.FILE_TYPE_UNSUPPORT, message);
                    }

                    if (recordSender != null) {
                        recordSender.flush();
                    }
                }
                return null;
            });


            LOG.info("end read source files...");
        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }

        @Override
        public void startRead(ChannelOutput channelOutput) {
            LOG.info("start read source HDFS files to stream channel...");
            hdfsReaderUtil.getUgi().doAs((PrivilegedAction<Object>) () ->{
                for(Object sourceFile : sourceFiles){
                    PathMeta pathMeta = JSONObject.parseObject(JSON.toJSONString(sourceFile), PathMeta.class);
                    String absolutePath = pathMeta.getAbsolute();
                    String relativePath = pathMeta.getRelative();
                    LOG.info(String.format("reading file : [%s]", absolutePath));
                    InputStream inputStream;
                    try{
                        Path path = new Path(absolutePath);
                        StreamMeta streamMeta = new StreamMeta();
                        streamMeta.setName(path.getName());
                        streamMeta.setAbsolutePath(absolutePath);
                        streamMeta.setRelativePath(relativePath);
                        OutputStream outputStream = channelOutput.createStream(streamMeta, encoding);
                        inputStream = hdfsReaderUtil.getInputStream(absolutePath);
                        UnstructuredStorageReaderUtil.readFromStream(inputStream, outputStream,
                                this.taskConfig);
                    }catch(IOException e){
                        throw DataXException.asDataXException(FrameworkErrorCode.CHANNEL_STREAM_ERROR, e);
                    }
                }
                return null;
            });
        }
    }

}