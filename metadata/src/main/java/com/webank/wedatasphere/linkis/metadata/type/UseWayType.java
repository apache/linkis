package com.webank.wedatasphere.linkis.metadata.type;


public enum UseWayType {
    /**
     *
     */
    OnceWriteMultiRead("一次写入多次读"),
    CRUD("增删改查"),
    MultiOverwrite("多次覆盖写"),
    OnceWriteOccasionllyRead("一次写偶尔读");

    private String name;

    UseWayType(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
