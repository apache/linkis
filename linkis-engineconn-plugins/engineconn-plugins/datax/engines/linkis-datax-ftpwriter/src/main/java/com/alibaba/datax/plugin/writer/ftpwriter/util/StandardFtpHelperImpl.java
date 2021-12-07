package com.alibaba.datax.plugin.writer.ftpwriter.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.plugin.writer.ftpwriter.FtpWriterErrorCode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.webank.wedatasphere.linkis.datax.common.CryptoUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StandardFtpHelperImpl implements IFtpHelper {
    private static final Logger LOG = LoggerFactory
            .getLogger(StandardFtpHelperImpl.class);
    FTPClient ftpClient = null;

    @Override
    public void loginFtpServer(FtpConnParams connParams) {
        this.ftpClient = new FTPClient();
        try {
            this.ftpClient.setControlEncoding("UTF-8");
            this.ftpClient.setDefaultTimeout(connParams.getTimeout());
            this.ftpClient.setConnectTimeout(connParams.getTimeout());
            this.ftpClient.setDataTimeout(connParams.getTimeout());

            // 连接登录
            this.ftpClient.connect(connParams.getHost(), connParams.getPort());
            this.ftpClient.login(connParams.getUsername(), (String) CryptoUtils.string2Object(connParams.getPassword()));

            this.ftpClient.enterRemotePassiveMode();
            this.ftpClient.enterLocalPassiveMode();
            int reply = this.ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                this.ftpClient.disconnect();
                String message = String
                        .format("与ftp服务器建立连接失败,host:%s, port:%s, username:%s, replyCode:%s",
                                connParams.getHost(), connParams.getPort(), connParams.getUsername(), reply);
                LOG.error(message);
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.FAIL_LOGIN, message);
            }
        } catch (UnknownHostException e) {
            String message = String.format(
                    "请确认ftp服务器地址是否正确，无法连接到地址为: [%s] 的ftp服务器, errorMessage:%s",
                    connParams.getHost(), e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.FAIL_LOGIN, message, e);
        } catch (IllegalArgumentException e) {
            String message = String.format(
                    "请确认连接ftp服务器端口是否正确，错误的端口: [%s], errorMessage:%s", connParams.getPort(),
                    e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.FAIL_LOGIN, message, e);
        } catch (Exception e) {
            String message = String
                    .format("与ftp服务器建立连接失败,host:%s, port:%s, username:%s, errorMessage:%s",
                            connParams.getHost(), connParams.getPort(), connParams.getUsername(), e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.FAIL_LOGIN, message, e);
        }

    }

    @Override
    public void logoutFtpServer() {
        if (this.ftpClient.isConnected()) {
            try {
                this.ftpClient.logout();
            } catch (IOException e) {
                String message = String.format(
                        "与ftp服务器断开连接失败, errorMessage:%s", e.getMessage());
                LOG.error(message);
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.FAIL_DISCONNECT, message, e);
            } finally {
                if (this.ftpClient.isConnected()) {
                    try {
                        this.ftpClient.disconnect();
                    } catch (IOException e) {
                        String message = String.format(
                                "与ftp服务器断开连接失败, errorMessage:%s",
                                e.getMessage());
                        LOG.error(message);
                        throw DataXException.asDataXException(
                                FtpWriterErrorCode.FAIL_DISCONNECT, message, e);
                    }
                }
                this.ftpClient = null;
            }
        }
    }

    @Override
    public void mkdir(String directoryPath) {
        String message = String.format("创建目录:%s时发生异常,请确认与ftp服务器的连接正常,拥有目录创建权限",
                directoryPath);
        try {
            this.printWorkingDirectory();
            boolean isDirExist = this.ftpClient
                    .changeWorkingDirectory(directoryPath);
            if (!isDirExist) {
                int replayCode = this.ftpClient.mkd(directoryPath);
                message = String
                        .format("%s,replayCode:%s", message, replayCode);
                if (replayCode != FTPReply.COMMAND_OK
                        && replayCode != FTPReply.PATHNAME_CREATED) {
                    throw DataXException.asDataXException(
                            FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION,
                            message);
                }
            }
        } catch (IOException e) {
            message = String.format("%s, errorMessage:%s", message,
                    e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
        }
    }

    @Override
    public void mkDirRecursive(String directoryPath) {
        StringBuilder dirPath = new StringBuilder();
        dirPath.append(IOUtils.DIR_SEPARATOR_UNIX);
        String[] dirSplit = StringUtils.split(directoryPath, IOUtils.DIR_SEPARATOR_UNIX);
        String message = String.format("创建目录:%s时发生异常,请确认与ftp服务器的连接正常,拥有目录创建权限", directoryPath);
        try {
            // ftp server不支持递归创建目录,只能一级一级创建
            for (String dirName : dirSplit) {
                if(StringUtils.isNotBlank(dirName)) {
                    dirPath.append(dirName);
                    boolean mkdirSuccess = mkDirSingleHierarchy(dirPath.toString());
                    dirPath.append(IOUtils.DIR_SEPARATOR_UNIX);
                    if (!mkdirSuccess) {
                        throw DataXException.asDataXException(
                                FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION,
                                message);
                    }
                }
            }
        } catch (IOException e) {
            message = String.format("%s, errorMessage:%s", message,
                    e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
        }
    }

    public boolean mkDirSingleHierarchy(String directoryPath) throws IOException {
        boolean isDirExist = this.ftpClient
                .changeWorkingDirectory(directoryPath);
        // 如果directoryPath目录不存在,则创建
        if (!isDirExist) {
            int replayCode = this.ftpClient.mkd(directoryPath);
            if (replayCode != FTPReply.COMMAND_OK
                    && replayCode != FTPReply.PATHNAME_CREATED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public OutputStream getOutputStream(String filePath) {
        try {
            this.printWorkingDirectory();
            String parentDir = filePath.substring(0,
                    StringUtils.lastIndexOf(filePath, IOUtils.DIR_SEPARATOR));
            this.ftpClient.changeWorkingDirectory(parentDir);
            this.printWorkingDirectory();
            //
            OutputStream writeOutputStream = this.ftpClient
                    .appendFileStream(filePath);
            String message = String.format(
                    "打开FTP文件[%s]获取写出流时出错,请确认文件%s有权限创建，有权限写出等", filePath,
                    filePath);
            if (null == writeOutputStream) {
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.OPEN_FILE_ERROR, message);
            }

            return writeOutputStream;
        } catch (IOException e) {
            String message = String.format(
                    "写出文件 : [%s] 时出错,请确认文件:[%s]存在且配置的用户有权限写, errorMessage:%s",
                    filePath, filePath, e.getMessage());
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
            this.ftpClient.changeWorkingDirectory(parentDir);
            this.printWorkingDirectory();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(22);
            this.ftpClient.retrieveFile(filePath, outputStream);
            String result = outputStream.toString();
            IOUtils.closeQuietly(outputStream);
            return result;
        } catch (IOException e) {
            String message = String.format(
                    "读取文件 : [%s] 时出错,请确认文件:[%s]存在且配置的用户有权限读取, errorMessage:%s",
                    filePath, filePath, e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.OPEN_FILE_ERROR, message);
        }
    }

    @Override
    public Set<String> getAllFilesInDir(String dir, String prefixFileName, boolean recurse, boolean fullFileName) {
        Set<String> allFilesWithPointedPrefix = new HashSet<String>();
        try {
            boolean isDirExist = this.ftpClient.changeWorkingDirectory(dir);
            if (!isDirExist) {
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION,
                        String.format("进入目录[%s]失败", dir));
            }
            this.printWorkingDirectory();
            FTPFile[] fs = this.ftpClient.listFiles(dir);
            LOG.debug(String.format("ls: %s",
                    JSON.toJSONString(fs, SerializerFeature.UseSingleQuotes)));
            StringBuilder dirBuilder = new StringBuilder(dir);
            if(!dirBuilder.toString().endsWith(String.valueOf(IOUtils.DIR_SEPARATOR))){
                dirBuilder.append(IOUtils.DIR_SEPARATOR);
            }
            for (FTPFile ff : fs) {
                String strName = ff.getName();
                if (".".equals(strName) || "src/test".equals(strName)) {
                    continue;
                }
                if(hasPrefixFileName(strName, prefixFileName)){
                    if(fullFileName){
                        allFilesWithPointedPrefix.add(dirBuilder.toString() + strName);
                    }else {
                        allFilesWithPointedPrefix.add(strName);
                    }
                    if(ff.isDirectory()){
                        if (recurse) {
                            Set<String> childWithPrefixFiles = getAllFilesInDir(dirBuilder.toString() + strName, prefixFileName, true, fullFileName);
                            for (String childWithPrefixFile : childWithPrefixFiles) {
                                if(fullFileName){
                                    allFilesWithPointedPrefix.add(dirBuilder.toString() + strName + IOUtils.DIR_SEPARATOR + childWithPrefixFile);
                                }else {
                                    allFilesWithPointedPrefix.add(strName + IOUtils.DIR_SEPARATOR + childWithPrefixFile);
                                }
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
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
        boolean deleteOk = false;
        try {
            this.printWorkingDirectory();
            for (String each : filesToDelete) {
                LOG.info(String.format("delete file [%s].", each));
                eachFile = each;
                deleteOk = this.ftpClient.deleteFile(each);
                if (!deleteOk) {
                    String message = String.format(
                            "删除文件:[%s] 时失败,请确认指定文件有删除权限", eachFile);
                    throw DataXException.asDataXException(
                            FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION,
                            message);
                }
            }
        } catch (IOException e) {
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
                    this.ftpClient.printWorkingDirectory()));
        } catch (Exception e) {
            LOG.warn(String.format("printWorkingDirectory error:%s",
                    e.getMessage()));
        }
    }

    @Override
    public void completePendingCommand() {
        /*
         * Q:After I perform a file transfer to the server,
         * printWorkingDirectory() returns null. A:You need to call
         * completePendingCommand() after transferring the file. wiki:
         * http://wiki.apache.org/commons/Net/FrequentlyAskedQuestions
         */
        try {
            boolean isOk = this.ftpClient.completePendingCommand();
            if (!isOk) {
                throw DataXException.asDataXException(
                        FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION,
                        "完成ftp completePendingCommand操作发生异常");
            }
        } catch (IOException e) {
            String message = String.format(
                    "完成ftp completePendingCommand操作发生异常, errorMessage:%s",
                    e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(
                    FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
        }
    }

    @Override
    public void rename(String srcPath, String destPath) {
        try {
            this.ftpClient.rename(srcPath, destPath);
        } catch (IOException e) {
            String message = String.format("rename srcPath:%s to destPath:%s error, message:%s ,please check your internet connection",
                    srcPath, destPath, e.getMessage());
            LOG.error(message);
            throw DataXException.asDataXException(FtpWriterErrorCode.COMMAND_FTP_IO_EXCEPTION, message, e);
        }
    }

    @Override
    public void moveToDirectory(List<String> srcPaths, String destDirPath) {
        //not support
    }

    @Override
    public boolean isFileExist(String filePath) {
        //not support
        return false;
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
