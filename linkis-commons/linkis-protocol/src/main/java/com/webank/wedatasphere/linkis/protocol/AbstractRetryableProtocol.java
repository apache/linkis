package com.webank.wedatasphere.linkis.protocol;


public class AbstractRetryableProtocol implements RetryableProtocol {

    @Override
    public long maxPeriod() {
        return 3000L;
    }

    @Override
    public Class<? extends Throwable>[] retryExceptions() {
        return new Class[]{};
    }

    @Override
    public int retryNum() {
        return 2;
    }

    @Override
    public long period() {
        return 1000L;
    }

}
