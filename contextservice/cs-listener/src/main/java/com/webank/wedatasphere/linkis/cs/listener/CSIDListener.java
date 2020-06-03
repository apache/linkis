package com.webank.wedatasphere.linkis.cs.listener;

import com.webank.wedatasphere.linkis.cs.listener.event.ContextIDEvent;

/**
 * @author peacewong
 * @date 2020/2/15 11:24
 */
public interface CSIDListener extends ContextAsyncEventListener {



    void onCSIDAccess(ContextIDEvent contextIDEvent);

    void onCSIDADD(ContextIDEvent contextIDEvent);

    void onCSIDRemoved(ContextIDEvent contextIDEvent);

}
