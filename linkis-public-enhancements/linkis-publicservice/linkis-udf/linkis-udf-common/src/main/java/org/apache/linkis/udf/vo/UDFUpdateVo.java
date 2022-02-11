package org.apache.linkis.udf.vo;

public class UDFUpdateVo {
    private Long id;
    private String udfName;
    private Integer udfType;
    private String path; //仅存储用户上一次上传的路径 作提示用
    private String registerFormat;
    private String useFormat;
    private String description;

    private Boolean isLoad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRegisterFormat() {
        return registerFormat;
    }

    public void setRegisterFormat(String registerFormat) {
        this.registerFormat = registerFormat;
    }

    public String getUseFormat() {
        return useFormat;
    }

    public void setUseFormat(String useFormat) {
        this.useFormat = useFormat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUdfName() {
        return udfName;
    }

    public void setUdfName(String udfName) {
        this.udfName = udfName;
    }

    public Integer getUdfType() {
        return udfType;
    }

    public void setUdfType(Integer udfType) {
        this.udfType = udfType;
    }

    public Boolean getLoad() {
        return isLoad;
    }

    public void setLoad(Boolean load) {
        isLoad = load;
    }
}
