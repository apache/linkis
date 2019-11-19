package com.webank.wedatasphere.linkis.metadata.type;


public enum Lifecycle {
    /**
     *
     */
    Permanent("永久"),
    Todday("当天有效"),
    ThisWeek("一周有效"),
    ThisMonth("一月有效"),
    HalfYear("半年有效");
    private String name;

    Lifecycle(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
