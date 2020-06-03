package com.webank.wedatasphere.linkis.cs.server.protocol;

/**
 * Created by patinousward on 2020/2/18.
 */
public interface HttpResponseProtocol<T> extends HttpProtocol {

    void waitForComplete() throws InterruptedException;

    void waitTimeEnd(long mills) throws InterruptedException;

    void notifyJob();

    T get();

    void set(T t);

    Object getResponseData();

    void setResponseData(Object responseData);
}
