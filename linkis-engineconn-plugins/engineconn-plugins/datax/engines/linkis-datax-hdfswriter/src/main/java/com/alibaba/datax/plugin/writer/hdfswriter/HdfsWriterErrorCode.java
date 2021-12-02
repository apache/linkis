package com.alibaba.datax.plugin.writer.hdfswriter;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * Created by shf on 15/10/8.
 */
public enum HdfsWriterErrorCode implements ErrorCode {
    /**
     * CONFIG INVALID EXCEPTION
     */
    CONFIG_INVALID_EXCEPTION("HdfsWriter-00", "The parameters' configuration is wrong(您的参数配置错误)"),
    /**
     * REQUIRED VALUE
     */
    REQUIRED_VALUE("HdfsWriter-01", "Lost the required parameters(您缺失了必须填写的参数值)"),
    /**
     * ILLEGAL VALUE
     */
    ILLEGAL_VALUE("HdfsWriter-02", "The value of configuration is illegal(您填写的参数值不合法)"),
    /**
     * CHARSET ERROR
     */
    WRITER_FILE_WITH_CHARSET_ERROR("HdfsWriter-03", "Character Error(您配置的编码未能正常写入)"),
    /**
     * WRITE FILE IO ERROR
     */
    WRITE_FILE_IO_ERROR("HdfsWriter-04", "Configuration Exception while writing(您配置的文件在写入时出现IO异常)"),
    /**
     * WRITE RUNTIME EXCEPTION
     */
    WRITER_RUNTIME_EXCEPTION("HdfsWriter-05", "RuntimeException(出现运行时异常, 请联系我们)"),
    /**
     * CONNECT HDFS TO ERROR
     */
    CONNECT_HDFS_IO_ERROR("HdfsWriter-06", "Occurred exception when building connection to HDFS(与HDFS建立连接时出现IO异常)"),
    /**
     * COLUMN REQUIRED VALUE
     */
    COLUMN_REQUIRED_VALUE("HdfsWriter-07", "Required value in column configuration(您column配置中缺失了必须填写的参数值)"),
    /**
     * HDFS RENAME FILE ERROR
     */
    HDFS_RENAME_FILE_ERROR("HdfsWriter-08", "Fail to move file to path configured(将文件移动到配置路径失败)"),
    /**
     * KERBEROS LOGIN ERROR
     */
    KERBEROS_LOGIN_ERROR("HdfsWriter-09", "KERBEROS authentication fail(KERBEROS认证失败)"),
    /**
     * HDFS PROXY ERROR
     */
    HDFS_PROXY_ERROR("HdfsWriter-10", "Fail to create HDFS PROXY(创建HDFS PROXY失败)"),
    /**
     * ADD PARTITION ERROR
     */
    ADD_PARTITION_ERROR("HdfsWriter-11", "Fail to add partition(增加partition失败)"),
    /**
     * UPDATE HIVE META
     */
    UPDATE_HIVE_META("HdfsWriter-12", "Fail to update hive meta更新HIVE元信息失败)"),
    /**
     * UPDATE CONFIG ERROR(update by hive meta)
     */
    UPDATE_CONFIG_ERROR("HdfsWriter-13", "Fail to update configuration dynamically(动态更新任务配置失败)");


    private final String code;
    private final String description;

    private HdfsWriterErrorCode(String code, String description) {
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
        return String.format("Code:[%s], Description:[%s].", this.code,
                this.description);
    }

}
