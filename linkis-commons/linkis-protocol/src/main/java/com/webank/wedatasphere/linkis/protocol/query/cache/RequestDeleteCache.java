package com.webank.wedatasphere.linkis.protocol.query.cache;

import com.webank.wedatasphere.linkis.protocol.query.QueryProtocol;

import java.util.List;

public class RequestDeleteCache implements QueryProtocol {

    private String executionContent;
    private String user;
    private List<String> labelsStr;

    public RequestDeleteCache(String executionContent, String user, List<String> labelsStr) {
        this.executionContent = executionContent;
        this.user = user;
        this.labelsStr = labelsStr;
    }

    public String getExecutionContent() {
        return executionContent;
    }

    public String getUser() {
        return user;
    }

    public List<String> getLabelsStr() {
        return labelsStr;
    }
}
