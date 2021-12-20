package com.alibaba.datax.core;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.container.communicator.AbstractContainerCommunicator;
import org.apache.commons.lang3.Validate;

public abstract class AbstractContainer {
    protected Configuration configuration;

    protected AbstractContainerCommunicator containerCommunicator;

    public AbstractContainer(Configuration configuration) {
        Validate.notNull(configuration, "Configuration can not be null.");

        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public AbstractContainerCommunicator getContainerCommunicator() {
        return containerCommunicator;
    }

    public void setContainerCommunicator(AbstractContainerCommunicator containerCommunicator) {
        this.containerCommunicator = containerCommunicator;
    }

    public abstract void start();

    public void shutdown(){

    }

    public void generateMetric(){

    }

    public Communication getScheduleReport(){
        return new Communication();
    }
}
