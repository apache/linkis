package com.webank.wedatasphere.linkis.cs.server.scheduler;

/**
 * Created by patinousward on 2020/2/18.
 */
public interface HttpPriorityJob extends HttpJob {

    int getPriority();

}
