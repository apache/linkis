package com.alibaba.datax.plugin.reader.ftpreader;

import com.alibaba.datax.plugin.unstructuredstorage.PathMeta;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderUtil;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

public abstract class FtpHelper {

    /**
     *
     * @param ftpConnParams connection parameters
     */
    public abstract void loginFtpServer(FtpConnParams ftpConnParams);

    /**
     * @param
     * @return void
     * @throws
     * @Title: LogoutFtpServer
     * todo 方法名首字母
     * @Description: 断开与ftp服务器的连接
     */
    public abstract void logoutFtpServer();

    /**
     * @param @param  directoryPath
     * @param @return
     * @return boolean
     * @throws
     * @Title: isDirExist
     * @Description: 判断指定路径是否是目录
     */
    public abstract boolean isDirExist(String directoryPath);

    /**
     * @param @param  filePath
     * @param @return
     * @return boolean
     * @throws
     * @Title: isFileExist
     * @Description: 判断指定路径是否是文件
     */
    public abstract boolean isFileExist(String filePath);

    /**
     * @param @param  filePath
     * @param @return
     * @return boolean
     * @throws
     * @Title: isSymbolicLink
     * @Description: 判断指定路径是否是软链接
     */
    public abstract boolean isSymbolicLink(String filePath);

    /**
     * @param @param  directoryPath
     * @param @param  parentLevel 父目录的递归层数（首次为0）
     * @param @param  maxTraversalLevel 允许的最大递归层数
     * @param @return
     * @return HashSet<String>
     * @throws
     * @Title: getListFiles
     * @Description: 递归获取指定路径下符合条件的所有文件绝对路径
     */
    public abstract HashSet<String> getListFiles(String directoryPath, int parentLevel, int maxTraversalLevel);

    /**
     * @param @param  filePath
     * @param @return
     * @return InputStream
     * @throws
     * @Title: getInputStream
     * @Description: 获取指定路径的输入流
     */
    public abstract InputStream getInputStream(String filePath);

    /**
     * get file's last modify time
     * @param filePath
     * @return
     */
    public abstract long getLastModifyTIme(String filePath);

    /**
     * delete file
     * @param filePath
     */
    public abstract void deleteFile(String filePath);
    /**
     * @param @param  srcPaths 路径列表
     * @param @param  parentLevel 父目录的递归层数（首次为0）
     * @param @param  maxTraversalLevel 允许的最大递归层数
     * @param @return
     * @return HashSet<String>
     * @throws
     * @Title: getAllFiles
     * @Description: 获取指定路径列表下符合条件的所有文件的绝对路径
     */
    public HashSet<PathMeta> getAllFiles(List<String> srcPaths, int parentLevel, int maxTraversalLevel) {
        HashSet<PathMeta> sourceAllFiles = new HashSet<>();
        if (!srcPaths.isEmpty()) {
            for (String eachPath : srcPaths) {
                HashSet<String> listFiles = getListFiles(eachPath, parentLevel, maxTraversalLevel);
                String parent = eachPath;
                if(eachPath.contains("*") || eachPath.contains("?")){
                    parent = UnstructuredStorageReaderUtil.getRegexPathParentPath(eachPath);
                }
                for(String file : listFiles){
                    if(file.equals(parent)){
                        sourceAllFiles.add(new PathMeta(file, parent.substring(parent.lastIndexOf(IOUtils.DIR_SEPARATOR))));
                    }else{
                        sourceAllFiles.add(new PathMeta(file, file.substring(file.indexOf(parent) + parent.length())));
                    }
                }
            }
        }
        return sourceAllFiles;
    }

}
