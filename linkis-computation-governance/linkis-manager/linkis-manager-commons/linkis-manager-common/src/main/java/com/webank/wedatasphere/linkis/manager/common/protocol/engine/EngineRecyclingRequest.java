package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.manager.common.entity.recycle.RecyclingRule;

import java.util.List;


public class EngineRecyclingRequest implements EngineRequest {

    private String user;

    private List<RecyclingRule> recyclingRuleList;

    @Override
    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<RecyclingRule> getRecyclingRuleList() {
        return recyclingRuleList;
    }

    public void setRecyclingRuleList(List<RecyclingRule> recyclingRuleList) {
        this.recyclingRuleList = recyclingRuleList;
    }
}
