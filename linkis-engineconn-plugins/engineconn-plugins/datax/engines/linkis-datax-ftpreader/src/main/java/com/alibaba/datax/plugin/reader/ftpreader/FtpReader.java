package com.alibaba.datax.plugin.reader.ftpreader;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.spi.Reader;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.PathMeta;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.linkis.datax.common.GsonUtil;
import org.apache.linkis.datax.core.job.meta.MetaSchema;
import org.apache.linkis.datax.core.transport.stream.ChannelOutput;
import org.apache.linkis.datax.core.transport.stream.StreamMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INCR_BEGIN_TIME;
import static com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INCR_END_TIME;

public class FtpReader extends Reader {
    public static class Job extends Reader.Job {

        private static final String SIGNAL_FILE_NAME = ".ok";

        private static final String DEFAULT_META_FILE_PATH = ".meta";

        private static final long WAIT_SIGNAL_SLEEP_INTERVAL = 3000;

        private static final Logger LOG = LoggerFactory.getLogger(Job.class);

        private Configuration originConfig = null;

        private List<String> path = null;

        private HashSet<PathMeta> sourceFiles;

        /**
         * ftp connection parameters
         */
        private FtpConnParams connParams;

        private int maxTraversalLevel;

        private long incrBeginTime = 0;

        private long incrEndTime = 0;

        private FtpHelper ftpHelper = null;

        private String signalFilePath;

        @Override
        public boolean isSupportStream() {
            return true;
        }

        @Override
        public void init() {
            this.originConfig = this.getPluginJobConf();
            this.sourceFiles = new HashSet<>();
            this.validateParameter();
            UnstructuredStorageReaderUtil.validateParameter(this.originConfig);
            if ("sftp".equals(connParams.getProtocol())) {
                //sftp协议
                this.connParams.setPort(originConfig.getInt(Key.PORT, Constant.DEFAULT_SFTP_PORT));
                this.ftpHelper = new SftpHelper();
            } else if ("ftp".equals(connParams.getProtocol())) {
                // ftp 协议
                this.connParams.setPort(originConfig.getInt(Key.PORT, Constant.DEFAULT_FTP_PORT));
                this.ftpHelper = new StandardFtpHelper();
            }
            ftpHelper.loginFtpServer(this.connParams);
            for(String eachPath : path){
                boolean notFound = !ftpHelper.isDirExist(eachPath) &&
                        (eachPath.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX)) || !ftpHelper.isFileExist(eachPath));
                if(notFound){
                    String message = String.format("cannot find the path: [%s], please check your configuration", eachPath);
                    LOG.error(message);
                    throw DataXException.asDataXException(FtpReaderErrorCode.PATH_NOT_FOUND, message);
                }
            }
        }

        @Override
        public MetaSchema syncMetaData() {
            //should wait for signal first
            waitForSignal();
            return getMetaSchema();
        }

        private void validateParameter() {
            this.connParams = FtpConnParams.compose(connParams -> {
                String protocol = this.originConfig.getNecessaryValue(Key.PROTOCOL, FtpReaderErrorCode.REQUIRED_VALUE);
                boolean protocolTag = "ftp".equals(protocol) || "sftp".equals(protocol);
                if (!protocolTag) {
                    throw DataXException.asDataXException(FtpReaderErrorCode.ILLEGAL_VALUE,
                            String.format("仅支持 ftp和sftp 传输协议 , 不支持您配置的传输协议: [%s]", protocol));
                }
                connParams.setProtocol(protocol);
                connParams.setHost(this.originConfig.getNecessaryValue(Key.HOST, FtpReaderErrorCode.REQUIRED_VALUE));
                connParams.setUsername(this.originConfig.getNecessaryValue(Key.USERNAME, FtpReaderErrorCode.REQUIRED_VALUE));
                connParams.setPrvKeyPath(this.originConfig.getString(Key.PRV_KEY_PATH, ""));
                connParams.setPassword(this.originConfig.getString(Key.PASSWORD, ""));
                if(StringUtils.isBlank(connParams.getPrvKeyPath()) && StringUtils.isBlank(connParams.getPassword())){
                    throw DataXException.asDataXException(FtpReaderErrorCode.REQUIRED_VALUE, "you need to set private key path or password");
                }
                connParams.setTimeout(this.originConfig.getInt(Key.TIMEOUT, Constant.DEFAULT_TIMEOUT));
                // only support connect pattern
                String connectPattern = this.originConfig.getUnnecessaryValue(Key.CONNECTPATTERN, Constant.DEFAULT_FTP_CONNECT_PATTERN, null);
                boolean connectPatternTag = "PORT".equals(connectPattern) || "PASV".equals(connectPattern);
                if (!connectPatternTag) {
                    throw DataXException.asDataXException(FtpReaderErrorCode.ILLEGAL_VALUE,
                            String.format("不支持您配置的ftp传输模式: [%s]", connectPattern));
                } else {
                    this.originConfig.set(Key.CONNECTPATTERN, connectPattern);
                }
                connParams.setConnectPattern(connectPattern);
            });
            this.maxTraversalLevel = originConfig.getInt(Key.MAXTRAVERSALLEVEL, Constant.DEFAULT_MAX_TRAVERSAL_LEVEL);
            //path check
            String pathInString = this.originConfig.getNecessaryValue(Key.PATH, FtpReaderErrorCode.REQUIRED_VALUE);
            if (!pathInString.startsWith("[") && !pathInString.endsWith("]")) {
                path = new ArrayList<>();
                path.add(pathInString);
            } else {
                path = this.originConfig.getList(Key.PATH, String.class);
                if (null == path || path.size() == 0) {
                    throw DataXException.asDataXException(FtpReaderErrorCode.REQUIRED_VALUE, "您需要指定待读取的源目录或文件");
                }
                if(path.size() > 1){
                    throw DataXException.asDataXException(FtpReaderErrorCode.ILLEGAL_VALUE, "you are allowed to add only one path");
                }
                for (String eachPath : path) {
                    if (!eachPath.startsWith("/")) {
                        String message = String.format("请检查参数path:[%s],需要配置为绝对路径", eachPath);
                        LOG.error(message);
                        throw DataXException.asDataXException(FtpReaderErrorCode.ILLEGAL_VALUE, message);
                    }
                }
            }
            this.incrBeginTime = this.originConfig.getLong(INCR_BEGIN_TIME, 0);
            this.incrEndTime = this.originConfig.getLong(INCR_END_TIME, 0);
        }

        @Override
        public void prepare() {
            LOG.debug("prepare() begin...");
            waitForSignal();
            this.sourceFiles = ftpHelper.getAllFiles(path, 0, maxTraversalLevel);
            LOG.info(String.format("find [%s] files in source path", this.sourceFiles.size()));
            Iterator<PathMeta> iterator = this.sourceFiles.iterator();
            while(iterator.hasNext()){
                PathMeta pathMeta = iterator.next();
                String absolutePath = pathMeta.getAbsolute();
                long modifyTime = ftpHelper.getLastModifyTIme(absolutePath);
                if(incrEndTime > 0) {
                    if (modifyTime <= incrBeginTime || modifyTime > incrEndTime) {
                        iterator.remove();
                    }
                }
            }
            LOG.info(String.format("您即将读取的文件数为: [%s]", this.sourceFiles.size()));
        }

        @Override
        public void post() {
            removeSignal();
        }

        @Override
        public void destroy() {
            try {
                this.ftpHelper.logoutFtpServer();
            } catch (Exception e) {
                String message = String.format(
                        "关闭与ftp服务器连接失败: [%s] host=%s, username=%s, port=%s",
                        e.getMessage(), connParams.getHost(), connParams.getUsername(), connParams.getPort());
                LOG.error(message, e);
            }
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            LOG.debug("split() begin...");
            List<Configuration> readerSplitConfigs = new ArrayList<Configuration>();

            // warn:每个slice拖且仅拖一个文件,
            // int splitNumber = adviceNumber;
            int splitNumber = this.sourceFiles.size();
            if (0 == splitNumber) {
                return new ArrayList<>();
            }

            List<List<PathMeta>> splitedSourceFiles = this.splitSourceFiles(new ArrayList<>(this.sourceFiles), splitNumber);
            for (List<PathMeta> files : splitedSourceFiles) {
                Configuration splitedConfig = this.originConfig.clone();
                splitedConfig.set(Constant.SOURCE_FILES, files);
                readerSplitConfigs.add(splitedConfig);
            }
            LOG.debug("split() ok and end...");
            return readerSplitConfigs;
        }

        private <T> List<List<T>> splitSourceFiles(final List<T> sourceList, int adviceNumber) {
            List<List<T>> splitedList = new ArrayList<>();
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

        private void waitForSignal(){
            if(originConfig.getBool(Key.TRANSIT, true)) {
                //get the first path to check if exist SIGNAL_FILE
                String pathFirst = getFirstPath();
                pathFirst += SIGNAL_FILE_NAME;
                signalFilePath = pathFirst;
                if (!ftpHelper.isFileExist(pathFirst)) {
                    LOG.info("check and wait for the creation of SIGNAL_FILE , path: {} ......", pathFirst);
                    do {
                        try {
                            Thread.sleep(WAIT_SIGNAL_SLEEP_INTERVAL);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw DataXException.asDataXException(FtpReaderErrorCode.RUNTIME_EXCEPTION, "interrupted while waiting for signal");
                        }
                    } while (!ftpHelper.isFileExist(pathFirst));
                }
            }
        }

        private void removeSignal(){
            boolean removable = StringUtils.isNotEmpty(signalFilePath) &&
                    ftpHelper.isFileExist(signalFilePath) && originConfig.getBool(Key.TRANSIT, true);
            if(removable){
                ftpHelper.deleteFile(signalFilePath);
            }
        }

        private MetaSchema getMetaSchema(){
            String path = getFirstPath() + this.originConfig.getString(Key.META_FILE_PATH, DEFAULT_META_FILE_PATH);
            try {
                if (ftpHelper.isFileExist(path)) {
                    InputStream inputStream = ftpHelper.getInputStream(path);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8 * 1024];
                    int size;
                    while ((size = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, size);
                    }
                    String metaSer = outputStream.toString(this.originConfig.getString(
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING,
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Constant.DEFAULT_ENCODING));
                    inputStream.close();
                    outputStream.close();
                    return GsonUtil.fromJson(metaSer, MetaSchema.class);
                }
            }catch (IOException e){
                throw DataXException.asDataXException(FtpReaderErrorCode.GET_META_SCHEMA_ERROR, e.getMessage(), e);
            }
            return null;
        }

        private String getFirstPath(){
            String pathFirst = path.get(0);
            //check if the pathFirst is the directory
            if(!ftpHelper.isDirExist(pathFirst)){
                pathFirst = pathFirst.substring(0, pathFirst.lastIndexOf(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX)));
            }
            if(pathFirst.contains("*") || pathFirst.contains("?")){
                pathFirst = UnstructuredStorageReaderUtil.getRegexPathParentPath(pathFirst);
            }
            if(!pathFirst.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))){
                pathFirst += String.valueOf(IOUtils.DIR_SEPARATOR_UNIX);
            }
            return pathFirst;
        }
    }

    public static class Task extends Reader.Task {
        private static Logger LOG = LoggerFactory.getLogger(Task.class);

        private FtpConnParams connParams;
        private Configuration readerSliceConfig;
        private List<Object> sourceFiles;

        private FtpHelper ftpHelper = null;

        @Override
        public void init() {//连接重试
            /* for ftp connection */
            this.readerSliceConfig = this.getPluginJobConf();
            this.connParams = FtpConnParams.compose( connParams ->{
                connParams.setHost(readerSliceConfig.getString(Key.HOST));
                connParams.setProtocol(readerSliceConfig.getString(Key.PROTOCOL));
                connParams.setUsername(readerSliceConfig.getString(Key.USERNAME));
                connParams.setPassword(readerSliceConfig.getString(Key.PASSWORD, ""));
                connParams.setPrvKeyPath(readerSliceConfig.getString(Key.PRV_KEY_PATH, ""));
                connParams.setTimeout(readerSliceConfig.getInt(Key.TIMEOUT, Constant.DEFAULT_TIMEOUT));
            });
            this.sourceFiles = this.readerSliceConfig.getList(Constant.SOURCE_FILES, Object.class);
            if ("sftp".equals(connParams.getProtocol())) {
                //sftp协议
                connParams.setPort(readerSliceConfig.getInt(Key.PORT, Constant.DEFAULT_SFTP_PORT));
                this.ftpHelper = new SftpHelper();
            } else if ("ftp".equals(connParams.getProtocol())) {
                // ftp 协议
                connParams.setPort(readerSliceConfig.getInt(Key.PORT, Constant.DEFAULT_FTP_PORT));
                // 默认为被动模式
                connParams.setConnectPattern(readerSliceConfig.getString(Key.CONNECTPATTERN, Constant.DEFAULT_FTP_CONNECT_PATTERN));
                this.ftpHelper = new StandardFtpHelper();
            }
            ftpHelper.loginFtpServer(connParams);

        }

        @Override
        public void prepare() {

        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {
            try {
                this.ftpHelper.logoutFtpServer();
            } catch (Exception e) {
                String message = String.format(
                        "关闭与ftp服务器连接失败: [%s] host=%s, username=%s, port=%s",
                        e.getMessage(), connParams.getHost(), connParams.getUsername(), connParams.getPort());
                LOG.error(message, e);
            }
        }

        @Override
        public void startRead(RecordSender recordSender) {
            LOG.info("start read source files...");
            for (Object sourceFile : this.sourceFiles) {
                PathMeta pathMeta = JSONObject.parseObject(JSON.toJSONString(sourceFile), PathMeta.class);
                String fileName = pathMeta.getAbsolute();
                LOG.info(String.format("reading file : [%s]", fileName));
                InputStream inputStream = null;
                inputStream = ftpHelper.getInputStream(fileName);

                UnstructuredStorageReaderUtil.readFromStream(inputStream, fileName, this.readerSliceConfig,
                        recordSender, this.getTaskPluginCollector());
                recordSender.flush();
            }

            LOG.info("end read source files...");
        }

        @Override
        public void startRead(ChannelOutput channelOutput) {
            LOG.info("start read source files to stream channel...");
            for(Object sourceFile : this.sourceFiles){
                PathMeta pathMeta = JSONObject.parseObject(JSON.toJSONString(sourceFile), PathMeta.class);
                String absolutePath = pathMeta.getAbsolute();
                String relativePath = pathMeta.getRelative();
                LOG.info(String.format("reading file: [%s]", absolutePath));
                InputStream inputStream;
                try{
                    String name = absolutePath.substring(absolutePath.lastIndexOf(IOUtils.DIR_SEPARATOR) + 1);
                    StreamMeta streamMeta = new StreamMeta();
                    streamMeta.setName(name);
                    streamMeta.setAbsolutePath(absolutePath);
                    streamMeta.setRelativePath(relativePath);
                    OutputStream outputStream = channelOutput.createStream(streamMeta, readerSliceConfig.getString(
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING,
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Constant.DEFAULT_ENCODING));
                    inputStream = ftpHelper.getInputStream(absolutePath);
                    UnstructuredStorageReaderUtil.readFromStream(inputStream, outputStream,
                            this.readerSliceConfig);
                }catch(IOException e){
                    throw DataXException.asDataXException(FrameworkErrorCode.CHANNEL_STREAM_ERROR, e);
                }
            }
            LOG.info("end read source files to stream channel...");
        }
    }
}
