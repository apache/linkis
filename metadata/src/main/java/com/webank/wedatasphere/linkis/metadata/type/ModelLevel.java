package com.webank.wedatasphere.linkis.metadata.type;


public enum ModelLevel {
    /**
     *
     */
    ODS("原始数据层"),DWD("明细数据层"),DWS("汇总数据层"),ADS("应用数据层");

    private String name;

    ModelLevel(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
