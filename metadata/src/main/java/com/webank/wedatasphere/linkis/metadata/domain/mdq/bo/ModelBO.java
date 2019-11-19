package com.webank.wedatasphere.linkis.metadata.domain.mdq.bo;


public class ModelBO {
    private Integer lifecycle;
    private Integer modelLevel;
    private Integer useWay;
    private Boolean isExternalUse;

    public Integer getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Integer lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Integer getModelLevel() {
        return modelLevel;
    }

    public void setModelLevel(Integer modelLevel) {
        this.modelLevel = modelLevel;
    }

    public Integer getUseWay() {
        return useWay;
    }

    public void setUseWay(Integer useWay) {
        this.useWay = useWay;
    }

    public Boolean getExternalUse() {
        return isExternalUse;
    }

    public void setExternalUse(Boolean externalUse) {
        isExternalUse = externalUse;
    }
}
