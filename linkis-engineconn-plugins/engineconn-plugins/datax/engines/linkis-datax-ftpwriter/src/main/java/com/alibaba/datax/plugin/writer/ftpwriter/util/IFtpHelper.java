package com.alibaba.datax.plugin.writer.ftpwriter.util;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public interface IFtpHelper {

    /**
     * 使用被动方式
     */
    void loginFtpServer(FtpConnParams ftpConnParams);

    void logoutFtpServer();

    /**
     * warn: 不支持递归创建, 比如 mkdir -p
     */
    void mkdir(String directoryPath);

    /**
     * 支持目录递归创建
     */
    void mkDirRecursive(String directoryPath);

    OutputStream getOutputStream(String filePath);

    String getRemoteFileContent(String filePath);

    Set<String> getAllFilesInDir(String dir, String prefixFileName, boolean recurse, boolean fullFileName);

    /**
     * delete files and empty directory
     * @param filesToDelete
     */
    void deleteFiles(Set<String> filesToDelete);

    void completePendingCommand();

    void rename(String srcPath, String destPath);

    void moveToDirectory(List<String> srcPaths, String destDirPath);

    /**
     * if the file exist
     * @param filePath
     * @return
     */
    boolean isFileExist(String filePath);

}
