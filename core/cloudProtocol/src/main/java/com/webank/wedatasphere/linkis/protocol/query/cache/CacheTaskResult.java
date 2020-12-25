package com.webank.wedatasphere.linkis.protocol.query.cache;

public class CacheTaskResult implements ResponseReadCache {

    private String resultLocation;

    public CacheTaskResult(String resultLocation) {
        this.resultLocation = resultLocation;
    }

    public String getResultLocation() {
        return resultLocation;
    }
}
