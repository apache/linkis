package com.webank.wedatasphere.linkis.protocol.label;

import com.webank.wedatasphere.linkis.protocol.util.ImmutablePair;

import java.util.List;
import java.util.Map;


public class InsLabelQueryResponse {

    public InsLabelQueryResponse() {}

    public InsLabelQueryResponse(List<ImmutablePair<String, String>> labelList) {
        this.labelList = labelList;
    }

    private List<ImmutablePair<String, String>> labelList;

    public List<ImmutablePair<String, String>> getLabelList() {
        return labelList;
    }

    public InsLabelQueryResponse setLabelList(List<ImmutablePair<String, String>> labelList) {
        this.labelList = labelList;
        return this;
    }

}
