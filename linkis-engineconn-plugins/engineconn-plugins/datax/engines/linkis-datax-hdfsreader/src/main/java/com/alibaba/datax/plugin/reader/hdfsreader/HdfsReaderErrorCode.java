package com.alibaba.datax.plugin.reader.hdfsreader;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * error code
 * @author davidhua
 */
public enum HdfsReaderErrorCode implements ErrorCode {
    /**
     * BAD CONFIG VALUE
     */
    BAD_CONFIG_VALUE("HdfsReader-00", "The value of configuration is illegal(您配置的值不合法)"),
    /**
     * PATH NOT FIND ERROR
     */
    PATH_NOT_FIND_ERROR("HdfsReader-01", "Path is necessary(您未配置path值)"),
    /**
     * DEFAULT_FA NOT FIND ERROR
     */
    DEFAULT_FS_NOT_FIND_ERROR("HdfsReader-02", "defaultFS is necessary(您未配置defaultFS值)"),
    /**
     * ILLEGAL VALUE
     */
    ILLEGAL_VALUE("HdfsReader-03", "Value error(值错误)"),
    /**
     * CONFIG INVALID EXCEPTION
     */
    CONFIG_INVALID_EXCEPTION("HdfsReader-04", "The parameters' configuration is wrong(参数配置错误)"),
    /**
     * REQUIRED VALUE
     */
    REQUIRED_VALUE("HdfsReader-05", "Lost the required parameters(您缺失了必须填写的参数值)"),
    /**
     * NO INDEX VALUE
     */
    NO_INDEX_VALUE("HdfsReader-06", "No index value(没有 Index)"),
    /**
     * MIXED INDEX VALUE
     */
    MIXED_INDEX_VALUE("HdfsReader-07", "Mixed index value(index 和 value 混合)"),
    /**
     * EMPTY DIR EXCEPTION
     */
    EMPTY_DIR_EXCEPTION("HdfsReader-08", "The directory is empty(您尝试读取的文件目录为空)"),
    /**
     * PATH CONFIG ERROR
     */
    PATH_CONFIG_ERROR("HdfsReader-09", "The path format you configured is incorrect(您配置的path格式有误)"),
    /**
     * READ FILE ERROR
     */
    READ_FILE_ERROR("HdfsReader-10", "Error reading file(读取文件出错)"),
    /**
     * MALFORMED ORC ERROR
     */
    MALFORMED_ORC_ERROR("HdfsReader-10", "ORC file format exception(ORCFILE格式异常)"),
    /**
     * FILE TYPE ERROR
     */
    FILE_TYPE_ERROR("HdfsReader-11", "File type configuration is incorrect(文件类型配置错误)"),
    /**
     * FILE TYPE UNSUPPORTED
     */
    FILE_TYPE_UNSUPPORT("HdfsReader-12", "File type is unsupported(文件类型目前不支持)"),
    /**
     * KERBEROS LOGIN ERROR
     */
    KERBEROS_LOGIN_ERROR("HdfsReader-13", "KERBEROS authentication fail(KERBEROS认证失败)"),
    /**
     * READ SEQUENCE FILE ERROR
     */
    READ_SEQUENCEFILE_ERROR("HdfsReader-14", "Error reading SequenceFile(读取SequenceFile文件出错)"),
    /**
     * READ RC FILE ERROR
     */
    READ_RCFILE_ERROR("HdfsReader-15", "Error reading RCFile(读取RCFile文件出错)"),
    /**
     * READ HFILE ERROR
     */
    READ_HFILE_ERROR("HdfsReader-21", "Error reading HFile(读取HFile文件错误)"),
    /**
     * HDFS PROXY ERROR
     */
    HDFS_PROXY_ERROR("HdfsReader-16", "Fail to create HDFS PROXY(创建HDFS PROXY失败)"),
    /**
     * CONNECT HDFS TO ERROR
     */
    CONNECT_HDFS_IO_ERROR("HdfsReader-17", "Occurred exception when building connection to HDFS(与HDFS建立连接时出现IO异常)"),
    /**
     * HDFS RENAME FILE ERROR
     */
    HDFS_RENAME_FILE_ERROR("HdfsWriter-08", "Fail to move file to path configured(将文件移动到配置路径失败)"),
    /**
     * PATH NOT FOUND
     */
    PATH_NOT_FOUND("HdfsReader-18", "Path not found(找不到路径)"),
    /**
     * OBTAIN METADATA ERROR
     */
    OBTAIN_METADATA_ERROR("HdfsReader-19","Fail to obtain metadata(获取元信息失败)"),
    /**
     * UPDATE CONFIG ERROR (update by hive meta)
     */
    UPDATE_CONFIG_ERROR("HdfsReader-20", "Fail to update configuration dynamically(动态更新任务配置失败)");

    private final String code;
    private final String description;

    private HdfsReaderErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s]. ", this.code,
                this.description);
    }
}