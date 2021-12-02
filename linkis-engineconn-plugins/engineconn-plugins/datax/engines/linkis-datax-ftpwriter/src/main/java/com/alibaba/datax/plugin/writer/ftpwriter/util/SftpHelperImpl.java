package com.alibaba.datax.plugin.writer.ftpwriter.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.plugin.unstructuredstorage.writer.UnstructuredStorageWriterUtil;
import com.alibaba.datax.plugin.writer.ftpwriter.FtpWriterErrorCode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.apache.linkis.datax.common.CryptoUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class SftpHelperImpl implements IFtpHelper {
    private static final Logger LOG = LoggerFactory
            .getLogger(SftpHelperImpl.class);

    private static final String FILE_NOT_EXIST_ = "no such file";

    private Session session = null;
    private ChannelSftp channelSftp = null;

    @Override
    public void loginFtpServer(FtpConnParams connParams) {
        JSch jsch = new JSch();
        try {
            if(StringUtils.isNotBlank(connParams.getPrvKeyPath())){
                jsch.addIdentity(connParams.getPrvKeyPath());
            }
            this.session = jsch.getSession(connParams.getUsername(), connParams.getHost(),
                    connParams.getPort());
            if (this.session == null) {
                throw DataXException
                        .asDataXException(FtpWriterErrorCode.FAIL_LOGIN,
                                "创建ftp连接this.session失败,无法通过sftp与服务器建立链接，请检查主机名和用户名是否正确.");
            }
            if(StringUtils.isNotBlank(connParams.getPassword())){
                this.session.setPassword((String) CryptoUtils.string2Object(connParams.getPassword()));
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "publickey,password");
            this.session.setConfig(config);
            this.session.setTimeout(connParams.getTimeout());
            this.session.connect();
            this.channelSftp = (ChannelSftp) this.session.openChannel("sftp");
            this.channelSftp.connect();
        } catch (JSchException | ClassNotFoundException | IOException e) {
            if (null != e.getCause()) {
                String cause = e.getCause().toString();
                String unknownHostException = "java.net.UnknownHostException: "
                        + connParams.getHost();
                String illegalArgumentException = "java.lang.IllegalArgumentException: port out of range:"
                        + connParams.getPort();
                String wrongPort = "java.net.ConnectException: Connection refused";
                if (unknownHostException.equals(cause)) {
                    String message = String
                            .format("请确认ftp服务器地址是否正确，无法连接到地址为: [%s] 的ftp服务器, errorMessage:%s",
                                    connParams.getHost(), e.getMessage());
                    LOG.error(message);
                    throw DataXException.asDataXException(
                            FtpWriterErrorCode.FAIL_LOGIN, message, e);
                } else if (illegalArgumentException.equals(cause)
                        || wrongPort.equals(cause)) {
                    String message = String.format(
                            "请确认连接ftp服务器端口是否正确，错误的端口: [%s], errorMessage:%s",
                            connParams.getPort(), e.getMessage());
                    LOG.error(message);
                    throw DataXException.asDataXException(
                            FtpWriterErrorCode.FAIL_LOGIN, message, e);
                }else{
                    String message = "cannot login to the sftp server, please check your configuration of connecting";
                    LOG.error(message);
                    throw DataXException.asDataXException(FtpWriterErrorCode.FAIL_LOGIN, message, e);
                }
            } else {
                String message = String
                        .format("与ftp服务器建立连接失败,请检查主机、用户名、密码是否正确, host:%s, port:%s, username:%s, errorMessage:%s",
                                connParams.getHost(), connParams.getPort(), connParams.getUsername(), e.getMessage());
                LOG.error(message);
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.FAIL_LOGIN, message);
            }
        }

    }

    @Override
    public void logoutFtpServer() {
        if (this.channelSftp != null) {
            this.channelSftp.disconnect();
            this.channelSftp = null;
        }
        if (this.session != null) {
            this.session.disconnect();
            this.session = null;
        }
    }

    @Override
    public void mkdir(String directoryPath) {
        boolean isDirExist = false;
        try {
            SftpATTRS sftpATTRS = this.channelSftp.lstat(directoryPath);
            isDirExist = sftpATTRS.isDir();
        } catch (SftpException e) {
            if (e.getMessage().toLowerCase().equals(FILE_NOT_EXIST_)) {
                LOG.warn(String.format(
                        "您的配置项path:[%s]不存在，将尝试进行目录创建, errorMessage:%s",
                        directoryPath, e.getMessage()), e);
                isDirExist = false;
            }
        }
        if (!isDirExist) {
            try {
                // warn 检查mkdir -p
                this.channelSftp.mkdir(directoryPath);
            } catch (SftpException e) {
                String message = String
                        .format("创建目录:%s时发生I/O异常,请确认与ftp服务器的连接正常,拥有目录创建权限, errorMessage:%s",
                                directoryPath, e.getMessage());
                LOG.error(message, e);
                throw DataXException
                        .asDataXException(
                                FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION,
                                message, e);
            }
        }
    }

    @Override
    public void mkDirRecursive(String directoryPath) {
        boolean isDirExist = false;
        try {
            SftpATTRS sftpATTRS = this.channelSftp.lstat(directoryPath);
            isDirExist = sftpATTRS.isDir();
        } catch (SftpException e) {
            if (e.getMessage().toLowerCase().equals(FILE_NOT_EXIST_)) {
                LOG.warn(String.format(
                        "您的配置项path:[%s]不存在，将尝试进行目录创建, errorMessage:%s",
                        directoryPath, e.getMessage()), e);
                isDirExist = false;
            }
        }
        if (!isDirExist) {
            StringBuilder dirPath = new StringBuilder();
            dirPath.append(IOUtils.DIR_SEPARATOR_UNIX);
            String[] dirSplit = StringUtils.split(directoryPath, IOUtils.DIR_SEPARATOR_UNIX);
            try {
                // ftp server不支持递归创建目录,只能一级一级创建
                for (String dirName : dirSplit) {
                    if(StringUtils.isNotBlank(dirName)) {
                        dirPath.append(dirName);
                        mkDirSingleHierarchy(dirPath.toString());
                        dirPath.append(IOUtils.DIR_SEPARATOR_UNIX);
                    }
                }
            } catch (SftpException e) {
                String message = String
                        .format("创建目录:%s时发生I/O异常,请确认与ftp服务器的连接正常,拥有目录创建权限, errorMessage:%s",
                                directoryPath, e.getMessage());
                LOG.error(message, e);
                throw DataXException
                        .asDataXException(
                                FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION,
                                message, e);
            }
        }
    }

    public boolean mkDirSingleHierarchy(String directoryPath) throws SftpException {
        boolean isDirExist = false;
        try {
            SftpATTRS sftpATTRS = this.channelSftp.lstat(directoryPath);
            isDirExist = sftpATTRS.isDir();
        } catch (SftpException e) {
            if (!isDirExist) {
                LOG.info(String.format("正在逐级创建目录 [%s]", directoryPath));
                this.channelSftp.mkdir(directoryPath);
                return true;
            }
        }
        if (!isDirExist) {
            LOG.info(String.format("正在逐级创建目录 [%s]", directoryPath));
            this.channelSftp.mkdir(directoryPath);
        }
        return true;
    }

    @Override
    public OutputStream getOutputStream(String filePath) {
        try {
            String parentDir = filePath.substring(0,
                    StringUtils.lastIndexOf(filePath, IOUtils.DIR_SEPARATOR));
            this.channelSftp.cd(parentDir);
            this.printWorkingDirectory();
            OutputStream writeOutputStream = this.channelSftp.put(filePath,
                    ChannelSftp.APPEND);
            String message = String.format(
                    "打开FTP文件[%s]获取写出流时出错,请确认文件%s有权限创建，有权限写出等", filePath,
                    filePath);
            if (null == writeOutputStream) {
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.OPEN_FILE_ERROR, message);
            }
            return writeOutputStream;
        } catch (SftpException e) {
            String message = String.format(
                    "写出文件[%s] 时出错,请确认文件%s有权限写出, errorMessage:%s", filePath,
                    filePath, e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.OPEN_FILE_ERROR, message);
        }
    }

    @Override
    public String getRemoteFileContent(String filePath) {
        try {
            this.completePendingCommand();
            this.printWorkingDirectory();
            String parentDir = filePath.substring(0,
                    StringUtils.lastIndexOf(filePath, IOUtils.DIR_SEPARATOR));
            this.channelSftp.cd(parentDir);
            this.printWorkingDirectory();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(22);
            this.channelSftp.get(filePath, outputStream);
            String result = outputStream.toString();
            IOUtils.closeQuietly(outputStream);
            return result;
        } catch (SftpException e) {
            String message = String.format(
                    "写出文件[%s] 时出错,请确认文件%s有权限写出, errorMessage:%s", filePath,
                    filePath, e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.OPEN_FILE_ERROR, message);
        }
    }

    @Override
    public Set<String> getAllFilesInDir(String dir, String prefixFileName, boolean recurse, boolean fullFileName) {
        Set<String> allFilesWithPointedPrefix = new HashSet<String>();
        try {
            @SuppressWarnings("rawtypes")
            Vector allFiles = this.channelSftp.ls(dir);
            LOG.debug(String.format("ls: %s", JSON.toJSONString(allFiles,
                    SerializerFeature.UseSingleQuotes)));
            StringBuilder dirBuilder = new StringBuilder(dir);
            if(!dirBuilder.toString().endsWith(String.valueOf(IOUtils.DIR_SEPARATOR))){
                dirBuilder.append(IOUtils.DIR_SEPARATOR);
            }
            for (Object allFile : allFiles) {
                LsEntry le = (LsEntry) allFile;
                String strName = le.getFilename();
                if(hasPrefixFileName(strName, prefixFileName)){
                    if (".".equals(strName) || "src/test".equals(strName)) {
                        continue;
                    }
                    if(fullFileName){
                        allFilesWithPointedPrefix.add(dirBuilder.toString() + strName);
                    }else{
                        allFilesWithPointedPrefix.add(strName);
                    }
                    if (this.channelSftp.stat(dir + IOUtils.DIR_SEPARATOR + strName).isDir()) {
                        if (recurse) {
                            String parent = dirBuilder.toString() + strName;
                            Set<String> childWithPrefixFiles = getAllFilesInDir(parent, prefixFileName, true, fullFileName);
                            for (String childWithPrefixFile : childWithPrefixFiles) {
                                if(fullFileName){
                                    allFilesWithPointedPrefix.add(parent + IOUtils.DIR_SEPARATOR + childWithPrefixFile);
                                }else {
                                    allFilesWithPointedPrefix.add(strName + IOUtils.DIR_SEPARATOR + childWithPrefixFile);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SftpException e) {
            String message = String
                    .format("获取path:[%s] 下文件列表时发生I/O异常,请确认与ftp服务器的连接正常,拥有目录ls权限, errorMessage:%s",
                            dir, e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
        }
        return allFilesWithPointedPrefix;
    }

    @Override
    public void deleteFiles(Set<String> filesToDelete) {
        String eachFile = null;
        try {
            for (String each : filesToDelete) {
                eachFile = each;
                if(!this.channelSftp.stat(each).isDir()) {
                    LOG.info(String.format("delete file [%s].", each));
                    this.channelSftp.rm(each);
                }else{
                    Set<String> subFiles = getAllFilesInDir(each, "", false, true);
                    deleteFiles(subFiles);
                    LOG.info(String.format("delete empty directory [%s]", each));
                    this.channelSftp.rmdir(each);
                }
            }
        } catch (SftpException e) {
            String message = String.format(
                    "删除文件:[%s] 时发生异常,请确认指定文件有删除权限,以及网络交互正常, errorMessage:%s",
                    eachFile, e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
        }
    }

    private void printWorkingDirectory() {
        try {
            LOG.info(String.format("current working directory:%s",
                    this.channelSftp.pwd()));
        } catch (Exception e) {
            LOG.warn(String.format("printWorkingDirectory error:%s",
                    e.getMessage()));
        }
    }

    @Override
    public void completePendingCommand() {
    }

    @Override
    public void rename(String srcPath, String destPath) {
        try {
            this.channelSftp.rename(srcPath, destPath);
        } catch (SftpException e) {
            String message = String.format("rename srcPath:%s to destPath:%s error, message:%s ,please check your internet connection",
                    srcPath, destPath, e.getMessage());
            LOG.error(message);
           throw DataXException.asDataXException(FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
        }
    }

    @Override
    public void moveToDirectory(List<String> srcPaths, String destDirPath) {
        try {
            mkdir(destDirPath);
            for (String srcPath : srcPaths) {
                SftpATTRS sftpATTRS = this.channelSftp.lstat(srcPath);
                if(sftpATTRS.isDir()) {
                    if(srcPath.endsWith(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX))){
                        srcPath = srcPath.substring(0, srcPath.length() - 1);
                    }
                    Set<String> moveFiles = getAllFilesInDir(srcPath, "", false, true);
                    moveToDirectory(new ArrayList<>(moveFiles),
                            UnstructuredStorageWriterUtil.buildFilePath(destDirPath,
                                    srcPath.substring(srcPath.lastIndexOf(IOUtils.DIR_SEPARATOR_UNIX)), ""));

                }else{
                    rename(srcPath, UnstructuredStorageWriterUtil.buildFilePath(destDirPath,
                            srcPath.substring(srcPath.lastIndexOf(IOUtils.DIR_SEPARATOR_UNIX)), ""));
                }
            }
        }catch(SftpException e){

        }
    }

    @Override
    public boolean isFileExist(String filePath) {
        boolean isExitFlag = false;
        try{
            SftpATTRS sftpATTRS = channelSftp.lstat(filePath);
            if(sftpATTRS.getSize() >= 0){
                isExitFlag = true;
            }
        }catch(SftpException e){
            if (!FILE_NOT_EXIST_.equals(e.getMessage().toLowerCase())) {
                String message = String.format("获取文件：[%s] 属性时发生I/O异常,请确认与ftp服务器的连接正常", filePath);
                LOG.error(message);
                throw DataXException.asDataXException(FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
            }
        }
        return isExitFlag;
    }

    /**
     * check if originalName stat with prefixFileName
     * @param originalName
     * @param prefixFileName
     * @return
     */
    private boolean hasPrefixFileName(String originalName, String prefixFileName){
        if(StringUtils.isBlank(prefixFileName)){
            return true;
        }
        return originalName != null && originalName.startsWith(prefixFileName);
    }
}
