package com.alibaba.datax.plugin.writer.txtfilewriter;

import com.alibaba.datax.common.constant.CommonConstant;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.CompressSuffixName;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.writer.UnstructuredStorageWriterUtil;
import com.webank.wedatasphere.linkis.datax.common.constant.TransportType;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ChannelInput;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.StreamMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by haiwei.luo on 14-9-17.
 */
public class TxtFileWriter extends Writer {
    public static class Job extends Writer.Job {
        private static final Logger LOG = LoggerFactory.getLogger(Job.class);

        private String tempPath;
        private Configuration writerSliceConfig = null;

        @Override
        public boolean isSupportStream() {
            return true;
        }

        @Override
        public void init() {
            this.writerSliceConfig = this.getPluginJobConf();
            this.validateParameter();
            String dateFormatOld = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FORMAT);
            String dateFormatNew = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.DATE_FORMAT);
            if (null == dateFormatNew) {
                this.writerSliceConfig
                        .set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.DATE_FORMAT,
                                dateFormatOld);
            }
            if (null != dateFormatOld) {
                LOG.warn("您使用format配置日期格式化, 这是不推荐的行为, 请优先使用dateFormat配置项, 两项同时存在则使用dateFormat.");
            }
            UnstructuredStorageWriterUtil
                    .validateParameter(this.writerSliceConfig);
        }

        private void validateParameter() {
            String path = this.writerSliceConfig.getNecessaryValue(Key.PATH,
                    TxtFileWriterErrorCode.REQUIRED_VALUE);

            try {
                // warn: 这里用户需要配一个目录
                File dir = new File(path);
                if (dir.isFile()) {
                    throw DataXException
                            .asDataXException(
                                    TxtFileWriterErrorCode.ILLEGAL_VALUE,
                                    String.format(
                                            "您配置的path: [%s] 不是一个合法的目录, 请您注意文件重名, 不合法目录名等情况.",
                                            path));
                }
                if (!dir.exists()) {
                    boolean createdOk = dir.mkdirs();
                    if (!createdOk) {
                        throw DataXException
                                .asDataXException(
                                        TxtFileWriterErrorCode.CONFIG_INVALID_EXCEPTION,
                                        String.format("您指定的文件路径 : [%s] 创建失败.",
                                                path));
                    }
                }
            } catch (SecurityException se) {
                throw DataXException.asDataXException(
                        TxtFileWriterErrorCode.SECURITY_NOT_ENOUGH,
                        String.format("您没有权限创建文件路径 : [%s] ", path), se);
            }
        }

        @Override
        public void prepare() {
            String path = this.writerSliceConfig.getString(Key.PATH);
            String writeMode = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.WRITE_MODE);
            // truncate option handler
            if ("truncate".equals(writeMode)) {
                try {
                    File dir = new File(path);
                    LOG.info(String.format("由于您配置了writeMode truncate, 开始清理 [%s] 下面所有内容", path));
                    if(dir.exists()) {
                        for (File eachFile : Objects.requireNonNull(dir.listFiles())) {
                            FileUtils.forceDelete(eachFile);
                        }
                    }
                } catch (NullPointerException npe) {
                    throw DataXException
                            .asDataXException(
                                    TxtFileWriterErrorCode.Write_FILE_ERROR,
                                    String.format("您配置的目录清空时出现空指针异常 : [%s]",
                                            path), npe);
                } catch (IllegalArgumentException iae) {
                    throw DataXException.asDataXException(
                            TxtFileWriterErrorCode.SECURITY_NOT_ENOUGH,
                            String.format("您配置的目录参数异常 : [%s]", path));
                } catch (SecurityException se) {
                    throw DataXException.asDataXException(
                            TxtFileWriterErrorCode.SECURITY_NOT_ENOUGH,
                            String.format("您没有权限查看目录 : [%s]", path));
                } catch (IOException e) {
                    throw DataXException.asDataXException(
                            TxtFileWriterErrorCode.Write_FILE_ERROR,
                            String.format("无法清空目录 : [%s]", path), e);
                }
            } else if ("append".equals(writeMode)) {
                LOG.info("由于您配置了writeMode append, 写入前不做清理工作");
            } else if ("nonConflict".equals(writeMode)) {
                LOG.info(String.format(
                        "由于您配置了writeMode nonConflict, 开始检查 [%s] 下面的内容", path));
                // warn: check two times about exists, mkdirs
                File dir = new File(path);
                try {
                    if (dir.exists()) {
                        if (dir.isFile()) {
                            throw DataXException
                                    .asDataXException(
                                            TxtFileWriterErrorCode.ILLEGAL_VALUE,
                                            String.format(
                                                    "您配置的path: [%s] 不是一个合法的目录, 请您注意文件重名, 不合法目录名等情况.",
                                                    path));
                        }
                        File[] filesWithFileNamePrefix = dir.listFiles();
                        if (null != filesWithFileNamePrefix && filesWithFileNamePrefix.length > 0) {
                            List<String> allFiles = new ArrayList<String>();
                            for (File eachFile : filesWithFileNamePrefix) {
                                allFiles.add(eachFile.getName());
                            }
                            LOG.error(String.format("冲突文件列表为: [%s]",
                                    StringUtils.join(allFiles, ",")));
                            throw DataXException
                                    .asDataXException(
                                            TxtFileWriterErrorCode.ILLEGAL_VALUE,
                                            String.format(
                                                    "您配置的path: [%s] 目录不为空, 下面存在其他文件或文件夹.",
                                                    path));
                        }
                    } else {
                        boolean createdOk = dir.mkdirs();
                        if (!createdOk) {
                            throw DataXException
                                    .asDataXException(
                                            TxtFileWriterErrorCode.CONFIG_INVALID_EXCEPTION,
                                            String.format(
                                                    "您指定的文件路径 : [%s] 创建失败.",
                                                    path));
                        }
                    }
                } catch (SecurityException se) {
                    throw DataXException.asDataXException(
                            TxtFileWriterErrorCode.SECURITY_NOT_ENOUGH,
                            String.format("您没有权限查看目录 : [%s]", path));
                }
            } else {
                throw DataXException
                        .asDataXException(
                                TxtFileWriterErrorCode.ILLEGAL_VALUE,
                                String.format(
                                        "仅支持 truncate, append, nonConflict 三种模式, 不支持您配置的 writeMode 模式 : [%s]",
                                        writeMode));
            }
        }

        @Override
        public void post() {
            String path = this.writerSliceConfig.getNecessaryValue(Key.PATH,
                    TxtFileWriterErrorCode.REQUIRED_VALUE);
            if(StringUtils.isNotBlank(this.tempPath)){
                try {
                    LOG.info(String.format("move files or directories under temporary path: %s to path: %s", tempPath, path));
                    try {
                        File[] moveFiles = new File(this.tempPath).listFiles();
                        for(File moveFile : moveFiles) {
                            moveToDirectory(moveFile, new File(path));
                        }
                    } catch (IOException e) {
                        throw DataXException.asDataXException(
                                TxtFileWriterErrorCode.Write_FILE_IO_ERROR,
                                String.format("cannot move temporary directory, message: %s"
                                        , e.getMessage())
                        );
                    }
                }finally{
                    try {
                        LOG.info(String.format("delete temporary path : %s", tempPath));
                        FileUtils.forceDelete(new File(this.tempPath));
                        this.tempPath = null;
                    }catch(IOException e){
                        DataXException de = DataXException.asDataXException(
                                TxtFileWriterErrorCode.Write_FILE_IO_ERROR,
                                String.format("cannot delete temporary directory %s", this.tempPath)
                        );
                        LOG.error(de.getMessage(), de);
                    }
                }
            }
        }

        @Override
        public void destroy() {
            if(StringUtils.isNotBlank(tempPath)){
                try {
                    LOG.info(String.format("delete temporary path : %s", tempPath));
                    FileUtils.forceDelete(new File(this.tempPath));
                }catch(IOException e){
                    DataXException de = DataXException.asDataXException(
                            TxtFileWriterErrorCode.Write_FILE_IO_ERROR,
                            String.format("cannot delete temporary directory %s", this.tempPath)
                    );
                    LOG.error(de.getMessage(), de);
                }
            }
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            LOG.info("begin do split...");
            List<Configuration> writerSplitConfigs = new ArrayList<Configuration>();
            String filePrefix = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FILE_NAME, "");

            Set<String> allFiles;
            String path = null;
            try {
                path = this.writerSliceConfig.getString(Key.PATH);
                File dir = new File(path);
                allFiles = new HashSet<>(Arrays.asList(Objects.requireNonNull(dir.list())));
            } catch (SecurityException se) {
                throw DataXException.asDataXException(
                        TxtFileWriterErrorCode.SECURITY_NOT_ENOUGH,
                        String.format("您没有权限查看目录 : [%s]", path));
            }
            this.tempPath = UnstructuredStorageWriterUtil.buildTmpFilePath(path,
                    String.format(CommonConstant.TEMP_PREFIX, System.currentTimeMillis()), IOUtils.DIR_SEPARATOR ,
                    allFiles::contains);
            String fileSuffix;
            for (int i = 0; i < mandatoryNumber; i++) {
                // handle same file name

                Configuration splitedTaskConfig = this.writerSliceConfig
                        .clone();
                splitedTaskConfig.set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.TEMP_PATH, this.tempPath);
                if(getTransportType() == TransportType.STREAM){
                    splitedTaskConfig.set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FILE_NAME,
                           filePrefix);
                    writerSplitConfigs.add(splitedTaskConfig);
                    continue;
                }
                String fullFileName = null;
                do{
                    fileSuffix = UUID.randomUUID().toString().replace('-', '_');
                    fullFileName = String.format("%s__%s", filePrefix,
                            fileSuffix);
                }while(allFiles.contains(fullFileName));
                allFiles.add(fullFileName);
                String suffix = CompressSuffixName.chooseSuffix(this.writerSliceConfig
                        .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.COMPRESS, ""));
                if(StringUtils.isNotBlank(suffix)){
                    fullFileName += suffix;
                }
                splitedTaskConfig
                        .set(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FILE_NAME,
                                fullFileName);

                LOG.info(String.format("splited write file name:[%s]",
                        fullFileName));

                writerSplitConfigs.add(splitedTaskConfig);
            }
            LOG.info("end do split.");
            return writerSplitConfigs;
        }

        private void moveToDirectory(File src, File destDir) throws IOException{
            if(src.isDirectory()){
                File childDestDir = new File(destDir, src.getName());
                if(childDestDir.exists()){
                    if(!childDestDir.isDirectory()){
                        throw new IOException("Destination has the conflict file named '" + childDestDir.getPath() + "'");
                    }
                    File[] childFiles = src.listFiles();
                    if(null != childFiles) {
                        for (File childFile : childFiles) {
                            moveToDirectory(childFile, childDestDir);
                        }
                    }
                }else{
                    FileUtils.moveToDirectory(src, destDir, true);
                }

            }else{
                File dest = new File(destDir, src.getName());
                boolean canMove = !dest.exists() || (dest.exists() && dest.delete());
                if(canMove){
                    FileUtils.moveFile(src, dest);
                }
            }
        }
    }

    public static class Task extends Writer.Task {
        private static final Logger LOG = LoggerFactory.getLogger(Task.class);

        private Configuration writerSliceConfig;


        private String fileName;

        private String tempPath;


        @Override
        public void init() {
            this.writerSliceConfig = this.getPluginJobConf();
            this.tempPath = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.TEMP_PATH, "");
            this.fileName = this.writerSliceConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.writer.Key.FILE_NAME, "");
        }

        @Override
        public void prepare() {

        }

        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            LOG.debug("begin do write...");
            String fileFullPath = this.buildFilePath(this.fileName);
            LOG.debug(String.format("write to file : [%s]", fileFullPath));

            OutputStream outputStream = null;
            try {
                File newFile = new File(fileFullPath);
                newFile.getParentFile().mkdirs();
                if(!newFile.createNewFile()){
                    throw DataXException.asDataXException(TxtFileWriterErrorCode.SECURITY_NOT_ENOUGH,
                            String.format("无法创建待写文件 : [%s]", this.fileName));
                }
                outputStream = new FileOutputStream(newFile);
                UnstructuredStorageWriterUtil.writeToStream(lineReceiver,
                        outputStream, this.writerSliceConfig, this.fileName,
                        this.getTaskPluginCollector());
            } catch (SecurityException se) {
                throw DataXException.asDataXException(
                        TxtFileWriterErrorCode.SECURITY_NOT_ENOUGH,
                        String.format("您没有权限创建文件  : [%s]", this.fileName));
            } catch (IOException ioe) {
                throw DataXException.asDataXException(
                        TxtFileWriterErrorCode.Write_FILE_IO_ERROR,
                        String.format("无法创建待写文件 : [%s]", this.fileName), ioe);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
            LOG.debug("end do write");
        }

        @Override
        public void startWrite(ChannelInput channelInput) {
            LOG.info("begin do write from stream channel...");
            try {
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
                    String fileFullPath = this.buildFilePath(relativePath);
                    File file = new File(fileFullPath);
                    file.getParentFile().mkdirs();
                    FileOutputStream outputStream = new FileOutputStream(file, false);
                    try {
                        UnstructuredStorageWriterUtil.writeToStream(inputStream, outputStream
                        , this.writerSliceConfig);
                        outputStream.flush();
                    }finally{
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            } catch (IOException e) {
                throw DataXException.asDataXException(FrameworkErrorCode.CHANNEL_STREAM_ERROR, e);
            }
            LOG.info("end do write from stream channel");
        }

        private String buildFilePath(String fileName) {
            boolean isEndWithSeparator = false;
            switch (IOUtils.DIR_SEPARATOR) {
                case IOUtils.DIR_SEPARATOR_UNIX:
                    isEndWithSeparator = this.tempPath.endsWith(String
                            .valueOf(IOUtils.DIR_SEPARATOR));
                    break;
                case IOUtils.DIR_SEPARATOR_WINDOWS:
                    isEndWithSeparator = this.tempPath.endsWith(String
                            .valueOf(IOUtils.DIR_SEPARATOR_WINDOWS));
                    break;
                default:
                    break;
            }
            if (!isEndWithSeparator) {
                this.tempPath = this.tempPath + IOUtils.DIR_SEPARATOR;
            }
            return String.format("%s%s", this.tempPath, fileName);
        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }
    }
}
