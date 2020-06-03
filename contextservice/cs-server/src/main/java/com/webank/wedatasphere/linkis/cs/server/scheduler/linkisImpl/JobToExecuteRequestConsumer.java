package com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl;

import java.util.function.Consumer;

/**
 * Created by patinousward on 2020/2/22.
 */
public interface JobToExecuteRequestConsumer<T> {

    T get();

    void set(T t);

    Consumer<T> getConsumer();

    void setConsuemr(Consumer<T> tConsumer);
}
