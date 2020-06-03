package com.webank.wedatasphere.linkis.cs.server.scheduler;

import com.webank.wedatasphere.linkis.cs.server.service.Service;

/**
 * Created by patinousward on 2020/2/21.
 */
public interface CsScheduler {

    void addService(Service service);

    Service[] getServices();

    void sumbit(HttpJob job) throws InterruptedException;
}
