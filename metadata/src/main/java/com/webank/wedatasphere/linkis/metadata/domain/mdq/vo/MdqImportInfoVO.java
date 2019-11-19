package com.webank.wedatasphere.linkis.metadata.domain.mdq.vo;

import java.util.Map;

public class MdqImportInfoVO {

    /**
     * 0 表示 csv
     * 1 表示 excel
     * 2 表示 hive
     */
    private Integer importType;
    private Map<String, Object> args;
    private String destination;
    private String source;

    public int getImportType() {
        return importType;
    }

    public void setImportType(int importType) {
        this.importType = importType;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "MdqImportInfoVO{" +
                "importType=" + importType +
                ", args=" + args +
                ", destination='" + destination + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
