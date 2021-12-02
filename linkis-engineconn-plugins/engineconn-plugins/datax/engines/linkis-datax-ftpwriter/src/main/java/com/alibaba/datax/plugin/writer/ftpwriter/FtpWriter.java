package com.alibaba.datax.plugin.writer.ftpwriter;

import com.alibaba.datax.common.constant.CommonConstant;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.common.util.RetryUtil;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.writer.UnstructuredStorageWriterUtil;
import com.alibaba.datax.plugin.unstructuredstorage.writer.ZipCollectOutputStream;
import com.alibaba.datax.plugin.writer.ftpwriter.util.*;
import org.apache.linkis.datax.common.GsonUtil;
import org.apache.linkis.datax.core.job.meta.MetaSchema;
import org.apache.linkis.datax.core.transport.stream.ChannelInput;
import org.apache.linkis.datax.core.transport.stream.StreamMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Callable;

public class FtpWriter extends Writer {
    private static void loginFtpWithRetry(IFtpHelper ftpHelper, FtpConnParams connParams, Logger log){
        try {
            RetryUtil.executeWithRetry((Callable<Void>) () -> {
                ftpHelper.loginFtpServer(connParams);
                return null;
            }, 3, 4000, true);
        } catch (Exception e) {
            String message = String
                    .format("与ftp服务器建立连接失败, host:%s, username:%s, port:%s, errorMessage:%s",
                            connParams.getHost(), connParams.getUsername(), connParams.getPort(), e.getMessage());
            log.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.FAIL_LOGIN, message, e);
        }
    }
    public static class Job extends Writer.Job {

        private static final String SIGNAL_FILE_NAME = ".ok";

        private static final String DEFAULT_META_FILE_PATH = ".meta";

        private static final long WAIT_SIGNAL_SLEEP_INTERVAL = 3000;

        private static final Logger LOG = LoggerFactory.getLogger(Job.class);

        private Configuration writerSliceConfig = null;
        private Set<String> allFileExists = null;

        private FtpConnParams connParams;

        private String tempPath;

        private IFtpHelper ftpHelper = null;

        private String signalFilePath;
        @Override
        public boolean isSupportStream() {
            return true;
        }

        @Override
        public void init() {
            this.writerSliceConfig = this.getPluginJobConf();
            this.validateParameter();
            UnstructuredStorageWriterUtil
                    .validateParameter(this.writerSliceConfig);
            loginFtpWithRetry(ftpHelper, this.connParams, LOG);
        }

        @Override
        public void syncMetaData(MetaSchema metaSchema) {
            String path = this.writerSliceConfig.getString(Key.PATH);
            //try to create direcotry
            this.ftpHelper.mkDirRecursive(path);
            //should wait for signal first
            waitForSignal(path);
            addMetaSchemaToFile(GsonUtil.toJson(metaSchema));
        }

        private void validateParameter() {
            String path = this.writerSliceConfig.getNecessaryValue(Key.PATH,
                    FtpWriterErrorCode.REQUIRED_VALUE);
            if (!path.startsWith("/")) {
                String message = String.format("请检查参数path:%s,需要配置为绝对路径", path);
                LOG.error(message);
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.ILLEGAL_VALUE, message);
            }
            this.connParams = FtpConnParams.compose(connParams ->{
                connParams.setHost(this.writerSliceConfig.getNecessaryValue(Key.HOST,
                        FtpWriterErrorCode.REQUIRED_VALUE));
                connParams.setUsername(this.writerSliceConfig.getNecessaryValue(
                        Key.USERNAME, FtpWriterErrorCode.REQUIRED_VALUE));
                connParams.setPassword(this.writerSliceConfig.getString(Key.PASSWORD, ""));
                connParams.setPrvKeyPath(this.writerSliceConfig.getString(Key.PRV_KEY_PATH, ""));
                if(StringUtils.isBlank(connParams.getPrvKeyPath()) && StringUtils.isBlank(connParams.getPassword())){
                    throw DataXException.asDataXException(FtpWriterErrorCode.REQUIRED_VALUE, "you need to set private key path or password");
                }
                connParams.setTimeout(this.writerSliceConfig.getInt(Key.TIMEOUT, Constant.DEFAULT_TIMEOUT));
                connParams.setProtocol(this.writerSliceConfig.getNecessaryValue(
                        Key.PROTOCOL, FtpWriterErrorCode.REQUIRED_VALUE));
                connParams.setPort(this.writerSliceConfig.getInt(Key.PORT,
                        Constant.DEFAULT_SFTP_PORT));
            });
            if ("sftp".equalsIgnoreCase(this.connParams.getProtocol())) {
                this.ftpHelper = new SftpHelperImpl();
            } else if ("ftp".equalsIgnoreCase(this.connParams.getProtocol())) {
                this.ftpHelper = new StandardFtpHelperImpl();
            } else {
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.ILLEGAL_VALUE, String.format(
                                "仅支持 ftp和sftp 传输协议 , 不支持您配置的传输协议: [%s]",
                                this.connParams.getProtocol()));
            }
            this.writerSliceConfig.set(Key.PORT, this.connParams.getPort());
        }

        @Override
        public void prepare() {
            String path = this.writerSliceConfig.getString(Key.PATH);
            waitForSignal(path);
            // warn: 这里用户需要配一个目录
            this.ftpHelper.mkDirRecursive(path);

            String writeMode = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.WRITE_MODE);
            Set<String> allFileExists = this.ftpHelper.getAllFilesInDir(path,
                    "", false, false);
            this.allFileExists = allFileExists;
            // truncate option handler
            if ("truncate".equals(writeMode)) {
                LOG.info(String.format(
                        "由于您配置了writeMode truncate, 开始清理 [%s] 下面的内容",
                        path));
                Set<String> fullFileNameToDelete = new HashSet<>();
                for (String each : allFileExists) {
                    //skip meta file
                    if(each.trim().equals(this.writerSliceConfig
                            .getString(Key.META_FILE_PATH, DEFAULT_META_FILE_PATH))){
                        continue;
                    }
                    fullFileNameToDelete.add(UnstructuredStorageWriterUtil
                            .buildFilePath(path, each, null));
                }
                LOG.info(String.format(
                        "删除目录path:[%s] 下文件列表如下: [%s]", path,
                        StringUtils.join(fullFileNameToDelete.iterator(), ", ")));

                this.ftpHelper.deleteFiles(fullFileNameToDelete);
            } else if ("append".equals(writeMode)) {
                LOG.info(String
                        .format("由于您配置了writeMode append, [%s] 目录写入前不做清理工作",
                                path));
                LOG.info(String.format(
                        "目录path:[%s] 下已经存在的文件列表如下: [%s]",
                        path,
                        StringUtils.join(allFileExists.iterator(), ", ")));
            } else if ("nonConflict".equals(writeMode)) {
                LOG.info(String.format(
                        "由于您配置了writeMode nonConflict, 开始检查 [%s] 下面的内容", path));
                if (!allFileExists.isEmpty()) {
                    LOG.info(String.format(
                            "目录path:[%s] 下冲突文件列表如下: [%s]",
                            path,
                            StringUtils.join(allFileExists.iterator(), ", ")));
                    throw DataXException
                            .asDataXException(
                                    FtpWriterErrorCode.ILLEGAL_VALUE,
                                    String.format(
                                            "您配置的path: [%s] 目录不为空, 下面存在其他文件或文件夹.",
                                            path));
                }
            } else {
                throw DataXException
                        .asDataXException(
                                FtpWriterErrorCode.ILLEGAL_VALUE,
                                String.format(
                                        "仅支持 truncate, append, nonConflict 三种模式, 不支持您配置的 writeMode 模式 : [%s]",
                                        writeMode));
            }
        }

        @Override
        public void post() {
            if(StringUtils.isNotBlank(this.tempPath)){
                String path = this.writerSliceConfig.getString(Key.PATH);
                try {
                    this.ftpHelper.moveToDirectory(new ArrayList<>(
                            this.ftpHelper.getAllFilesInDir(this.tempPath, "", false, true)
                    ), path);
                }finally{
                    this.ftpHelper.deleteFiles(Collections.singleton(this.tempPath));
                }
                this.tempPath = null;
            }
            //add signal file
            addSignal();
        }

        @Override
        public void destroy() {
            if(StringUtils.isNotBlank(this.tempPath)){
                this.ftpHelper.deleteFiles(Collections.singleton(this.tempPath));
            }
            try {
                this.ftpHelper.logoutFtpServer();
            } catch (Exception e) {
                String message = String
                        .format("关闭与ftp服务器连接失败, host:%s, username:%s, port:%s, errorMessage:%s",
                                this.connParams.getHost(), this.connParams.getUsername(),
                                this.connParams.getPort(), e.getMessage());
                LOG.error(message, e);
            }
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            this.tempPath = UnstructuredStorageWriterUtil.buildTmpFilePath(
                    this.writerSliceConfig.getString(Key.PATH),
                    String.format(CommonConstant.TEMP_PREFIX, System.currentTimeMillis()),
                    IOUtils.DIR_SEPARATOR,
                    path -> this.allFileExists.contains(path)
            );
            this.writerSliceConfig.set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.TEMP_PATH,
                    this.tempPath);
            //mkdir
            this.ftpHelper.mkDirRecursive(this.tempPath);
            return UnstructuredStorageWriterUtil.split(this.writerSliceConfig,
                    this.allFileExists,  getTransportType() ,mandatoryNumber);
        }

        private void waitForSignal(String path0){
            if(this.writerSliceConfig.getBool(Key.TRANSIT, true)) {
                String path = path0;
                if (!path.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))) {
                    path += String.valueOf(IOUtils.DIR_SEPARATOR_UNIX);
                }
                path += SIGNAL_FILE_NAME;
                signalFilePath = path;
                if (ftpHelper.isFileExist(path)) {
                    LOG.info("signal file: {} exits, wait for the consuming of downstream...", signalFilePath);
                    do {
                        try {
                            Thread.sleep(WAIT_SIGNAL_SLEEP_INTERVAL);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw DataXException.asDataXException(FtpWriterErrorCode.RUNTIME_EXCEPTION,
                                    "interrupted while waiting for the consuming of downstream");
                        }
                    } while ((ftpHelper.isFileExist(path)));
                }
            }
        }

        private void addSignal(){
            if(StringUtils.isNotEmpty(signalFilePath) && this.writerSliceConfig.getBool(Key.TRANSIT, true)){
                //empty signal file
                ftpHelper.getOutputStream(signalFilePath);
            }
        }

        private void addMetaSchemaToFile(String content){
            String path = this.writerSliceConfig.getString(Key.PATH);
            if(!path.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))){
                path += String.valueOf(IOUtils.DIR_SEPARATOR_UNIX);
            }
            String metaPath = this.writerSliceConfig.getString(Key.META_FILE_PATH, DEFAULT_META_FILE_PATH);
            path += metaPath;
            try {
                if(ftpHelper.isFileExist(path)){
                    ftpHelper.deleteFiles(Collections.singleton(path));
                }
                ByteArrayInputStream inputStream = new ByteArrayInputStream(
                        content.getBytes(this.writerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.ENCODING,
                                "UTF-8")));
                OutputStream outputStream = ftpHelper.getOutputStream(path);
                byte[] buffer = new byte[8 * 1024];
                int size;
                while((size = inputStream.read(buffer)) > 0){
                    outputStream.write(buffer, 0, size);
                }
                //close simplify
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw DataXException.asDataXException(FtpWriterErrorCode.FAIL_SYNC_METASCHEMA, e.getMessage(), e);
            }
        }
    }

    public static class Task extends Writer.Task {
        private static final Logger LOG = LoggerFactory.getLogger(Task.class);

        private Configuration writerSliceConfig;

        private String tempPath;
        private String fileName;
        private String suffix;

        private FtpConnParams connParams;

        private IFtpHelper ftpHelper = null;

        @Override
        public void init() {
            this.writerSliceConfig = this.getPluginJobConf();
            this.tempPath = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.TEMP_PATH, "");
            this.fileName = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FILE_NAME, "");
            this.suffix = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.SUFFIX);
            this.connParams = FtpConnParams.compose(connParams -> {
                connParams.setHost(this.writerSliceConfig.getString(Key.HOST));
                connParams.setPort(this.writerSliceConfig.getInt(Key.PORT));
                connParams.setUsername(this.writerSliceConfig.getString(Key.USERNAME));
                connParams.setPassword(this.writerSliceConfig.getString(Key.PASSWORD, ""));
                connParams.setPrvKeyPath(this.writerSliceConfig.getString(Key.PRV_KEY_PATH, ""));
                connParams.setTimeout(this.writerSliceConfig.getInt(Key.TIMEOUT,
                        Constant.DEFAULT_TIMEOUT));
                connParams.setProtocol(this.writerSliceConfig.getString(Key.PROTOCOL));
            });
            if ("sftp".equalsIgnoreCase(this.connParams.getProtocol())) {
                this.ftpHelper = new SftpHelperImpl();
            } else if ("ftp".equalsIgnoreCase(this.connParams.getProtocol())) {
                this.ftpHelper = new StandardFtpHelperImpl();
            }
            loginFtpWithRetry(ftpHelper, this.connParams, LOG);
        }

        @Override
        public void prepare() {

        }

        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            LOG.info("begin do write...");
            String fileFullPath = UnstructuredStorageWriterUtil.buildFilePath(
                    this.tempPath, this.fileName, this.suffix);
            LOG.info(String.format("write to file : [%s]", fileFullPath));

            OutputStream outputStream = null;
            try {
                ftpHelper.mkDirRecursive(fileFullPath.substring(0,
                        StringUtils.lastIndexOf(fileFullPath, IOUtils.DIR_SEPARATOR)));
                outputStream = this.ftpHelper.getOutputStream(fileFullPath);
                String encoding = writerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.ENCODING,
                        com.alibaba.datax.plugin.unstructuredstorage.writer.Constant.DEFAULT_ENCODING);
                String compress = writerSliceConfig.getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.COMPRESS, "");
                if("zip".equalsIgnoreCase(compress)){
                    outputStream = new ZipCollectOutputStream(fileName.substring(0, fileName.lastIndexOf(".")),
                            outputStream, encoding);
                }
                UnstructuredStorageWriterUtil.writeToStream(lineReceiver,
                        outputStream, this.writerSliceConfig, this.fileName,
                        this.getTaskPluginCollector());
            } catch (Exception e) {
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.WRITE_FILE_IO_ERROR,
                        String.format("无法创建待写文件 : [%s]", this.fileName), e);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
            LOG.info("end do write");
        }


        @Override
        public void startWrite(ChannelInput channelInput) {
            LOG.info("begin do write form stream channel");
            try{
                InputStream inputStream;
                while((inputStream = channelInput.nextStream()) != null){
                    StreamMeta metaData = channelInput.streamMetaData(this.writerSliceConfig
                            .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.ENCODING, "UTF-8"));
                    LOG.info("begin do read input stream, name : " + metaData.getName() + ", relativePath: " + metaData.getRelativePath());
                    String relativePath = metaData.getRelativePath();
                    if(StringUtils.isNotBlank(fileName)){
                        //modify the relativePath
                        relativePath = relativePath.substring(0, relativePath.lastIndexOf(IOUtils.DIR_SEPARATOR) + 1)
                                + fileName + "_" + metaData.getName();
                    }
                    String fileFullPath = UnstructuredStorageWriterUtil.buildFilePath(this.tempPath, relativePath, "");
                    ftpHelper.mkDirRecursive(fileFullPath.substring(0,
                            StringUtils.lastIndexOf(fileFullPath, IOUtils.DIR_SEPARATOR)));
                    OutputStream outputStream = ftpHelper.getOutputStream(fileFullPath);
                    try{
                        UnstructuredStorageWriterUtil.writeToStream(inputStream, outputStream,
                                this.writerSliceConfig);
                    }finally{
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            } catch (IOException e){
                throw DataXException.asDataXException(FrameworkErrorCode.CHANNEL_STREAM_ERROR, e);
            }
            LOG.info("end to write from stream channel");
        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {
            try {
                this.ftpHelper.logoutFtpServer();
            } catch (Exception e) {
                String message = String
                        .format("关闭与ftp服务器连接失败, host:%s, username:%s, port:%s, errorMessage:%s",
                                this.connParams.getHost(), this.connParams.getUsername(), this.connParams.getPort(), e.getMessage());
                LOG.error(message, e);
            }
        }
    }

}
