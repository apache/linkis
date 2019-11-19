package com.webank.wedatasphere.linkis.metadata.domain.mdq.bo;

import java.util.Map;


public class MdqTableImportInfoBO {
    private Integer importType;
    private Map<String,String> args;
    // destination和source还没定义

    private String destination;

    private String source;

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

    public Integer getImportType() {
        return importType;
    }

    public void setImportType(Integer importType) {
        this.importType = importType;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }
}
