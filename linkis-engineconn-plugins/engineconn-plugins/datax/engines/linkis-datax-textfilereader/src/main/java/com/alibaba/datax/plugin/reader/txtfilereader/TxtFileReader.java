package com.alibaba.datax.plugin.reader.txtfilereader;

import com.webank.wedatasphere.linkis.datax.common.constant.TransportType;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.spi.Reader;
import com.alibaba.datax.common.util.Configuration;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.ChannelOutput;
import com.webank.wedatasphere.linkis.datax.core.transport.stream.StreamMeta;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.PathMeta;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.regex.Pattern;

import static com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INCR_BEGIN_TIME;
import static com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INCR_END_TIME;


/**
 * Created by haiwei.luo on 14-9-20.
 */
public class TxtFileReader extends Reader {
    public static class Job extends Reader.Job {
        private static final Logger LOG = LoggerFactory.getLogger(Job.class);

        private Configuration originConfig = null;

        private List<String> path = null;

        private List<PathMeta> sourceFiles;

        private Map<String, Pattern> pattern;

        private Map<String, Boolean> isRegexPath;

        private long incrBeginTime = 0;

        private long incrEndTime = 0;

        @Override
        public boolean isSupportStream() {
            return true;
        }

        @Override
        public void init() {
            this.originConfig = this.getPluginJobConf();
            this.pattern = new HashMap<String, Pattern>();
            this.isRegexPath = new HashMap<String, Boolean>();
            this.validateParameter();
        }

        private void validateParameter() {
            // Compatible with the old version, path is a string before
            String pathInString = this.originConfig.getNecessaryValue(Key.PATH,
                    TxtFileReaderErrorCode.REQUIRED_VALUE);
            if (StringUtils.isBlank(pathInString)) {
                throw DataXException.asDataXException(
                        TxtFileReaderErrorCode.REQUIRED_VALUE,
                        "您需要指定待读取的源目录或文件");
            }
            if (!pathInString.startsWith("[") && !pathInString.endsWith("]")) {
                path = new ArrayList<>();
                path.add(pathInString);
            } else {
                path = this.originConfig.getList(Key.PATH, String.class);
                if (null == path || path.size() == 0) {
                    throw DataXException.asDataXException(
                            TxtFileReaderErrorCode.REQUIRED_VALUE,
                            "您需要指定待读取的源目录或文件");
                }
                for(String eachPath : path){
                    if(!eachPath.startsWith("/")){
                        String message = String.format("请检查参数path:[%s],需要配置为绝对路径", eachPath);
                        LOG.error(message);
                        throw DataXException.asDataXException(TxtFileReaderErrorCode.ILLEGAL_VALUE, message);
                    }
                    if(!new File(eachPath).exists()){
                        String message = String.format("cannot find the path: [%s], please check your configuration", eachPath);
                        LOG.error(message);
                        throw DataXException.asDataXException(TxtFileReaderErrorCode.PATH_NOT_FOUND, message);
                    }
                }
            }

            String encoding = this.originConfig
                    .getString(
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING,
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Constant.DEFAULT_ENCODING);
            if (StringUtils.isBlank(encoding)) {
                this.originConfig
                        .set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING,
                                com.alibaba.datax.plugin.unstructuredstorage.reader.Constant.DEFAULT_ENCODING);
            } else {
                try {
                    encoding = encoding.trim();
                    this.originConfig
                            .set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING,
                                    encoding);
                    Charsets.toCharset(encoding);
                } catch (UnsupportedCharsetException uce) {
                    throw DataXException.asDataXException(
                            TxtFileReaderErrorCode.ILLEGAL_VALUE,
                            String.format("不支持您配置的编码格式 : [%s]", encoding), uce);
                } catch (Exception e) {
                    throw DataXException.asDataXException(
                            TxtFileReaderErrorCode.CONFIG_INVALID_EXCEPTION,
                            String.format("编码配置异常, 请联系我们: %s", e.getMessage()),
                            e);
                }
            }

            // column: 1. index type 2.value type 3.when type is Date, may have
            // format
            List<Configuration> columns = this.originConfig
                    .getListConfiguration(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN);
            // handle ["*"]
            if (null != columns && 1 == columns.size()) {
                String columnsInStr = columns.get(0).toString();
                if ("\"*\"".equals(columnsInStr) || "'*'".equals(columnsInStr)) {
                    this.originConfig
                            .set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COLUMN,
                                    null);
                    columns = null;
                }
            }

            if (null != columns && columns.size() != 0) {
                for (Configuration eachColumnConf : columns) {
                    eachColumnConf
                            .getNecessaryValue(
                                    com.alibaba.datax.plugin.unstructuredstorage.reader.Key.TYPE,
                                    TxtFileReaderErrorCode.REQUIRED_VALUE);
                    Integer columnIndex = eachColumnConf
                            .getInt(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.INDEX);
                    String columnValue = eachColumnConf
                            .getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.VALUE);

                    if (null == columnIndex && null == columnValue) {
                        throw DataXException.asDataXException(
                                TxtFileReaderErrorCode.NO_INDEX_VALUE,
                                "由于您配置了type, 则至少需要配置 index 或 value");
                    }

                    if (null != columnIndex && null != columnValue) {
                        throw DataXException.asDataXException(
                                TxtFileReaderErrorCode.MIXED_INDEX_VALUE,
                                "您混合配置了index, value, 每一列同时仅能选择其中一种");
                    }
                    if (null != columnIndex && columnIndex < 0) {
                        throw DataXException.asDataXException(
                                TxtFileReaderErrorCode.ILLEGAL_VALUE, String
                                        .format("index需要大于等于0, 您配置的index为[%s]",
                                                columnIndex));
                    }
                }
            }

            // only support compress types
            String compress = this.originConfig
                    .getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COMPRESS);
            if (StringUtils.isBlank(compress)) {
                this.originConfig
                        .set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COMPRESS,
                                null);
            } else {
                Set<String> supportedCompress = Sets
                        .newHashSet("gzip", "bzip2", "zip");
                compress = compress.toLowerCase().trim();
                if (!supportedCompress.contains(compress)) {
                    throw DataXException
                            .asDataXException(
                                    TxtFileReaderErrorCode.ILLEGAL_VALUE,
                                    String.format(
                                            "仅支持 gzip, bzip2, zip 文件压缩格式 , 不支持您配置的文件压缩格式: [%s]",
                                            compress));
                }
                this.originConfig
                        .set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COMPRESS,
                                compress);
            }
            if(getTransportType() == TransportType.RECORD) {
                String delimiterInStr = this.originConfig
                        .getString(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.FIELD_DELIMITER, ",");
                // warn: if have, length must be one
                if (null != delimiterInStr && 1 != delimiterInStr.length()) {
                    throw DataXException.asDataXException(
                            UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
                            String.format("仅仅支持单字符切分, 您配置的切分为 : [%s]",
                                    delimiterInStr));
                }
            }
            this.incrBeginTime = this.originConfig.getLong(INCR_BEGIN_TIME, 0);
            this.incrEndTime = this.originConfig.getLong(INCR_END_TIME, Calendar.getInstance().getTimeInMillis());
        }

        @Override
        public void prepare() {
            LOG.debug("prepare() begin...");
            // warn:make sure this regex string
            // warn:no need trim
            for (String eachPath : this.path) {
                String regexString = eachPath.replace("*", ".*").replace("?",
                        ".?");
                Pattern patt = Pattern.compile(regexString);
                this.pattern.put(eachPath, patt);
                this.sourceFiles = this.buildSourceTargets();
            }

            LOG.info(String.format("您即将读取的文件数为: [%s]", this.sourceFiles.size()));
        }

        @Override
        public void post() {
        }

        @Override
        public void destroy() {
        }

        // warn: 如果源目录为空会报错，拖空目录意图=>空文件显示指定此意图
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

            List<List<PathMeta>> splitedSourceFiles = this.splitSourceFiles(
                    this.sourceFiles, splitNumber);
            for (List<PathMeta> files : splitedSourceFiles) {
                Configuration splitedConfig = this.originConfig.clone();
                splitedConfig.set(Constant.SOURCE_FILES, files);
                readerSplitConfigs.add(splitedConfig);
            }
            LOG.debug("split() ok and end...");
            return readerSplitConfigs;
        }

        /**
         * validate the path, path must be a absolute path
          */
        private List<PathMeta> buildSourceTargets() {
            //0: absolute path, 1: relative path
            List<PathMeta> sourceTargets = new ArrayList<>();
            for (String eachPath : this.path) {
                // for each path
                Set<String> toBeReadFiles = new HashSet<>();
                int endMark;
                for (endMark = 0; endMark < eachPath.length(); endMark++) {
                    if ('*' == eachPath.charAt(endMark)
                            || '?' == eachPath.charAt(endMark)) {
                        this.isRegexPath.put(eachPath, true);
                        break;
                    }
                }

                String parentDirectory;
                if (BooleanUtils.isTrue(this.isRegexPath.get(eachPath))) {
                    int lastDirSeparator = eachPath.substring(0, endMark)
                            .lastIndexOf(IOUtils.DIR_SEPARATOR);
                    parentDirectory = eachPath.substring(0,
                            lastDirSeparator + 1);
                } else {
                    this.isRegexPath.put(eachPath, false);
                    parentDirectory = eachPath;
                }
                this.buildSourceTargetsEathPath(eachPath, parentDirectory,
                        toBeReadFiles);
                toBeReadFiles.forEach( toBeReadFile ->{
                    boolean toBeRead = true;
                    if(getTransportType() == TransportType.STREAM){
                        File localFile = new File(toBeReadFile);
                        if(localFile.lastModified() <= incrBeginTime
                                || localFile.lastModified() > incrEndTime){
                            toBeRead = false;
                        }
                    }
                    if(toBeRead) {
                        String relativePath;
                        if (toBeReadFile.equals(parentDirectory)) {
                            relativePath = parentDirectory.substring(parentDirectory
                                    .lastIndexOf(IOUtils.DIR_SEPARATOR));
                        } else {
                            relativePath = toBeReadFile.substring(parentDirectory.length());
                        }
                        sourceTargets.add(new PathMeta(toBeReadFile , relativePath));
                    }
                });
            }
            return sourceTargets;
        }

        private void buildSourceTargetsEathPath(String regexPath,
                                                String parentDirectory, Set<String> toBeReadFiles) {
            // 检测目录是否存在，错误情况更明确
            try {
                File dir = new File(parentDirectory);
                boolean isExists = dir.exists();
                if (!isExists) {
                    String message = String.format("您设定的目录不存在 : [%s]",
                            parentDirectory);
                    LOG.error(message);
                    throw DataXException.asDataXException(
                            TxtFileReaderErrorCode.FILE_NOT_EXISTS, message);
                }
            } catch (SecurityException se) {
                String message = String.format("您没有权限查看目录 : [%s]",
                        parentDirectory);
                LOG.error(message);
                throw DataXException.asDataXException(
                        TxtFileReaderErrorCode.SECURITY_NOT_ENOUGH, message);
            }

            directoryRover(regexPath, parentDirectory, toBeReadFiles);
        }

        private void directoryRover(String regexPath, String parentDirectory,
                                    Set<String> toBeReadFiles) {
            File directory = new File(parentDirectory);
            if(directory.getName().startsWith(".")){
                //skip hidden files
                return;
            }
            // is a normal file
            if (!directory.isDirectory()) {
                if (this.isTargetFile(regexPath, directory.getAbsolutePath())) {
                    toBeReadFiles.add(parentDirectory);
                    LOG.info(String.format(
                            "add file [%s] as a candidate to be read.",
                            parentDirectory));

                }
            } else {
                // 是目录
                try {
                    // warn:对于没有权限的目录,listFiles 返回null，而不是抛出SecurityException
                    File[] files = directory.listFiles();
                    if (null != files) {
                        for (File subFileNames : files) {
                            directoryRover(regexPath,
                                    subFileNames.getAbsolutePath(),
                                    toBeReadFiles);
                        }
                    } else {
                        // warn: 对于没有权限的文件，是直接throw DataXException
                        String message = String.format("您没有权限查看目录 : [%s]",
                                directory);
                        LOG.error(message);
                        throw DataXException.asDataXException(
                                TxtFileReaderErrorCode.SECURITY_NOT_ENOUGH,
                                message);
                    }

                } catch (SecurityException e) {
                    String message = String.format("您没有权限查看目录 : [%s]",
                            directory);
                    LOG.error(message);
                    throw DataXException.asDataXException(
                            TxtFileReaderErrorCode.SECURITY_NOT_ENOUGH,
                            message, e);
                }
            }
        }

        // 正则过滤
        private boolean isTargetFile(String regexPath, String absoluteFilePath) {
            if (this.isRegexPath.get(regexPath)) {
                return this.pattern.get(regexPath).matcher(absoluteFilePath)
                        .matches();
            } else {
                return true;
            }

        }

        private <T> List<List<T>> splitSourceFiles(final List<T> sourceList,
                                                   int adviceNumber) {
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

    }

    public static class Task extends Reader.Task {
        private static Logger LOG = LoggerFactory.getLogger(Task.class);

        private Configuration readerSliceConfig;
        private List<Object> sourceFiles;

        @Override
        public void init() {
            this.readerSliceConfig = this.getPluginJobConf();
            this.sourceFiles = this.readerSliceConfig.getList(
                    Constant.SOURCE_FILES, Object.class);
        }

        @Override
        public void prepare() {

        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }

        @Override
        public void startRead(RecordSender recordSender) {
            LOG.debug("start read source files...");
            for (Object sourceFile : this.sourceFiles) {
                PathMeta pathMeta = JSONObject.parseObject(JSON.toJSONString(sourceFile), PathMeta.class);
                String fileName = pathMeta.getAbsolute();
                LOG.info(String.format("reading file : [%s]", fileName));
                InputStream inputStream;
                try {
                    inputStream = new FileInputStream(fileName);
                    UnstructuredStorageReaderUtil.readFromStream(inputStream,
                            fileName, this.readerSliceConfig, recordSender,
                            this.getTaskPluginCollector());
                    recordSender.flush();
                } catch (FileNotFoundException e) {
                    // warn: sock 文件无法read,能影响所有文件的传输,需要用户自己保证
                    String message = String
                            .format("找不到待读取的文件 : [%s]", fileName);
                    LOG.error(message);
                    throw DataXException.asDataXException(
                            TxtFileReaderErrorCode.OPEN_FILE_ERROR, message);
                }
            }
            LOG.debug("end read source files...");
        }

        @Override
        public void startRead(ChannelOutput channelOutput) {
            LOG.info("start read source files to stream channel...");
            for(Object sourceFile: this.sourceFiles){
                PathMeta pathMeta = JSONObject.parseObject(JSON.toJSONString(sourceFile), PathMeta.class);
                String absolutePath = pathMeta.getAbsolute();
                String relativePath = pathMeta.getRelative();
                LOG.info(String.format("reading file : [%s]", absolutePath));
                InputStream inputStream;
                try{
                    File file = new File(absolutePath);
                    StreamMeta streamMeta = new StreamMeta();
                    streamMeta.setName(file.getName());
                    streamMeta.setAbsolutePath(absolutePath);
                    streamMeta.setRelativePath(relativePath);
                    OutputStream outputStream = channelOutput.createStream(streamMeta,readerSliceConfig.getString(
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING,
                            com.alibaba.datax.plugin.unstructuredstorage.reader.Constant.DEFAULT_ENCODING));
                    inputStream = new FileInputStream(file);
                    UnstructuredStorageReaderUtil.readFromStream(inputStream, outputStream,
                            this.readerSliceConfig);
                }catch(FileNotFoundException e){
                    String message = String.format("找不到待读取的文件 : [%s]", absolutePath);
                    LOG.error(message);
                    throw DataXException.asDataXException(TxtFileReaderErrorCode.OPEN_FILE_ERROR, message);
                }catch(IOException e){
                    throw DataXException.asDataXException(FrameworkErrorCode.CHANNEL_STREAM_ERROR, e);
                }
            }
            LOG.info("end read source files to stream channel...");
        }
    }
}
