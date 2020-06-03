package com.webank.wedatasphere.linkis.cs.client.listener;

import com.webank.wedatasphere.linkis.cs.listener.callback.imp.ContextKeyValueBean;

import java.io.Closeable;
import java.util.List;

/**
 * created by cooperyang on 2020/2/11
 * Description:
 */
public interface HeartBeater extends Closeable {
    /**
     *
     */
    public void heartBeat();
    public void dealCallBack(List<ContextKeyValueBean> kvs);

    public void start();

}
