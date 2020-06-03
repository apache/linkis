package com.webank.wedatasphere.linkis.cs.client.test.listener;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.cs.client.listener.ContextKeyListener;

/**
 * created by cooperyang on 2020/3/1
 * Description:
 */
public class CommonContextKeyListener extends ContextKeyListener {




    @Override
    public void onContextCreated(Event event) {
        System.out.println("it is created");
    }

    @Override
    public void onContextUpdated(Event event) {
        super.onContextUpdated(event);
        System.out.println("it is updated");
    }

    @Override
    public void onEventError(Event event, Throwable t) {
        System.out.println("it is not ok");
    }
}
