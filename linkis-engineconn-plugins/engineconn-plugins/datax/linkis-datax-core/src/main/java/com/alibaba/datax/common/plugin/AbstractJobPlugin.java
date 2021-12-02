package com.alibaba.datax.common.plugin;

import org.apache.linkis.datax.common.constant.TransportType;

/**
 * Created by jingxing on 14-8-24.
 */
public abstract class AbstractJobPlugin extends AbstractPlugin {
    private TransportType transportType;

    public void setTransportType(TransportType transportType){
        this.transportType = transportType;
    }

    public TransportType getTransportType(){
        return this.transportType;
    }
    /**
     * @return the jobPluginCollector
     */
    public JobPluginCollector getJobPluginCollector() {
        return jobPluginCollector;
    }

    /**
     * @param jobPluginCollector the jobPluginCollector to set
     */
    public void setJobPluginCollector(
            JobPluginCollector jobPluginCollector) {
        this.jobPluginCollector = jobPluginCollector;
    }

    private JobPluginCollector jobPluginCollector;

}
