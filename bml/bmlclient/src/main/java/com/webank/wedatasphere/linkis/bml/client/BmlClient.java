package com.webank.wedatasphere.linkis.bml.client;

import com.webank.wedatasphere.linkis.bml.protocol.*;

import java.io.InputStream;

/**
 * created by cooperyang on 2019/5/15
 * Description:
 */
public interface BmlClient {
    /**
     * 传入resourceID bmlclient会resource的输入流,如果不传入version,默认返回最新的版本
     * @param resourceID resourceID
     * @return InputStream
     */
    public BmlDownloadResponse downloadResource(String user, String resourceID);

    public BmlDownloadResponse downloadResource(String user, String resourceId, String version);


    public BmlDownloadResponse downloadResource(String user, String resourceId, String version, String path, boolean overwrite);


    /**
     * relateResource方法将targetFilePath路径的文件关联到resourceID下面
     * targetFilePath需要包括schema，如果不包含schema，默认是hdfs
     * @param resourceID resourceID
     * @param targetFilePath 指定文件目录
     * @return BmlRelateResult  包含resourceId和新的version
     */
    public BmlRelateResponse relateResource(String resourceID, String targetFilePath);


    /**
     * 更新资源信息
     * @param resourceID 资源id
     * @param filePath 目标文件路径
     * @return resourceId 新的版本信息
     */

    public BmlUpdateResponse updateResource(String user, String resourceID, String filePath);

    public BmlUpdateResponse updateResource(String user, String resourceID, String filePath, InputStream inputStream);




    /**
     * 上传资源,用户指定输入流
     * @param user 用户名
     * @param filePath 上传的资源的路径
     * @param inputStream  上传资源的输入流
     * @return 包含resourceId和version
     */
    public BmlUploadResponse uploadResource(String user, String filePath, InputStream inputStream);

    /**
     * 上传文件，用户指定文件路径，客户端自动获取输入流
     * @param user 用户名
     * @param filePath 文件路径
     * @return 包含resourceId和version
     */
    public BmlUploadResponse uploadResource(String user, String filePath);





    /**
     * 获取resource的所有版本
     * @param user 用户名
     * @param resourceId 资源Id
     * @return resourceId对应下的所有版本信息
     */
    public BmlResourceVersionsResponse getVersions(String user, String resourceId);


}
