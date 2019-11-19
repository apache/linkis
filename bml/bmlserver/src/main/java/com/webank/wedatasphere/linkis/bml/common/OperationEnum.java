package com.webank.wedatasphere.linkis.bml.common;

/**
 * @author v_wblwyan
 * @date 2019-9-17
 */
public enum OperationEnum {
    /**
     * 任务操作类型
     */
    UPLOAD("upload", 0),
    UPDATE("update", 1),
    DOWNLOAD("download", 2),
    DELETE_VERSION("deleteVersion", 3),
    DELETE_RESOURCE("deleteResource", 4),
    DELETE_RESOURCES("deleteResources", 5);
    private String value;
    private int id;
    private OperationEnum(String value, int id){
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
