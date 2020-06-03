package com.webank.wedatasphere.linkis.cs.server.service;

import com.webank.wedatasphere.linkis.cs.common.exception.CSWarnException;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpJob;

import java.io.Closeable;

/**
 * Created by patinousward on 2020/2/18.
 */
public interface Service extends Closeable {

    void init();

    void start();

    String getName();

    boolean ifAccept(HttpJob job);

    void accept(HttpJob job) throws CSWarnException;
}
