package com.webank.wedatasphere.linkis.metadata.domain.mdq.bo;


public class MdqTableBaseInfoBO {
    private BaseBO base;
    private ModelBO model;
    private ApplicationBO application;

    public BaseBO getBase() {
        return base;
    }

    public void setBase(BaseBO base) {
        this.base = base;
    }

    public ModelBO getModel() {
        return model;
    }

    public void setModel(ModelBO model) {
        this.model = model;
    }

    public ApplicationBO getApplication() {
        return application;
    }

    public void setApplication(ApplicationBO application) {
        this.application = application;
    }
}
