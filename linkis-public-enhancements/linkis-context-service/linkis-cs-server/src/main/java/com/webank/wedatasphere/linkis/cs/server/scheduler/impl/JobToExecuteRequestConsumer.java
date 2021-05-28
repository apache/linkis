package com.webank.wedatasphere.linkis.cs.server.scheduler.impl;

import java.util.function.Consumer;

public interface JobToExecuteRequestConsumer<T> {

    T get();

    void set(T t);

    Consumer<T> getConsumer();

    void setConsuemr(Consumer<T> tConsumer);
}
