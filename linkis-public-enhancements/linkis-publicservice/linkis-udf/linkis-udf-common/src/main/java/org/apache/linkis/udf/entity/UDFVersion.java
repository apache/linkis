package org.apache.linkis.udf.entity;

import java.util.Date;

public class UDFVersion {
    private Long id;
    private Long udfId;
    private String path; // 仅存储用户上一次上传的路径 作提示用
    private String bmlResourceId;
    private String bmlResourceVersion;
    private Boolean isPublished; // 共享udf被使用的是已发布的最新版本
    private String registerFormat;
    private String useFormat;
    private String description;
    private Date createTime;

    private String md5;

    public UDFVersion() {}

    public UDFVersion(
            Long id,
            Long udfId,
            String path,
            String bmlResourceId,
            String bmlResourceVersion,
            Boolean isPublished,
            String registerFormat,
            String useFormat,
            String description,
            Date createTime,
            String md5) {
        this.id = id;
        this.udfId = udfId;
        this.path = path;
        this.bmlResourceId = bmlResourceId;
        this.bmlResourceVersion = bmlResourceVersion;
        this.isPublished = isPublished;
        this.registerFormat = registerFormat;
        this.useFormat = useFormat;
        this.description = description;
        this.createTime = createTime;
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUdfId(Long udfId) {
        this.udfId = udfId;
    }

    public Long getUdfId() {
        return udfId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setBmlResourceId(String bmlResourceId) {
        this.bmlResourceId = bmlResourceId;
    }

    public String getBmlResourceId() {
        return bmlResourceId;
    }

    public void setBmlResourceVersion(String bmlResourceVersion) {
        this.bmlResourceVersion = bmlResourceVersion;
    }

    public String getBmlResourceVersion() {
        return bmlResourceVersion;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public Boolean getPublished() {
        return isPublished;
    }

    public void setRegisterFormat(String registerFormat) {
        this.registerFormat = registerFormat;
    }

    public String getRegisterFormat() {
        return registerFormat;
    }

    public void setUseFormat(String useFormat) {
        this.useFormat = useFormat;
    }

    public String getUseFormat() {
        return useFormat;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
