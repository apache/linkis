package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.common.ServiceInstance;


public class DefaultScoreServiceInstance implements ScoreServiceInstance {

    private double score;

    private ServiceInstance serviceInstance;

    @Override
    public double getScore() {
        return this.score;
    }

    @Override
    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public ServiceInstance getServiceInstance() {
        return this.serviceInstance;
    }

    @Override
    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
