package com.webank.wedatasphere.linkis.metadata.domain.mdq.vo;




public class MdqTableBaseInfoVO {
    private BaseVO base;
    private ModelVO model;
    private ApplicationVO application;

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public ModelVO getModel() {
        return model;
    }

    public void setModel(ModelVO model) {
        this.model = model;
    }

    public ApplicationVO getApplication() {
        return application;
    }

    public void setApplication(ApplicationVO application) {
        this.application = application;
    }
}
